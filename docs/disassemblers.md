# Disassemblers

BinDiff relies on a separate disassembler and a matching exporter plugin to
access disassembly information.

## Support Tiers

Historically, BinDiff only supported the IDA Pro disassembler. More recently,
support for additional disassemblers has been added. The following support tiers
outline the level of functionality to be expected:

-   **Tier 1** - Using a this disassembler, BinDiff is fully functional and
    integrated, providing the best user experience.
-   **Tier 2** - BinDiff is almost fully functional, with only minor
    inconveniences.
-   **Tier 3** - The core functionality is available from within the
    disassembler, but there is no feature parity compared to Tier 1 and 2.
-   **Tier 4** - Work in progress or not well integrated yet. Bindiffing will
    still requires manual steps to work.

| Support Tier | Disassembler | License | Minimum Version |
| - | - | - | - |
| 1 | Hex-Rays [IDA Pro](https://hex-rays.com/ida-pro/)/[Home](https://hex-rays.com/ida-home/) | Commercial, Hobbyist | 9.4 |
| 2 | Hex-Rays [IDA Freeware](https://hex-rays.com/ida-free/)¹ | Non-commercial use | 9.4 |
| 4 | [Binary Ninja](https://binary.ninja/) | Commercial, Educational | 3.5 |
| 4 | [Ghidra](https://ghidra-sre.org/) | Apache 2.0 | 10 |

¹ *Does not include Python scripting, so some BinDiff functionality may be*
*unavailable*.
