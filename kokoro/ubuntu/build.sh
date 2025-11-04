#!/bin/bash

# Fail on any error, display commands being run
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
  -e "KOKORO_PIPER_CHANGELIST=${KOKORO_PIPER_CHANGELIST}" \
  -v "${KOKORO_ARTIFACTS_DIR}/git/binexport":/build/deps/binexport \
  -v "${KOKORO_PIPER_DIR}/google3/third_party/idasdk":/build/deps/idasdk \
  -v "${KOKORO_ARTIFACTS_DIR}/git/absl":/build/out/_deps/absl-src \
  -v "${KOKORO_ARTIFACTS_DIR}/git/googletest":/build/out/_deps/googletest-src \
  -v "${KOKORO_ARTIFACTS_DIR}/git/protobuf":/build/out/_deps/protobuf-src \
  -v "${KOKORO_PIPER_DIR}/google3/third_party/sqlite/src":/build/out/_deps/sqlite-src \
  -v "${build_dir}/out":/build/out \
  -v "${KOKORO_ARTIFACTS_DIR}":/build/src \
  -v "${KOKORO_GFILE_DIR}":/build/src/gfile \
  gcr.io/zynamics-build/debian9-clang \
  src/gfile/dockerized_build.sh
