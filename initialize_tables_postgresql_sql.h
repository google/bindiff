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

// PostgreSQL table initialization. This file should only be included from
// database.cc.

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_INITIALIZE_TABLES_POSTGRESQL_SQL_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_INITIALIZE_TABLES_POSTGRESQL_SQL_H_

#include "third_party/zynamics/binexport/database_writer.h"

const std::string DatabaseWriter::postgresql_initialize_tables_ = R"raw(
DROP TABLE IF EXISTS "ex_?_address_comments";
DROP TABLE IF EXISTS "ex_?_address_references";
DROP TABLE IF EXISTS "ex_?_expression_substitutions";
DROP TABLE IF EXISTS "ex_?_operands";
DROP TABLE IF EXISTS "ex_?_expression_tree_nodes";
DROP TABLE IF EXISTS "ex_?_expression_trees";
DROP TABLE IF EXISTS "ex_?_expression_nodes";
DROP TABLE IF EXISTS "ex_?_control_flow_graphs";
DROP TABLE IF EXISTS "ex_?_callgraph";
DROP TABLE IF EXISTS "ex_?_basic_block_instructions";
DROP TABLE IF EXISTS "ex_?_instructions";
DROP TABLE IF EXISTS "ex_?_basic_blocks";
DROP TABLE IF EXISTS "ex_?_functions";
DROP TABLE IF EXISTS "ex_?_type_renderers";
DROP TABLE IF EXISTS "ex_?_base_types";
DROP TABLE IF EXISTS "ex_?_types";
DROP TABLE IF EXISTS "ex_?_sections";
DROP TABLE IF EXISTS "ex_?_type_substitution_paths";

CREATE TABLE "ex_?_functions" (
    "address" BIGINT NOT NULL,
    "name" TEXT NOT NULL,
    "demangled_name" TEXT NULL DEFAULT NULL,
    "has_real_name" BOOLEAN NOT NULL,
    "type" INTEGER NOT NULL DEFAULT 0 CHECK ("type" IN (0, 1, 2, 3, 4)),
    "module_name" TEXT NULL DEFAULT NULL,
    "stack_frame" INTEGER NULL DEFAULT NULL,
    "prototype" INTEGER NULL DEFAULT NULL
);

CREATE TABLE "ex_?_basic_blocks" (
    "id" INTEGER NOT NULL,
    "parent_function" BIGINT NOT NULL,
    "address" BIGINT NOT NULL
);

CREATE TABLE "ex_?_instructions" (
    "address" BIGINT NOT NULL,
    "mnemonic" VARCHAR(32) NOT NULL,
    "data" bytea NOT NULL
);

CREATE TABLE "ex_?_basic_block_instructions" (
    "basic_block_id" INTEGER NOT NULL,
    "instruction" BIGINT NOT NULL,
    "sequence" INTEGER NOT NULL
);

CREATE TABLE "ex_?_callgraph" (
    "id" SERIAL,
    "source" BIGINT NOT NULL,
    "source_basic_block_id" INTEGER NOT NULL,
    "source_address" BIGINT NOT NULL,
    "destination" BIGINT NOT NULL
);

CREATE TABLE "ex_?_control_flow_graphs" (
    "id" SERIAL,
    "parent_function" BIGINT NOT NULL,
    "source" INTEGER NOT NULL,
    "destination" INTEGER NOT NULL,
    "type" INTEGER NOT NULL DEFAULT 0 CHECK ("type" IN (0, 1, 2, 3))
);

CREATE TABLE "ex_?_expression_trees" (
    "id" SERIAL
);

CREATE TABLE "ex_?_expression_nodes" (
    "id" SERIAL,
    "type" INTEGER NOT NULL DEFAULT 0 CHECK ("type" >= 0 AND "type" <= 7),
    "symbol" VARCHAR(256),
    "immediate" BIGINT,
    "position" INTEGER,
    "parent_id" INTEGER CHECK ("id" > "parent_id")
);

CREATE TABLE "ex_?_expression_tree_nodes" (
    "expression_tree_id" INTEGER NOT NULL,
    "expression_node_id" INTEGER NOT NULL
);

