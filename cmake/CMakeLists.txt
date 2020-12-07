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

cmake_minimum_required(VERSION 3.12)

project(binexport-super NONE)

list(APPEND CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR}
                              ${PROJECT_SOURCE_DIR}/..)
include(ExternalProject)

include(BinExportDeps.cmake)
include(openssl.cmake)
include(postgresql.cmake)  # Depends on OpenSSL

find_package(IdaSdk REQUIRED)

option(BINEXPORT_ENABLE_POSTGRESQL "Enable export to PostresSQL databases" OFF)

string(REPLACE ";" "^^" CMAKE_PREFIX_PATH_ALT_SEP "${CMAKE_PREFIX_PATH}")
ExternalProject_Add(binexport
  DEPENDS postgresql
  PREFIX ${CMAKE_CURRENT_BINARY_DIR}
  SOURCE_DIR ${PROJECT_SOURCE_DIR}/..
  LIST_SEPARATOR ^^
  CMAKE_ARGS -DCMAKE_INSTALL_PREFIX:PATH=<INSTALL_DIR>
             "-DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH_ALT_SEP}"
             "-DIdaSdk_DIR=${IdaSdk_DIR}"
             "-DBINEXPORT_ENABLE_POSTGRESQL=${BINEXPORT_ENABLE_POSTGRESQL}"
             "-DBUILD_TESTING=OFF"
)
