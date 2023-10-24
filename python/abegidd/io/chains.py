import json
from pathlib import Path
from typing import List

from abegidd.entities import EvidenceChainsList


def write_evidence_chains(filepath: Path, chains: List[EvidenceChainsList]):
    """
    Writes evidence chains to file, where each line is a distinct JSON blob. The blobs
     are written one-per-line as a JSONL file to allow fast scanning of large files.
     Each blob has the following structure;

    {
        "prediction": (str) predicted entity,
        "prediction_score": (float) score for predicted entity,
        "chain": {
            "start_node": (str) entity at start of chain,
            "end_node": (str) entity at end of chain,
            "metapath": [
                list of node and edge types constructing abstract metapath
                {
                    "label": (str) edge label - ie `COMPOUND_treats_DISEASE`
                    "reversed": (bool) whether the edge is traversed backward
                }, ...
            ],
            "path": {
                (dict) actual path / evidence chain between nodes in graph
                "path_score": (float) score for this path,
                "path_nodes": (list) nodes in this path
                [
                    "node": (str) node name
                ]
            }
        }
    }


    :param filepath: filepath where to write evidence chains
    :param chains: evidence chains collection
    """

    with filepath.open("w") as fh:
        for metapath_chain in chains:
            base_blob = {
                "prediction": metapath_chain.prediction,
                "prediction_score": metapath_chain.prediction_score,
            }

            for chain in metapath_chain.evidence_chains:
                chain_blob = {
                    "start_node": chain.start_node,
                    "end_node": chain.end_node,
                    "metapath": [
                        {"label": edge.label, "reversed": not edge.forward}
                        for edge in chain.metapath
                    ],
                }

                assert len(chain.paths) > 0

                for path in chain.paths:
                    path_blob = {
                        "path_score": path.path_score,
                        "path": [node for node in path.path_nodes],
                    }

                    line_blob = {**base_blob, **chain_blob, **path_blob}

                    json_str = json.dumps(line_blob)
                    fh.write(f"{json_str}\n")
