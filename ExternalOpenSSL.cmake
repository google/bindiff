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

find_path(OPENSSL_ROOT_DIR
  NAMES include/openssl/opensslv.h
  PATHS ${CMAKE_CURRENT_BINARY_DIR}/openssl
  DOC "Location of the OpenSSL installation"
  NO_DEFAULT_PATH)

add_library(ssl STATIC IMPORTED)
add_library(crypto STATIC IMPORTED)

if(EXISTS ${OPENSSL_ROOT_DIR})
  message("-- Found OpenSSL: ${OPENSSL_ROOT_DIR}")
else()
  set(OPENSSL_SOURCE_DIR
    ${CMAKE_CURRENT_BINARY_DIR}/src/external-openssl)
  set(OPENSSL_URL
    https://www.openssl.org/source/openssl-1.0.2k.tar.gz)
  set(OPENSSL_URL_HASH
    SHA256=6b3977c61f2aedf0f96367dcfb5c6e578cf37e7b8d913b4ecb6643c3cb88d8c0)
  set(OPENSSL_ROOT_DIR ${CMAKE_CURRENT_BINARY_DIR}/openssl)

  if(WIN32)
    file(TO_NATIVE_PATH ${OPENSSL_SOURCE_DIR} _src)
    file(TO_NATIVE_PATH ${OPENSSL_ROOT_DIR} _inst)
    if(NOT COMPILE_64BIT)
      set(_os_flags VC-WIN32 no-asm)
      set(_os_prep ms\\do_ms.bat)
    else()
      set(_os_flags VC-WIN64A no-asm)
      set(_os_prep ms\\do_win64a.bat)
    endif()
    set(OPENSSL_CONFIGURE_COMMAND cd /d "${_src}"
      COMMAND perl Configure ${_os_flags} "--prefix=${_inst}")
    set(OPENSSL_BUILD_COMMAND cd /d "${_src}"
      COMMAND ${_os_prep})
    set(OPENSSL_INSTALL_COMMAND cd /d "${_src}"
      COMMAND nmake /nologo /f ms\\nt.mak /s install)
  elseif(UNIX)
    if(NOT COMPILE_64BIT)
      if(APPLE)
        set(_os_flags no-asm no-shared no-engines no-dso no-hw no-zlib
                      darwin-i386-cc)
      else()
        set(_os_flags no-asm no-dso no-shared no-zlib linux-generic32 -m32)
      endif()
    else()
      if(APPLE)
        set(_os_flags no-asm no-shared no-engines no-dso no-hw no-zlib
                      darwin64-x86_64-cc)
      else()
        set(_os_flags no-asm no-dso no-engine shared no-zlib linux-generic64)
      endif()
    endif()
    set(OPENSSL_CONFIGURE_COMMAND
      cd "${OPENSSL_SOURCE_DIR}" &&
      "${OPENSSL_SOURCE_DIR}/Configure" ${_os_flags}
                                        "--prefix=${OPENSSL_ROOT_DIR}")
    set(OPENSSL_BUILD_COMMAND
      $(MAKE) -sC "${OPENSSL_SOURCE_DIR}" depend build_libs build_apps)
    set(OPENSSL_INSTALL_COMMAND
      $(MAKE) -sC "${OPENSSL_SOURCE_DIR}" install_sw)
  endif()

  ExternalProject_Add(external-openssl
    URL ${OPENSSL_URL}
    URL_HASH ${OPENSSL_URL_HASH}
    PREFIX ${CMAKE_CURRENT_BINARY_DIR}
    SOURCE_DIR ${OPENSSL_SOURCE_DIR}
    CONFIGURE_COMMAND ${OPENSSL_CONFIGURE_COMMAND}
    BUILD_COMMAND ${OPENSSL_BUILD_COMMAND}
    INSTALL_COMMAND ${OPENSSL_INSTALL_COMMAND})
  add_dependencies(ssl external-openssl)
  add_dependencies(crypto external-openssl)
endif()

set(OPENSSL_INCLUDE_DIR ${OPENSSL_ROOT_DIR}/include)
set(OPENSSL_LIBRARIES ssl crypto)

if(WIN32)
  set_property(TARGET ssl
    PROPERTY IMPORTED_LOCATION ${OPENSSL_ROOT_DIR}/lib/libeay32.lib)
  set_property(TARGET crypto
    PROPERTY IMPORTED_LOCATION ${OPENSSL_ROOT_DIR}/lib/ssleay32.lib)
elseif(UNIX)
  set_property(TARGET ssl
    PROPERTY IMPORTED_LOCATION ${OPENSSL_ROOT_DIR}/lib/libssl.a)
  set_property(TARGET crypto
    PROPERTY IMPORTED_LOCATION ${OPENSSL_ROOT_DIR}/lib/libcrypto.a)
endif()
