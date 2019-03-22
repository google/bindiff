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

set(openssl_dir ${CMAKE_CURRENT_BINARY_DIR}/openssl)
set(postgresql_dir ${CMAKE_CURRENT_BINARY_DIR}/postgresql)
set(postgresql_src_dir ${postgresql_dir}/src/postgresql)
if(WIN32)
  file(TO_NATIVE_PATH ${postgresql_src_dir} postgresql_src_dir_native)
  file(TO_NATIVE_PATH ${postgresql_dir} postgresql_inst)

  # Set OpenSSL location and create header install script
  file(WRITE ${postgresql_dir}/tmp/config.pl
             "$config->{openssl} = '${openssl_dir}';")
  file(WRITE ${postgresql_dir}/tmp/pg_install_headers.pl
             "use lib 'src/tools/msvc';use Install;"
             "Install::CopyIncludeFiles('${postgresql_inst}');")

  set(POSTGRESQL_CONFIGURE_COMMAND
    cd /d "${postgresql_src_dir_native}\\src\\tools\\msvc"
    COMMAND "${CMAKE_COMMAND}" -E copy
            "${postgresql_dir}/tmp/config.pl"
            "${postgresql_src_dir}/src/tools/msvc/config.pl"
    COMMAND "${CMAKE_COMMAND}" -E copy
            "${postgresql_dir}/tmp/pg_install_headers.pl"
            "${postgresql_src_dir}"
    COMMAND perl -pi.bak -e "s/MultiThreadedDLL/MultiThreaded/g"
            MSBuildProject.pm
    COMMAND perl mkvcbuild.pl)
  set(POSTGRESQL_BUILD_COMMAND
    cd /d "${postgresql_src_dir_native}"
    COMMAND msbuild "${postgresql_src_dir_native}\\pgsql.sln"
            /t:interfaces\\libpq
            /p:Configuration=Release
            /p:ConfigurationType=StaticLibrary
            /m
  )
  set(POSTGRESQL_INSTALL_COMMAND
    cd /d "${postgresql_src_dir_native}"
    COMMAND perl pg_install_headers.pl
    COMMAND "${CMAKE_COMMAND}" -E copy "Release/libpgport/libpgport.lib"
            "${postgresql_dir}/lib/libpgport.lib"
    COMMAND "${CMAKE_COMMAND}" -E copy "Release/libpq/libpq.lib"
            "${postgresql_dir}/lib/libpq.lib"
  )
elseif(UNIX)
  set(POSTGRESQL_CONFIGURE_COMMAND
    env "LD_LIBRARY_PATH=${openssl_dir}/lib"
    "<SOURCE_DIR>/configure"
    --disable-debug
    --disable-profiling
    --disable-coverage
    --without-tcl
    --without-perl
    --without-python
    --without-gssapi
    --without-ldap
    --without-bonjour
    --without-readline
    --without-zlib
    --with-openssl
    "--with-includes=${openssl_dir}/include"
    "--with-libraries=${openssl_dir}/lib"
    "--prefix=<INSTALL_DIR>"
  )
  set(POSTGRESQL_BUILD_COMMAND
    $(MAKE) -sC "<SOURCE_DIR>/src/interfaces/libpq"
  )
  set(POSTGRESQL_INSTALL_COMMAND
    $(MAKE) -isC "<SOURCE_DIR>/src/include" install
    COMMAND $(MAKE) -sC "<SOURCE_DIR>/src/interfaces/libpq" install
    BUILD_IN_SOURCE 1
  )
endif()

ExternalProject_Add(postgresql
  DEPENDS openssl
  URL https://ftp.postgresql.org/pub/source/v9.6.6/postgresql-9.6.6.tar.gz
  URL_HASH SHA256=53e1cd5fdff5f45415ae9d5b645177275265a3e800c86becbb94ce183a3a5061
  PREFIX ${postgresql_dir}
  CONFIGURE_COMMAND ${POSTGRESQL_CONFIGURE_COMMAND}
  BUILD_COMMAND ${POSTGRESQL_BUILD_COMMAND}
  INSTALL_COMMAND ${POSTGRESQL_INSTALL_COMMAND}
)
ExternalProject_Get_Property(postgresql INSTALL_DIR)
list(APPEND CMAKE_PREFIX_PATH ${INSTALL_DIR})
