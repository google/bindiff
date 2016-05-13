// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

#include "third_party/zynamics/binexport/database_writer.h"

#include <cassert>
#include <cinttypes>
#include <fstream>
#include <stdexcept>
#include <string>

#include <boost/algorithm/string.hpp>
#include <boost/tokenizer.hpp>

#include "base/logging.h"
#include "base/stringprintf.h"
#include "third_party/zynamics/binexport/base_types.h"
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/flow_graph.h"
#include "third_party/zynamics/binexport/hex_codec.h"
#include "third_party/zynamics/binexport/initialize_constraints_postgresql_sql.h"
#include "third_party/zynamics/binexport/initialize_indices_postgresql_sql.h"
#include "third_party/zynamics/binexport/initialize_tables_postgresql_sql.h"
#include "third_party/zynamics/binexport/maintenance_postgresql_sql.h"
#include "third_party/zynamics/binexport/query_builder.h"
#include "third_party/zynamics/binexport/type_system.h"
#include "third_party/zynamics/binexport/types_container.h"

namespace {

std::string EncodeHash(const std::string& hash) {
  if (hash.size() <= 20) {
    return EncodeHex(hash);
  }
  return hash;
}

// Creates a string representation for the base type id of the corresponding
// stack frame of the given function. Returns "null" if no such stack frame
// exists.
std::string GetFrameTypeId(const TypeSystem* type_system,
                           const Function& function) {
  if (type_system) {
    const BaseType* stack_frame = type_system->GetStackFrame(function);
    if (stack_frame) {
      return std::to_string(stack_frame->GetId());
    }
  }
  return "null";
}

// Creates a string representation for the base type id of the corresponding
// function prototype of the given function. Returns "null" if no such
// function prototype exists.
std::string GetFunctionPrototypeId(const TypeSystem* type_system,
                                   const Function& function) {
  if (type_system) {
    const BaseType* function_prototype =
        type_system->GetFunctionPrototype(function);
    if (function_prototype) {
      return std::to_string(function_prototype->GetId());
    }
  }
  return "null";
}

// Creates a string representation of the member path for the "path" column in
// the expression_types table.
void BuildMemberPath(const BaseType::MemberIds& member_ids,
                     QueryBuilder* builder) {
  if (member_ids.empty()) {
    *builder << "'{}'";
  } else {
    *builder << "'{ ";
    for (int i = 0; i < member_ids.size(); ++i) {
      *builder << member_ids[i];
      if (i < member_ids.size() - 1) {
        *builder << ", ";
      }
    }
    *builder << " }'";
  }
}

}  // namespace

DatabaseWriter::DatabaseWriter(const std::string& schema,
                               const std::string& module_name, int module_id,
                               const std::string& md5, const std::string& sha1,
                               const std::string& architecture,
                               const Address base_address,
                               const std::string& program_version,
                               const std::string& connection_string)
    : database_(connection_string.c_str()),
      query_size_(32 << 20 /* 32 MiB */),
      module_id_(module_id),
      module_name_(module_name),
      schema_(database_.EscapeIdentifier(schema)),
      program_version_(program_version) {
  CreateSchema();
  CreateModulesTable();
  Transaction transaction(&database_);
  database_.Execute(R"(LOCK TABLE "modules" IN ACCESS EXCLUSIVE MODE)");
  if (module_id_ <= 0) {
    try {
      database_.Execute(
          R"(SELECT COALESCE(MAX(id), 0) + 1 FROM modules)") >>
          module_id_;
    } catch (...) {
      module_id_ = 1;  // module table doesn't exist yet
    }
  }
  PrepareDatabase(md5, sha1, architecture, base_address);
}

DatabaseWriter::~DatabaseWriter() = default;

