# Copyright 2011-2019 Google LLC. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

cmake_minimum_required(VERSION 3.7)
cmake_policy(VERSION 3.7)
project(binexport VERSION 10)  # Only major version is used

# XXX: Replace for OSS!
set(ThirdParty_DIR ${PROJECT_SOURCE_DIR}/../binexport10_g3/google3/third_party)

list(APPEND CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR})
include(copts.cmake)
include(util.cmake)
include(sqlite.cmake)

# Use a copy of a subset of Boost that we ship
set(Boost_NO_SYSTEM_PATHS TRUE)
set(BOOST_ROOT ${PROJECT_SOURCE_DIR}/third_party/boost_parts)

set(OPENSSL_USE_STATIC_LIBS TRUE)
set(Protobuf_USE_STATIC_LIBS TRUE)

find_package(Boost 1.55 REQUIRED)
find_package(Git)
find_package(IdaSdk REQUIRED)
find_package(OpenSSL 1.0.2 REQUIRED)
find_package(PostgreSQL 9.5 REQUIRED)
find_package(Protobuf 3.0.0 REQUIRED)
include(googletest.cmake)
include(absl.cmake)

enable_testing()

if(WIN32)
  list(GET PostgreSQL_LIBRARIES 0 postgresql_lib)
  get_filename_component(postgresql_root ${postgresql_lib} DIRECTORY)
  list(APPEND PostgreSQL_LIBRARIES ${postgresql_root}/libpgport.lib)
endif()

# Make Google-style includes work
set(_binexport_src ${PROJECT_BINARY_DIR}/src_include/third_party/zynamics)
set(_binexport_gen ${PROJECT_BINARY_DIR}/gen_include/third_party/zynamics)
create_directory_symlink(${PROJECT_SOURCE_DIR} ${_binexport_src}/binexport)
create_directory_symlink(${absl_src_dir}/absl ${_binexport_src}/../absl)
create_directory_symlink(${PROJECT_BINARY_DIR} ${_binexport_gen}/binexport)

# Find the Git revision number, if applicable
set(REVISION unknown)
if(GIT_FOUND)
  execute_process(COMMAND ${GIT_EXECUTABLE} rev-parse --short HEAD
                  OUTPUT_VARIABLE REVISION ERROR_QUIET)
  if(NOT ${REVISION} STREQUAL "")
    string(STRIP ${REVISION} REVISION)
  elseif(NOT $ENV{KOKORO_PIPER_CHANGELIST} STREQUAL "")
    set(REVISION $ENV{KOKORO_PIPER_CHANGELIST})
  else()
    set(REVISION internal)
  endif()
endif()

configure_file(version.h.in version.h ESCAPE_QUOTES @ONLY)

# Interface library with include paths used by BinExport
add_library(binexport_base INTERFACE)
target_include_directories(binexport_base INTERFACE
  ${PROJECT_SOURCE_DIR}
  ${PROJECT_SOURCE_DIR}/stubs
  ${PROJECT_BINARY_DIR}/gen_include
  ${PROJECT_BINARY_DIR}/src_include
  ${Boost_INCLUDE_DIR}
  ${PostgreSQL_INCLUDE_DIRS}
  ${Protobuf_INCLUDE_DIRS}
)
target_link_libraries(binexport_base INTERFACE
  ${Protobuf_LIBRARIES}  # Same as protobuf::libprotobuf
)

# Interface library to be used by tests that don't need data files
add_library(binexport_test_base INTERFACE)
target_link_libraries(binexport_test_base INTERFACE
  gtest_main
  gmock
)

# BinExport format version 2 proto library
protobuf_generate_cpp(binexport2_proto binexport2_proto_h binexport2.proto)

# Utility library code shared with BinDiff
add_library(binexport_shared STATIC
  ${binexport2_proto_h}
  ${binexport2_proto}
  binexport.cc
  binexport.h
  hash.cc
  hash.h
  util/canonical_errors.cc
  util/canonical_errors.h
  util/filesystem.cc
  util/filesystem.h
  util/format.cc
  util/format.h
  util/status.cc
  util/status.h
  util/status_macros.h
  util/statusor.h
  util/timer.h
)
target_link_libraries(binexport_shared PUBLIC
  absl::bad_optional_access
  absl::optional
  absl::str_format
  absl::strings
  absl::time
  absl::variant
  binexport_base
)
if(WIN32)
  target_link_libraries(binexport_shared PUBLIC
    shlwapi.lib
  )
endif()
add_executable(binexport_shared_test
  util/filesystem_test.cc
  util/format_test.cc
  util/status_macros_test.cc
  util/status_matchers.h
  util/status_test.cc
  util/statusor_test.cc
  util/timer_test.cc
)
target_link_libraries(binexport_shared_test PUBLIC
  binexport_test_base
  binexport_shared
)
add_test(BinExportSharedTests binexport_shared_test)

