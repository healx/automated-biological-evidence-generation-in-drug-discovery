import logging
from pathlib import Path

import click

from abegidd.system import find_jar, run_anyburl

logger = logging.getLogger(__name__)


@click.command()
@click.argument(
    "data-dir", type=click.Path(dir_okay=True, file_okay=False, exists=True),
)
def _predict(data_dir: str) -> None:
    logger.setLevel(level=logging.DEBUG)
    jar_files = find_jar(Path.cwd())
    predict_class_path = "de.unima.ki.anyburl.Apply"
    config_path = Path.cwd() / data_dir / "config-predict.properties"

    logger.info("running AnyBURL")
    run_anyburl(jar_files, predict_class_path, config_path)


if __name__ == "__main__":
    _predict()
