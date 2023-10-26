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

We recommend instaling the code in a python virtualenv. Once you have a virtualenv set up, the code can be installed
with

```shell
$ pip install -e '.[dev]'
```

and tests can be run with tox

```shell
$ tox
```


## Running the example: Parkinson Disease

Train AnyBURL using the graph provided in [`data/triples.txt`](data/triples.txt)
```shell
$ anyburl-train data
```
results are written to a `results` directory.

Generate rules from the trained AnyBURL artefacts in `results`
```shell
$ anyburl-predict data
```
further results are written to the `results` directory.

### Filter the rules and create Evidence

Run this command to create a list of evidence chains for "Parkinson disease" treatments (defined in [
data/parkinson_disease_predicted_treatments.txt](
data/parkinson_disease_predicted_treatments.txt)), the output is written to `evidence-chains.jsonl`.

```shell
$ healx-chains \
    data \
    results \
    results/predict-1000 \
    results/predict-explanation \
    --predictions-filter-file data/parkinson_disease_predicted_treatments.txt \
    --explanations-filter-file data/parkinson_disease_explanations_filter.txt \
    --disease "Parkinson disease"
```

### Filter the chains for Parkinson Disease

Run this command (after creating the evidence chains file) to filter chains by gene and pathway for "Parkinson disease".

```shell
$ healx-gene-pathway-chains-filter \
    evidence-chains.jsonl \
    data/parkinson-disease-filter/genes.txt \
    data/parkinson-disease-filter/pathways.txt \
    data/parkinson-disease-filter/predictions.txt \
    --filtered-evidence-chains-file filtered-evidence-chains.txt
```
