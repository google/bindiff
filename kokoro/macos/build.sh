#!/bin/bash

# Fail on error, display commands being run
set -ex

if [[ $1 == release ]]; then
  echo "Release build"
else
  echo "Continuous integration build"
fi

bindiff_release=8

# Set Xcode version to well-known value
export DEVELOPER_DIR=/Applications/Xcode_15.1.app/Contents/Developer

build_dir=${PWD}/build
mkdir -p "${build_dir}"

# Verify/extract dependencies
pushd "${KOKORO_GFILE_DIR}"
cat << 'EOF' | shasum -c
0b5def3f77617b2ce0ec6701f96e4123a7b14bd0b8a5674ab0556c364ff7cb52  cmake-3.25.2-macos-universal.tar.gz
EOF
popd
tar --strip-components=3 -C "${build_dir}" -xzf \
  "${KOKORO_GFILE_DIR}/cmake-3.25.2-macos-universal.tar.gz"
export PATH=${build_dir}/bin:${PATH}

# Build BinDiff
src_dir=${KOKORO_ARTIFACTS_DIR}/git
out_dir=${build_dir}
deps_dir=${build_dir}

# Symlink extra Binary Ninja API dependency
mkdir -p "${KOKORO_PIPER_DIR}/google3/third_party/binaryninja_api/third_party"
ln -s "${KOKORO_PIPER_DIR}/google3/third_party/jsoncpp" \
  "${KOKORO_PIPER_DIR}/google3/third_party/binaryninja_api/third_party/jsoncpp"

pushd "${build_dir}"
cmake "${src_dir}/bindiff" \
  -DFETCHCONTENT_FULLY_DISCONNECTED=ON \
  "-DFETCHCONTENT_SOURCE_DIR_ABSL=${KOKORO_ARTIFACTS_DIR}/git/absl" \
  "-DFETCHCONTENT_SOURCE_DIR_GOOGLETEST=${KOKORO_ARTIFACTS_DIR}/git/googletest" \
  "-DFETCHCONTENT_SOURCE_DIR_PROTOBUF=${KOKORO_ARTIFACTS_DIR}/git/protobuf" \
  "-DFETCHCONTENT_SOURCE_DIR_BINARYNINJAAPI=${KOKORO_PIPER_DIR}/google3/third_party/binaryninja_api" \
  "-DFETCHCONTENT_SOURCE_DIR_SQLITE=${KOKORO_PIPER_DIR}/google3/third_party/sqlite/src" \
  "-DCMAKE_OSX_ARCHITECTURES=arm64;x86_64" \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_RULE_MESSAGES=OFF \
  "-DCMAKE_INSTALL_PREFIX=${out_dir}" \
  "-DBOOST_ROOT=${KOKORO_PIPER_DIR}/google3/third_party/boost/do_not_include_from_google3_only_third_party/boost" \
  "-DIdaSdk_ROOT_DIR=${KOKORO_PIPER_DIR}/google3/third_party/idasdk"
cmake --build . --config Release -- "-j$(sysctl -n hw.logicalcpu)"
ctest --build-config Release --output-on-failure -R '^[A-Z]'
cmake --install . --config Release --strip
popd

[[ $1 != release ]] && exit 0

# Release build, code sign and notarize the artifacts
echo "Code signing artifacts..."
codesign --force \
  --options runtime \
  --timestamp \
  --sign "Developer ID Application: Google LLC (EQHXZ8M8AV)" \
  --keychain "${HOME}/Library/Keychains/MacApplicationSigning.keychain" \
  "${out_dir}/bindiff-prefix/bindiff" \
  "${out_dir}/bindiff-prefix/bindiff_config_setup" \
  "${out_dir}/bindiff-prefix/bindiff_launcher_macos" \
  "${out_dir}/bindiff-prefix/bindiff${bindiff_release}_ida.dylib" \
  "${out_dir}/bindiff-prefix/bindiff${bindiff_release}_ida64.dylib"
