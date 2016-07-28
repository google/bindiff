# BinExport [![Build Status](https://api.travis-ci.org/google/binexport.svg?branch=master)](https://travis-ci.org/google/binexport) [![Coverity Scan Build Status](https://scan.coverity.com/projects/8977/badge.svg)](https://scan.coverity.com/projects/google-binexport)

Copyright 2011-2016 Google Inc.

Disclaimer: This is not an official Google product (experimental or otherwise),
it is just code that happens to be owned by Google.

## Introduction

BinExport is the exporter component of the [BinNavi
project](https://github.com/google/binnavi). It is a plugin for the commercial
IDA Pro disassembler and exports disassemblies into the PostgreSQL database
format that BinNavi requires. A previous version (`zynamics_binexport_8`) also
ships with [BinDiff](http://www.zynamics.com/bindiff.html), serving a similar
purpose.

This repository contains the complete source code necessary to build the IDA Pro
plugin for Linux, Windows and macOS.

## Installation

Download the binaries from the release page and copy them into the IDA Pro
plugins directory. These are the default paths:

OS      | Plugin path
--------|-------------------------------------------
Linux   | `/opt/ida-6.9/plugins`
macOS   | `/Applications/IDA Pro 6.9/idabin/plugins`
Windows | `%ProgramFiles(x86)%\IDA 6.9\plugins`

Note (Windows only): Due to the way the BinExport build works currently, you
also have to copy the PostgreSQL client libray and SSL libraries to the IDA
installation directory. See The "Build BinExport" section below.

## Usage

The main use case is via [BinNavi](https://github.com/google/binnavi).
However, BinExport can also be used to export IDA Pro disassembly to files of
various formats:
* Protocol Buffer based full export
* Statistics text file
* Text format for debugging
* BinNavi database export into a PostgreSQL database

### Verifying the installation version

1. In IDA, select `Help`|`About programm...`
2. Click `Addons...`
3. If installed correctly, the following dialog box appears:

   ![IDA addons dialog](/doc/binexport9-ida-addons-dialog.png)

### Invocation

#### Via the UI

1. Open an IDB
2. Select `Edit`|`Plugins`|`BinExport 9`
3. The following dialog box appears:

   ![BinExport plugin dialog](/doc/binexport9-plugin-dialog.png)

4. Select the type of the file to be exported

Note: There is no UI for the database export.

### IDC Scripting

The BinExport plugin registers the IDC functions below. The function names
are versioned in order to support side-by-side installation of different
versions (i.e. BinDiff's BinExport 8).

IDC Function name     | Exports to           | Arguments
----------------------|----------------------|--------------------
BinExport2Sql9        | PostgreSQL database  | host, port, database, schema, user, password
BinExport2Diff9       | Protocol Buffer      | filename
BinExport2Text9       | Text file dump       | filename
BinExport2Statistics9 | Statistics text file | filename

BinExport also supports exporting to a database via the `RunPlugin()` IDC
function:

    static main() {
      Batch(0);
      Wait();
      RunPlugin("zynamics_binexport_9", 1);
      Exit(0);
    }

Use the plugin options listed below to setup the database connection in that
case. See also the `CBinExportImporter` class in BinNavi.

### Plugin Options

BinExport defines the following plugin options, that can be specified on IDA's
command line:

Option                           | Description
---------------------------------|------------------------------------------------
`-OExporterHost:<HOST>`          | Database server to connect to
`-OExporterPort:<PORT>`          | Port to connect to. PostgreSQL default is 5432.
`-OExporterUser:<USER>`          | User name
`-OExporterPassword:<PASS>`      | Password
`-OExporterDatabase:<DB>`        | Database to use
`-OExporterSchema:<SCHEMA>`      | Database schema. BinNavi only uses "public".
`-OExporterLogFile:<FILE>`       | Log messages to a file
`-OExporterAlsoLogToStdErr:TRUE` | If specified, also log to standard error

Note that these options must come before any files.

## How to build

### Preparing the build environment

As we support exporting into PostgreSQL databases as well as a Protocol Buffer
based format, there are quite a few dependencies to satisfy:

* Boost 1.55.0 or higher (a partial copy of 1.61.0 ships in
  `third_party/boost_parts`)
* [CMake](https://cmake.org/download/) 2.8.11 or higher
* GCC 4.8 or a recent version of Clang on Linux/macOS. On Windows, use the
  Visual Studio 2013 compiler and the Windows SDK for Windows 8.1/10.
* Git 1.8 or higher
* IDA SDK 6.9 (unpack into `third_party/idasdk`)
* OpenSSL 1.0.1 or higher (checked out as a sub-module)
* Perl 5.6 or higher (needed for OpenSSL and PostgreSQL)
* PostgreSQL client libraries 9.3 or higher
* Protocol Buffers 3.0.0 beta 2 or higher (checked out as a sub-module)

### Linux

#### Prerequisites

The preferred build environment is Ubuntu 14.04 LTS (64-bit Intel).

This should install all the necessary packages:

    sudo dpkg --add-architecture i386
    sudo add-apt-repository ppa:ubuntu-toolchain-r/test -y
    sudo apt-get update -qq
    sudo apt-get install -qq --no-install-recommends \
        build-essential cmake \
        g++-4.8:amd64 g++:amd64 lib32stdc++-4.8-dev \
    export CXX="g++-4.8" CC="gcc-4.8"

The following sections assume that your current working directory is at the
root of the cloned repository.

Initialize the submodules in the `third_party` directory:

    git submodule update --init --recursive

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands
are for IDA 6.9:

    unzip PATH/TO/idasdk69.zip -d third_party/idasdk
    mv third_party/idasdk/idasdk69/* third_party/idasdk
    rmdir third_party/idasdk/idasdk69

#### OpenSSL

Configure and build 32-bit static libraries of OpenSSL:

    cd third_party/openssl
    ./Configure no-asm no-dso no-krb5 no-shared no-zlib linux-generic32 -m32 \
        --prefix=${PWD}/../../build_linux/openssl
    make -s
    make install
    cd ../..

##### Using the distribution packages of OpenSSL

You may skip the above and install the `libssl-dev:i386` package. If you do
that, you also need to adjust the paths of the OpenSSL libraries when
configuring the source:

    ...
    -DOPENSSL_CRYPTO_LIBRARY=/usr/lib/i386-linux-gnu/libcrypto.a \
    -DOPENSSL_SSL_LIBRARY=/usr/lib/i386-linux-gnu/libssl.a \
    ...

#### PostgreSQL client libraries

Download the PostgreSQL source file distribution from its
[download page](https://ftp.postgresql.org/pub/source/v9.5.3/postgresql-9.5.3.tar.bz2).

Then extract it to `build_linux\postgresql`:

    tar xjf PATH/TO/postgresql-9.5.3.tar.bz2
    mv postgresql-9.5.3 third_party/postgresql

Configure and build static libraries:

    cd third_party/postgresql
    ./configure CFLAGS=-m32 CXXFLAGS=-m32 LDFLAGS=-m32 \
        --prefix=${PWD}/../../build_linux/postgresql --disable-debug \
        --disable-profiling --disable-coverage --without-tcl --without-perl \
        --without-python --without-gssapi --without-ldap --without-bonjour \
        --without-readline
    make -is -C src/include install
    make -s -C src/interfaces install
    cd ../..

##### Using the distribution packages of PostgreSQL

Like with OpenSSL, you may skip the above and install the `libpq-dev:i386`
package and its dependencies:

    sudo apt-get install libpq-dev:i386 libpq5:i386 krb5-multidev:i386

If you do that, you can omit the line starting with `-DPostgreSQL` when
configuring the BinExport source later.

#### Protocol Buffers

Configure and build 32-bit static libraries of Protocol Buffers. This will
also build the compiler, `protoc`:

    cd third_party/protobuf
    ./autogen.sh && ./configure CFLAGS=-m32 CXXFLAGS=-m32 LDFLAGS=-m32 \
        --disable-dependency-tracking \
        --disable-maintainer-mode \
        --enable-silent-rules \
        --prefix=${PWD}/../../build_linux/protobuf
    make -s -j$(nproc)
    make -s install
    cd ../..

Note that this will download Google Mock if not already present in
`third_party/protobuf/gmock`.

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

    ln -sf ../.. third_party/zynamics/binexport
    cd build_linux
    cmake \
        -DCMAKE_FIND_ROOT_PATH=${PWD} \
        -DCMAKE_FIND_ROOT_PATH_MODE_INCLUDE=ONLY \
        -DCMAKE_FIND_ROOT_PATH_MODE_LIBRARY=ONLY \
        -DOPENSSL_ROOT_DIR=${PWD}/openssl \
        -DPostgreSQL_ROOT=${PWD}/postgresql \
        ..
    make

Note: If you chose to use the distribution packages of OpenSSL, add the
additional flags mentioned above.

If all went well, the `build_linux` directory should contain two IDA plugin
binaries `zynamics_binexport_9.plx` and `zynamics_binexport_9.plx64` for use
with `idaq` and `idaq64`, respectively.

### macOS

#### Prerequisites

The preferred build environment is Mac OS X 10.11.3 "El Capitan" (64-bit
Intel) using Xcode 7.2.1. Using the latest public beta of macOS "Sierra" should
also work.

After installing the Developer Tools, make sure to install the command-line
tools:

    sudo xcode-select --install

Recent versions of the Developer Tools no longer include GNU Autotools.
You can install those via [Homebrew](http://brew.sh/) (recommended) or via
[MacPorts](https://www.macports.org/install.php). Follow the installation
instructions on the respective websites.

For Homebrew:

    brew install autoconf automake libtool

For MacPorts:

    sudo /opt/local/bin/port install autoconf automake libtool

The following sections assume that your current working directory is at the
root of the cloned repository.

Initialize the submodules in the `third_party` directory:

    git submodule update --init --recursive

#### CMake

Download the latest stable version of CMake from the official site and mount
its disk image:

    curl -L https://cmake.org/files/v3.4/cmake-3.4.3-Darwin-x86_64.dmg \
        -o $HOME/Downloads/cmake-osx.dmg
    hdiutil attach $HOME/Downloads/cmake-osx.dmg

At this point you will need to review and accept CMake's license agreement.
Now install CMake:

    sudo cp -Rf /Volumes/cmake-3.4.3-Darwin-x86_64/CMake.app /Applications/
    hdiutil detach /Volumes/cmake-3.4.3-Darwin-x86_64
    sudo /Applications/CMake.app/Contents/bin/cmake-gui --install

The last command makes CMake available in the system path.

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands
are for IDA 6.9:

    unzip PATH/TO/idasdk69.zip -d third_party/idasdk
    mv third_party/idasdk/idasdk69/* third_party/idasdk
    rmdir third_party/idasdk/idasdk69

#### OpenSSL

Configure and build 32-bit static libraries of OpenSSL:

    cd third_party/openssl
    ./Configure no-asm no-zlib darwin-i386-cc
    make -s -j$(sysctl -n hw.ncpu)
    cd ../..

Warnings about object files in `libcrypto.a` having no symbols can safely be
ignored.

#### Using the Developer Tools' version of OpenSSL

You can skip the above and link against the version of OpenSSL that ships with
Apple's Developer Tools. This is not recommended, however, since that version
is quite old (0.9.8).

#### PostgreSQL client libraries

The OS X Developer Tools actually ship with a version of the PostgreSQL client
libraries. However, these were only built for 64-bit and so cannot be used for
building the BinExport plugin (which, because of IDA Pro, is always 32-bit).

Download the official packages and do an unattended installation of
PostgreSQL. Unfortunately there is no ready-made package for just the client
libraries, so this installs a full database server.

    curl -L http://get.enterprisedb.com/postgresql/postgresql-9.3.11-1-osx.dmg \
        -o $HOME/Downloads/postgresql-osx.dmg
    hdiutil attach $HOME/Downloads/postgresql-osx.dmg
    sudo /Volumes/PostgreSQL\ 9.3.11-1/postgresql-9.3.11-1-osx.app/Contents/MacOS/osx-intel \
        --mode unattended --unattendedmodeui none \
        --disable-stackbuilder 1 --create_shortcuts 0
    hdiutil detach /Volumes/PostgreSQL\ 9.3.11-1

Optional: Remove the "postgres" user, if running a full PostgreSQL instance
on the local machine is not desired:

    sudo dscl . -delete /Users/postgres

Note: You can also download the PostgreSQL source code and build the client
libraries manually.

#### Protocol Buffers

Configure and build 32-bit static libraries of Protocol Buffers. This will
also build the compiler, `protoc`:

    cd third_party/protobuf
    ./autogen.sh && ./configure CFLAGS=-m32 CXXFLAGS=-m32 LDFLAGS=-m32 \
        --disable-dependency-tracking \
        --disable-maintainer-mode \
        --enable-silent-rules
    make -s -j$(sysctl -n hw.ncpu)
    cd ../..

Note that this will download Google Mock if not already present in
`third_party/protobuf/gmock`.

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

    ./configure \
        -DOPENSSL_ROOT_DIR=${PWD}/third_party/openssl \
        -DPostgreSQL_INCLUDE_DIR=/Library/PostgreSQL/9.3/include \
        -DPostgreSQL_LIBRARY=/Library/PostgreSQL/9.3/lib/libpq.a \
        -DOPENSSL_LIBRARIES=third_party/openssl/libssl.a:third_party/openssl/libcrypto.a
    make

Note: If you chose to use the distribution packages of OpenSSL, add the
additional flags mentioned above.

If all went well, the source tree should contain two IDA plugin binaries
`zynamics_binexport_9.pmc` and `zynamics_binexport_9.pmc64` for use with
`idaq` and `idaq64`, respectively.

### Windows

The preferred build environment is Windows 8.1 (64-bit Intel) using the
Visual Studio 2015 compiler and the
[Windows SDK for Windows 8.1](https://dev.windows.com/en-us/downloads/windows-8-1-sdk).
The previous Visual Studio 2013 also works.

#### CMake

Download and install CMake from its
[download page](https://cmake.org/download/). Make sure to select "Add CMake
to the system PATH for all users".

#### Git

Download and install Git from its
[download page](https://git-scm.com/download/win). Make sure to select the
following options:
* The installation directory should be left at the default
  `%ProgramFiles%\Git\bin\git.exe`
* "Use Git from the Windows Command Prompt" - have the setup utility add Git
   to your system path.
* "Use Windows' default console window" - to be able to use Git from the
  regular command prompt.

### Perl

Download and install ActiveState Perl from its
[download page](http://www.activestate.com/activeperl/downloads). This should
add Perl to the system path.

#### Prepare

The following sections assume an open command prompt with the current working
directory located at the root of the cloned BinExport repository:

    git clone https://github.com/google/binexport.git
    cd binexport

Initialize the submodules in the `third_party` directory and create the build
directory for an out of source build:

    git submodule update --init --recursive
    mkdir build_msvc

Make the Visual Studio compiler and build tools available (Use
`VS120COMNTOOLS` for Visual Studio 2013):

    call "%VS140COMNTOOLS%\..\..\VC\vcvarsall.bat" x86

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands
are for IDA 6.9, assuming that Git was installed into the default directory
first:

    "%ProgramFiles%\Git\usr\bin\unzip" PATH\TO\idasdk69.zip -d third_party
    rename third_party\idasdk69 idasdk

#### OpenSSL

Configure and build 32-bit static libraries of OpenSSL:

    cd third_party\openssl
    perl Configure VC-WIN32 no-asm
    call ms\do_ms
    nmake /f ms\nt.mak ^
        INSTALLTOP=..\..\build_msvc\openssl ^
        OPENSSLDIR=..\..\build_msvc\openssl install
    cd ..\..

#### PostgreSQL client libraries

Download the PostgreSQL source file distribution from its
[download page](https://ftp.postgresql.org/pub/source/v9.5.3/postgresql-9.5.3.tar.bz2).

Then extract it to `build_msvc\postgresql`, again using the utilities that
ship with Git:

    "%ProgramFiles%\Git\usr\bin\bunzip2" -c PATH\TO\postgresql-9.5.3.tar.bz2 ^
        | "%ProgramFiles%\Git\usr\bin\tar" xf -
    move postgresql-9.5.3 third_party\postgresql

Configure and build static libraries:

    cd third_party\postgresql\src\tools\msvc
    echo $config-^>{openssl} = '..\..\build_msvc\openssl'; > config.pl
    "%ProgramFiles%\Git\usr\bin\sed" -i ^
        "s/MultiThreadedDLL/MultiThreaded/" MSBuildProject.pm
    perl mkvcbuild.pl
    cd ..\..\..
    msbuild pgsql.sln /t:interfaces\libpq ^
        /p:Configuration="Release" ^
        /p:ConfigurationType=StaticLibrary

Now copy include files and libraries:

    perl -e "use lib 'src/tools/msvc';use Install;Install::CopyIncludeFiles('..\..\build_msvc\postgresql')"
    mkdir ..\..\build_msvc\postgresql\lib
    copy Release\libpgport\libpgport.lib ..\..\build_msvc\postgresql\lib
    copy Release\libpq\libpq.lib ..\..\build_msvc\postgresql\lib
    cd ..\..

#### Protocol Buffers

Configure and build 32-bit static libraries of Protocol Buffers. This will
also build the compiler, `protoc.exe`:

    cd third_party\protobuf
    mkdir build_msvc
    cd build_msvc
    cmake ^
        -G "Visual Studio 14 2015" ^
        -Dprotobuf_BUILD_SHARED_LIBS=OFF ^
        -Dprotobuf_BUILD_TESTS=OFF ^
        ..\cmake
    msbuild protobuf.sln /p:Platform=Win32 /p:Configuration=Release
    cd ..\..\..

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

    mklink /J third_party\zynamics\binexport .
    cd build_msvc
    cmake ^
        -DCMAKE_FIND_ROOT_PATH="%cd%" ^
        -DCMAKE_FIND_ROOT_PATH_MODE_INCLUDE=ONLY ^
        -DCMAKE_FIND_ROOT_PATH_MODE_LIBRARY=ONLY ^
        ..
    msbuild binexport.sln /p:Configuration=Release

If all went well, the `build_msvc` directory should contain two IDA plugin
binaries `zynamics_binexport_9.plw` and `zynamics_binexport_9.p64` for use with
`idaq.exe` and `idaq64.exe`, respectively.
