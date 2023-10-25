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
