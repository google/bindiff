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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_DATABASE_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_DATABASE_H_

#include <functional>
#include <memory>
#include <string>
#include <vector>

#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/status.h"

namespace security {
namespace binexport {

class Statement {
 public:
  virtual ~Statement() = default;

  virtual Statement* BindInt(int value) = 0;
  virtual Statement* BindInt64(int64 value) = 0;
  virtual Statement* BindDouble(double value) = 0;
  virtual Statement* BindText(absl::string_view text) = 0;
  virtual Statement* BindNull() = 0;

  virtual Statement* Into(int* value, bool* is_null = nullptr) = 0;
  virtual Statement* Into(int64* value, bool* is_null = nullptr) = 0;
  virtual Statement* Into(double* value, bool* is_null = nullptr) = 0;
  virtual Statement* Into(std::string* value, bool* is_null = nullptr) = 0;

  virtual Statement* Execute() = 0;
  virtual void Reset() = 0;
};

class Database {
 public:
  Database(const Database&) = delete;
  Database& operator=(const Database&) = delete;

  virtual ~Database() = default;

  not_absl::Status Execute(absl::string_view sql) {
    ExecuteNoResult(sql);
    return status();
  }

  // Prepares a SQL statement for execution. Returns the new statement.
  // Arguments can be bound by using of its Bind*() methods.
  virtual std::unique_ptr<Statement> Prepare(absl::string_view sql) = 0;

  // Runs the given functor t in a transaction. If t returns true, commits
  // the transaction, otherwise rolls it back.
  void Transaction(std::function<bool(Database* context)> t);

  bool ok() const { return status().ok(); }
  const not_absl::Status& status() const { return status_; }

 protected:
  Database() = default;

  void set_status(not_absl::Status status) { status_ = std::move(status); }

  // Executes a SQL statement without retrieving the reults. The default
  // implementation just calls Execute() on a prepared statement.
  virtual void ExecuteNoResult(absl::string_view sql) {
    Prepare(sql)->Execute();
  }

 private:
  virtual void Begin() = 0;
  virtual void Commit() = 0;
  virtual void Rollback() = 0;

  not_absl::Status status_;
};

inline void Database::Transaction(std::function<bool(Database* context)> t) {
  Begin();
  if (t(this)) {
    Commit();
  } else {
    Rollback();
  }
}

}  // namespace binexport
}  // namespace security

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_DATABASE_H_
