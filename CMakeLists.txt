# Copyright 2011-2024 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# CMake build file for BinDiff. This file relies on the open source version of
# BinExport a lot.
cmake_minimum_required(VERSION 3.20..3.28)

cmake_policy(VERSION 3.20)

foreach(_bindiff_policy IN ITEMS
  CMP0135  # Set download timestamp to current time
  CMP0144  # Use upper-case <PACKAGENAME>_ROOT variables
  CMP0167  # Drop the FindBoost module
)
  if(POLICY ${_bindiff_policy})
    cmake_policy(SET ${_bindiff_policy} NEW)
  endif()
endforeach()

project(bindiff VERSION 8)

# BinDiff settings
set(BINDIFF_BINARY_DIR "${PROJECT_BINARY_DIR}" CACHE INTERNAL "" FORCE)
set(BINDIFF_SOURCE_DIR "${PROJECT_SOURCE_DIR}" CACHE INTERNAL "" FORCE)
set(BINDIFF_BINEXPORT_DIR "${PROJECT_SOURCE_DIR}/../binexport" CACHE
    PATH "Path to a BinExport source tree")

# Import targets shared with BinExport
set(BINEXPORT_ENABLE_BINARYNINJA OFF CACHE INTERNAL "" FORCE)
add_subdirectory("${BINDIFF_BINEXPORT_DIR}" "_deps/binexport-build" EXCLUDE_FROM_ALL)

# CMake settings
set(CMAKE_EXPORT_COMPILE_COMMANDS TRUE)
list(APPEND CMAKE_MODULE_PATH "${BINDIFF_SOURCE_DIR}/cmake"
                              "${BINDIFF_BINEXPORT_DIR}/cmake")

# BinDiff CMake modules, order matters
include(CompileOptions)
include(BinDiffOptions)
include(BinDiffDeps)

set(bindiff_BINEXPORT_RELEASE 12)

string(TOLOWER "${CMAKE_BUILD_TYPE}" _bindiff_build_type)
if(NOT _bindiff_build_type STREQUAL "debug")
  include(CheckIPOSupported)
  check_ipo_supported(RESULT _bindiff_ipo_supported)
else()
  set(_bindiff_ipo_supported FALSE)
endif()
option(BINDIFF_ENABLE_IPO "Enable interprocedural optimization"
                          ${_bindiff_ipo_supported})

include(CTest)
if(BUILD_TESTING AND BINDIFF_BUILD_TESTING)
  include(GoogleTest)
  enable_testing()
endif()

# Make Google-style includes work
create_directory_symlink(
  "${BINDIFF_SOURCE_DIR}"
  "${BINDIFF_BINARY_DIR}/src_include/third_party/zynamics/bindiff"
)
create_directory_symlink(
  "${absl_SOURCE_DIR}/absl"
  "${BINDIFF_BINARY_DIR}/src_include/third_party/absl"
)
create_directory_symlink(
  "${sqlite_SOURCE_DIR}"
  "${BINDIFF_BINARY_DIR}/src_include/third_party/sqlite/src"
)
create_directory_symlink(
  "${BINDIFF_BINARY_DIR}"
  "${BINDIFF_BINARY_DIR}/gen_include/third_party/zynamics/bindiff"
)

# Set revision number, if applicable
if(NOT $ENV{KOKORO_PIPER_CHANGELIST} STREQUAL "")
  set(REVISION $ENV{KOKORO_PIPER_CHANGELIST})
else()
  set(REVISION internal)
endif()

# Plugin names for IDA Pro
set(bindiff_ida_plugin_name bindiff${bindiff_VERSION_MAJOR}_ida)

# Interface library with include paths used by BinDiff
add_library(bindiff_base INTERFACE)
target_include_directories(bindiff_base INTERFACE
  "${BINEXPORT_SOURCE_DIR}/stubs"  # Use BinExport infrastructure
  "${BINDIFF_BINARY_DIR}/src_include"
  "${BINDIFF_BINARY_DIR}/gen_include"
  "${Boost_INCLUDE_DIR}"
)
target_link_libraries(bindiff_base INTERFACE
  protobuf::libprotobuf
)