void DatabaseWriter::ExecuteInternalStatement(InternalStatement id,
                                              const std::string& replacement) {
  const std::string* stream = 0;
  switch (id) {
    case INIT_TABLES:
      stream = &postgresql_initialize_tables_;
      break;
    case INIT_CONSTRAINTS:
      stream = &postgresql_initialize_constraints_;
      break;
    case INIT_INDICES:
      stream = &postgresql_initialize_indices_;
      break;
    case MAINTENANCE:
      stream = &postgresql_maintenance_;
      break;
  }

  if (!stream) {
    return;
  }

  boost::tokenizer<boost::char_separator<char>> tokenizer(
      *stream, boost::char_separator<char>(";"));
  for (auto query : tokenizer) {
    boost::algorithm::trim(query);
    if (!query.empty()) {
      boost::replace_all(query, "?", replacement);
      database_.Execute(query.c_str());
    }
  }
}

void DatabaseWriter::CreateSchema() {
  try {
    database_.Execute(("CREATE SCHEMA " + schema_ + "").c_str());
  } catch (...) {
    // We assume this failed because the schema already exists (which is OK)
    // thus we ignore the error.
  }
  database_.Execute(("SET SEARCH_PATH TO " + schema_ + "").c_str());
}

void DatabaseWriter::CreateModulesTable() {
  try {
    database_.Execute(
        "CREATE TABLE \"modules\" ("
        "\"id\" SERIAL, "
        "\"name\" TEXT NOT NULL, "
        "\"architecture\" VARCHAR(32) NOT NULL, "
        "\"base_address\" BIGINT NOT NULL, "
        "\"exporter\" VARCHAR(256) NOT NULL, "
        "\"version\" INT NOT NULL, "
        "\"md5\" CHAR(32) NOT NULL, "
        "\"sha1\" CHAR(40) NOT NULL, "
        "\"comment\" TEXT, "
        "\"import_time\" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
        "primary key (\"id\"));");
  } catch (...) {
    // We assume the error is because the modules table already existed.
    // PostgreSQL doesn't support "if exists" for table creation and querying
    // the information_schema for our table name doesn't work either because we
    // don't know what schema we are in.
  }
}

void DatabaseWriter::PrepareDatabase(const std::string& md5,
                                     const std::string& sha1,
                                     const std::string& architecture,
                                     const Address base_address) {
  enum { kDbVersion = 7 };

  try {
    int num_modules = 0;
    database_.Execute("SELECT COUNT(*) FROM MODULES") >> num_modules;
    if (num_modules != 0) {
      int versionDelta = 0;
      database_.Execute("SELECT MAX(version) - MIN(version) FROM modules") >>
          versionDelta;
      if (versionDelta != 0) {
        throw std::runtime_error("Version error - check modules table");
      }

      int version = 0;
      database_.Execute("SELECT VERSION FROM MODULES LIMIT 1") >> version;
      if (version != kDbVersion) {
        throw std::runtime_error("Version error - check modules table");
      }
    }
  } catch (...) {
    throw std::runtime_error("Version error - check modules table");
  }

  int num_modules = 0;
  database_.Execute("SELECT COUNT(*) FROM modules WHERE id = $1::int LIMIT 1",
                    Parameters() << module_id_) >>
      num_modules;
  if (num_modules > 0) {
    // TODO(user): Danger Will Robinson! Is this really what we want?
    database_.Execute("DELETE FROM modules WHERE id = $1::int",
                      Parameters() << module_id_);
  }

  boost::replace_all(module_name_, "\\", "/");
  const std::string::size_type pos = module_name_.rfind("/");
  const std::string module_name =
      pos != std::string::npos ? module_name_.substr(pos + 1) : module_name_;

  database_.Execute(
      "INSERT INTO MODULES VALUES($1::int, $2::text, $3::varchar, $4::bigint, "
      "$5::varchar, $6::int, $7::varchar, $8::varchar, '', NOW())",
      Parameters() << static_cast<int32_t>(module_id_) << module_name
                   << architecture << static_cast<int64_t>(base_address)
                   << program_version_ << static_cast<int32_t>(kDbVersion)
                   << EncodeHash(md5) << EncodeHash(sha1));
}

