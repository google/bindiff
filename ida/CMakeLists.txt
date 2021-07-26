# Copyright 2011-2021 Google LLC
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

# Plugins will be named "bindiffX_ida.so" and "bindiffX_ida64.so" (or .dll or
# .dylib), where X is the release number.
set(bindiff_plugin_name bindiff${PROJECT_VERSION_MAJOR}_ida)
add_ida_plugin(${bindiff_plugin_name}
  bindiff_icon.cc
  bindiff_icon.h
  main_plugin.cc
  main_plugin.h
  matched_functions_chooser.cc
  matched_functions_chooser.h
  names.cc
  names.h
  results.cc
  results.h
  statistics_chooser.cc
  statistics_chooser.h
  unmatched_functions_chooser.cc
  unmatched_functions_chooser.h
  visual_diff.cc
  visual_diff.h
)
ida_target_link_libraries(${bindiff_plugin_name}
  binexport_core
  binexport_plugin_shared
  bindiff_base
  bindiff_shared
  absl::str_format
  absl::time
)
if(WIN32)
  ida_target_link_libraries(${bindiff_plugin_name}
    ws2_32.lib
  )
endif()
set_ida_target_properties(${bindiff_plugin_name} PROPERTIES
  POSITION_INDEPENDENT_CODE ON
  INTERPROCEDURAL_OPTIMIZATION ${BINDIFF_ENABLE_IPO}
)