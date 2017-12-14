This directory contains a CMake "superbuild" that will download, configure
and build all external dependencies.

Linux
-----

Configure with locally installed IDA SDK at `/opt/idasdk`:

   mkdir build
   cd build
   cmake ../cmake -DIdaSdk_ROOT_DIR=/opt/idasdk -DCMAKE_BUILD_TYPE=Release

Replace `Release` with `Debug` for a debug build.

To download, configure and install all other external dependencies and start
the build:

   cmake --build .


macOS
-----

Configure with locally installed IDA SDK at `/usr/local/opt/idasdk`:

   mkdir build
   cd build
   cmake ../cmake -DIdaSdk_ROOT_DIR=/usr/local/opt/idasdk -G "Xcode"

To download, configure and install all other external dependencies and start
the build:

   cmake --build . --config Release

Replace `Release` with `Debug` for a debug build.


Windows
-------

Configure with locally installed IDA SDK at `C:\idasdk`:

   mkdir build
   cd build
   cmake ../cmake -DIdaSdk_ROOT_DIR=C:\idasdk -G "Visual Studio 14 2015 Win64"

To download, configure and install all other external dependencies and start
the build:

   cmake --build . --config Release -- /clp:NoSummary;ForceNoAlign /v:minimal

Replace `Release` with `Debug` for a debug build.
