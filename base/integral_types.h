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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_BASE_INTEGRAL_TYPES_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_BASE_INTEGRAL_TYPES_H_

// Only include if not include any IDA headers, which also define most of the
// symbols below.
#ifndef IDA_SDK_VERSION
#include <google/protobuf/stubs/port.h>

// Map names from the Protocol Buffers stubs into the global namespace.
using ::google::protobuf::int16;
using ::google::protobuf::int32;
using ::google::protobuf::int64;
using ::google::protobuf::int8;
using ::google::protobuf::kint32max;
using ::google::protobuf::kint32min;
using ::google::protobuf::kint64max;
using ::google::protobuf::kint64min;
using ::google::protobuf::kuint32max;
using ::google::protobuf::kuint64max;
using ::google::protobuf::uint16;
using ::google::protobuf::uint32;
using ::google::protobuf::uint64;
using ::google::protobuf::uint8;
#endif
#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_BASE_INTEGRAL_TYPES_H_