void DatabaseWriter::InsertAddressComments(const CallGraph& call_graph) {
  std::ostringstream query;
  query << "INSERT INTO \"ex_" << module_id_ << "_address_comments\" VALUES ";
  QueryBuilder address_comments_query(&database_, query.str(), query_size_);
  Address last_address = std::numeric_limits<Address>::max();
  for (const Comment& comment : call_graph.GetComments()) {
    if (comment.type_ != Comment::REGULAR || last_address == comment.address_) {
      continue;
    }
    std::string comment_string;
    for (auto comment_char : *comment.comment_) {
      comment_string += (isascii(comment_char) ? comment_char : '?');
    }
    address_comments_query << "(" << static_cast<int64_t>(comment.address_)
                           << "," << database_.EscapeLiteral(comment_string)
                           << ")," << kFlushQuery;
    last_address = comment.address_;
  }
  address_comments_query.Execute();
}

void DatabaseWriter::InsertFlowGraphs(const CallGraph& call_graph,
                                      const FlowGraph& flow_graph,
                                      const Instructions& const_instructions,
                                      const TypeSystem* type_system) {
  // Reset exported flag.
  // For the caller a call to this function is actually non-mutating
  // we just store temporary state in instructions.
  Instructions& instructions = const_cast<Instructions&>(const_instructions);
  for (auto& instruction : instructions) {
    instruction.SetExported(false);
  }

  std::ostringstream query;
  query << "INSERT INTO \"ex_" << module_id_ << "_functions\" VALUES ";
  QueryBuilder function_query_builder(&database_, query.str(), query_size_);

  query.str("");
  query << "INSERT INTO \"ex_" << module_id_ << "_basic_blocks\" VALUES ";
  QueryBuilder basic_block_query(&database_, query.str(), query_size_);
  int basic_block_id = 0;

  query.str("");
  query << "INSERT INTO \"ex_" << module_id_
        << "_basic_block_instructions\" VALUES ";
  QueryBuilder basic_block_instructions_query(&database_, query.str(),
                                              query_size_);

  query.str("");
  query << "INSERT INTO \"ex_" << module_id_
        << "_control_flow_graphs\" (\"parent_function\", \"source\", "
           "\"destination\", \"type\") VALUes ";
  QueryBuilder flow_graph_query(&database_, query.str(), query_size_);

  query.str("");
  query << "INSERT INTO \"ex_" << module_id_ << "_instructions\" VALUES ";
  QueryBuilder instruction_query(&database_, query.str(), query_size_);

  query.str("");
  query << "INSERT INTO \"ex_" << module_id_ << "_operands\" VALUES ";
  QueryBuilder operands_query(&database_, query.str(), query_size_);

  for (const auto& function_entry : flow_graph.GetFunctions()) {
    // Store functions.
    const Function& function = *function_entry.second;
    std::string mangled_name(function.GetName(Function::MANGLED));
    std::string demangled_name(function.GetName(Function::DEMANGLED));
    std::string module_name(function.IsImported() ? function.GetModuleName()
                                                  : module_name_);
    function_query_builder << "("
                           << static_cast<int64_t>(function.GetEntryPoint())
                           << "," << database_.EscapeLiteral(mangled_name)
                           << ","
                           << (demangled_name != mangled_name
                                   ? database_.EscapeLiteral(demangled_name)
                                   : "null")
                           << "," << (function.HasRealName() ? "true" : "false")
                           << ", " << function.GetType(false) << ","
                           << database_.EscapeLiteral(module_name) << ","
                           << GetFrameTypeId(type_system, function) << ","
                           << GetFunctionPrototypeId(type_system, function)
                           << ")," << kFlushQuery;

    for (const auto& basic_block_ptr : function.GetBasicBlocks()) {
      // Store basic blocks.
      BasicBlock& basic_block = *basic_block_ptr;
      basic_block.set_id(++basic_block_id);
      basic_block_query << "(" << basic_block.id() << ","
                        << static_cast<int64_t>(function.GetEntryPoint())
                        << ", " << basic_block.GetEntryPoint() << "),"
                        << kFlushQuery;

      int instruction_sequence = 0;
      for (auto& instruction : basic_block) {
        // Store instructions.
        basic_block_instructions_query
            << "(" << basic_block.id() << ","
            << static_cast<int64_t>(instruction.GetAddress()) << ","
            << instruction_sequence << ")," << kFlushQuery;

        // This is inefficient and ugly but has to be done because shared basic
        // blocks will get their id reassigned later on. The call graph edge
        // needs the original id however.
        CallGraph::Edges& edges = const_cast<CallGraph&>(call_graph).GetEdges();
        for (auto i = std::lower_bound(
                 edges.begin(), edges.end(),
                 EdgeInfo(function_entry.second, instruction.GetAddress(), 0));
             i != edges.end() && i->source_ == instruction.GetAddress(); ++i) {
          EdgeInfo& edge = const_cast<EdgeInfo&>(*i);
          edge.source_basic_block_id_ = basic_block.id();
        }

        if (!instruction.IsExported()) {
          instruction.SetExported(true);
          const std::string mnemonic(instruction.GetMnemonic());
          instruction_query
              << "(" << static_cast<int64_t>(instruction.GetAddress()) << ","
              << database_.EscapeLiteral(mnemonic) << ",decode('"
              << EncodeHex(instruction.GetBytes()) << "','hex')"
              << ")," << kFlushQuery;

          int operand_sequence = 0;
          for (const auto* operand : instruction) {
            operands_query << "("
                           << static_cast<int64_t>(instruction.GetAddress())
                           << "," << operand->GetId() << "," << operand_sequence
                           << ")," << kFlushQuery;
            ++operand_sequence;
          }
        }
        ++instruction_sequence;
      }
    }

    // Store flow graph edges.
    for (const auto& edge : function.GetEdges()) {
      const BasicBlock* source = function.GetBasicBlockForAddress(edge.source);
      const BasicBlock* target = function.GetBasicBlockForAddress(edge.target);
      if (source == 0 || target == 0) {
        assert(false && "corrupted input data");
        continue;  // BUG: Should not happen.
      }
      flow_graph_query << "(" << static_cast<int64_t>(function.GetEntryPoint())
                       << "," << source->id() << "," << target->id() << ","
                       << (edge.type - 1) << ")," << kFlushQuery;
    }
  }
  operands_query.Execute();
  instruction_query.Execute();
  function_query_builder.Execute();
  basic_block_query.Execute();
  basic_block_instructions_query.Execute();
  flow_graph_query.Execute();

  for (auto& instruction : instructions) {
    instruction.SetExported(false);
  }
}

