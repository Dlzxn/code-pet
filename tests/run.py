"""Compile, test mechanics and IDE XML persistence, and render a preview."""
import os
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(root))
from sdk import locate
ide, java, info = locate()
subprocess.run([sys.executable, str(root / "build.py")], check=True)
out = root / "build/test-classes"
out.mkdir(exist_ok=True)
cp = os.pathsep.join([str(root / "build/classes"), *map(str, sorted((ide / "lib").glob("*.jar")))])
subprocess.run([str(java / "javac"), "--release", "25", "-encoding", "UTF-8", "-cp", cp,
                "-d", str(out), *map(str, sorted((root / "tests").glob("*.java")))], check=True)
subprocess.run([str(java / "java"), "-Djava.awt.headless=true", "-cp", str(out) + os.pathsep + cp,
                "dev.codepet.PetChecks", str(root / "build/reports")], check=True)

subprocess.run([str(java / "java"), "-Djava.awt.headless=true", "-cp", str(out) + os.pathsep + cp,
                "dev.codepet.SdkChecks"], check=True)
