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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_BASE_LOGGING_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_BASE_LOGGING_H_

#include <google/protobuf/stubs/logging.h>

// Map names from the Protocol Buffers stubs into the global namespace.
using ::google::protobuf::LogLevel;
using ::google::protobuf::LogHandler;
using ::google::protobuf::SetLogHandler;

#define LOG GOOGLE_LOG
#define LOG_IF GOOGLE_LOG_IF
#define CHECK GOOGLE_CHECK
#define CHECK_OK GOOGLE_CHECK_OK
#define CHECK_EQ GOOGLE_CHECK_EQ
#define CHECK_NE GOOGLE_CHECK_NE
#define CHECK_LT GOOGLE_CHECK_LT
#define CHECK_LE GOOGLE_CHECK_LE
#define CHECK_GT GOOGLE_CHECK_GT
#define CHECK_GE GOOGLE_CHECK_GE
#define CHECK_NOTNULL GOOGLE_CHECK_NOTNULL

#define DLOG GOOGLE_DLOG
#define DCHECK GOOGLE_DCHECK
#define DCHECK_OK GOOGLE_DCHECK_OK
#define DCHECK_EQ GOOGLE_DCHECK_EQ
#define DCHECK_NE GOOGLE_DCHECK_NE
#define DCHECK_LT GOOGLE_DCHECK_LT
#define DCHECK_LE GOOGLE_DCHECK_LE
#define DCHECK_GT GOOGLE_DCHECK_GT
#define DCHECK_GE GOOGLE_DCHECK_GE

#define LOGLEVEL_QFATAL LOGLEVEL_FATAL
#define QCHECK GOOGLE_CHECK
#define QCHECK_OK GOOGLE_CHECK_OK
#define QCHECK_EQ GOOGLE_CHECK_EQ
#define QCHECK_NE GOOGLE_CHECK_NE
#define QCHECK_LT GOOGLE_CHECK_LT
#define QCHECK_LE GOOGLE_CHECK_LE
#define QCHECK_GT GOOGLE_CHECK_GT
#define QCHECK_GE GOOGLE_CHECK_GE

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_BASE_LOGGING_H_
