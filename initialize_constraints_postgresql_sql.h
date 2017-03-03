// Copyright 2011-2017 Google Inc. All Rights Reserved.
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

// PostgreSQL constraints initialization. This file should only be included from
// database.cc.

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_INITIALIZE_CONSTRAINTS_POSTGRESQL_SQL_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_INITIALIZE_CONSTRAINTS_POSTGRESQL_SQL_H_

#include "third_party/zynamics/binexport/database_writer.h"

const std::string DatabaseWriter::postgresql_initialize_constraints_ = R"raw(
ALTER TABLE "ex_?_functions" ADD PRIMARY KEY ("address");

ALTER TABLE "ex_?_basic_blocks" ADD PRIMARY KEY ("id");
ALTER TABLE "ex_?_basic_blocks"
    ADD CONSTRAINT "ex_?_basic_blocks_parent_function_fkey"
    FOREIGN KEY ("parent_function") REFERENCES "ex_?_functions" ("address")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "ex_?_instructions" ADD PRIMARY KEY ("address");

ALTER TABLE "ex_?_basic_block_instructions"
    ADD CONSTRAINT "ex_?_basic_block_instructions_bb_fkey"
    FOREIGN KEY ("basic_block_id") REFERENCES "ex_?_basic_blocks" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ex_?_basic_block_instructions"
    ADD CONSTRAINT "ex_?_basic_block_instructions_ins_fkey"
    FOREIGN KEY ("instruction") REFERENCES "ex_?_instructions" ("address")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "ex_?_callgraph" ADD PRIMARY KEY ("id");
ALTER TABLE "ex_?_callgraph"
    ADD CONSTRAINT "ex_?_callgraph_source_fkey"
    FOREIGN KEY ("source") REFERENCES "ex_?_functions" ("address")
    ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ex_?_callgraph"
    ADD CONSTRAINT "ex_?_callgraph_destination_fkey"
    FOREIGN KEY ("destination") REFERENCES "ex_?_functions" ("address")
    ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ex_?_callgraph"
    ADD CONSTRAINT "ex_?_callgraph_source_basic_block_id_fkey"
    FOREIGN KEY ("source_basic_block_id") REFERENCES "ex_?_basic_blocks" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ex_?_callgraph"
    ADD CONSTRAINT "ex_?_callgraph_source_address_fkey"
    FOREIGN KEY ("source_address") REFERENCES "ex_?_instructions" ("address")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "ex_?_control_flow_graphs" ADD PRIMARY KEY ("id");
ALTER TABLE "ex_?_control_flow_graphs"
    ADD CONSTRAINT "ex_?_control_flow_graphs_parent_function_fkey"
    FOREIGN KEY ("parent_function") REFERENCES "ex_?_functions" ("address")
    ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ex_?_control_flow_graphs"
    ADD CONSTRAINT "ex_?_control_flow_graphs_source_fkey"
    FOREIGN KEY ("source") REFERENCES "ex_?_basic_blocks" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ex_?_control_flow_graphs"
    ADD CONSTRAINT "ex_?_control_flow_graphs_destination_fkey"
    FOREIGN KEY ("destination") REFERENCES "ex_?_basic_blocks" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "ex_?_expression_trees" ADD PRIMARY KEY ("id");

ALTER TABLE "ex_?_expression_nodes" ADD PRIMARY KEY ("id");
ALTER TABLE "ex_?_expression_nodes"
    ADD CONSTRAINT "ex_?_expression_nodes_parent_id_fkey"
    FOREIGN KEY ("parent_id") REFERENCES "ex_?_expression_nodes" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "ex_?_expression_tree_nodes"
    ADD CONSTRAINT "ex_?_expression_tree_nodes_expression_tree_id_fkey"
    FOREIGN KEY ("expression_tree_id") REFERENCES "ex_?_expression_trees" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ex_?_expression_tree_nodes"
    ADD CONSTRAINT "ex_?_expression_tree_nodes_expression_node_id_fkey"
    FOREIGN KEY ("expression_node_id") REFERENCES "ex_?_expression_nodes" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "ex_?_operands" ADD PRIMARY KEY ("address", "position");
