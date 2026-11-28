import unittest

from verify_apk_certificate import verify_certificates


class VerifyApkCertificateTest(unittest.TestCase):
	def test_valid_signer_formats_and_schemes(self):
		digest = "ab" * 32
		for output in (
			f"Signer #1 certificate SHA-256 digest: {digest}",
			f"V2 Signer: certificate SHA-256 digest: {digest}",
			f"V2 Signer: certificate SHA-256 digest: {digest}\nV3.0 Signer: certificate SHA-256 digest: {digest}",
		):
			with self.subTest(output=output):
				verify_certificates(output, ":".join(["AB"] * 32))

	def test_rejects_missing_invalid_and_mismatched_digests(self):
		digest = "ab" * 32
		for output, expected in (
			("", digest),
			(f"Source Stamp Signer certificate SHA-256 digest: {digest}", digest),
			("Signer #1 certificate SHA-256 digest: invalid", digest),
			(f"Signer #1 certificate SHA-256 digest: {digest}", "invalid"),
			(f"Signer #1 certificate SHA-256 digest: {digest}\nSigner #2 certificate SHA-256 digest: {'cd' * 32}", digest),
			(f"V2 Signer: certificate SHA-256 digest: {digest}\nV3.0 Signer: certificate SHA-256 digest: invalid", digest),
			(f"V2 Signer: certificate SHA-256 digest: {digest}\nV3.0 Signer: certificate SHA-256 digest: {'cd' * 32}", digest),
		):
			with self.subTest(output=output, expected=expected), self.assertRaises(ValueError):
				verify_certificates(output, expected)

	def test_release_requires_one_signer_across_schemes(self):
		digest = "ab" * 32
		verify_certificates(f"V2 Signer: certificate SHA-256 digest: {digest}\nV3.0 Signer: certificate SHA-256 digest: {digest}", digest, single_signer=True)
		for prefix in ("Signer #2", "V2 Signer #2:", "Signer #1"):
			output = f"Signer #1 certificate SHA-256 digest: {digest}\n{prefix} certificate SHA-256 digest: {digest}"
			with self.subTest(prefix=prefix), self.assertRaises(ValueError):
				verify_certificates(output, digest, single_signer=True)

	def test_ignores_source_stamp(self):
		digest = "ab" * 32
		verify_certificates(f"V2 Signer: certificate SHA-256 digest: {digest}\nSource Stamp Signer certificate SHA-256 digest: {'cd' * 32}", digest)


if __name__ == "__main__":
	unittest.main()
