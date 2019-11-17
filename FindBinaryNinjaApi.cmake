# Copyright 2019 Google LLC. All Rights Reserved.
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

# FindBinaryNinjaApi
# ------------------
#
# Locates and configures the Binary Ninja API. Needs version 1.2.1921 or
# later.
#
# Use this module by invoking find_package with the form:
#
#   find_package(BinaryNinjaApi
#                [REQUIRED]  # Stop the build if Binary Ninja API is not found
#               )
#
# Defines the following variables:
#
#   BinaryNinjaApi_INCLUDE_DIRS - Include directories for the Binary Ninja API.
#   BinaryNinjaApi_LIBRARIES    - Library files to link against
#
# This module reads hints about search locations from variables:
#
#   BinaryNinjaApi_ROOT_DIR - Preferred installation prefix for the API
#   ENV{BINJA_API_DIR}      - Like BinaryNinjaApi_ROOT_DIR
#   BinaryNinja_DIR         - Installation prefix containing the Binary Ninja
#                             core library
#   ENV{BINJA_DIR}          - Like BinaryNinja_DIR
#
# Example:
#
#   find_package(BinaryNinjaApi REQUIRED)
#
#   # Builds targets plugin.dll
#   add_library(exmaple_plugin myplugin.cc)
#   target_link_libraries(exmaple_plugin PRIVATE BinaryNinja::API)

include(CMakeParseArguments)
include(FindPackageHandleStandardArgs)

find_path(BinaryNinjaApi_DIR
  NAMES binaryninjaapi.h
  HINTS "${BinaryNinjaApi_ROOT_DIR}" ENV BINJA_API_DIR
  PATHS "${CMAKE_CURRENT_LIST_DIR}/third_party/binaryninja-api"
        "$ENV{HOME}/binaryninja-api"
  DOC "Location of the Binary Ninja API"
  NO_DEFAULT_PATH
)
set(BinaryNinjaApi_INCLUDE_DIRS "${BinaryNinjaApi_DIR}")

find_library(BinaryNinjaApi_LIBRARY
  NAMES binaryninjaapi
  PATHS "${BinaryNinjaApi_DIR}/bin"
  NO_DEFAULT_PATH
)

set(_binaryninjaapi_FIND_LIBRARY_SUFFIXES ${CMAKE_FIND_LIBRARY_SUFFIXES})
set(CMAKE_FIND_LIBRARY_SUFFIXES ${CMAKE_SHARED_LIBRARY_SUFFIX})
find_library(BinaryNinjaCore_LIBRARY
  NAMES binaryninjacore
  NAMES_PER_DIR
  HINTS "${BinaryNinja_DIR}" ENV BINJA_DIR
        "$ENV{ProgramFiles}/Vector35/BinaryNinja"  # Windows
        "$ENV{HOME}/binaryninja"
  PATHS "/Applications/Binary Ninja.app/Contents/MacOS"
        "/opt/binaryninja"
  DOC "Location of Binary Ninja"
  NO_DEFAULT_PATH
)
set(CMAKE_FIND_LIBRARY_SUFFIXES ${_binaryninjaapi_FIND_LIBRARY_SUFFIXES})

if(BinaryNinjaCore_LIBRARY AND BinaryNinjaApi_LIBRARY)
  list(APPEND BinaryNinjaApi_LIBRARIES
    "${BinaryNinjaApi_LIBRARY}"
    "${BinaryNinjaCore_LIBRARY}"
  )
  add_library(binaryninjaapi INTERFACE IMPORTED GLOBAL)
  add_library(BinaryNinja::API ALIAS binaryninjaapi)
  target_include_directories(binaryninjaapi INTERFACE
    ${BinaryNinjaApi_INCLUDE_DIRS})
  target_link_libraries(binaryninjaapi INTERFACE
    ${BinaryNinjaApi_LIBRARIES})
endif()

find_package_handle_standard_args(BinaryNinjaApi
  FOUND_VAR BinaryNinjaApi_FOUND
  REQUIRED_VARS BinaryNinjaApi_DIR
                BinaryNinjaApi_INCLUDE_DIRS
                BinaryNinjaApi_LIBRARIES
  FAIL_MESSAGE "Binary Ninja API not found, try setting BinaryNinjaApi_ROOT_DIR and/or BinaryNinja_DIR. Note that the API itself needs to be built first."
)
