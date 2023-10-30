from typing import List

import pytest
from precisely import assert_that, contains_exactly, equal_to

from abegidd.cli.chains_filter import _chain_to_string, _write_filtered_chains
from abegidd.entities import JsonChain, stub_json_chain, stub_json_metapath


@pytest.fixture
def chains() -> List[JsonChain]:
    return [
        stub_json_chain(
            path=["node_a_COMPOUND", "node_b_GENE", "node_c_DISEASE"],
            metapath=[
                stub_json_metapath("COMPOUND_regulates_GENE"),
                stub_json_metapath("GENE_associates_DISEASE"),
            ],
        )
    ]


class TestWriteFilteredChains:
    def test_chains_to_string(self, chains):
        chain = chains[0]
        chain_string = _chain_to_string(chain)

        assert_that(chain_string, equal_to("NODE_A regulates NODE_B associates NODE_C"))

    def test_chains_written(self, chains, tmp_path):
        chains_file = tmp_path / "chains.txt"

        _write_filtered_chains(chains, str(chains_file))

        with chains_file.open("r") as fh:
            assert_that(
                fh.readlines(),
                contains_exactly("NODE_A regulates NODE_B associates NODE_C\n"),
            )
