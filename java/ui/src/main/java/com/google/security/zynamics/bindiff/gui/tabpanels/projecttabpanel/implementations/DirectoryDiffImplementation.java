package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.implementations;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.BinExportException;
import com.google.security.zynamics.bindiff.exceptions.DifferException;
import com.google.security.zynamics.bindiff.gui.dialogs.directorydiff.DiffPairTableData;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.processes.DiffProcess;
import com.google.security.zynamics.bindiff.processes.ExportProcess;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.diff.DiffLoader;
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.CFileUtils;
import com.google.security.zynamics.bindiff.utils.ExternalAppUtils;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** TODO(cblichmann): comment function. */
public class DirectoryDiffImplementation extends CEndlessHelperThread {
  private final List<String> diffingErrorMessages = new ArrayList<>();
  private final List<String> openingDiffErrorMessages = new ArrayList<>();

  private final MainWindow parentWindow;
  private final Workspace workspace;
  private final String primarySourcePath;
  private final String secondarySourcePath;
  private final List<DiffPairTableData> idbPairs;

  public DirectoryDiffImplementation(
      MainWindow parent,
      Workspace workspace,
      String priSourceBasePath,
      String secSourceBasePath,
      List<DiffPairTableData> idbPairs) {
    parentWindow = Preconditions.checkNotNull(parent);
    this.workspace = Preconditions.checkNotNull(workspace);
    primarySourcePath = Preconditions.checkNotNull(priSourceBasePath);
    secondarySourcePath = Preconditions.checkNotNull(secSourceBasePath);
    this.idbPairs = Preconditions.checkNotNull(idbPairs);
  }

  private String createUniqueExportFileName(
      final File priIdb, final File secIdb, final ESide side) {
    String priName = priIdb.getName();
    priName = CFileUtils.removeFileExtension(priName);

    String secName = secIdb.getName();
    secName = CFileUtils.removeFileExtension(secName);

    if (priName.equals(secName)) {
      priName = priName + "_primary";
      secName = secName + "_secondary";
    }

    priName =
        CFileUtils.forceFilenameEndsWithExtension(priName, Constants.BINDIFF_BINEXPORT_EXTENSION);
    secName =
        CFileUtils.forceFilenameEndsWithExtension(secName, Constants.BINDIFF_BINEXPORT_EXTENSION);

    return side == ESide.PRIMARY ? priName : secName;
  }

  private void deleteDirectory(final MainWindow mainWindow, final File destinationFolder) {
    try {
      CFileUtils.deleteDirectory(destinationFolder);
    } catch (final IOException exception) {
      Logger.logException(
          exception,
          String.format(
              "Couldn't delete diff folder '%s' after exporting failed.\n"
                  + "Please delete this folder manually.",
              destinationFolder.getPath()));
      CMessageBox.showWarning(
          mainWindow,
          String.format(
              "Couldn't delete diff folder '%s' after exporting failed.\n"
                  + "Please delete this folder manually.",
              destinationFolder.getPath()));
    }
  }

