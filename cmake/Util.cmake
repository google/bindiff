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

function(create_directory_symlink target link_name)
  # Make sure parent directory exists
  get_filename_component(_parent ${link_name} DIRECTORY)
  file(MAKE_DIRECTORY ${_parent})

  # Windows needs native paths. No-op on other systems.
  file(TO_NATIVE_PATH ${target} _target)
  file(TO_NATIVE_PATH ${link_name} _link_name)
  if(WIN32)
    # Fake directory symlinks by using junctions. While NTFS historically has
    # always supported symlinks, nowadays they either require Windows 10 with
    # Dev mode enabled or administrative privileges. The CI build hosts for
    # BinExport have neither. Junctions always work for unprivileged users.
    set(_cmd $ENV{ComSpec} /c mklink /J ${_link_name} ${_target}
             ERROR_QUIET)
  else()
    set(_cmd ${CMAKE_COMMAND} -E create_symlink ${_target} ${_link_name})
  endif()
  execute_process(COMMAND ${_cmd})
endfunction()