if(BUILD_TESTING AND BINDIFF_BUILD_TESTING)
  # Interface library for tests
  add_library(bindiff_test_base INTERFACE)
  target_link_libraries(bindiff_test_base INTERFACE
    bindiff_base
    gtest_main
    gmock
  )
endif()

# Version information
configure_file(version.cc.in version.cc ESCAPE_QUOTES @ONLY)

add_library(bindiff_version STATIC
  version.h
  ${CMAKE_CURRENT_BINARY_DIR}/version.cc
)
target_link_libraries(bindiff_version
  absl::strings
  absl::time
  bindiff_base
)
set_target_properties(bindiff_version PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${BINDIFF_ENABLE_IPO}
)

# Configuration library
file(READ bindiff.json bindiff_BINDIFF_JSON)
configure_file(config_defaults.h.in config_defaults.h @ONLY)

protobuf_generate_cpp(bd_config_proto bd_config_proto_h
  bindiff_config.proto
)

add_library(bindiff_config STATIC
  "${bd_config_proto_h}"
  "${bd_config_proto}"
  config.cc
  config.h
  ${CMAKE_CURRENT_BINARY_DIR}/config_defaults.h
)
target_link_libraries(bindiff_config
  bindiff_version
  binexport_shared
  absl::flat_hash_map
  absl::hash
  absl::status
)
set_target_properties(bindiff_config PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${BINDIFF_ENABLE_IPO})

# Sources shared by the differ and the IDA plugin.
add_library(bindiff_shared STATIC
  call_graph.cc
  call_graph.h
  change_classifier.cc
  change_classifier.h
  comment.h
  database_writer.cc
  database_writer.h
  differ.cc
  differ.h
  fixed_points.cc
  fixed_points.h
  flow_graph.cc
  flow_graph.h
  groundtruth_writer.cc
  groundtruth_writer.h
  instruction.cc
  instruction.h
  log_writer.cc
  log_writer.cc
  match/basic_block_call_refs.cc
  match/basic_block_call_refs.h
  match/basic_block_edges_lengauer_tarjan.cc
  match/basic_block_edges_lengauer_tarjan.h
  match/basic_block_edges_mdindex.cc
  match/basic_block_edges_mdindex.h
  match/basic_block_edges_prime.cc
  match/basic_block_edges_prime.h
  match/basic_block_entry_node.cc
  match/basic_block_entry_node.h
  match/basic_block_hash.cc
  match/basic_block_hash.h
  match/basic_block_instruction_count.cc
  match/basic_block_instruction_count.h
  match/basic_block_jump_sequence.cc
  match/basic_block_jump_sequence.h
  match/basic_block_loop_entry.cc
  match/basic_block_loop_entry.h
  match/basic_block_mdindex.cc
  match/basic_block_mdindex.h
  match/basic_block_mdindex_relaxed.cc
  match/basic_block_mdindex_relaxed.h
  match/basic_block_prime.cc
  match/basic_block_prime.h
  match/basic_block_self_loop.cc
  match/basic_block_self_loop.h
  match/basic_block_string_refs.cc
  match/basic_block_string_refs.h
  match/call_graph.cc
  match/call_graph.h
  match/context.cc
  match/context.h
  match/flow_graph.cc
  match/flow_graph.h
  match/function_address_sequence.cc
  match/function_address_sequence.h
  match/function_call_graph_edges_proximity_mdindex.cc
  match/function_call_graph_edges_proximity_mdindex.h
  match/function_call_graph_mdindex.cc
  match/function_call_graph_mdindex.h
  match/function_call_graph_mdindex_relaxed.cc
  match/function_call_graph_mdindex_relaxed.h
  match/function_call_sequence.cc
  match/function_call_sequence.h
  match/function_flow_graph_edges_mdindex.h
  match/function_flow_graph_mdindex.cc
  match/function_flow_graph_mdindex.h
  match/function_hash.cc
  match/function_hash.h
  match/function_instruction_count.cc
  match/function_instruction_count.h
  match/function_loops.cc
  match/function_loops.h
  match/function_name_hash.cc
  match/function_name_hash.h
  match/function_prime.cc
  match/function_prime.h
  match/function_string_refs.cc
  match/function_string_refs.h
  match_colors.cc
  match_colors.h
  prime_signature.cc
  prime_signature.h
  reader.cc
  reader.h
  sqlite.cc
  sqlite.h
  start_ui.cc
  start_ui.h
  statistics.h
  writer.cc
  writer.h
)
target_link_libraries(bindiff_shared
  absl::btree
  absl::flat_hash_map
  absl::hash
  absl::memory
  absl::nullability
  absl::str_format
  absl::strings
  absl::status
  binexport_shared
  sqlite
  bindiff_version
  bindiff_config
)
set_target_properties(bindiff_shared PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${BINDIFF_ENABLE_IPO}
)

