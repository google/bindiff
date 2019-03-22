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

#include "third_party/zynamics/binexport/postgresql.h"

#include <cassert>
#include <memory>
#include <stdexcept>

#include "base/logging.h"
#include "third_party/absl/strings/escaping.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_replace.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/strings/strip.h"
#include <libpq-fe.h>  // NOLINT

namespace security {
namespace binexport {

class PostgreSqlStatement {
  PostgreSqlStatement(PostgreSqlDatabase* db, void* handle)
      : db_{db}, handle_{handle} {}
  ~PostgreSqlStatement();

 private:
  Statement* BindInt(int value) override;
  Statement* BindInt64(int64 value) override;
  Statement* BindDouble(double value) override;
  Statement* BindText(absl::string_view value) override;
  Statement* BindNull() override;

  Statement* Into(int* value, bool* is_null) override;
  Statement* Into(int64* value, bool* is_null) override;
  Statement* Into(double* value, bool* is_null) override;
  Statement* Into(string* value, bool* is_null) override;

  Statement* Execute() override;
  void Reset() override;
};

}  // namespace
}  // namespace

namespace {

// Callback for outputting PostgreSQL diagnostic messages sent from the server.
// By default these go directly to stderr which interferes with our own logging
// thread. Using this callback we divert output to the same logging sink.
void PostgresNoticeProcessor(void* /*arg*/, const char* raw_message) {
  // Postgres sends line breaks which our logging adds automatically, thus we
  // remove it here.
  LOG(INFO) << absl::StrCat("PostgreSQL: ",
                            absl::StripAsciiWhitespace(raw_message));
}

}  // namespace

Database::Database(const char* connection_string)
    : connection_(PQconnectdb(connection_string)),
      result_(nullptr),
      result_index_(0) {
  if (PQstatus(connection_) != CONNECTION_OK) {
    const std::string error = std::string(PQerrorMessage(connection_));
    PQfinish(connection_);
    throw std::runtime_error(("Failed connecting to database: '" + error +
                              "', connection std::string used: '" +
                              connection_string + "'").c_str());
  }

  // We are not interested in NOTICE spam (examples of which are implicit
  // sequence creation for serial columns, non existing tables in delete table
  // and similar stuff).
  Execute("SET client_min_messages TO WARNING;");

  // Set a custom handler for log messages coming from the server.
  PQsetNoticeProcessor(connection_, &PostgresNoticeProcessor,
                       NULL /*user data*/);
}

Database::~Database() {
  PQclear(result_);
  result_ = nullptr;
  PQfinish(connection_);
  connection_ = nullptr;
}

Database& Database::Prepare(const char* query, const char* name) {
  PQclear(result_);
  // Parameter types and count will be passed in when executing the statement.
  result_ = PQprepare(connection_, name, query, 0, 0);
  if (PQresultStatus(result_) != PGRES_COMMAND_OK) {
    const char* error = PQerrorMessage(connection_);
    throw std::runtime_error(
        ("Preparing query failed: " + std::string(error)).c_str());
  }
  return *this;
}

Database& Database::ExecutePrepared(const Parameters& parameters,
                                    const char* name) {
  const bool has_parameters = !parameters.data_.empty();
  PQclear(result_);
  result_ = PQexecPrepared(
      connection_, name /* Prepared statement name */, parameters.Size(),
      has_parameters ? &parameters.GetParameters().front() : 0,
      has_parameters ? &parameters.sizes_.front() : 0,
      // Get result in text (0) or binary (1) format.
      has_parameters ? &parameters.types_.front() : 0, 1);
  result_index_ = 0;

  switch (PQresultStatus(result_)) {
    case PGRES_EMPTY_QUERY:
    case PGRES_COMMAND_OK:
    case PGRES_TUPLES_OK:
      break;
    case PGRES_COPY_OUT:
    case PGRES_COPY_IN:
    case PGRES_BAD_RESPONSE:
    case PGRES_NONFATAL_ERROR:
    case PGRES_FATAL_ERROR:
    default: {
      const char* error = PQerrorMessage(connection_);
      throw std::runtime_error(
          ("Executing prepared statement failed: " + std::string(error))
              .c_str());
    }
  }
  return *this;
}

