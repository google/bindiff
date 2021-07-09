![BinDiff Logo](docs/images/bindiff-lockup-vertical.png)

Copyright 2011-2021 Google LLC.

# BinDiff

This repository contains the BinDiff source code. BinDiff is an open-source
comparison tool for binary files to quickly find differences and similarities
in disassembled code.

## Table of Contents

- [About BinDiff](#about)
- [Quickstart](#quickstart)
- [Codemap](#codemap)
- [License](#license)
- [Getting Involved](#contributing)

<a name="about"></a>
## About BinDiff

BinDiff is an open-source comparison tool for binary files, that assists
vulnerability researchers and engineers to quickly find differences and
similarities in disassembled code.

With BinDiff, researchers can identify and isolate fixes for vulnerabilities in
vendor-supplied patches. It can also be used to port symbols and comments
between disassemblies of multiple versions of the same binary. This makes
tracking changes over time easier and allows organizations to retain analysis
results and enables knowledge transfer among binary analysts.

### Use Cases

* Compare binary files for x86, MIPS, ARM, PowerPC, and other architectures
  supported by the IDA Pro, Binary Ninja or Ghidra disassemblers
* Identify identical and similar functions in different binaries
* Port function names, comments and local names from one disassembly to the
  other
* Detect and highlight changes between two variants of the same function

## Quickstart

If you want to just get started using BinDiff, you download prebuilt
installation packages from the
[release page](https://github.com/google/binexport/releases).

## Codemap

BinDiff contains the following components:

* [`cmake`](cmake) - CMake build files declaring external dependencies
* [`fixtures`](fixtures) - A collection of test files to excercise the BinDiff
  core engine
* [`ida`](ida) - Integration with the IDA Pro disassembler
* [`java`](java) - Java source code. This contains the the BinDiff visual diff
  user interface and its corresponding utility library.
* [`match`](match) - Matching algorithms for the BinDiff core engine
* [`packaging`](packaging) - Package sources for the installation packages
* [`tools`](tools) - Helper executables that are shipped with the product

## Further reading / Similar tools

The original papers outlining the general ideas behind BinDiff:

* Thomas Dullien and Rolf Rolles. *Graph-Based Comparison of Executable
  Objects*. [bindiffsstic05-1.pdf](docs/papers/bindiffsstic05-1.pdf).
  SSTIC ’05, Symposium sur la Sécurité des Technologies de l’Information et des
  Communications. 2005.
* Halvar Flake. *Structural Comparison of Executable Objects*.
  [dimva_paper2.pdf](docs/papers/dimva_paper2.pdf). pp 161-173. Detection of
  Intrusions and Malware & Vulnerability Assessment. 2004.3-88579-375-X.

Other tools in the same problem domain:

* [Diaphora](https://github.com/joxeankoret/diaphora), an advanced program
  diffing tool implementing many of the same ideas.

## License

BinDiff is licensed under the terms of the Apache license. See
[LICENSE](LICENSE) for more information.

<a name="contributing"></a>
## Getting Involved

If you want to contribute, please read [CONTRIBUTING.md](CONTRIBUTING.md)
before sending pull requests. You can also report bugs or file feature
requests.