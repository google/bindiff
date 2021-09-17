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

// TODO(cblichmann): We may want to export directly to SQLite from BinExport in
//                   the future. In that case, move this file to BinExport.

#ifndef SQLITE_H_
#define SQLITE_H_

#include <cstdint>
#include <memory>

#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/util/types.h"

struct sqlite3;
struct sqlite3_stmt;

namespace security::bindiff {

class SqliteStatement;

class SqliteDatabase {
 public:
  SqliteDatabase(const SqliteDatabase&) = delete;
  SqliteDatabase& operator=(const SqliteDatabase&) = delete;

  SqliteDatabase(SqliteDatabase&& other);
  SqliteDatabase& operator=(SqliteDatabase&& other);

  ~SqliteDatabase();

  static absl::StatusOr<SqliteDatabase> Connect(absl::string_view filename);
  void Disconnect();

  void Begin();
  void Commit();
  void Rollback();

  std::unique_ptr<SqliteStatement> Statement(absl::string_view statement);

 private:
  friend class SqliteStatement;

  SqliteDatabase() = default;

  sqlite3* database_ = nullptr;
};

class SqliteStatement {
 public:
  explicit SqliteStatement(SqliteDatabase* database,
                           absl::string_view statement);
  ~SqliteStatement();

  SqliteStatement(const SqliteStatement&) = delete;
  SqliteStatement& operator=(const SqliteStatement&) = delete;

  SqliteStatement& BindInt(int value);
  SqliteStatement& BindInt64(int64_t value);
  SqliteStatement& BindDouble(double value);
  SqliteStatement& BindText(absl::string_view value);
  SqliteStatement& BindNull();

  SqliteStatement& Into(int* value, bool* is_null = nullptr);
  SqliteStatement& Into(int64_t* value, bool* is_null = nullptr);
  SqliteStatement& Into(Address* value, bool* is_null = nullptr);
  SqliteStatement& Into(double* value, bool* is_null = nullptr);
  SqliteStatement& Into(std::string* value, bool* is_null = nullptr);

  SqliteStatement& Execute();
  SqliteStatement& Reset();
  bool GotData() const;

 private:
  sqlite3* database_;
  sqlite3_stmt* statement_ = nullptr;
  int parameter_ = 0;
  int column_ = 0;
  bool got_data_ = false;
};

}  // namespace security::bindiff

#endif  // SQLITE_H_
