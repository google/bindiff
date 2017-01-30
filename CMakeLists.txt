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
include(ExternalOpenSSL)
include(ExternalPostgreSQL)
include(ExternalProtobuf)
include(CompileOptions)

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
if(UNIX)
  execute_process(COMMAND
    ${CMAKE_COMMAND} -E create_symlink ../..
    ${PROJECT_SOURCE_DIR}/third_party/zynamics/binexport)
elseif(WIN32)
  file(TO_NATIVE_PATH
    ${PROJECT_SOURCE_DIR}/third_party/zynamics/binexport _from)
  file(TO_NATIVE_PATH ${PROJECT_SOURCE_DIR} _to)
  execute_process(COMMAND $ENV{ComSpec} /c mklink /J ${_from} ${_to}
    ERROR_QUIET)
endif()

include_directories(${PROJECT_SOURCE_DIR}
                    ${PROJECT_BINARY_DIR}  # To find generated protobufs
                    ${BinExport_EXTRA_INCLUDE_DIR}
                    ${Boost_INCLUDE_DIR}
                    ${IdaSdk_INCLUDE_DIRS}
                    ${OPENSSL_INCLUDE_DIR}
                    ${PostgreSQL_INCLUDE_DIR}
                    ${PROTOBUF_INCLUDE_DIRS}
                    # Add the Protobuf source tree itself, as we use its
                    # infrastructure for things like StringPiece etc.
                    ${PROTOBUF_SOURCE_DIR}/src)

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

add_library(binexport_plugin_shared STATIC hash.cc
                                           hex_codec.cc
                                           ida/digest.cc
                                           ida/log.cc)
add_dependencies(binexport_plugin_shared ${OPENSSL_LIBRARIES}
                                         ${PROTOBUF_LIBRARIES})
target_compile_definitions(binexport_plugin_shared PUBLIC
                           ${IdaSdk_PLATFORM}
                           __IDP__
                           USE_DANGEROUS_FUNCTIONS
                           USE_STANDARD_FILE_FUNCTIONS)

add_subdirectory(tools)

if(NOT COMPILE_64BIT)
  add_ida_plugin(zynamics_binexport_9 EA64
                 address_references.cc
                 base_types.cc
                 basic_block.cc
                 binexport2_writer.cc
                 call_graph.cc
                 chain_writer.cc
                 comment.cc
                 database_writer.cc
                 dump_writer.cc
                 edge.cc
                 entry_point.cc
                 expression.cc
                 flow_analyzer.cc
                 flow_graph.cc
                 function.cc
                 ida/arm.cc
                 ida/dalvik.cc
                 ida/generic.cc
                 ida/main_plugin.cc
                 ida/metapc.cc
                 ida/mips.cc
                 ida/names.cc
                 ida/ppc.cc
                 ida/types_container.cc
                 instruction.cc
                 library_manager.cc
                 operand.cc
                 postgresql.cc
                 query_builder.cc
                 statistics_writer.cc
                 type_system.cc
                 version.h
                 virtual_memory.cc
                 x86_nop.cc)

  set(BinExport_LIBRARIES ${IdaSdk_LIBRARIES}
                          binexport_proto
                          binexport_shared
                          binexport_plugin_shared
                          ${OPENSSL_LIBRARIES}
                          ${PostgreSQL_LIBRARIES}
                          ${PROTOBUF_LIBRARIES})
  if(WIN32)
    list(APPEND BinExport_LIBRARIES crypt32.lib
                                    secur32.lib
                                    shlwapi.lib
                                    ws2_32.lib
                                    wldap32.lib
                                    ${PostgreSQL_LIBRARIES})
  endif()
  target_link_libraries(zynamics_binexport_9${_plx} ${BinExport_LIBRARIES})
  target_link_libraries(zynamics_binexport_9${_plx64} ${BinExport_LIBRARIES})
endif()
