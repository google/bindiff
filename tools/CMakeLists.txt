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

add_executable(bindiff_config_setup config_setup.cc)
target_link_libraries(bindiff_config_setup
  absl::flags_parse
  absl::str_format
  absl::status
  absl::statusor
  absl::strings
  bindiff_config
  bindiff_version
  binexport_shared
  bindiff_base
)
install(TARGETS bindiff_config_setup RUNTIME DESTINATION bindiff-prefix)