add_library(binexport_test_util
  binexport_test_util.cc
  binexport_test_util.h
)
target_link_libraries(binexport_test_util PUBLIC
  binexport_test_base
  binexport_shared
  absl::memory
)

# General BinExport tests
add_executable(binexport_test
  binexport_test.cc
)
target_link_libraries(binexport_test PUBLIC
  binexport_test_base
  binexport_shared
  absl::memory
)
add_test(BinExportTests binexport_test)

# binexport2dump tool
add_subdirectory(tools)

# Code shared with the BinDiff plugin
add_ida_library(binexport_plugin_shared STATIC
  hash.cc
  hash.h
  ida/digest.cc
  ida/digest.h
  ida/log.cc
  ida/log.h
)
ida_target_link_libraries(binexport_plugin_shared PUBLIC
  absl::time
  binexport_base
)

add_ida_library(binexport_db STATIC
  database.cc
  database.h
)
ida_target_include_directories(binexport_db PUBLIC
  ${ThirdParty_DIR}/..
)
ida_target_link_libraries(binexport_db PUBLIC
  absl::strings
  binexport_base
  sqlite
)

set(binexport_plugin_name binexport${binexport_VERSION_MAJOR})
add_ida_plugin(${binexport_plugin_name}
  address_references.cc
  address_references.h
  base_types.cc
  base_types.h
  basic_block.cc
  basic_block.h
  binexport2_writer.cc
  binexport2_writer.h
  call_graph.cc
  call_graph.h
  comment.cc
  comment.h
  database_writer.cc
  database_writer.h
  dump_writer.cc
  dump_writer.h
  edge.cc
  edge.h
  entry_point.cc
  entry_point.h
  expression.cc
  expression.h
  flow_analyzer.cc
  flow_analyzer.h
  flow_graph.cc
  flow_graph.h
  function.cc
  function.h
  ida/arm.cc
  ida/arm.h
  ida/dalvik.cc
  ida/dalvik.h
  ida/generic.cc
  ida/generic.h
  ida/main_plugin.cc
  ida/main_plugin.h
  ida/metapc.cc
  ida/metapc.h
  ida/mips.cc
  ida/mips.h
  ida/names.cc
  ida/names.h
  ida/ppc.cc
  ida/ppc.h
  ida/types_container.cc
  ida/types_container.h
  ida/plugin.h
  ida/ui.cc
  ida/ui.h
  initialize_constraints_postgresql_sql.h
  initialize_indices_postgresql_sql.h
  initialize_tables_postgresql_sql.h
  instruction.cc
  instruction.h
  library_manager.cc
  library_manager.h
  maintenance_postgresql_sql.h
  operand.cc
  operand.h
  postgresql.cc
  postgresql.h
  query_builder.cc
  query_builder.h
  statistics_writer.cc
  statistics_writer.h
  type_system.cc
  type_system.h
  version.h
  virtual_memory.cc
  virtual_memory.h
  x86_nop.cc
  x86_nop.h
)
if(WIN32)
  list(APPEND binexport_libraries crypt32.lib
                                  secur32.lib
                                  shlwapi.lib
                                  ws2_32.lib
                                  wldap32.lib)
endif()
ida_target_link_libraries(${binexport_plugin_name}
  absl::bad_optional_access
  absl::flat_hash_map
  absl::flat_hash_set
  absl::hash
  absl::node_hash_map
  absl::node_hash_set
  absl::optional
  absl::strings
  absl::time
  binexport_db
  binexport_shared
  binexport_plugin_shared
  ${PostgreSQL_LIBRARIES}
  # OpenSSL must come after PostgreSQL
  OpenSSL::SSL
  ${binexport_libraries}
)
set_ida_target_properties(${binexport_plugin_name}
  PROPERTIES POSITION_INDEPENDENT_CODE ON
)
ida_install(TARGETS ${binexport_plugin_name}
            ARCHIVE DESTINATION binexport-prefix
            RUNTIME DESTINATION binexport-prefix
            LIBRARY DESTINATION binexport-prefix)

# Experimental BinExport reader library
add_library(binexport_reader STATIC
  reader/call_graph.cc
  reader/call_graph.h
  reader/flow_graph.cc
  reader/flow_graph.h
  reader/graph_utility.h
  reader/instruction.cc
  reader/instruction.h
)
target_link_libraries(binexport_reader PUBLIC
  absl::strings
  binexport_shared
)
add_executable(binexport_reader_test
  reader/call_graph_test.cc
  reader/flow_graph_test.cc
  reader/graph_utility_test.cc
  reader/instruction_test.cc
  reader/reader_test_util.cc
  reader/reader_test_util.h
)
target_link_libraries(binexport_reader_test PUBLIC
  gtest_main
  gmock
  binexport_reader
)
add_test(
  NAME BinExportReaderTests
  COMMAND binexport_reader_test ${PROJECT_SOURCE_DIR}/reader
)
