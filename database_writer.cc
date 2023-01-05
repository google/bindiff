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

#include "third_party/zynamics/bindiff/database_writer.h"

#include <cstdio>
#include <fstream>
#include <memory>

#include "third_party/absl/container/btree_set.h"
#include "third_party/absl/container/flat_hash_set.h"
#include "third_party/absl/log/log.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/match/call_graph.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"
#include "third_party/zynamics/bindiff/sqlite.h"
#include "third_party/zynamics/bindiff/version.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"
#include "third_party/zynamics/binexport/util/status_macros.h"

namespace security::bindiff {

using ::security::binexport::FormatAddress;

void GetCounts(const FixedPoint& fixed_point, int& basic_blocks, int& edges,
               int& instructions) {
  FlowGraphs flow1, flow2;
  flow1.insert(fixed_point.GetPrimary());
  flow2.insert(fixed_point.GetSecondary());
  FixedPoints fix;
  fix.insert(fixed_point);
  Histogram hist;
  Counts counts;
  GetCountsAndHistogram(flow1, flow2, fix, &hist, &counts);

  basic_blocks = fixed_point.GetBasicBlockFixedPoints().size();
  edges = counts[Counts::kFlowGraphEdgeMatchesLibrary] +
          counts[Counts::kFlowGraphEdgeMatchesNonLibrary];
  instructions = counts[Counts::kInstructionMatchesLibrary] +
                 counts[Counts::kInstructionMatchesNonLibrary];
}

absl::Status ReadInfos(const std::string& filename, CallGraph& call_graph,
                       FlowGraphInfos& flow_graph_infos) {
  std::ifstream file(filename.c_str(), std::ios_base::binary);
  if (!file) {
    return absl::FailedPreconditionError(
        absl::StrCat("failed reading \"", filename, "\""));
  }
  BinExport2 proto;
  if (!proto.ParseFromIstream(&file)) {
    return absl::FailedPreconditionError("failed parsing protocol buffer");
  }

  const auto& meta_information = proto.meta_information();
  call_graph.SetExeFilename(meta_information.executable_name());
  call_graph.SetExeHash(meta_information.executable_id());
  NA_RETURN_IF_ERROR(call_graph.Read(proto, filename));

  Instruction::Cache instruction_cache;
  for (const auto& flow_graph_proto : proto.flow_graph()) {
    // Create an ephemeral FlowGraph instance to update the instruction cache
    // and to use it to parse the BinExport2 information.
    FlowGraph flow_graph;
    NA_RETURN_IF_ERROR(flow_graph.Read(proto, flow_graph_proto, &call_graph,
                                       &instruction_cache));

    Counts counts;
    Count(flow_graph, &counts);
    Address address = flow_graph.GetEntryPointAddress();
    FlowGraphInfo& info = flow_graph_infos[address];
    info.address = address;
    info.name = &flow_graph.GetName();
    info.demangled_name = &flow_graph.GetDemangledName();
    info.basic_block_count = counts[Counts::kBasicBlocksLibrary] +
                             counts[Counts::kBasicBlocksNonLibrary];
    info.edge_count =
        counts[Counts::kEdgesLibrary] + counts[Counts::kEdgesNonLibrary];
    info.instruction_count = counts[Counts::kInstructionsLibrary] +
                             counts[Counts::kInstructionsNonLibrary];
  }
  return absl::OkStatus();
}

DatabaseWriter::DatabaseWriter(const std::string& path, Options options)
    : filename_(path),
      options_(std::move(options)),
      database_(*SqliteDatabase::Connect(filename_)) {
  if (absl::Status status = PrepareDatabase(); !status.ok()) {
    throw std::runtime_error(std::string(status.message()));
  }
}

DatabaseWriter::DatabaseWriter(const std::string& path, bool recreate)
    : database_([this, &path, recreate]() -> SqliteDatabase {
        auto temp_dir = GetOrCreateTempDirectory("BinDiff");
        if (!temp_dir.ok()) {
          throw std::runtime_error(std::string(temp_dir.status().message()));
        }
        filename_ = JoinPath(*temp_dir, Basename(path));
        if (recreate) {
          std::remove(filename_.c_str());
        }
        auto database = SqliteDatabase::Connect(filename_);
        if (!database.ok()) {
          throw std::runtime_error(std::string(database.status().message()));
        }
        return std::move(*database);
      }()) {
  options_.include_function_names = false;
  if (recreate) {
    absl::Status status;
    if (status = PrepareDatabase(); !status.ok()) {
      throw std::runtime_error(std::string(status.message()));
    }
    if (status = WriteAlgorithms(); !status.ok()) {
      throw std::runtime_error(std::string(status.message()));
    }
  }
}

SqliteDatabase* DatabaseWriter::GetDatabase() { return &database_; }

const std::string& DatabaseWriter::GetFilename() const { return filename_; }

void DatabaseWriter::Close() { database_.Disconnect(); }

void DatabaseWriter::SetCommentsPorted(const FixedPointInfos& fixed_points) {
  database_
      .StatementOrThrow(
          "CREATE TABLE IF NOT EXISTS commentsported ("
          "address BIGINT PRIMARY KEY)")
      .ExecuteOrThrow();
  database_.StatementOrThrow("DELETE FROM commentsported").ExecuteOrThrow();
  // We don't have to account for address2 because every function can only be
  // matched once.
  SqliteStatement statement = database_.StatementOrThrow(
      "INSERT INTO commentsported VALUES (:address)");
  for (const auto& fixed_point : fixed_points) {
    if (fixed_point.comments_ported) {
      statement.BindInt64(fixed_point.primary).ExecuteOrThrow().Reset();
    }
  }
}

void DatabaseWriter::WriteToTempDatabase(const FixedPoint& fixed_point) {
  DeleteFromTempDatabase(fixed_point.GetPrimary()->GetEntryPointAddress(),
                         fixed_point.GetSecondary()->GetEntryPointAddress());

  FixedPoints fixed_points;
  fixed_points.insert(fixed_point);
  if (absl::Status status = WriteMatches(fixed_points); !status.ok()) {
    throw std::runtime_error(std::string(status.message()));
  }
}

// Deletes a function match from the result database.
void DatabaseWriter::DeleteFromTempDatabase(Address primary,
                                            Address secondary) {
  // Delete instructions
  database_
      .StatementOrThrow(
          "DELETE FROM instruction WHERE basicblockid IN ("
          "SELECT b.id FROM function AS f "
          "INNER JOIN basicblock AS b ON b.functionid = f.id "
          "WHERE f.address1 = :address1 AND f.address2 = :address2"
          ")")
      .BindInt64(primary)
      .BindInt64(secondary)
      .ExecuteOrThrow();

  // Delete basic blocks
  database_
      .StatementOrThrow(
          "DELETE FROM basicblock WHERE functionid IN ("
          "SELECT f.id FROM function AS f "
          "WHERE f.address1 = :address1 AND f.address2 = :address2"
          ")")
      .BindInt64(primary)
      .BindInt64(secondary)
      .ExecuteOrThrow();

  // Delete functions
  database_
      .StatementOrThrow(
          "DELETE FROM function "
          "WHERE address1 = :address1 AND address2 = :address2")
      .BindInt64(primary)
      .BindInt64(secondary)
      .ExecuteOrThrow();
}

absl::Status DatabaseWriter::PrepareDatabase() {
  NA_RETURN_IF_ERROR(database_.Execute("DROP TABLE IF EXISTS metadata"));
  NA_RETURN_IF_ERROR(database_.Execute("DROP TABLE IF EXISTS file"));
  NA_RETURN_IF_ERROR(database_.Execute("DROP TABLE IF EXISTS instruction"));
  NA_RETURN_IF_ERROR(database_.Execute("DROP TABLE IF EXISTS basicblock"));
  NA_RETURN_IF_ERROR(
      database_.Execute("DROP TABLE IF EXISTS basicblockalgorithm"));
  NA_RETURN_IF_ERROR(database_.Execute("DROP TABLE IF EXISTS function"));
  NA_RETURN_IF_ERROR(
      database_.Execute("DROP TABLE IF EXISTS functionalgorithm"));

  NA_RETURN_IF_ERROR(
      database_.Execute("CREATE TABLE basicblockalgorithm ("
                        "id SMALLINT PRIMARY KEY,"
                        "name TEXT"
                        ")"));
  NA_RETURN_IF_ERROR(
      database_.Execute("CREATE TABLE functionalgorithm ("
                        "id SMALLINT PRIMARY KEY,"
                        "name TEXT"
                        ")"));
  NA_RETURN_IF_ERROR(
      database_.Execute("CREATE TABLE file ("
                        "id INT,"
                        "filename TEXT,"
                        "exefilename TEXT,"
                        "hash CHARACTER(40),"
                        "functions INT,"
                        "libfunctions INT,"
                        "calls INT,"
                        "basicblocks INT,"
                        "libbasicblocks INT,"
                        "edges INT,"
                        "libedges INT,"
                        "instructions INT,"
                        "libinstructions INT"
                        ")"));
  NA_RETURN_IF_ERROR(
      database_.Execute("CREATE TABLE metadata ("
                        "version TEXT,"
                        "file1 INT,"
                        "file2 INT,"
                        "description TEXT,"
                        "created DATE,"
                        "modified DATE,"
                        "similarity DOUBLE PRECISION,"
                        "confidence DOUBLE PRECISION,"
                        "FOREIGN KEY(file1) REFERENCES file(id),"
                        "FOREIGN KEY(file2) REFERENCES file(id)"
                        ")"));
  NA_RETURN_IF_ERROR(database_.Execute(
      "CREATE TABLE function ("
      "id INT,"
      "address1 BIGINT,"
      "name1 TEXT,"
      "address2 BIGINT,"
      "name2 TEXT,"
      "similarity DOUBLE PRECISION,"
      "confidence DOUBLE PRECISION,"
      "flags INTEGER,"
      "algorithm SMALLINT,"
      "evaluate BOOLEAN,"
      "commentsported BOOLEAN,"
      "basicblocks INTEGER,"
      "edges INTEGER,"
      "instructions INTEGER,"
      "UNIQUE(address1, address2),"
      "PRIMARY KEY(id),"
      "FOREIGN KEY(algorithm) REFERENCES functionalgorithm(id)"
      ")"));
  NA_RETURN_IF_ERROR(database_.Execute(
      "CREATE TABLE basicblock ("
      "id INT,"
      "functionid INT,"
      "address1 BIGINT,"
      "address2 BIGINT,"
      "algorithm SMALLINT,"
      "evaluate BOOLEAN,"
      "PRIMARY KEY(id),"
      "FOREIGN KEY(functionid) REFERENCES function(id),"
      "FOREIGN KEY(algorithm) REFERENCES basicblockalgorithm(id)"
      ")"));
  NA_RETURN_IF_ERROR(
      database_.Execute("CREATE TABLE instruction ("
                        "basicblockid INT,"
                        "address1 BIGINT,"
                        "address2 BIGINT,"
                        "FOREIGN KEY(basicblockid) REFERENCES basicblock(id)"
                        ")"));
  return absl::OkStatus();
}

absl::Status DatabaseWriter::WriteMetadata(const CallGraph& call_graph1,
                                           const CallGraph& call_graph2,
                                           const FlowGraphs& flow_graphs1,
                                           const FlowGraphs& flow_graphs2,
                                           const FixedPoints& fixed_points) {
  Confidences confidences;
  Histogram histogram;
  Counts counts;
  GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points, &histogram,
                        &counts);

