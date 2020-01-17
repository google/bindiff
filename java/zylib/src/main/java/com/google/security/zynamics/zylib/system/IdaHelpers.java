// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** Contains a few simple methods that are useful for dealing with IDA and command line handling */
public final class IdaHelpers {
  public static final String IDA32_EXECUTABLE;
  public static final String IDA64_EXECUTABLE;

  static {
    if (SystemHelpers.isRunningWindows()) {
      IDA32_EXECUTABLE = "ida.exe";
      IDA64_EXECUTABLE = "ida64.exe";
    } else {
      IDA32_EXECUTABLE = "ida";
      IDA64_EXECUTABLE = "ida64";
    }
  }

  /**
   * Exports an IDB using BinExport to a .BinExport file.
   *
   * <p>This implementation should behave similar to the one used in the native BinDiff executable
   * and the IDA Pro plugin.
   *
   * @param idaExe the IDA Pro executable to use. @see #IDA32_EXECUTABLE.
   * @param idbFileName the database file to export
   * @param outputDirectory where to store the resulting file. The final filename will be
   *     idbFilename with its file extension replaced by ".BinExport".
   * @return the started process.
   */
  public static Process createIdaProcess(
      final String idaExe, final String idbFileName, final String outputDirectory)
      throws IdaException {
    final ProcessBuilder processBuilder =
        new ProcessBuilder(
            idaExe,
            "-A",
            "-OBinExportModule:" + outputDirectory,
            "-OBinExportAutoAction:BinExportBinary",
            idbFileName);
    processBuilder.environment().put("TVHEADLESS", "1");

    // Now launch the exporter to export the IDB to the database
    try {
      processBuilder.redirectErrorStream(true);
      final Process processInfo = processBuilder.start();

      // Java manages the streams internally - if they are full, the
      // process blocks, i.e. IDA hangs, so we need to consume them.
      try (final BufferedReader reader =
          new BufferedReader(new InputStreamReader(processInfo.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      }
      return processInfo;
    } catch (final IOException e) {
      throw new IdaException(
          "Failed attempting to launch the importer with IDA: " + e.getMessage(), e);
    }
  }
}
