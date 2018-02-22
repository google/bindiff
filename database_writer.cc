#include "third_party/zynamics/bindiff/database_writer.h"

#include <memory>

#include "third_party/zynamics/bindiff/call_graph_matching.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/flow_graph_matching.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/filesystem_util.h"

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
  edges = counts["flowGraph edge matches (non-library)"] +
      counts["flowGraph edge matches (library)"];
  instructions = counts["instruction matches (non-library)"] +
      counts["instruction matches (library)"];
}

void ReadInfos(const string& filename, CallGraph& call_graph,
               FlowGraphInfos& flow_graph_infos) {
  std::ifstream file(filename.c_str(), std::ios_base::binary);
  if (!file) {
    throw std::runtime_error(("failed reading \"" + filename + "\"").c_str());
  }
  BinExport2 proto;
  if (!proto.ParseFromIstream(&file)) {
    throw std::runtime_error("failed parsing protocol buffer");
  }

  const auto& meta_information = proto.meta_information();
  call_graph.SetExeFilename(meta_information.executable_name());
  call_graph.SetExeHash(meta_information.executable_id());
  call_graph.Read(proto, filename);

  Instruction::Cache instruction_cache;
  for (const auto& flow_graph_proto : proto.flow_graph()) {
    // Create an ephemeral FlowGraph instance to update the instruction cache
    // and to use it to parse the BinExport2 information.
    FlowGraph flow_graph;
    flow_graph.Read(proto, flow_graph_proto, &call_graph, &instruction_cache);

    Counts counts;
    ::Count(flow_graph, &counts);
    auto address = flow_graph.GetEntryPointAddress();
    FlowGraphInfo& info = flow_graph_infos[address];
    info.address = address;
    info.name = &flow_graph.GetName();
    info.demangled_name = &flow_graph.GetDemangledName();
    info.basic_block_count =
        counts["basicBlocks (library)"] + counts["basicBlocks (non-library)"];
    info.edge_count = counts["edges (library)"] + counts["edges (non-library)"];
    info.instruction_count =
        counts["instructions (library)"] + counts["instructions (non-library)"];
  }
}

DatabaseWriter::DatabaseWriter(const string& path)
    : database_(path.c_str()), filename_(path) {
  PrepareDatabase();
}

DatabaseWriter::DatabaseWriter(const string& path, bool recreate) {
  filename_ = JoinPath(GetTempDirectory("BinDiff", /* create = */ true),
                       Basename(path));
  if (recreate) {
    remove(filename_.c_str());
  }
  const bool needs_init = !FileExists(filename_);
  database_.Connect(filename_.c_str());
  if (needs_init) {
    PrepareDatabase();
    WriteAlgorithms();
  }
}

SqliteDatabase* DatabaseWriter::GetDatabase() {
  return &database_;
}

const string& DatabaseWriter::GetFilename() const {
  return filename_;
}

void DatabaseWriter::Close() {
  database_.Disconnect();
}

void DatabaseWriter::SetCommentsPorted(const FixedPointInfos& fixed_points) {
  database_.Statement(
      "create table if not exists \"commentsported\" "
      "(\"address\" bigint primary key);")->Execute();
  database_.Statement("delete from \"commentsported\"")->Execute();
  // We don't have to account for address2 because every function can only be
  // matched once.
  SqliteStatement statement(&database_,
      "insert into \"commentsported\" values (:address)");
  for (auto i = fixed_points.cbegin(), end = fixed_points.cend(); i != end;
       ++i) {
    if (i->comments_ported) {
      statement
          .BindInt64(i->primary)
          .Execute()
          .Reset();
    }
  }
}

void DatabaseWriter::WriteToTempDatabase(const FixedPoint& fixed_point) {
  DeleteFromTempDatabase(fixed_point.GetPrimary()->GetEntryPointAddress(),
                         fixed_point.GetSecondary()->GetEntryPointAddress());

  FixedPoints fixed_points;
  fixed_points.insert(fixed_point);
  WriteMatches(fixed_points);
}

