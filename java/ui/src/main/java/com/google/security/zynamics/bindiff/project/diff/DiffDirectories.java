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

package com.google.security.zynamics.bindiff.project.diff;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.matches.DiffMetadata;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DiffDirectories {
  private DiffDirectories() {}

  public static File getBinExportFile(
      final File matchesBinary, final DiffMetadata metaData, final ESide side) {
    return new File(
        String.format(
            "%s%s%s.%s",
            matchesBinary.getParent(),
            File.separator,
            metaData.getIdbName(side),
            Constants.BINDIFF_BINEXPORT_EXTENSION));
  }

  public static String getDiffDestinationDirectoryName(
      final String primaryInFile, final String secondaryInFile) {
    final StringBuilder dirName = new StringBuilder();

    final File primaryFile = new File(primaryInFile);
    final File secondaryFile = new File(secondaryInFile);

    final List<String> extensions =
        Arrays.asList(
            Constants.BINDIFF_BINEXPORT_EXTENSION.toLowerCase(),
            Constants.IDB32_EXTENSION.toLowerCase(),
            Constants.IDB64_EXTENSION.toLowerCase());
    if (!extensions.contains(FileUtils.getFileExtension(primaryFile).toLowerCase())
        || !extensions.contains(FileUtils.getFileExtension(secondaryFile).toLowerCase())) {
      return "";
    }

    dirName.append(FileUtils.getFileBasename(primaryFile));
    dirName.append(" vs ");
    dirName.append(FileUtils.getFileBasename(secondaryFile));
    return dirName.toString();
  }
}
