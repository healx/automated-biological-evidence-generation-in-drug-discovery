from collections import Counter
from functools import partial
from operator import itemgetter
from pathlib import Path
from typing import List, Set

import click

from abegidd.entities import JsonChain
from abegidd.expanders import build_deductive_chains, group_chains_by_prediction
from abegidd.io import read_evidence_chains


@click.command
@click.argument("evidence-chains-file", type=click.Path(file_okay=True, exists=True))
@click.argument("genes-filter-file", type=click.Path(file_okay=True, exists=True))
@click.argument("pathways-filter-file", type=click.Path(file_okay=True, exists=True))
@click.argument("predictions-filter-file", type=click.Path(file_okay=True, exists=True))
@click.argument(
    "prioritised-edge-names-file", type=click.Path(file_okay=True, exists=True)
)
@click.option(
    "-f",
    "--filtered-evidence-chains-file",
    type=click.Path(file_okay=True),
    default="filtered-evidence-chains.txt",
)
def _chains_filter(
    evidence_chains_file: str,
    genes_filter_file: str,
    pathways_filter_file: str,
    predictions_filter_file: str,
    prioritised_edge_names_file: str,
    filtered_evidence_chains_file: str,
):
    genes: Set[str] = {*_read_lines(genes_filter_file)}
    pathways: Set[str] = {*_read_lines(pathways_filter_file)}
    compound_predictions: Set[str] = {*_read_lines(predictions_filter_file)}

    prioritised_edge_names: List[str] = _read_lines(prioritised_edge_names_file)

    _filter = partial(
        _filter_evidence_chain,
        genes=genes,
        pathways=pathways,
        compound_predictions=compound_predictions,
    )

    chains: List[JsonChain] = [
        json_chain for json_chain in read_evidence_chains(Path(evidence_chains_file))
    ]
    print(f"Read {len(chains)} from file {evidence_chains_file}")

    inferred_chains: List[JsonChain] = list(
        build_deductive_chains(chains, prioritised_edge_names=prioritised_edge_names)
    )
    print(f"Inferred {len(inferred_chains)} new chains")

    filtered_chains: List[JsonChain] = [
        chain for chain in chains + inferred_chains if _filter(chain)
    ]
    print(f"Filtered to {len(filtered_chains)} chains")

    chain_prediction_count: Counter[str] = Counter(
        map(itemgetter("prediction"), read_evidence_chains(Path(evidence_chains_file)))
    )
    _print_prediction_stats(filtered_chains, chain_prediction_count)

    _write_filtered_chains(filtered_chains, filtered_evidence_chains_file)


def _write_filtered_chains(chains: List[JsonChain], output_file: str):
    with Path(output_file).open("w") as fh:
        for chain in chains:
            line = " ".join(chain["path"])
            fh.write(f"{line}\n")


def _print_prediction_stats(
    filtered_chains: List[JsonChain], chain_prediction_count: Counter[str]
) -> None:
    for prediction, group in group_chains_by_prediction(filtered_chains):
        prediction_name = prediction.strip("_COMPOUND").upper()
        print(
            f"Total paths for {prediction_name}: {chain_prediction_count[prediction]}"
        )
        print(
            f"Total paths for {prediction_name} with gene and pathway filter"
            f" {len(list(group))}"
        )
        print()


def _filter_evidence_chain(
    chain: JsonChain,
    genes: Set[str],
    pathways: Set[str],
    compound_predictions: Set[str],
) -> bool:
    path_nodes: Set[str] = {*chain["path"]}
    compounds_present: bool = len(list(compound_predictions & path_nodes)) > 0
    genes_present: bool = len(list(genes & path_nodes)) > 0
    pathways_present: bool = len(list(pathways & path_nodes)) > 0

    # keep the chain if compounds_present and (genes_present or pathways_present)
    return compounds_present and (genes_present or pathways_present)


def _read_lines(filepath: str) -> List[str]:
    return list(map(lambda line: line.strip(), Path(filepath).open("r").readlines()))


if __name__ == "__main__":
    _chains_filter()