if(BUILD_TESTING AND BINDIFF_BUILD_TESTING)
  # Library that helps with testing BinDiff's core engine.
  add_library(bindiff_test_util STATIC
    test_util.cc
    test_util.h
  )
  target_link_libraries(bindiff_test_util
    absl::btree
    binexport_testing
    bindiff_test_base
    bindiff_shared
  )

  add_executable(call_graph_test call_graph_test.cc)
  target_link_libraries(call_graph_test
    bindiff_test_util
    bindiff_test_base
    bindiff_shared
  )
  gtest_discover_tests(call_graph_test)


  add_executable(change_classifier_test change_classifier_test.cc)
  target_link_libraries(change_classifier_test
    bindiff_test_base
    bindiff_test_util
    bindiff_shared
  )
  gtest_discover_tests(change_classifier_test)

  add_executable(database_writer_test database_writer_test.cc)
  target_link_libraries(database_writer_test
    bindiff_test_base
    bindiff_test_util
    bindiff_shared
  )
  gtest_discover_tests(database_writer_test)

  add_executable(match_tests match/basic_block_instruction_count_test.cc)
  target_link_libraries(match_tests
    bindiff_test_base
    bindiff_test_util
    bindiff_shared
  )
  gtest_discover_tests(match_tests)

  add_executable(prime_signature_test prime_signature_test.cc)
  target_link_libraries(prime_signature_test
    bindiff_test_base
    bindiff_shared
  )
  gtest_discover_tests(prime_signature_test)

  add_executable(sqlite_test sqlite_test.cc)
  target_link_libraries(sqlite_test
    absl::status_matchers
    bindiff_test_base
    bindiff_test_util
    bindiff_shared
  )
  gtest_discover_tests(sqlite_test)

  add_executable(writer_test
    log_writer_test.cc
    writer_test.cc
  )
  target_link_libraries(writer_test
    absl::status_matchers
    bindiff_test_base
    bindiff_test_util
    bindiff_shared
    binexport_shared
  )
  gtest_discover_tests(writer_test)

  if(BINDIFF_BUILD_BENCHMARK)
    # Needs to be run from the build dir in order to find the fixtures:
    #   cd src_include/third_party/zynamics; ../../../differ_benchmark
    add_executable(differ_benchmark
      differ_benchmark.cc
    )
    target_link_libraries(differ_benchmark
      bindiff_base
      benchmark::benchmark_main
      gtest
      gmock
      bindiff_test_util
      bindiff_shared
      binexport_shared
    )
  endif()
endif()

add_executable(bindiff main_portable.cc)
target_link_libraries(bindiff
  bindiff_shared
  absl::flags_parse
  absl::flat_hash_set
  absl::log_globals
  absl::time
)
if(NOT MSVC)
  # IPO breaks Abseil's flag parsing on Windows
  set_target_properties(bindiff PROPERTIES
    INTERPROCEDURAL_OPTIMIZATION ${BINDIFF_ENABLE_IPO}
  )
endif()
install(TARGETS bindiff RUNTIME DESTINATION bindiff-prefix)

# IDA Pro plugins
if(IdaSdk_FOUND)
  add_subdirectory(ida)
endif(IdaSdk_FOUND)

# Utility programs. For now, only contains a tool to modify the BinDiff
# config from the installer/package scripts.
add_subdirectory(tools)