// Deletes a function match from the result database.
void DatabaseWriter::DeleteFromTempDatabase(Address primary,
                                            Address secondary) {
  // delete instructions
  database_.Statement(
      "delete from instruction where basicblockid in ( select b.id from "
      "function as f inner join basicblock as b on b.functionid = f.id "
      "where f.address1 = :address1 and f.address2 = :address2 )")
      ->BindInt64(primary)
      .BindInt64(secondary)
      .Execute();

  // delete basic blocks
  database_.Statement(
      "delete from basicblock where functionid in ( select f.id from "
      "\"function\" as f where f.address1 = :address1 and f.address2 = "
      ":address2 )")
      ->BindInt64(primary)
      .BindInt64(secondary)
      .Execute();

  // delete functions
  database_.Statement(
      "delete from \"function\" where address1 = :address1 and address2 = "
      ":address2")
      ->BindInt64(primary)
      .BindInt64(secondary)
      .Execute();
}

void DatabaseWriter::PrepareDatabase() {
  database_.Statement("DROP TABLE IF EXISTS metadata;")->Execute();
  database_.Statement("DROP TABLE IF EXISTS \"file\";")->Execute();
  database_.Statement("DROP TABLE IF EXISTS instruction;")->Execute();
  database_.Statement("DROP TABLE IF EXISTS basicblock;")->Execute();
  database_.Statement("DROP TABLE IF EXISTS basicblockalgorithm")->Execute();
  database_.Statement("DROP TABLE IF EXISTS function;")->Execute();
  database_.Statement("DROP TABLE IF EXISTS functionalgorithm")->Execute();

  database_.Statement(
      "CREATE TABLE basicblockalgorithm ("
      "id SMALLINT PRIMARY KEY, "
      "name TEXT"
      ");")->Execute();

  database_.Statement(
      "CREATE TABLE functionalgorithm ("
      "id SMALLINT PRIMARY KEY, "
      "name TEXT"
      ");")->Execute();

  database_.Statement(
      "CREATE TABLE \"file\" ("
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
      ");")->Execute();

  database_.Statement(
      "CREATE TABLE \"metadata\" ("
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
      ");")->Execute();

  database_.Statement(
      "CREATE TABLE \"function\" ("
      "id INT,"
      "address1 BIGINT,"
      "address2 BIGINT,"
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
      ");")->Execute();

  database_.Statement(
      "CREATE TABLE basicblock ("
      "id INT,"
      "functionid INT,"
      "address1 BIGINT,"
      "address2 BIGINT,"
      "algorithm SMALLINT,"
      "evaluate BOOLEAN,"
      "PRIMARY KEY(id),"
      "FOREIGN KEY(functionid) REFERENCES \"function\"(id),"
      "FOREIGN KEY(algorithm) REFERENCES basicblockalgorithm(id)"
      ");")->Execute();

  database_.Statement(
      "CREATE TABLE instruction ("
      "basicblockid INT,"
      "address1 BIGINT,"
      "address2 BIGINT,"
      "FOREIGN KEY(basicblockid) REFERENCES basicblock(id)"
      ");")->Execute();
}

