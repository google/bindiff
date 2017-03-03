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

if(UNIX)
  add_compile_options(-Wno-deprecated)
  list(APPEND CMAKE_CXX_FLAGS --std=c++11)
  set(CMAKE_SKIP_BUILD_RPATH TRUE)
  if(NOT COMPILE_64BIT)
    add_compile_options(-m32)
    set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -m32")
  endif()
elseif(WIN32)
  if(MSVC)
    add_definitions(-D_CRT_SECURE_NO_WARNINGS)

    # Use the static runtime
    foreach(flag_var CMAKE_C_FLAGS CMAKE_CXX_FLAGS
                     CMAKE_C_FLAGS_DEBUG CMAKE_CXX_FLAGS_DEBUG
                     CMAKE_C_FLAGS_RELEASE CMAKE_CXX_FLAGS_RELEASE
                     CMAKE_C_FLAGS_MINSIZEREL CMAKE_CXX_FLAGS_MINSIZEREL
                     CMAKE_C_FLAGS_RELWITHDEBINFO CMAKE_CXX_FLAGS_RELWITHDEBINFO)
      if(${flag_var} MATCHES "/MD")
        string(REGEX REPLACE "/MD" "/MT" ${flag_var} "${${flag_var}}")
      endif()
      if(${flag_var} MATCHES "/MDd")
        string(REGEX REPLACE "/MDd" "/MTd" ${flag_var} "${${flag_var}}")
      endif()
    endforeach(flag_var)
  endif()
endif()

# Enforce a single output directory Linux/macOS and Windows. Note that this
# will flatten all subdirectories as well (i.e. "tools/binexport2dump" will
# end up in CMAKE_BINARY_DIR directly).
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR})
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR})
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR})
foreach(out_config ${CMAKE_CONFIGURATION_TYPES})
  string(TOUPPER ${out_config} out_config)
  set(CMAKE_RUNTIME_OUTPUT_DIRECTORY_${out_config} ${CMAKE_BINARY_DIR})
  set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_${out_config} ${CMAKE_BINARY_DIR})
  set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY_${out_config} ${CMAKE_BINARY_DIR})
endforeach()