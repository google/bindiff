// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.bindiff.logging;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.system.SystemHelpers;
import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;

/** Configures application-wide logging. */
public class Logger {
  private static final java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");

  private static final LogFormatter formatter = new LogFormatter();
  private static final Filter filter =
      r -> r.getSourceClassName().startsWith("com.google.security.zynamics.");
  private static final ConsoleHandler consoleHandler = new ConsoleHandler();
  private static FileHandler fileHandler = null;

  static {
    consoleHandler.setFilter(filter);
    consoleHandler.setFormatter(formatter);
    for (final Handler h : rootLogger.getHandlers()) {
      rootLogger.removeHandler(h);
    }
    rootLogger.addHandler(consoleHandler);
  }

  private Logger() {}

  public static String levelToString(Level level) {
    if (Level.FINEST.equals(level)) {
      return "debug";
    } else if (Level.INFO.equals(level)) {
      return "info";
    } else if (Level.WARNING.equals(level)) {
      return "warning";
    } else if (Level.SEVERE.equals(level)) {
      return "error";
    } else if (Level.OFF.equals(level)) {
      return "off";
    }
    return Integer.toString(level.intValue());
  }

  public static void setFileHandler(final FileHandler handler) {
    if (fileHandler != null) {
      rootLogger.removeHandler(handler);
    }

    fileHandler = handler;
    fileHandler.setFormatter(formatter);
    fileHandler.setFilter(filter);
  }

  public static void setConsoleLogging(final boolean enable) {
    rootLogger.removeHandler(consoleHandler);
    if (enable) {
      rootLogger.addHandler(consoleHandler);
    }
  }

  public static void setFileLogging(final boolean enable) {
    rootLogger.removeHandler(fileHandler);
    if (enable) {
      rootLogger.addHandler(fileHandler);
    }
  }

  public static String getDefaultLoggingDirectoryPath() {
    final File logDir =
        new File(
            SystemHelpers.getApplicationDataDirectory(Constants.PRODUCT_NAME)
                + Constants.LOG_DIRECTORYNAME);

    if (!logDir.exists()) {
      logDir.mkdirs();
    }

    return logDir.getPath();
  }

  public static String getLoggingFilePath(final String logDestinationFolderPath) {
    String path;
    if (isNullOrEmpty(logDestinationFolderPath)) {
      path = getDefaultLoggingDirectoryPath();
    } else {
      path = logDestinationFolderPath;
    }

    final File file = new File(path);
    if (!file.exists()) {
      file.mkdirs();
    }

    path = BinDiffFileUtils.forceFileSeparator(path);
    return FileUtils.ensureTrailingSlash(path) + Constants.LOG_FILE_NAME;
  }

  public static void setLogLevel(final Level level) {
    consoleHandler.setLevel(level);
    if (fileHandler != null) {
      fileHandler.setLevel(level);
    }
    rootLogger.setLevel(level);
  }
}
