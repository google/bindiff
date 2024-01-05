// Copyright 2024 Google LLC
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

using ::not_absl::IsOk;
using ::not_absl::StatusIs;
using ::security::binexport::GetTestTempPath;
using ::testing::Eq;
using ::testing::IsFalse;

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
  ASSERT_THAT(
      database_->Execute("CREATE TABLE t1 (id INT PRIMARY KEY, text TEXT)"),
      IsOk());
  {
    absl::StatusOr<SqliteStatement> stmt =
        database_->Statement("INSERT INTO t1 VALUES (:id, :text)");
    ASSERT_THAT(stmt, IsOk());
    EXPECT_THAT(stmt->BindInt(42).BindText("answer").Execute(), IsOk());
  }
  {
    int id = 0;
    absl::StatusOr<SqliteStatement> stmt =
        database_->Statement("SELECT id FROM t1 LIMIT 1");
    ASSERT_THAT(stmt, IsOk());
    EXPECT_THAT(stmt->Execute(), IsOk());
    stmt->Into(&id);
    EXPECT_THAT(id, Eq(42));
  }
  EXPECT_THAT(database_->Execute("DROP TABLE t1"), IsOk());
}

TEST_F(SqliteTest, InvalidSyntax) {
  EXPECT_THAT(database_->Execute("NOTASQL STATEMENT"),
              StatusIs(absl::StatusCode::kUnknown));
}

TEST_F(SqliteTest, RollbackTransaction) {
  ASSERT_THAT(
      database_->Execute("CREATE TABLE t2 (id INT PRIMARY KEY, text TEXT)"),
      IsOk());
  NA_ASSERT_OK_AND_ASSIGN(SqliteStatement stmt,
                          database_->Statement("SELECT id FROM t2 LIMIT 1"));
  ASSERT_THAT(database_->Begin(), IsOk());

  EXPECT_THAT(database_->Execute(R"(INSERT INTO t2 VALUES (10, "ten"))"),
              IsOk());
  int id = 0;
  EXPECT_THAT(stmt.Execute(), IsOk());
  stmt.Into(&id);
  EXPECT_THAT(id, Eq(10));
  EXPECT_THAT(database_->Rollback(), IsOk());

  EXPECT_THAT(stmt.Execute(), IsOk());
  EXPECT_THAT(stmt.GotData(), IsFalse());

  EXPECT_THAT(database_->Execute("DROP TABLE t2"), IsOk());
}

}  // namespace
}  // namespace security::bindiff
