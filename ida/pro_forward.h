// Copyright 2011-2017 Google Inc. All Rights Reserved.
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

// Forward header to safely include IDA's pro.h. In order to work, this must be
// included before including any IDA Pro headers.

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_PRO_FORWARD_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_PRO_FORWARD_H_

// Alias IDA-specific integer type which conflicts with the one defined by
// Protocol Buffers.
#define uint128 ida_uint128

#include <pro.h>

// Undo our preprocessor changes.
#undef ida_uint128

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_PRO_FORWARD_H_
