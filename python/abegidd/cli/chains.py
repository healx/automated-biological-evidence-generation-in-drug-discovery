import logging
from pathlib import Path
from typing import Dict, List, Tuple

import attrs
import click

from abegidd.entities import Explanation, Prediction
from abegidd.filters import filter_explanations, filter_predictions_for_node_names
from abegidd.io import (
    anyburl,
    read_explanations_filters,
    read_predictions_filters,
    write_evidence_chains,
)
from abegidd.generator import EvidenceChainsGenerator


logger = logging.getLogger(__name__)


@click.command()
@click.argument(
    "graph-dir",
    type=click.Path(dir_okay=True, file_okay=False, exists=True),
)
@click.argument(
    "results-dir",
    type=click.Path(dir_okay=True, file_okay=False),
)
@click.argument(
    "predictions-file",
    type=click.Path(dir_okay=False, file_okay=True),
)
@click.argument(
    "explanations-file",
    type=click.Path(dir_okay=False, file_okay=True),
)
@click.option(
    "--predictions-filter-file",
    type=click.Path(dir_okay=False, file_okay=True, exists=True),
    default=None,
)
@click.option(
    "--explanations-filter-file",
    type=click.Path(dir_okay=False, file_okay=True, exists=True),
    default=None,
)
@click.option("--disease", type=str)
def _chains(
    graph_dir,
    results_dir,
    predictions_file,
    explanations_file,
    predictions_filter_file,
    explanations_filter_file,
    disease,
):
    triples_file = Path().cwd() / graph_dir / "triples.txt"
    with triples_file.open("r") as fh:
        triples = [tuple(line.strip("\n").split("\t")) for line in fh.readlines()]

    predictions = anyburl.read_predictions(Path(predictions_file))
    filter_node_names = read_predictions_filters(Path(predictions_filter_file))
    check_filter_nodes_in_triples(filter_node_names, triples)
    predictions = filter_predictions_for_node_names(predictions, filter_node_names)

    explanations = anyburl.read_explanations(Path(explanations_file))
    explanations_filters = read_explanations_filters(Path(explanations_filter_file))
    check_explanations_filter_in_triples(explanations_filters, triples)
    explanations = filter_explanations(explanations, explanations_filters)

    predictions = sort_and_slice_predictions_by_score(predictions)

    evidence_chains = generate_evidence_chains(triples, predictions, explanations)

    assert len(evidence_chains) > 0, "no chains produced"

    write_evidence_chains(Path("evidence-chains.jsonl"), evidence_chains)


def check_filter_nodes_in_triples(
    filter_node_names: List[str], triples: List[Tuple[str, str, str]]
) -> None:
    node_names = set(name for head, _, tail in triples for name in (head, tail))
    for filter_node_name in filter_node_names:
        if filter_node_name not in node_names:
            raise ValueError("Node %s not in training data", filter_node_name)


def check_explanations_filter_in_triples(
    explanations_filters: Dict[int, List[str]], triples: List[Tuple[str, str, str]]
) -> None:
    relation_names = set(relation for _, relation, _ in triples)
    for rank, explanations_filters in explanations_filters.items():
        for explanations_filter in explanations_filters:
            if explanations_filter not in relation_names:
                raise ValueError(
                    "Relation %s not in training data", explanations_filter
                )


def sort_and_slice_predictions_by_score(
    predictions: List[Prediction],
) -> List[Prediction]:
    """
    Sorts the predictions by score and slices them to top k predictions if
    `top_k_predictions` param is provided

    :param predictions: predictions collection
    :param top_k_predictions: top k predictions to be returned, if omitted it
        will return all
    :return: same predictions object inline sorted with sliced predicted heads/tails
    """

    # sort and slice the predictions based on the input
    sorted_predictions = []
    for prediction in predictions:
        predicted_heads = sorted(
            prediction.predicted_heads,
            key=lambda pred: pred.score,
            reverse=True,
        )
        predicted_tails = sorted(
            prediction.predicted_tails,
            key=lambda pred: pred.score,
            reverse=True,
        )

        sorted_predictions.append(
            attrs.evolve(
                prediction,
                predicted_heads=predicted_heads,
                predicted_tails=predicted_tails,
            )
        )

    return sorted_predictions


def generate_evidence_chains(
    triples: List[
        Tuple[
            str,
            str,
            str,
        ]
    ],
    predictions=List[Prediction],
    explanations=List[Explanation],
):
    chains_generator = EvidenceChainsGenerator(
        triples=triples, predictions=predictions, explanations=explanations
    )

    return chains_generator.get_chains_for_predicted_heads()


if __name__ == "__main__":
    _chains()
