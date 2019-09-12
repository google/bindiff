# Copyright 2011-2019 Google LLC. All Rights Reserved.
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
  URL https://github.com/google/protobuf/archive/v3.6.1.tar.gz
  URL_HASH SHA256=3d4e589d81b2006ca603c1ab712c9715a76227293032d05b26fca603f90b3f5b
  PREFIX ${CMAKE_CURRENT_BINARY_DIR}/protobuf
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
