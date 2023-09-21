#!/bin/bash

# Fail on any error, display commands being run
set -ex

src_dir=/build/src/git
out_dir=/build/out
deps_dir=/build/deps

cd "${out_dir}"
trap "chmod -R ugo+rwX \"${out_dir}\"" EXIT

cmake "${src_dir}/bindiff/" \
  -G "Ninja" \
  -DFETCHCONTENT_FULLY_DISCONNECTED=ON \
  -DCMAKE_BUILD_TYPE=Release \
  "-DCMAKE_INSTALL_PREFIX=${out_dir}" \
  "-DIdaSdk_ROOT_DIR=${deps_dir}/idasdk"
cmake --build . --config Release
ctest --build-config Release --output-on-failure -R '^[A-Z]'
cmake --install . --config Release --strip
