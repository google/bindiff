#!/bin/bash
# Copyright 2011-2022 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Updates the Binary Ninja API stub file

# Exit on error
set -e

# Portable "readlink -e"
canonical_path()
(
  file=$1
  cd $(dirname "$file")
  file=$(basename "$file")
  limit=0
  while [ -L "$file" -a $limit -lt 1000 ]; do
    file=$(readlink "$file")
    cd $(dirname "$file")
    file=$(basename "$file")
    limit=$(($limit+1))
  done
  echo "$(pwd -P)/$file"
)

THIS=$(basename "$0")
THIS_DIR=$(dirname "$(canonical_path "$0")")

if [ -z "$1" ]; then
  echo "usage: ${THIS} PATH_TO_BINARYNINJA_API" 2>&1
  exit 1
fi

BINARYNINJA_API_SRC=$(canonical_path "$1")

cd "${THIS_DIR}"

(
  cat <<EOF
// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// This file is auto-generated. To regenerate, run the "${THIS}" script.

#include <cstdint>
#include <cstddef>

#define BINARYNINJACORE_LIBRARY
// clang-format off
#include "binaryninjaapi.h"  // NOLINT
// clang-format on

extern "C" {
EOF
  # The following extracts all C API functions from the Binary Ninja API
  # and creates empty stub functions for them. This is later compiled
  # into an API compatible library that is used to link against. This
  # avoids having to use install a copy of Binary Ninja on the machine
  # that builds BinExport.
  # Note that the script line below can be replaced with a small Clang
  # tool to make it more robust.
  cat "${BINARYNINJA_API_SRC}/binaryninjacore.h" | \
    sed '
      /^[[:space:]]*__attribute__/d;
      s,//.*$,,
    ' | \
    perl -pe 's/void\n/void/igs' | \
    clang-format --style='{BasedOnStyle: Google, Language: Cpp, ColumnLimit: 100000}' | \
    grep ^BINARYNINJACOREAPI | \
    sed '
      s,\(BINARYNINJACOREAPI void .*\);,\1 {},;
      s,\(BINARYNINJACOREAPI .*\);,\1 { return {}; },
    '
cat <<EOF
}  // extern "C"
EOF
) | \
  clang-format --style=Google > binaryninjacore.cc
