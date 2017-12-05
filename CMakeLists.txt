# CMake build file for BinDiff. This file relies on the open source version of
# BinExport a lot.
cmake_minimum_required(VERSION 3.7)
cmake_policy(VERSION 3.7)
project(bindiff VERSION 5.0.0)

set(BinExport_DIR ${PROJECT_SOURCE_DIR}/../binexport)
set(Google3_DIR ${PROJECT_SOURCE_DIR}/../../..)
set(ThirdParty_DIR ${PROJECT_SOURCE_DIR}/../../../third_party)

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
include(${BinExport_DIR}/cctz.cmake)
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
  ${cctz_src_dir}/include
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
)

protobuf_generate_cpp(binexport2_proto binexport2_proto_h
  ${BinExport_DIR}/binexport2.proto
)
add_library(binexport_shared STATIC
  ${binexport2_proto_h}
  ${binexport2_proto}
  ${BinExport_DIR}/filesystem_util.cc
  ${BinExport_DIR}/filesystem_util.h
  ${BinExport_DIR}/hash.cc
  ${BinExport_DIR}/hash.h
  ${BinExport_DIR}/hex_codec.cc
  ${BinExport_DIR}/hex_codec.h
)
target_link_libraries(binexport_shared
  bindiff_base
  absl::strings
  OpenSSL::SSL
)
if(WIN32)
  target_link_libraries(binexport_shared shlwapi.lib)
endif()

# XML configuration library
add_library(xmlconfig STATIC xmlconfig.cc
                             xmlconfig.h)
target_link_libraries(xmlconfig binexport_shared
                                tinyxpath)

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
  call_graph_matching.cc
  call_graph_matching.h
  change_classifier.cc
  change_classifier.h
  comments.cc
  comments.h
  database_writer.cc
  database_writer.h
  differ.cc
  differ.h
  fixed_points.cc
  fixed_points.h
  flow_graph.cc
  flow_graph.h
  flow_graph_matching.cc
  flow_graph_matching.h
  groundtruth_writer.cc
  groundtruth_writer.h
  instruction.cc
  instruction.h
  log_writer.cc
  log_writer.cc
  matching.cc
  matching.h
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

add_library(binexport_plugin_shared STATIC ${BinExport_DIR}/ida/digest.cc
                                           ${BinExport_DIR}/ida/digest.h
                                           ${BinExport_DIR}/ida/log.cc
                                           ${BinExport_DIR}/ida/log.h
                                           ${BinExport_DIR}/ida/ui.cc
                                           ${BinExport_DIR}/ida/ui.h)
target_compile_definitions(binexport_plugin_shared PUBLIC
                           ${IdaSdk_PLATFORM}
                           __IDP__
                           USE_DANGEROUS_FUNCTIONS
                           USE_STANDARD_FILE_FUNCTIONS)
target_include_directories(binexport_plugin_shared PUBLIC
  ${IdaSdk_INCLUDE_DIRS}
)
target_link_libraries(binexport_plugin_shared
  bindiff_base
  OpenSSL::SSL
)

# TODO(cblichmann): Commented out the following since the IDA plugin is not
#                   ready yet.
#[[
# Plugins will be named "bindiff50.so" and "bindiff5064.so"
set(_bindiff_plugin_name
  bindiff${PROJECT_VERSION_MAJOR}${PROJECT_VERSION_MINOR})
add_ida_plugin(${_bindiff_plugin_name} EA64
  ida/main_plugin.cc
  ida/names.cc
  ida/names.h
  ida/results.cc
  ida/results.h
  ida/ui.cc
  ida/ui.h
  ida/visual_diff.cc
  ida/visual_diff.h
)

set(bindiff_plugin_libraries binexport_plugin_shared
                             bindiff)
if(WIN32)
  list(APPEND Plugin_LIBRARIES ws2_32.lib)
endif()
target_link_libraries(${_bindiff_plugin_name}${_plx}
  ${bindiff_plugin_libraries}
)
target_link_libraries(${_bindiff_plugin_name}${_plx64}
  ${bindiff_plugin_libraries}
)
set_target_properties(
  ${bindiff_plugin_name}${_plx}
  ${bindiff_plugin_name}${_plx64}
  PROPERTIES POSITION_INDEPENDENT_CODE ON
)
install(TARGETS ${bindiff_plugin_name}${_plx}
                ${bindiff_plugin_name}${_plx64}
        ARCHIVE DESTINATION bindiff-prefix
        RUNTIME DESTINATION bindiff-prefix
        LIBRARY DESTINATION bindiff-prefix)
]]

add_executable(bindiff main_portable.cc)
target_link_libraries(bindiff
  bindiff_shared
  absl::time
  gflags
)
install(TARGETS bindiff RUNTIME DESTINATION bindiff-prefix)
