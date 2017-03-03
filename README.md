# BinExport [![Build Status](https://api.travis-ci.org/google/binexport.svg?branch=master)](https://travis-ci.org/google/binexport) [![Coverity Scan Build Status](https://scan.coverity.com/projects/8977/badge.svg)](https://scan.coverity.com/projects/google-binexport)

Copyright 2011-2017 Google Inc.

Disclaimer: This is not an official Google product (experimental or otherwise),
it is just code that happens to be owned by Google.

# Table of Contents

*   [Introduction](#introduction)
*   [Installation](#installation)
*   [Usage](#usage)
    *   [Verifying the installation
        version](#verifying-the-installation-version)
    *   [Invocation](#invocation)
        *   [Via the UI](#via-the-ui)
    *   [IDC Scripting](#idc-scripting)
    *   [IDAPython](#idapython)
    *   [Plugin Options](#plugin-options)
*   [How to build](#how-to-build)
    *   [Preparing the build environment](#preparing-the-build-environment)
    *   [Linux](#linux)
        *   [Prerequisites](#prerequisites)
        *   [IDA SDK](#ida-sdk)
        *   [Build BinExport](#build-binexport)
    *   [macOS](#macos)
        *   [Prerequisites](#prerequisites-1)
        *   [CMake](#cmake)
        *   [IDA SDK](#ida-sdk-1)
        *   [Build BinExport](#build-binexport-1)
    *   [Windows](#windows)
        *   [CMake](#cmake-1)
        *   [Git](#git)
        *   [Perl](#perl)
        *   [Prepare](#prepare)
        *   [IDA SDK](#ida-sdk-2)
        *   [Build BinExport](#build-binexport-2)

## Introduction

BinExport is the exporter component of the [BinNavi
project](https://github.com/google/binnavi) as well as
[BinDiff](https://www.zynamics.com/software.html). It is a plugin for the
commercial IDA Pro disassembler and exports disassemblies into the PostgreSQL
database format that BinNavi requires.

This repository contains the complete source code necessary to build the IDA Pro
plugin for Linux, macOS and Windows.

## Installation

Download the binaries from the release page and copy them into the IDA Pro
plugins directory. These are the default paths:

OS      | Plugin path
------- | -------------------------------------------
Linux   | `/opt/ida-6.95/plugins`
macOS   | `/Applications/IDA Pro 6.95/idabin/plugins`
Windows | `%ProgramFiles(x86)%\IDA 6.95\plugins`

## Usage

The main use case is via [BinNavi](https://github.com/google/binnavi). However,
BinExport can also be used to export IDA Pro disassembly to files of various
formats:

*   Protocol Buffer based full export
*   Statistics text file
*   Text format for debugging
*   BinNavi database export into a PostgreSQL database

### Verifying the installation version

1.  In IDA, select `Help`|`About programm...`
2.  Click `Addons...`
3.  If installed correctly, the following dialog box appears:

    ![IDA addons dialog](/doc/binexport9-ida-addons-dialog.png)

### Invocation

#### Via the UI

1.  Open an IDB
2.  Select `Edit`|`Plugins`|`BinExport 9`
3.  The following dialog box appears:

    ![BinExport plugin dialog](/doc/binexport9-plugin-dialog.png)

4.  Select the type of the file to be exported

Note: There is no UI for the database export.

### IDC Scripting

The BinExport plugin registers the IDC functions below. The function names are
versioned in order to support side-by-side installation of different versions
(i.e. BinDiff's BinExport 8).

| IDC Function name     | Exports to           | Arguments                     |
| --------------------- | -------------------- | ----------------------------- |
| BinExport2Sql9        | PostgreSQL database  | host, port, database, schema, |
:                       :                      : user, password                :
| BinExport2Diff9       | Protocol Buffer      | filename                      |
| BinExport2Text9       | Text file dump       | filename                      |
| BinExport2Statistics9 | Statistics text file | filename                      |

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

#### IDAPython

The option flags are the same as IDC (listed above).

```python
import idaapi
idc_lang = idaapi.find_extlang_by_name("idc")
idaapi.run_statements("BinExport2Sql9(\"host\", 5342, \"database\", \"public\", \"user\", \"pass\")", idc_lang)
```

### Plugin Options

BinExport defines the following plugin options, that can be specified on IDA's
command line:

| Option                           | Description                               |
| -------------------------------- | ----------------------------------------- |
| `-OExporterHost:<HOST>`          | Database server to connect to             |
| `-OExporterPort:<PORT>`          | Port to connect to. PostgreSQL default is |
:                                  : 5432.                                     :
| `-OExporterUser:<USER>`          | User name                                 |
| `-OExporterPassword:<PASS>`      | Password                                  |
| `-OExporterDatabase:<DB>`        | Database to use                           |
| `-OExporterSchema:<SCHEMA>`      | Database schema. BinNavi only uses        |
:                                  : "public".                                 :
| `-OExporterLogFile:<FILE>`       | Log messages to a file                    |
| `-OExporterAlsoLogToStdErr:TRUE` | If specified, also log to standard error  |

Note that these options must come before any files.

## How to build

### Preparing the build environment

As we support exporting into PostgreSQL databases as well as a Protocol Buffer
based format, there are quite a few dependencies to satisfy:

*   Boost 1.55.0 or higher (a partial copy of 1.61.0 ships in
    `third_party/boost_parts`)
*   [CMake](https://cmake.org/download/) 2.8.11 or higher
*   GCC 4.8 or a recent version of Clang on Linux/macOS. On Windows, use the
    Visual Studio 2015 compiler and the Windows SDK for Windows 8.1/10.
*   Git 1.8 or higher
*   IDA SDK 6.9 (unpack into `third_party/idasdk`)
*   OpenSSL 1.0.1 or higher
*   Perl 5.6 or higher (needed for OpenSSL and PostgreSQL)
*   PostgreSQL client libraries 9.3 or higher
*   Protocol Buffers 3.0.0 beta 2 or higher

### Linux

#### Prerequisites

The preferred build environment is Ubuntu 14.04 LTS (64-bit Intel).

This should install all the necessary packages:

    sudo dpkg --add-architecture i386
    sudo add-apt-repository ppa:ubuntu-toolchain-r/test -y
    sudo apt-get update -qq
    sudo apt-get install -qq --no-install-recommends \
        build-essential cmake g++-4.8-multilib lib32stdc++-4.8-dev \
        linux-libc-dev:i386
    export CXX="g++-4.8" CC="gcc-4.8"

The following sections assume that your current working directory is at the root
of the cloned repository.

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 6.9:

    unzip PATH/TO/idasdk69.zip -d third_party/idasdk
    mv third_party/idasdk/idasdk69/* third_party/idasdk
    rmdir third_party/idasdk/idasdk69

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

    mkdir -p build_linux && cd build_linux
    cmake -DCMAKE_BUILD_TYPE=Release ..
    make

This will download and build OpenSSL, Protocol Buffers and the PostgreSQL client
libraries. If all went well, the `build_linux` directory should contain two IDA
plugin binaries `zynamics_binexport_9.plx` and `zynamics_binexport_9.plx64` for
use with `idaq` and `idaq64`, respectively.

### macOS

#### Prerequisites

The preferred build environment is Mac OS X 10.11.3 "El Capitan" (64-bit Intel)
using Xcode 7.2.1. Using macOS 10.12 "Sierra" should also work.

After installing the Developer Tools, make sure to install the command-line
tools:

    sudo xcode-select --install

Recent versions of the Developer Tools no longer include GNU Autotools, which is
required by the PostgreSQL dependency. You can install Autotools via
[Homebrew](http://brew.sh/) (recommended) or via
[MacPorts](https://www.macports.org/install.php). Follow the installation
instructions on the respective websites.

For Homebrew:

    brew install autoconf automake libtool

For MacPorts:

    sudo /opt/local/bin/port install autoconf automake libtool

The following sections assume that your current working directory is at the root
of the cloned repository.

#### CMake

Download the latest stable version of CMake from the official site and mount its
disk image:

    curl -L https://cmake.org/files/v3.7/cmake-3.7.0-Darwin-x86_64.dmg \
        -o $HOME/Downloads/cmake-osx.dmg
    hdiutil attach $HOME/Downloads/cmake-osx.dmg

At this point you will need to review and accept CMake's license agreement. Now
install CMake:

    sudo cp -Rf /Volumes/cmake-3.7.0-Darwin-x86_64/CMake.app /Applications/
    hdiutil detach /Volumes/cmake-3.7.0-Darwin-x86_64
    sudo /Applications/CMake.app/Contents/bin/cmake-gui --install

The last command makes CMake available in the system path.

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 6.9:

    unzip PATH/TO/idasdk69.zip -d third_party/idasdk
    mv third_party/idasdk/idasdk69/* third_party/idasdk
    rmdir third_party/idasdk/idasdk69

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

    mkdir -p build_mac && cd build_mac
    cmake -DCMAKE_BUILD_TYPE=Release ..
    make

This will download and build OpenSSL, Protocol Buffers and the PostgreSQL client
libraries. If all went well, the `build_mac` directory should contain two IDA
plugin binaries `zynamics_binexport_9.pmc` and `zynamics_binexport_9.pmc64` for
use with `idaq` and `idaq64`, respectively.

### Windows

The preferred build environment is Windows 10 (64-bit Intel) using the Visual
Studio 2015 compiler and the [Windows SDK for Windows
10](https://dev.windows.com/en-us/downloads/windows-10-sdk). The previous Visual
Studio 2013 and earlier versions of Windows also work.

#### CMake

Download and install CMake from its [download
page](https://cmake.org/download/). Make sure to select "Add CMake to the system
PATH for all users".

#### Git

Download and install Git from its [download
page](https://git-scm.com/download/win). Make sure to select the following
options: * The installation directory should be left at the default
`%ProgramFiles%\Git\bin\git.exe` * "Use Git from the Windows Command Prompt" -
have the setup utility add Git to your system path. * "Use Windows' default
console window" - to be able to use Git from the regular command prompt.

### Perl

Download and install ActiveState Perl from its [download
page](http://www.activestate.com/activeperl/downloads). This should add Perl to
the system path.

#### Prepare

The following sections assume an open command prompt with the current working
directory located at the root of the cloned BinExport repository:

    git clone https://github.com/google/binexport.git
    cd binexport

Make the Visual Studio compiler and build tools available (Use `VS120COMNTOOLS`
for Visual Studio 2013):

    call "%VS140COMNTOOLS%\..\..\VC\vcvarsall.bat" x86

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 6.9, assuming that Git was installed into the default directory first:

    "%ProgramFiles%\Git\usr\bin\unzip" PATH\TO\idasdk69.zip -d third_party
    rename third_party\idasdk69 idasdk

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

    if not exist build_msvc mkdir build_msvc
    cd build_msvc
    cmake ..
    msbuild binexport.sln /p:Configuration=Release /m

This will download and build OpenSSL, Protocol Buffers and the PostgreSQL client
libraries. If all went well, the `build_msvc` directory should contain two IDA
plugin binaries `zynamics_binexport_9.plw` and `zynamics_binexport_9.p64` for
use with `idaq.exe` and `idaq64.exe`, respectively.
