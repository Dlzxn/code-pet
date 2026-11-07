"""Verify install layout, descriptor references, SDK compatibility, and shipped payload."""
from io import BytesIO
from pathlib import Path
from zipfile import ZipFile
import xml.etree.ElementTree as ET


def verify(root):
    expected = ET.parse(root / "resources/META-INF/plugin.xml").getroot()
    version = expected.findtext("version")
    archive_path = root / f"dist/code-pet-{version}.zip"
    with ZipFile(archive_path) as archive:
        assert archive.testzip() is None, "Corrupt plugin ZIP"
        assert archive.namelist() == ["code-pet/lib/code-pet.jar"], "Unexpected plugin install layout"
        with ZipFile(BytesIO(archive.read(archive.namelist()[0]))) as jar:
            assert jar.testzip() is None, "Corrupt plugin JAR"
            descriptor = ET.fromstring(jar.read("META-INF/plugin.xml"))
            assert descriptor.findtext("version") == version, "Version mismatch"
            assert descriptor.find("idea-version").attrib == {"since-build": "263", "until-build": "263.*"}
            references = []
            for extension in descriptor.find("extensions"):
                references.append(extension.get("implementation") or extension.get("factoryClass"))
            references.extend(item.get("class") for item in descriptor.find("applicationListeners"))
            references.extend(item.get("class") for item in descriptor.find("actions"))
            for reference in references:
                assert reference and reference.replace(".", "/") + ".class" in jar.namelist(), f"Missing extension: {reference}"
            for name in jar.namelist():
                assert name.startswith(("dev/codepet/", "META-INF/")), f"Unexpected payload: {name}"
                assert not any(part in name for part in ("PetChecks", "SdkChecks", "Test", "code-pet.xml")), f"Test/private data in JAR: {name}"
    print(f"Verified {archive_path.name}: descriptor, extensions, actions, payload and ZIP integrity")


if __name__ == "__main__":
    verify(Path(__file__).resolve().parents[1])
