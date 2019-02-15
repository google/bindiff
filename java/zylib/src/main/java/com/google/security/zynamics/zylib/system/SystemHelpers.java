package com.google.security.zynamics.zylib.system;

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
   * writable for users without administrative privileges, even on Windows. The path returned
   * by this method always contains a trailing path separator.
   *
   * @param product the product name to use when building the directory name
   * @return the machine-wide application data directory
   */
  public static String getAllUsersApplicationDataDirectory(final String product) {
    return getAllUsersApplicationDataDirectory()
        + (isRunningWindows() ? product : "." + product.toLowerCase())
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
        + (isRunningWindows() ? product : "." + product.toLowerCase())
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

  /**
   * Returns true if the current system has a 64bit architecture.
   */
  public static boolean is64BitArchitecture() {
    if (isRunningWindows()) {
      return "AMD64".equals(System.getenv("PROCESSOR_ARCHITECTURE"))
          || "AMD64".equals(System.getenv("PROCESSOR_ARCHITEW6432"));
    }

    throw new RuntimeException("Not implemented");
  }
}
