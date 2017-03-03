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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_BASE_STRINGPRINTF_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_BASE_STRINGPRINTF_H_

#include <google/protobuf/stubs/stringprintf.h>

// Map names from the Protocol Buffers stubs into the global namespace.
using ::google::protobuf::StringPrintf;
using ::google::protobuf::SStringPrintf;
using ::google::protobuf::StringAppendF;
using ::google::protobuf::StringAppendV;
using ::google::protobuf::kStringPrintfVectorMaxArgs;
using ::google::protobuf::StringPrintfVector;

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_BASE_STRINGPRINTF_H_
