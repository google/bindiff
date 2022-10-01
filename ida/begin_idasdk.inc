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

// Allow to safely include IDA's pro.h. In order to work, any IDA Pro header
// must be included between includes of begin_idasdk.inc (this file) and
// end_idasdk.inc (which undoes the preprocessor changes from this file).
//
// Note: There is no header guard in this file, we rely on the one in IDA's
//       pro.h to provide the basic types IDA plugins need. Other than that,
//       we do want to support the case where IDA plugin code includes other
//       headers that need to include IDA headers. Using include guards here
//       would prevent that.

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

// Silence some compiler warnings in IDA SDK headers
#ifdef __clang__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wlogical-op-parentheses"
#pragma clang diagnostic ignored "-Wnon-virtual-dtor"
#pragma clang diagnostic ignored "-Wnullability-completeness"
#pragma clang diagnostic ignored "-Wvarargs"
#elif __GNUC__
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wlogical-not-parentheses"
#pragma GCC diagnostic ignored "-Wnon-virtual-dtor"
#pragma GCC diagnostic ignored "-Wnonnull"
#pragma GCC diagnostic ignored "-Wvarargs"
#elif _MSC_VER
#pragma warning(push)
#pragma warning(disable : 4005)
#pragma warning(disable : 4244)
#pragma warning(disable : 4267)
#endif

// Now include the problematic header, end_idasdk.inc will clean up again.
#include <pro.h>
