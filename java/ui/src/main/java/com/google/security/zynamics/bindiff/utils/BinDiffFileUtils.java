package com.google.security.zynamics.bindiff.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** BinDiff-specific file name and filesystem utility methods. */
public class BinDiffFileUtils {

  private BinDiffFileUtils() {
    // Static utility class
  }

  public static void deleteDirectory(final File directory) throws IOException {
    if (directory == null || !directory.isDirectory() || !directory.exists()) {
      return;
    }

    try {
      final File[] files = directory.listFiles();
      if (files == null) {
        throw new IOException("Internal error while listing files");
      }
      for (final File file : files) {
        if (file.isDirectory()) {
          deleteDirectory(file);
          continue;
        }

        if (!file.delete()) {
          throw new IOException("Error while deleting file");
        }
      }

      if (!directory.delete()) {
        throw new IOException("Error while deleting directory");
      }
    } catch (final SecurityException e) {
      throw new IOException("Error while deleting directory or sub-tree: " + e.getMessage());
    }
  }

  public static List<String> findFiles(final File directory, final List<String> extensionFilter) {
    final List<String> foundFiles = new ArrayList<>();
    if (directory == null || !directory.isDirectory() || !directory.exists()) {
      return foundFiles;
    }

    final File[] files = directory.listFiles();
    if (files == null) {
      return foundFiles;
    }

    for (final File file : files) {
      if (file.isDirectory()) {
        foundFiles.addAll(findFiles(file, extensionFilter));
      }

      for (final String extension : extensionFilter) {
        final String filter = "." + extension;

        if (file.getPath().endsWith(filter)) {
          foundFiles.add(file.getPath());
        }
      }
    }

    return foundFiles;
  }

  public static String forceFilenameEndsNotWithExtension(
      final String filename, final String extension) {
    String value = filename;
    if (value.toLowerCase().endsWith(("." + extension).toLowerCase())) {
      value = filename.substring(0, filename.length() - ("." + extension).length());
    }

    return value;
  }

  public static String forceFilenameEndsWithExtension(
      final String filename, final String extension) {
    if (filename.toLowerCase().endsWith(("." + extension).toLowerCase())) {
      return filename;
    }

    return filename + "." + extension;
  }

  public static String forceFileSeparator(final String filename) {
    return filename.replace(File.separator.equals("/") ? "\\" : "/", File.separator);
  }

  public static String removeFileExtension(final String fileName) {
    final int index = fileName.lastIndexOf(".");
    return index < 1 ? fileName : fileName.substring(0, index);
  }
}
