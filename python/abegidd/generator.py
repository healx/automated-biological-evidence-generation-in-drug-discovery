import logging
import re
from typing import Collection, List, Optional, Tuple

import numpy as np
from attr import define, field

from abegidd.entities import (
    Atom,
    EdgeType,
    EvidenceChain,
    EvidenceChainScoredPath,
    EvidenceChainsList,
    Explanation,
    Graph,
    MetaPath,
    MetapathEdgeData,
    PredictedEntity,
    Prediction,
    Rule,
)
from abegidd.graph import graph_from_triples, paths_with_metapath
from abegidd.iterables import first
from abegidd.path_scorer import CachedArrayIndexLookup, DegreeWeightedPathScorer

logger = logging.getLogger(__name__)


@define
class _RuleData:
    """
    Single rule data container used to pass info to the
    worker function that runs path discovery using data provided
    in this container
    """

    start_node: str
    end_node: str
    rule: Rule


@define
class _PredictedRulesData:
    """
    Data container used by the evidence chain generator to keep
    information about the data needed for parallel path discovery
    """

    predicted: str
    score: float
    rules: List[_RuleData]


@define
class EvidenceChainsGenerator:
    """
    Main class utility for generation evidence chain using the
    prediction and explanation sets. Each chain is also scored
    using a degree weighted path scoring.
    """

    triples: List[Tuple[str, str, str]]
    predictions: Collection[Prediction]
    explanations: Collection[Explanation]
    _scoring: DegreeWeightedPathScorer = field(alias="_scoring", init=False)
    _index_lookup: CachedArrayIndexLookup = field(alias="_index_lookup", init=False)
    _graph: Graph = field(alias="_graph", init=False)

    def __attrs_post_init__(self):
        self._scoring = DegreeWeightedPathScorer(self.triples)
        self._index_lookup = CachedArrayIndexLookup(np.array(self.triples))
        self._graph = graph_from_triples(self.triples)

    @staticmethod
    def _chain_start_end_nodes(
        explanation_attr: str,
        explanation: Explanation,
        rule: Rule,
    ) -> Tuple[str, str]:
        """
        Resolves the start/end nodes for the explanation and rule depending
        on the provided explanation attr

        :param explanation_attr: explanation attr
        :param explanation: explanation object
        :param rule: rule object
        :return: tuple containing start/end node indices resolved from the explanation,
            rule and the provided explanation attr
        """
        if explanation_attr == "rule_heads":
            start = rule.predicted_entity
            end = explanation.tail
        elif explanation_attr == "rule_tails":
            start = explanation.head
            end = rule.predicted_entity
        else:
            raise ValueError(
                "Invalid explanation attr used. Use `rule_heads` or `rule_tails`"
            )

        return start, end

    def _chain_from_rule(
        self, start_node: str, end_node: str, rule: Rule, max_length: int
    ) -> Optional[EvidenceChain]:
        """
        Gets all chains from a single anyburl rule

        :param start_node: chains start_node
        :param end_node: chain end node index
        :param rule: anyburl rule object`
        :param max_length: max length of the chain`
        :return: evidence chains object
        """

        chains = []
        # the metapath must have at least 2 atoms
        if len(rule.body_atoms) < 2 or len(rule.body_atoms) > max_length:
            return None

        meta_path = _metapath_from_atoms(rule.body_atoms)
        paths = paths_with_metapath(
            self._graph,
            meta_path,
            start_node,
            end_node,
            self._index_lookup,
        )

        # go over all paths, score them and resolved the path nodes
        # make sure to keep the ordering of node indices within a path!
        for path in paths:
            node_path = [first(pair) for pair in path] + [path[-1][1]]

            if len(node_path) != len(meta_path) + 1:
                continue

            chains.append(
                EvidenceChainScoredPath(
                    path_score=self._scoring.path(node_path),
                    path_nodes=node_path,
                )
            )

        # sort chains by score in descending order
        if len(chains) > 0:
            chains = sorted(chains, key=lambda e: e.path_score, reverse=True)

        return EvidenceChain(meta_path.edges, start_node, end_node, chains)

    def _get_rule_data_from_explanations(
        self, explanation_attr: str, pred_node: PredictedEntity
    ) -> List[_RuleData]:
        """
        Fetches the explanations using the explanation attr, and
        creates rule  data that will be used in for chain
        processing

        :param explanation_attr: which explanation rule attr to match either
             rule_heads or rule_tails
        :param pred_node: predicted node object
        :return: list of rule data objects
        """
        rules_data = []
        for exp in self.explanations:
            for rule in getattr(exp, explanation_attr):
                # if the explanation rule predicted entity matches the
                # entity from the prediction and if the rule is not a constant
                # rule meaning rule starts with c(X,Y) <= or c(Y, X) <= then
                # this rule will be used for evidence chains discovery
                if (
                    rule.predicted_entity == pred_node.entity
                    and rule.head_atom.head in ("X", "Y")
                    and rule.head_atom.tail in ("X", "Y")
                ):
                    start, end = self._chain_start_end_nodes(
                        explanation_attr, exp, rule
                    )
                    rules_data.append(
                        _RuleData(
                            start_node=start,
                            end_node=end,
                            rule=rule,
                        )
                    )
        return rules_data

    def _prepare_rules_for_chain_resolution(
        self, prediction_attr: str, explanation_attr: str
    ) -> List[_PredictedRulesData]:
        """
        Prepares the rules that needs to undergo metapath chain resolution.

        :param prediction_attr: which prediction to get head/tail
        :param explanation_attr: which explanation rule attr to match either
             rule_heads or rule_tails
        :return: list containing data to be processed
        """
        predicted_rules: List[_PredictedRulesData] = []
        for pred in self.predictions:
            for pred_node in getattr(pred, prediction_attr):
                rules_data = self._get_rule_data_from_explanations(
                    explanation_attr, pred_node
                )
                predicted_rules.append(
                    _PredictedRulesData(
                        predicted=pred_node.entity,
                        score=pred_node.score,
                        rules=rules_data,
                    )
                )
        return predicted_rules

    def _get_chains(
        self,
        prediction_attr: str,
        explanation_attr: str,
        max_length: int = 4,
        top_k_paths: Optional[int] = None,
    ) -> List[EvidenceChainsList]:
        """
        Returns all chains for predicted head or tails depending on the
        prediction and explanation attr

        :param prediction_attr: which prediction to get head/tail
        :param explanation_attr: which explanation rule attr to match either
             rule_heads or rule_tails
        :param max_length: max length of the chain, defaults to 4
        :param top_k_paths: how many paths to return, across all chains,
             default None for all
        """

        def _is_done(_total_paths):
            # if we discovered more than top_k paths - the generator is done
            if top_k_paths is not None and _total_paths >= top_k_paths:
                return True
            return False

        predicted_rules = self._prepare_rules_for_chain_resolution(
            prediction_attr, explanation_attr
        )
        evidence_chains: List[EvidenceChainsList] = []

        num_rules = len(predicted_rules)
        logger.info("Generating ev chains for [%s] predictions", num_rules)

        for pred_idx, predicted in enumerate(predicted_rules):
            logger.info(
                "Processing prediction index=[%s], out of total=[%s]",
                pred_idx,
                num_rules,
            )
            # counter used to limit the maximum number of paths
            total_paths = 0
            rule_evidence_chains = []

            for rule_data in predicted.rules:
                if _is_done(total_paths):
                    break

                chain = self._chain_from_rule(
                    rule_data.start_node,
                    rule_data.end_node,
                    rule_data.rule,
                    max_length,
                )

                # if the chain is None - just skip it
                # nothing to be saved in the output
                if chain is None:
                    continue

                rule_evidence_chains.append(chain)
                total_paths += len(chain.paths)

            if len(rule_evidence_chains) > 0:
                evidence_chains.append(
                    EvidenceChainsList(
                        prediction=predicted.predicted,
                        prediction_score=predicted.score,
                        evidence_chains=rule_evidence_chains,
                    )
                )

        return evidence_chains

    def get_chains_for_predicted_heads(
        self, max_length: int = 4, top_k_paths: Optional[int] = None
    ) -> List[EvidenceChainsList]:
        """
        Returns all chains for predicted heads

        :param max_length: max length of the chain, defaults to 4
        :param top_k_paths: how many paths to return, across all chains,
             default None for all
        :return: list of all evidence chains for predicted heads
        """

        return self._get_chains(
            max_length=max_length,
            top_k_paths=top_k_paths,
            prediction_attr="predicted_heads",
            explanation_attr="rule_heads",
        )

    def get_chains_for_predicted_tails(
        self, max_length: int = 4, top_k_paths: Optional[int] = None
    ) -> List[EvidenceChainsList]:
        """
        Returns all chains for predicted tails

        :param max_length: max length of the chain, defaults to 4
        :param top_k_paths: how many paths to return, across all chains,
             default None for all
        :return: list of all evidence chains for predicted tails
        """

        return self._get_chains(
            max_length=max_length,
            top_k_paths=top_k_paths,
            prediction_attr="predicted_tails",
            explanation_attr="rule_tails",
        )


