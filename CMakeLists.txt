# Copyright 2011-2021 Google LLC
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

# CMake build file for BinDiff. This file relies on the open source version of
# BinExport a lot.
cmake_minimum_required(VERSION 3.14)
cmake_policy(VERSION 3.14)
project(bindiff VERSION 7)

# BinDiff settings
set(BINDIFF_BINARY_DIR "${PROJECT_BINARY_DIR}" CACHE INTERNAL "" FORCE)
set(BINDIFF_SOURCE_DIR "${PROJECT_SOURCE_DIR}" CACHE INTERNAL "" FORCE)
set(BINDIFF_BINEXPORT_DIR "${PROJECT_SOURCE_DIR}/../binexport" CACHE
    PATH "Path to a BinExport source tree")

# Import BinExport targets
set(BINEXPORT_ENABLE_TESTS OFF CACHE BOOL "" FORCE)
add_subdirectory("${BINDIFF_BINEXPORT_DIR}" "_deps/binexport-build" EXCLUDE_FROM_ALL)

# CMake settings
set(CMAKE_EXPORT_COMPILE_COMMANDS TRUE)
list(APPEND CMAKE_MODULE_PATH "${BINDIFF_SOURCE_DIR}/cmake"
                              "${BINDIFF_BINEXPORT_DIR}/cmake")

# BinDiff CMake modules, order matters
include(CompileOptions)
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

include(GoogleTest)
enable_testing()

# Make Google-style includes work
set(_bindiff_src "${BINDIFF_BINARY_DIR}/src_include/third_party/zynamics")
set(_bindiff_gen "${BINDIFF_BINARY_DIR}/gen_include/third_party/zynamics")
create_directory_symlink("${BINDIFF_SOURCE_DIR}" "${_bindiff_src}/bindiff")
create_directory_symlink("${absl_SOURCE_DIR}/absl" "${_bindiff_src}/../absl")
create_directory_symlink("${sqlite_SOURCE_DIR}" "${_bindiff_src}/../sqlite/src")
create_directory_symlink("${BINDIFF_BINARY_DIR}" "${_bindiff_gen}/bindiff")

# Set revision number, if applicable
if(NOT $ENV{KOKORO_PIPER_CHANGELIST} STREQUAL "")
  set(REVISION $ENV{KOKORO_PIPER_CHANGELIST})
else()
  set(REVISION internal)
endif()

# Interface library with include paths used by BinDiff
add_library(bindiff_base INTERFACE)
target_include_directories(bindiff_base INTERFACE
  ${BINEXPORT_SOURCE_DIR}/stubs  # Use BinExport infrastructure
  ${BINDIFF_BINARY_DIR}/src_include
  ${BINDIFF_BINARY_DIR}/gen_include
  ${Boost_INCLUDE_DIR}
)
target_link_libraries(bindiff_base INTERFACE
  protobuf::libprotobuf
)

# Interface library for tests
add_library(bindiff_test_base INTERFACE)
target_link_libraries(bindiff_test_base INTERFACE
  bindiff_base
  gtest_main
  gmock
)

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
  INTERPROCEDURAL_OPTIMIZATION ${BINDIFF_ENABLE_IPO})

# Configuration library
file(READ bindiff.json bindiff_BINDIFF_JSON)
configure_file(config_defaults.h.in config_defaults.h @ONLY)

protobuf_generate_cpp(bd_config_proto bd_config_proto_h
  bindiff_config.proto
)

