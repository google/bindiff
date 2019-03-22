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

if(WIN32)
  file(TO_NATIVE_PATH <SOURCE_DIR> openssl_src)
  file(TO_NATIVE_PATH <INSTALL_DIR> openssl_inst)
  set(OPENSSL_CONFIGURE_COMMAND cd /d "${openssl_src}"
    COMMAND perl Configure VC-WIN64A no-asm "--prefix=${openssl_inst}")
  set(OPENSSL_BUILD_COMMAND cd /d "${openssl_src}"
    COMMAND ms\\do_win64a.bat)
  set(OPENSSL_INSTALL_COMMAND cd /d "${openssl_src}"
    COMMAND nmake /nologo /f ms\\nt.mak /s install)
elseif(UNIX)
  if(APPLE)
    set(openssl_configure_flags no-asm no-dso no-shared no-engines
                                no-hw no-zlib darwin64-x86_64-cc)
  else()  # Assume Linux
    set(openssl_configure_flags -fpic no-asm no-dso no-shared no-engines
                                no-hw no-zlib linux-x86_64)
  endif()
  set(OPENSSL_CONFIGURE_COMMAND
    cd "<SOURCE_DIR>" &&
    "<SOURCE_DIR>/Configure" ${openssl_configure_flags}
                             "--prefix=<INSTALL_DIR>")
  set(OPENSSL_BUILD_COMMAND
    $(MAKE) -sC "<SOURCE_DIR>" build_libs build_apps)
  set(OPENSSL_INSTALL_COMMAND
    $(MAKE) -sC "<SOURCE_DIR>" install_sw)
endif()

ExternalProject_Add(openssl
  URL https://www.openssl.org/source/openssl-1.0.2l.tar.gz
  URL_HASH SHA256=ce07195b659e75f4e1db43552860070061f156a98bb37b672b101ba6e3ddf30c
  PREFIX ${CMAKE_CURRENT_BINARY_DIR}/openssl
  CONFIGURE_COMMAND ${OPENSSL_CONFIGURE_COMMAND}
  BUILD_COMMAND ${OPENSSL_BUILD_COMMAND}
  INSTALL_COMMAND ${OPENSSL_INSTALL_COMMAND}
)
ExternalProject_Get_Property(openssl INSTALL_DIR)
list(APPEND CMAKE_PREFIX_PATH ${INSTALL_DIR})
