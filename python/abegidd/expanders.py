import logging
from itertools import groupby
from typing import Dict, Generator, List, Tuple

from _operator import itemgetter

from abegidd.entities import JsonChain
from abegidd.iterables import first


def build_deductive_chains(
    chains: List[JsonChain], prioritised_edge_names: List[str]
) -> Generator[JsonChain, None, None]:
    """
    Build additional evidence chains using prioritised edge types when we have multiple
    chains involving the same nodes
    :param chains: evidence chains
    :param prioritised_edge_names: edge names, in priority order
    :return: extra 'deduced' chains
    """
    edge_name_priorities_lookup = {
        edge_type: rank for rank, edge_type in enumerate(prioritised_edge_names)
    }

    for prediction, prediction_chains in group_chains_by_prediction(chains):
        for same_node_chains in _group_chains_with_the_same_nodes(prediction_chains):
            yield _build_deduced_chain(same_node_chains, edge_name_priorities_lookup)


def _group_chains_with_the_same_nodes(chains: List[JsonChain]) -> List[List[JsonChain]]:
    """
    Only find groups of chains with the same nodes (chains with no other matching chains
    are not returned)
    :param chains:
    :return: grouped chains
    """
    sorted_chains = sorted(chains, key=itemgetter("path"))
    grouped_chains = [
        list(chain_group)
        for _, chain_group in groupby(sorted_chains, key=itemgetter("path"))
    ]
    return [chain_group for chain_group in grouped_chains if len(chain_group) > 1]


def group_chains_by_prediction(
    chains=Generator[JsonChain, None, None]
) -> Generator[Tuple[str, List[JsonChain]], None, None]:
    """
    Returns a generator which whill lazily return tuples with the prediction as a string
    and an iterable containing the chains
    :param chains:
    :yield: prediction and iterable of chains
    """
    sorted_chains = sorted(chains, key=itemgetter("prediction"))
    for prediction, group in groupby(sorted_chains, itemgetter("prediction")):
        logging.info("Grouping %s chains", prediction)
        yield prediction, list(group)


def _build_deduced_chain(
    same_node_chains: List[JsonChain], edge_name_priorities_lookup: Dict[str, int]
) -> JsonChain:
    base_chain = first(same_node_chains)

    # for each edge type set in same_node_chains ie
    # (
    #   {'label': 'COMPOUND_causes_PHENOTYPE', 'reversed': False},
    #   {'label': 'COMPOUND_causes_PHENOTYPE', 'reversed': False}
    # )
    # (
    #   {'label': 'COMPOUND_causes_PHENOTYPE', 'reversed': True},
    #   {'label': 'COMPOUND_treats_PHENOTYPE', 'reversed': True}
    # )
    base_chain["metapath"] = [
        first(
            sorted(
                edge_type_set,
                key=lambda metapath_edge: edge_name_priorities_lookup.get(
                    metapath_edge["label"], 100
                ),
            )
        )
        for edge_type_set in zip(*[chain["metapath"] for chain in same_node_chains])
    ]

    return base_chain