  int file1 = 1;
  int file2 = 2;
  NA_ASSIGN_OR_RETURN(
      SqliteStatement stmt,
      database_.Statement(
          "INSERT INTO file VALUES ("
          ":id,:filename,:exefilename,:hash,:functions,:libfunctions,:calls,"
          ":basicblocks,:libbasicblocks,:edges,:libedges,:instructions,"
          ":libinstructions"
          ")"));
  NA_RETURN_IF_ERROR(
      stmt.BindInt(file1)
          .BindText(call_graph1.GetFilename().c_str())
          .BindText(call_graph1.GetExeFilename().c_str())
          .BindText(call_graph1.GetExeHash().c_str())
          .BindInt(counts[Counts::kFunctionsPrimaryNonLibrary])
          .BindInt(counts[Counts::kFunctionsPrimaryLibrary])
          .BindInt(boost::num_edges(call_graph1.GetGraph()))
          .BindInt(counts[Counts::kBasicBlocksPrimaryNonLibrary])
          .BindInt(counts[Counts::kBasicBlocksPrimaryLibrary])
          .BindInt(counts[Counts::kFlowGraphEdgesPrimaryNonLibrary])
          .BindInt(counts[Counts::kFlowGraphEdgesPrimaryLibrary])
          .BindInt(counts[Counts::kInstructionsPrimaryNonLibrary])
          .BindInt(counts[Counts::kInstructionsPrimaryLibrary])
          .Execute());

