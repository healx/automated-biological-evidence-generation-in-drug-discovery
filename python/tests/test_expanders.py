from typing import List

from precisely import assert_that, contains_exactly, mapping_includes

from abegidd.entities import JsonChain
from abegidd.expanders import _group_chains_with_the_same_nodes


def _stub_json_chain(path: List[str]):
    return JsonChain(
        prediction=path[0],
        prediction_score=0.0,
        start_node=path[0],
        end_node=path[-1],
        metapath=[],
        path_score=0.0,
        path=path,
    )


class TestGroupChainsWithTheSameNodes:
    def test_grouping(self):
        chains = [
            _stub_json_chain(["node-a", "node-b", "node-c"]),
            _stub_json_chain(["node-a", "node-b", "node-d"]),
            _stub_json_chain(["node-a", "node-b", "node-c"]),
        ]

        grouped_chains = list(_group_chains_with_the_same_nodes(chains))

        assert_that(
            grouped_chains,
            contains_exactly(
                contains_exactly(
                    mapping_includes({"path": ["node-a", "node-b", "node-c"]}),
                    mapping_includes({"path": ["node-a", "node-b", "node-c"]}),
                ),
            ),
        )
