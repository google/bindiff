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

cmake_minimum_required(VERSION 3.7)

project(binexport-super NONE)

list(APPEND CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR}
                              ${PROJECT_SOURCE_DIR}/..)
include(ExternalProject)

include(absl.cmake)
include(cctz.cmake)
include(googletest.cmake)
include(openssl.cmake)
include(postgresql.cmake)  # Depends on openssl
include(protobuf.cmake)

find_package(IdaSdk REQUIRED)

string(REPLACE ";" "^^" CMAKE_PREFIX_PATH_ALT_SEP "${CMAKE_PREFIX_PATH}")
ExternalProject_Add(binexport
  DEPENDS
    absl
    cctz
    googletest
    openssl
    postgresql
    protobuf
  PREFIX ${CMAKE_CURRENT_BINARY_DIR}
  SOURCE_DIR ${PROJECT_SOURCE_DIR}/..
  LIST_SEPARATOR ^^
  CMAKE_ARGS -DCMAKE_INSTALL_PREFIX:PATH=<INSTALL_DIR>
             "-DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH_ALT_SEP}"
             "-DABSL_ROOT_DIR=${CMAKE_CURRENT_BINARY_DIR}/absl"
             "-DCCTZ_ROOT_DIR=${CMAKE_CURRENT_BINARY_DIR}/cctz"
             "-DGOOGLETEST_ROOT_DIR=${CMAKE_CURRENT_BINARY_DIR}/googletest"
             "-DIdaSdk_DIR=${IdaSdk_ROOT_DIR}"
             "-DBUILD_TESTING=OFF"
)