  NA_ASSIGN_OR_RETURN(
      stmt,
      database_.Statement(
          "INSERT INTO file VALUES ("
          ":id,:filename,:exefilename,:hash,:functions,:libfunctions,:calls,"
          ":basicblocks,:libbasicblocks,:edges,:libedges,:instructions,"
          ":libinstructions"
          ")"));
  NA_RETURN_IF_ERROR(
      stmt.BindInt(file2)
          .BindText(call_graph2.GetFilename().c_str())
          .BindText(call_graph2.GetExeFilename().c_str())
          .BindText(call_graph2.GetExeHash().c_str())
          .BindInt(counts[Counts::kFunctionsSecondaryNonLibrary])
          .BindInt(counts[Counts::kFunctionsSecondaryLibrary])
          .BindInt(boost::num_edges(call_graph2.GetGraph()))
          .BindInt(counts[Counts::kBasicBlocksSecondaryNonLibrary])
          .BindInt(counts[Counts::kBasicBlocksSecondaryLibrary])
          .BindInt(counts[Counts::kFlowGraphEdgesSecondaryNonLibrary])
          .BindInt(counts[Counts::kFlowGraphEdgesSecondaryLibrary])
          .BindInt(counts[Counts::kInstructionsSecondaryNonLibrary])
          .BindInt(counts[Counts::kInstructionsSecondaryLibrary])
          .Execute());

