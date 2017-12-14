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

// Allow to safely include IDA's pro.h. In order to work, any IDA Pro header
// must be included between includes of begin_idasdk.h (this file) and
// end_idasdk.h (which undoes the preprocessor changes from this file).

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_IDASDK_BEGIN_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_IDASDK_BEGIN_H_

// Alias IDA-specific integer types which conflict with the ones defined by
// Protocol Buffers/Abseil.
#define int8 ida_int8
#define sint8 ida_sint8
#define uint8 ida_uint8
#define int16 ida_int16
#define uint16 ida_uint16
#define int32 ida_int32
#define uint32 ida_uint32
#define int64 ida_int64
#define uint64 ida_uint64
#define int128 ida_int128
#define uint128 ida_uint128

#include <pro.h>

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_IDASDK_BEGIN_H_
