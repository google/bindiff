# Copyright 2011-2018 Google LLC. All Rights Reserved.
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

include(ExternalProject)
ExternalProject_Add(protobuf
  URL https://github.com/google/protobuf/archive/v3.4.1.tar.gz
  URL_HASH SHA256=8e0236242106e680b4f9f576cc44b8cd711e948b20a9fc07769b0a20ceab9cc4
  PREFIX ${CMAKE_CURRENT_BINARY_DIR}/protobuf
  SOURCE_SUBDIR cmake
  CMAKE_ARGS -DCMAKE_INSTALL_PREFIX:PATH=<INSTALL_DIR>
             -DCMAKE_BUILD_TYPE=@CMAKE_BUILD_TYPE@
             -DCMAKE_POSITION_INDEPENDENT_CODE=ON
             -Dprotobuf_BUILD_TESTS=OFF
             -Dprotobuf_BUILD_SHARED_LIBS=OFF
             -Dprotobuf_WITH_ZLIB=OFF
)
set(_pb_stubs_src "<SOURCE_DIR>/src/google/protobuf/stubs")
set(_pb_stubs_inst "<INSTALL_DIR>/include/google/protobuf/stubs")
ExternalProject_Add_Step(protobuf copy-stub-headers COMMAND
  "${CMAKE_COMMAND}" -E copy "${_pb_stubs_src}/status_macros.h" "${_pb_stubs_inst}/" &&
  "${CMAKE_COMMAND}" -E copy "${_pb_stubs_src}/statusor.h" "${_pb_stubs_inst}/" &&
  "${CMAKE_COMMAND}" -E copy "${_pb_stubs_src}/stringprintf.h" "${_pb_stubs_inst}/" &&
  "${CMAKE_COMMAND}" -E copy "${_pb_stubs_src}/strutil.h" "${_pb_stubs_inst}/"
    DEPENDEES install)
ExternalProject_Get_Property(protobuf INSTALL_DIR)
list(APPEND CMAKE_PREFIX_PATH ${INSTALL_DIR})
