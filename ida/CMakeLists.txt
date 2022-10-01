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

# Code shared with the BinDiff plugin
add_ida_library(binexport_plugin_shared STATIC
  digest.cc
  digest.h
  flow_analysis.cc
  flow_analysis.h
  log_sink.cc
  log_sink.h
  names.cc
  names.h
  ui.cc
  ui.h
  util.cc
  util.h
)
ida_target_link_libraries(binexport_plugin_shared PUBLIC
  absl::time
  binexport_base
  binexport_core
)

add_ida_plugin(${binexport_ida_plugin_name}
  arm.cc
  arm.h
  dalvik.cc
  dalvik.h
  generic.cc
  generic.h
  main_plugin.cc
  main_plugin.h
  metapc.cc
  metapc.h
  mips.cc
  mips.h
  plugin.h
  ppc.cc
  ppc.h
)
ida_target_link_libraries(${binexport_ida_plugin_name}
  absl::bad_optional_access
  absl::flat_hash_map
  absl::flat_hash_set
  absl::hash
  absl::log
  absl::log_initialize
  absl::node_hash_map
  absl::node_hash_set
  absl::optional
  absl::strings
  absl::time
  binexport_core
  binexport_plugin_shared
)
if(WIN32)
  ida_target_link_libraries(${binexport_ida_plugin_name}
    crypt32.lib
    delayimp.lib  # Helper for /DELAYLOAD
    secur32.lib
    shlwapi.lib
    ws2_32.lib
    wldap32.lib
  )
  # See b/206656063 for context
  set_ida_target_properties(${binexport_ida_plugin_name} PROPERTIES
    LINK_FLAGS /DELAYLOAD:dbghelp.dll
  )
endif()
set_ida_target_properties(${binexport_ida_plugin_name} PROPERTIES
  POSITION_INDEPENDENT_CODE ON
)
ida_install(TARGETS ${binexport_ida_plugin_name}
            ARCHIVE DESTINATION binexport-prefix
            RUNTIME DESTINATION binexport-prefix
            LIBRARY DESTINATION binexport-prefix)
