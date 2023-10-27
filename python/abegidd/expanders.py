from itertools import groupby
from operator import itemgetter
from typing import List

from abegidd.entities import JsonChain


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
