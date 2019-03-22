// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#include "third_party/zynamics/binexport/database.h"
#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/statusor.h"

using Blob = std::vector<uint8_t>;

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
>>>>>>> 49cbae584a2324f3c354b0f2d4b606ca59704160

struct pg_conn;
struct pg_result;

namespace security {
namespace binexport {

class PostgreSqlStatement;

class PostgreSqlDatabase : public Database {
 public:
  static not_absl::StatusOr<std::unique_ptr<PostgreSqlDatabase>> Connect(
      absl::string_view connection_string);

  ~PostgreSqlDatabase() override;

  std::unique_ptr<Statement> Prepare(absl::string_view sql) override;

  string EscapeLiteral(const string& text) const;
  string EscapeIdentifier(const string& text) const;
  //explicit PostgreSqlDatabase(const char* connection_string);
  //Database& Execute(const char* query,
  //                  const Parameters& parameters = Parameters{});
  //Database& Prepare(const char* query, const char* name = "");
  //Database& ExecutePrepared(const Parameters& parameters,
  //                          const char* name = "");
  //operator bool() const;
  //Database& operator>>(bool& value);    // NOLINT
  //Database& operator>>(int32& value);   // NOLINT
  //Database& operator>>(int64& value);   // NOLINT
  //Database& operator>>(double& value);  // NOLINT
  //Database& operator>>(string& value);  // NOLINT
  //Database& operator>>(Blob& value);    // NOLINT
 private:
  friend class PostgreSqlStatement;

  void Begin() override;
  void Commit() override;
  void Rollback() override;

  pg_conn* connection_;
  pg_result* result_;
};

}  // namespace binexport
}  // namespace security

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_POSTGRESQL_H_
