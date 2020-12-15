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

package com.google.security.zynamics.bindiff.processes;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.exceptions.DifferException;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class DiffProcess {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private DiffProcess() {}

  private static void handleExitCode(final int exitCode) throws DifferException {
    if (exitCode != 0) {
      if (exitCode == 1) {
        throw new DifferException("An error occurred while diffing. Exit code 1.");
      }
      if (exitCode == 2) {
        throw new DifferException("An error occurred while diffing. Exit code 2.");
      }
      if (exitCode == 3) {
        throw new DifferException("Machine ran out of memory. Exit code 3.");
      }
      throw new DifferException(
          String.format("Unknown error occurred while diffing. Exit code %d.", exitCode));
    }
  }

  public static String getBinDiffFilename(
      final String exportedPrimaryFilename, final String exportedSecondaryFilename)
      throws DifferException {
    try {
      final String primaryFilename = BinDiffFileUtils.removeFileExtension(exportedPrimaryFilename);
      String secondaryFileBasename =
          exportedSecondaryFilename.substring(
              exportedSecondaryFilename.lastIndexOf(File.separator) + 1);
      secondaryFileBasename = BinDiffFileUtils.removeFileExtension(secondaryFileBasename);

      return primaryFilename
          + "_vs_"
          + secondaryFileBasename
          + "."
          + Constants.BINDIFF_MATCHES_DB_EXTENSION;
    } catch (final Exception e) {
      throw new DifferException(e, "Adding Diff to workspace.");
    }
  }

  public static void startDiffProcess(
      final File differExe,
      final String primaryExportedName,
      final String secondaryExportedName,
      final File outputDir)
      throws DifferException {
    final ProcessBuilder diffProcess =
        new ProcessBuilder(
            differExe.getPath(),
            "--nologo",
            "--primary",
            primaryExportedName,
            "--secondary",
            secondaryExportedName,
            "--output_dir",
            outputDir.getPath(),
            "--output_format=bin");

    logger.at(Level.INFO).log(String.join(" ", diffProcess.command()));

    int exitCode = -1;
    Process processInfo = null;

    ProcessOutputStreamReader s1 = null;
    ProcessOutputStreamReader s2 = null;
    try {
      diffProcess.redirectErrorStream(true);
      processInfo = diffProcess.start();

      // This is needed to avoid a deadlock!
      // More information see:
      // http://www.javakb.com/Uwe/Forum.aspx/java-programmer/7243/Process-waitFor-vs-Process-destroy
      s1 = new ProcessOutputStreamReader("BinDiff Process - stdout", processInfo.getInputStream());
      s2 = new ProcessOutputStreamReader("BinDiff Process - stderr", processInfo.getErrorStream());
      s1.start();
      s2.start();

      exitCode = processInfo.waitFor();

      s1.interruptThread();
      s2.interruptThread();

      handleExitCode(exitCode);
    } catch (final DifferException e) {
      throw e;
    } catch (final IOException e) {
      throw new DifferException(
          e, String.format("Couldn't start diffing process. Exit code %d.", exitCode));
    } catch (final InterruptedException e) {
      throw new DifferException(
          e,
          String.format("Diffing process was interrupted unexpectedly. Exit code %d.", exitCode));
    } catch (final Exception e) {
      throw new DifferException(
          e, String.format("Diffing process failed. Exit code %d.", exitCode));
    } finally {
      if (s1 != null) {
        s1.interruptThread();
      }
      if (s2 != null) {
        s2.interruptThread();
      }
    }
  }
}
