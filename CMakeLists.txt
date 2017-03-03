# Copyright 2011-2017 Google Inc. All Rights Reserved.
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

cmake_minimum_required(VERSION 2.8 FATAL_ERROR)
project(binexport C CXX)

list(APPEND CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR})
include(ExternalProject)
include(CompileOptions)
include(ExternalGTest)
include(ExternalOpenSSL)
include(ExternalPostgreSQL)
include(ExternalProtobuf)

# BinExport release number. Increase every time any backwards incompatible
# change is made (usually when a new BinDiff or BinNavi version is released.
set(RELEASE 9)

# Use a copy of a subset of Boost that we ship
set(Boost_NO_SYSTEM_PATHS ON)
set(BOOST_ROOT ${PROJECT_SOURCE_DIR}/third_party/boost_parts)
find_package(Boost 1.55 REQUIRED)

find_package(Git)

# Find and configure the IDA SDK. Copying allins.hpp to ins/pc.hpp is a
# workaround to make intel.hpp compile (changed in the 6.95 SDK).
find_package(IdaSdk REQUIRED)
file(COPY ${IdaSdk_DIR}/include/allins.hpp
     DESTINATION ${PROJECT_BINARY_DIR}/ins)
file(RENAME ${PROJECT_BINARY_DIR}/ins/allins.hpp
            ${PROJECT_BINARY_DIR}/ins/pc.hpp)

# Make Google-style includes work
file(MAKE_DIRECTORY ${PROJECT_BINARY_DIR}/third_party/zynamics)
if(UNIX)
  execute_process(COMMAND
    ${CMAKE_COMMAND} -E create_symlink ../../..
    ${PROJECT_BINARY_DIR}/third_party/zynamics/binexport)
elseif(WIN32)
  file(TO_NATIVE_PATH
    ${PROJECT_SOURCE_DIR}/third_party/zynamics/binexport _be_from)
  file(TO_NATIVE_PATH ${PROJECT_BINARY_DIR} _be_to)
  execute_process(COMMAND $ENV{ComSpec} /c mklink /J ${_be_from} ${_be_to}
    ERROR_QUIET)
endif()

enable_testing()

include_directories(${PROJECT_SOURCE_DIR}
                    ${PROJECT_BINARY_DIR}  # To find generated protobufs
                    ${BinExport_EXTRA_INCLUDE_DIR}
                    ${Boost_INCLUDE_DIR}
                    ${GTEST_INCLUDE_DIRS}
                    ${OPENSSL_INCLUDE_DIR}
                    ${PostgreSQL_INCLUDE_DIR}
                    ${PROTOBUF_INCLUDE_DIRS})

# Find the Git revision number, if applicable
set(REVISION unknown)
if(GIT_FOUND)
  execute_process(COMMAND ${GIT_EXECUTABLE} rev-parse --short HEAD
                  OUTPUT_VARIABLE REVISION
                  ERROR_QUIET)
  if(NOT ${REVISION} STREQUAL "")
    string(STRIP ${REVISION} REVISION)
  else()
    set(REVISION internal)
  endif()
endif()

configure_file(version.h.in version.h ESCAPE_QUOTES @ONLY)

set(BinExport_Proto_SRCS binexport2.proto)
PROTOBUF_GENERATE_CPP(PROTO_SRCS PROTO_HDRS ${BinExport_Proto_SRCS})
add_library(binexport_proto STATIC ${PROTO_SRCS})
add_dependencies(binexport_proto ${PROTOBUF_LIBRARIES})

add_library(binexport_shared STATIC filesystem_util.cc)
add_dependencies(binexport_shared ${PROTOBUF_LIBRARIES})
add_executable(binexport_shared_test filesystem_util_test.cc
                                     timer_test.cc)
set(binexport_shared_test_LIBRARIES binexport_shared
                                    ${GTEST_BOTH_LIBRARIES}
                                    ${PROTOBUF_LIBRARIES})
if(WIN32)
  list(APPEND binexport_shared_test_LIBRARIES shlwapi.lib)
endif()
target_link_libraries(binexport_shared_test ${binexport_shared_test_LIBRARIES})
add_test(AllTestsInBinExportShared binexport_shared_test)

add_subdirectory(tools)

if(NOT COMPILE_64BIT)
  add_library(binexport_plugin_shared STATIC hash.cc
                                             hash.h
                                             hex_codec.cc
                                             hex_codec.h
                                             ida/digest.cc
                                             ida/digest.h
                                             ida/log.cc
                                             ida/log.h)
  add_dependencies(binexport_plugin_shared ${OPENSSL_LIBRARIES}
                                           ${PROTOBUF_LIBRARIES})
  target_compile_definitions(binexport_plugin_shared PUBLIC
                             ${IdaSdk_PLATFORM}
                             __IDP__
                             USE_DANGEROUS_FUNCTIONS
                             USE_STANDARD_FILE_FUNCTIONS)

  set(_be_plugin_name zynamics_binexport_${RELEASE})
  add_ida_plugin(${_be_plugin_name} EA64 address_references.cc
                                         address_references.h
                                         base_types.cc
                                         base_types.h
                                         basic_block.cc
                                         basic_block.h
                                         binexport2_writer.cc
                                         binexport2_writer.h
                                         call_graph.cc
                                         call_graph.h
                                         chain_writer.cc
                                         chain_writer.h
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
                                         ida/ui.cc
                                         ida/ui.h
                                         instruction.cc
                                         instruction.h
                                         library_manager.cc
                                         library_manager.h
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
                                         x86_nop.h)
  target_include_directories(${_be_plugin_name}${_plx} PUBLIC ${IdaSdk_INCLUDE_DIRS})
  target_include_directories(${_be_plugin_name}${_plx64} PUBLIC ${IdaSdk_INCLUDE_DIRS})
  target_include_directories(binexport_plugin_shared PUBLIC ${IdaSdk_INCLUDE_DIRS})

  set(BinExport_LIBRARIES ${IdaSdk_LIBRARIES}
                          binexport_proto
                          binexport_shared
                          binexport_plugin_shared
                          ${PostgreSQL_LIBRARIES}
                          ${PROTOBUF_LIBRARIES}
                          # OpenSSL must come after PostgreSQL
                          ${OPENSSL_LIBRARIES})
  if(WIN32)
    list(APPEND BinExport_LIBRARIES crypt32.lib
                                    secur32.lib
                                    shlwapi.lib
                                    ws2_32.lib
                                    wldap32.lib)
  endif()
  target_link_libraries(${_be_plugin_name}${_plx} ${BinExport_LIBRARIES})
  target_link_libraries(${_be_plugin_name}${_plx64} ${BinExport_LIBRARIES})
endif()