add_library(bindiff_config STATIC
  ${bd_config_proto_h}
  ${bd_config_proto}
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
  call_graph_match.cc
  call_graph_match.h
  call_graph_match_function_address_sequence.cc
  call_graph_match_function_address_sequence.h
  call_graph_match_function_call_graph_edges_proximity_mdindex.cc
  call_graph_match_function_call_graph_edges_proximity_mdindex.h
  call_graph_match_function_call_graph_mdindex.cc
  call_graph_match_function_call_graph_mdindex.h
  call_graph_match_function_call_graph_mdindex_relaxed.cc
  call_graph_match_function_call_graph_mdindex_relaxed.h
  call_graph_match_function_call_sequence.cc
  call_graph_match_function_call_sequence.h
  call_graph_match_function_flow_graph_edges_mdindex.h
  call_graph_match_function_flow_graph_mdindex.cc
  call_graph_match_function_flow_graph_mdindex.h
  call_graph_match_function_hash.cc
  call_graph_match_function_hash.h
  call_graph_match_function_instruction_count.cc
  call_graph_match_function_instruction_count.h
  call_graph_match_function_loops.cc
  call_graph_match_function_loops.h
  call_graph_match_function_name_hash.cc
  call_graph_match_function_name_hash.h
  call_graph_match_function_prime.cc
  call_graph_match_function_prime.h
  call_graph_match_function_string_refs.cc
  call_graph_match_function_string_refs.h
  change_classifier.cc
  change_classifier.h
  comments.h
  database_writer.cc
  database_writer.h
  differ.cc
  differ.h
  fixed_points.cc
  fixed_points.h
  flow_graph.cc
  flow_graph.h
  flow_graph_match.cc
  flow_graph_match.h
  flow_graph_match_basic_block_call_refs.cc
  flow_graph_match_basic_block_call_refs.h
  flow_graph_match_basic_block_edges_lengauer_tarjan.cc
  flow_graph_match_basic_block_edges_lengauer_tarjan.h
  flow_graph_match_basic_block_edges_mdindex.cc
  flow_graph_match_basic_block_edges_mdindex.h
  flow_graph_match_basic_block_edges_prime.cc
  flow_graph_match_basic_block_edges_prime.h
  flow_graph_match_basic_block_entry_node.cc
  flow_graph_match_basic_block_entry_node.h
  flow_graph_match_basic_block_hash.cc
  flow_graph_match_basic_block_hash.h
  flow_graph_match_basic_block_instruction_count.cc
  flow_graph_match_basic_block_instruction_count.h
  flow_graph_match_basic_block_jump_sequence.cc
  flow_graph_match_basic_block_jump_sequence.h
  flow_graph_match_basic_block_loop_entry.cc
  flow_graph_match_basic_block_loop_entry.h
  flow_graph_match_basic_block_mdindex.cc
  flow_graph_match_basic_block_mdindex.h
  flow_graph_match_basic_block_mdindex_relaxed.cc
  flow_graph_match_basic_block_mdindex_relaxed.h
  flow_graph_match_basic_block_prime.cc
  flow_graph_match_basic_block_prime.h
  flow_graph_match_basic_block_self_loop.cc
  flow_graph_match_basic_block_self_loop.h
  flow_graph_match_basic_block_string_refs.cc
  flow_graph_match_basic_block_string_refs.h
  groundtruth_writer.cc
  groundtruth_writer.h
  instruction.cc
  instruction.h
  log_writer.cc
  log_writer.cc
  match_context.cc
  match_context.h
  prime_signature.cc
  prime_signature.h
  reader.cc
  reader.h
  sqlite.cc
  sqlite.h
  start_ui.cc
  statistics.h
  start_ui.h
  writer.cc
  writer.h
)
target_link_libraries(bindiff_shared
  absl::flat_hash_map
  absl::hash
  absl::str_format
  absl::strings
  binexport_shared
  sqlite
  bindiff_version
  bindiff_config
)
set_target_properties(bindiff_shared PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${BINDIFF_ENABLE_IPO})

# Library that helps with testing BinDiff's core engine.
add_library(bindiff_test_util STATIC
  test_util.cc
  test_util.h
)
target_link_libraries(bindiff_test_util
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
  bindiff_test_util
  bindiff_test_base
  bindiff_shared
)
gtest_discover_tests(change_classifier_test)

add_executable(prime_signature_test prime_signature_test.cc)
target_link_libraries(prime_signature_test
  bindiff_test_base
  bindiff_shared
)
gtest_discover_tests(prime_signature_test)

add_executable(writer_test
  log_writer_test.cc
  writer_test.cc
)
target_link_libraries(writer_test
  bindiff_test_base
  bindiff_test_util
  bindiff_shared
  binexport_shared
)
gtest_discover_tests(writer_test)

# Plugins will be named "bindiffX.so" and "bindiffX64.so" (or .dll or .dylib),
# where X is the release number.
set(bindiff_plugin_name bindiff${PROJECT_VERSION_MAJOR})
add_ida_plugin(${bindiff_plugin_name}
  ida/bindiff_icon.cc
  ida/bindiff_icon.h
  ida/main_plugin.cc
  ida/main_plugin.h
  ida/matched_functions_chooser.cc
  ida/matched_functions_chooser.h
  ida/names.cc
  ida/names.h
  ida/results.cc
  ida/results.h
  ida/statistics_chooser.cc
  ida/statistics_chooser.h
  ida/unmatched_functions_chooser.cc
  ida/unmatched_functions_chooser.h
  ida/visual_diff.cc
  ida/visual_diff.h
  match_colors.cc
  match_colors.h
)
if(WIN32)
  list(APPEND bindiff_plugin_libraries ws2_32.lib)
endif()
ida_target_link_libraries(${bindiff_plugin_name}
  binexport_core
  binexport_plugin_shared
  bindiff_base
  bindiff_shared
  absl::str_format
  absl::time
  ${bindiff_plugin_libraries}
)
set_ida_target_properties(${bindiff_plugin_name} PROPERTIES
  POSITION_INDEPENDENT_CODE ON
  INTERPROCEDURAL_OPTIMIZATION ${BINDIFF_ENABLE_IPO}
)
ida_install(TARGETS ${bindiff_plugin_name}
            ARCHIVE DESTINATION bindiff-prefix
            RUNTIME DESTINATION bindiff-prefix
            LIBRARY DESTINATION bindiff-prefix)

add_executable(bindiff main_portable.cc)
target_link_libraries(bindiff
  bindiff_shared
  absl::flat_hash_set
  absl::flags_parse
  absl::time
)
set_target_properties(bindiff PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${BINDIFF_ENABLE_IPO})
install(TARGETS bindiff RUNTIME DESTINATION bindiff-prefix)

# Utility programs. For now, only contains a tool to modify the BinDiff
# config from the installer/package scripts.
add_subdirectory(tools)