ALTER TABLE "ex_?_operands"
    ADD CONSTRAINT "ex_?_operands_expression_tree_id_fkey"
    FOREIGN KEY ("expression_tree_id") REFERENCES "ex_?_expression_trees" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ex_?_operands"
    ADD CONSTRAINT "ex_?_operands_address_fkey"
    FOREIGN KEY ("address") REFERENCES "ex_?_instructions" ("address")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "ex_?_expression_substitutions"
    ADD CONSTRAINT "ex_?_expression_substitutions_address_position_fkey"
    FOREIGN KEY ("address", "position")
    REFERENCES "ex_?_operands" ("address", "position")
    ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ex_?_expression_substitutions"
    ADD CONSTRAINT "ex_?_expression_substitutions_expression_node_id_fkey"
    FOREIGN KEY ("expression_node_id")
    REFERENCES "ex_?_expression_nodes" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "ex_?_address_references"
    ADD CONSTRAINT "ex_?_address_references_address_position"
    FOREIGN KEY ("address", "position")
    REFERENCES "ex_?_operands" ("address", "position")
    ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ex_?_address_references"
    ADD CONSTRAINT "ex_?_address_references_expression_node_id_fkey"
    FOREIGN KEY ("expression_node_id") REFERENCES "ex_?_expression_nodes" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "ex_?_base_types" ADD PRIMARY KEY ("id");
ALTER TABLE "ex_?_base_types"
    ADD CONSTRAINT "ex_?_base_types_pointer_fkey"
    FOREIGN KEY ("pointer") REFERENCES "ex_?_base_types" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE deferrable initially deferred;

ALTER TABLE "ex_?_types" ADD PRIMARY KEY ("id");
ALTER TABLE "ex_?_types"
    ADD CONSTRAINT "ex_?_types_parent_id_fkey"
    FOREIGN KEY ("parent_id") REFERENCES "ex_?_base_types" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE deferrable initially deferred;
ALTER TABLE "ex_?_types"
    ADD CONSTRAINT "ex_?_types_base_type_fkey"
    FOREIGN KEY ("base_type") REFERENCES "ex_?_base_types" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "ex_?_expression_types"
    ADD PRIMARY KEY ("address", "position", "expression_id");
ALTER TABLE "ex_?_expression_types"
    ADD CONSTRAINT "ex_?_expression_type_type_fkey"
    FOREIGN KEY ("type") REFERENCES "ex_?_base_types" ("id")
    ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ex_?_sections" ADD PRIMARY KEY ("id");

ALTER TABLE "ex_?_type_instances" ADD PRIMARY KEY ("id");
ALTER TABLE "ex_?_type_instances"
    ADD CONSTRAINT ex_?_type_instances_type_id_fkey
    FOREIGN KEY ("type_id") REFERENCES ex_?_base_types ("id") MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE "ex_?_type_instances"
    ADD CONSTRAINT ex_?_type_instances_section_id_fkey
    FOREIGN KEY ("section_id") REFERENCES ex_?_sections ("id") MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "ex_?_expression_type_instances"
    ADD PRIMARY KEY ("address", "position", "expression_node_id");
ALTER TABLE "ex_?_expression_type_instances"
    ADD CONSTRAINT "ex_?_expression_type_instances_type_instance_id_fkey"
    FOREIGN KEY ("type_instance_id")
    REFERENCES ex_?_type_instances ("id") MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE "ex_?_expression_type_instances"
    ADD CONSTRAINT "ex_?_expression_type_instances_address_position_fkey"
    FOREIGN KEY ("address", "position")
    REFERENCES ex_?_operands ("address", "position") MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE "ex_?_expression_type_instances"
    ADD CONSTRAINT "ex_?_expression_type_instances_expression_node_id_fkey"
    FOREIGN KEY ("expression_node_id")
    REFERENCES ex_?_expression_nodes ("id") MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE;
)raw";

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_INITIALIZE_CONSTRAINTS_POSTGRESQL_SQL_H_
