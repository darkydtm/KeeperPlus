#!/usr/bin/env python3
import hashlib
import json
import os
import re
import sys
from pathlib import Path


def positive_integer(value):
	if not re.fullmatch(r"[1-9][0-9]*", value):
		raise ValueError("Expected a positive integer")
	return int(value)


def verify_artifact(artifact, artifact_id, run_id, build_attempt, current_attempt, sha):
	artifact_id = positive_integer(artifact_id)
	run_id = positive_integer(run_id)
	build_attempt = positive_integer(build_attempt)
	current_attempt = positive_integer(current_attempt)
	if build_attempt > current_attempt:
		raise ValueError("Build attempt is newer than delivery attempt")
	if (
		artifact.get("id") != artifact_id
		or artifact.get("expired") is not False
		or artifact.get("name") != f"signed-android-intermediate-{run_id}-{build_attempt}"
		or (artifact.get("workflow_run") or {}).get("id") != run_id
		or (artifact.get("workflow_run") or {}).get("head_sha") != sha
	):
		raise ValueError("Artifact identity does not match the successful build")


def verify_metadata(path, expected):
	lines = Path(path).read_text(encoding="utf-8").splitlines()
	entries = {}
	for line in lines:
		key, separator, value = line.partition("=")
		if not separator or key in entries:
			raise ValueError("Invalid or duplicate metadata field")
		entries[key] = value
	if entries != expected:
		raise ValueError("Artifact metadata does not match the release request")


def verify_checksums(directory):
	directory = Path(directory)
	names = {"Keeper.apk", "mapping.txt", "metadata.txt"}
	files = list(directory.iterdir())
	if {path.name for path in files} != names | {"SHA256SUMS"} or any(path.is_symlink() or not path.is_file() for path in files):
		raise ValueError("Unexpected release artifact contents")
	manifest = (directory / "SHA256SUMS").read_text(encoding="utf-8").splitlines()
	checksums = {}
	for line in manifest:
		match = re.fullmatch(r"([0-9a-f]{64})  (Keeper\.apk|mapping\.txt|metadata\.txt)", line)
		if not match or match[2] in checksums:
			raise ValueError("Invalid checksum manifest")
		checksums[match[2]] = match[1]
	if set(checksums) != names:
		raise ValueError("Incomplete checksum manifest")
	for name, digest in checksums.items():
		with (directory / name).open("rb") as source:
			if hashlib.file_digest(source, "sha256").hexdigest() != digest:
				raise ValueError(f"Checksum mismatch: {name}")


def main():
	env = os.environ
	if sys.argv[1:] == ["artifact"]:
		verify_artifact(json.load(sys.stdin), env["ARTIFACT_ID"], env["GITHUB_RUN_ID"], env["BUILD_ATTEMPT"], env["GITHUB_RUN_ATTEMPT"], env["GITHUB_SHA"])
	elif sys.argv[1:] == ["metadata"]:
		verify_checksums("release")
		verify_metadata("release/metadata.txt", {
			"commit": env["GITHUB_SHA"],
			"version_name": env["VERSION_NAME"],
			"version_code": env["VERSION_CODE"],
			"run_id": env["GITHUB_RUN_ID"],
			"run_attempt": env["BUILD_ATTEMPT"],
		})
	else:
		raise ValueError("Expected artifact or metadata")


if __name__ == "__main__":
	try:
		main()
	except (KeyError, OSError, ValueError) as error:
		sys.exit(f"::error::{error}")
