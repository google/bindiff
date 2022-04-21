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

package com.google.security.zynamics.zylib.io;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.system.SystemHelpers;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Helper class that provides common file operations.
 */
public class FileUtils {
  /**
   * Calculates the MD5 value of a file.
   *
   * @param file The file in question.
   * @return A string that holds the MD5 sum of the file.
   *
   * @throws IOException
   */
  public static String calcMD5(final File file) throws IOException {
    // TODO: This method read the entire file in RAM. Make it iterative and
    // use a BufferedReader

    final FileInputStream reader = new FileInputStream(file);
    final byte[] data = new byte[(int) file.length()];

    reader.read(data);
    reader.close();

    final MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (final NoSuchAlgorithmException e) {
      assert false : "MD5 not in list of algorithms";
      throw new RuntimeException(e);
    }

    md.update(data);

    final byte[] digest = md.digest();

    final StringBuilder md5 = new StringBuilder();

    for (final byte b : digest) {
      md5.append(String.format("%02X", b));
    }

    return md5.toString();
  }

  /**
   * Returns a copy of the specified String with a trailing path separator. This method ensures that
   * there are no duplicate trailing separators.
   *
   * @param path the path to ensure a trailing separator for
   * @return the copied String or an empty String if path is null or empty.
   */
  public static String ensureTrailingSlash(final String path) {
    if ((path == null) || path.equals("")) {
      return "";
    }

    final StringBuilder buf = new StringBuilder(path);
    while (buf.charAt(buf.length() - 1) == File.separatorChar) {
      buf.deleteCharAt(buf.length() - 1);
    }
    return buf.append(File.separatorChar).toString();
  }

  public static boolean exists(final String filename) {
    return new File(filename).exists();
  }

  public static String extractNameFromPath(final String name) {
    return new File(name).getName();
  }

