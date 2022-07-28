# BinExport

Copyright 2011-2022 Google LLC.

[![Linux Build Status](https://github.com/google/binexport/workflows/linux-build/badge.svg)](https://github.com/google/binexport/actions?query=workflow%3Alinux-build)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/8977/badge.svg)](https://scan.coverity.com/projects/google-binexport)

Disclaimer: This is not an official Google product (experimental or otherwise),
it is just code that happens to be owned by Google.

# Table of Contents

*   [Introduction](#introduction)
*   [Installation](#installation)
    *   [IDA Pro](#ida-pro)
    *   [Binary Ninja](#binary-ninja)
    *   [Ghidra](#ghidra)
*   [Usage](#usage)
*   [How to build](#how-to-build)
    *   [Preparing the build environment](#preparing-the-build-environment)
    *   [Linux](#linux)
    *   [macOS](#macos)
    *   [Windows](#windows)

## Introduction

BinExport is the exporter component of
[BinDiff](https://www.zynamics.com/software.html). It is a plugin/extension for
the the disassemblers IDA Pro, Binary Ninja and Ghida that exports disassembly
data into the Protocol Buffer format that BinDiff requires.

An experimental version for the open source sofware reverse engineering suite
Ghidra is available in the `java/BinExport` directory.

This repository contains the complete source code necessary to build BinExport
plugin binaries for Linux, macOS and Windows.

## Installation

### IDA Pro

Download the binaries from the [releases](https://github.com/google/binexport/releases) page and copy them into the IDA Pro
plugins directory. These are the default paths:

| OS      | Plugin path                                 |
| ------- | ------------------------------------------- |
| Linux   | `/opt/idapro-7.6/plugins`                   |
| macOS   | `/Applications/IDA Pro 7.6/idabin/plugins`  |
| Windows | `%ProgramFiles%\IDA 7.6\plugins`            |

To install just for the current user, copy the files into one of these
directories instead:

| OS          | Plugin                |
| ----------- | ------------------------------------ |
| Linux/macOS | `~/.idapro/plugins`                  |
| Windows     | `%AppData%\Hex-Rays\IDA Pro\plugins` |


#### Verifying the installation version

1.  In IDA, select `Help`|`About programm...`
2.  Click `Addons...`
3.  If installed correctly, the following dialog box appears:

    ![IDA addons dialog](/doc/binexport10-ida-addons-dialog.png)


### Binary Ninja

Download the binaries from the [releases](https://github.com/google/binexport/releases) page and copy them into the Binary Ninja
plugins directory. These are the default paths for the current user:

| OS      | Plugin path                                           |
| ------- | ----------------------------------------------------- |
| Linux   | `~/.binaryninja/plugins`                              |
| macOS   | `~/Library/Application Support/Binary Ninja/plugins/` |
| Windows | `%AppData%\Binary Ninja\plugins`                      |


#### Verifying the installation version

1.  Start Binary Ninja
2.  Select the `Log` native dock. If this is not visible, enable it via
    `View`|`Native Docks`|`Show Log`.
3.  If installed correctly, the log window contains a line similar to this one:

```
BinExport 12 (@internal, Mar 12 2021), (c)2004-2011 zynamics GmbH, (c)2011-2022 Google LLC.
```

### Ghidra

1.  Download the binaries from the [releases](https://github.com/google/binexport/releases) page.
2.  Start Ghidra, select `File`|`Install Extensions...`
3.  In the "Install Extensions" dialog, click the plus icon in the upper right to "Add extension".
4.  In the "Select extension" dialog, enter the path to the `ghidra_BinExport.zip` you downloaded
    in step 1 and click `OK`.
5.  Click `OK` twice to close both the "Install Extensions" dialog and the notice to restart Ghidra.
6.  Exit Ghidra.

#### Verifying the installation version

1.  Start Ghidra
2.  Select `File`|`Install Extensions...`
3.  If installed correctly, the "Install Extensions" dialog should list the "BinExport" extension
    next to a selected checkbox.

## Usage

The main use case is via [BinDiff](https://zynamics.com/bindiff.html). However,
BinExport can also be used to export disassembly into different formats:

*   Protocol Buffer based full export
*   Statistics text file
*   Text format for debugging

### IDA Pro

1.  Open an IDA Pro database
2.  Select `Edit`|`Plugins`|`BinExport 12`
3.  The following dialog box appears:

    ![BinExport plugin dialog](/doc/binexport10-plugin-dialog.png)

4.  Select the type of the file to be exported

#### IDC Scripting

The BinExport plugin registers the IDC functions below.

| IDC Function name   | Exports to           | Arguments  |
| ------------------- | -------------------- | ---------- |
| BinExportBinary     | Protocol Buffer      | filename   |
| BinExportText       | Text file dump       | filename   |
| BinExportStatistics | Statistics text file | filename   |

Alternatively, the plugin can be invoked from IDC by calling its main function
directly:

```c
static main() {
  batch(0);
  auto_wait();
  load_and_run_plugin("binexport12_ida", 2 /* kBinary */);
  qexit(0);
}
```

Note that this does provide any control over the output filename. BinExport
will always use the filename of the currently loaded database (without
extension) and append ".BinExport".

#### IDAPython

The arguments are the same as for IDC (listed above).

Example invocation of one of the registered IDC functions:

```python
import idaapi
idaapi.ida_expr.eval_idc_expr(None, ida_idaapi.BADADDR,
  'BinExportBinary("exported.BinExport");')
```

#### Plugin Options

BinExport defines the following plugin options, that can be specified on IDA's
command line:

| Option                                 | Description                                                           |
| -------------------------------------- | --------------------------------------------------------------------- |
| `-OBinExportAutoAction:<ACTION>`       | Invoke a BinExport IDC function and exit                              |
| `-OBinExportModule:<PARAM>`            | Argument for `BinExportAutoAction`                                    |
| `-OBinExportLogFile:<FILE>`            | Log messages to a file                                                |
| `-OBinExportAlsoLogToStdErr:TRUE`      | If specified, also log to standard error                              |
| `-OBinExportX86NoReturnHeuristic:TRUE` | Enable the X86-specific heuristic to identify non-returning functions |

Note: These options must come before any files.

### Binary Ninja

There is only minimal integration into the Binary Ninja UI at this time.

1.  Open or create a new analysis database
2.  Select `Tools`|`Plugins`|`BinExport`. This will start the export process.

The `.BinExport` file is placed next to the analysis database, in the same
directory.

### Ghidra

There is only minimal integration into the Ghidra UI at this time.

1.  Open or create a project. For new projects, import a file first using `File`|`Import File...`
2.  Right-click a file in the current project list and select `Export...` from the context menu.
3.  In the "Export" dialog, under "Format", choose "Binary Export (v2) for BinDiff".
4.  Under "Output File", enter the desired output file path. If the file extension is missing,
    `.BinExport` will be appended automatically.
5.  Optional: click "Options..." to set additional export options.
6.  Click "OK", then click "OK" again to dismiss the "Export Results Summary" dialog.

## How to build

Below are build instructions for the native code plugins for IDA Pro and
Binary Ninja. To build the Java-based extension for Ghidra, please refer
to the [BinExport for Ghidra](/java) instructions.

### Preparing the build environment

There are quite a few dependencies to satisfy:

*   Boost 1.71.0 or higher (a partial copy of 1.71.0 ships in
    `third_party/boost_parts`)
*   [CMake](https://cmake.org/download/) 3.14 or higher
*   Suggested: [Ninja](https://ninja-build.org/) for speedy builds
*   GCC 9 or a recent version of Clang on Linux/macOS. On Windows, use the
    Visual Studio 2019 compiler and the Windows SDK for Windows 10.
*   Git 1.8 or higher
*   IDA Pro only: IDA SDK 7.6 (unpack into `third_party/idasdk`)
*   Dependencies that will be downloaded:
    *   Abseil, GoogleTest and Protocol Buffers (3.14)
    *   Binary Ninja SDK

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
wget https://github.com/Kitware/CMake/releases/download/v3.20.1/cmake-3.20.1-linux-x86_64.sh
mkdir ${HOME}/cmake
sh cmake-3.20.1-Linux-x86_64.sh --prefix=${HOME}/cmake --exclude-subdir
export PATH=${HOME}/cmake/bin:${PATH}
```

The following sections assume that your current working directory is at the root
of the cloned repository.

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 7.6:

```bash
unzip PATH/TO/idasdk76.zip -d third_party/idasdk
mv third_party/idasdk/idasdk76/* third_party/idasdk
rmdir third_party/idasdk/idasdk76
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
    -DBINEXPORT_ENABLE_IDAPRO=ON \
    "-DIdaSdk_ROOT_DIR=${PWD}/../third_party/idasdk" \
    -DBINEXPORT_ENABLE_BINARYNINJA=ON \
cmake --build . --config Release
ctest --build-config Release --output-on-failure
cmake --install . --config Release --strip
```

Note: If you don't want to use Ninja to perform the actual build, omit
the `-G Ninja` part.

To disable the IDA Pro build, set `-DBINEXPORT_ENABLE_IDAPRO=OFF`. Likewise, to
disable the Binary Ninja build, set `-DBINEXPORT_ENABLE_BINARYNINJA=OFF`.

This will download and build Abseil, GoogleTest, Protocol Buffers and the
Binary Ninja API. If all went well, the `build_linux/binexport-prefix`
directory should contain two the files `binexport12_ida.so` and
`binexport12_ida64.so` (for use with `ida` and `ida64`, respectively) as well
as `binexport12_binaryninja.so` (for Binary Ninja).


### macOS

#### Prerequisites

The preferred build environment is Mac OS X 10.15 "Catalina" using Xcode
12. Using macOS 11 "Big Sur" should also work.

After installing the Developer Tools, make sure to install the command-line
tools as well:

```bash
sudo xcode-select --install
```

The following sections assume that your current working directory is at the root
of the cloned repository.

#### CMake

Download the latest stable version of CMake from the official site and mount its
disk image:

```bash
curl -fsSL https://github.com/Kitware/CMake/releases/download/v3.20.1/cmake-3.20.1-Darwin-x86_64.dmg \
    -o $HOME/Downloads/cmake-osx.dmg
hdiutil attach $HOME/Downloads/cmake-osx.dmg
```

At this point you will need to review and accept CMake's license agreement. Now
install CMake:

```bash
sudo cp -Rf /Volumes/cmake-3.20.1-Darwin-x86_64/CMake.app /Applications/
hdiutil detach /Volumes/cmake-3.20.1-Darwin-x86_64
sudo /Applications/CMake.app/Contents/bin/cmake-gui --install
```

The last command makes CMake available in the system path.

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 7.6:

```bash
unzip PATH/TO/idasdk76.zip -d third_party/idasdk
mv third_party/idasdk/idasdk76/* third_party/idasdk
rmdir third_party/idasdk/idasdk76
```

#### Build BinExport

With all prerequisites in place, configure and build BinExport and run
its tests:

```bash
mkdir -p build_mac && cd build_mac
cmake .. \
    -DCMAKE_BUILD_TYPE=Release \
    "-DCMAKE_INSTALL_PREFIX=${PWD}" \
    -DBINEXPORT_ENABLE_IDAPRO=ON \
    "-DIdaSdk_ROOT_DIR=${PWD}/../third_party/idasdk" \
    -DBINEXPORT_ENABLE_BINARYNINJA=ON \
cmake --build . --config Release -- "-j$(sysctl -n hw.logicalcpu)"
ctest --build-config Release --output-on-failure
cmake --install . --config Release --strip
```

Note: This will use the standard CMake "Makefile Generator". You can use XCode
or Ninja as generators as well.

To disable the IDA Pro build, set `-DBINEXPORT_ENABLE_IDAPRO=OFF`. Likewise, to
disable the Binary Ninja build, set `-DBINEXPORT_ENABLE_BINARYNINJA=OFF`.

This will download and build Abseil, GoogleTest, Protocol Buffers and the
Binary Ninja API. If all went well, the `build_mac/binexport-prefix`
directory should contain two the files `binexport12_ida.dylib` and
`binexport12_ida64.dylib` (for use with `ida` and `ida64`, respectively) as well
as `binexport12_binaryninja.dylib` (for Binary Ninja).


### Windows

The preferred build environment is Windows 10 (64-bit Intel) using the Visual
Studio 2019 compiler and the [Windows SDK for Windows
10](https://dev.windows.com/en-us/downloads/windows-10-sdk).

#### CMake

Download and install the lastest stable CMake (3.20.1 at the time of writing)
from its [download page](https://cmake.org/download/). Make sure to select
"Add CMake to the system PATH for all users".

#### Git

Download and install Git from its [download
page](https://git-scm.com/download/win). Make sure to select the following
options: * The installation directory should be left at the default
`%ProgramFiles%\Git\bin\git.exe` * "Use Git from the Windows Command Prompt" -
have the setup utility add Git to your system path. * "Use Windows' default
console window" - to be able to use Git from the regular command prompt.

#### Prepare

The following sections assume an open command prompt with the current working
directory located at the root of the cloned BinExport repository:

```bat
git clone https://github.com/google/binexport.git
cd binexport
```

#### IDA SDK

Unzip the contents of the IDA SDK into `third_party/idasdk`. Shown commands are
for IDA 7.6, assuming that Git was installed into the default directory first:

```bat
"%ProgramFiles%\Git\usr\bin\unzip" PATH\TO\idasdk76.zip -d third_party
rename third_party\idasdk76 idasdk
```

#### Build BinExport

With all prerequisites in place, configure and build BinExport:

```bat
if not exist build_msvc mkdir build_msvc
cd build_msvc
cmake .. ^
    -G "Visual Studio 16 2019" ^
    -DCMAKE_BUILD_TYPE=Release ^
    "-DCMAKE_INSTALL_PREFIX=%cd%" ^
    -DBINEXPORT_ENABLE_IDAPRO=ON ^
    -DIdaSdk_ROOT_DIR=%cd%\..\third_party\idasdk ^
    -DBINEXPORT_ENABLE_BINARYNINJA=ON
cmake --build . --config Release -- /m /clp:NoSummary;ForceNoAlign /v:minimal
ctest --build-config Release --output-on-failure
cmake --install . --config Release --strip
```

Note: This will use the CMake "Visual Studio" generator. You can use the Ninja
generator as well.

To disable the IDA Pro build, set `-DBINEXPORT_ENABLE_IDAPRO=OFF`. Likewise, to
disable the Binary Ninja build, set `-DBINEXPORT_ENABLE_BINARYNINJA=OFF`.

This will download and build Abseil, GoogleTest, Protocol Buffers and the
Binary Ninja API. If all went well, the `build_msvc/binexport-prefix`
directory should contain two the files `binexport12_ida.dll` and
`binexport12_ida64.dll` (for use with `ida.exe` and `ida64.exe`, respectively) as well
as `binexport12_binaryninja.dll` (for Binary Ninja).
