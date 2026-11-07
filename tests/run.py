"""Compile, test mechanics and IDE XML persistence, and render a preview."""
import argparse
import os
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--compile-only", action="store_true", help="Build plugin and test classes without executing tests")
args = parser.parse_args()
sys.path.insert(0, str(root))
from sdk import locate
ide, java, info = locate()
subprocess.run([sys.executable, str(root / "build.py")], check=True)
out = root / "build/test-classes"
out.mkdir(exist_ok=True)
cp = os.pathsep.join([str(root / "build/classes"), *map(str, sorted((ide / "lib").glob("*.jar")))])
subprocess.run([str(java / "javac"), "--release", "25", "-encoding", "UTF-8", "-cp", cp,
                "-d", str(out), *map(str, sorted((root / "tests").glob("*.java")))], check=True)
if args.compile_only:
    print("Plugin and test classes compiled; no tests executed.")
    raise SystemExit(0)
subprocess.run([str(java / "java"), "-Djava.awt.headless=true", "-cp", str(out) + os.pathsep + cp,
                "dev.codepet.PetChecks", str(root / "build/reports")], check=True)

subprocess.run([str(java / "java"), "-Djava.awt.headless=true", "-cp", str(out) + os.pathsep + cp,
                "dev.codepet.SdkChecks"], check=True)
subprocess.run([str(java / "java"), "-Djava.awt.headless=true", "-cp", str(out) + os.pathsep + cp,
                "dev.codepet.GameplayChecks"], check=True)