void DatabaseWriter::InsertCallGraph(const CallGraph& call_graph) {
  if (call_graph.GetEdges().empty()) {
    return;
  }

  // Store call graph edges.
  std::ostringstream query;
  query << "insert into \"ex_" << module_id_
        << "_callgraph\" (\"source\", \"source_basic_block_id\", "
           "\"source_address\", \"destination\") values ";
  QueryBuilder call_graph_query(&database_, query.str(), query_size_);
  for (const auto& edge : call_graph.GetEdges()) {
    if (edge.source_basic_block_id_ < 0) {
      LOG(INFO) << StringPrintf(
          "Warning: skipping broken call graph edge %08" PRIx64
          " -> %08" PRIx64,
          edge.source_, edge.target_);
      continue;
    }
    call_graph_query << "("
                     << static_cast<int64_t>(edge.function_->GetEntryPoint())
                     << "," << edge.source_basic_block_id_ << ","
                     << static_cast<int64_t>(edge.source_) << ","
                     << static_cast<int64_t>(edge.target_) << "),"
                     << kFlushQuery;
  }
  call_graph_query.Execute();
}

void DatabaseWriter::InsertExpressionTree() {
  std::ostringstream query;
  query << "INSERT INTO \"ex_" << module_id_ << "_expression_nodes\" VALUES ";
  QueryBuilder expression_tree_query(&database_, query.str(), query_size_);
  for (const auto& element : Expression::GetExpressions()) {
    const Expression& expression = element.second;
    std::string name(expression.GetSymbol());
    assert(!expression.GetSymbol().empty() || expression.IsImmediate());
    expression_tree_query << "(" << expression.GetId() << ","
                          << (expression.GetType() <=
                                      Expression::TYPE_DEREFERENCE
                                  ? expression.GetType()
                                  : Expression::TYPE_IMMEDIATE_INT)
                          << ","
                          << (!expression.GetSymbol().empty()
                                  ? database_.EscapeLiteral(name.substr(0, 255))
                                  : "NULL")
                          << ","
                          << (expression.IsImmediate()
                                  ? std::to_string(expression.GetImmediate())
                                  : "NULL")
                          << "," << expression.GetPosition() << ",";
    if (expression.GetParent() != 0) {
      expression_tree_query << expression.GetParent()->GetId();
    } else {
      expression_tree_query << "NULL";
    }
    expression_tree_query << ")," << kFlushQuery;
  }
  expression_tree_query.Execute();
}

