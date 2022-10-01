# Copyright 2011-2022 Google LLC
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

add_executable(binexport2dump binexport2dump.cc)
target_link_libraries(binexport2dump
  absl::flat_hash_map
  absl::hash
  absl::log_initialize
  absl::str_format
  absl::time
  binexport_base
  binexport_shared
)
install(TARGETS binexport2dump RUNTIME DESTINATION binexport-prefix)
