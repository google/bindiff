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

# These affect ABI and linking, so set them globally, even for dependencies
set(CMAKE_CXX_STANDARD_REQUIRED TRUE)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_EXTENSIONS FALSE)

set(CMAKE_SKIP_BUILD_RPATH TRUE)
set(CMAKE_POSITION_INDEPENDENT_CODE TRUE)

# Enforce static linkage
set(CMAKE_FIND_LIBRARY_SUFFIXES ${CMAKE_STATIC_LIBRARY_SUFFIX})

if(CMAKE_CXX_COMPILER_ID STREQUAL "GNU")  # GCC
elseif(CMAKE_CXX_COMPILER_ID MATCHES "Clang")  # Clang or Apple Clang
  add_compile_options(-Wno-nullability-completeness)
elseif(MSVC)  # Visual Studio
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
else()
  message(FATAL_ERROR "Unsupported compiler")
endif()

if(UNIX)
  add_compile_options(-Wno-deprecated)
  if(APPLE)
    add_compile_options(-gfull)
    add_link_options(-dead_strip)
  else()
   add_compile_options(
     -ffunction-sections
     -fdata-sections
   )
   add_link_options(LINKER:-gc-sections)
  endif()
elseif(WIN32)
  add_definitions(
    # Protobuf iterators trigger deprecation warnings
    -D_SILENCE_ALL_CXX17_DEPRECATION_WARNINGS
    # Do not define min/max macros which collide with std::min()/std::max()
    -DNOMINMAX
    -DWIN32_LEAN_AND_MEAN
  )
endif()