void DatabaseWriter::WriteMetaData(const CallGraph& call_graph1,
                                   const CallGraph& call_graph2,
                                   const FlowGraphs& flow_graphs1,
                                   const FlowGraphs& flow_graphs2,
                                   const FixedPoints& fixed_points) {
  Confidences confidences;
  Histogram histogram;
  Counts counts;
  GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points, &histogram,
                        &counts);

  int file1 = 1, file2 = 2;
  database_.Statement(
      "INSERT INTO \"file\" VALUES (:id,:filename,:exefilename,:hash,"
      ":functions,:libfunctions,:calls,:basicblocks,:libbasicblocks,"
      ":edges,:libedges,:instructions,:libinstructions )")
      ->BindInt(file1)
      .BindText(call_graph1.GetFilename().c_str())
      .BindText(call_graph1.GetExeFilename().c_str())
      .BindText(call_graph1.GetExeHash().c_str())
      .BindInt(counts["functions primary (non-library)"])
      .BindInt(counts["functions primary (library)"])
      .BindInt(boost::num_edges(call_graph1.GetGraph()))
      .BindInt(counts["basicBlocks primary (non-library)"])
      .BindInt(counts["basicBlocks primary (library)"])
      .BindInt(counts["flowGraph edges primary (non-library)"])
      .BindInt(counts["flowGraph edges primary (library)"])
      .BindInt(counts["instructions primary (non-library)"])
      .BindInt(counts["instructions primary (library)"])
      .Execute();

  database_.Statement(
      "INSERT INTO \"file\" VALUES (:id,:filename,:exefilename,:hash,"
      ":functions,:libfunctions,:calls,:basicblocks,:libbasicblocks,"
      ":edges,:libedges,:instructions,:libinstructions )")
      ->BindInt(file2)
      .BindText(call_graph2.GetFilename().c_str())
      .BindText(call_graph2.GetExeFilename().c_str())
      .BindText(call_graph2.GetExeHash().c_str())
      .BindInt(counts["functions secondary (non-library)"])
      .BindInt(counts["functions secondary (library)"])
      .BindInt(boost::num_edges(call_graph2.GetGraph()))
      .BindInt(counts["basicBlocks secondary (non-library)"])
      .BindInt(counts["basicBlocks secondary (library)"])
      .BindInt(counts["flowGraph edges secondary (non-library)"])
      .BindInt(counts["flowGraph edges secondary (library)"])
      .BindInt(counts["instructions secondary (non-library)"])
      .BindInt(counts["instructions secondary (library)"])
      .Execute();

  database_.Statement(
      "INSERT INTO \"metadata\" VALUES (:version,:file1,:file2,:description,"
      "DATETIME('NOW'),DATETIME('NOW'),:similarity,:confidence );")
      ->BindText(kProgramVersion)
      .BindInt(file1)
      .BindInt(file2)
      .BindText("")
      .BindDouble(
          GetSimilarityScore(call_graph1, call_graph2, histogram, counts))
      .BindDouble(GetConfidence(histogram, &confidences))
      .Execute();
}

void DatabaseWriter::WriteMatches(const FixedPoints& fixed_points) {
  string temp;
  database_.Statement("select coalesce( max( id ) + 1, 1 ) from \"function\"")
      ->Execute()
      .Into(&temp);

  int function_id = std::stoi(temp);
  database_.Statement("select coalesce( max( id ) + 1, 1 ) from basicblock")
      ->Execute()
      .Into(&temp);

  int basic_block_id = std::stoi(temp);

  SqliteStatement function_match_statement(&database_,
      "insert into \"function\" values (:id,:primary,:secondary,:similarity,"
      ":confidence,:flags,:step,:evaluate,:commentsported,:basicblocks,:edges,"
      ":instructions)");
  SqliteStatement basic_block_match_statement(&database_,
      "insert into basicblock values (:id,:functionId,:primaryBB,:secondaryBB,"
      ":step,:evaluate)");
  SqliteStatement instruction_statement(&database_,
      "insert into instruction values (:basicBlockId,:primaryInstruction,"
      ":secondaryInstruction)");
  for (auto i = fixed_points.cbegin(); i != fixed_points.cend();
       ++i, ++function_id) {
    int basic_block_count, edge_count, instruction_count;
    GetCounts(*i, basic_block_count, edge_count, instruction_count);
    const FlowGraph& primary = *i->GetPrimary();
    const FlowGraph& secondary = *i->GetSecondary();
    function_match_statement
        .BindInt(function_id)
        .BindInt64(primary.GetEntryPointAddress())
        .BindInt64(secondary.GetEntryPointAddress())
        .BindDouble(i->GetSimilarity())
        .BindDouble(i->GetConfidence())
        .BindInt(i->GetFlags())
        .BindInt(function_steps_[i->GetMatchingStep()])
        .BindInt(0)
        .BindInt(i->GetCommentsPorted() ? 1 : 0)
        .BindInt(basic_block_count)
        .BindInt(edge_count)
        .BindInt(instruction_count)
        .Execute()
        .Reset();

    for (auto j = i->GetBasicBlockFixedPoints().cbegin(),
              jend = i->GetBasicBlockFixedPoints().cend();
         j != jend; ++j, ++basic_block_id) {
      basic_block_match_statement
          .BindInt(basic_block_id)
          .BindInt(function_id)
          .BindInt64(primary.GetAddress(j->GetPrimaryVertex()))
          .BindInt64(secondary.GetAddress(j->GetSecondaryVertex()))
          .BindInt(basic_block_steps_[j->GetMatchingStep()])
          .BindInt(0)
          .Execute()
          .Reset();

      for (auto k = j->GetInstructionMatches().cbegin(),
                kend = j->GetInstructionMatches().cend();
           k != kend; ++k) {
        instruction_statement
          .BindInt(basic_block_id)
          .BindInt64(k->first->GetAddress())
          .BindInt64(k->second->GetAddress())
          .Execute()
          .Reset();
      }
    }
  }
}

