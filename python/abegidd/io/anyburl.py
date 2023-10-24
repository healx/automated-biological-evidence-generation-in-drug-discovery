from pathlib import Path
from typing import Any, Dict, Iterator, List, Tuple

from abegidd.entities import Explanation, PredictedEntity, Prediction, rule_from_str


def read_predictions(filepath: Path) -> List[Prediction]:
    """
    Read the predictions from raw anyburl output file.

    :param filepath: anyburl predictions output file to read
    :return: list of predictions for triples
    """
    predictions = [
        Prediction(
            head=head,
            tail=tail,
            edge_type=relation,
            predicted_heads=_predicted_entity_list(predicted_heads),
            predicted_tails=_predicted_entity_list(predicted_tails),
        )
        for (
            head,
            relation,
            tail,
        ), predicted_heads, predicted_tails in _read_prediction(filepath)
    ]

    # keep the prediction order but remove the duplicates
    return _deduplicate_list(predictions)


def _read_prediction(filepath: Path) -> Iterator[Tuple[Tuple[str, ...], str, str]]:
    """
    Yields predictions from output file. Not lazy as the out_file
    needs to be read entirely into memory

    :param filepath: path to the prediction file
    """
    with open(filepath, "r") as file:
        for triple, heads, tails in _chunks(file.read().splitlines(), 3):
            assert heads.startswith("Heads: ")
            assert tails.startswith("Tails: ")
            yield tuple(triple.split()), heads, tails


def _predicted_entity_list(line: str) -> List[PredictedEntity]:
    """
    Reads single line of predicted entities

    :param line: predictions line
    """
    _, line = line.split(": ", 1)
    line_split = line.strip("\t").split("\t")
    if not any(line_split):
        return []

    return [
        PredictedEntity(entity=entity, score=float(score))
        for entity, score in _chunks(line.strip("\t").split("\t"), 2)
    ]


def _chunks(lst: List[str], chunk_size: int) -> Iterator[List[str]]:
    """Generator utility for chunking the prediction line data"""
    for i in range(0, len(lst), chunk_size):
        yield lst[i : i + chunk_size]


def _deduplicate_list(values: List[Any]) -> List[Any]:
    seen: Dict[Any, Any] = {}
    return [seen.setdefault(value, value) for value in values if value not in seen]


def read_explanations(filepath: Path) -> List[Explanation]:
    """
    Read the explanations from raw anyburl explanations output file

    :param filepath: anyburl explanations output file to read
    :return: list of read rules
    """

    if isinstance(filepath, str):
        filepath = Path(filepath)

    # open the explanation file for reading
    explanations_file = filepath.open("r")

    explanations = []
    is_head = False
    heads, tails = [], []
    current_explanation: List[str] = []

    while line := explanations_file.readline():
        # if its a heads str mark that the reading of heads has started
        if "Heads:" in line:
            is_head = True
            continue
        # if its a tails str mark that the reading of tails has started
        if "Tails:" in line:
            is_head = False
            continue
        # if its a rule add it to the active list, either heads or tails
        if "<=" in line:
            if is_head:
                heads.append(rule_from_str(line))
            else:
                tails.append(rule_from_str(line))
            continue
        # else it must be a triple that needs reading. This means we
        # have read all explanations and should create an explanation set
        if current_explanation:
            explanations.append(
                Explanation(
                    head=current_explanation[0],
                    edge_type=current_explanation[1],
                    tail=current_explanation[2],
                    rule_heads=heads,
                    rule_tails=tails,
                )
            )
            # reset saved head and tails
            heads, tails = [], []
        else:
            current_explanation = [part.strip() for part in line.split(" ")]

    # keep the explanations order but remove the duplicates
    return _deduplicate_list(explanations)
