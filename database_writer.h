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

#ifndef DATABASE_WRITER_H_
#define DATABASE_WRITER_H_

#include <string>

#include "third_party/absl/status/status.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/sqlite.h"
#include "third_party/zynamics/bindiff/writer.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

struct DatabaseWriterOptions {
  DatabaseWriterOptions& set_include_function_names(bool value) {
    include_function_names = value;
    return *this;
  }

  bool include_function_names = true;
};

class DatabaseWriter : public Writer {
 public:
  using Options = DatabaseWriterOptions;

  // Regular constructor for creating result databases.
  explicit DatabaseWriter(const std::string& path, Options options = {});

  // Special constructor for creating the temporary database.
  DatabaseWriter(const std::string& path, bool recreate);

  void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
             const FlowGraphs& flow_graphs1, const FlowGraphs& flow_graphs2,
             const FixedPoints& fixed_points) override;

  void Close();
  void WriteToTempDatabase(const FixedPoint& fixed_point);
  void DeleteFromTempDatabase(Address primary, Address secondary);
  const std::string& GetFilename() const;

  // Mark all matches in the database for which comments have been ported.
  void SetCommentsPorted(const FixedPointInfos& fixed_points);

  // Exposing internal details for use in the temporary database.
  SqliteDatabase* GetDatabase();

 private:
  void PrepareDatabase();
  void WriteMetaData(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);
  void WriteMatches(const FixedPoints& fixed_points);
  void WriteAlgorithms();

  using NameToId = std::map<std::string, int>;
  NameToId basic_block_steps_;
  NameToId function_steps_;
  std::string filename_;
  Options options_;
  SqliteDatabase database_;
};

class DatabaseTransmuter : public Writer {
 public:
  DatabaseTransmuter(SqliteDatabase& database,
                     const FixedPointInfos& fixed_points);
  void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
             const FlowGraphs& flow_graphs1, const FlowGraphs& flow_graphs2,
             const FixedPoints& fixed_points) override;

  static void MarkPortedComments(SqliteDatabase* database,
                                 const char* temp_database,
                                 const FixedPointInfos& fixed_points);
  static void DeleteTempFile();

 private:
  using TempFixedPoints = std::set<std::pair<Address, Address>>;

  void DeleteMatches(const TempFixedPoints& kill_me);

  SqliteDatabase& database_;
  TempFixedPoints fixed_points_;
  const FixedPointInfos& fixed_point_infos_;
};

class DatabaseReader : public Reader {
 public:
  explicit DatabaseReader(SqliteDatabase& database, const std::string& filename,
                          const std::string& temp_directory);
  absl::Status Read(CallGraph& call_graph1, CallGraph& call_graph2,
                    FlowGraphInfos& flow_graphs1, FlowGraphInfos& flow_graphs2,
                    FixedPointInfos& fixed_points) override;

  static void ReadFullMatches(SqliteDatabase* database,
                              CallGraph* call_graph1, CallGraph* call_graph2,
                              FlowGraphs* flow_graphs1,
                              FlowGraphs* flow_graphs2,
                              FixedPoints* fixed_points);

  std::string GetInputFilename() const;
  std::string GetPrimaryFilename() const;
  std::string GetSecondaryFilename() const;
  const Histogram& GetBasicBlockFixedPointInfo() const;

 private:
  SqliteDatabase& database_;
  std::string input_filename_;
  std::string primary_filename_;
  std::string secondary_filename_;
  std::string path_;
  std::string temporary_directory_;
  Histogram basic_block_fixed_point_info_;
};

}  // namespace security::bindiff

#endif  // DATABASE_WRITER_H_
