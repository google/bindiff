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

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/zynamics/binexport/testing.h"
#include "third_party/zynamics/binexport/util/status_matchers.h"

namespace security::bindiff {
namespace {

using ::security::binexport::GetTestTempPath;
using ::testing::Eq;

class SqliteTest : public ::testing::Test {
 protected:
  void SetUp() override {
    NA_ASSERT_OK_AND_ASSIGN(
        auto database,
        SqliteDatabase::Connect(GetTestTempPath("test_db.sqlite")));
    database_ = new SqliteDatabase(std::move(database));
  }

  void TearDown() override {
    if (database_) {
      delete database_;
      database_ = nullptr;
    }
  }

  SqliteDatabase* database_;
};

TEST_F(SqliteTest, BasicFunctionality) {
  database_->StatementOrThrow("CREATE TABLE t (id INT PRIMARY KEY, text TEXT)")
      .ExecuteOrThrow();
  database_->StatementOrThrow("INSERT INTO t VALUES (:id, :text)")
      .BindInt(42)
      .BindText("answer")
      .ExecuteOrThrow();

  int id = 0;
  database_->StatementOrThrow("SELECT id FROM t LIMIT 1")
      .ExecuteOrThrow()
      .Into(&id);
  EXPECT_THAT(id, Eq(42));

  database_->StatementOrThrow("DROP TABLE t").ExecuteOrThrow();
}

}  // namespace
}  // namespace security::bindiff
