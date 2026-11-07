"""Locate a macOS or Linux PyCharm SDK consistently for build, tests and CI."""
import json
import os
from pathlib import Path


def locate():
    ide = Path(os.environ.get("PYCHARM_HOME", "/Applications/PyCharm.app/Contents")).resolve()
    info_path = next((p for p in (ide / "Resources/product-info.json", ide / "product-info.json") if p.is_file()), None)
    if info_path is None:
        raise SystemExit("PyCharm SDK not found. Set PYCHARM_HOME to Contents (macOS) or the extracted IDE directory (Linux).")
    info = json.loads(info_path.read_text())
    if info.get("buildNumber") != "263.7307.206":
        raise SystemExit(f"Expected PyCharm SDK build 263.7307.206, got {info.get('buildNumber')}. Update SDK pin and compatibility together.")
    candidates = [ide / "jbr/Contents/Home/bin", ide / "jbr/bin"]
    if os.environ.get("JAVA_HOME"):
        candidates.append(Path(os.environ["JAVA_HOME"]) / "bin")
    java = next((p for p in candidates if (p / "javac").is_file() and (p / "java").is_file()), None)
    if java is None:
        raise SystemExit("Java 25+ JDK not found in the SDK. Set JAVA_HOME to a JDK installation.")
    return ide, java, info
