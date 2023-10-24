import re
from typing import List, Tuple

from attrs import define, field, frozen


@frozen
class Atom:
    """
    AnyBURL rule atom class. Used to represent the atomic entry of the
    anyburl rule. Examples may include:

        COMPOUND_DISEASE_treats(X,Y)
        PATHWAY_COMPOUND_involves(A,B)
        etc.

    The semantics include a relation, head node labelled with X or Y and free
    variables A,B,C ... Z. Each atom can be negated with a ! sign
    """

    head: str
    edge_type: str
    tail: str
    negated: bool


@frozen
class Explanation:
    """
    Predictions for any head, relation, tail triple
    """

    head: str
    tail: str
    edge_type: str
    rule_heads: List = field(eq=False)
    rule_tails: List = field(eq=False)


@frozen
class PredictedEntity:
    """
    Predicted entity - entity and score
    """

    entity: str
    score: float


@frozen
class Prediction:
    """
    Predictions for any head, relation, tail triple
    """

    head: str
    tail: str
    edge_type: str
    predicted_heads: List[PredictedEntity] = field(eq=False)
    predicted_tails: List[PredictedEntity] = field(eq=False)


@frozen
class Rule:
    """
    AnyBURL rule class. Contains information about the rule semantics including
    the prediction metadata that is generated as part of the explanations.

    There are three different types of rules:
    AC1:
        h(c0, X) ← b1(X, A2), ..., bn(An, cn+1)
    AC2:
        h(c0, X) ← b1(X, A2), ..., bn(An, An+1)
    C:
        h(Y, X) ← b1(X, A2), ..., bn(An, Y )

    AC2 rules are generalizations of acyclic ground path rules, C rules are
    generalizations of cyclic ground path rules, while AC1 rules can be
    generalized to both.

    For more information about the rules please check the AnyBURL paper.

    * predicted_entity
    * predicted_count
        # how many times the rule was predicted/fired
    * correctly_predicted
        # how many times was it correctly predicted
    * confidence
        # how confident is AnyBURL in predictions, higher is better
    * head_atom
        # head rule atom
    * body_atoms
        # rule body atoms
    """

    predicted_entity: str
    predicted_count: int
    correctly_predicted: int
    confidence: float
    head_atom: Atom
    body_atoms: List[Atom]


def atom_from_str(value: str) -> Atom:
    """
    Parses the rule atom from rule atom string.

    :param value: string to parse
    """
    match = re.match(r"(.*)\((.*),(.*)\)", value)
    if not match:
        raise ValueError("Failed to parse atom!")
    edge_type = match.group(1)
    negated = False
    if "!" in edge_type:
        edge_type = edge_type[1:]
        negated = True
    head = match.group(2)
    tail = match.group(3)
    return Atom(head=head, edge_type=edge_type, tail=tail, negated=negated)


def rule_from_str(value: str) -> Rule:
    """
    Parses anyburl rule string and creates adequate rule object

    :param value: rule string
    :return: rule object
    """
    info = value.split("\t")

    predicted_entity = info[0]
    predicted_count = int(info[1])
    correctly_predicted = int(info[2])
    confidence = float(info[3])

    rule_info = info[4].split(" <= ")
    head_atom = atom_from_str(rule_info[0])

    body_atoms = []
    for atom in rule_info[1].split(", "):
        body_atom = atom_from_str(atom)
        body_atoms.append(body_atom)

    return Rule(
        predicted_entity=predicted_entity,
        predicted_count=predicted_count,
        correctly_predicted=correctly_predicted,
        confidence=confidence,
        head_atom=head_atom,
        body_atoms=body_atoms,
    )


@frozen
class EvidenceChainScoredPath:
    """
    The scored evidence chain path. Contains the path score
    and nodes that are part of that path
    """

    path_score: float
    path_nodes: List[str]


@frozen
class EvidenceChain:
    """
    Evidence chain contains the metapath used to create
    that chain, start and ending nodes and all the socred
    paths
    """

    metapath: List["EdgeType"]
    start_node: str
    end_node: str
    paths: List[EvidenceChainScoredPath]


@frozen
class EvidenceChainsList:
    """
    Evidence chain list contains the predicted node,
    the prediction score and all evidence chains for that
    prediction
    """

    prediction: str
    prediction_score: float
    evidence_chains: List[EvidenceChain]


@frozen
class Graph:
    triples: List[Tuple[str, str, str]]
    edge_types: List[str]
    nodes: List[str]


@frozen
class EdgeType:
    """EdgeType - description of a connection between two vertices in a graph."""

    head_type: str
    tail_type: str
    pref_name: str
    label: str
    forward: bool


@define
class MetaPath:
    """MetaPath - an abstract map of entity types in a path."""

    edges: List[EdgeType]

    def _edges_match(self, first_edge: EdgeType, second_edge: EdgeType) -> bool:
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
        elif first_edge.forward and not second_edge.forward:
            return first_edge.tail_type == second_edge.tail_type

        # case C
        elif not first_edge.forward and second_edge.forward:
            return first_edge.head_type == second_edge.head_type

        # case D
        elif not first_edge.forward and not second_edge.forward:
            return first_edge.head_type == second_edge.tail_type

        else:
            raise Exception("Unreachable code")

    def __len__(self):
        """Return the length of the MetaPath (len).

        :return: Integer length of the graph
        """
        return len(self.edges)


@define
class MetapathEdgeData:
    """
    Metapath edge data container used to save all edge info
    during the metapath direction resolution
    """

    head: str
    tail: str
    label: str
    pref_name: str
    head_type: str
    tail_type: str
    forward: bool = True

    @staticmethod
    def direct_metapath_edges(first: "MetapathEdgeData", second: "MetapathEdgeData"):
        """
        Directs the metapath edges (first, second) by inspecting the
        relationship (direction) of the associated edge heads and tails
        """
        if first.tail == second.tail:
            second.forward = False
        elif first.head == second.head:
            first.forward = False
        elif first.head == second.tail:
            first.forward = False
            second.forward = False

    def to_edge(self) -> EdgeType:
        """
        Convert the metapath edge data object to a plain healnet edge

        :return: EdgeType object
        """
        return EdgeType(
            label=self.label,
            pref_name=self.pref_name,
            forward=self.forward,
            head_type=self.head_type,
            tail_type=self.tail_type,
        )
