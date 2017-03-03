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

find_path(PROTOBUF_ROOT_DIR
  NAMES include/google/protobuf/message_lite.h
  PATHS ${CMAKE_CURRENT_BINARY_DIR}/protobuf
  DOC "Location of the Protocol Buffers source tree"
  NO_DEFAULT_PATH)

add_library(protobuf STATIC IMPORTED)

if(EXISTS ${PROTOBUF_ROOT_DIR})
  message("-- Found Protobuf: ${PROTOBUF_ROOT_DIR}")
else()
  set(PROTOBUF_SOURCE_DIR
    ${CMAKE_CURRENT_BINARY_DIR}/src/external-protobuf)
  set(PROTOBUF_URL
    https://github.com/google/protobuf/archive/v3.2.0rc2.tar.gz)
  set(PROTOBUF_URL_HASH
    SHA256=82b124816eb8e9e99e8312b6c751c68ec690e2b1eaef7fa2c20743152367ec80)
  set(PROTOBUF_ROOT_DIR ${CMAKE_CURRENT_BINARY_DIR}/protobuf)

  if(UNIX AND NOT COMPILE_64BIT)
    set(_pb_flags -DCMAKE_C_FLAGS=-m32
                  -DCMAKE_CXX_FLAGS=-m32)
  endif()

  ExternalProject_Add(external-protobuf
    URL ${PROTOBUF_URL}
    URL_HASH ${PROTOBUF_URL_HASH}
    PREFIX ${CMAKE_CURRENT_BINARY_DIR}
    SOURCE_DIR ${PROTOBUF_SOURCE_DIR}
    # For CMake 3.7+ use:
    # SOURCE_SUBDIR ${PROTOBUF_SOURCE_DIR}/cmake
    # CMAKE_ARGS ...
    CONFIGURE_COMMAND cd "<BINARY_DIR>" &&
      "${CMAKE_COMMAND}" "<SOURCE_DIR>/cmake"
      -G "${CMAKE_GENERATOR}"
      -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
      "-DCMAKE_INSTALL_PREFIX=${PROTOBUF_ROOT_DIR}"
      -DCMAKE_INSTALL_LIBDIR=lib
      -DCMAKE_RULE_MESSAGES=OFF
      ${_pb_flags}
      -Dprotobuf_BUILD_TESTS=OFF
      -Dprotobuf_BUILD_SHARED_LIBS=OFF
      -Dprotobuf_WITH_ZLIB=OFF
    BUILD_COMMAND ""
    INSTALL_COMMAND "${CMAKE_COMMAND}"
      --build "<BINARY_DIR>" --config ${CMAKE_BUILD_TYPE} --target install)
  ExternalProject_Add_Step(external-protobuf copy-stub-headers COMMAND
    "${CMAKE_COMMAND}" -E copy
      "${PROTOBUF_SOURCE_DIR}/src/google/protobuf/stubs/stringprintf.h"
      "${PROTOBUF_ROOT_DIR}/include/google/protobuf/stubs/" &&
    "${CMAKE_COMMAND}" -E copy
      "${PROTOBUF_SOURCE_DIR}/src/google/protobuf/stubs/strutil.h"
      "${PROTOBUF_ROOT_DIR}/include/google/protobuf/stubs/" DEPENDEES install)
  add_dependencies(protobuf external-protobuf)
endif()

set(PROTOBUF_INCLUDE_DIRS ${PROTOBUF_ROOT_DIR}/include)
set(PROTOBUF_LIBRARIES protobuf)

if(WIN32)
  set_property(TARGET protobuf
    PROPERTY IMPORTED_LOCATION ${PROTOBUF_ROOT_DIR}/lib/libprotobuf.lib)
  set(PROTOBUF_PROTOC_EXECUTABLE ${PROTOBUF_ROOT_DIR}/bin/protoc.exe)
elseif(UNIX)
  set_property(TARGET protobuf
    PROPERTY IMPORTED_LOCATION ${PROTOBUF_ROOT_DIR}/lib/libprotobuf.a)
  set(PROTOBUF_PROTOC_EXECUTABLE ${PROTOBUF_ROOT_DIR}/bin/protoc)
endif()

# Code below copied from CMake's own FindProtobuf.cmake

# By default have PROTOBUF_GENERATE_CPP macro pass -I to protoc
# for each directory where a proto file is referenced.
if(NOT DEFINED PROTOBUF_GENERATE_CPP_APPEND_PATH)
  set(PROTOBUF_GENERATE_CPP_APPEND_PATH TRUE)
endif()

function(PROTOBUF_GENERATE_CPP SRCS HDRS)
  if(NOT ARGN)
    message(SEND_ERROR "Error: PROTOBUF_GENERATE_CPP() called without any proto files")
    return()
  endif()

  if(PROTOBUF_GENERATE_CPP_APPEND_PATH)
    # Create an include path for each file specified
    foreach(FIL ${ARGN})
      get_filename_component(ABS_FIL ${FIL} ABSOLUTE)
      get_filename_component(ABS_PATH ${ABS_FIL} PATH)
      list(FIND _protobuf_include_path ${ABS_PATH} _contains_already)
      if(${_contains_already} EQUAL -1)
          list(APPEND _protobuf_include_path -I ${ABS_PATH})
      endif()
    endforeach()
  else()
    set(_protobuf_include_path -I ${CMAKE_CURRENT_SOURCE_DIR})
  endif()

  if(DEFINED PROTOBUF_IMPORT_DIRS)
    foreach(DIR ${PROTOBUF_IMPORT_DIRS})
      get_filename_component(ABS_PATH ${DIR} ABSOLUTE)
      list(FIND _protobuf_include_path ${ABS_PATH} _contains_already)
      if(${_contains_already} EQUAL -1)
          list(APPEND _protobuf_include_path -I ${ABS_PATH})
      endif()
    endforeach()
  endif()

  set(${SRCS})
  set(${HDRS})
  foreach(FIL ${ARGN})
    get_filename_component(ABS_FIL ${FIL} ABSOLUTE)
    get_filename_component(FIL_WE ${FIL} NAME_WE)

    list(APPEND ${SRCS} "${CMAKE_CURRENT_BINARY_DIR}/${FIL_WE}.pb.cc")
    list(APPEND ${HDRS} "${CMAKE_CURRENT_BINARY_DIR}/${FIL_WE}.pb.h")

    add_custom_command(
      OUTPUT "${CMAKE_CURRENT_BINARY_DIR}/${FIL_WE}.pb.cc"
             "${CMAKE_CURRENT_BINARY_DIR}/${FIL_WE}.pb.h"
      COMMAND  ${PROTOBUF_PROTOC_EXECUTABLE}
      ARGS --cpp_out  ${CMAKE_CURRENT_BINARY_DIR} ${_protobuf_include_path} ${ABS_FIL}
      DEPENDS ${ABS_FIL}
      COMMENT "Running C++ protocol buffer compiler on ${FIL}"
      VERBATIM )
  endforeach()

  set_source_files_properties(${${SRCS}} ${${HDRS}} PROPERTIES GENERATED TRUE)
  set(${SRCS} ${${SRCS}} PARENT_SCOPE)
  set(${HDRS} ${${HDRS}} PARENT_SCOPE)
endfunction()
