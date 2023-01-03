// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.processes;

import static com.google.common.base.Verify.verify;
import static java.lang.Math.max;
import static java.util.stream.Collectors.joining;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.exceptions.DifferException;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class DiffProcess {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private DiffProcess() {}

  private static void handleExitCode(final int exitCode) throws DifferException {
    if (exitCode != 0) {
      throw new DifferException(String.format("Error while diffing, exit code %d.", exitCode));
    }
  }

  /**
   * Creates a fully specified filename no longer than 250 characters.
   *
   * <p>It should behave exactly the same as the engine's main_portable.cc version. Note: path must
   * include a trailing slash, extension needs to start with a dot.
   */
  private static String getTruncatedFilename(
      String path, String part1, String middle, String part2, String extension) {
    final var maxFilename = 250;

    int length =
        path.length() + part1.length() + middle.length() + part2.length() + extension.length();
    if (length <= maxFilename) {
      return path + part1 + middle + part2 + extension;
    }

    int overflow = length - maxFilename;

    // First, shorten the longer of the two strings.
    String one = part1;
    String two = part2;
    if (part1.length() > part2.length()) {
      one =
          part1.substring(
              0, max(part2.length(), part1.length() > overflow ? part1.length() - overflow : 0));
      overflow -= part1.length() - one.length();
    } else if (part2.length() > part1.length()) {
      two =
          part2.substring(
              0, max(part1.length(), part2.length() > overflow ? part2.length() - overflow : 0));
      overflow -= part2.length() - two.length();
    }
    if (overflow == 0) {
      return path + one + middle + two + extension;
    }

    // Second, if that still wasn't enough, shorten both strings equally.
    verify(one.length() == two.length());
    if (overflow / 2 >= one.length()) {
      throw new IllegalArgumentException(
          "Cannot create a valid filename, choose shorter input names/directories: "
              + path
              + part1
              + middle
              + part2
              + extension);
    }
    return path
        + part1.substring(0, one.length() - overflow / 2)
        + middle
        + part2.substring(0, two.length() - overflow / 2)
        + extension;
  }

  public static String getBinDiffFilename(
      String primaryBinExportPath, String secondaryBinExportPath) {
    // Using getTruncatedFilename() fixes b/189736868.
    return getTruncatedFilename(
        FileUtils.ensureTrailingSlash(new File(primaryBinExportPath).getParent()),
        FileUtils.getFileBasename(new File(primaryBinExportPath)),
        "_vs_",
        FileUtils.getFileBasename(new File(secondaryBinExportPath)),
        "." + Constants.BINDIFF_MATCHES_DB_EXTENSION);
  }

  public static void startDiffProcess(
      File differExe, String primaryExportedName, String secondaryExportedName, File outputDir)
      throws DifferException {
    final ProcessBuilder processBuilder =
        new ProcessBuilder(
            differExe.getPath(),
            "--nologo",
            "--primary=" + primaryExportedName,
            "--secondary=" + secondaryExportedName,
            "--output_dir=" + outputDir.getPath(),
            "--output_format=bin");
    int exitCode = -1;

    ProcessOutputStreamReader s1 = null;
    ProcessOutputStreamReader s2 = null;
    try {
      processBuilder.redirectErrorStream(true);
      logger.atFinest().log(
          "%s",
          processBuilder.command().stream()
              .map(s -> (!s.contains(" ") ? s : "\"" + s + "\""))
              .collect(joining(" ")));
      final Process diffProcess = processBuilder.start();

      // This is needed to avoid a deadlock!
      // More information see:
      // http://www.javakb.com/Uwe/Forum.aspx/java-programmer/7243/Process-waitFor-vs-Process-destroy
      s1 = new ProcessOutputStreamReader("BinDiff - stdout", diffProcess.getInputStream());
      s2 = new ProcessOutputStreamReader("BinDiff - stderr", diffProcess.getErrorStream());
      s1.start();
      s2.start();

      exitCode = diffProcess.waitFor();

      s1.interruptThread();
      s2.interruptThread();

      handleExitCode(exitCode);
    } catch (IOException e) {
      throw new DifferException(
          e, String.format("Couldn't start diffing process. Exit code %d.", exitCode));
    } catch (InterruptedException e) {
      throw new DifferException(
          e,
          String.format("Diffing process was interrupted unexpectedly. Exit code %d.", exitCode));
    } catch (Exception e) {
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
