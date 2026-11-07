"""Install the exact Linux SDK used by CI, checking the publisher's SHA256."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import tarfile
import tempfile
import urllib.request

VERSION = "2026.3"
BUILD = "263.7307.206"
URL = f"https://download.jetbrains.com/python/pycharm-{VERSION}.tar.gz"
DEST = Path(".ci-sdk").resolve()


def download(url, target):
    request = urllib.request.Request(url, headers={"User-Agent": "CodePet-CI/0.3"})
    with urllib.request.urlopen(request, timeout=120) as source, target.open("wb") as output:
        shutil.copyfileobj(source, output)


def main():
    info_path = DEST / "product-info.json"
    cached = info_path.is_file() and json.loads(info_path.read_text()).get("buildNumber") == BUILD
    if not cached:
        with tempfile.TemporaryDirectory(prefix="code-pet-sdk-") as temp:
            staging = Path(temp)
            checksum = staging / "sdk.sha256"
            archive = staging / "sdk.tar.gz"
            download(URL + ".sha256", checksum)
            expected = checksum.read_text().split()[0].lower()
            if not re.fullmatch(r"[0-9a-f]{64}", expected):
                raise SystemExit("Invalid SDK checksum from JetBrains")
            download(URL, archive)
            with archive.open("rb") as source:
                actual = hashlib.file_digest(source, "sha256").hexdigest()
            if actual != expected:
                raise SystemExit("SDK checksum mismatch")
            unpacked = staging / "extracted"
            with tarfile.open(archive) as tar:
                tar.extractall(unpacked, filter="data")
            roots = [p.parent for p in unpacked.glob("*/product-info.json")]
            if len(roots) != 1 or json.loads((roots[0] / "product-info.json").read_text()).get("buildNumber") != BUILD:
                raise SystemExit("Unexpected PyCharm SDK layout/build")
            if DEST.exists():
                shutil.rmtree(DEST)
            shutil.move(str(roots[0]), DEST)
    if os.environ.get("GITHUB_ENV"):
        with open(os.environ["GITHUB_ENV"], "a") as environment:
            environment.write(f"PYCHARM_HOME={DEST}\n")
    print(f"PyCharm SDK {BUILD}: {DEST}")


if __name__ == "__main__":
    main()