void DatabaseWriter::WriteAlgorithms() {
  if (!basic_block_steps_.empty()) {
    return;  // Assume we have already done this step.
  }

  {
    const MatchingStepsFlowGraph& steps = GetDefaultMatchingStepsBasicBlock();
    int id = 0;
    for (auto i = steps.cbegin(), end = steps.cend(); i != end; ++i) {
      basic_block_steps_[(*i)->GetName()] = ++id;
      database_.Statement(
          "insert into basicblockalgorithm values (:id, :name)")
          ->BindInt(id)
          .BindText((*i)->GetName().c_str())
          .Execute();
    }
    basic_block_steps_["basicBlock: propagation (size==1)"] = ++id;
    database_.Statement(
        "insert into basicblockalgorithm values (:id, :name)")
        ->BindInt(id)
        .BindText("basicBlock: propagation (size==1)")
        .Execute();

    basic_block_steps_["basicblock: manual"] = ++id;
    database_.Statement(
        "insert into basicblockalgorithm values (:id, :name)")
        ->BindInt(id)
        .BindText("basicblock: manual")
        .Execute();
  }

  {
    const MatchingSteps& steps = GetDefaultMatchingSteps();
    int id = 0;
    for (auto i = steps.cbegin(), end = steps.cend(); i != end; ++i) {
      function_steps_[(*i)->GetName()] = ++id;
      database_.Statement(
          "insert into functionalgorithm values (:id, :name)")
          ->BindInt(id)
          .BindText((*i)->GetName().c_str()).Execute();
    }
    function_steps_["function: call reference matching"] = ++id;
    database_.Statement(
        "insert into functionalgorithm values (:id, :name)")
        ->BindInt(id)
        .BindText("function: call reference matching")
        .Execute();

    function_steps_["function: manual"] = ++id;
    database_.Statement(
        "insert into functionalgorithm values (:id, :name)")
        ->BindInt(id)
        .BindText("function: manual")
        .Execute();
  }
}

