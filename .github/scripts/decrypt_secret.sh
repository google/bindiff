#!/bin/bash
#
# Decrypts the checked-in IDA SDK. In order to not leak the passphrase in the
# workflow log, it is set as an environment variable before being passed to
# GPG.

set -e

gpg --quiet --batch --yes --decrypt \
  "--passphrase=${IDASDK_SECRET}" \
  --output "${RUNNER_WORKSPACE}/build/idasdk75.zip" \
  "${GITHUB_WORKSPACE}/third_party/idasdk75.zip.gpg"
