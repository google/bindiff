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

find_path(PostgreSQL_ROOT_DIR
  NAMES include/libpq-fe.h
  HINTS ${PostgreSQL_ROOT_DIR}
  PATHS ${CMAKE_CURRENT_BINARY_DIR}/postgresql
  DOC "Location of the PostgreSQL installation"
  NO_DEFAULT_PATH)

add_library(pq STATIC IMPORTED)
if(WIN32)
    add_library(pgport STATIC IMPORTED)
endif()

if(EXISTS ${PostgreSQL_ROOT_DIR})
  message("-- Found PostgreSQL: ${PostgreSQL_ROOT_DIR}")
else()
  set(PostgreSQL_SOURCE_DIR
    ${CMAKE_CURRENT_BINARY_DIR}/src/external-postgresql)
  set(PostgreSQL_URL
    https://ftp.postgresql.org/pub/source/v9.6.1/postgresql-9.6.1.tar.bz2)
  set(PostgreSQL_URL_HASH
    SHA256=e5101e0a49141fc12a7018c6dad594694d3a3325f5ab71e93e0e51bd94e51fcd)
  set(PostgreSQL_ROOT_DIR ${CMAKE_CURRENT_BINARY_DIR}/postgresql)

  if(WIN32)
    file(TO_NATIVE_PATH ${CMAKE_CURRENT_BINARY_DIR} _pg_bin)
    file(TO_NATIVE_PATH ${PostgreSQL_SOURCE_DIR} _pg_src)
    file(TO_NATIVE_PATH ${PostgreSQL_ROOT_DIR} _pg_inst)

    # Set OpenSSL location, dependency is added further below.
    file(WRITE ${CMAKE_CURRENT_BINARY_DIR}/pg_config.pl
               "$config->{openssl} = '${OPENSSL_ROOT_DIR}';")
    # Install headers
    file(WRITE ${CMAKE_CURRENT_BINARY_DIR}/pg_install_headers.pl
               "use lib 'src/tools/msvc';use Install;"
               "Install::CopyIncludeFiles('${_pg_inst}');")

    set(PostgreSQL_CONFIGURE_COMMAND cd /d "${_pg_src}\\src\\tools\\msvc"
    COMMAND copy ${_pg_bin}\\pg_config.pl
                 ${_pg_src}\\src\\tools\\msvc\\config.pl
    COMMAND copy ${_pg_bin}\\pg_install_headers.pl ${_pg_src}
      COMMAND perl -pi.bak -e "s/MultiThreadedDLL/MultiThreaded/g"
      MSBuildProject.pm
      COMMAND perl mkvcbuild.pl)
    set(PostgreSQL_BUILD_COMMAND
      msbuild "${_pg_src}\\pgsql.sln" /t:interfaces\\libpq
      /p:Configuration=Release /p:ConfigurationType=StaticLibrary /m)
    set(PostgreSQL_INSTALL_COMMAND cd /d "${_pg_src}"
    COMMAND perl pg_install_headers.pl
    COMMAND if not exist "${_pg_inst}\\lib" mkdir "${_pg_inst}\\lib"
    COMMAND copy Release\\libpgport\\libpgport.lib "${_pg_inst}\\lib"
    COMMAND copy Release\\libpq\\libpq.lib "${_pg_inst}\\lib")
  elseif(UNIX)
    if(NOT COMPILE_64BIT)
      if(APPLE)
        set(_pg_flags --build=i386-apple-darwin
          "CFLAGS=-arch=i386 -m32" "LDFLAGS=-arch=i386 -m32")
      else()
        set(_pg_flags CFLAGS=-m32 LDFLAGS=-m32)
      endif()
    endif()
    set(PostgreSQL_CONFIGURE_COMMAND
      "${PostgreSQL_SOURCE_DIR}/configure" ${_pg_flags}
      --disable-debug --disable-profiling --disable-coverage --without-tcl
      --without-perl --without-python --without-gssapi --without-ldap
      --without-bonjour --without-readline --without-zlib
      --with-openssl
      "--with-includes=${OPENSSL_INCLUDE_DIR}"
      "--with-libraries=${OPENSSL_ROOT_DIR}/lib"
      "--prefix=${PostgreSQL_ROOT_DIR}")
    set(PostgreSQL_BUILD_COMMAND "true")
    set(PostgreSQL_INSTALL_COMMAND
      $(MAKE) -isC "${PostgreSQL_SOURCE_DIR}/src/include" install
      COMMAND $(MAKE) -sC "${PostgreSQL_SOURCE_DIR}/src/interfaces/libpq" install
      BUILD_IN_SOURCE 1)
  endif()

  ExternalProject_Add(external-postgresql
    URL ${PostgreSQL_URL}
    URL_HASH ${PostgreSQL_URL_HASH}
    PREFIX ${CMAKE_CURRENT_BINARY_DIR}
    LIST_SEPARATOR ^^
    SOURCE_DIR ${PostgreSQL_SOURCE_DIR}
    CONFIGURE_COMMAND ${PostgreSQL_CONFIGURE_COMMAND}
    BUILD_COMMAND ${PostgreSQL_BUILD_COMMAND}
    INSTALL_COMMAND ${PostgreSQL_INSTALL_COMMAND})
  add_dependencies(external-postgresql ${OPENSSL_LIBRARIES})
  add_dependencies(pq external-postgresql)
endif()

set(PostgreSQL_LIBRARIES pq)

if(WIN32)
  set_property(TARGET pq PROPERTY IMPORTED_LOCATION
                      ${PostgreSQL_ROOT_DIR}/lib/libpq.lib)
  set_property(TARGET pgport PROPERTY IMPORTED_LOCATION
                      ${PostgreSQL_ROOT_DIR}/lib/libpgport.lib)
  list(APPEND PostgreSQL_LIBRARIES pgport)
elseif(UNIX)
  set_property(TARGET pq
    PROPERTY IMPORTED_LOCATION ${PostgreSQL_ROOT_DIR}/lib/libpq.a)
endif()

set(PostgreSQL_INCLUDE_DIR ${PostgreSQL_ROOT_DIR}/include)