def _metapath_from_atoms(atoms: List[Atom]) -> MetaPath:
    """
    Creates a metapath used to generate evidence chains path
    from anyburl atoms.

    :param atoms: list of anyburl rule atoms
    :return: metapath object
    """

    # metapath can be constructed of at least two atoms
    # two anyburl atoms correspond to three edges needed for the metapath
    if len(atoms) < 2:
        raise ValueError("Metapath can be constructed of at least two atoms!")

    # edgeType class is an attr(frozen=True) class therefore we
    # prepare the data as dicts and assign all edges as forward
    # later we iterate over this data and reverse the edges accordingly
    # prior creating proper EdgeTypes objects
    edge_data = []
    for atom in atoms:
        head_type, pref_name, tail_type = split_edge_string(atom.edge_type)
        edge_data.append(
            MetapathEdgeData(
                head=atom.head,
                tail=atom.tail,
                head_type=head_type.upper(),
                tail_type=tail_type.upper(),
                pref_name=pref_name,
                label=atom.edge_type,
            )
        )

    # correct the edge direction
    for _first, _second in zip(edge_data, edge_data[1:]):
        MetapathEdgeData.direct_metapath_edges(_first, _second)

    meta_path_edge_types = [edge_type.to_edge() for edge_type in edge_data]

    # We disable the validation below, this is an equivalent safety check to avoid
    # assertion errors because of unequal custom Enum types (values are always equal)
    for first_edge, second_edge in zip(meta_path_edge_types, meta_path_edge_types[1:]):
        assert _check_metapath_edges_match(
            first_edge, second_edge
        ), f"MetaPath edges do not match:\n{first_edge}vs.\n{second_edge}"

    return MetaPath(meta_path_edge_types)


