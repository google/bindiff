#!/bin/bash

# Fail on error, display commands being run
set -ex

src_dir=/build/src/git
out_dir=/build/out
deps_dir=/build/deps

trap "chmod -R ugo+rX \"${out_dir}\"" EXIT

cd "${src_dir}/bindiff/java"

# Symlink if the file does not exist (Copybara will not export symlinks that
# point outside of the repo).
ln -s "${src_dir}/binexport/binexport2.proto" \
  ui/src/main/proto/binexport2.proto > /dev/null 2>&1 || true

export GHIDRA_INSTALL_DIR="${deps_dir}/ghidra"
export YFILES_DIR="${deps_dir}/yfiles/v2_17"
gradle --offline obfuscatedJar

mv ui/build/libs/bindiff-ui-all.out.jar "${out_dir}/bindiff.jar"
