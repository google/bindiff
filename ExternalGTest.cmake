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

find_path(GTEST_ROOT_DIR
  NAMES include/gtest/gtest.h
  PATHS ${CMAKE_CURRENT_BINARY_DIR}/gtest
  DOC "Location of the Google Test source tree"
  NO_DEFAULT_PATH)

add_library(gtest STATIC IMPORTED)
add_library(gtest_main STATIC IMPORTED)

if(EXISTS ${GTEST_ROOT_DIR})
  message("-- Found Google Test: ${GTEST_ROOT_DIR}")
else()
  set(GTEST_SOURCE_DIR
    ${CMAKE_CURRENT_BINARY_DIR}/src/external-gtest)
  set(GTEST_URL
    https://github.com/google/googletest/archive/release-1.8.0.tar.gz)
  set(GTEST_URL_HASH
    SHA256=58a6f4277ca2bc8565222b3bbd58a177609e9c488e8a72649359ba51450db7d8)
  set(GTEST_ROOT_DIR ${CMAKE_CURRENT_BINARY_DIR}/gtest)

  if(UNIX AND NOT COMPILE_64BIT)
    set(_gt_flags -DCMAKE_C_FLAGS=-m32
                  -DCMAKE_CXX_FLAGS=-m32)
  endif()

  ExternalProject_Add(external-gtest
    URL ${GTEST_URL}
    URL_HASH ${GTEST_URL_HASH}
    PREFIX ${CMAKE_CURRENT_BINARY_DIR}
    SOURCE_DIR ${GTEST_SOURCE_DIR}
    CONFIGURE_COMMAND cd "<BINARY_DIR>" &&
      "${CMAKE_COMMAND}" "<SOURCE_DIR>/googletest"
      -G "${CMAKE_GENERATOR}"
      -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
      "-DCMAKE_INSTALL_PREFIX=${GTEST_ROOT_DIR}"
      -DCMAKE_RULE_MESSAGES=OFF
      ${_gt_flags}
    BUILD_COMMAND ""
    INSTALL_COMMAND "${CMAKE_COMMAND}"
      --build "<BINARY_DIR>" --config ${CMAKE_BUILD_TYPE} --target install)
  add_dependencies(gtest external-gtest)
endif()

set(GTEST_INCLUDE_DIRS ${GTEST_ROOT_DIR}/include)
set(GTEST_LIBRARIES gtest)
if(UNIX)
  list(APPEND GTEST_LIBRARIES pthread)
endif()
set(GTEST_MAIN_LIBRARIES gtest_main)
set(GTEST_BOTH_LIBRARIES ${GTEST_LIBRARIES} ${GTEST_MAIN_LIBRARIES})

if(WIN32)
  set_property(TARGET gtest
    PROPERTY IMPORTED_LOCATION ${GTEST_ROOT_DIR}/lib/gtest.lib)
  set_property(TARGET gtest_main
    PROPERTY IMPORTED_LOCATION ${GTEST_ROOT_DIR}/lib/gtest_main.lib)
elseif(UNIX)
  set_property(TARGET gtest
    PROPERTY IMPORTED_LOCATION ${GTEST_ROOT_DIR}/lib/libgtest.a)
  set_property(TARGET gtest_main
    PROPERTY IMPORTED_LOCATION ${GTEST_ROOT_DIR}/lib/libgtest_main.a)
endif()
