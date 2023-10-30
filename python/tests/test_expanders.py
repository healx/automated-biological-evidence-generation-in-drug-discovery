from typing import List, Optional

from precisely import assert_that, contains_exactly, equal_to, mapping_includes

from abegidd.entities import JsonChain
from abegidd.expanders import (
    _build_deduced_chain,
    group_chains_with_the_same_nodes,
    group_chains_by_prediction,
)


def _stub_json_chain(path: List[str], prediction: Optional[str] = None):
    if prediction is None:
        prediction = path[0]
    return JsonChain(
        prediction=prediction,
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

        grouped_chains = list(group_chains_with_the_same_nodes(chains))

        assert_that(
            grouped_chains,
            contains_exactly(
                contains_exactly(
                    mapping_includes({"path": ["node-a", "node-b", "node-c"]}),
                    mapping_includes({"path": ["node-a", "node-b", "node-c"]}),
                ),
                contains_exactly(
                    mapping_includes({"path": ["node-a", "node-b", "node-d"]}),
                ),
            ),
        )


class TestGroupChainsByPrediction:
    def test_grouping(self):
        chains = [
            _stub_json_chain(prediction="node-a", path=["a", "b", "c"]),
            _stub_json_chain(prediction="node-a", path=["a", "b", "d"]),
            _stub_json_chain(prediction="node-b", path=["b", "a", "c"]),
        ]

        grouped_chains = list(group_chains_by_prediction(chains))

        assert_that(
            grouped_chains,
            contains_exactly(
                contains_exactly(
                    equal_to("node-a"),
                    contains_exactly(
                        mapping_includes({"path": ["a", "b", "c"]}),
                        mapping_includes({"path": ["a", "b", "d"]}),
                    ),
                ),
                contains_exactly(
                    equal_to("node-b"),
                    contains_exactly(
                        mapping_includes({"path": ["b", "a", "c"]}),
                    ),
                ),
            ),
        )


class TestBuildDeducedChains:
    def test_build(self):
        chains = [
            JsonChain(
                **{
                    "prediction": "amantadine_COMPOUND",
                    "metapath": [
                        {"label": "COMPOUND_causes_PHENOTYPE", "reversed": False},
                        {"label": "COMPOUND_causes_PHENOTYPE", "reversed": True},
                        {"label": "COMPOUND_in_trial_for_DISEASE", "reversed": False},
                    ],
                    "path": [
                        "amantadine_COMPOUND",
                        "abnormal_left_ventricular_function_PHENOTYPE",
                        "dextrose_COMPOUND",
                        "parkinson_disease_DISEASE",
                    ],
                }
            ),
            JsonChain(
                **{
                    "prediction": "amantadine_COMPOUND",
                    "metapath": [
                        {"label": "COMPOUND_causes_PHENOTYPE", "reversed": False},
                        {"label": "COMPOUND_treats_PHENOTYPE", "reversed": True},
                        {"label": "COMPOUND_in_trial_for_DISEASE", "reversed": False},
                    ],
                    "path": [
                        "amantadine_COMPOUND",
                        "abnormal_left_ventricular_function_PHENOTYPE",
                        "dextrose_COMPOUND",
                        "parkinson_disease_DISEASE",
                    ],
                }
            ),
        ]
        # compound causes phenotype is prioritised
        edge_name_priorities_lookup = {
            "COMPOUND_treats_PHENOTYPE": 0,
            "COMPOUND_causes_PHENOTYPE": 1,
        }
        deduced_chain = _build_deduced_chain(chains, edge_name_priorities_lookup)

        assert_that(
            deduced_chain,
            mapping_includes(
                {
                    "metapath": [
                        {"label": "COMPOUND_causes_PHENOTYPE", "reversed": False},
                        {"label": "COMPOUND_treats_PHENOTYPE", "reversed": True},
                        {"label": "COMPOUND_in_trial_for_DISEASE", "reversed": False},
                    ],
                    "path": [
                        "amantadine_COMPOUND",
                        "abnormal_left_ventricular_function_PHENOTYPE",
                        "dextrose_COMPOUND",
                        "parkinson_disease_DISEASE",
                    ],
                }
            ),
        )

    def test_prioritised_edge_types_honoured(self):
        chains = [
            JsonChain(
                **{
                    "prediction": "amantadine_COMPOUND",
                    "metapath": [
                        {"label": "COMPOUND_causes_PHENOTYPE", "reversed": False},
                        {"label": "COMPOUND_causes_PHENOTYPE", "reversed": True},
                        {"label": "COMPOUND_in_trial_for_DISEASE", "reversed": False},
                    ],
                    "path": [
                        "amantadine_COMPOUND",
                        "abnormal_left_ventricular_function_PHENOTYPE",
                        "dextrose_COMPOUND",
                        "parkinson_disease_DISEASE",
                    ],
                }
            ),
            JsonChain(
                **{
                    "prediction": "amantadine_COMPOUND",
                    "metapath": [
                        {"label": "COMPOUND_causes_PHENOTYPE", "reversed": False},
                        {"label": "COMPOUND_treats_PHENOTYPE", "reversed": True},
                        {"label": "COMPOUND_in_trial_for_DISEASE", "reversed": False},
                    ],
                    "path": [
                        "amantadine_COMPOUND",
                        "abnormal_left_ventricular_function_PHENOTYPE",
                        "dextrose_COMPOUND",
                        "parkinson_disease_DISEASE",
                    ],
                }
            ),
        ]
        # compound causes phenotype is prioritised
        edge_name_priorities_lookup = {
            "COMPOUND_causes_PHENOTYPE": 0,
            "COMPOUND_treats_PHENOTYPE": 1,
        }

        deduced_chain = _build_deduced_chain(chains, edge_name_priorities_lookup)

        assert_that(
            deduced_chain,
            mapping_includes(
                {
                    "metapath": [
                        {"label": "COMPOUND_causes_PHENOTYPE", "reversed": False},
                        {"label": "COMPOUND_causes_PHENOTYPE", "reversed": True},
                        {"label": "COMPOUND_in_trial_for_DISEASE", "reversed": False},
                    ],
                    "path": [
                        "amantadine_COMPOUND",
                        "abnormal_left_ventricular_function_PHENOTYPE",
                        "dextrose_COMPOUND",
                        "parkinson_disease_DISEASE",
                    ],
                }
            ),
        )
