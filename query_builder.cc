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

#include "third_party/zynamics/binexport/query_builder.h"

#include <algorithm>  // std::min

#include "base/logging.h"
#include "third_party/zynamics/binexport/postgresql.h"

const Terminator kFlushQuery = {};

QueryBuilder::QueryBuilder(Database* database, const std::string& base_query,
                           size_t query_size)
    : base_query_(base_query),
      query_size_(query_size),
      database_(database),
      last_flush_position_(0) {
  current_query_ << base_query_;
}

void QueryBuilder::Execute() {
  const std::string& query = current_query_.str();
  if (query != base_query_) {
    database_->Execute(query.substr(0, query.size() - 1).c_str());
  }
}

QueryBuilder& operator<<(QueryBuilder& builder, const Terminator&) {
  if (static_cast<size_t>(builder.current_query_.tellp()) >=
      builder.query_size_) {
    // Buffer overrun, create new query.
    builder.database_->Execute(
        builder.current_query_.str()
            .substr(0,
                    static_cast<unsigned int>(builder.last_flush_position_) - 1)
            .c_str());
    std::string query = builder.current_query_.str().substr(
        static_cast<unsigned int>(builder.last_flush_position_));
    builder.current_query_.str("");
    builder.current_query_ << builder.base_query_ << query;
    builder.last_flush_position_ = builder.current_query_.tellp();
  } else {
    builder.last_flush_position_ = builder.current_query_.tellp();
  }
  return builder;
}

QueryBuilder& operator<<(QueryBuilder& builder, const std::string& query) {
  builder.current_query_ << query;
  return builder;
}

QueryBuilder& operator<<(QueryBuilder& builder, int64_t value) {
  builder.current_query_ << value;
  return builder;
}

