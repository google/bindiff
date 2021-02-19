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

# FindIdaSdk
# ----------
#
# Locates and configures the IDA Pro SDK. Supports version 7.0 or higher.
#
# Use this module by invoking find_package with the form:
#
#   find_package(IdaSdk
#                [REQUIRED]  # Fail with an error if IDA SDK is not found
#               )
#
# Defines the following variables:
#
#   IdaSdk_INCLUDE_DIRS - Include directories for the IDA Pro SDK.
#   IdaSdk_PLATFORM     - IDA SDK platform, one of __LINUX__, __NT__ or
#                         __MAC__.
#
# This module reads hints about search locations from variables:
#
#   IdaSdk_ROOT_DIR  - Preferred installation prefix
#
# Example (this assumes Windows):
#
#   find_package(IdaSdk REQUIRED)
#
#   # Builds targets plugin.dll and plugin64.dll
#   add_ida_plugin(plugin myplugin.cc)
#   # Builds target plugin64.dll
#   add_ida_plugin(plugin NOEA32 myplugin.cc)
#   # Builds target plugin.dll
#   add_ida_plugin(plugin NOEA64 myplugin.cc)
#
#   Builds targets ldr.dll and ldr64.dll
#   add_ida_loader(ldr myloader.cc)
#
# For platform-agnostic build files, the variables _so, and _so64 are
# available (and map to .dll, .so, .dylib as necessary):
#
#   add_ida_plugin(plugin myplugin.cc)
#   target_link_libraries(plugin${_so} ssl)
#   target_link_libraries(plugin${_so64} ssl)
#
# To avoid the duplication above, these functions, which mimic the built-in
# ones, are also defined:
#
#   add_ida_library(<name> NOEA64|NOEA64 ...) <=> add_libary()
#   ida_target_link_libraries(...)            <=> target_link_libraries()
#   ida_target_include_directories(...)       <=> target_include_directories()
#   set_ida_target_properties(...)            <=> set_target_properties()
#   ida_install(...)                          <=> install()

include(CMakeParseArguments)
include(FindPackageHandleStandardArgs)

find_path(IdaSdk_DIR NAMES include/pro.h
                     HINTS ${IdaSdk_ROOT_DIR} ENV IDASDK_ROOT
                     PATHS ${CMAKE_CURRENT_LIST_DIR}/../third_party/idasdk
                     PATH_SUFFIXES idasdk
                     DOC "Location of the IDA SDK"
                     NO_DEFAULT_PATH)
set(IdaSdk_INCLUDE_DIRS ${IdaSdk_DIR}/include)

find_package_handle_standard_args(
  IdaSdk FOUND_VAR IdaSdk_FOUND
         REQUIRED_VARS IdaSdk_DIR
                       IdaSdk_INCLUDE_DIRS
         FAIL_MESSAGE "IDA SDK not found, try setting IdaSdk_ROOT_DIR")

# Define some platform specific variables for later use.
set(_so ${CMAKE_SHARED_LIBRARY_SUFFIX})
set(_so64 64${CMAKE_SHARED_LIBRARY_SUFFIX})  # An additional "64"
# _plx, _plx64, _llx, _llx64 are kept to stay compatible with older
# CMakeLists.txt files.
set(_plx ${CMAKE_SHARED_LIBRARY_SUFFIX})
set(_plx64 64${CMAKE_SHARED_LIBRARY_SUFFIX})  # An additional "64"
set(_llx ${CMAKE_SHARED_LIBRARY_SUFFIX})
set(_llx64 64${CMAKE_SHARED_LIBRARY_SUFFIX})  # An additional "64"
if(APPLE)
  set(IdaSdk_PLATFORM __MAC__)
elseif(UNIX)
  set(IdaSdk_PLATFORM __LINUX__)
elseif(WIN32)
  set(IdaSdk_PLATFORM __NT__)
else()
  message(FATAL_ERROR "Unsupported system type: ${CMAKE_SYSTEM_NAME}")
endif()

function(_ida_common_target_settings t ea64)
  if(ea64)  # Support for 64-bit addresses.
    target_compile_definitions(${t} PUBLIC __EA64__)
  endif()
  # Add the necessary __IDP__ define and allow to use "dangerous" and standard
  # file functions.
  target_compile_definitions(${t} PUBLIC ${IdaSdk_PLATFORM}
                                         __X64__
                                         __IDP__
                                         USE_DANGEROUS_FUNCTIONS
                                         USE_STANDARD_FILE_FUNCTIONS)
  target_include_directories(${t} PUBLIC ${IdaSdk_INCLUDE_DIRS})
endfunction()

