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

#include "third_party/zynamics/binexport/postgresql.h"

#include <cassert>
#include <memory>
#include <stdexcept>

#include "base/logging.h"

#include <libpq-fe.h>
#include "strings/stringpiece.h"
#include "strings/strutil.h"

namespace {

// Callback for outputting PostgreSQL diagnostic messages sent from the server.
// By default these go directly to stderr which interferes with our own logging
// thread. Using this callback we divert output to the same logging sink.
void PostgresNoticeProcessor(void* /*arg*/, const char* raw_message) {
  // Postgres sends line breaks which our logging adds automatically, thus we
  // remove it here.
  StringPiece message(raw_message);
  auto trimmed(message.ToString());
  StripWhitespace(&trimmed);
  LOG(INFO) << "PostgreSQL: " << trimmed;
}

}  // namespace

Transaction::Transaction(Database* database) : database_(database) {
  database_->Execute("BEGIN");
}

Transaction::~Transaction() { database_->Execute("COMMIT"); }

int Parameters::Size() const { return static_cast<int>(sizes_.size()); }

void Parameters::Clear() {
  data_.clear();
  parameters_.clear();
  sizes_.clear();
  types_.clear();
}

std::vector<const char*> Parameters::GetParameters() const {
  std::vector<const char*> parameters;
  parameters.reserve(parameters_.size());
  for (const auto index : parameters_) {
    if (index != -1) {
      parameters.push_back(
          reinterpret_cast<const char*>(&*(data_.begin() + index)));
    } else {
      parameters.push_back(0);
    }
  }
  return parameters;
}

Parameters& Parameters::operator<<(const bool value) {
  parameters_.push_back(static_cast<int>(data_.size()));
  data_.push_back(uint8_t(value ? 1 : 0));
  sizes_.push_back(1);
  types_.push_back(1);
  return *this;
}

Parameters& Parameters::operator<<(const int32_t value) {
  parameters_.push_back(static_cast<int>(data_.size()));
  data_.push_back(static_cast<uint8_t>((value >> 24) & 0xFF));
  data_.push_back(static_cast<uint8_t>((value >> 16) & 0xFF));
  data_.push_back(static_cast<uint8_t>((value >> 8) & 0xFF));
  data_.push_back(static_cast<uint8_t>((value)&0xFF));
  sizes_.push_back(4);
  types_.push_back(1);
  return *this;
}

Parameters& Parameters::operator<<(const int64_t value) {
  parameters_.push_back(static_cast<int>(data_.size()));
  data_.push_back(static_cast<uint8_t>((value >> 56) & 0xFF));
  data_.push_back(static_cast<uint8_t>((value >> 48) & 0xFF));
  data_.push_back(static_cast<uint8_t>((value >> 40) & 0xFF));
  data_.push_back(static_cast<uint8_t>((value >> 32) & 0xFF));
  data_.push_back(static_cast<uint8_t>((value >> 24) & 0xFF));
  data_.push_back(static_cast<uint8_t>((value >> 16) & 0xFF));
  data_.push_back(static_cast<uint8_t>((value >> 8) & 0xFF));
  data_.push_back(static_cast<uint8_t>((value)&0xFF));
  sizes_.push_back(8);
  types_.push_back(1);
  return *this;
}

Parameters& Parameters::operator<<(double value) {
  parameters_.push_back(static_cast<int>(data_.size()));
  const uint8_t* p = (reinterpret_cast<const uint8_t*>(&value) + 7);
  for (int i = 0; i < 8; ++i) {
    data_.push_back(*(p - i));
  }
  sizes_.push_back(8);
  types_.push_back(1);
  return *this;
}

Parameters& Parameters::operator<<(const std::string& value) {
  parameters_.push_back(static_cast<int>(data_.size()));
  data_.insert(data_.end(), value.begin(), value.end());
  sizes_.push_back(static_cast<int>(value.size()));
  types_.push_back(1);
  return *this;
}

Parameters& Parameters::operator<<(const Blob& value) {
  parameters_.push_back(static_cast<int>(data_.size()));
  data_.insert(data_.end(), value.begin(), value.end());
  sizes_.push_back(static_cast<int>(value.size()));
  types_.push_back(1);
  return *this;
}

Parameters& Parameters::operator<<(const Null&) {
  parameters_.push_back(-1);
  sizes_.push_back(0);
  types_.push_back(0);
  return *this;
}

Database::Database(const char* connection_string)
    : connection_(PQconnectdb(connection_string)),
      result_(nullptr),
      result_index_(0) {
  if (PQstatus(connection_) != CONNECTION_OK) {
    const std::string error = std::string(PQerrorMessage(connection_));
    PQfinish(connection_);
    throw std::runtime_error(("Failed connecting to database: '" + error +
                              "', connection string used: '" +
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
  std::shared_ptr<const char> result(
      PQescapeLiteral(connection_, text.c_str(), text.size()), &PQfreemem);
  return result.get() ? std::string(result.get()) : std::string();
}

std::string Database::EscapeIdentifier(const std::string& text) const {
  std::shared_ptr<const char> result(
      PQescapeIdentifier(connection_, text.c_str(), text.size()), &PQfreemem);
  return result.get() ? std::string(result.get()) : std::string();
}
