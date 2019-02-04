# Copyright 2011-2019 Google LLC. All Rights Reserved.
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

# Directly build with the project as recommended upstream
find_path(absl_src_dir
  absl/base/port.h
  HINTS ${ABSL_ROOT_DIR}
  PATHS ${PROJECT_BINARY_DIR}/absl
)
add_subdirectory(${absl_src_dir} ${PROJECT_BINARY_DIR}/absl
                 EXCLUDE_FROM_ALL)

if(WIN32)
  foreach(target absl_base
                 absl_algorithm
                 absl_container
                 absl_debugging
                 absl_hash
                 absl_memory
                 absl_meta
                 absl_numeric
                 absl_strings
                 absl_synchronization
                 absl_time
                 absl_utility)
    if(MSVC)
      target_compile_options(${target} PRIVATE
        /wd4005  # macro-redefinition
        /wd4068  # unknown pragma
        /wd4244  # conversion from 'type1' to 'type2'
        /wd4267  # conversion from 'size_t' to 'type2'
        /wd4800  # force value to bool 'true' or 'false' (performance warning)
      )
    endif()
    target_compile_definitions(${target} PRIVATE
      /DNOMINMAX
      /DWIN32_LEAN_AND_MEAN=1
      /D_CRT_SECURE_NO_WARNINGS
    )
  endforeach()
endif()
