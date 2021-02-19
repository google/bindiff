// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
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
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/escaping.h"

namespace security::binexport {

absl::StatusOr<std::string> GetInputFileSha256() {
  std::string hash(32, '\0');
  if (!retrieve_input_file_sha256(reinterpret_cast<uchar*>(&hash[0]))) {
    return absl::InternalError("Failed to load SHA256 hash of input file");
  }
  return absl::AsciiStrToLower(absl::BytesToHexString(hash));
}

absl::StatusOr<std::string> GetInputFileMd5() {
  std::string hash(32, '\0');
  if (!retrieve_input_file_md5(reinterpret_cast<uchar*>(&hash[0]))) {
    return absl::InternalError("Failed to load MD5 hash of input file");
  }
  return absl::AsciiStrToLower(absl::BytesToHexString(hash));
}

}  // namespace security::binexport
