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

package com.google.security.zynamics.zylib.system;

import com.google.common.base.Ascii;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.io.File;

/**
 * Contains a few simple methods that are useful for determining information about the underlying
 * operating system in a platform-independent way.
 */
public final class SystemHelpers {
  /**
   * Determines the machine-wide application data directory in a platform-independent way. Note that
   * the directory returned by this method is usually not writable for users without administrative
   * privileges (yes, even on Windows). The path returned by this method always contains a trailing
   * path separator.
   *
   * @return the machine-wide application data directory
   */
  public static String getAllUsersApplicationDataDirectory() {
    final String result;
    if (isRunningWindows()) {
      // This should be the same as passing CSIDL_COMMON_APPDATA to the native SHGetFolderPath()
      // Win32 function.
      result = System.getenv("ProgramData");
    } else if (isRunningLinux()) {
      result = "/etc/opt";
    } else if (isRunningMacOSX()) {
      result = "/Library/Application Support";
    } else {
      // Fallback to local user directory
      result = System.getProperty("user.home");
    }
    return FileUtils.ensureTrailingSlash(result);
  }

  /**
   * Determines the machine-wide application data directory for the specified product in a
   * platform-independent way. Note that the directory returned by this method is usually not
   * writable for users without administrative privileges, even on Windows. The path returned by
   * this method always contains a trailing path separator.
   *
   * @param product the product name to use when building the directory name
   * @return the machine-wide application data directory
   */
  public static String getAllUsersApplicationDataDirectory(final String product) {
    return getAllUsersApplicationDataDirectory()
        + (isRunningWindows() || isRunningMacOSX() ? product : Ascii.toLowerCase(product))
        + File.separator;
  }

  /**
   * Determines the application data directory in a platform-independent way. The path returned by
   * this method always contains a trailing path separator.
   *
   * @return the application data directory for the current platform.
   */
  public static String getApplicationDataDirectory() {
    String result;
    if (isRunningWindows()) {
      // This should be the same as passing CSIDL_APPDATA to the native SHGetFolderPath() Win32
      // function.
      result = System.getenv("APPDATA");
    } else {
      result = System.getProperty("user.home");
      if (isRunningMacOSX()) {
        result += "/Library/Application Support";
      }
    }
    return FileUtils.ensureTrailingSlash(result);
  }

  /**
   * Determines the application data directory for the specified product in a platform-independent
   * way. The path returned by this method always contains a trailing path separator.
   *
   * @param product the product name to use when building the directory name
   * @return the application data directory for the current platform.
   */
  public static String getApplicationDataDirectory(final String product) {
    return getApplicationDataDirectory()
        + (isRunningLinux() ? "." + product.toLowerCase() : product)
        + File.separator;
  }

  /**
   * Determines the system temporary directory. The path returned by this method always contains a
   * trailing path separator.
   *
   * @return The directory where to save temporary files in.
   */
  public static String getTempDirectory() {
    return FileUtils.ensureTrailingSlash(System.getProperty("java.io.tmpdir"));
  }

  /**
   * Determines the system temporary directory for the specified product. The path returned by this
   * method always contains a trailing path separator.
   *
   * @param product the product name to use when building the directory name
   * @return The directory where to save temporary files in.
   */
  public static String getTempDirectory(final String product) {
    return getTempDirectory()
        + (isRunningWindows() ? product : product.toLowerCase())
        + File.separator;
  }

  /**
   * Returns the user's home directory. The path returned by this method always contains a trailing
   * path separator.
   *
   * @return the full path to the user's home directory.
   */
  public static String getUserDirectory() {
    return FileUtils.ensureTrailingSlash(System.getProperty("user.home"));
  }

  /**
   * Determines whether the program is running in Linux
   *
   * @return true, if the program is running in Linux, false otherwise.
   */
  public static boolean isRunningLinux() {
    return System.getProperty("os.name").startsWith("Linux");
  }

  /**
   * Determines whether the program is running in Windows.
   *
   * @return true, if the program is running in Windows, false otherwise.
   */
  public static boolean isRunningMacOSX() {
    return System.getProperty("os.name").startsWith("Mac");
  }

  /**
   * Determines whether the program is running in Windows.
   *
   * @return true, if the program is running in Windows, false otherwise.
   */
  public static boolean isRunningWindows() {
    return System.getProperty("os.name").startsWith("Windows");
  }
}
