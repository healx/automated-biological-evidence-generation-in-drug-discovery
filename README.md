# Automated Biological Evidence Generation In Drug-Discovery


## AnyBURL

The directory [java](java) contains source files for the [AnyBURL](https://web.informatik.uni-mannheim.de/AnyBURL/)
algorithm with the original license preserved. Some changes have been applied to the source to address bugs and other
small issues.

The java code must be compiled before running the example scripts here, to compile the code ensure that you have Java
installed (we recommend [OpenJDK](https://openjdk.org/) v14 and [Apache Maven](https://maven.apache.org/) v3.9.4).

To compile the java code;

```shell
$ make build-java
```
## Python

We recommend installing the code in a python virtualenv. Once you have a virtualenv set up, the code can be installed
with

```shell
pip install -e '.[dev]'
```

and tests can be run with tox

```shell
tox
```

and linters can be run with the shortcut
```shell
make lint
```


## Running the example: Parkinson Disease

Train AnyBURL using the graph provided in [`data/triples.txt`](data/triples.txt)
```shell
anyburl-train data
```
results are written to a `results` directory.

Generate rules from the trained AnyBURL artefacts in `results`
```shell
anyburl-predict data
```
further results are written to the `results` directory.

### Filter the rules and create Evidence

Run this command to create a list of evidence chains for "Parkinson disease" treatments (defined in [
data/parkinson_disease_predicted_treatments.txt](
data/parkinson_disease_predicted_treatments.txt)), the output is written to `evidence-chains.jsonl`.

```shell
healx-chains \
    data \
    results \
    results/predict-1000 \
    results/predict-explanation \
    --predictions-filter-file data/parkinson-disease-filter/predictions.txt \
    --explanations-filter-file data/parkinson-disease-filter/prioritised-edge-types.txt
```
Results are written to an evidence chains file `results/evidence-chains.jsonl` in the JSONL format. Each line contains
a generated "chain" prediction with the following example structure

```json
{
  "prediction": "methixene_COMPOUND",
  "prediction_score": 0.08924107199497017,
  "start_node": "methixene_COMPOUND",
  "end_node": "parkinson_disease_DISEASE",
  "metapath": [
    {
      "label": "COMPOUND_inhibits_GENE",
      "reversed": false
    },
    {
      "label": "DISEASE_associates_GENE",
      "reversed": true
    }
  ],
  "path_score": 0.005949453701297636,
  "path": [
    "methixene_COMPOUND",
    "htr2c_GENE",
    "parkinson_disease_DISEASE"
  ]
}
```
we can expect to find thousands of chains in this file.

### Filter the chains for Parkinson Disease

Run this command (after creating the evidence chains file) to filter chains by gene and pathway for "Parkinson disease".

```shell
healx-filter \
    results/evidence-chains.jsonl \
    data/parkinson-disease-filter/genes.txt \
    data/parkinson-disease-filter/pathways.txt \
    data/parkinson-disease-filter/predictions-short-list.txt \
    data/parkinson-disease-filter/prioritised-edge-types.txt \
    --filtered-evidence-chains-file filtered-evidence-chains.txt
```

which will produce a filtered evidence chains file in text format, `filtered-evidence-chains.txt` - one line per text
chain. An example of one of the lines from this file are

```
AMANTADINE inhibits CHRNA3 participates NEUROACTIVE_LIGAND-RECEPTOR_INTERACTION involves CABERGOLINE in_trial_for PARKINSON_DISEASE
```
.