// Write operand expressions nodes, expression tree ids, and
// expression_tree_nodes.
void DatabaseWriter::InsertExpressions() {
  {  // expression_trees
    std::ostringstream query;
    query << "insert into \"ex_" << module_id_ << "_expression_trees\" values ";
    QueryBuilder expression_tree_query(&database_, query.str(), query_size_);
    for (const auto& element : Operand::GetOperands()) {
      const Operand& operand = element.second;
      expression_tree_query << "(" << operand.GetId() << ")," << kFlushQuery;
    }
    expression_tree_query.Execute();
  }

  {  // expression_tree_nodes
    std::ostringstream query;
    query << "insert into \"ex_" << module_id_
          << "_expression_tree_nodes\" values ";
    QueryBuilder expression_tree_query(&database_, query.str(), query_size_);
    for (const auto& operand : Operand::GetOperands()) {
      for (const auto* expression : operand.second) {
        expression_tree_query << "(" << operand.second.GetId() << ","
                              << expression->GetId() << ")," << kFlushQuery;
      }
    }
    expression_tree_query.Execute();
  }
}

void DatabaseWriter::InsertTypes(const TypeSystem& type_system,
                                 const AddressSpaceIds& address_space_ids) {
  const TypesContainer& types = type_system.GetTypes();
  // First batch: all the base types.
  std::ostringstream base_type_query;
  base_type_query << "insert into \"ex_" << module_id_
                  << "_base_types\" (\"id\", \"name\", \"size\", \"pointer\", "
                  << "\"signed\", \"category\") values ";
  QueryBuilder base_types_builder(&database_, base_type_query.str(),
                                  query_size_);
  for (const auto* base_type : types.GetBaseTypes()) {
    base_types_builder
        << "(" << base_type->GetId() << ","
        << database_.EscapeLiteral(base_type->GetName()) << ","
        << base_type->GetSize() << ","
        << (base_type->GetPointer() == nullptr
                ? "null"
                : std::to_string(base_type->GetPointer()->GetId()))
        << "," << (base_type->IsSigned() ? "true" : "false") << ","
        << database_.EscapeLiteral(base_type->GetCategoryString()) << "),"
        << kFlushQuery;
  }

  base_types_builder.Execute();

  // Second batch: all the members.
  std::ostringstream compound_type_query;
  compound_type_query
      << "insert into \"ex_" << module_id_
      << "_types\" (\"id\", \"name\", \"base_type\", \"parent_id\", "
      << "\"offset\", \"argument\", \"number_of_elements\") values ";
  QueryBuilder compound_types_builder(&database_, compound_type_query.str(),
                                      query_size_);
  for (const auto* type_member : types.GetMemberTypes()) {
    compound_types_builder
        << "(" << type_member->id << ","
        << database_.EscapeLiteral(type_member->name) << ","
        << type_member->type->GetId() << ","
        << (type_member->parent_type == nullptr
                ? "null"
                : std::to_string(type_member->parent_type->GetId()))
        << "," << (type_member->offset == MemberType::DB_NULL_VALUE
                       ? "null"
                       : std::to_string(type_member->offset))
        << "," << (type_member->argument == MemberType::DB_NULL_VALUE
                       ? "null"
                       : std::to_string(type_member->argument))
        << "," << (type_member->num_elements == MemberType::DB_NULL_VALUE
                       ? "null"
                       : std::to_string(type_member->num_elements))
        << ")," << kFlushQuery;
  }
  compound_types_builder.Execute();

  // Third batch: all expression substitutions for types.
  std::ostringstream expr_subst_type_query;
  expr_subst_type_query
      << "insert into \"ex_" << module_id_
      << "_expression_types\" (\"address\", \"position\", \"expression_id\", "
      << "\"type\", \"path\", \"offset\") values ";
  QueryBuilder expr_subst_type_builder(&database_, expr_subst_type_query.str(),
                                       query_size_);
  for (const auto& type_substitution : type_system.GetTypeSubstitutions()) {
    expr_subst_type_builder << "(" << type_substitution.address << ","
                            << type_substitution.operand_num << ","
                            << type_substitution.expression_id << ","
                            << type_substitution.base_type_id << ",";
    BuildMemberPath(type_substitution.member_path, &expr_subst_type_builder);
    expr_subst_type_builder << "," << type_substitution.offset << "),"
                            << kFlushQuery;
  }
  expr_subst_type_builder.Execute();

  // Fourth batch: type instances.
  std::ostringstream type_instances_query;
  type_instances_query
      << "insert into \"ex_" << module_id_
      << "_type_instances\" (\"id\", \"section_offset\", \"type_id\", "
         "\"section_id\", \"name\") values ";
  QueryBuilder instances_builder(&database_, type_instances_query.str(),
                                 query_size_);
  for (const auto& type_instance : type_system.GetTypeInstances()) {
    auto it = address_space_ids.find(type_instance.segment_address);
    int address_space_id = 0;
    if (it != address_space_ids.end()) {
      address_space_id = it->second;
    }
    instances_builder << "(" << type_instance.database_id << ","
                      << type_instance.section_offset << ","
                      << type_instance.base_type->GetId() << ","
                      << address_space_id << ","
                      << database_.EscapeLiteral(type_instance.name) << "),"
                      << kFlushQuery;
  }
  instances_builder.Execute();

  // Fifth batch: data x-refs.
  std::ostringstream data_xrefs_query;
  data_xrefs_query
      << "insert into \"ex_" << module_id_
      << "_expression_type_instances\" (\"address\", \"position\", "
      << "\"expression_node_id\", \"type_instance_id\") values ";
  QueryBuilder xrefs_builder(&database_, data_xrefs_query.str(), query_size_);
  for (const auto& data_xref : type_system.GetDataXRefs()) {
    xrefs_builder << "(" << data_xref.address << "," << data_xref.operand_num
                  << "," << data_xref.expression_id << ","
                  << data_xref.type_instance->database_id << "),"
                  << kFlushQuery;
  }
  xrefs_builder.Execute();
}

