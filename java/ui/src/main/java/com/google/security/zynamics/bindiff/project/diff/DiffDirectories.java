package com.google.security.zynamics.bindiff.project.diff;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DiffDirectories {
  private static File getDiffsSubDirectory(final Diff diff, final String name) {
    final File diffFile = diff.getMatchesDatabase();

    final String diffViewDirPath =
        String.format(
            "%s%s%s - %s", diffFile.getParent(), File.separator, name, diffFile.getName());

    return new File(diffViewDirPath);
  }

  public static File createDiffCommentsDirectory(final Diff diff) throws IOException {
    final File diffCommentsDir = getDiffCommentsDirectory(diff);

    if (!diffCommentsDir.exists()) {
      if (!diffCommentsDir.mkdir()) {
        Logger.logSevere(
            "Severe: Couldn't create diffs comments folder. Save view comment failed.");

        throw new IOException("Couldn't create diffs comments folder. Save view comment failed.");
      }
    }

    return diffCommentsDir;
  }

  public static File createDiffDirectory(final File workspaceDir, final File matchesBinary)
      throws IOException {
    final String newDiffDirPath =
        String.format("%s%s%s", workspaceDir.getPath(), File.separator, matchesBinary.getName());

    final File newDiffDir = new File(newDiffDirPath);

    newDiffDir.mkdir();

    if (!newDiffDir.exists()) {
      throw new IOException();
    }

    return newDiffDir;
  }

  public static File createDiffReportsDirectory(final Diff diff) throws IOException {
    final File diffReportsDir = getDiffReportsDirectory(diff);

    if (!diffReportsDir.exists()) {
      if (!diffReportsDir.mkdir()) {
        Logger.logSevere("Severe: Couldn't create diffs reports folder. Generate report failed.");

        throw new IOException("Couldn't create diffs reports folder. Generate report failed.");
      }
    }

    return diffReportsDir;
  }

  public static File createDiffViewsDirectory(final Diff diff) throws IOException {
    final File diffViewDir = getDiffViewsDirectory(diff);

    if (!diffViewDir.exists()) {
      if (!diffViewDir.mkdir()) {
        Logger.logSevere("Severe: Couldn't create views folder. Save view failed.");

        throw new IOException("Couldn't create user views folder. Save view failed.");
      }
    }

    return diffViewDir;
  }

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

  public static File getDiffCommentsDirectory(final Diff diff) {
    return getDiffsSubDirectory(diff, "Comments");
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

  public static File getDiffReportsDirectory(final Diff diff) {
    return getDiffsSubDirectory(diff, "Reports");
  }

  public static File getDiffViewsDirectory(final Diff diff) {
    return getDiffsSubDirectory(diff, "Views");
  }
}
