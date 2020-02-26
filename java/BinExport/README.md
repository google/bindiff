# BinExport for Ghidra

## Table of Contents

*   [Introduction](#introduction)
*   [How to Build](#how-to-build)
    *   [Build Dependencies](#build-dependencies)
    *   [Build BinExport](#build-binexport)
    *   [Development using Eclipse](#development-using-eclipse)
*   [Installing the Ghidra Extension](#installing-the-ghidra-extension)
*   [Usage](#usage)
    *   [Verifying the installation version](#verifying-the-installation-version)
    *   [Invocation](#invocation)
    *   [BinDiff Exported Files](#bindiff-exported-files)

## Introduction

BinExport is the exporter component of
[BinDiff](https://www.zynamics.com/software.html). This subdirectory contains an
experimental version for the open source software reverse engineering suite
[Ghidra](https://ghidra-sre.org/).

## How to Build

### Build Dependencies

In order to build, the following software needs to be installed on the
development machine:

*   OpenJDK 11 or later
*   Ghidra 9.1.2 (https://ghidra-sre.org/releaseNotes_9.1.2.html)
*   Gradle 5.6 or later
    (https://services.gradle.org/distributions/gradle-5.6.3-all.zip)

The Gradle build tool will addtionally download these dependencies:

*   Gradle plugin for protobuf
*   Protobuf 3.10.0 for Java

For development, a recent version of Eclipse and the Ghidra development plugin
are recommended (see [below](#development-using-eclipse))

### Build BinExport

From the command-line, start the build with

```bash
gradle
```

After downloading the addtional dependencies and compiling the Java sources, the
extension is available as a .zip file under
`dist/ghidra_9.1.2_PUBLIC_YYYYMMDD_BinExport.zip`, where `YYYYMMDD` stands for
the current date.

### Development using Eclipse

For development Eclipse 4.12 (2019-06) or higher is recommended
(https://www.eclipse.org/downloads/packages/release/2019-12/r/eclipse-ide-java-developers).

The Gradle build tool can automatically create an Eclipse project. From the
command-line, run

```bash
grade clean eclipse
```

Then import the project in Eclipse using `File|Open Projects from File
System...`. To enable build and debug configurations for Ghidra, follow the
[Installation Guide](https://ghidra-sre.org/InstallationGuide.html#Extensions)

Using GhidraDev, link the project to your local Ghidra installation via
`GhidraDev|Link Ghidra...`.

**Note**: Due to the way Gradle project generation works, you may need to
manually clean up the build class path in `.classpath` before linking to your
local Ghidra installation.

## Installing the Ghidra Extension

After a successful `dist`-build the BinExport extension can be installed like
any other extension:

1.  Start Ghidra, then select `File|Install Extensions...`.
2.  Click the `+` button to `Add extension`.
3.  In the `Select Extension` dialog, navigate to your source directory and open
    the `dist` folder.
4.  Select the .zip file you created and click `OK`
5.  Click `OK` to confirm and again to dismiss the restart message. Then restart
    Ghidra.

## Usage

This version of the Java based exporter for Ghidra has the following features
compared to the native C++ version for IDA Pro:

| | Ghidra Extension | IDA Pro plugin |
| --- | --- | --- |
| Protocol Buffer based full export | ✓¹ | ✓ |
| Statistics text file | - | ✓ |
| Text format for debugging | - | ✓ |
| BinNavi database export into a PostgreSQL database | - | ✓ |

¹ No operand trees

### Verifying the installation version

1.  In Ghidra, select `File|Install Extensions...`.
2.  Verify that `BinExport` is listed and has the correct `Install Path`

### Invocation

1.  In Ghidra, open a project or create a new one.
2.  If not already done, open the binary to export in the Code Browser tool and
    run Ghidra's initial analysis. You may want to enable the "aggressive
    instruction finder" option to get better coverage in the export.
3.  In the project view, right-click the binary to export and select `Export...`
4.  From the drop-down list, select `Binary BinExport (v2) for BinDiff`
5.  Select a path for the output file. This can be the original filename, as
    `.BinExport` will be appended.
6.  Click `OK`.

### BinDiff Exported Files

If you have BinDiff installed (available from
https://www.zynamics.com/software.html), exported files can be diffed and the
results displayed in its UI:

1.  Export two binaries following the instructions [above](#invocation). The
    following steps assume `primary.BinExport` and `secondary.BinExport`.
2.  From the command-line, run the BinDiff engine with

    ```
    bindiff primary.BinExport secondary.BinExport
    ```

    This will create a file `primary_vs_secondary.BinDiff` in the current
    directory.

3.  Launch the BinDiff UI, either via `bindiff --ui` or using the launcher for
    your operating system.

4.  Create a new workspace or open an existing one.

5.  Select `Diffs|Add Existing Diff...`.

6.  Under `Choose Diff`, select the `primary_vs_secondary.BinDiff` created in
    step 2.

7.  Click `OK`, followed by `Add`. The diff is now shown in the tree view on the
    left and can be opened by double-clicking it.

8.  Use BinDiff normally to display the call graph or flow graphs of matched
    functions.
