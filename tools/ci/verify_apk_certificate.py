import os
import re
import sys


SIGNER_DIGEST = re.compile(
	r"^(?:Signer #[0-9]+|V[0-9]+(?:\.[0-9]+)? Signer(?: #[0-9]+)?:) certificate SHA-256 digest:(.*)$"
)


def normalize_digest(value):
	return re.sub(r"[:\s]", "", value).upper()


def verify_certificates(output, expected, single_signer=False):
	expected = normalize_digest(expected)
	if not re.fullmatch(r"[0-9A-F]{64}", expected):
		raise ValueError("Invalid expected APK certificate digest")
	lines = [line for line in output.splitlines() if SIGNER_DIGEST.fullmatch(line)]
	if single_signer and (sum(line.startswith("Signer #") for line in lines) > 1 or any(re.search(r"Signer #(?!1\b)[0-9]+", line) for line in lines)):
		raise ValueError("Multiple APK signers are not allowed")
	digests = [normalize_digest(SIGNER_DIGEST.fullmatch(line).group(1)) for line in lines]
	if not digests or any(not re.fullmatch(r"[0-9A-F]{64}", digest) or digest != expected for digest in digests):
		raise ValueError("Missing, invalid, or mismatched APK signer certificate digest")


if __name__ == "__main__":
	try:
		verify_certificates(sys.stdin.read(), os.environ.get(sys.argv[1], ""), single_signer="--single-signer" in sys.argv[2:])
	except ValueError as error:
		raise SystemExit(f"::error::{error}")
