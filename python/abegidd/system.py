import logging
import re
import subprocess
from pathlib import Path

from abegidd.iterables import first

logger = logging.getLogger(__file__)

ANYBURL_JAR_REGEX: str = r"anyburl-[0-9]+\.[0-9]+\.[0-9]+\.jar"


def find_jar(basepath: Path) -> Path:
    """
    Find JAR file matching maven AnyBURL pattern

    :param basepath: directory to start search
    :return: path to JAR file
    """
    try:
        return first(
            pp
            for pp in basepath.glob("../**/target/*.jar")
            if re.match(ANYBURL_JAR_REGEX, pp.name)
        )
    except ValueError as value_exception:
        raise Exception(
            "No .jar files found, have you run `$ make build-java` ? "
            "Original Exception: %s",
            value_exception,
        ) from value_exception


def run_anyburl(jar_path: Path, class_path: str, config_path: Path, memory: str = "4G"):
    """
    Run AnyBURL java application from python

    :param jar_path: JAR file to call
    :param class_path: Path to class in JAR
    :param config_path: config properties file
    :param memory: jvm heap memory max size
    """

    args = [
        "java",
        f"-Xmx{memory}",
        "-cp",
        str(jar_path.resolve()),
        class_path,
        str(config_path.resolve()),
    ]

    logger.info("Calling AnyBURL: %s", args)
    print("Calling AnyBURL: %s", " ".join(args))

    with subprocess.Popen(
        args,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        universal_newlines=True,
    ) as process:
        if process.stdout is not None:
            while line := process.stdout.readline():
                print(line.strip())
        process.wait()

        if process.returncode != 0:
            error = process.stderr.read() if process.stderr is not None else ""
            stdout = process.stdout.read() if process.stdout is not None else ""
            raise ChildProcessError(
                "AnyBURL exited with non-zero code:", process.returncode, error, stdout
            )
