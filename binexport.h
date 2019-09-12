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

// This file provide a set of commonly used utility functions to work with
// BinExport2 protos.

#ifndef BINEXPORT_H_
#define BINEXPORT_H_

#include <vector>

#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/types.h"

namespace security {

// This namespace collects functions that work directly with BinExport2 protocol
// buffers, similar to a class with just static methods.
namespace binexport {

// Returns the address for an instruction. Takes care of instructions without an
// address (that are part of a continuous run of instructions, for example).
// Aborts the process with a fatal error, if no address can be found for the
// instruction. Note that this should note happen with well-formed BinExport2
// protos. If the specified index is out of bounds, the behavior is undefined.
Address GetInstructionAddress(const BinExport2& proto, int index);

// Like above, but returns the addresses for all instructions in a BinExport2
// proto. This function is more efficient than calling GetInstructionAddress()
// repeatedly in a loop. The trade-off is that the returned vector will store
// all addresses, unlike the BinExport2 proto itself which only stores the
// beginning of continuous instruction runs.
std::vector<Address> GetAllInstructionAddresses(const BinExport2& proto);

}  // namespace binexport
}  // namespace security

#endif  // BINEXPORT_H_
