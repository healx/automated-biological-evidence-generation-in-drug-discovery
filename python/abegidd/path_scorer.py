from collections import Counter
from functools import lru_cache
from typing import Callable, List, Optional, Tuple

import numpy as np
from attr import define, field


@define
class DegreeWeightedPathScorer:
    """Class to find the DWPC scores for a list of paths."""

    triples: List[Tuple[str, str, str]]
    node_degrees_lookup: Counter[str] = field(factory=Counter)

    def __attrs_post_init__(self):
        """Cache the degree for all nodes in the graph.

        :return: DegreeWeightedPathScorer class
        """
        self.node_degrees_lookup = Counter(
            node_name for head, _, tail in self.triples for node_name in (head, tail)
        )

    def path(self, path: List[str]) -> float:
        """Calculate the DWPC score for a path in the graph.

        :param path: Path to find score for
        :return: the DWPC score
        """
        return float(
            np.prod(
                [_degree_weight(self.node_degrees_lookup[index]) for index in path],
                axis=0,
            )
        )


def _degree_weight(degree: int, weight: float = 0.4) -> float:
    return degree ** (-weight)


@define
class CachedArrayIndexLookup:
    """Numpy based array index lookup class with lru mask caching."""

    array: np.ndarray
    _relations_cache: Callable = field(factory=lru_cache)
    _tails_cache: Callable = field(factory=lru_cache)
    _heads_cache: Callable = field(factory=lru_cache)

    def __attrs_post_init__(self):
        """Initialise the CachedArrayIndexLookup (internal class)."""
        self._relations_cache = lru_cache(maxsize=None)(self.__relations)
        self._tails_cache = lru_cache(maxsize=None)(self.__tails)
        self._heads_cache = lru_cache(maxsize=None)(self.__heads)

    def _heads(self, heads_index: str):
        return self._heads_cache(heads_index)

    def _tails(self, tails_index: str):
        return self._tails_cache(tails_index)

    def _relations(self, relation_index: str):
        return self._relations_cache(relation_index)

    def __heads(self, heads_index: str) -> np.ndarray:
        # uncached lookup
        return self.array[:, 0] == heads_index

    def __tails(self, tails_index: str) -> np.ndarray:
        # uncached lookup
        return self.array[:, 2] == tails_index

    def __relations(self, relation_index: str) -> np.ndarray:
        # uncached lookup
        return self.array[:, 1] == relation_index

    def linked_tails(
        self, head: str, edge_label: str, end_node_index: Optional[str] = None
    ) -> List[Tuple[str, str]]:
        """Return all tails linked by head and edge from cache.

        :param head:
        :param edge_label:
        :param end_node_index:
        :return: List of tail indices
        """
        linked_nodes_index = self._heads(head)
        indices = np.logical_and(linked_nodes_index, self._relations(edge_label))
        pairs = self.array[indices][:, [0, 2]]
        if end_node_index is not None:
            pairs = pairs[pairs[:, 1] == end_node_index]
        return [(head, tail) for (head, tail) in np.atleast_2d(pairs)]

    def linked_heads(
        self, tail: str, edge_label: str, end_node_index: Optional[str] = None
    ) -> List[Tuple[str, str]]:
        """Return all heads linked by tail and edge from cache.

        :param tail_index:
        :param edge_index:
        :param end_node_index:
        :return: List of head indices
        """
        linked_nodes_index = self._tails(tail)
        indices = np.logical_and(linked_nodes_index, self._relations(edge_label))
        pairs = self.array[indices][:, [0, 2]][:, ::-1]
        if end_node_index is not None:
            pairs = pairs[pairs[:, 1] == end_node_index]
        return [(head, tail) for (head, tail) in np.atleast_2d(pairs)]
