"""Offline tests for SDK detection, checksum/extraction and package validation."""
import importlib.util
import io
import json
import os
from pathlib import Path
import sys
import tarfile
import tempfile
import unittest
from unittest.mock import patch
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))
import sdk


def module(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    result = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(result)
    return result


installer = module("installer", ROOT / "scripts/install_ci_sdk.py")
packager = module("packager", ROOT / "scripts/verify_package.py")


class ToolingChecks(unittest.TestCase):
    def test_sdk_mac_and_linux_layouts(self):
        for layout in ("mac", "linux"):
            with self.subTest(layout=layout), tempfile.TemporaryDirectory() as temp:
                root = Path(temp)
                info = root / ("Resources/product-info.json" if layout == "mac" else "product-info.json")
                info.parent.mkdir(parents=True, exist_ok=True)
                info.write_text(json.dumps({"buildNumber": "263.7307.206"}))
                binaries = root / ("jbr/Contents/Home/bin" if layout == "mac" else "jbr/bin")
                binaries.mkdir(parents=True)
                (binaries / "java").touch()
                (binaries / "javac").touch()
                with patch.dict(os.environ, {"PYCHARM_HOME": temp}):
                    self.assertEqual(sdk.locate()[:2], (root.resolve(), binaries.resolve()))
                    info.write_text('{"buildNumber":"264.1"}')
                    with self.assertRaisesRegex(SystemExit, "Expected PyCharm SDK"):
                        sdk.locate()

    def test_missing_sdk_has_actionable_error(self):
        with tempfile.TemporaryDirectory() as temp, patch.dict(os.environ, {"PYCHARM_HOME": temp}):
            with self.assertRaisesRegex(SystemExit, "PYCHARM_HOME"):
                sdk.locate()

    def sdk_tar(self, build="263.7307.206"):
        buffer = io.BytesIO()
        with tarfile.open(fileobj=buffer, mode="w:gz") as archive:
            data = json.dumps({"buildNumber": build}).encode()
            item = tarfile.TarInfo("pycharm/product-info.json")
            item.size = len(data)
            archive.addfile(item, io.BytesIO(data))
        return buffer.getvalue()

    def install(self, data, checksum=None):
        import hashlib
        expected = checksum or hashlib.sha256(data).hexdigest()

        def download(url, target):
            target.write_bytes((expected + "  sdk.tar.gz").encode() if url.endswith(".sha256") else data)

        with tempfile.TemporaryDirectory() as temp:
            destination = Path(temp) / "sdk"
            environment = Path(temp) / "environment"
            with patch.object(installer, "DEST", destination), patch.object(installer, "download", side_effect=download), patch.dict(os.environ, {"GITHUB_ENV": str(environment)}):
                installer.main()
                self.assertEqual(json.loads((destination / "product-info.json").read_text())["buildNumber"], installer.BUILD)
                self.assertIn(str(destination), environment.read_text())
                with patch.object(installer, "download", side_effect=AssertionError("Valid cache should be reused")):
                    installer.main()

    def test_install_validates_and_reuses_cache(self):
        self.install(self.sdk_tar())

    def test_install_rejects_corruption(self):
        with self.assertRaisesRegex(SystemExit, "checksum mismatch"):
            self.install(self.sdk_tar(), "0" * 64)

    def test_install_rejects_wrong_sdk(self):
        with self.assertRaisesRegex(SystemExit, "Unexpected PyCharm SDK"):
            self.install(self.sdk_tar("264.1"))

    def test_install_rejects_invalid_checksum(self):
        with self.assertRaisesRegex(SystemExit, "Invalid SDK checksum"):
            self.install(self.sdk_tar(), "not-a-hash")

    def test_descriptor_and_installer_version_agree(self):
        import xml.etree.ElementTree as ET
        descriptor = ET.parse(ROOT / "resources/META-INF/plugin.xml").getroot()
        self.assertTrue(installer.BUILD.startswith(descriptor.find("idea-version").get("since-build") + "."))
        self.assertNotIn("code-pet-0.", (ROOT / "build.py").read_text())


if __name__ == "__main__":
    unittest.main()
