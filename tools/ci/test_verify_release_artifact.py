import hashlib
import tempfile
import unittest
from pathlib import Path

from verify_release_artifact import verify_artifact, verify_checksums, verify_metadata


class VerifyReleaseArtifactTest(unittest.TestCase):
	def setUp(self):
		self.artifact = {
			"id": 42,
			"expired": False,
			"name": "signed-android-intermediate-123-1",
			"workflow_run": {"id": 123, "head_sha": "abc"},
		}

	def test_failed_delivery_rerun_reuses_build(self):
		verify_artifact(self.artifact, "42", "123", "1", "2", "abc")

	def test_full_rerun_uses_new_build(self):
		self.artifact["name"] = "signed-android-intermediate-123-2"
		verify_artifact(self.artifact, "42", "123", "2", "2", "abc")

	def test_rejects_missing_or_mismatched_identity(self):
		for artifact_id, run_id, attempt, current, sha in [
			("", "123", "1", "2", "abc"),
			("42", "124", "1", "2", "abc"),
			("42", "123", "2", "1", "abc"),
			("42", "123", "1", "2", "wrong"),
		]:
			with self.subTest(artifact_id=artifact_id, run_id=run_id, attempt=attempt, sha=sha):
				with self.assertRaises(ValueError):
					verify_artifact(self.artifact, artifact_id, run_id, attempt, current, sha)

	def test_rejects_expired_artifact(self):
		self.artifact["expired"] = True
		with self.assertRaises(ValueError):
			verify_artifact(self.artifact, "42", "123", "1", "2", "abc")

	def test_checksums_require_exact_manifest_and_contents(self):
		with tempfile.TemporaryDirectory() as directory:
			root = Path(directory)
			names = ("Keeper.apk", "mapping.txt", "metadata.txt")
			for name in names:
				(root / name).write_bytes(name.encode())
			manifest = "".join(f"{hashlib.sha256(name.encode()).hexdigest()}  {name}\n" for name in names)
			(root / "SHA256SUMS").write_text(manifest, encoding="utf-8")
			verify_checksums(root)
			(root / "Keeper.apk").write_bytes(b"corrupt")
			with self.assertRaises(ValueError):
				verify_checksums(root)
			(root / "Keeper.apk").write_bytes(b"Keeper.apk")
			(root / "SHA256SUMS").write_text(manifest.splitlines()[0] + "\n", encoding="utf-8")
			with self.assertRaises(ValueError):
				verify_checksums(root)

	def test_metadata_matches_exactly(self):
		expected = {"commit": "abc", "run_id": "123"}
		with tempfile.TemporaryDirectory() as directory:
			path = Path(directory) / "metadata.txt"
			path.write_text("commit=abc\nrun_id=123\n", encoding="utf-8")
			verify_metadata(path, expected)
			for contents in ("commit=wrong\nrun_id=123\n", "commit=abc\ncommit=abc\nrun_id=123\n", "commit=abc\nrun_id=123\nextra=x\n"):
				path.write_text(contents, encoding="utf-8")
				with self.subTest(contents=contents), self.assertRaises(ValueError):
					verify_metadata(path, expected)


if __name__ == "__main__":
	unittest.main()
