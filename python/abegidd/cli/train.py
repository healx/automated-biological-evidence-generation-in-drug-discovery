import logging
from pathlib import Path

import click

from abegidd.system import find_jar, run_anyburl

logger = logging.getLogger(__name__)


@click.command()
@click.argument(
    "data-dir", type=click.Path(dir_okay=True, file_okay=False, exists=True),
)
def _train(data_dir: str) -> None:
    logger.setLevel(level=logging.DEBUG)
    jar_files = find_jar(Path.cwd())
    train_class_path = "de.unima.ki.anyburl.LearnReinforced"
    config_path = Path.cwd() / data_dir / "config-train.properties"

    results = Path.cwd() / "results"
    if not results.is_dir():
        results.mkdir()

    logger.info("running AnyBURL")
    run_anyburl(jar_files, train_class_path, config_path)


if __name__ == "__main__":
    _train()
