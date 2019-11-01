package com.google.security.zynamics.bindiff.project.diff;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DiffDirectories {
  private DiffDirectories() {}

  public static File getBinExportFile(
      final File matchesBinary, final DiffMetaData metaData, final ESide side) {
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
