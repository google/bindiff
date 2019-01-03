# CMake build file for BinDiff. This file relies on the open source version of
# BinExport a lot.
cmake_minimum_required(VERSION 3.7)
if(POLICY CMP0069)  # For CheckIPOSupported
  cmake_policy(SET CMP0069 NEW)
endif()
project(bindiff VERSION 5.0.0)

set(BinExport_DIR ${PROJECT_SOURCE_DIR}/../binexport)
set(Google3_DIR ${PROJECT_SOURCE_DIR}/../../..)
set(ThirdParty_DIR ${PROJECT_SOURCE_DIR}/../../../third_party)

# CheckIPOSupported requires at least CMake 3.9
if(CMAKE_VERSION VERSION_GREATER_EQUAL 3.9)
  include(CheckIPOSupported)
  check_ipo_supported(RESULT _bindiff_ipo_supported)
endif()

list(APPEND CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR} ${BinExport_DIR})
include(${BinExport_DIR}/copts.cmake)
include(${BinExport_DIR}/util.cmake)
include(gflags.cmake)
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

configure_file(version.h.in version.h ESCAPE_QUOTES @ONLY)

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
  ${BinExport_DIR}/util/filesystem.cc
  ${BinExport_DIR}/util/filesystem.h
  ${BinExport_DIR}/util/format.cc
  ${BinExport_DIR}/util/format.h
  ${BinExport_DIR}/util/timer.h
)
target_link_libraries(binexport_shared
  bindiff_base
  absl::strings
  absl::time
  OpenSSL::SSL
)
if(WIN32)
  target_link_libraries(binexport_shared shlwapi.lib)
endif()
set_target_properties(binexport_shared PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${_bindiff_ipo_supported})

# XML configuration library
add_library(xmlconfig STATIC xmlconfig.cc
                             xmlconfig.h)
target_link_libraries(xmlconfig binexport_shared
                                tinyxpath)
set_target_properties(xmlconfig PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${_bindiff_ipo_supported})

add_executable(xmlconfig_test xmlconfig_test.cc)
target_link_libraries(xmlconfig_test
  bindiff_tests_base
  xmlconfig
)
add_test(AllTestsInXmlConfig xmlconfig_test)

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
  flow_graph_match_basic_block_call_refs.h
  flow_graph_match_basic_block_edges_lengauer_tarjan.h
  flow_graph_match_basic_block_edges_mdindex.h
  flow_graph_match_basic_block_edges_prime.h
  flow_graph_match_basic_block_entry_node.h
  flow_graph_match_basic_block_hash.h
  flow_graph_match_basic_block_instruction_count.h
  flow_graph_match_basic_block_jump_sequence.h
  flow_graph_match_basic_block_loop_entry.h
  flow_graph_match_basic_block_mdindex_relaxed.h
  flow_graph_match_basic_block_prime.h
  flow_graph_match_basic_block_self_loop.h
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
  utility.cc
  utility.h
  writer.cc
  writer.h
)
target_link_libraries(bindiff_shared
  absl::strings
  binexport_shared
  sqlite
  xmlconfig)
set_target_properties(bindiff_shared PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${_bindiff_ipo_supported})

add_executable(utility_test utility_test.cc)
target_link_libraries(utility_test
  bindiff_tests_base
  bindiff_shared
)
add_test(AllTestsInUtility utility_test)

# Plugins will be named "bindiff50.so" and "bindiff5064.so"
set(bindiff_plugin_name
  bindiff${PROJECT_VERSION_MAJOR}${PROJECT_VERSION_MINOR})
add_ida_plugin(${bindiff_plugin_name}
  ${BinExport_DIR}/ida/log.cc
  ${BinExport_DIR}/ida/log.h
  ${BinExport_DIR}/ida/ui.cc
  ${BinExport_DIR}/ida/ui.h
  ${BinExport_DIR}/ida/digest.cc
  ${BinExport_DIR}/ida/digest.h
  ida/bindiff_icon.cc
  ida/bindiff_icon.h
  ida/main_plugin.cc
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
  absl::time
  gflags
)
set_target_properties(bindiff PROPERTIES
  INTERPROCEDURAL_OPTIMIZATION ${_bindiff_ipo_supported})
install(TARGETS bindiff RUNTIME DESTINATION bindiff-prefix)
