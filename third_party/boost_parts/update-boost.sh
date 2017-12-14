#!/bin/bash
# Copyright 2011-2017 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Updates the partial copy of Boost in third_party/boost to the latest Boost
# version.
# See https://github.com/Valloric/ycmd/blob/master/update_boost.sh for the
# script that this one is based on.

# Exit on error
set -e

if [ -z "$1" ]; then
  echo "usage: $0 PATH_TO_BOOST" 2>&1
  exit 1
fi

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
BOOST_SRC="$1"

# Change to root of source tree.
cd "$THIS_DIR/../.."

# Build bcp utility if necessary
if [ ! -x "$BOOST_SRC/dist/bin/bcp" ]; then
  cd "$BOOST_SRC"
  "$BOOST_SRC/bootstrap.sh"
  "$BOOST_SRC/b2" --built-type=minimal -j$(nproc) tools/bcp
fi

BOOST_OUT=$(mktemp -d)
trap "rm -rf ${BOOST_OUT}" EXIT

# Find and copy the parts of Boost that we need
"$BOOST_SRC/dist/bin/bcp" --boost="$BOOST_SRC" \
  --scan *.cc *.h ida/*.cc ida/*.h "$BOOST_OUT"
find "$BOOST_OUT/libs" -not \( -name "*.?pp" -o -name "*.inl" \) \
  -type f -delete

rm -rf "$THIS_DIR/boost"
# Note: We're not moving the libs/ because we're only using header-only
#       libraries.
mv "$BOOST_OUT/boost" "$THIS_DIR"
