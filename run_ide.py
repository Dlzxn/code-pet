"""Run PyCharm with an isolated configuration and this plugin. Close it normally."""
import os
from pathlib import Path
import subprocess
import sys
import zipfile
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parent
ide = Path(os.environ.get("PYCHARM_HOME", "/Applications/PyCharm.app/Contents"))
subprocess.run([sys.executable, str(root / "build.py")], check=True)
sandbox = root / "build/sandbox"
for name in ("config", "system", "plugins", "log", "project"):
    (sandbox / name).mkdir(parents=True, exist_ok=True)
version = ET.parse(root / "resources/META-INF/plugin.xml").findtext("version")
with zipfile.ZipFile(root / f"dist/code-pet-{version}.zip") as archive:
    archive.extractall(sandbox / "plugins")
properties = sandbox / "idea.properties"
properties.write_text("\n".join(f"idea.{name}.path={sandbox / name}"
                                  for name in ("config", "system", "plugins", "log")) + "\n")
(sandbox / "project/example.py").touch(exist_ok=True)
env = os.environ.copy()
env["PYCHARM_PROPERTIES"] = str(properties)
print(f"Starting isolated PyCharm. Settings, plugins and logs: {sandbox}", flush=True)
result = subprocess.run([str(ide / "MacOS/pycharm"), str(sandbox / "project")], env=env)
if result.returncode:
    raise SystemExit("PyCharm could not start. Check the output above and build/sandbox/log. "
                     "Run this command from a regular macOS terminal outside the agent sandbox.")
