// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "third_party/zynamics/bindiff/sqlite.h"

#include <cstring>
#include <memory>
#include <stdexcept>

#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/sqlite/src/sqlite3.h"
#include "third_party/zynamics/binexport/util/status_macros.h"

namespace security::bindiff {
namespace {

absl::Status Sqlite3ErrToStatus(sqlite3* handle, absl::string_view message) {
  return absl::UnknownError(
      absl::StrCat(message, ": ", sqlite3_errmsg(handle)));
}

}  // namespace

SqliteDatabase::SqliteDatabase(SqliteDatabase&& other)
    : database_(other.database_) {
  other.database_ = nullptr;
}

SqliteDatabase& SqliteDatabase::operator=(SqliteDatabase&& other) {
  if (this != &other) {
    database_ = other.database_;
    other.database_ = nullptr;
  }
  return *this;
}

SqliteDatabase::~SqliteDatabase() { Disconnect(); }

absl::StatusOr<SqliteDatabase> SqliteDatabase::Connect(
    absl::string_view filename) {
  sqlite3* handle = nullptr;
  if (sqlite3_open(std::string(filename).c_str(), &handle) != SQLITE_OK) {
    absl::Status status = Sqlite3ErrToStatus(
        handle, absl::StrCat("open database '", filename, "'"));
    sqlite3_close(handle);
    throw std::runtime_error(std::string(status.message()));
  }

  if (!handle) {
    throw std::runtime_error("failed opening database");
  }

  SqliteDatabase database;
  database.database_ = handle;
  return database;
}

void SqliteDatabase::Disconnect() {
  if (!database_) {
    return;
  }

  sqlite3_close(database_);
  database_ = nullptr;
}

absl::StatusOr<SqliteStatement> SqliteDatabase::Statement(
    absl::string_view statement) {
  return SqliteStatement::Prepare(*this, statement);
}

SqliteStatement SqliteDatabase::StatementOrThrow(absl::string_view statement) {
  auto stmt = Statement(statement);
  if (!stmt.ok()) {
    throw std::runtime_error(std::string(stmt.status().message()));
  }
  return std::move(*stmt);
}

absl::Status SqliteDatabase::Execute(absl::string_view statement) {
  NA_ASSIGN_OR_RETURN(auto stmt, SqliteStatement::Prepare(*this, statement));
  return stmt.Execute();
}

absl::Status SqliteDatabase::Begin() { return Execute("BEGIN TRANSACTION"); }

absl::Status SqliteDatabase::Commit() { return Execute("COMMIT TRANSACTION"); }

absl::Status SqliteDatabase::Rollback() {
  return Execute("ROLLBACK TRANSACTION");
}

SqliteStatement::SqliteStatement(SqliteStatement&& other) {
  *this = std::move(other);
}

SqliteStatement& SqliteStatement::operator=(SqliteStatement&& other) {
  if (this != &other) {
    database_ = other.database_;
    other.database_ = nullptr;
    sqlite3_finalize(statement_);  // API is nullptr safe
    statement_ = other.statement_;
    other.statement_ = nullptr;
    column_ = other.column_;
    other.column_ = 0;
    parameter_ = other.parameter_;
    other.parameter_ = 0;
    got_data_ = other.got_data_;
    other.got_data_ = false;
  }
  return *this;
}

SqliteStatement::~SqliteStatement() {
  sqlite3_finalize(statement_);  // API is nullptr safe
}

absl::StatusOr<SqliteStatement> SqliteStatement::Prepare(
    SqliteDatabase& database, absl::string_view statement) {
  SqliteStatement sqlite_statement;
  sqlite_statement.database_ = database.database_;
  if (sqlite3_prepare_v2(sqlite_statement.database_, statement.data(),
                         statement.size(), &sqlite_statement.statement_,
                         nullptr) != SQLITE_OK) {
    return Sqlite3ErrToStatus(
        database.database_,
        absl::StrCat("preparing statement '", statement, "'"));
  }
  return sqlite_statement;
}

SqliteStatement& SqliteStatement::BindInt(int value) {
  sqlite3_bind_int(statement_, ++parameter_, value);
  return *this;
}

SqliteStatement& SqliteStatement::BindInt64(int64_t value) {
  sqlite3_bind_int64(statement_, ++parameter_, value);
  return *this;
}

SqliteStatement& SqliteStatement::BindDouble(double value) {
  sqlite3_bind_double(statement_, ++parameter_, value);
  return *this;
}

// This creates a copy of the string which may well be undesired/inefficient.
SqliteStatement& SqliteStatement::BindText(absl::string_view value) {
  sqlite3_bind_text(statement_, ++parameter_, value.data(), value.size(),
                    SQLITE_TRANSIENT);
  return *this;
}

SqliteStatement& SqliteStatement::BindNull() {
  sqlite3_bind_null(statement_, ++parameter_);
  return *this;
}

SqliteStatement& SqliteStatement::Into(int* value, bool* is_null) {
  if (is_null) {
    *is_null = sqlite3_column_type(statement_, column_) == SQLITE_NULL;
  }
  *value = sqlite3_column_int(statement_, column_);
  ++column_;
  return *this;
}

SqliteStatement& SqliteStatement::Into(int64_t* value, bool* is_null) {
  if (is_null) {
    *is_null = sqlite3_column_type(statement_, column_) == SQLITE_NULL;
  }
  *value = sqlite3_column_int64(statement_, column_);
  ++column_;
  return *this;
}

SqliteStatement& SqliteStatement::Into(Address* value, bool* is_null) {
  if (is_null) {
    *is_null = sqlite3_column_type(statement_, column_) == SQLITE_NULL;
  }
  *value = static_cast<Address>(sqlite3_column_int64(statement_, column_));
  ++column_;
  return *this;
}

SqliteStatement& SqliteStatement::Into(double* value, bool* is_null) {
  if (is_null) {
    *is_null = sqlite3_column_type(statement_, column_) == SQLITE_NULL;
  }
  *value = sqlite3_column_double(statement_, column_);
  ++column_;
  return *this;
}

SqliteStatement& SqliteStatement::Into(std::string* value, bool* is_null) {
  if (is_null) {
    *is_null = sqlite3_column_type(statement_, column_) == SQLITE_NULL;
  }
  if (auto* data = reinterpret_cast<const char*>(
          sqlite3_column_text(statement_, column_)))
    value->assign(data);
  ++column_;
  return *this;
}

absl::Status SqliteStatement::Execute() {
  const int return_code = sqlite3_step(statement_);
  if (return_code != SQLITE_ROW && return_code != SQLITE_DONE) {
    return Sqlite3ErrToStatus(
        database_,
        absl::StrCat("executing statement '", sqlite3_sql(statement_), "'"));
  }

  parameter_ = 0;
  column_ = 0;
  got_data_ = return_code == SQLITE_ROW;
  return absl::OkStatus();
}

SqliteStatement& SqliteStatement::ExecuteOrThrow() {
  absl::Status status = Execute();
  if (!status.ok()) {
    throw std::runtime_error(std::string(status.message()));
  }
  return *this;
}

SqliteStatement& SqliteStatement::Reset() {
  sqlite3_reset(statement_);
  got_data_ = false;
  return *this;
}

bool SqliteStatement::GotData() const { return got_data_; }

}  // namespace security::bindiff