CREATE TABLE "ex_?_operands" (
    "address" BIGINT NOT NULL,
    "expression_tree_id" INTEGER NOT NULL,
    "position" INTEGER NOT NULL
);

CREATE TABLE "ex_?_expression_substitutions" (
    "id" SERIAL,
    "address" BIGINT NOT NULL,
    "position" INTEGER NOT NULL,
    "expression_node_id" INTEGER NOT NULL,
    "replacement" TEXT NOT NULL
);

CREATE TABLE "ex_?_address_references" (
    "address" BIGINT NOT NULL,
    "position" INTEGER null,
    "expression_node_id" INTEGER null,
    "destination" BIGINT NOT NULL,
    "type" INTEGER NOT NULL DEFAULT 0 CHECK ("type" >= 0 AND "type" <= 8)
);

CREATE TABLE "ex_?_address_comments" (
    "address" BIGINT NOT NULL,
    "comment" TEXT NOT NULL
);

DROP TYPE IF EXISTS "ex_?_type_category";
CREATE TYPE "ex_?_type_category" AS ENUM (
    'atomic', 'pointer', 'array',
    'struct', 'union', 'function_pointer');

CREATE TABLE "ex_?_base_types" (
    "id" INTEGER NOT NULL,
    "name" TEXT NOT NULL,
    "size" INTEGER NOT NULL,
    "pointer" INTEGER,
    "signed" BOOLEAN,
    "category" "ex_?_type_category" NOT NULL
);
CREATE TABLE "ex_?_types" (
    "id" SERIAL NOT NULL,
    "name" TEXT NOT NULL,
    "base_type" INTEGER NOT NULL,
    "parent_id" INTEGER,
    "offset" INTEGER,
    "argument" INTEGER,
    "number_of_elements" INTEGER
);

DROP TYPE IF EXISTS "ex_?_type_renderers_renderer_type";
CREATE TYPE "ex_?_type_renderers_renderer_type" AS ENUM (
    'integer', 'floating point', 'boolean', 'ascii', 'utf8', 'utf16');

CREATE TABLE "ex_?_type_renderers" (
    "type_id" INTEGER NOT NULL,
    "renderer" "ex_?_type_renderers_renderer_type" NOT NULL
);

DROP TYPE IF EXISTS "ex_?_section_permission_type";
CREATE TYPE "ex_?_section_permission_type" AS ENUM (
    'READ', 'WRITE', 'EXECUTE', 'READ_WRITE', 'READ_EXECUTE', 'WRITE_EXECUTE',
    'READ_WRITE_EXECUTE');

CREATE TABLE "ex_?_sections" (
    "id" SERIAL NOT NULL,
    "name" TEXT NOT NULL,
    "start_address" BIGINT NOT NULL,
    "end_address" BIGINT NOT NULL,
    "permission" "ex_?_section_permission_type" NOT NULL,
    "data" bytea NOT NULL
);

CREATE TABLE "ex_?_expression_types" (
    "address" BIGINT NOT NULL,
    "position" INTEGER NOT NULL,
    "expression_id" INTEGER NOT NULL,
    "type" INTEGER NOT NULL,
    "path" INTEGER[] NOT NULL,
    "offset" INTEGER
);

CREATE TABLE "ex_?_expression_type_instances" (
    "address" BIGINT NOT NULL,
    "position" INTEGER NOT NULL,
    "expression_node_id" INTEGER NOT NULL,
    "type_instance_id" INTEGER NOT NULL
);

CREATE TABLE "ex_?_type_instances" (
    "id" INTEGER NOT NULL,
    "name" TEXT NOT NULL,
    "section_offset" BIGINT NOT NULL,
    "type_id" INTEGER NOT NULL,
    "section_id" INTEGER NOT NULL
);

CREATE TABLE "ex_?_type_substitution_paths" (
    "id" INTEGER NOT NULL,
    "child_id" INTEGER,
    "type_id" INTEGER NOT NULL
);
)raw";

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_INITIALIZE_TABLES_POSTGRESQL_SQL_H_