  NA_ASSIGN_OR_RETURN(
      stmt,
      database_.Statement("INSERT INTO metadata VALUES ("
                          ":version,:file1,:file2,:description,DATETIME('NOW'),"
                          "DATETIME('NOW'),:similarity,:confidence"
                          ")"));
  NA_RETURN_IF_ERROR(
      stmt.BindText(absl::StrCat("BinDiff ", kBinDiffDetailedVersion))
          .BindInt(file1)
          .BindInt(file2)
          .BindText("")
          .BindDouble(
              GetSimilarityScore(call_graph1, call_graph2, histogram, counts))
          .BindDouble(GetConfidence(histogram, &confidences))
          .Execute());
  return absl::OkStatus();
}

absl::Status DatabaseWriter::WriteMatches(const FixedPoints& fixed_points) {
  std::string temp;
  NA_ASSIGN_OR_RETURN(
      SqliteStatement stmt,
      database_.Statement("SELECT COALESCE(MAX(id) + 1, 1) FROM function"));
  NA_RETURN_IF_ERROR(stmt.Execute());
  stmt.Into(&temp);
  int function_id = std::stoi(temp);

  NA_ASSIGN_OR_RETURN(
      stmt,
      database_.Statement("SELECT COALESCE(MAX(id) + 1, 1) FROM basicblock"));
  NA_RETURN_IF_ERROR(stmt.Execute());
  stmt.Into(&temp);
  int basic_block_id = std::stoi(temp);

  NA_ASSIGN_OR_RETURN(
      SqliteStatement function_match_statement,
      database_.Statement(
          "INSERT INTO function VALUES ("
          ":id,:primary,:name1,:secondary,:name2,:similarity,:confidence,:"
          "flags,"
          ":step,:evaluate,:commentsported,:basicblocks,:edges,:instructions"
          ")"));
  NA_ASSIGN_OR_RETURN(
      SqliteStatement basic_block_match_statement,
      database_.Statement(
          "INSERT INTO basicblock VALUES ("
          ":id,:functionId,:primaryBB,:secondaryBB,:step,:evaluate"
          ")"));
  NA_ASSIGN_OR_RETURN(
      SqliteStatement instruction_statement,
      database_.Statement(
          "INSERT INTO instruction VALUES ("
          ":basicBlockId,:primaryInstruction,:secondaryInstruction"
          ")"));
  for (auto i = fixed_points.cbegin(); i != fixed_points.cend();
       ++i, ++function_id) {
    int basic_block_count = 0;
    int edge_count = 0;
    int instruction_count = 0;
    GetCounts(*i, basic_block_count, edge_count, instruction_count);
    const FlowGraph& primary = *i->GetPrimary();
    const FlowGraph& secondary = *i->GetSecondary();
    std::string primary_name;
    std::string secondary_name;
    if (options_.include_function_names) {
      if (primary_name = primary.GetDemangledName(); primary_name.empty()) {
        primary_name = primary.GetName();
      }
      if (secondary_name = secondary.GetDemangledName();
          secondary_name.empty()) {
        secondary_name = secondary.GetName();
      }
    }
    NA_RETURN_IF_ERROR(function_match_statement.BindInt(function_id)
                           .BindInt64(primary.GetEntryPointAddress())
                           .BindText(primary_name)
                           .BindInt64(secondary.GetEntryPointAddress())
                           .BindText(secondary_name)
                           .BindDouble(i->GetSimilarity())
                           .BindDouble(i->GetConfidence())
                           .BindInt(i->GetFlags())
                           .BindInt(function_steps_[i->GetMatchingStep()])
                           .BindInt(0)
                           .BindInt(i->GetCommentsPorted() ? 1 : 0)
                           .BindInt(basic_block_count)
                           .BindInt(edge_count)
                           .BindInt(instruction_count)
                           .Execute());
    function_match_statement.Reset();

    for (auto j = i->GetBasicBlockFixedPoints().cbegin(),
              jend = i->GetBasicBlockFixedPoints().cend();
         j != jend; ++j, ++basic_block_id) {
      NA_RETURN_IF_ERROR(
          basic_block_match_statement.BindInt(basic_block_id)
              .BindInt(function_id)
              .BindInt64(primary.GetAddress(j->GetPrimaryVertex()))
              .BindInt64(secondary.GetAddress(j->GetSecondaryVertex()))
              .BindInt(basic_block_steps_[j->GetMatchingStep()])
              .BindInt(0)
              .Execute());
      basic_block_match_statement.Reset();

      for (auto k = j->GetInstructionMatches().cbegin(),
                kend = j->GetInstructionMatches().cend();
           k != kend; ++k) {
        NA_RETURN_IF_ERROR(instruction_statement.BindInt(basic_block_id)
                               .BindInt64(k->first->GetAddress())
                               .BindInt64(k->second->GetAddress())
                               .Execute());
        instruction_statement.Reset();
      }
    }
  }
  return absl::OkStatus();
}

