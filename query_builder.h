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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_QUERY_BUILDER_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_QUERY_BUILDER_H_

#include <sstream>
#include <string>
#include <vector>

#include "third_party/zynamics/binexport/types.h"

class Database;

struct Terminator {};

class QueryBuilder {
 public:
  // Does not take ownership.
  QueryBuilder(Database* database, const std::string& base_query,
               size_t query_size);
  void Execute();

 private:
  QueryBuilder(const QueryBuilder&) = delete;
  QueryBuilder& operator=(const QueryBuilder&) = delete;

  friend QueryBuilder& operator<<(QueryBuilder&, const std::string&);
  friend QueryBuilder& operator<<(QueryBuilder&, int64_t);
  friend QueryBuilder& operator<<(QueryBuilder&, const Terminator&);

  std::string base_query_;
  std::ostringstream current_query_;
  size_t query_size_;
  Database* database_;
  std::streamoff last_flush_position_;
};

extern const Terminator kFlushQuery;

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_QUERY_BUILDER_H_
