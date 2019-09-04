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

#ifndef IDA_DIGEST_H_
#define IDA_DIGEST_H_

#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/statusor.h"

namespace security {
namespace binexport {
//
// Returns the lowercase hex string of the SHA256 hash of the original input
// file for the current IDB.
not_absl::StatusOr<std::string> GetInputFileSha256();

// Returns the MD5 hash of the original input file for the current IDB. Like
// GetInputFileSha256(), the result is a lowercase hex string. This function
// exists to support the legacy MD5 field in the BinExport database schema.
not_absl::StatusOr<std::string> GetInputFileMd5();

}  // namespace binexport
}  // namespace security

#endif  // IDA_DIGEST_H_
