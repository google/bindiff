# Copyright 2011-2026 Google LLC
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


# TODO(cblichmann): Remove this workaround for macOS protobuf build
if(APPLE)
  add_definitions(-DGOOGLE_PROTOBUF_NO_RDTSC)
endif()

# Plugins will be named "bindiffX_ida.so" (or .dll or .dylib), where X is the
# release number.
ida_add_plugin(${bindiff_ida_plugin_name}
  SOURCES bindiff_icon.cc
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
target_compile_definitions(${bindiff_ida_plugin_name}
  PUBLIC USE_DANGEROUS_FUNCTIONS
         USE_STANDARD_FILE_FUNCTIONS
)
target_link_libraries(${bindiff_ida_plugin_name}
  PUBLIC binexport_core
         binexport_plugin_shared
         bindiff_base
         bindiff_shared
         absl::cleanup
         absl::function_ref
         absl::memory
         absl::nullability
         absl::status_macros
         absl::str_format
         absl::time
)
if(WIN32)
  target_link_libraries(${bindiff_ida_plugin_name}
    PUBLIC delayimp.lib  # Helper for /DELAYLOAD
           ws2_32.lib
  )
  # See b/206656063 for context
  set_target_properties(${bindiff_ida_plugin_name} PROPERTIES
    LINK_FLAGS /DELAYLOAD:dbghelp.dll
  )
endif()
set_target_properties(${bindiff_ida_plugin_name} PROPERTIES
  POSITION_INDEPENDENT_CODE ON
  INTERPROCEDURAL_OPTIMIZATION ${BINDIFF_ENABLE_IPO}
  LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/ida"
  RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/ida"
)

