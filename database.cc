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

#include "third_party/zynamics/binexport/database.h"

#include "third_party/absl/memory/memory.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/sqlite/src/sqlite3.h"
#include "third_party/zynamics/binexport/util/status.h"
#include "third_party/zynamics/binexport/util/statusor.h"

namespace security {
namespace binexport {

class SqliteStatement;

class SqliteDatabase : public Database {
 public:
  static not_absl::StatusOr<std::unique_ptr<SqliteDatabase>> Open(
      absl::string_view filename);

  ~SqliteDatabase() override;

  std::unique_ptr<Statement> Prepare(absl::string_view sql) override;

 private:
  friend class SqliteStatement;

  explicit SqliteDatabase(sqlite3* handle) : handle_{handle} {}

  bool CheckResultAndSetStatus(int result);

  void Begin() override;
  void Commit() override;
  void Rollback() override;

  sqlite3* handle_;
};

class SqliteStatement : public Statement {
 public:
  SqliteStatement(SqliteDatabase* db, sqlite3_stmt* handle)
      : db_{db}, handle_{handle} {}
  ~SqliteStatement();

 private:
  Statement* BindInt(int value) override;
  Statement* BindInt64(int64 value) override;
  Statement* BindDouble(double value) override;
  Statement* BindText(absl::string_view value) override;
  Statement* BindNull() override;

  Statement* Into(int* value, bool* is_null) override;
  Statement* Into(int64* value, bool* is_null) override;
  Statement* Into(double* value, bool* is_null) override;
  Statement* Into(std::string* value, bool* is_null) override;

  Statement* Execute() override;
  void Reset() override;

  SqliteDatabase* db_;
  sqlite3_stmt* handle_;
  int bound_ = 0;   // Current input argument to be bound
  int column_ = 0;  // Current column result to fetch next
};

not_absl::Status FromSqlite3Error(int result) {
  // TODO(cblichmann): Map SQLite status codes to canonical status codes.
  if (result == SQLITE_OK) {
    return not_absl::OkStatus();
  }
  return not_absl::Status{not_absl::StatusCode::kUnknown,
                          absl::StrCat("SQLite: ", sqlite3_errstr(result))};
}

bool SqliteDatabase::CheckResultAndSetStatus(int result) {
  if (result == SQLITE_OK) {
    return true;
  }
  set_status(FromSqlite3Error(result));
  return false;
}

void SqliteDatabase::Begin() {
  ExecuteNoResult("BEGIN");
  CheckResultAndSetStatus(sqlite3_errcode(handle_));
}

void SqliteDatabase::Commit() {
  ExecuteNoResult("COMMIT");
  CheckResultAndSetStatus(sqlite3_errcode(handle_));
}

void SqliteDatabase::Rollback() {
  ExecuteNoResult("ROLLBACK");
  CheckResultAndSetStatus(sqlite3_errcode(handle_));
}

not_absl::StatusOr<std::unique_ptr<SqliteDatabase>> SqliteDatabase::Open(
    absl::string_view filename) {
  sqlite3* handle{};
  auto result = sqlite3_open_v2(std::string(filename).c_str(), &handle,
                                SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE,
                                /*zVfs=*/nullptr);
  if (result != SQLITE_OK) {
    return FromSqlite3Error(result);
  }
  if (!handle) {
    return not_absl::Status{not_absl::StatusCode::kInternal,
                            "unexpected database handle"};
  }
  return absl::WrapUnique(new SqliteDatabase{handle});
}

std::unique_ptr<Statement> SqliteDatabase::Prepare(absl::string_view sql) {
  sqlite3_stmt* statement_handle{};
  CheckResultAndSetStatus(sqlite3_prepare_v2(handle_, sql.data(), sql.size(),
                                             &statement_handle,
                                             /*pzTail=*/nullptr));
  return absl::make_unique<SqliteStatement>(this, statement_handle);
}

SqliteDatabase::~SqliteDatabase() {
  sqlite3_close(handle_);  // Null-safe
}

SqliteStatement::~SqliteStatement() {
  db_->CheckResultAndSetStatus(sqlite3_finalize(handle_));
  handle_ = nullptr;
}

Statement* SqliteStatement::BindInt(int value) {
  db_->CheckResultAndSetStatus(sqlite3_bind_int(handle_, bound_++, value));
  return this;
}

Statement* SqliteStatement::BindInt64(int64 value) {
  db_->CheckResultAndSetStatus(sqlite3_bind_int64(handle_, bound_++, value));
  return this;
}

Statement* SqliteStatement::BindDouble(double value) {
  db_->CheckResultAndSetStatus(sqlite3_bind_double(handle_, bound_++, value));
  return this;
}

Statement* SqliteStatement::BindText(absl::string_view value) {
//  db_->CheckResultAndSetStatus(sqlite3_bind_text64(handle_, bound_++, value));
  return this;
}

Statement* SqliteStatement::BindNull() {
  db_->CheckResultAndSetStatus(sqlite3_bind_null(handle_, bound_++));
  return this;
}

Statement* SqliteStatement::Into(int* value, bool* is_null) {
  const auto column = column_++;
  if (is_null) {
    auto type = sqlite3_column_type(handle_, column);
    *is_null = type == SQLITE_NULL;
  }
  db_->CheckResultAndSetStatus(sqlite3_column_int(handle_, column));
  return this;
}

Statement* SqliteStatement::Execute() {
  db_->CheckResultAndSetStatus(sqlite3_step(handle_));
  return this;
}

void SqliteStatement::Reset() {
  bound_ = 0;
  column_ = 0;
}

static bool dummy_unused = []() {
  auto db = std::move(SqliteDatabase::Open("my_file.db")).ValueOrDie();
  int count;
  db->Prepare("SELECT * FROM file WHERE :1 = 1")
    ->BindInt(5)
    ->Into(&count)
    ->Execute();
  db->Transaction([](Database* db) {
    db->Prepare("INSERT INTO file VALUES(1)")
      ->Execute();
    return true;
  });

  return true;
}();

}  // namespace binexport
}  // namespace security
