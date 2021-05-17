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

// Companion header to begin_idasdk.inc. In order to work, this must be
// included after including any IDA Pro headers.

#ifdef __clang__
#pragma clang diagnostic pop
#elif __GNUC__
#pragma GCC diagnostic pop
#elif _MSC_VER
#pragma warning(pop)
#endif

#undef uint128
#undef int128
#undef uint64
#undef int64
#undef uint32
#undef int32
#undef uint16
#undef int16
#undef uint8
#undef sint8
#undef int8

// Undefine the operand shortcuts Op1-Op8, as those pollute the global
// namespace.
#undef Op8
#undef Op7
#undef Op6
#undef Op5
#undef Op4
#undef Op3
#undef Op2
#undef Op1
