// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.io;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Helper class that provides common directory functions.
 */
public class DirUtils {
  /**
   * Recursively traverses through a directory.
   * 
   * @param directory The directory to traverse.
   * @param callback The callback to call for each file.
   */
  public static void traverse(final File directory, final IDirectoryTraversalCallback callback) {
    traverse(directory, callback, true);
  }

  public static void traverse(final File directory, final IDirectoryTraversalCallback callback,
      final boolean recurse) {
    final File[] files = directory.listFiles();

    if (files == null) {
      return;
    }

    callback.entering(directory);

    for (final File file : files) {
      if (!file.isDirectory()) {
        callback.nextFile(file);
      }
    }

    if (recurse) {
      for (final File file : files) {
        if (file.isDirectory()) {
          traverse(file, callback);
        }
      }
    }

    callback.leaving(directory);
  }

  /**
   * Recursively traverses through a directory.
   * 
   * @param directory The directory to traverse.
   * @param callback The callback to call for each file.
   */
  public static void traverse(final File directory, final IDirectoryTraversalCallback callback,
      final Comparator<File> sorter) {
    final File[] files = directory.listFiles();

    if (files == null) {
      return;
    }

    Arrays.sort(files, sorter);

    callback.entering(directory);

    for (final File file : files) {
      if (!file.isDirectory()) {
        callback.nextFile(file);
      }
    }

    for (final File file : files) {
      if (file.isDirectory()) {
        traverse(file, callback);
      }
    }

    callback.leaving(directory);
  }
}
