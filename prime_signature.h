// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

// TODO(cblichmann): Remove this file once we remove support for the legacy
//                   BinExport format.

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_PRIME_SIGNATURE_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_PRIME_SIGNATURE_H_

#include <cstdint>
#include <string>

namespace bindetego {

uint32_t GetPrime(const std::string& mnemonic);

}  // namespace bindetego

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_PRIME_SIGNATURE_H_
