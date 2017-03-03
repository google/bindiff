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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_POSTGRESQL_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_POSTGRESQL_H_

#include <string>
#include <vector>

#include "third_party/zynamics/binexport/types.h"

typedef std::vector<uint8_t> Blob;

// Intentionally empty, used to indicate dbnull parameter values.
struct Null {};

// Parameters used in a query. Reference in a statement with explicit type casts
// like so $1::varchar, $2::varchar, $3::bytea.
// Assumes we are on a little endian machine (intel).
class Parameters {
 public:
  int Size() const;
  void Clear();
  Parameters& operator<<(const bool value);
  Parameters& operator<<(const int32_t value);
  Parameters& operator<<(const int64_t value);
  Parameters& operator<<(double value);
  Parameters& operator<<(const std::string& value);
  Parameters& operator<<(const Blob& value);
  Parameters& operator<<(const Null& value);

 private:
  friend class Database;

  Blob data_;
  std::vector<int> parameters_;
  std::vector<int> sizes_;
  std::vector<int> types_;

  std::vector<const char*> GetParameters() const;
};

struct pg_conn;
struct pg_result;

class Database {
 public:
  explicit Database(const char* connection_string);
  ~Database();

  Database& Execute(const char* query,
                    const Parameters& parameters = Parameters());
  Database& Prepare(const char* query, const char* name = "");
  Database& ExecutePrepared(const Parameters& parameters,
                            const char* name = "");

  std::string EscapeLiteral(const std::string& text) const;
  std::string EscapeIdentifier(const std::string& text) const;

  operator bool() const;
  Database& operator>>(bool& value);  // NOLINT
  Database& operator>>(int32_t& value);  // NOLINT
  Database& operator>>(int64_t& value);  // NOLINT
  Database& operator>>(double& value);  // NOLINT
  Database& operator>>(std::string& value);  // NOLINT
  Database& operator>>(Blob& value);  // NOLINT

 private:
  Database(const Database&) = delete;
  Database& operator=(const Database&) = delete;

  pg_conn* connection_;
  pg_result* result_;
  int result_index_;
};

class Transaction {
 public:
  // Does not take ownership
  explicit Transaction(Database* database);
  ~Transaction();

 private:
  Transaction(const Transaction&) = delete;
  Transaction& operator=(const Transaction&) = delete;

  Database* database_;
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_POSTGRESQL_H_
