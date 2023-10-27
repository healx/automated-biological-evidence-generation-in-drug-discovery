import json
import logging
from collections import Counter
from functools import partial
from itertools import groupby
from operator import itemgetter
from pathlib import Path
from typing import Generator, Iterable, List, Set, Tuple

import click

from abegidd.entities import JsonChain


@click.command
@click.argument("evidence-chains-file", type=click.Path(file_okay=True, exists=True))
@click.argument("genes-filter-file", type=click.Path(file_okay=True, exists=True))
@click.argument("pathways-filter-file", type=click.Path(file_okay=True, exists=True))
@click.argument("predictions-filter-file", type=click.Path(file_okay=True, exists=True))
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
    filtered_evidence_chains_file: str,
):
    genes: Set[str] = {*_read_lines(genes_filter_file)}
    pathways: Set[str] = {*_read_lines(pathways_filter_file)}
    compound_predictions: Set[str] = {*_read_lines(predictions_filter_file)}

    _filter = partial(
        _filter_evidence_chain,
        genes=genes,
        pathways=pathways,
        compound_predictions=compound_predictions,
    )

    chain_prediction_count: Counter[str] = Counter(
        map(
            itemgetter("prediction"),
            map(json.loads, Path(evidence_chains_file).open("r").readlines()),
        )
    )

    filtered_chains: List[JsonChain] = [
        json_chain
        for json_chain in map(
            json.loads, Path(evidence_chains_file).open("r").readlines()
        )
        if _filter(json_chain)
    ]

    _print_prediction_stats(filtered_chains, chain_prediction_count)

    _write_filtered_chains(filtered_chains, filtered_evidence_chains_file)


def _write_filtered_chains(chains: List[JsonChain], output_file: str):
    with Path(output_file).open("w") as fh:
        for chain in chains:
            line = " ".join(chain["path"])
            fh.write(f"{line}\n")


def _group_chains_by_prediction(
    chains=Generator[JsonChain, None, None]
) -> Generator[Tuple[str, Iterable[JsonChain]], None, None]:
    """
    Returns a generator which whill lazily return tuples with the prediction as a string
    and an iterable containing the chains
    :param chains:
    :yield: prediction and iterable of chains
    """
    sorted_chains = sorted(chains, key=itemgetter("prediction"))
    for prediction, group in groupby(sorted_chains, itemgetter("prediction")):
        logging.info("Grouping %s chains", prediction)
        yield prediction, group


def _print_prediction_stats(
    filtered_chains: List[JsonChain], chain_prediction_count: Counter[str]
) -> None:
    for prediction, group in _group_chains_by_prediction(filtered_chains):
        prediction_name = prediction.strip("_COMPOUND").upper()
        print(
            f"Total paths for {prediction_name}: {chain_prediction_count[prediction]}"
        )
        print(
            f"Total paths for {prediction_name} with gene and pathway filter {len(list(group))}"
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
