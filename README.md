# BinExport [![Build Status](https://travis-ci.org/cblichmann/binexport.svg?branch=master)](https://travis-ci.org/cblichmann/binexport)

Copyright 2011-2019 Google LLC.

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

| OS      | Plugin path                                 |
| ------- | ------------------------------------------- |
| Linux   | `/opt/idapro-7.4/plugins`                   |
| macOS   | `/Applications/IDA Pro 7.4/idabin/plugins`  |
| Windows | `%ProgramFiles(x86)%\IDA 7.4\plugins`       |

To install just for the current user, copy the files into one of these
directories instead:

| OS          | Plugin path                          |
| ----------- | ------------------------------------ |
| Linux/macOS | `~/.idapro/plugins`                  |
| Windows     | `%AppData%\Hex-Rays\IDA Pro\plugins` |


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

    ![IDA addons dialog](/doc/binexport10-ida-addons-dialog.png)

### Invocation

#### Via the UI

1.  Open an IDB
2.  Select `Edit`|`Plugins`|`BinExport 10`
3.  The following dialog box appears:

    ![BinExport plugin dialog](/doc/binexport10-plugin-dialog.png)

4.  Select the type of the file to be exported

Note: There is no UI for the database export.

### IDC Scripting

The BinExport plugin registers the IDC functions below. The function names are
versioned in order to support side-by-side installation of different versions
(i.e. BinDiff's BinExport 8).

| IDC Function name   | Exports to           | Arguments                                    |
| ------------------- | -------------------- | -------------------------------------------- |
| BinExportSql        | PostgreSQL database  | host, port, database, schema, user, password |
| BinExportDiff       | Protocol Buffer      | filename                                     |
| BinExportText       | Text file dump       | filename                                     |
| BinExportStatistics | Statistics text file | filename                                     |

BinExport also supports exporting to a database via the `RunPlugin()` IDC
function:

```c
static main() {
  batch(0);
  auto_wait();
  load_and_run_plugin("binexport10", 1);
  qexit(0);
}
```

Use the plugin options listed below to setup the database connection in that
case. See also the `CBinExportImporter` class in BinNavi.

#### IDAPython

The option flags are the same as IDC (listed above).

```python
import idaapi
idc_lang = idaapi.find_extlang_by_name("idc")
idaapi.run_statements(
    'BinExportSql("{}", {}, "{}", "{}", "{}", "{}")'.format(
        "host", 5432, "database", "public", "user", "pass"), idc_lang)
```

### Plugin Options

BinExport defines the following plugin options, that can be specified on IDA's
command line:

| Option                                  | Description                                                            |
| --------------------------------------- | ---------------------------------------------------------------------- |
| `-OBinExportAutoAction:<ACTION>`        | Invoke a BinExport IDC function and exit                               |
| `-OBinExportModule:<PARAM>`             | Argument for `BinExportAutoAction`                                     |
| `-OBinExportHost:<HOST>`                | Database server to connect to                                          |
| `-OBinExportPort:<PORT>`                | Port to connect to. PostgreSQL default is 5432.                        |
| `-OBinExportUser:<USER>`                | User name                                                              |
| `-OBinExportPassword:<PASS>`            | Password                                                               |
| `-OBinExportDatabase:<DB>`              | Database to use                                                        |
| `-OBinExportSchema:<SCHEMA>`            | Database schema. BinNavi only uses "public".                           |
| `-OBinExportLogFile:<FILE>`             | Log messages to a file                                                 |
| `-OBinExportAlsoLogToStdErr:TRUE`       | If specified, also log to standard error                               |
| `-OBinExportX86NoReturnHeuristic:FALSE` | Disable the X86-specific heuristic to identify non-returning functions |

Note that these options must come before any files.

## How to build

### Preparing the build environment

As we support exporting into PostgreSQL databases as well as a Protocol Buffer
based format, there are quite a few dependencies to satisfy:

*   Boost 1.67.0 or higher (a partial copy of 1.71.0 ships in
    `third_party/boost_parts`)
*   [CMake](https://cmake.org/download/) 3.12 or higher
*   GCC 7 or a recent version of Clang on Linux/macOS. On Windows, use the
    Visual Studio 2017 compiler (need at least Update 9) and the Windows SDK
    for Windows 10.
*   Git 1.8 or higher
*   IDA SDK 7.4 (unpack into `third_party/idasdk`)
*   OpenSSL 1.0.2 or higher
*   Perl 5.6 or higher (needed for OpenSSL and PostgreSQL)
*   PostgreSQL client libraries 9.3 or higher
*   Protocol Buffers 3.6.1 or higher

### Linux

#### Prerequisites

The preferred build environment is Debian testing (version 10, "Buster").

This should install all the necessary packages:

```bash
sudo apt update -qq
sudo apt install -qq --no-install-recommends build-essential cmake
```

The following sections assume that your current working directory is at the root
of the cloned repository.

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 7.4:

```bash
unzip PATH/TO/idasdk74.zip -d third_party/idasdk
mv third_party/idasdk/idasdk74/* third_party/idasdk
rmdir third_party/idasdk/idasdk74
```

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

```bash
mkdir -p build_linux && cd build_linux
cmake ../cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_RULE_MESSAGES=OFF \
    -DIdaSdk_ROOT_DIR=$PWD/../third_party/idasdk
cmake --build .
```

This will download and build OpenSSL, Protocol Buffers and the PostgreSQL client
libraries. If all went well, the `build_linux/binexport-prefix` directory should
contain two IDA plugin binaries `binexport10.so` and `binexport1064.so` for use
with `ida` and `ida64`, respectively.

### macOS

#### Prerequisites

The preferred build environment is Mac OS X 10.12 "Sierra" (64-bit Intel) using
Xcode 8.1. Using macOS 10.13 "High Sierra" should also work.

After installing the Developer Tools, make sure to install the command-line
tools:

```bash
sudo xcode-select --install
```

Recent versions of the Developer Tools no longer include GNU Autotools, which is
required by the PostgreSQL dependency. You can install Autotools via
[Homebrew](http://brew.sh/) (recommended) or via
[MacPorts](https://www.macports.org/install.php). Follow the installation
instructions on the respective websites.

For Homebrew:

```bash
brew install autoconf automake libtool
```

For MacPorts:

```bash
sudo /opt/local/bin/port install autoconf automake libtool
```

The following sections assume that your current working directory is at the root
of the cloned repository.

#### CMake

Download the latest stable version of CMake from the official site and mount its
disk image:

```bash
curl -L https://cmake.org/files/v3.7/cmake-3.7.2-Darwin-x86_64.dmg \
    -o $HOME/Downloads/cmake-osx.dmg
hdiutil attach $HOME/Downloads/cmake-osx.dmg
```

At this point you will need to review and accept CMake's license agreement. Now
install CMake:

```bash
sudo cp -Rf /Volumes/cmake-3.7.2-Darwin-x86_64/CMake.app /Applications/
hdiutil detach /Volumes/cmake-3.7.2-Darwin-x86_64
sudo /Applications/CMake.app/Contents/bin/cmake-gui --install
```

The last command makes CMake available in the system path.

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 7.4:

```bash
unzip PATH/TO/idasdk74.zip -d third_party/idasdk
mv third_party/idasdk/idasdk74/* third_party/idasdk
rmdir third_party/idasdk/idasdk74
```

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

```bash
mkdir -p build_mac && cd build_mac
cmake ../cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_RULE_MESSAGES=OFF \
    -DIdaSdk_ROOT_DIR=$PWD/../third_party/idasdk
cmake --build .
```

This will download and build OpenSSL, Protocol Buffers and the PostgreSQL client
libraries. If all went well, the `build_mac/binexport-prefix` directory should
contain two IDA plugin binaries `binexport10.dylib` and `binexport1064.dylib`
for use with `ida` and `ida64`, respectively.

### Windows

The preferred build environment is Windows 10 (64-bit Intel) using the Visual
Studio 2017 compiler and the [Windows SDK for Windows
10](https://dev.windows.com/en-us/downloads/windows-10-sdk).

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

```bat
git clone https://github.com/google/binexport.git
cd binexport
```

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 7.4, assuming that Git was installed into the default directory first:

```bat
"%ProgramFiles%\Git\usr\bin\unzip" PATH\TO\idasdk74.zip -d third_party
rename third_party\idasdk74 idasdk
```

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

```bat
if not exist build_msvc mkdir build_msvc
cd build_msvc
cmake ../cmake -DIdaSdk_ROOT_DIR=%cd%\..\third_party\idasdk ^
    -G "Visual Studio 15 2017 Win64"
cmake --build . --config Release --install -- /m /clp:NoSummary;ForceNoAlign /v:minimal
```

This will download and build OpenSSL, Protocol Buffers and the PostgreSQL client
libraries. If all went well, the `build_msvc\binexport-prefix` directory should
contain two IDA plugin binaries `binexport10.dll` and `binexport1064.dll` for use
with `ida.exe` and `ida64.exe`, respectively.
