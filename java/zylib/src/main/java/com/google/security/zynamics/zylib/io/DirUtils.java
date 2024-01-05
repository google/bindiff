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

package com.google.security.zynamics.zylib.io;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/** Helper class that provides common directory functions. */
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

  public static void traverse(
      final File directory, final IDirectoryTraversalCallback callback, final boolean recurse) {
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
  public static void traverse(
      final File directory,
      final IDirectoryTraversalCallback callback,
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