absl::Status DatabaseWriter::WriteAlgorithms() {
  if (!basic_block_steps_.empty()) {
    return absl::OkStatus();  // Assume we have already done this step.
  }

  int id = 0;
  for (const auto* step : GetDefaultMatchingStepsBasicBlock()) {
    basic_block_steps_[step->name()] = ++id;
    NA_ASSIGN_OR_RETURN(
        SqliteStatement algorithm_statement,
        database_.Statement(
            "INSERT INTO basicblockalgorithm VALUES (:id, :name)"));
    NA_RETURN_IF_ERROR(
        algorithm_statement.BindInt(id).BindText(step->name()).Execute());
  }
  basic_block_steps_[MatchingStepFlowGraph::kBasicBlockPropagationName] = ++id;
  NA_ASSIGN_OR_RETURN(
      SqliteStatement algorithm_statement,
      database_.Statement(
          "INSERT INTO basicblockalgorithm VALUES (:id, :name)"));
  NA_RETURN_IF_ERROR(
      algorithm_statement.BindInt(id)
          .BindText(MatchingStepFlowGraph::kBasicBlockPropagationName)
          .Execute());

  basic_block_steps_[MatchingStepFlowGraph::kBasicBlockManualName] = ++id;
  NA_ASSIGN_OR_RETURN(
      algorithm_statement,
      database_.Statement(
          "INSERT INTO basicblockalgorithm VALUES (:id, :name)"));
  NA_RETURN_IF_ERROR(algorithm_statement.BindInt(id)
                         .BindText(MatchingStepFlowGraph::kBasicBlockManualName)
                         .Execute());

  id = 0;
  for (const auto* step : GetDefaultMatchingSteps()) {
    function_steps_[step->name()] = ++id;
    NA_ASSIGN_OR_RETURN(
        algorithm_statement,
        database_.Statement(
            "INSERT INTO functionalgorithm VALUES (:id, :name)"));
    NA_RETURN_IF_ERROR(algorithm_statement.BindInt(id)
                           .BindText(step->name().c_str())
                           .Execute());
  }
  function_steps_[MatchingStep::kFunctionCallReferenceName] = ++id;
  NA_ASSIGN_OR_RETURN(
      algorithm_statement,
      database_.Statement("INSERT INTO functionalgorithm VALUES (:id, :name)"));
  NA_RETURN_IF_ERROR(algorithm_statement.BindInt(id)
                         .BindText(MatchingStep::kFunctionCallReferenceName)
                         .Execute());

  function_steps_[MatchingStep::kFunctionManualName] = ++id;
  NA_ASSIGN_OR_RETURN(
      algorithm_statement,
      database_.Statement("INSERT INTO functionalgorithm VALUES (:id, :name)"));
  NA_RETURN_IF_ERROR(algorithm_statement.BindInt(id)
                         .BindText(MatchingStep::kFunctionManualName)
                         .Execute());
  return absl::OkStatus();
}

void DatabaseWriter::Write(const CallGraph& call_graph1,
                           const CallGraph& call_graph2,
                           const FlowGraphs& flow_graphs1,
                           const FlowGraphs& flow_graphs2,
                           const FixedPoints& fixed_points) {
  try {
    auto status = [&]() -> absl::Status {
      NA_RETURN_IF_ERROR(database_.Begin());

      NA_RETURN_IF_ERROR(WriteMetadata(call_graph1, call_graph2, flow_graphs1,
                                       flow_graphs2, fixed_points));
      NA_RETURN_IF_ERROR(WriteAlgorithms());
      NA_RETURN_IF_ERROR(WriteMatches(fixed_points));

      NA_RETURN_IF_ERROR(database_.Commit());
      return absl::OkStatus();
    }();
    if (!status.ok()) {
      throw std::runtime_error(std::string(status.message()));
    }
  } catch (...) {
    database_.Rollback().IgnoreError();
    throw;
  }
}

DatabaseTransmuter::DatabaseTransmuter(SqliteDatabase& database,
                                       const FixedPointInfos& fixed_points)
    : database_(database), fixed_points_(), fixed_point_infos_(fixed_points) {
  for (auto i = fixed_points.cbegin(), end = fixed_points.cend(); i != end;
       ++i) {
    fixed_points_.insert(std::make_pair(i->primary, i->secondary));
  }
}

