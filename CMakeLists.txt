# Copyright 2011-2022 Google LLC
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

cmake_minimum_required(VERSION 3.14..3.22)
cmake_policy(VERSION 3.14)
project(binexport VERSION 12)  # Only major version is used

# BinExport settings
set(BINEXPORT_BINARY_DIR "${PROJECT_BINARY_DIR}" CACHE INTERNAL "" FORCE)
set(BINEXPORT_SOURCE_DIR "${PROJECT_SOURCE_DIR}" CACHE INTERNAL "" FORCE)

# CMake settings
set(CMAKE_EXPORT_COMPILE_COMMANDS TRUE)
list(APPEND CMAKE_MODULE_PATH "${BINEXPORT_SOURCE_DIR}/cmake")

# BinExport CMake modules, order matters
include(CompileOptions)
include(Util)
include(BinExportOptions)
include(BinExportDeps)

include(CTest)
if(BUILD_TESTING AND BINEXPORT_BUILD_TESTING)
  include(GoogleTest)
  enable_testing()
endif()

# Make Google-style includes work
set(_binexport_src "${BINEXPORT_BINARY_DIR}/src_include/third_party/zynamics")
set(_binexport_gen "${BINEXPORT_BINARY_DIR}/gen_include/third_party/zynamics")
create_directory_symlink("${BINEXPORT_SOURCE_DIR}" "${_binexport_src}/binexport")
create_directory_symlink("${absl_SOURCE_DIR}/absl" "${_binexport_src}/../absl")
create_directory_symlink("${BINEXPORT_BINARY_DIR}" "${_binexport_gen}/binexport")
# For Google3 workspaces
create_directory_symlink("${BINEXPORT_SOURCE_DIR}/third_party/boost_parts/boost" "${_binexport_src}/../boost/allowed")

# Find the Git revision number, if applicable
# TODO(cblichmann): Move this to Util.cmake
set(REVISION unknown)
if(NOT "$ENV{KOKORO_PIPER_CHANGELIST}" STREQUAL "")
  set(REVISION $ENV{KOKORO_PIPER_CHANGELIST})
elseif(GIT_FOUND)
  execute_process(COMMAND "${GIT_EXECUTABLE}" rev-parse --short HEAD
                  WORKING_DIRECTORY "${PROJECT_SOURCE_DIR}"
                  OUTPUT_VARIABLE REVISION ERROR_QUIET)
  if(NOT REVISION STREQUAL "")
    string(STRIP "${REVISION}" REVISION)
  else()
    set(REVISION internal)
  endif()
endif()

configure_file(version.cc.in version.cc ESCAPE_QUOTES @ONLY)

# Plugin names for IDA Pro and Binary Ninja
if(BINEXPORT_ENABLE_IDAPRO)
  set(binexport_ida_plugin_name binexport${binexport_VERSION_MAJOR}_ida)
endif()
if(BINEXPORT_ENABLE_BINARYNINJA)
  set(binexport_bn_plugin_name binexport${binexport_VERSION_MAJOR}_binaryninja)
endif()

# Interface library with include paths used by BinExport
add_library(binexport_base INTERFACE)
target_include_directories(binexport_base INTERFACE
  ${PROJECT_SOURCE_DIR}
  ${PROJECT_SOURCE_DIR}/stubs
  ${PROJECT_BINARY_DIR}/gen_include
  ${PROJECT_BINARY_DIR}/src_include
  ${Boost_INCLUDE_DIR}
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
  architectures.h
  binexport.cc
  binexport.h
  util/filesystem.cc
  util/filesystem.h
  util/format.cc
  util/format.h
  util/hash.cc
  util/hash.h
  util/idb_export.cc
  util/idb_export.h
  util/logging.cc
  util/logging.h
  util/nested_iterator.h
  util/process.cc
  util/process.h
  util/range.h
  util/status_macros.h
  util/timer.h
  util/types.h
)
target_link_libraries(binexport_shared PUBLIC
  absl::bad_optional_access
  absl::check
  absl::flat_hash_map
  absl::flat_hash_set
  absl::log
  absl::log_initialize
  absl::optional
  absl::status
  absl::statusor
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
if(BUILD_TESTING AND BINEXPORT_BUILD_TESTING)
  add_executable(binexport_shared_test
    util/filesystem_test.cc
    util/format_test.cc
    util/process_test.cc
    util/status_macros_test.cc
    util/status_matchers.h
    util/timer_test.cc
  )
  target_link_libraries(binexport_shared_test PUBLIC
    binexport_test_base
    binexport_shared
  )
  gtest_discover_tests(binexport_shared_test)
endif()

# Test helper library
add_library(binexport_testing
  testing.cc
  testing.h
)
target_link_libraries(binexport_testing PUBLIC
  binexport_test_base
  binexport_shared
)

# IDB export test
if(BUILD_TESTING AND BINEXPORT_BUILD_TESTING)
  add_executable(idb_export_test util/idb_export_test.cc)
  target_link_libraries(idb_export_test
    binexport_test_base
    binexport_shared
  )
  gtest_discover_tests(idb_export_test PROPERTIES
    ENVIRONMENT "TEST_TMPDIR=${PROJECT_BINARY_DIR}/idb_export_test_tmp"
  )
endif()

# General BinExport tests
if(BUILD_TESTING AND BINEXPORT_BUILD_TESTING)
  add_executable(binexport_test
    binexport_test.cc
  )
  target_link_libraries(binexport_test PUBLIC
    binexport_test_base
    binexport_shared
    absl::memory
  )
  gtest_discover_tests(binexport_test)
endif()

# binexport2dump tool
add_subdirectory(tools)

# Non-plugin code shared with BinDiff/the Binary Ninja plugin
add_library(binexport_core
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
  dump_writer.cc
  dump_writer.h
  edge.cc
  edge.h
  entry_point.cc
  entry_point.h
  expression.cc
  expression.h
  flow_analysis.cc
  flow_analysis.h
  flow_graph.cc
  flow_graph.h
  function.cc
  function.h
  instruction.cc
  instruction.h
  library_manager.cc
  library_manager.h
  operand.cc
  operand.h
  statistics_writer.cc
  statistics_writer.h
  version.h
  ${CMAKE_CURRENT_BINARY_DIR}/version.cc
  virtual_memory.cc
  virtual_memory.h
  x86_nop.cc
  x86_nop.h
)
target_link_libraries(binexport_core PUBLIC
  absl::bad_optional_access
  absl::btree
  absl::check
  absl::flat_hash_map
  absl::flat_hash_set
  absl::hash
  absl::node_hash_map
  absl::node_hash_set
  absl::log
  absl::optional
  absl::strings
  absl::time
  binexport_shared
)

# IDA Pro plugins
if(BINEXPORT_ENABLE_IDAPRO)
  add_subdirectory(ida)
endif()

# Binary Ninja plugin
if(BINEXPORT_ENABLE_BINARYNINJA)
  add_subdirectory(binaryninja)
endif()

# BinExport reader library
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
  absl::check
  absl::inlined_vector
  absl::log
  absl::strings
  binexport_shared
)
if(BUILD_TESTING AND BINEXPORT_BUILD_TESTING)
  add_executable(binexport_reader_test
    reader/call_graph_test.cc
    reader/flow_graph_test.cc
    reader/graph_utility_test.cc
    reader/instruction_test.cc
  )
  target_link_libraries(binexport_reader_test PUBLIC
    gtest_main
    gmock
    binexport_reader
    binexport_testing
  )
  gtest_discover_tests(binexport_reader_test PROPERTIES
    ENVIRONMENT "TEST_SRCDIR=${_binexport_src}"
  )
endif()
