// Copyright 2011-2020 Google LLC
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

#ifndef DATABASE_POSTGRESQL_WRITER_H_
#define DATABASE_POSTGRESQL_WRITER_H_

#include "third_party/absl/status/status.h"
#include "third_party/zynamics/binexport/address_references.h"
#include "third_party/zynamics/binexport/database/postgresql.h"
#include "third_party/zynamics/binexport/operand.h"
#include "third_party/zynamics/binexport/writer.h"

class FlowGraph;
class CallGraph;

namespace security::binexport {

class DatabaseWriter : public Writer {
 public:
  enum InternalStatement {
    INIT_TABLES,
    INIT_CONSTRAINTS,
    INIT_INDICES,
    MAINTENANCE,
  };

  using AddressSpaceIds = std::map<Address, int>;

  DatabaseWriter(const std::string& schema, const std::string& module_name,
                 int module_id, const std::string& md5,
                 const std::string& sha256, const std::string& architecture,
                 const Address base_address, const std::string& program_version,
                 const std::string& connection_string);
  ~DatabaseWriter();

  absl::Status Write(const CallGraph& call_graph, const FlowGraph& flow_graph,
                     const Instructions& instructions,
                     const AddressReferences& address_references,
                     const TypeSystem* type_system,
                     const AddressSpace& address_space) override;

  int query_size() const { return query_size_; }
  void set_query_size(int value) { query_size_ = value; }

 private:
  static const std::string postgresql_initialize_tables_;
  static const std::string postgresql_initialize_constraints_;
  static const std::string postgresql_initialize_indices_;
  static const std::string postgresql_maintenance_;

  void CreateSchema();
  void CreateModulesTable();
  void PrepareDatabase(const std::string& md5, const std::string& sha256,
                       const std::string& architecture,
                       const Address base_address);
  void ExecuteInternalStatement(InternalStatement id,
                                const std::string& replacement);

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
  std::string module_name_;
  std::string schema_;
  std::string program_version_;
};

}  // namespace security::binexport

#endif  // DATABASE_POSTGRESQL_WRITER_H_
