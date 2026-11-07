"""Run exactly the same checks locally and in GitHub Actions, fail on the first error."""
import json
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
REPORTS = ROOT / "build/reports"
REPORTS.mkdir(parents=True, exist_ok=True)
steps = [
    ("python", [sys.executable, "-m", "unittest", "discover", "-s", "tests", "-p", "test_*.py", "-v"]),
    ("java", [sys.executable, "tests/run.py"]),
    ("package", [sys.executable, "scripts/verify_package.py"]),
]
results = []
for label, command in steps:
    result = subprocess.run(command, cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    (REPORTS / f"{label}.log").write_text(result.stdout)
    print(result.stdout, end="", flush=True)
    results.append({"name": label, "passed": result.returncode == 0})
    (REPORTS / "summary.json").write_text(json.dumps(results, indent=2) + "\n")
    if result.returncode:
        raise SystemExit(result.returncode)
print("All local/CI checks passed. Reports: build/reports/")
