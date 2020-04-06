# Copyright 2011-2020 Google LLC
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
cmake_minimum_required(VERSION 3.12)
cmake_policy(VERSION 3.12)
project(bindiff VERSION 6)

set(BinExport_DIR ${PROJECT_SOURCE_DIR}/../binexport)
set(Google3_DIR ${PROJECT_SOURCE_DIR}/../../..)
set(ThirdParty_DIR ${PROJECT_SOURCE_DIR}/../../../third_party)

set(CMAKE_EXPORT_COMPILE_COMMANDS TRUE)

string(TOLOWER "${CMAKE_BUILD_TYPE}" _bindiff_build_type)
if(NOT _bindiff_build_type STREQUAL "debug")
  include(CheckIPOSupported)
  check_ipo_supported(RESULT _bindiff_ipo_supported)
else()
  set(_bindiff_ipo_supported FALSE)
endif()

include(GoogleTest) # Needs to come before changing module path
list(APPEND CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR} ${BinExport_DIR})
include(${BinExport_DIR}/copts.cmake)
include(${BinExport_DIR}/util.cmake)
include(sqlite.cmake)
include(tinyxpath.cmake)

# Use a copy of a subset of Boost that ships with BinExport
set(Boost_NO_SYSTEM_PATHS ON)
set(BOOST_ROOT ${BinExport_DIR}/third_party/boost_parts)

set(OPENSSL_USE_STATIC_LIBS TRUE)
set(Protobuf_USE_STATIC_LIBS TRUE)

find_package(Boost 1.55 REQUIRED)
find_package(IdaSdk REQUIRED)
find_package(OpenSSL 1.0.2 REQUIRED)
find_package(Protobuf 3.0.0 REQUIRED)
include(${BinExport_DIR}/googletest.cmake)
include(${BinExport_DIR}/absl.cmake)

enable_testing()

# Make Google-style includes work
set(_bindiff_src ${PROJECT_BINARY_DIR}/src_include/third_party/zynamics)
set(_bindiff_gen ${PROJECT_BINARY_DIR}/gen_include/third_party/zynamics)
create_directory_symlink(${PROJECT_SOURCE_DIR} ${_bindiff_src}/bindiff)
create_directory_symlink(${PROJECT_BINARY_DIR} ${_bindiff_gen}/bindiff)
create_directory_symlink(${PROJECT_BINARY_DIR} ${_bindiff_gen}/binexport)
create_directory_symlink(${absl_src_dir}/absl ${_bindiff_src}/../absl)

# Set revision number, if applicable
if(NOT $ENV{KOKORO_PIPER_CHANGELIST} STREQUAL "")
  set(REVISION $ENV{KOKORO_PIPER_CHANGELIST})
else()
  set(REVISION internal)
endif()

configure_file(version.cc.in version.cc ESCAPE_QUOTES @ONLY)

# Interface library with include paths used by BinDiff
add_library(bindiff_base INTERFACE)
target_include_directories(bindiff_base INTERFACE
  ${BinExport_DIR}/stubs  # Use BinExport infrastructure
  ${PROJECT_BINARY_DIR}/src_include
  ${PROJECT_BINARY_DIR}/gen_include
  ${absl_src_dir}
  ${Boost_INCLUDE_DIR}
  ${Protobuf_INCLUDE_DIRS}
  ${Google3_DIR}
)
target_link_libraries(bindiff_base INTERFACE
  ${Protobuf_LIBRARIES}  # Same as protobuf::libprotobuf
)

# Interface library for tests
add_library(bindiff_tests_base INTERFACE)
target_link_libraries(bindiff_tests_base INTERFACE
  bindiff_base
  gtest_main
  gmock
)

