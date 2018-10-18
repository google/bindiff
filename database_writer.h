// Copyright 2011-2018 Google LLC. All Rights Reserved.
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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_DATABASE_WRITER_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_DATABASE_WRITER_H_

#include "third_party/zynamics/binexport/address_references.h"
#include "third_party/zynamics/binexport/operand.h"
#include "third_party/zynamics/binexport/postgresql.h"
#include "third_party/zynamics/binexport/writer.h"

class FlowGraph;
class CallGraph;

class DatabaseWriter : public Writer {
 public:
  enum InternalStatement {
    INIT_TABLES,
    INIT_CONSTRAINTS,
    INIT_INDICES,
    MAINTENANCE,
  };

  using AddressSpaceIds = std::map<Address, int>;

  DatabaseWriter(const string& schema, const string& module_name, int module_id,
                 const string& md5, const string& sha1,
                 const string& architecture, const Address base_address,
                 const string& program_version,
                 const string& connection_string);
  ~DatabaseWriter();

  util::Status Write(const CallGraph& call_graph, const FlowGraph& flow_graph,
                     const Instructions& instructions,
                     const AddressReferences& address_references,
                     const TypeSystem* type_system,
                     const AddressSpace& address_space) override;

  int query_size() const { return query_size_; }
  void set_query_size(int value) { query_size_ = value; }

 private:
  static const string postgresql_initialize_tables_;
  static const string postgresql_initialize_constraints_;
  static const string postgresql_initialize_indices_;
  static const string postgresql_maintenance_;

  void CreateSchema();
  void CreateModulesTable();
  void PrepareDatabase(const string& md5, const string& sha1,
                       const string& architecture, const Address base_address);
  void ExecuteInternalStatement(InternalStatement id,
                                const string& replacement);

  void InsertAddressComments(const CallGraph& call_graph);
  void InsertFlowGraphs(const CallGraph& call_graph,
                        const FlowGraph& flow_graph,
                        const Instructions& instructions,
                        const TypeSystem* type_system);
  void InsertCallGraph(const CallGraph& call_graph);
  void InsertExpressionTree();
  void InsertExpressions();
  void InsertExpressionSubstitutions(
      const FlowGraph& flow_graph, const Instructions& instructions,
      const AddressReferences& address_references);
  void InsertTypes(const TypeSystem& type_system,
                   const AddressSpaceIds& address_space_ids);
  void InsertSections(const AddressSpace& address_space,
                      AddressSpaceIds* address_space_ids);

  Database database_;
  int query_size_;
  int module_id_;
  string module_name_;
  string schema_;
  string program_version_;
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_DATABASE_WRITER_H_
