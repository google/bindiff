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

ExternalProject_Add(cctz
  GIT_REPOSITORY https://github.com/google/cctz.git
  GIT_TAG a59b930afc821e5f5a0b868dfee56482075db185
  SOURCE_DIR ${CMAKE_CURRENT_BINARY_DIR}/cctz
  # Just use CMake to clone into directory
  CONFIGURE_COMMAND ""
  BUILD_COMMAND ""
  INSTALL_COMMAND ""
)
