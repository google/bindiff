# Version History

This page provides information about the changes in each release of BinDiff up
to version 7.

For information on later versions, please refer to the
[releases page](https://github.com/google/bindiff/releases).

## BinDiff 7

**July 1, 2021**

### New

-   Full support for IDA Pro 7.6 SP1, minimum required version is still 7.4
-   Ghidra support: The BinExport Ghidra extension is now included in the
    package. This is still considered beta and will require to manually export
    files from Ghidra in order to bindiff them.
-   Binary Ninja support: A BinExport plugin for Binary Ninja is included. This
    is still considered beta. Use the third-party
    [BD Viewer](https://github.com/PistonMiner/binaryninja-bindiff-viewer) or
    export manually to bindiff files.
-   Function names are now exported in `.BinDiff` result files, making
    post-processing easier
-   Improved speed in native BinExport plugins. Exporting from IDA Pro and
    Binary Ninja is up to 30% faster compared to BinDiff 6.
-   User interface: Shipping with updated Java runtime based on JDK 16 on all
    platforms, including Linux
-   User interface: Better HiDPI support on all platforms
-   Installation packages: disassembler plugins are now symlinked into per-user
    plugin directories instead of being copied into the target disassembler
    installation.
-   User configuration now uses JSON format, making future changes easier and
    fixes some long-standing serialization issues
-   macOS: Notarized binaries/package compatible with macOS 11 "Big Sur"
-   macOS: Universal Binaries supporting ARM64 (aka "Apple Sillicon") and
    x86-64
-   Fixed a security issue where a specially crafted .BinExport file could lead
    to an out-of-bounds memory access. Thanks to Mickey Jin of Trend Micro
    Mobile Security Research Team for the report.


### Deprecated/Removed

-   **Deprecated** Plugins for 32-bit address versions of IDA Pro are
    deprecated. Future versions of BinDiff may eventually only ship with plugins
    for the 64-bit address aware version. <p> If you are starting a new reverse
    engineering effort, even if the binary you are analyzing is 32-bit, using
    the 64-bit address aware version of IDA Pro (`ida64.exe` on Windows,
    `ida64` on Linux/macOS) is highly recommended.
-   Removed export to PostgreSQL databases from the BinExport plugin. If you
    need this (for example for BinNavi), the previous version of BinExport can
    be used.


## BinDiff 6

**March 1, 2020**

### New

-   Built on IDA SDK 7.4
-   Experimental support for the Ghidra disassembler (see the
    [BinExport documentation](https://github.com/google/binexport/tree/v11/java/BinExport))
-   Bug fixes to the core BinDiff engine
-   Improved porting of symbols and comments
-   Merged config file for command-line, plugin and UI
-   Better macOS integration
-   Fixed a security issue with the Windows Installer
    ([b/140218119](http://issuetracker.google.com/140218119)). Thanks to Eran
    Shimony of CyberArk Labs for the report.

## BinDiff 5

**March 11, 2019**

### New

-   Ported BinDiff and BinExport plugins to IDA 7
-   Built on IDA SDK 7.2
-   Improvements and bug fixes to the core BinDiff engine
-   Fixed symbol and comment porting
-   Config file locations and default install paths changed
-   On Windows and macOS, ship with a bundled Java runtime based on OpenJDK 11

### Deprecated/Removed

-   32-bit builds of the plugins

## BinDiff 4.3

**July 11, 2017**

### New

-   Built on IDA SDK 6.95
-   **macOS support**
-   Improvements to the core BinDiff engine
-   Using the open source version of BinExport that writes the new BinExport2
    format, based on Protocol Buffers. Source is available
    [on GitHub](https://github.com/google/binexport).
-   New human-readable config file format for the UI
-   UI change: Switched to left-click drag for the graph views

## BinDiff 4.2

**July 27, 2015**

### New

-   Built on IDA SDK 6.8, so this is the new minimum version required
-   Support for AArch64 (a.k.a. ARM64)
-   Increase maximum export file size to 2GiB
-   Improvements to the core BinDiff engine
-   New import symbols and comments feature to mark imported comments as coming
    from an external library

### Deprecated/Removed

-   No OS X support for this version

## BinDiff 4.1

**August 27, 2014**

### New

-   Support for IDA 6.5 or higher
-   Support for Windows 8 and 8.1
-   Support for large operand sizes used by the AVX/AVX2 instructions
-   Improvements to the core BinDiff engine

### Deprecated/Removed

-   No OS X support for this version

## BinDiff 4.0

**December 5, 2011**:

### New

-   The user interface for visual diff has been rewritten
-   Call graph views
-   Proximity browsing in flowgraphs and callgraphs
-   New "combined" view of flowgraphs
-   Faster graph rendering and better rendering quality
-   Improved instruction match representation
-   Improved search functionality
-   IDA comments are exported
-   Selection history with undo and redo
-   Copyable basic block and function node contents
-   Multi-tab layout
-   Organize multiple diffs in workspaces
-   New exporter format based on Google Protocol Buffers
-   Incremental diffing - manually confirm matches that will be kept in another
    diff iteration while reassiging others, allows to iteratively improve the
    result
-   Auto-generated comments no longer get ported
-   New column in "Matched functions" table allows to keep track of one's
    progress for comment porting
-   Support for the Dalvik architecture (used by Android)

### Deprecated/Removed

-   Removed the "assembly" view from the 3.x versions
-   HTML report generation has been removed

## BinDiff 3.2.1

**February 1st, 2011**

### New

-   Bug fixes
-   Linux packages for Debian 5.0 ("Lenny") and Ubuntu 10.04 LTS ("Lucid Lynx")

## BinDiff 3.2

**August 17, 2010**:

-   Mac OS X support
-   Support for IDA Pro 6.0 or higher (version 3.2.2 adds support for IDA Pro
    6.2)

## BinDiff 3.0

### New

-   Big change: New internal diff engine that produces more detailed and more
    accurate results
-   Fixed an edge-layout bug that led to improperly connected basic blocks
-   Improved HTML report generation
-   Added menu items to IDA to allow for a global hotkey and fast access to
    subviews
-   New statistics subview

### Deprecated/Removed

-   Removed configuration dialog from IDA plugin. The plugin configuration can
    be changed via its XML-based configuration file

## BinDiff 2.1

-   Linux version of the BinDiff Plugin for IDA (to be downloaded separately)
-   Windows only: Removed dependency on Windows registry, configuration data is
    now stored in plain XML files
-   Compatibility with Windows Vista: BinDiff Plugin for IDA no longer writes to
    the
-   IDA directory anymore
-   Updated Documentation in XHTML format
-   Improved installation experience using Windows Installer
-   Bugfix: Labelled addresses are now properly displayed

## BinDiff 2.0

-   First version with an external Java based graphical user interface

## BinDiff 1.8

-   HTML output generation: BinDiff can now generate an HTML file summarizing
    the changes detected and providing detailed information on changes to the
    call graph and ambiguous situations.
-   Edge-Vector matching: BinDiff 1.8 features a new algorithm for generating
    initial fixedpoints, leading to a significant reduction in diffing time on
    larger disassemblies (several thousand functions) where no symbols are
    available.

## BinDiff 1.6

-   New methods to generate initial fixedpoints:
    -   String matching: Creates fixedpoints from functions that reference
        identical strings
    -   Recursive function matching: Creates fixedpoints from functions that
        call themselves
    -   Prime Products: Fixedpoints are created for functions that contain the
        same instructions in identical quantities (but not necessarily in the
        same order)
-   Initial fixedpoints by name now ignores "unknown_libnames" and similar
    categories of functions
-   New isomorphism algorithm for function-level diffing
-   New visualization option for visual diff color corresponding nodes in
    identical colors
-   New CPU-independent instruction-level isomorphism algorithm
-   New comment porting algorithm based on the above; currently ports:
    -   Local labels
    -   Anterior/posterior comments
    -   Regular and repeatable comments
    -   Regular and repeatable function comments
    -   Operand to standard enum member
-   Added processor: SPARC
-   New fully CPU-independent mode for not explicitly supported processors (used
    by default if the CPU is not supported). This means that all CPUs that IDA
    supports are supported using this default mode. However, CPUs with
    conditional execution (IA64, ARM) may yield suboptimal results.
-   Option to color functions identified as changed within the database.
    Functions colored with this option will be colored in any graphs created by
    the user through IDA's graphing facilities.