protobuf_generate_cpp(binexport2_proto binexport2_proto_h
  ${BinExport_DIR}/binexport2.proto
)
add_library(binexport_shared STATIC
  ${binexport2_proto_h}
  ${binexport2_proto}
  ${BinExport_DIR}/binexport.cc
  ${BinExport_DIR}/binexport.h
  ${BinExport_DIR}/hash.cc
  ${BinExport_DIR}/hash.h
  ${BinExport_DIR}/util/canonical_errors.cc
  ${BinExport_DIR}/util/canonical_errors.h
  ${BinExport_DIR}/util/filesystem.cc
  ${BinExport_DIR}/util/filesystem.h
  ${BinExport_DIR}/util/format.cc
  ${BinExport_DIR}/util/format.h
  ${BinExport_DIR}/util/status.cc
  ${BinExport_DIR}/util/status.h
  ${BinExport_DIR}/util/status_macros.h
  ${BinExport_DIR}/util/statusor.h
  ${BinExport_DIR}/util/timer.h
)
target_link_libraries(binexport_shared PUBLIC
  absl::bad_optional_access
  absl::optional
  absl::str_format
  absl::strings
  absl::time
  absl::variant
  bindiff_base
)
if(WIN32)
  target_link_libraries(binexport_shared PUBLIC
    shlwapi.lib
  )
else()
  if(NOT CMAKE_CXX_COMPILER_ID STREQUAL "AppleClang")
    target_link_libraries(binexport_shared PUBLIC
      stdc++fs
    )
  endif()
endif()
set_target_properties(binexport_shared PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${_bindiff_ipo_supported})

# XML configuration library
add_library(xmlconfig STATIC xmlconfig.cc
                             xmlconfig.h)
target_link_libraries(xmlconfig
  binexport_shared
  absl::flat_hash_map
  absl::hash
  tinyxpath
)
set_target_properties(xmlconfig PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${_bindiff_ipo_supported})

add_executable(xmlconfig_test xmlconfig_test.cc)
target_link_libraries(xmlconfig_test
  bindiff_tests_base
  xmlconfig
)
gtest_discover_tests(xmlconfig_test)

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
  config.cc
  config.h
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
  idb_export.cc
  idb_export.h
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
  utility.cc
  utility.h
  version.h
  ${CMAKE_CURRENT_BINARY_DIR}/version.cc
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
  xmlconfig)
set_target_properties(bindiff_shared PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${_bindiff_ipo_supported})

add_executable(idb_export_test idb_export_test.cc)
target_link_libraries(idb_export_test
  bindiff_tests_base
  bindiff_shared
)
gtest_discover_tests(idb_export_test PROPERTIES
  ENVIRONMENT "TEST_TMPDIR=${PROJECT_BINARY_DIR}/idb_export_test_tmp"
)

add_executable(prime_signature_test prime_signature_test.cc)
target_link_libraries(prime_signature_test
  bindiff_tests_base
  bindiff_shared
)
gtest_discover_tests(prime_signature_test)

add_executable(utility_test utility_test.cc)
target_link_libraries(utility_test
  bindiff_tests_base
  bindiff_shared
)
gtest_discover_tests(utility_test)

# Plugins will be named "bindiffX.so" and "bindiffX64.so" (or .dll or .dylib),
# where X is the release number.
set(bindiff_plugin_name bindiff${PROJECT_VERSION_MAJOR})
add_ida_plugin(${bindiff_plugin_name}
  ${BinExport_DIR}/ida/digest.cc
  ${BinExport_DIR}/ida/digest.h
  ${BinExport_DIR}/ida/log.cc
  ${BinExport_DIR}/ida/log.h
  ${BinExport_DIR}/ida/plugin.h
  ${BinExport_DIR}/ida/ui.cc
  ${BinExport_DIR}/ida/ui.h
  ${BinExport_DIR}/ida/util.cc
  ${BinExport_DIR}/ida/util.h
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
  ida/ui.cc
  ida/ui.h
  ida/unmatched_functions_chooser.cc
  ida/unmatched_functions_chooser.h
  ida/visual_diff.cc
  ida/visual_diff.h
)
if(WIN32)
  list(APPEND bindiff_plugin_libraries ws2_32.lib)
endif()
ida_target_link_libraries(${bindiff_plugin_name}
  bindiff_base
  bindiff_shared
  absl::str_format
  absl::time
  ${bindiff_plugin_libraries}
)
set_ida_target_properties(${bindiff_plugin_name} PROPERTIES
  POSITION_INDEPENDENT_CODE ON
  INTERPROCEDURAL_OPTIMIZATION ${_bindiff_ipo_supported}
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
  INTERPROCEDURAL_OPTIMIZATION ${_bindiff_ipo_supported})
install(TARGETS bindiff RUNTIME DESTINATION bindiff-prefix)
