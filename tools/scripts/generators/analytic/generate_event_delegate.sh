#!/usr/bin/env bash
#
# Retired analytics generator.
#
# Analytics event generation was removed with the optional analytics module. This
# entry point remains only to fail clearly if old automation invokes it.

set -euo pipefail

echo "Analytics event generation is retired; no files were generated." >&2
exit 1
