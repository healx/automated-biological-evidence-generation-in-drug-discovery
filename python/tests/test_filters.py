from precisely import assert_that, contains_exactly, mapping_includes

from abegidd.entities import stub_json_chain, stub_json_metapath
from abegidd.filters import filter_low_priority_duplicate_chains


class TestFilterLowPriorityDuplicateChains:
    def test_prioritised_edge_names_chosen(self):
        path = ["n1", "n2", "n3"]
        chains = [
            stub_json_chain(
                prediction="n1",
                path=path,
                metapath=[
                    stub_json_metapath(label="COMPOUND_in_trial_for_DISEASE"),
                    stub_json_metapath(label="DISEASE_ancestor_DISEASE"),
                ],
            ),
            stub_json_chain(
                prediction="n1",
                path=path,
                metapath=[
                    stub_json_metapath(label="COMPOUND_treats_DISEASE"),
                    stub_json_metapath(label="DISEASE_ancestor_DISEASE"),
                ],
            ),
        ]
        prioritised_edge_names = [
            "COMPOUND_treats_DISEASE",
            "COMPOUND_in_trial_for_DISEASE",
        ]

        filtered_chains = filter_low_priority_duplicate_chains(
            chains, prioritised_edge_names
        )

        assert_that(
            filtered_chains,
            contains_exactly(
                mapping_includes(
                    {
                        "path": ["n1", "n2", "n3"],
                        "metapath": contains_exactly(
                            mapping_includes({"label": "COMPOUND_treats_DISEASE"}),
                            mapping_includes({"label": "DISEASE_ancestor_DISEASE"}),
                        ),
                    }
                ),
            ),
        )

    def test_non_prioritised_edge_names_returned(self):
        path = ["n1", "n2", "n3"]
        chains = [
            stub_json_chain(
                prediction="n1",
                path=path,
                metapath=[
                    stub_json_metapath(label="COMPOUND_associates_GENE"),
                    stub_json_metapath(label="GENE_associates_DISEASE"),
                ],
            ),
            stub_json_chain(
                prediction="n1",
                path=path,
                metapath=[
                    stub_json_metapath(label="COMPOUND_not_associates_GENE"),
                    stub_json_metapath(label="GENE_associates_DISEASE"),
                ],
            ),
        ]
        prioritised_edge_names = [
            "COMPOUND_treats_DISEASE",
            "COMPOUND_in_trial_for_DISEASE",
        ]

        filtered_chains = filter_low_priority_duplicate_chains(
            chains, prioritised_edge_names
        )

        assert_that(
            filtered_chains,
            contains_exactly(
                mapping_includes(
                    {
                        "path": ["n1", "n2", "n3"],
                        "metapath": contains_exactly(
                            mapping_includes({"label": "COMPOUND_associates_GENE"}),
                            mapping_includes({"label": "GENE_associates_DISEASE"}),
                        ),
                    }
                ),
                mapping_includes(
                    {
                        "path": ["n1", "n2", "n3"],
                        "metapath": contains_exactly(
                            mapping_includes({"label": "COMPOUND_not_associates_GENE"}),
                            mapping_includes({"label": "GENE_associates_DISEASE"}),
                        ),
                    }
                ),
            ),
        )
