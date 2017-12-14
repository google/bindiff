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

# FindIdaSdk
# ----------
#
# Locates and configures the IDA Pro SDK.
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
# Starting with IDA 7.0 SDK, all builds are 64-bit by default. For
# compatibility with 6.95, which is 32-bit, users can tell this module to
# configure the build using these variables:
#
#   IdaSdk_COMPILE_32BIT          - Always compile 32-bit binaries
#   IdaSdk_LEGACY_FILE_EXTENSIONS - For IDA up until 6.95, use the special
#                                   platform-specific file extensions
#                                   (plx/pmc/plw etc.).
#
# Example:
#
#   set(IdaSdk_COMPILE_32BIT ON)
#   set(IdaSdk_LEGACY_FILE_EXTENSIONS ON)
#   find_package(IdaSdk REQUIRED)
#   include_directories(${IdaSdk_INCLUDE_DIRS})
#
#   # Builds target plugin32.plx
#   add_ida_plugin(plugin32 myplugin.cc)
#   # Builds targets plugin.plx and plugin.plx64
#   add_ida_plugin(plugin EA64 myplugin.cc)
#   # Builds target plugin64.plx64
#   add_ida_plugin(plugin64 NOEA32 EA64 myplugin.cc)
#
#   Builds targets ldr.llx and ldr64.llx64
#   add_ida_loader(ldr EA64 myloader.cc)
#
#   For platform-agnostic build files, the variables _plx, _plx64, _llx and
#   _llx64 are available:
#   add_ida_plugin(plugin EA64 myplugin.cc)
#   target_link_libraries(plugin${_plx} ssl)
#   target_link_libraries(plugin${_plx64} ssl)

include(CMakeParseArguments)
include(FindPackageHandleStandardArgs)

find_path(IdaSdk_DIR NAMES include/pro.h
                     HINTS ${IdaSdk_ROOT_DIR} ENV IDASDK_ROOT
                     PATHS ${CMAKE_CURRENT_LIST_DIR}/third_party/idasdk
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
if(NOT IdaSdk_LEGACY_FILE_EXTENSIONS)
  set(_plx ${CMAKE_SHARED_LIBRARY_SUFFIX})
  set(_plx64 64${CMAKE_SHARED_LIBRARY_SUFFIX})  # An additional "64"
  set(_llx ${CMAKE_SHARED_LIBRARY_SUFFIX})
  set(_llx64 64${CMAKE_SHARED_LIBRARY_SUFFIX})  # An additional "64"
endif()
if(APPLE)
  set(IdaSdk_PLATFORM __MAC__)
  if(IdaSdk_LEGACY_FILE_EXTENSIONS)
    set(_plx .pmc)
    set(_plx64 .pmc64)  # No extra "64"
    set(_llx .lmc)
    set(_llx64 64.lmc64)   # An additional "64"
  endif()
elseif(UNIX)
  set(IdaSdk_PLATFORM __LINUX__)
  if(IdaSdk_LEGACY_FILE_EXTENSIONS)
    set(_plx .plx)
    set(_plx64 .plx64)  # No extra "64"
    set(_llx .llx)
    set(_llx64 64.llx64)   # An additional "64"
  endif()
elseif(WIN32)
  set(IdaSdk_PLATFORM __NT__)
  if(IdaSdk_LEGACY_FILE_EXTENSIONS)
    set(_plx .plw)
    set(_plx64 .p64)  # No extra "64"
    set(_llx .ldw)
    set(_llx64 64.l64)  # An additional "64"
  endif()
else()
  message(FATAL_ERROR "Unsupported system type: ${CMAKE_SYSTEM_NAME}")
endif()

function(_ida_plugin name ea64 link_script)  # ARGN contains sources
  # Define a module with the specified sources.
  add_library(${name} MODULE ${ARGN})

  # Support for 64-bit addresses.
  if(ea64)
    target_compile_definitions(${name} PUBLIC __EA64__)
  endif()

  # Build 64-bit by default.
  if(NOT IdaSdk_COMPILE_32BIT)
    target_compile_definitions(${name} PUBLIC __X64__)
  endif()

  # Add the necessary __IDP__ define and allow to use "dangerous" and standard
  # file functions.
  target_compile_definitions(${name} PUBLIC
                             ${IdaSdk_PLATFORM}
                             __IDP__
                             USE_DANGEROUS_FUNCTIONS
                             USE_STANDARD_FILE_FUNCTIONS)

  set_target_properties(${name} PROPERTIES PREFIX "" SUFFIX "")
  if(UNIX)
    if(NOT IdaSdk_COMPILE_32BIT)
      set(_ida_cflag -m64)
    else()
      set(_ida_cflag -m32)
    endif()
    # Always use the linker script needed for IDA.
    target_compile_options(${name} PUBLIC ${_ida_cflag})
    if(APPLE)
      target_link_libraries(${name} ${_ida_cflag}
                                    -Wl,-flat_namespace
                                    -Wl,-undefined,warning
                                    -Wl,-exported_symbol,_PLUGIN)
    else()
      set(script_flag )
      target_link_libraries(${name}
        ${_ida_cflag} -Wl,--version-script ${IdaSdk_DIR}/${link_script})
    endif()

    # For qrefcnt_obj_t in ida.hpp
    target_compile_options(${name} PUBLIC -Wno-non-virtual-dtor)
  elseif(WIN32)
    if(NOT IdaSdk_COMPILE_32BIT)
      set(_ida_prefix x64)
    else()
      set(_ida_prefix x86)
    endif()
    if(ea64)
      set(IdaSdk_LIBRARY ${IdaSdk_DIR}/lib/${_ida_prefix}_win_vc_64/ida.lib)
    else()
      set(IdaSdk_LIBRARY ${IdaSdk_DIR}/lib/${_ida_prefix}_win_vc_32/ida.lib)
    endif()
    target_link_libraries(${name} ${IdaSdk_LIBRARY})
  endif()
endfunction()

function(add_ida_plugin name)
  set(options NOEA32 EA64)
  cmake_parse_arguments(add_ida_plugin "${options}" "" "" ${ARGN})

  if(NOT DEFINED(add_ida_plugin_NOEA32))
    _ida_plugin(${name}${_plx} FALSE plugins/plugin.script
                ${add_ida_plugin_UNPARSED_ARGUMENTS})
  endif()
  if(add_ida_plugin_EA64)
    _ida_plugin(${name}${_plx64} TRUE plugins/plugin.script
                ${add_ida_plugin_UNPARSED_ARGUMENTS})
  endif()
endfunction()

function(add_ida_loader name)
  set(options NOEA32 EA64)
  cmake_parse_arguments(add_ida_loader "${options}" "" "" ${ARGN})

  if(NOT DEFINED(add_ida_loader_NOEA32))
    _ida_plugin(${name}${_llx} FALSE ldr/ldr.script
                ${add_ida_loader_UNPARSED_ARGUMENTS})
  endif()
  if(add_ida_loader_EA64)
    _ida_plugin(${name}${_llx64} TRUE ldr/ldr.script
                ${add_ida_loader_UNPARSED_ARGUMENTS})
  endif()
endfunction()