SPLIT_EDGE_STRING_RE = re.compile(r"([A-Z_]+)_([a-z_]+)_([A-Z_]+)")


def split_edge_string(edge_string: str) -> Tuple[str, str, str]:
    try:
        (matches,) = re.findall(SPLIT_EDGE_STRING_RE, edge_string)
    except ValueError:
        raise ValueError("checkout %s", edge_string)
    assert len(matches) == 3, f"uh oh {edge_string}"
    return matches


SPLIT_NODE_STRING_RE = re.compile(r"([(),\-0-9a-z_]+)_([A-Z_]+)")


def split_node_string(node_string: str) -> Tuple[str, str]:
    try:
        (matches,) = re.findall(SPLIT_NODE_STRING_RE, node_string)
    except ValueError:
        raise ValueError("checkout %s", node_string)
    assert len(matches) == 2, f"uh oh {node_string}"
    return matches


def _check_metapath_edges_match(first_edge: EdgeType, second_edge: EdgeType) -> bool:
    # We test 4 cases;
    #   A) first edge (if forward) tail matches second edge head (if forward)
    #   B) first edge (if forward) tail matches second edge tail (if not forward)
    #   C) first edge (if not forward) head matches second edge head (if forward)
    #   D) first edge (if not forward) head matches second edge tail (if not
    #       forward)
    # case A
    if first_edge.forward and second_edge.forward:
        return first_edge.tail_type == second_edge.head_type

    # case B
    if first_edge.forward and not second_edge.forward:
        return first_edge.tail_type == second_edge.tail_type

    # case C
    if not first_edge.forward and second_edge.forward:
        return first_edge.head_type == second_edge.head_type

    # case D
    if not first_edge.forward and not second_edge.forward:
        return first_edge.head_type == second_edge.tail_type

    raise Exception("Unreachable code")
