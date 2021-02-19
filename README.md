# BinExport

Copyright 2011-2021 Google LLC.

[![Linux Build Status](https://github.com/google/binexport/workflows/linux-build/badge.svg)](https://github.com/google/binexport/actions?query=workflow%3Alinux-build)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/8977/badge.svg)](https://scan.coverity.com/projects/google-binexport)

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

BinExport is the exporter component of
[BinDiff](https://www.zynamics.com/software.html) as well as
[BinNavi](https://github.com/google/binnavi). It is a plugin for the commercial
IDA Pro disassembler and exports disassemblies into the Protocol Buffer format
that BinDiff requires. Exporting into a PostgreSQL databases for BinNavi is
supported on a best-effort basis.

An experimental version for the open source sofware reverse engineering suite
Ghidra is available in the `java/BinExport` directory.

This repository contains the complete source code necessary to build the IDA Pro
plugin for Linux, macOS and Windows.

## Installation

Download the binaries from the release page and copy them into the IDA Pro
plugins directory. These are the default paths:

| OS      | Plugin path                                 |
| ------- | ------------------------------------------- |
| Linux   | `/opt/idapro-7.5/plugins`                   |
| macOS   | `/Applications/IDA Pro 7.5/idabin/plugins`  |
| Windows | `%ProgramFiles%\IDA 7.5\plugins`            |

To install just for the current user, copy the files into one of these
directories instead:

| OS          | Plugin path                          |
| ----------- | ------------------------------------ |
| Linux/macOS | `~/.idapro/plugins`                  |
| Windows     | `%AppData%\Hex-Rays\IDA Pro\plugins` |


## Usage

The main use case is via [BinDiff](https://zynamics.com/bindiff.html). However,
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
2.  Select `Edit`|`Plugins`|`BinExport 11`
3.  The following dialog box appears:

    ![BinExport plugin dialog](/doc/binexport10-plugin-dialog.png)

4.  Select the type of the file to be exported

Note: There is no UI for the database export.

### IDC Scripting

The BinExport plugin registers the IDC functions below.

| IDC Function name   | Exports to           | Arguments                                    |
| ------------------- | -------------------- | -------------------------------------------- |
| BinExportSql¹       | PostgreSQL database  | host, port, database, schema, user, password |
| BinExportDiff       | Protocol Buffer      | filename                                     |
| BinExportText       | Text file dump       | filename                                     |
| BinExportStatistics | Statistics text file | filename                                     |

¹Note: Exporting into PostgreSQL databases is deprecated and requires the
`ENABLE_POSTGRESQL` define to be set during the build.

*Deprecated*: BinExport also supports exporting to a database via the
`RunPlugin()` IDC function:

```c
static main() {
  batch(0);
  auto_wait();
  load_and_run_plugin("binexport11", 1);
  qexit(0);
}
```

Use the plugin options listed below to setup the database connection in that
case. See also the `CBinExportImporter` class in [BinNavi](https://github.com/google/binnavi/blob/master/src/main/java/com/google/security/zynamics/binnavi/Importers/CBinExportImporter.java#L34).

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
| `-OBinExportHost:<HOST>`¹               | Database server to connect to                                          |
| `-OBinExportPort:<PORT>`¹               | Port to connect to. PostgreSQL default is 5432.                        |
| `-OBinExportUser:<USER>`¹               | User name                                                              |
| `-OBinExportPassword:<PASS>`¹           | Password                                                               |
| `-OBinExportDatabase:<DB>`¹             | Database to use                                                        |
| `-OBinExportSchema:<SCHEMA>`¹           | Database schema. BinNavi only uses "public".                           |
| `-OBinExportLogFile:<FILE>`             | Log messages to a file                                                 |
| `-OBinExportAlsoLogToStdErr:TRUE`       | If specified, also log to standard error                               |
| `-OBinExportX86NoReturnHeuristic:FALSE` | Disable the X86-specific heuristic to identify non-returning functions |

Note: These options must come before any files.

¹Note: Exporting into PostgreSQL databases is deprecated and requires the
`ENABLE_POSTGRESQL` define to be set.

## How to build

### Preparing the build environment

As we support exporting into PostgreSQL databases as well as a Protocol Buffer
based format, there are quite a few dependencies to satisfy:

*   Boost 1.71.0 or higher (a partial copy of 1.71.0 ships in
    `third_party/boost_parts`)
*   [CMake](https://cmake.org/download/) 3.14 or higher
*   Suggested: [Ninja](https://ninja-build.org/) for speedy builds
*   GCC 9 or a recent version of Clang on Linux/macOS. On Windows, use the
    Visual Studio 2019 compiler and the Windows SDK for Windows 10.
*   Git 1.8 or higher
*   IDA SDK 7.5 (unpack into `third_party/idasdk`)
*   Dependencies that will be downloaded:
    *   Abseil, GoogleTest and Protocol Buffers (3.14)
    *   Binary Ninja SDK
*   Optional when building with PostgreSQL support:
    *   Perl 5.6 or higher (needed for OpenSSL and PostgreSQL)
    *   OpenSSL 1.0.2 or higher (needs to be 1.0.x series)
    *   PostgreSQL client libraries 9.5 or higher

### Linux

#### Prerequisites

The preferred build environment is Debian testing (version 10, "Buster").

This should install all the necessary packages:

```bash
sudo apt update -qq
sudo apt install -qq --no-install-recommends build-essential
```

Install the latest stable version of CMake:

```bash
wget https://github.com/Kitware/CMake/releases/download/v3.19.1/cmake-3.19.1-Linux-x86_64.sh
mkdir ${HOME}/cmake
sh cmake-3.19.1-Linux-x86_64.sh --prefix=${HOME}/cmake --exclude-subdir
export PATH=${HOME}/cmake/bin:${PATH}
```

The following sections assume that your current working directory is at the root
of the cloned repository.

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 7.5:

```bash
unzip PATH/TO/idasdk75.zip -d third_party/idasdk
mv third_party/idasdk/idasdk75/* third_party/idasdk
rmdir third_party/idasdk/idasdk75
```

#### Build BinExport

With all prerequisites in place, configure and build BinExport and run
its tests:

```bash
mkdir -p build_linux && cd build_linux
cmake .. \
    -G Ninja \
    -DCMAKE_BUILD_TYPE=Release \
    "-DCMAKE_INSTALL_PREFIX=${PWD}" \
    "-DIdaSdk_ROOT_DIR=${PWD}/../third_party/idasdk"
cmake --build . --config Release
ctest --build-config Release --output-on-failure
cmake --install . --config Release --strip
```

Note: If you don't want to use Ninja to perform the actual build, omit
the `-G Ninja` part.

This will download and build Abseil, GoogleTest, Protocol Buffers and the
Binary Ninja API. If all went well, the `build_linux/binexport-prefix`
directory should contain two the files `binexport11.so` and
`binexport1164.so` (for use with `ida` and `ida64`, respectively) as well
as `binexport11_binaryninja.so` (for Binary Ninja).

To enable support for exporting into PostgreSQL databases, add
`-DBINEXPORT_ENABLE_POSTGRESQL=ON` to the first CMake command. Note that
this feature is deprecated.


### macOS

#### Prerequisites

The preferred build environment is Mac OS X 10.15 "Catalina" using Xcode
11.3. Using macOS 11 "Big Sur" and/or Xcode 12 should also work.

After installing the Developer Tools, make sure to install the command-line
tools:

```bash
sudo xcode-select --install
```

Optional, only necessary for the deprecated PostgreSQL support: Current
versions of the Developer Tools no longer include GNU Autotools, which is
required by PostgreSQL. You can install Autotools via
[Homebrew](http://brew.sh/):

```bash
brew install autoconf automake libtool
```

The following sections assume that your current working directory is at the root
of the cloned repository.

#### CMake

Download the latest stable version of CMake from the official site and mount its
disk image:

```bash
curl -fsSL https://github.com/Kitware/CMake/releases/download/v3.19.1/cmake-3.19.1-Darwin-x86_64.dmg \
    -o $HOME/Downloads/cmake-osx.dmg
hdiutil attach $HOME/Downloads/cmake-osx.dmg
```

At this point you will need to review and accept CMake's license agreement. Now
install CMake:

```bash
sudo cp -Rf /Volumes/cmake-3.19.1-Darwin-x86_64/CMake.app /Applications/
hdiutil detach /Volumes/cmake-3.19.1-Darwin-x86_64
sudo /Applications/CMake.app/Contents/bin/cmake-gui --install
```

The last command makes CMake available in the system path.

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 7.5:

```bash
unzip PATH/TO/idasdk75.zip -d third_party/idasdk
mv third_party/idasdk/idasdk75/* third_party/idasdk
rmdir third_party/idasdk/idasdk75
```

#### Build BinExport

With all prerequisites in place, configure and build BinExport and run
its tests:

```bash
mkdir -p build_mac && cd build_mac
cmake .. \
    -DCMAKE_BUILD_TYPE=Release \
    "-DCMAKE_INSTALL_PREFIX=${PWD}" \
    "-DIdaSdk_ROOT_DIR=${PWD}/../third_party/idasdk"
cmake --build . --config Release -- "-j$(sysctl -n hw.logicalcpu)"
ctest --build-config Release --output-on-failure
cmake --install . --config Release --strip
```

Note: This will use the standard CMake "Makefile Generator". You can use XCode
or Ninja as generators as well.

This will download and build Abseil, GoogleTest, Protocol Buffers and the
Binary Ninja API. If all went well, the `build_mac/binexport-prefix`
directory should contain two the files `binexport11.dylib` and
`binexport1164.dylib` (for use with `ida` and `ida64`, respectively) as well
as `binexport11_binaryninja.dylib` (for Binary Ninja).

To enable support for exporting into PostgreSQL databases, add
`-DBINEXPORT_ENABLE_POSTGRESQL=ON` to the first CMake command. Note that
this feature is deprecated.


### Windows

The preferred build environment is Windows 10 (64-bit Intel) using the Visual
Studio 2017 compiler and the [Windows SDK for Windows
10](https://dev.windows.com/en-us/downloads/windows-10-sdk).

#### CMake

Download and install the lastest stable CMake (3.19.1 at the time of writing)
from its [download page](https://cmake.org/download/). Make sure to select
"Add CMake to the system PATH for all users".

#### Git

Download and install Git from its [download
page](https://git-scm.com/download/win). Make sure to select the following
options: * The installation directory should be left at the default
`%ProgramFiles%\Git\bin\git.exe` * "Use Git from the Windows Command Prompt" -
have the setup utility add Git to your system path. * "Use Windows' default
console window" - to be able to use Git from the regular command prompt.

### Perl

Note: This is only needed for the deprecated PostgreSQL support and can be
skipped safely.

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
for IDA 7.5, assuming that Git was installed into the default directory first:

```bat
"%ProgramFiles%\Git\usr\bin\unzip" PATH\TO\idasdk75.zip -d third_party
rename third_party\idasdk74 idasdk
```

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

```bat
if not exist build_msvc mkdir build_msvc
cd build_msvc
cmake .. ^
    -G "Visual Studio 16 2019 Win64" ^
    -DCMAKE_BUILD_TYPE=Release ^
    "-DCMAKE_INSTALL_PREFIX=%cd%" ^
    -DIdaSdk_ROOT_DIR=%cd%\..\third_party\idasdk
cmake --build . --config Release -- /m /clp:NoSummary;ForceNoAlign /v:minimal
ctest --build-config Release --output-on-failure
cmake --install . --config Release --strip
```

Note: This will use the CMake "Visual Studio" generator. You can use the Ninja
generator as well.

This will download and build Abseil, GoogleTest, Protocol Buffers and the
Binary Ninja API. If all went well, the `build_msvc/binexport-prefix`
directory should contain two the files `binexport11.dll` and
`binexport1164.dll` (for use with `ida.exe` and `ida64.exe`, respectively) as well
as `binexport11_binaryninja.dll` (for Binary Ninja).

To enable support for exporting into PostgreSQL databases, add
`-DBINEXPORT_ENABLE_POSTGRESQL=ON` to the first CMake command. Note that
this feature is deprecated.
