// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
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
import java.nio.charset.Charset;
import java.util.ArrayList;

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
   * @param idbFilename the database file to export
   * @param binexportFilename where to store the resulting file.
   * @param alsologtostderr whether to log to standard output.
   * @param logFilename path to a log file. If empty, file logging is disabled.
   * @param x86NoReturnHeuristic whether to apply an x86/x86-64 specific heuristic to find functions
   *     that do not return.
   * @return the started process.
   */
  public static Process createIdaProcess(
      String idaExe,
      String idbFilename,
      String binexportFilename,
      boolean alsologtostderr,
      String logFilename,
      boolean x86NoReturnHeuristic)
      throws IdaException {
    var args = new ArrayList<String>();
    args.add(idaExe);
    args.add("-A");
    args.add("-OBinExportAutoAction:BinExportBinary");
    args.add("-OBinExportModule:" + binexportFilename);
    args.add("-OBinExportAlsoLogToStdErr:" + (alsologtostderr ? "TRUE" : "FALSE"));
    if (!logFilename.isEmpty()) {
      args.add("-OBinExportLogFile:" + logFilename);
    }
    args.add("-OBinExportX86NoReturnHeuristic:" + (x86NoReturnHeuristic ? "TRUE" : "FALSE"));
    args.add(idbFilename);
    var processBuilder = new ProcessBuilder(args);
    processBuilder.environment().put("TVHEADLESS", "1");

    // Now launch the exporter to export the IDB to the database
    try {
      processBuilder.redirectErrorStream(true);
      final Process idaProcess = processBuilder.start();

      // Java manages the streams internally - if they are full, the
      // process blocks, i.e. IDA hangs, so we need to consume them.
      try (var reader =
          new BufferedReader(
              new InputStreamReader(idaProcess.getInputStream(), Charset.defaultCharset()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      }
      return idaProcess;
    } catch (IOException e) {
      throw new IdaException(
          "Failed attempting to launch the importer with IDA: " + e.getMessage(), e);
    }
  }
}
