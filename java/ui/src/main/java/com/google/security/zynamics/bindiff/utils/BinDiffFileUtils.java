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

package com.google.security.zynamics.bindiff.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/** BinDiff-specific file name and filesystem utility methods. */
public class BinDiffFileUtils {

  private BinDiffFileUtils() {}

  public static void deleteDirectory(final File directory) throws IOException {
    if (directory == null || !directory.isDirectory() || !directory.exists()) {
      return;
    }

    Files.walkFileTree(
        directory.toPath(),
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
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
