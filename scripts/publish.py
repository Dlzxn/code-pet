"""Commit project source and push via the laptop's existing SSH configuration. Tests run in CI."""
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
PATHS = [".github", ".gitignore", "README.md", "CHANGELOG.md", "docs", "resources", "src", "tests", "scripts", "build.py", "run_ide.py", "sdk.py"]


def git(*args, **kwargs):
    return subprocess.run(["git", *args], cwd=ROOT, check=True, **kwargs)


def main():
    branch = git("branch", "--show-current", capture_output=True, text=True).stdout.strip()
    if branch != "main":
        raise SystemExit(f"Expected main, found {branch!r}; switch to the intended branch before publishing.")
    remote = git("remote", "get-url", "origin", capture_output=True, text=True).stdout.strip()
    if remote not in ("git@github.com:Dlzxn/code-pet.git", "ssh://git@github.com/Dlzxn/code-pet.git"):
        raise SystemExit(f"Unexpected origin: {remote}. Expected Dlzxn/code-pet over SSH.")
    git("add", "--", *PATHS)
    git("diff", "--cached", "--check")
    changed = subprocess.run(["git", "diff", "--cached", "--quiet"], cwd=ROOT)
    if changed.returncode == 1:
        git("commit", "-m", "Fix editor keys, add shared sofa scene and simplify companion controls")
    elif changed.returncode != 0:
        raise SystemExit(changed.returncode)
    git("push", "-u", "origin", "main")
    print("Pushed. CI starts automatically: https://github.com/Dlzxn/code-pet/actions")
    print("Local test: close the sandbox IDE, then run python3 run_ide.py")


if __name__ == "__main__":
    try:
        main()
    except subprocess.CalledProcessError as error:
        raise SystemExit(error.returncode)
