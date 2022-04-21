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

#include "third_party/zynamics/binexport/ida/digest.h"

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <nalt.hpp>                                             // NOLINT
#include <netnode.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/escaping.h"

namespace security::binexport {

absl::StatusOr<std::string> GetInputFileSha256() {
  constexpr int kNumSha256Bytes = 32;
  std::string hash(kNumSha256Bytes, '\0');
  auto* hash_uchar = reinterpret_cast<uchar*>(&hash[0]);
  if (!retrieve_input_file_sha256(hash_uchar) &&
      // b/186782665: IDA 7.5 and lower use the root_node instead.
      (root_node.supval(RIDX_SHA256, hash_uchar, kNumSha256Bytes) !=
       kNumSha256Bytes)) {
    return absl::InternalError("Failed to load SHA256 hash of input file");
  }
  return absl::AsciiStrToLower(absl::BytesToHexString(hash));
}

absl::StatusOr<std::string> GetInputFileMd5() {
  constexpr int kNumMd5Bytes = 16;
  std::string hash(kNumMd5Bytes, '\0');
  auto* hash_uchar = reinterpret_cast<uchar*>(&hash[0]);
  if (!retrieve_input_file_md5(hash_uchar) &&
      // b/186782665: IDA 7.5 and lower use the root_node instead.
      (root_node.supval(RIDX_MD5, hash_uchar, kNumMd5Bytes) != kNumMd5Bytes)) {
    return absl::InternalError("Failed to load MD5 hash of input file");
  }
  return absl::AsciiStrToLower(absl::BytesToHexString(hash));
}

}  // namespace security::binexport
