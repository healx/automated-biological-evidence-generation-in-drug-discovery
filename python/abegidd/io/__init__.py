from collections import defaultdict
from pathlib import Path
from typing import Dict, List, Tuple

from abegidd.io.chains import read_evidence_chains, write_evidence_chains


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


def read_triples_file(filepath: Path) -> List[Tuple[str, str, str]]:
    with filepath.open("r") as fh:
        values = [line.strip("\n").split("\t") for line in fh.readlines()]
    return [(head, relation, tail) for head, relation, tail in values]


__ALL__ = [
    "anyburl",
    "read_predictions_filter",
    "read_explanations_filter",
    read_evidence_chains,
    read_triples_file,
    write_evidence_chains,
]