Database& Database::Execute(const char* query, const Parameters& parameters) {
  const bool has_parameters = !parameters.data_.empty();
  PQclear(result_);
  result_ =
      PQexecParams(connection_, query, parameters.Size(),
                   0 /* Deduce type information automatically */,
                   has_parameters ? &*parameters.GetParameters().begin() : 0,
                   has_parameters ? &*parameters.sizes_.begin() : 0,
                   // Get result in text (0) or binary (1) format.
                   has_parameters ? &*parameters.types_.begin() : 0, 1);
  result_index_ = 0;

  switch (PQresultStatus(result_)) {
    case PGRES_EMPTY_QUERY:
    case PGRES_COMMAND_OK:
    case PGRES_TUPLES_OK:
      break;
    case PGRES_COPY_OUT:
    case PGRES_COPY_IN:
    case PGRES_BAD_RESPONSE:
    case PGRES_NONFATAL_ERROR:
    case PGRES_FATAL_ERROR:
    default: {
      const char* error = PQerrorMessage(connection_);
      throw std::runtime_error(
          ("Executing query failed: " + std::string(error)).c_str());
    }
  }
  return *this;
}

Database::operator bool() const {
  return result_ && result_index_ < PQnfields(result_) * PQntuples(result_);
}

Database& Database::operator>>(bool& value) {
  const int num_columns = PQnfields(result_);
  assert(!PQgetisnull(result_, result_index_ / num_columns,
                      result_index_ % num_columns) &&
         "null values not supported");
  const uint8_t* source = reinterpret_cast<const uint8_t*>(PQgetvalue(
      result_, result_index_ / num_columns, result_index_ % num_columns));
  value = *source != 0;
  ++result_index_;
  return *this;
}

Database& Database::operator>>(int32_t& value) {
  const int num_columns = PQnfields(result_);
  assert(!PQgetisnull(result_, result_index_ / num_columns,
                      result_index_ % num_columns) &&
         "null values not supported");
  uint8_t* dest = reinterpret_cast<uint8_t*>(&value);
  const uint8_t* source =
      reinterpret_cast<const uint8_t*>(PQgetvalue(
          result_, result_index_ / num_columns, result_index_ % num_columns)) +
      3;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  ++result_index_;
  return *this;
}

Database& Database::operator>>(int64_t& value) {
  const int num_columns = PQnfields(result_);
  assert(!PQgetisnull(result_, result_index_ / num_columns,
                      result_index_ % num_columns) &&
         "null values not supported");
  uint8_t* dest = reinterpret_cast<uint8_t*>(&value);
  const uint8_t* source =
      reinterpret_cast<const uint8_t*>(PQgetvalue(
          result_, result_index_ / num_columns, result_index_ % num_columns)) +
      7;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  ++result_index_;
  return *this;
}

Database& Database::operator>>(double& value) {
  const int num_columns = PQnfields(result_);
  assert(!PQgetisnull(result_, result_index_ / num_columns,
                      result_index_ % num_columns) &&
         "null values not supported");
  uint8_t* dest = reinterpret_cast<uint8_t*>(&value);
  const uint8_t* source =
      reinterpret_cast<const uint8_t*>(PQgetvalue(
          result_, result_index_ / num_columns, result_index_ % num_columns)) +
      7;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  *dest++ = *source--;
  ++result_index_;
  return *this;
}

Database& Database::operator>>(std::string& value) {
  const int num_columns = PQnfields(result_);
  assert(!PQgetisnull(result_, result_index_ / num_columns,
                      result_index_ % num_columns) &&
         "null values not supported");
  const int size = PQgetlength(result_, result_index_ / num_columns,
                               result_index_ % num_columns);
  const std::string::value_type* source =
      reinterpret_cast<const std::string::value_type*>(PQgetvalue(
          result_, result_index_ / num_columns, result_index_ % num_columns));
  value.assign(source, source + size);
  ++result_index_;
  return *this;
}

Database& Database::operator>>(Blob& value) {
  const int num_columns = PQnfields(result_);
  assert(!PQgetisnull(result_, result_index_ / num_columns,
                      result_index_ % num_columns) &&
         "null values not supported");
  const int size = PQgetlength(result_, result_index_ / num_columns,
                               result_index_ % num_columns);
  const Blob::value_type* source =
      reinterpret_cast<const Blob::value_type*>(PQgetvalue(
          result_, result_index_ / num_columns, result_index_ % num_columns));
  value.assign(source, source + size);
  ++result_index_;
  return *this;
}

std::string Database::EscapeLiteral(const std::string& text) const {
  return absl::StrCat("E'", absl::CEscape(text), "'");
}

std::string Database::EscapeIdentifier(const std::string& text) const {
  // NOTE(jduart): The only case where the escape performed by CEscape doesn't
  // work for literals is  when we have a double-quote. Options are to change
  // double-quotes by single-quotes or use a double-" which seems to be what
  // PostgreSQL does after v9.0. Note that this removes double-quotes, as
  // "escaped""word" == "escapedword".
  return absl::StrCat(
      "\"",
      absl::StrReplaceAll(absl::CEscape(text),
                          {{"\\\"" /* replace escaped double-quote \" */,
                            "\"\"" /* for double double-quote " */}}),
      "\"");
}