  private List<String> directoryDiff() {
    // TODO(cblichmann): Rewrite error-handling to not return a list of
    // plain error messages
    final List<String> matchesPaths = new ArrayList<>();

    final String engineExe = ExternalAppUtils.getCommandLineDiffer();

    if (!new File(engineExe).exists()) {
      final String msg =
          String.format("Can't start directory diff. Couldn't find engine at '%s'", engineExe);

      Logger.logSevere(msg);
      CMessageBox.showError(parentWindow, msg);

      return matchesPaths;
    }

    Logger.logInfo(
        "Start Directory Diff '" + primarySourcePath + "' vs '" + secondarySourcePath + "'");

    final String workspacePath = workspace.getWorkspaceDir().getPath();
    final List<DiffPairTableData> idbPairsData = idbPairs;
    for (final DiffPairTableData data : idbPairsData) {
      final String destination =
          String.format("%s%s%s", workspacePath, File.separator, data.getDestinationDirectory());

      final String primarySource =
          String.format(
              "%s%s%s%s%s",
              primarySourcePath,
              File.separator,
              data.getIDBLocation(),
              File.separator,
              data.getIDBName());

      final String secondarySource =
          String.format(
              "%s%s%s%s%s",
              secondarySourcePath,
              File.separator,
              data.getIDBLocation(),
              File.separator,
              data.getIDBName());

      setDescription(String.format("%s vs %s", data.getIDBName(), data.getIDBName()));

      final File primarySourceFile = new File(primarySource);
      final File secondarySourceFile = new File(secondarySource);

      final String priTargetName =
          createUniqueExportFileName(primarySourceFile, secondarySourceFile, ESide.PRIMARY);
      final String secTargetName =
          createUniqueExportFileName(primarySourceFile, secondarySourceFile, ESide.SECONDARY);

      final File destinationFolder = new File(destination);

      if (destinationFolder.exists()) {
        String msg =
            String.format(
                "'%s' failed. Reason: Destination folder already exists.",
                data.getDestinationDirectory());
        diffingErrorMessages.add(msg);

        continue;
      }

      destinationFolder.mkdir();

      if (!destinationFolder.exists()) {
        String msg =
            String.format(
                "'%s' failed. Reason: Destination folder cannot be created.",
                data.getDestinationDirectory());
        diffingErrorMessages.add(msg);

        continue;
      }

      // export primary IDB
      try {
        Logger.logInfo(
            " - Start exporting primary IDB '" + primarySource + "' to '" + destination + "'");

        final File idaExe = ExternalAppUtils.getIdaExe(primarySourceFile);

        if (idaExe == null || !idaExe.canExecute()) {
          final String msg =
              "Can't start disassembler. Please set correct path in the main settings first.";

          Logger.logSevere(msg);
          CMessageBox.showError(parentWindow, msg);

          deleteDirectory(parentWindow, destinationFolder);

          return matchesPaths;
        }

        ExportProcess.startExportProcess(
            idaExe, destinationFolder, primarySourceFile, priTargetName);

        Logger.logInfo(
            " - Finished exporting primary IDB '"
                + primarySource
                + "' to '"
                + destination
                + "' successfully.");
      } catch (final BinExportException e) {
        Logger.logInfo(
            " - Exporting primary '%s' to '%s' failed. Reason: %s",
            primarySource, destination, e.getMessage());
        String msg =
            String.format(
                "Exporting primary '%s' failed. Reason: %s", primarySource, e.getMessage());
        diffingErrorMessages.add(msg);

        deleteDirectory(parentWindow, destinationFolder);

        continue;
      }

      // export secondary IDB
      try {
        Logger.logInfo(
            " - Start exporting secondary IDB '" + secondarySource + "' to '" + destination + "'");

        final File idaExe = ExternalAppUtils.getIdaExe(secondarySourceFile);

        if (idaExe == null || !idaExe.canExecute()) {
          final String msg =
              "Can't start disassembler. Please set correct path in the main settings first.";

          Logger.logSevere(msg);
          CMessageBox.showError(parentWindow, msg);

          return matchesPaths;
        }

        ExportProcess.startExportProcess(
            idaExe, destinationFolder, secondarySourceFile, secTargetName);

        Logger.logInfo(
            " - Finished exporting secondary IDB '"
                + secondarySource
                + "' to '"
                + destination
                + "' successfully.");
      } catch (final BinExportException e) {
        Logger.logInfo(
            " - Exporting secondary '%s' to '%s' failed. Reason: %s",
            secondarySource, destination, e.getMessage());
        String msg =
            String.format(
                "Exporting secondary '%s' failed. Reason: %s", secondarySource, e.getMessage());
        diffingErrorMessages.add(msg);

        deleteDirectory(parentWindow, destinationFolder);

        continue;
      }

      // diff
      try {
        final String primaryDifferArgument =
            ExportProcess.getExportedFileName(priTargetName, destinationFolder);
        final String secondaryDifferArgument =
            ExportProcess.getExportedFileName(secTargetName, destinationFolder);

        Logger.logInfo(" - Start Diffing '" + destinationFolder.getName() + "'");
        DiffProcess.startDiffProcess(
            engineExe, primaryDifferArgument, secondaryDifferArgument, destinationFolder);

        final String diffBinaryPath =
            DiffProcess.getBinDiffFileName(primaryDifferArgument, secondaryDifferArgument);

        Logger.logInfo(" - Diffing '%s' done successfully.", destinationFolder.getName());

        matchesPaths.add(diffBinaryPath);
      } catch (final BinExportException e) {
        Logger.logInfo(
            " - Diffing '%s' failed. Reason: %s", destinationFolder.getName(), e.getMessage());
        String msg =
            String.format(
                "Diffing '%s' failed. Reason: %s", data.getDestinationDirectory(), e.getMessage());
        diffingErrorMessages.add(msg);

        continue;
      } catch (final DifferException e) {
        Logger.logInfo(
            " - Diffing '%s' failed. Reason: %s", destinationFolder.getName(), e.getMessage());
        String msg =
            String.format(
                "Diffing '%s' failed. Reason: %s", data.getDestinationDirectory(), e.getMessage());
        diffingErrorMessages.add(msg);

        continue;
      }
    }

    if (diffingErrorMessages.size() == 0) {
      Logger.logInfo(
          "Finished Directory Diff '"
              + primarySourcePath
              + "' vs '"
              + secondarySourcePath
              + "' successfully.");
    } else {
      Logger.logInfo(
          "Finished Directory Diff '"
              + primarySourcePath
              + "' vs '"
              + secondarySourcePath
              + "' with errors.");
    }

    return matchesPaths;
  }

  @Override
  protected void runExpensiveCommand() throws Exception {
    List<String> matchesPaths = directoryDiff();

    if (matchesPaths.size() > 0) {
      setGeneralDescription("Preloading diffs...");
    }

    for (final String path : matchesPaths) {
      if (path == null) {
        continue;
      }
      final File newMatchesDatabase = new File(path);

      try {
        setDescription(String.format("Loading '%s'", newMatchesDatabase.getName()));

        if (newMatchesDatabase.exists()) {
          final DiffMetaData preloadedMatches = DiffLoader.preloadDiffMatches(newMatchesDatabase);

          workspace.addDiff(newMatchesDatabase, preloadedMatches, false);
        }
      } catch (final Exception e) {
        String msg =
            String.format("Could not load '%s' into workspace.", newMatchesDatabase.getName());
        openingDiffErrorMessages.add(msg);
      }
    }
  }

  public List<String> getDiffingErrorMessages() {
    return diffingErrorMessages;
  }

  public List<String> getOpeningDiffErrorMessages() {
    return openingDiffErrorMessages;
  }
}