absl::Status DatabaseTransmuter::DeleteMatches(const TempFixedPoints& kill_me) {
  for (auto i = kill_me.cbegin(), end = kill_me.cend(); i != end; ++i) {
    const Address primary_address = i->first;
    const Address secondary_address = i->second;
    NA_ASSIGN_OR_RETURN(
        SqliteStatement match_delete_statement,
        database_.Statement(
            "DELETE FROM instruction WHERE basicblockid IN ("
            "SELECT b.id FROM function AS f "
            "INNER JOIN basicblock AS b ON b.functionid = f.id "
            "WHERE f.address1 = :address1 AND f.address2 = :address2"
            ")"));
    NA_RETURN_IF_ERROR(match_delete_statement.BindInt64(primary_address)
                           .BindInt64(secondary_address)
                           .Execute());

    NA_ASSIGN_OR_RETURN(
        match_delete_statement,
        database_.Statement(
            "DELETE FROM basicblock WHERE functionid IN ("
            "SELECT f.id FROM function AS f "
            "WHERE f.address1 = :address1 AND f.address2 = :address2"
            ")"));
    NA_RETURN_IF_ERROR(match_delete_statement.BindInt64(primary_address)
                           .BindInt64(secondary_address)
                           .Execute());

    NA_ASSIGN_OR_RETURN(
        match_delete_statement,
        database_.Statement(
            "DELETE FROM function "
            "WHERE address1 = :address1 AND address2 = :address2"));
    NA_RETURN_IF_ERROR(match_delete_statement.BindInt64(primary_address)
                           .BindInt64(secondary_address)
                           .Execute());
  }
  return absl::OkStatus();
}

absl::StatusOr<std::string> GetTempFileName() {
  std::string temp_dir;
  NA_ASSIGN_OR_RETURN(temp_dir, GetOrCreateTempDirectory("BinDiff"));
  return JoinPath(temp_dir, "temporary.database");
}

void DatabaseTransmuter::DeleteTempFile() {
  if (GetTempDirectory("BinDiff").ok()) {
    std::remove(GetTempFileName().value().c_str());
  }
}

void DatabaseTransmuter::MarkPortedComments(
    SqliteDatabase* database, const char* temp_database,
    const FixedPointInfos& /* fixed_points */) {
  database->StatementOrThrow("ATTACH :filename AS ported")
      .BindText(temp_database)
      .ExecuteOrThrow();

  database
      ->StatementOrThrow(
          "UPDATE function SET commentsported = EXISTS("
          "SELECT * FROM ported.commentsported WHERE address = address1"
          ") "
          "WHERE commentsported = 0")
      .ExecuteOrThrow();
}

void DatabaseTransmuter::Write(const CallGraph& /*call_graph1*/,
                               const CallGraph& /*call_graph2*/,
                               const FlowGraphs& /*flow_graphs1*/,
                               const FlowGraphs& /*flow_graphs2*/,
                               const FixedPoints& /*fixed_points*/) {
  // Step 1: Remove deleted matches.
  TempFixedPoints current_fixed_points;

  {
    SqliteStatement statement =
        database_.StatementOrThrow("SELECT address1, address2 FROM function");
    statement.ExecuteOrThrow();
    for (; statement.GotData(); statement.ExecuteOrThrow()) {
      int64_t primary;
      int64_t secondary;
      statement.Into(&primary).Into(&secondary);
      current_fixed_points.insert(std::make_pair(
          static_cast<Address>(primary), static_cast<Address>(secondary)));
    }
  }

  TempFixedPoints kill_me;
  std::set_difference(current_fixed_points.begin(), current_fixed_points.end(),
                      fixed_points_.begin(), fixed_points_.end(),
                      std::inserter(kill_me, kill_me.begin()));
  if (absl::Status status = DeleteMatches(kill_me); !status.ok()) {
    throw std::runtime_error(std::string(status.message()));
  }

  // Step 2: Merge new matches from temp database.
  auto temp_file = GetTempFileName();
  if (!temp_file.ok()) {
    // TODO(cblichmann): Refactor Writer interface to return absl::Status.
    throw std::runtime_error(std::string(temp_file.status().message()));
  }
  if (FileExists(*temp_file)) {
    database_.StatementOrThrow("ATTACH :filename AS newmatches")
        .BindText(temp_file->c_str())
        .ExecuteOrThrow();
    int function_id = 0, basic_block_id = 0;
    database_.StatementOrThrow("SELECT COALESCE(MAX(id), 0) FROM function")
        .ExecuteOrThrow()
        .Into(&function_id);
    database_.StatementOrThrow("SELECT COALESCE(MAX(id), 0) FROM basicblock")
        .ExecuteOrThrow()
        .Into(&basic_block_id);
    database_
        .StatementOrThrow(
            "INSERT INTO function SELECT "
            "id + :id, address1, name1, address2, name2, similarity, "
            "confidence, flags, algorithm, evaluate, commentsported, "
            "basicblocks, edges, instructions "
            "FROM newmatches.function")
        .BindInt(function_id)
        .ExecuteOrThrow();
    database_
        .StatementOrThrow(
            "INSERT INTO basicblock SELECT "
            "id + :id, functionId + :fid, address1, address2, algorithm, "
            "evaluate "
            "FROM newmatches.basicblock")
        .BindInt(basic_block_id)
        .BindInt(function_id)
        .ExecuteOrThrow();
    database_
        .StatementOrThrow(
            "INSERT INTO instruction SELECT "
            "basicblockid + :id, address1, address2 "
            "FROM newmatches.instruction")
        .BindInt(basic_block_id)
        .ExecuteOrThrow();
  }

  // Step 3: Update changed matches (user set algorithm type to "manual").
  int algorithm = 0;
  database_.StatementOrThrow("SELECT MAX(id) FROM functionalgorithm")
      .ExecuteOrThrow()
      .Into(&algorithm);
  SqliteStatement statement = database_.StatementOrThrow(
      "UPDATE function SET confidence=1.0, algorithm=:algorithm "
      "WHERE address1=:address1 AND address2=:address2");
  for (auto i = fixed_point_infos_.cbegin(), end = fixed_point_infos_.cend();
       i != end; ++i) {
    if (!i->IsManual()) {
      continue;
    }
    statement.BindInt(algorithm)
        .BindInt64(i->primary)
        .BindInt64(i->secondary)
        .ExecuteOrThrow()
        .Reset();
  }

  // Step 4: Update last changed timestamp.
  database_.StatementOrThrow("UPDATE metadata SET modified=DATETIME('NOW')")
      .ExecuteOrThrow();
}

