#!/bin/bash

# Fail on error, display commands being run
set -ex

if [ "$1" == "release" ]; then
  echo "Release build"
else
  echo "Continuous integration build"
fi

build_dir=${PWD}/build
mkdir -p "${build_dir}"

# Setup Docker access to Google Container Registry
cp "${KOKORO_KEYSTORE_DIR}/73933_zynamics-build" zynamics_build_key.json
gcloud auth activate-service-account --key-file=zynamics_build_key.json
gcloud auth configure-docker -q

chmod 755 "${KOKORO_GFILE_DIR}/dockerized_build.sh"
docker run \
  -w /build \
  -v "${KOKORO_ARTIFACTS_DIR}":/build/src \
  -v "${KOKORO_GFILE_DIR}":/build/src/gfile \
  -v "${KOKORO_PIPER_DIR}/google3/third_party/ghidra":/build/deps/ghidra \
  -v "${KOKORO_PIPER_DIR}/google3/third_party/java/yfiles":/build/deps/yfiles \
  -v "${build_dir}/out":/build/out \
  gcr.io/zynamics-build/debian10-java11 \
  src/gfile/dockerized_build.sh