  /**
   * Find the local root path for the specified class. For classes packaged in a JAR archive, the
   * local root path is the directory the JAR file is contained in. For regular .class files, the
   * local root path is conceptually the directory the default package resides in.
   *
   * <p>Example for a class com.google.security.zynamics.bindiff.ui.Main packaged in
   * /opt/bindiff/bin/bindiff.jar: /opt/bindiff/bin/
   *
   * <p>Example for a class com.google.security.zynamics.bindiff.ui.Main with the file Main.class
   * residing in /home/user/Dev/BinDiffGUI/bin/com/zynamics/bindiff/ui:
   * /home/user/Dev/BinDiffGUI/bin/
   *
   * @param klazz a {@code Class} instance for the class to find the root path for.
   * @return the local root path for klazz
   */
  public static String findLocalRootPath(final Class<?> klazz) {
    final String compiledFileName = klazz.getSimpleName() + ".class";
    final URL classUrl = klazz.getResource(compiledFileName);
    Preconditions.checkNotNull(
        classUrl, "Cannot find resource for class " + klazz.getCanonicalName());

    final String classUrlProto = classUrl.getProtocol();
    final String classUrlPath = classUrl.getPath();
    final String result;

    // Define a special path start index on Windows to deal with slashes before drive letters.
    final int startIdx = SystemHelpers.isRunningWindows() ? 1 : 0;
    if (classUrlProto.equals("file")) {
      // Local file system path
      final Package classPackage = klazz.getPackage();
      if (classPackage != null) {
        final String classPackagePath = classPackage.getName().replace('.', '/');
        // If the ClassLoader did load the class specified by klazz,
        // classPackagePath can always be found in resPath
        result = classUrlPath.substring(startIdx, classUrlPath.lastIndexOf(classPackagePath));
      } else {
        // Special case, find the path of a class in the default package
        result = classUrlPath.substring(startIdx, classUrlPath.lastIndexOf('/'));
      }
    } else {
      if (!classUrlProto.equals("jar")) {
        throw new RuntimeException("Cannot find root path for remote classes");
      }
      if (!classUrlPath.startsWith("file:")) {
        throw new IllegalArgumentException(
            "Cannot find root path for class " + klazz.getCanonicalName());
      }

      // The class is packaged in a JAR archive
      final String jarPath = classUrlPath.substring(5, classUrlPath.lastIndexOf('!'));

      result = jarPath.substring(startIdx, jarPath.lastIndexOf('/'));
    }

    try {
      return URLDecoder.decode(
          result.replace('/', File.separatorChar), System.getProperty("file.encoding"));
    } catch (final UnsupportedEncodingException e) {
      assert false : "Platform default URL encoding should always be available";
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the basename of the specified <code>File</code>.
   *
   * @param file a <code>File</code> object to obtain the basename from
   * @return the basename of file
   */
  public static String getFileBasename(final File file) {
    final String filename = file.getName();
    final int dotIdx = filename.lastIndexOf(".");

    return dotIdx == -1 ? filename : filename.substring(0, dotIdx);
  }

  public static String getFileExtension(final File file) {
    final String filename = file.getAbsolutePath();
    final int pos = filename.lastIndexOf(".");
    if (pos < 1 || pos == filename.length() - 1) {
      return "";
    }
    return filename.substring(pos + 1);
  }

  /**
   * Shortens a filename to a specified length by adding path ellipsis ("..."). Applies a heuristic
   * to work for both Windows and Unix style paths.
   *
   * @param filename the filename to shorten
   * @param maxlen the maximum number of characters to return
   * @return the truncated string with ellipsis added
   */
  public static String getPathEllipsis(final String filename, final int maxlen) {
    return getPathEllipsis(filename, maxlen, "...");
  }

  /**
   * Shortens a filename to a specified length by adding path ellipsis. Applies a heuristic to work
   * for both Windows and Unix style paths.
   *
   * @param filename the filename to shorten
   * @param maxlen the maximum number of characters to return
   * @param ellipsis the string to be used as an ellipsis
   * @return the truncated string with ellipsis added
   */
  public static String getPathEllipsis(
      final String filename, final int maxlen, final String ellipsis) {
    final int len = filename.length();
    final int ellLen = ellipsis.length();
    Preconditions.checkArgument(maxlen >= (4 * ellLen), "Maximum length too short");

    if (len <= maxlen) {
      return filename;
    }

    // Heuristic to detect path separator used
    int i = 0;
    char sep = File.separatorChar;
    boolean found = false;
    while (!found && (i < len)) {
      sep = filename.charAt(i);
      found = ((sep == '/') || (sep == '\\'));
      i++;
    }

    final StringBuilder result = new StringBuilder();
    if (!found) {
      // Filename only, just truncate and add ellipsis
      result.append(filename.substring(0, maxlen - ellLen));
      result.append(ellipsis);
    } else {
      // Since filenames contain valuable information, split the string
      // right before the filename and truncate both halves.
      final int lastComp = filename.lastIndexOf(sep);

      final int splitLen = maxlen - (len - lastComp);
      final int splitPos = (splitLen / 2) - (ellLen / 2);
      if (splitPos > 0) {
        // Left half
        result.append(filename.substring(0, (splitPos - (ellLen % 2)) + (splitLen % 2)));
        // Ellipsis
        result.append(ellipsis);
        // Right half and filename
        result.append(filename.substring((lastComp - splitPos) + (ellLen % 2)));
      } else {
        // The split point is negative, this means the filename is too
        // long. Handle this specially by keeping the first 3
        // characters, add an ellipsis and truncate the rest. This is
        // done because the first few characters can convey useful
        // information (on Windows, for example, the drive letter).
        result.append(filename.substring(0, 3));
        result.append(ellipsis);
        result.append(filename.substring(lastComp, (lastComp + maxlen) - 3 - (2 * ellLen)));
        result.append(ellipsis);
      }
    }

    return result.toString();
  }

  /**
   * Reads a text file into a string.
   *
   * @param file The file to read.
   * @return The text read from the file.
   *
   * @throws IOException
   */
  public static String readTextfile(final File file) throws IOException {
    final StringBuffer contents = new StringBuffer();

    final BufferedReader input = new BufferedReader(new FileReader(file));

    final String lineSeparator = System.getProperty("line.separator");

    try {
      String line = null;

      while ((line = input.readLine()) != null) {
        contents.append(line);
        contents.append(lineSeparator);
      }
    } finally {
      input.close();
    }

    return contents.toString();
  }

  /**
   * Reads a text file into a string.
   *
   * @param filename The name of the text file.
   * @return The text read from the file.
   *
   * @throws IOException
   */
  public static String readTextfile(final String filename) throws IOException {
    return readTextfile(new File(filename));
  }

  public static List<String> readTextfileLines(final File file) throws IOException {
    return StreamUtils.readLinesFromReader(new FileReader(file));
  }

  /**
   * Writes a byte array to a binary file.
   *
   * @param file The file to write to.
   * @param data The data to write.
   *
   * @throws IOException
   */
  public static void writeBinaryFile(final File file, final byte[] data) throws IOException {
    final FileOutputStream fos = new FileOutputStream(file);

    try {
      fos.write(data);
    } finally {
      fos.close();
    }
  }

  /**
   * Write text to file.
   *
   * @param file The {@link File file} to write to.
   * @param text The {@link String text} which to write.
   *
   * @throws IOException if the text could not be written to the file.
   */
  public static void writeTextFile(final File file, final String text) throws IOException {
    Preconditions.checkNotNull(file, "Error: file argument can not be null");
    Preconditions.checkNotNull(text, "Error: text argument can not be null");
    final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    try {
      writer.write(text);
    } finally {
      writer.close();
    }
  }
}
