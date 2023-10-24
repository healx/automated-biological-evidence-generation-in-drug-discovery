from typing import Iterable, List, Optional, Tuple, Union

import numpy as np
from attrs import define, field, validators

from abegidd.entities import EdgeType, Graph, MetaPath
from abegidd.iterables import flatten
from abegidd.path_scorer import CachedArrayIndexLookup


def graph_from_triples(triples: List[Tuple[str, str, str]]) -> Graph:
    nodes: List[str] = list(
        set(node_name for head, _, tail in triples for node_name in (head, tail))
    )
    edge_types: List[str] = list(set(edge_type for _, edge_type, _ in triples))

    return Graph(triples=triples, nodes=nodes, edge_types=edge_types)


def is_len_2(instance, attribute, value):
    if len(value) != 2:
        raise ValueError("_Tree tuple is not length 2")


@define
class _Tree:
    """Utility class for carrying information about the discovered paths."""

    pair: Tuple[str, str] = field(validator=[validators.instance_of(tuple), is_len_2])
    children: List["_Tree"] = field(factory=list)


def paths_with_metapath(
    graph: Graph,
    metapath: MetaPath,
    start_node: str,
    end_node: Optional[str] = None,
    index_lookup: Optional[CachedArrayIndexLookup] = None,
) -> List[Tuple[str, str]]:
    """Find all paths between start node and end node that follow metapath.

    :param graph: KnowledgeGraph to search
    :param metapath: MetaPath to search for paths along
    :param start_node: Starting Node
    :param end_node: Ending Node
    :param index_lookup: Optional - cached lookup array (internal)
    :return: List of paths
    """
    if index_lookup is None:
        index_lookup = CachedArrayIndexLookup(np.array(graph.triples))

    if len(metapath) == 1:
        raise NotImplementedError(f"Odd metapath: {metapath}")
    elif len(metapath) == 2:
        paths = _one_hop_paths_with_metapath(
            index_lookup, metapath, start_node, end_node
        )
    elif len(metapath) == 3:
        paths = _two_hop_paths_with_metapath(
            index_lookup, metapath, start_node, end_node
        )
    elif len(metapath) == 4:
        paths = _three_hop_paths_with_metapath(
            index_lookup, metapath, start_node, end_node
        )
    else:
        raise NotImplementedError(f"Metapaths of length {len(metapath)}")

    return paths
    return _remove_start_end_duplicates(start_node, end_node, paths)


def _one_hop_paths_with_metapath(
    index_lookup: CachedArrayIndexLookup,
    metapath: MetaPath,
    start_node: str,
    end_node: Optional[str],
) -> List:
    """Calculate one hop paths with metapath.

    Returns list of node index pairs forming all paths from start to end node,
    following the edges provided in the metapath (1 edge)

    :param index_lookup: index lookup class
    :param metapath: metapath containing the list of edges
    :param start: starting node
    :param end: end node, defaults to None
    :return: list of pairs forming a path from the matapath
    """
    assert len(metapath) == 2

    roots = [
        _Tree(triple)
        for triple in _linked_pairs(index_lookup, start_node, edge=metapath.edges[0])
    ]

    for first_pair in roots:
        first_pair.children = [
            _Tree(pair)
            for pair in _linked_pairs(
                index_lookup,
                first_pair.pair[1],
                edge=metapath.edges[1],
                end_node_index=end_node,
            )
        ]

    return [
        (first_pair.pair, second_pair.pair)
        for first_pair in roots
        for second_pair in first_pair.children
    ]


def _two_hop_paths_with_metapath(
    index_lookup: CachedArrayIndexLookup,
    metapath: MetaPath,
    start_node: str,
    end_node: Optional[str],
) -> List:
    """Calculate two hop paths with metapath.

    Returns list of node index pairs forming all paths from start to end node,
    following the edges provided in the metapath (2 edges)

    :param index_lookup: index lookup class
    :param metapath: metapath containing the list of edges
    :param start: starting node
    :param end: end node, defaults to None
    :return: list of pairs forming a path from the matapath
    """
    assert len(metapath) == 3

    roots = [
        _Tree(triple)
        for triple in _linked_pairs(index_lookup, start_node, edge=metapath.edges[0])
    ]

    for first_pair in roots:
        first_pair.children = [
            _Tree(pair)
            for pair in _linked_pairs(
                index_lookup, first_pair.pair[1], edge=metapath.edges[1]
            )
        ]

        for second_pair in first_pair.children:
            second_pair.children = [
                _Tree(pair)
                for pair in _linked_pairs(
                    index_lookup,
                    second_pair.pair[1],
                    edge=metapath.edges[2],
                    end_node_index=end_node,
                )
            ]

    return [
        (first_pair.pair, second_pair.pair, third_pair.pair)
        for first_pair in roots
        for second_pair in first_pair.children
        for third_pair in second_pair.children
    ]


def _three_hop_paths_with_metapath(
    index_lookup: CachedArrayIndexLookup,
    metapath: MetaPath,
    start_node: str,
    end_node: Optional[str],
) -> List:
    """Calculate three hop paths with metapath.

    Returns list of node index pairs forming all paths from start to end node,
    following the edges provided in the metapath (3 edges).

    :param index_lookup: index lookup class
    :param metapath: Metapath containing the list of edges
    :param start: Starting node
    :param end: Optional end node
    :return: list of pairs forming a path from the matapath
    """
    assert len(metapath) == 4

    roots = [
        _Tree(triple)
        for triple in _linked_pairs(index_lookup, start_node, edge=metapath.edges[0])
    ]

    for first_pair in roots:
        first_pair.children = [
            _Tree(pair)
            for pair in _linked_pairs(
                index_lookup, first_pair.pair[1], edge=metapath.edges[1]
            )
        ]

        for second_pair in first_pair.children:
            second_pair.children = [
                _Tree(pair)
                for pair in _linked_pairs(
                    index_lookup, second_pair.pair[1], edge=metapath.edges[2]
                )
            ]

            for third_pair in second_pair.children:
                third_pair.children = [
                    _Tree(pair)
                    for pair in _linked_pairs(
                        index_lookup,
                        third_pair.pair[1],
                        edge=metapath.edges[3],
                        end_node_index=end_node,
                    )
                ]

    return [
        (first_pair.pair, second_pair.pair, third_pair.pair, fourth_hop.pair)
        for first_pair in roots
        for second_pair in first_pair.children
        for third_pair in second_pair.children
        for fourth_hop in third_pair.children
    ]


def _remove_start_end_duplicates(start_node, end_node, paths):
    return [
        path
        for path in paths
        if _check_start_end_duplicates(start_node, end_node, path)
    ]


def _check_start_end_duplicates(
    start_node: str, end_node: Union[str, None], path: Tuple
) -> bool:
    flat_path = flatten(path)

    if end_node is None:
        if flat_path.count(start_node.index) == 1:
            return True
        else:
            return False
    else:
        if (
            flat_path.count(start_node.index) == 1
            and flat_path.count(end_node.index) == 1
        ):
            return True
        else:
            return False


def _linked_pairs(
    index_lookup: CachedArrayIndexLookup,
    node: str,
    edge: EdgeType,
    end_node_index: Optional[int] = None,
) -> Iterable[Tuple[str, str]]:
    if edge.forward:
        pairs = index_lookup.linked_tails(node, edge.label, end_node_index)
    else:
        pairs = index_lookup.linked_heads(node, edge.label, end_node_index)

    return map(tuple, np.atleast_2d(pairs))
