"""Build against the installed PyCharm SDK, without downloading dependencies."""
import os
from pathlib import Path
import shutil
import subprocess
import zipfile
from sdk import locate
import xml.etree.ElementTree as ET


def main():
    root = Path(__file__).resolve().parent
    ide, java, info = locate()
    out = root / "build/classes"
    if out.exists():
        shutil.rmtree(out)
    out.mkdir(parents=True)
    classpath = os.pathsep.join(str(p) for p in sorted((ide / "lib").glob("*.jar")))
    subprocess.run([str(java / "javac"), "--release", "25", "-encoding", "UTF-8", "-cp", classpath,
                    "-d", str(out), *map(str, sorted((root / "src").rglob("*.java")))], check=True)
    jar = root / "build/code-pet.jar"
    with zipfile.ZipFile(jar, "w", zipfile.ZIP_DEFLATED) as archive:
        for base in (out, root / "resources"):
            for path in sorted(base.rglob("*")):
                if path.is_file():
                    archive.write(path, path.relative_to(base))
    dist = root / "dist"
    dist.mkdir(exist_ok=True)
    version = ET.parse(root / "resources/META-INF/plugin.xml").findtext("version")
    target = dist / f"code-pet-{version}.zip"
    with zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.write(jar, "code-pet/lib/code-pet.jar")
    print(f"Built for PyCharm {info['version']} ({info['buildNumber']}): {target}")


if __name__ == "__main__":
    main()
