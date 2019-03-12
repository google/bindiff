// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <nalt.hpp>                                             // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "base/integral_types.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/escaping.h"
#include "third_party/zynamics/binexport/util/status.h"

namespace security {
namespace binexport {

not_absl::StatusOr<std::string> GetInputFileSha256() {
  constexpr int kBinarySha256Length = 32;
  unsigned char hash[kBinarySha256Length];
  if (!retrieve_input_file_sha256(hash)) {
    return not_absl::Status{not_absl::StatusCode::kInternal,
                            "Failed to load SHA256 hash of input file"};
  }
  return absl::AsciiStrToLower(absl::BytesToHexString(absl::string_view(
      reinterpret_cast<const char*>(hash), kBinarySha256Length)));
}

not_absl::StatusOr<std::string> GetInputFileMd5() {
  constexpr int kBinaryMd5Length = 16;
  unsigned char hash[kBinaryMd5Length];
  if (!retrieve_input_file_md5(hash)) {
    return not_absl::Status{not_absl::StatusCode::kInternal,
                            "Failed to load MD5 hash of input file"};
  }
  return absl::AsciiStrToLower(absl::BytesToHexString(absl::string_view(
      reinterpret_cast<const char*>(hash), kBinaryMd5Length)));
}

}  // namespace binexport
}  // namespace security