void DatabaseWriter::InsertExpressionSubstitutions(
    const FlowGraph& flow_graph, const Instructions& const_instructions,
    const AddressReferences& address_references) {
  // For the caller a call to this function is actually non-mutating
  // we just store temporary state in instructions.
  Instructions* instructions = const_cast<Instructions*>(&const_instructions);
  for (auto& i : *instructions) {
    i.SetExported(false);
  }

  // Insert expression substitutions. note that the done set is necessary for
  // shared basic blocks.
  std::ostringstream query;
  query << "insert into \"ex_" << module_id_
        << "_expression_substitutions\" (\"address\", \"position\", "
           "\"expression_node_id\", \"replacement\") values ";
  QueryBuilder query_builder(&database_, query.str(), query_size_);
  for (const auto& substitution : flow_graph.GetSubstitutions()) {
    std::string replacement = *substitution.second;
    query_builder << "(" << static_cast<int64_t>(
                                std::get<0 /* Address */>(substitution.first))
                  << "," << std::get<1 /* Operand No */>(substitution.first)
                  << "," << std::get<2 /* Expression Id */>(substitution.first)
                  << "," << database_.EscapeLiteral(replacement) << "),"
                  << kFlushQuery;
    GetInstruction(instructions, std::get<0>(substitution.first))
        ->SetExported(true);
  }

  for (const auto& i : flow_graph.GetFunctions()) {
    const Function& func = *i.second;
    for (auto* basic_block : func.GetBasicBlocks()) {
      for (auto& instruction : *basic_block) {
        if (instruction.IsExported()) continue;
        instruction.SetExported(true);
        int operand_num = -1;
        for (const auto* operand : instruction) {
          for (const auto* expression : *operand) {
            if (!expression->GetParent()) {
              ++operand_num;
            }
            if (expression->IsImmediate() && !expression->GetSymbol().empty()) {
              std::string replacement = expression->GetSymbol();
              query_builder
                  << "(" << static_cast<int64_t>(instruction.GetAddress())
                  << "," << operand_num << "," << expression->GetId() << ","
                  << database_.EscapeLiteral(replacement) << "),"
                  << kFlushQuery;
            }
            ++operand_num;
          }
        }
      }
    }
  }
  query_builder.Execute();

  {  // address references
    std::ostringstream query;
    query << "insert into \"ex_" << module_id_
          << "_address_references\" values ";
    QueryBuilder address_references_query(&database_, query.str(), query_size_);
    for (const auto& address_reference : address_references) {
      if (!Instruction::IsExported(address_reference.source_)) {
        continue;
      }
      const auto& source_operand =
          address_reference.source_operand_ != -1
              ? std::to_string(address_reference.source_operand_)
              : "null";
      const auto& source_expression =
          address_reference.source_expression_ != -1
              ? std::to_string(address_reference.source_expression_)
              : "null";
      address_references_query
          << "(" << static_cast<int64_t>(address_reference.source_) << ","
          << source_operand << "," << source_expression << ","
          << static_cast<int64_t>(address_reference.target_) << ","
          << address_reference.kind_ << ")," << kFlushQuery;
    }
    address_references_query.Execute();
  }

  // Reset exported flag.
  for (auto& i : *instructions) {
    i.SetExported(false);
  }
}