void DatabaseWriter::Write(const CallGraph& call_graph1,
                           const CallGraph& call_graph2,
                           const FlowGraphs& flow_graphs1,
                           const FlowGraphs& flow_graphs2,
                           const FixedPoints& fixed_points) {
  try {
    database_.Begin();
    WriteMetaData(call_graph1, call_graph2, flow_graphs1, flow_graphs2,
                  fixed_points);
    WriteAlgorithms();
    WriteMatches(fixed_points);

    database_.Commit();
  } catch(...) {
    database_.Rollback();
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

void DatabaseTransmuter::DeleteMatches(const TempFixedPoints& kill_me) {
  for (auto i = kill_me.cbegin(), end = kill_me.cend(); i != end; ++i) {
    const Address primary_address = i->first;
    const Address secondary_address = i->second;
    database_.Statement(
        "delete from instruction where basicblockid in ( select b.id from "
        "function as f inner join basicblock as b on b.functionid = f.id "
        "where f.address1 = :address1 and f.address2 = :address2 )")
        ->BindInt64(primary_address)
        .BindInt64(secondary_address)
        .Execute();

    database_.Statement(
        "delete from basicblock where functionid in ( select f.id from "
        "\"function\" as f where f.address1 = :address1 and f.address2 = "
        ":address2 )")
        ->BindInt64(primary_address)
        .BindInt64(secondary_address)
        .Execute();

    database_.Statement(
        "delete from \"function\" where address1 = :address1 and address2 = "
        ":address2")
        ->BindInt64(primary_address)
        .BindInt64(secondary_address)
        .Execute();
  }
}

string DatabaseTransmuter::GetTempFile() {
  return JoinPath(GetTempDirectory("BinDiff", /* create = */ true),
                  "temporary.database");
}

void DatabaseTransmuter::DeleteTempFile() {
  try {
    remove(GetTempFile().c_str());
  } catch(...) {
  }
}

void DatabaseTransmuter::MarkPortedComments(
    SqliteDatabase* database, const char* temp_database,
    const FixedPointInfos& /* fixed_points */) {
  database->Statement("attach :filename as ported")->BindText(temp_database)
                      .Execute();

  database->Statement("update function set commentsported = exists "
      "(select * from ported.commentsported where address = address1)"
      " where commentsported = 0")
      ->Execute();
}

void DatabaseTransmuter::Write(const CallGraph& /*call_graph1*/,
                               const CallGraph& /*call_graph2*/,
                               const FlowGraphs& /*flow_graphs1*/,
                               const FlowGraphs& /*flow_graphs2*/,
                               const FixedPoints& /*fixed_points*/) {
  // step 1: Remove deleted matches.
  TempFixedPoints current_fixed_points;

  {
    SqliteStatement statement(&database_,
                              "select address1, address2 from \"function\"");
    statement.Execute();
    for (; statement.GotData(); statement.Execute()) {
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
  DeleteMatches(kill_me);

  // step 2: Merge new matches from temp database.
  const string temp_dir(GetTempFile());
  if (FileExists(temp_dir)) {
    database_.Statement(
        "attach :filename as newMatches")
        ->BindText(temp_dir.c_str())
        .Execute();
    int function_id = 0, basic_block_id = 0;
    database_.Statement(
        "select coalesce( max( id ), 0 ) from \"function\"")
        ->Execute()
        .Into(&function_id);
    database_.Statement(
        "select coalesce( max( id ), 0 ) from \"basicblock\"")
        ->Execute()
        .Into(&basic_block_id);
    database_.Statement(
        "insert into \"function\" select id + :id, "
        "address1, address2, similarity, confidence, flags, algorithm, "
        "evaluate, commentsported, basicblocks, edges, instructions from "
        "newMatches.\"function\"")
        ->BindInt(function_id)
        .Execute();
    database_.Statement(
        "insert into basicblock select id + :id, functionId + :fid, address1, "
        "address2, algorithm, evaluate from newMatches.basicblock")
        ->BindInt(basic_block_id)
        .BindInt(function_id)
        .Execute();
    database_.Statement(
        "insert into instruction select basicblockid + :id, address1, address2 "
        "from newMatches.instruction")
        ->BindInt(basic_block_id)
        .Execute();
  }

  // step 3: Update changed matches (user set algorithm type to "manual").
  int algorithm = 0;
  database_.Statement("select max(id) from functionalgorithm")
      ->Execute().Into(&algorithm);
  SqliteStatement statement(&database_,
      "update \"function\" set confidence=1.0, algorithm=:algorithm "
      "where address1=:address1 and address2=:address2");
  for (auto i = fixed_point_infos_.cbegin(), end = fixed_point_infos_.cend();
       i != end; ++i) {
    if (!i->IsManual())
      continue;
    statement
        .BindInt(algorithm)
        .BindInt64(i->primary)
        .BindInt64(i->secondary)
        .Execute()
        .Reset();
  }

  // step 4: Update last changed timestamp.
  database_.Statement("update \"metadata\" set modified=DATETIME('NOW')")
      ->Execute();
}

DatabaseReader::DatabaseReader(SqliteDatabase& database,
                               const string& filename,
                               const string& temp_dir)
    : database_(database),
      input_filename_(filename),
      primary_filename_(),
      secondary_filename_(),
      path_(filename),
      temporary_directory_(temp_dir),
      basic_block_fixed_point_info_() {
  std::replace(path_.begin(), path_.end(), '\\', '/');
  const string::size_type pos = path_.rfind('/');
  if (pos != string::npos) {
    path_ = path_.substr(0, pos + 1);
  }
}

string DatabaseReader::GetInputFilename() const {
  return input_filename_;
}

string DatabaseReader::GetPrimaryFilename() const {
  return primary_filename_;
}

string DatabaseReader::GetSecondaryFilename() const {
  return secondary_filename_;
}

const Counts& DatabaseReader::GetBasicBlockFixedPointInfo() const {
  return basic_block_fixed_point_info_;
}

void DatabaseReader::ReadFullMatches(SqliteDatabase* database,
                                     CallGraph* call_graph1,
                                     CallGraph* call_graph2,
                                     FlowGraphs* /*flow_graphs1*/,
                                     FlowGraphs* /*flow_graphs2*/,
                                     FixedPoints* fixed_points) {
  SqliteStatement statement(database,
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
  for (statement.Execute(); statement.GotData(); statement.Execute()) {
    FixedPoint* fixed_point = 0;
    Address function1 = 0;
    Address function2 = 0;
    Address basic_block1 = 0;
    Address basic_block2 = 0;
    string function_algorithm;
    string basic_block_algorithm;
    double similarity = 0.0;
    double confidence = 0.0;
    bool basic_block_is_null = false;
    statement
        .Into(&function1)
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
      // TODO(cblichmann): b/35456354: This truncates all 64-bit addresses.
      //                   FlowGraph::Graph should really use 64-bit
      //                   vertices/edges. We cannot just fix it without proper
      //                   testing in place, though. Also, almost all "normal"
      //                   64-bit binaries will still work fine, as long as
      //                   there is less than 4G of code/address space used.
      fixed_point->Add(basic_block1, basic_block2, basic_block_algorithm);
    }
  }
}

void DatabaseReader::Read(CallGraph& call_graph1, CallGraph& call_graph2,
                          FlowGraphInfos& flow_graphs1,
                          FlowGraphInfos& flow_graphs2,
                          FixedPointInfos& fixed_points) {
  database_.Statement(
      "select file1.filename as filename1, file2.filename as filename2, "
      "similarity, confidence from metadata "
      "inner join file as file1 on file1.id = file1 "
      "inner join file as file2 on file2.id = file2;")
      ->Execute()
      .Into(&primary_filename_)
      .Into(&secondary_filename_)
      .Into(&similarity_)
      .Into(&confidence_);

  {  // function matches
    SqliteStatement statement(&database_,
        "select address1, address2, similarity, confidence, flags, a.name, "
        "evaluate, commentsported, basicblocks, edges, instructions "
        "from \"function\" as f "
        "inner join functionalgorithm as a on a.id = f.algorithm");
    statement.Execute();
    for (; statement.GotData(); statement.Execute()) {
      string algorithm;
      FixedPointInfo fixed_point;
      int evaluate = 0, comments_ported = 0;
      statement
          .Into(&fixed_point.primary)
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
      fixed_points.insert(fixed_point);
    }
  }

  {  // basic block matches
    SqliteStatement statement(&database_,
        "select a.name, count(*) from basicblock as b inner join "
        "basicblockalgorithm as a on a.id = b.algorithm group by b.algorithm");
    statement.Execute();
    for (; statement.GotData(); statement.Execute()) {
      string algorithm_name;
      int count = 0;
      statement.Into(&algorithm_name).Into(&count);
      basic_block_fixed_point_info_[algorithm_name] = count;
    }
  }
  ReadInfos((path_ + (primary_filename_ + ".BinExport")),
            call_graph1, flow_graphs1);
  ReadInfos((path_ + (secondary_filename_ + ".BinExport")),
            call_graph2, flow_graphs2);
}
