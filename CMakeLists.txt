# Copyright 2011-2016 Google Inc. All Rights Reserved.
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

# BinExport release number. Increase every time any backwards incompatible
# change is made (usually when a new BinDiff or BinNavi version is released.
set(RELEASE 9)

list(APPEND CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR})
list(APPEND CMAKE_FIND_ROOT_PATH ${PROJECT_SOURCE_DIR}/third_party)

# Use a copy of a subset of Boost that we ship
set(Boost_NO_SYSTEM_PATHS, ON)
set(BOOST_ROOT ${PROJECT_SOURCE_DIR}/third_party/boost_parts)
find_package(Boost 1.55 REQUIRED)

find_package(Git)
find_package(IdaSdk REQUIRED)

set(OPENSSL_USE_STATIC_LIBS ON)
find_package(OpenSSL 1.0.1 REQUIRED)

set(PostgreSQL_TYPE_INCLUDE_DIR "none")  # We're not using the server-side API
find_package(PostgreSQL 9.3 REQUIRED)

# We ship Protocol Buffers as a sub-module and expect it to be built before
set(PROTOBUF_ROOT_FOLDER ${PROJECT_SOURCE_DIR}/third_party/protobuf)
set(PROTOBUF_SRC_ROOT_FOLDER ${PROTOBUF_ROOT_FOLDER}/src)
set(PROTOBUF_INCLUDE_DIR ${PROTOBUF_SRC_ROOT_FOLDER})
if(UNIX)
  set(PROTOBUF_PROTOC_EXECUTABLE ${PROTOBUF_SRC_ROOT_FOLDER}/protoc)
  set(PROTOBUF_LIBRARY ${PROTOBUF_SRC_ROOT_FOLDER}/.libs/libprotobuf.a)
else()
  set(PROTOBUF_PROTOC_EXECUTABLE
      ${PROTOBUF_ROOT_FOLDER}/build_msvc/Release/protoc.exe)
  set(PROTOBUF_LIBRARY
      ${PROTOBUF_ROOT_FOLDER}/build_msvc/Release/libprotobuf.lib)
endif()
find_package(Protobuf 3.0 REQUIRED)

include_directories(${PROJECT_SOURCE_DIR}
                    ${PROJECT_BINARY_DIR}  # To find generated protobufs
                    ${Boost_INCLUDE_DIR}
                    ${IdaSdk_INCLUDE_DIRS}
                    ${OPENSSL_INCLUDE_DIR}
                    ${PostgreSQL_INCLUDE_DIRS}
                    ${PROTOBUF_INCLUDE_DIRS}
                    ${PROTOBUF_SRC_ROOT_FOLDER})

if(UNIX)
  add_compile_options(--std=c++11 -Wno-deprecated)
  set(CMAKE_SKIP_BUILD_RPATH TRUE)
  if(NOT COMPILE_64BIT)
    add_compile_options(-m32)
    set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -m32")
  endif()
elseif(WIN32)
  # TODO(cblichmann): Configure compiler warnings for Windows
  if(MSVC)
    # Use the static runtime
    foreach(flag_var CMAKE_CXX_FLAGS
                     CMAKE_CXX_FLAGS_DEBUG
                     CMAKE_CXX_FLAGS_RELEASE
                     CMAKE_CXX_FLAGS_MINSIZEREL
                     CMAKE_CXX_FLAGS_RELWITHDEBINFO)
      if(${flag_var} MATCHES "/MD")
        string(REGEX REPLACE "/MD" "/MT" ${flag_var} "${${flag_var}}")
      endif()
      if(${flag_var} MATCHES "/MDd")
        string(REGEX REPLACE "/MDd" "/MTd" ${flag_var} "${${flag_var}}")
      endif()
    endforeach(flag_var)
  endif()
endif()

if(NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
  add_definitions(-DNDEBUG)
endif()

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
if(EXISTS "binexport.proto")
  list(APPEND BinExport_Proto_SRCS binexport.proto)
endif()
PROTOBUF_GENERATE_CPP(PROTO_SRCS PROTO_HDRS ${BinExport_Proto_SRCS})
if(EXISTS "binexport_header.cc")
  set(BinExport_Header_SRCS binexport_header.cc)
endif()
add_library(binexport_proto STATIC ${PROTO_SRCS} ${BinExport_Header_SRCS})

add_library(binexport_shared STATIC filesystem_util.cc)

add_library(binexport_plugin_shared STATIC hash.cc
                                           hex_codec.cc
                                           ida/digest.cc
                                           ida/log.cc)
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
                          ${PROTOBUF_LIBRARIES})
  if(APPLE)
    list(APPEND BinExport_LIBRARIES ${PostgreSQL_LIBRARY}
                                    krb5
                                    ldap
                                    ${OPENSSL_LIBRARIES})
  elseif(UNIX)
    list(APPEND BinExport_LIBRARIES ${PostgreSQL_LIBRARY_DIR}/libpq.a)
  elseif(WIN32)
    list(APPEND BinExport_LIBRARIES crypt32.lib
                                    secur32.lib
                                    shlwapi.lib
                                    ws2_32.lib
                                    wldap32.lib
                                    ${PostgreSQL_LIBRARY}
                                    ${PostgreSQL_LIBRARY_DIR}/libpgport.lib)
  endif()
  target_link_libraries(zynamics_binexport_9${_plx} ${BinExport_LIBRARIES})
  target_link_libraries(zynamics_binexport_9${_plx64} ${BinExport_LIBRARIES})
endif()
