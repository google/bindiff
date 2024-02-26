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

# SQLite embedded database, underlies the .BinDiff file format
FetchContent_Declare(sqlite
  URL      https://sqlite.org/2024/sqlite-amalgamation-3450100.zip
  URL_HASH SHA3_256=e311198775d5d5b2889d5fabe1d9a490567a14e605591d6a9e4c833804a8b4cb
)
FetchContent_GetProperties(sqlite)
if(NOT sqlite_POPULATED)
  FetchContent_Populate(sqlite)
  add_library(sqlite STATIC
    ${sqlite_SOURCE_DIR}/sqlite3.c
  )
  if(UNIX AND (NOT APPLE))
    target_link_libraries(sqlite ${CMAKE_DL_LIBS})
  endif()
endif()

# Setup IDA SDK. Uses FindIdaSdk.cmake from BinExport
find_package(IdaSdk)

find_package(Protobuf 3.14 REQUIRED) # Make protobuf_generate_cpp available