// Returns a permission string which can be used to store permissions in the
// BinNavi database.
std::string GetPermissionsString(int permission) {
  std::string permission_string;
  if (permission & AddressSpace::kRead) {
    permission_string.append("READ");
  }
  if (permission & AddressSpace::kWrite) {
    if (!permission_string.empty()) {
      permission_string.append("_");
    }
    permission_string.append("WRITE");
  }
  if (permission & AddressSpace::kExecute) {
    if (!permission_string.empty()) {
      permission_string.append("_");
    }
    permission_string.append("EXECUTE");
  }
  if (permission_string.empty()) {
    // BinNavi can't deal with "UNKNOWN", so default to READ_WRITE_EXECUTE.
    return "READ_WRITE_EXECUTE";
  }
  return permission_string;
}

void DatabaseWriter::InsertSections(const AddressSpace& address_space,
                                    AddressSpaceIds* address_space_ids) {
  std::ostringstream query;
  query << "INSERT INTO \"ex_" << module_id_ << "_sections\" ("
        << "\"id\", \"name\", \"start_address\", \"end_address\", "
        << "\"permission\", \"data\") VALUES ("
        << "$1::integer, $2::text, $3::bigint, $4::bigint, "
        << "$5::ex_" << module_id_ << "_section_permission_type, $6::bytea)";
  database_.Prepare(query.str().c_str());
  const std::string empty_section_name;
  int id = 0;
  for (const auto& data : address_space.data()) {
    database_.ExecutePrepared(
        Parameters() << id << empty_section_name
                     << static_cast<int64_t>(data.first)
                     << static_cast<int64_t>(data.first + data.second.size())
                     << GetPermissionsString(address_space.GetFlags(data.first))
                     << data.second);
    address_space_ids->insert({data.first, id});
    ++id;
  }
}