DatabaseReader::DatabaseReader(SqliteDatabase& database,
                               const std::string& filename,
                               const std::string& temp_dir)
    : database_(database),
      input_filename_(filename),
      primary_filename_(),
      secondary_filename_(),
      path_(filename),
      temporary_directory_(temp_dir),
      basic_block_fixed_point_info_() {
  std::replace(path_.begin(), path_.end(), '\\', '/');
  const std::string::size_type pos = path_.rfind('/');
  if (pos != std::string::npos) {
    path_ = path_.substr(0, pos + 1);
  }
}

std::string DatabaseReader::GetInputFilename() const { return input_filename_; }

std::string DatabaseReader::GetPrimaryFilename() const {
  return primary_filename_;
}

std::string DatabaseReader::GetSecondaryFilename() const {
  return secondary_filename_;
}

const Histogram& DatabaseReader::GetBasicBlockFixedPointInfo() const {
  return basic_block_fixed_point_info_;
}

void DatabaseReader::ReadFullMatches(SqliteDatabase* database,
                                     CallGraph* call_graph1,
                                     CallGraph* call_graph2,
                                     FlowGraphs* /*flow_graphs1*/,
                                     FlowGraphs* /*flow_graphs2*/,
                                     FixedPoints* fixed_points) {
  SqliteStatement statement = database->StatementOrThrow(
      "SELECT "
      " function.address1, function.address2, functionalgorithm.name, "
      " function.similarity, function.confidence, "
      " basicblock.address1, basicblock.address2, basicblockalgorithm.name "
      "FROM function "
      "INNER JOIN functionalgorithm "
      " ON functionalgorithm.id = function.algorithm "
      "LEFT JOIN basicblock "
      " ON basicblock.functionid = function.id "
      "LEFT JOIN basicblockalgorithm "
      " ON basicblockalgorithm.id = basicblock.algorithm "
      "ORDER BY function.address1, basicblock.address1");
  for (statement.ExecuteOrThrow(); statement.GotData();
       statement.ExecuteOrThrow()) {
    FixedPoint* fixed_point = nullptr;
    Address function1 = 0;
    Address function2 = 0;
    Address basic_block1 = 0;
    Address basic_block2 = 0;
    std::string function_algorithm;
    std::string basic_block_algorithm;
    double similarity = 0.0;
    double confidence = 0.0;
    bool basic_block_is_null = false;
    statement.Into(&function1)
        .Into(&function2)
        .Into(&function_algorithm)
        .Into(&similarity)
        .Into(&confidence)
        .Into(&basic_block1, &basic_block_is_null)
        .Into(&basic_block2)
        .Into(&basic_block_algorithm);
    auto* flow_graph1 = call_graph1->GetFlowGraph(function1);
    if (!fixed_point || flow_graph1 != fixed_point->GetPrimary()) {
      auto* flow_graph2 = call_graph2->GetFlowGraph(function2);
      FixedPoint new_fixed_point;
      new_fixed_point.Create(flow_graph1, flow_graph2);
      new_fixed_point.SetMatchingStep(function_algorithm);
      new_fixed_point.SetSimilarity(similarity);
      new_fixed_point.SetConfidence(confidence);
      fixed_point = const_cast<FixedPoint*>(
          &*fixed_points->insert(new_fixed_point).first);
      flow_graph1->SetFixedPoint(fixed_point);
      flow_graph2->SetFixedPoint(fixed_point);
    }
    if (!basic_block_is_null) {
      auto* flow_graph2 = call_graph2->GetFlowGraph(function2);
      const auto primary_vertex = flow_graph1->GetVertex(basic_block1);
      const auto secondary_vertex = flow_graph2->GetVertex(basic_block2);
      fixed_point->Add(primary_vertex, secondary_vertex, basic_block_algorithm);
    }
  }
}

