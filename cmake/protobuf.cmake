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

include(ExternalProject)

if(CMAKE_SYSTEM_NAME STREQUAL "Linux")
  # Clang also understands these
  set(extra_EXE_LINKER_FLAGS "-static-libgcc -static-libstdc++")
endif()

ExternalProject_Add(protobuf
  GIT_REPOSITORY https://github.com/protocolbuffers/protobuf.git
  GIT_TAG    v3.12.4 # 2020-07-10
  PREFIX     "${CMAKE_CURRENT_BINARY_DIR}/protobuf"
  SOURCE_SUBDIR cmake
  CMAKE_ARGS -DCMAKE_INSTALL_PREFIX:PATH=<INSTALL_DIR>
             -DCMAKE_BUILD_TYPE=@CMAKE_BUILD_TYPE@
             -DCMAKE_POSITION_INDEPENDENT_CODE=ON
             -DCMAKE_EXE_LINKER_FLAGS=${extra_EXE_LINKER_FLAGS}
             -Dprotobuf_BUILD_TESTS=OFF
             -Dprotobuf_BUILD_SHARED_LIBS=OFF
             -Dprotobuf_WITH_ZLIB=OFF
)
ExternalProject_Get_Property(protobuf INSTALL_DIR)
list(APPEND CMAKE_PREFIX_PATH ${INSTALL_DIR})
