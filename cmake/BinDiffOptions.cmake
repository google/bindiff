# Copyright 2011-2024 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

if(CMAKE_SOURCE_DIR STREQUAL CMAKE_CURRENT_SOURCE_DIR)
  set(BINDIFF_TOP_LEVEL TRUE)
endif()

# If unset, force a default value for this standard option
if(NOT BUILD_TESTING)
  set(BUILD_TESTING OFF)
endif()

option(BINDIFF_BUILD_TESTING
       "If ON, this will build all of BinDiff's own tests" ${BINDIFF_TOP_LEVEL})
option(BINDIFF_BUILD_BENCHMARK
       "If this and BINDIFF_BUILD_TESTING is ON, build benchmark tests" OFF)
option(BINDIFF_ENABLE_IDAPRO
       "If ON, enable building the IDA Pro plugin" ON)

if(BINDIFF_BUILD_TESTING)
  # Have BinExport download GoogleTest/Benchmark for us
  set(BINEXPORT_BUILD_TESTING ON CACHE BOOL "" FORCE)
  if(BINDIFF_BUILD_BENCHMARK)
    set(BINEXPORT_BUILD_BENCHMARK ON CACHE BOOL "" FORCE)
  endif()
endif()

