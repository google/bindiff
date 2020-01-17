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

# Directly build with the project as needed by Abseil and recommended upstream
find_path(googletest_src_dir
  googletest/include/gtest/gtest.h
  HINTS ${GOOGLETEST_ROOT_DIR}
  PATHS ${PROJECT_BINARY_DIR}/googletest
)
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
message("${googletest_src_dir}")
add_subdirectory(${googletest_src_dir} ${PROJECT_BINARY_DIR}/googletest
                 EXCLUDE_FROM_ALL)
