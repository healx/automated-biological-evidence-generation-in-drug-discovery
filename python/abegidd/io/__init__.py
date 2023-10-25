from collections import defaultdict
from pathlib import Path
from typing import Dict, List

from abegidd.io.chains import write_evidence_chains


def read_predictions_filters(filepath: Path) -> List[str]:
    with filepath.open("r") as fh:
        return [line.strip("\n") for line in fh.readlines()]


def read_explanations_filters(filepath: Path) -> Dict[int, List[str]]:
    explanation_filters = defaultdict(list)
    with filepath.open("r") as fh:
        for line in fh.readlines():
            rank, relation = line.strip("\n").split("\t")
            explanation_filters[int(rank)].append(relation)
    return explanation_filters


__ALL__ = [
    "anyburl",
    "read_predictions_filter",
    "read_explanations_filter",
    write_evidence_chains,
]