function(_ida_plugin name ea64 link_script)  # ARGN contains sources
  if(ea64)
    set(t ${name}${_so64})
  else()
    set(t ${name}${_so})
  endif()

  # Define a module with the specified sources.
  add_library(${t} MODULE ${ARGN})
  _ida_common_target_settings(${t} ${ea64})

  set_target_properties(${t} PROPERTIES PREFIX "" SUFFIX "")
  if(UNIX)
    target_compile_options(${t} PUBLIC ${_ida_compile_options})
    if(APPLE)
      target_link_libraries(${t} ${_ida_compile_options}
                                 -Wl,-flat_namespace
                                 -Wl,-undefined,warning
                                 -Wl,-exported_symbol,_PLUGIN)
    else()
      # Always use the linker script needed for IDA.
      target_link_libraries(${t} ${_ida_compile_options}
        -Wl,--version-script ${IdaSdk_DIR}/${link_script})
    endif()

    # For qrefcnt_obj_t in ida.hpp
    # TODO(cblichmann): This belongs in an interface library instead.
    target_compile_options(${t} PUBLIC
      -Wno-non-virtual-dtor
      -Wno-varargs
    )
  elseif(WIN32)
    if(ea64)
      target_link_libraries(${t} ${IdaSdk_DIR}/lib/x64_win_vc_64/ida.lib)
    else()
      target_link_libraries(${t} ${IdaSdk_DIR}/lib/x64_win_vc_32/ida.lib)
    endif()
  endif()
endfunction()

macro(_ida_check_bitness)
  if(opt_NOEA32 AND opt_NOEA64)
    message(FATAL_ERROR "NOEA32 and NOEA64 cannot be used at the same time")
  endif()
endmacro()

function(_ida_library name ea64)
  if(ea64)
    set(t ${name}_ea64)
  else()
    set(t ${name}_ea32)
  endif()

  # Define the actual library.
  add_library(${t} ${ARGN})
  _ida_common_target_settings(${t} ${ea64})
endfunction()

function(add_ida_library name)
  cmake_parse_arguments(PARSE_ARGV 1 opt "NOEA32;NOEA64" "" "")
  _ida_check_bitness(opt_NOEA32 opt_NOEA64)

  if(NOT DEFINED(opt_NOEA32))
    _ida_library(${name} FALSE ${opt_UNPARSED_ARGUMENTS})
  endif()
  if(NOT DEFINED(opt_NOEA64))
    _ida_library(${name} TRUE ${opt_UNPARSED_ARGUMENTS})
  endif()
endfunction()

function(add_ida_plugin name)
  cmake_parse_arguments(PARSE_ARGV 1 opt "NOEA32;NOEA64" "" "")
  _ida_check_bitness(opt_NOEA32 opt_NOEA64)

  if(NOT opt_NOEA32)
    _ida_plugin(${name} FALSE plugins/exports.def ${opt_UNPARSED_ARGUMENTS})
  endif()
  if(NOT opt_NOEA64)
    _ida_plugin(${name} TRUE plugins/exports.def ${opt_UNPARSED_ARGUMENTS})
  endif()
endfunction()

function(add_ida_loader name)
  cmake_parse_arguments(PARSE_ARGV 1 opt "NOEA32;NOEA64" "" "")
  _ida_check_bitness(opt_NOEA32 opt_NOEA64)

  if(NOT opt_NOEA32)
    _ida_plugin(${name} FALSE ldr/exports.def ${opt_UNPARSED_ARGUMENTS})
  endif()
  if(NOT opt_NOEA64)
    _ida_plugin(${name} TRUE ldr/exports.def ${opt_UNPARSED_ARGUMENTS})
  endif()
endfunction()

function(ida_target_link_libraries name)
  foreach(item IN LISTS ARGN)
    if(TARGET ${item}_ea32 OR TARGET ${item}_ea64)
      if(TARGET ${item}_ea32)
        list(APPEND args32 ${item}_ea32)
      endif()
      if(TARGET ${item}_ea64)
        list(APPEND args64 ${item}_ea64)
      endif()
    else()
      list(APPEND args ${item})
    endif()
  endforeach()
  foreach(target ${name}${_so} ${name}_ea32)
    if(TARGET ${target})
      target_link_libraries(${target} ${args32} ${args})
      set(added TRUE)
    endif()
  endforeach()
  foreach(target ${name}${_so64} ${name}_ea64)
    if(TARGET ${target})
      target_link_libraries(${target} ${args64} ${args})
      set(added TRUE)
    endif()
  endforeach()
  if (NOT added)
    message(FATAL_ERROR "No such target: ${name}")
  endif()
endfunction()

function(ida_target_include_directories name)
  foreach(target ${name}${_so} ${name}${_so64}
                 ${name}_ea32 ${name}_ea64)
    if(TARGET ${target})
      target_include_directories(${target} ${ARGN})
      set(added TRUE)
    endif()
  endforeach()
  if (NOT added)
    message(FATAL_ERROR "No such target: ${name}")
  endif()
endfunction()

function(set_ida_target_properties name)
  foreach(target ${name}${_so} ${name}${_so64}
                 ${name}_ea32 ${name}_ea64)
    if(TARGET ${target})
      set_target_properties(${target} ${ARGN})
      set(added TRUE)
    endif()
  endforeach()
  if (NOT added)
    message(FATAL_ERROR "No such target: ${name}")
  endif()
endfunction()

function(ida_install)
  foreach(item IN LISTS ARGN)
    if(TARGET ${item}${_so} OR TARGET ${item}${_so64})
      if(TARGET ${item}${_so})
        list(APPEND args ${item}${_so})
      endif()
      if(TARGET ${item}${_so64})
        list(APPEND args ${item}${_so64})
      endif()
    else()
      list(APPEND args ${item})
    endif()
  endforeach()
  install(${args})
endfunction()