absl::Status DatabaseReader::Read(CallGraph& call_graph1,
                                  CallGraph& call_graph2,
                                  FlowGraphInfos& flow_graphs1,
                                  FlowGraphInfos& flow_graphs2,
                                  FixedPointInfos& fixed_points) {
  absl::flat_hash_set<FixedPointInfo> database_fixed_points;
  try {
    database_
        .StatementOrThrow(
            "SELECT "
            " file1.filename AS filename1, file2.filename AS filename2, "
            " similarity, confidence "
            "FROM metadata "
            "INNER JOIN file AS file1 ON file1.id = file1 "
            "INNER JOIN file AS file2 ON file2.id = file2")
        .ExecuteOrThrow()
        .Into(&primary_filename_)
        .Into(&secondary_filename_)
        .Into(&similarity_)
        .Into(&confidence_);

    {  // Function matches
      SqliteStatement statement = database_.StatementOrThrow(
          "SELECT address1, address2, similarity, confidence, flags, a.name, "
          "evaluate, commentsported, basicblocks, edges, instructions "
          "FROM function AS f "
          "INNER JOIN functionalgorithm AS a ON a.id = f.algorithm");
      for (statement.ExecuteOrThrow(); statement.GotData();
           statement.ExecuteOrThrow()) {
        std::string algorithm;
        FixedPointInfo fixed_point;
        int evaluate = 0;
        int comments_ported = 0;
        statement.Into(&fixed_point.primary)
            .Into(&fixed_point.secondary)
            .Into(&fixed_point.similarity)
            .Into(&fixed_point.confidence)
            .Into(&fixed_point.flags)
            .Into(&algorithm)
            .Into(&evaluate)
            .Into(&comments_ported)
            .Into(&fixed_point.basic_block_count)
            .Into(&fixed_point.edge_count)
            .Into(&fixed_point.instruction_count);
        fixed_point.algorithm = FindString(algorithm);
        fixed_point.evaluate = evaluate != 0;
        fixed_point.comments_ported = comments_ported != 0;
        database_fixed_points.insert(fixed_point);
      }
    }
    {  // Basic block matches
      SqliteStatement statement = database_.StatementOrThrow(
          "SELECT a.name, COUNT(*) FROM basicblock AS b INNER JOIN "
          "basicblockalgorithm AS a ON a.id = b.algorithm GROUP BY "
          "b.algorithm");
      for (statement.ExecuteOrThrow(); statement.GotData();
           statement.ExecuteOrThrow()) {
        std::string algorithm_name;
        int count = 0;
        statement.Into(&algorithm_name).Into(&count);
        basic_block_fixed_point_info_[algorithm_name] = count;
      }
    }
  } catch (const std::exception& error) {
    return absl::UnknownError(error.what());
  } catch (...) {
    return absl::UnknownError("Unknown error querying matches database");
  }

  NA_RETURN_IF_ERROR(
      ReadInfos(JoinPath(path_, primary_filename_ + ".BinExport"), call_graph1,
                flow_graphs1));
  NA_RETURN_IF_ERROR(
      ReadInfos(JoinPath(path_, secondary_filename_ + ".BinExport"),
                call_graph2, flow_graphs2));

  // Check consistency between BinExport data and results
  bool inconsistent = false;
  for (const auto& fixed_point : database_fixed_points) {
    if (call_graph1.GetVertex(fixed_point.primary) ==
        CallGraph::kInvalidVertex) {
      LOG(ERROR) << "Address " << FormatAddress(fixed_point.primary)
                 << " not in primary call graph";
      inconsistent = true;
    } else if (call_graph2.GetVertex(fixed_point.secondary) ==
               CallGraph::kInvalidVertex) {
      LOG(ERROR) << "Address " << FormatAddress(fixed_point.secondary)
                 << " not in secondary call graph";
      inconsistent = true;
    } else {
      fixed_points.insert(fixed_point);
    }
  }
  LOG_IF(ERROR, inconsistent)
      << "Call graph data is inconsistent, results may not be accurate";
  return absl::OkStatus();
}

}  // namespace security::bindiff