// IDA will sometimes produce completely fucked up disassemblies with invalid
// flow graphs.
void CleanUpZombies(Database* database, int module_id_int) {
  const std::string module_id(std::to_string(module_id_int));
  database->Execute(("DELETE FROM ex_" + module_id +
                     "_instructions AS instructions USING ex_" + module_id +
                     "_basic_block_instructions AS basic_block_instructions "
                     "WHERE basic_block_instructions.instruction = "
                     "instructions.address AND basic_block_id IS NULL")
                        .c_str());

  database->Execute(("DELETE FROM ex_" + module_id +
                     "_basic_block_instructions WHERE basic_block_id IS NULL")
                        .c_str());

  database->Execute(
      ("DELETE FROM ex_" + module_id +
       "_address_references WHERE address IN (SELECT address FROM ex_" +
       module_id + "_address_references except SELECT address FROM ex_" +
       module_id + "_instructions)")
          .c_str());

  database->Execute(
      ("DELETE FROM ex_" + module_id +
       "_address_comments WHERE address IN (SELECT address FROM ex_" +
       module_id + "_address_comments except SELECT address FROM ex_" +
       module_id + "_instructions)")
          .c_str());

  database->Execute(
      ("DELETE FROM ex_" + module_id +
       "_expression_substitutions WHERE address IN (SELECT address FROM ex_" +
       module_id + "_expression_substitutions except SELECT address FROM ex_" +
       module_id + "_instructions)")
          .c_str());

  database->Execute(("DELETE FROM ex_" + module_id +
                     "_operands WHERE address IN (SELECT address FROM ex_" +
                     module_id + "_operands except SELECT address FROM ex_" +
                     module_id + "_instructions)")
                        .c_str());

  // Removes type instances that reference non-existing operands.
  database->Execute(
      ("DELETE FROM ex_" + module_id +
       "_expression_type_instances WHERE address IN (SELECT address FROM ex_" +
       module_id + "_expression_type_instances except SELECT address FROM ex_" +
       module_id + "_operands)")
          .c_str());
}

util::Status DatabaseWriter::Write(const CallGraph& call_graph,
                                   const FlowGraph& flow_graph,
                                   const Instructions& instructions,
                                   const AddressReferences& address_references,
                                   const TypeSystem* type_system,
                                   const AddressSpace& address_space) {
  LOG(INFO) << "Writing module: \"" << module_name_
            << "\" to schema: " << schema_ << ", module id: " << module_id_
            << ".";

  const Functions& functions = flow_graph.GetFunctions();
  if (functions.empty()) {
    return util::Status::OK;
  }

  {
    Transaction transaction(&database_);

    ExecuteInternalStatement(INIT_TABLES, std::to_string(module_id_));

    AddressSpaceIds address_space_ids;
    LOG(INFO) << "...sections";
    InsertSections(address_space, &address_space_ids);

    if (type_system) {
      LOG(INFO) << "...types";
      InsertTypes(*type_system, address_space_ids);
    }

    LOG(INFO) << "...address comments";
    InsertAddressComments(call_graph);

    LOG(INFO) << "...flow graphs";
    InsertFlowGraphs(call_graph, flow_graph, instructions, type_system);

    LOG(INFO) << "...call graph";
    InsertCallGraph(call_graph);

    LOG(INFO) << "...expression tree";
    InsertExpressionTree();

    LOG(INFO) << "...operands";
    InsertExpressions();

    LOG(INFO) << "...creating indices";
    ExecuteInternalStatement(INIT_INDICES, std::to_string(module_id_));

    LOG(INFO) << "...expression substitutions";
    InsertExpressionSubstitutions(flow_graph, instructions, address_references);

    CleanUpZombies(&database_, module_id_);

    ExecuteInternalStatement(INIT_CONSTRAINTS, std::to_string(module_id_));
    database_.Execute("DEALLOCATE ALL");  // Release all prepared statements.
  }
  ExecuteInternalStatement(MAINTENANCE, std::to_string(module_id_));
  return util::Status::OK;
}
