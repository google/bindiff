// Copyright 2023 Google LLC
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

#include "third_party/zynamics/bindiff/database_writer.h"

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/zynamics/bindiff/match/call_graph.h"
#include "third_party/zynamics/bindiff/sqlite.h"
#include "third_party/zynamics/bindiff/test_util.h"
#include "third_party/zynamics/binexport/testing.h"
#include "third_party/zynamics/binexport/util/status_matchers.h"

namespace security::bindiff {
namespace {

using ::not_absl::IsOk;
using ::security::binexport::GetTestTempPath;
using ::testing::Eq;
using ::testing::StrEq;

class DatabaseWriterTest : public BinDiffTest {
 private:
  void SetUp() override {
    SetUpBasicFunctionMatch();
  }
};

TEST_F(DatabaseWriterTest, SimpleDatabaseCreation) {
  const std::string db_path = GetTestTempPath("test_database_writer_1.sqlite");
  NA_ASSERT_OK_AND_ASSIGN(
      auto writer, DatabaseWriter::Create(db_path, DatabaseWriter::Options()));
  EXPECT_NO_THROW(writer->Write(primary_->call_graph, secondary_->call_graph,
                                primary_->flow_graphs, secondary_->flow_graphs,
                                fixed_points_));

  NA_ASSERT_OK_AND_ASSIGN(auto database, SqliteDatabase::Connect(db_path));
  NA_ASSERT_OK_AND_ASSIGN(
      auto stmt,
      database.Statement(
          "SELECT COUNT(*) FROM functionalgorithm WHERE name = :name"));
  EXPECT_THAT(stmt.BindText(MatchingStep::kFunctionManualName).Execute(),
              IsOk());
  int count = 0;
  stmt.Into(&count);
  EXPECT_THAT(count, Eq(1));

  NA_ASSERT_OK_AND_ASSIGN(
      stmt,
      database.Statement("SELECT a.name FROM function AS f, functionalgorithm "
                         "AS a WHERE f.algorithm = a.id LIMIT 1"));
  EXPECT_THAT(stmt.Execute(), IsOk());
  std::string algorithm;
  stmt.Into(&algorithm);
  EXPECT_THAT(algorithm, StrEq(MatchingStep::kFunctionManualName));
}

}  // namespace
}  // namespace security::bindiff
