// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.implementations;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.BinExportException;
import com.google.security.zynamics.bindiff.exceptions.DifferException;
import com.google.security.zynamics.bindiff.gui.dialogs.directorydiff.DiffPairTableData;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.processes.DiffProcess;
import com.google.security.zynamics.bindiff.processes.ExportProcess;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.diff.DiffLoader;
import com.google.security.zynamics.bindiff.project.matches.DiffMetadata;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import com.google.security.zynamics.bindiff.utils.ExternalAppUtils;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Directory diffing thread implementation that displays visual progress. */
// TODO(cblichmann): This class should just call the BinDiff engine for "batch diffing". The way
//                   this is currently implemented in the UI is confusing: Only files that exist in
//                   the primary and secondary directories with the same basename are considered.
public class DirectoryDiffImplementation extends CEndlessHelperThread {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

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
    parentWindow = checkNotNull(parent);
    this.workspace = checkNotNull(workspace);
    primarySourcePath = checkNotNull(priSourceBasePath);
    secondarySourcePath = checkNotNull(secSourceBasePath);
    this.idbPairs = checkNotNull(idbPairs);
  }

  private String createUniqueExportFileName(File priIdb, File secIdb, ESide side) {
    String priName = FileUtils.getFileBasename(priIdb);
    String secName = FileUtils.getFileBasename(secIdb);

    if (priName.equals(secName)) {
      priName = priName + "_primary";
      secName = secName + "_secondary";
    }

    priName =
        BinDiffFileUtils.forceFilenameEndsWithExtension(
            priName, Constants.BINDIFF_BINEXPORT_EXTENSION);
    secName =
        BinDiffFileUtils.forceFilenameEndsWithExtension(
            secName, Constants.BINDIFF_BINEXPORT_EXTENSION);

    return side == ESide.PRIMARY ? priName : secName;
  }

  private void deleteDestinationDirectory(File destinationDirectory) {
    try {
      BinDiffFileUtils.deleteDirectory(destinationDirectory);
    } catch (IOException e) {
      logger.atSevere().withCause(e).log(
          "Couldn't delete diff folder '%s' after exporting failed",
          destinationDirectory.getPath());
      CMessageBox.showWarning(
          parentWindow,
          String.format(
              "Couldn't delete diff folder '%s' after exporting failed.\n"
                  + "Please delete this folder manually.",
              destinationDirectory.getPath()));
    }
  }

  private List<String> directoryDiff() {
    var matchesPaths = new ArrayList<String>();

    File engineExe;
    try {
      engineExe = ExternalAppUtils.getBinDiffEngine();
    } catch (DifferException | FileNotFoundException e) {
      logger.atSevere().withCause(e).log();
      CMessageBox.showError(parentWindow, e.getMessage());

      return matchesPaths;
    }

    logger.atInfo().log(
        "Start directory diff '%s' vs '%s'", primarySourcePath, secondarySourcePath);

    String workspacePath = workspace.getWorkspaceDir().getPath();
    for (DiffPairTableData data : idbPairs) {
      var destination = String.join(File.separator, workspacePath, data.getDestinationDirectory());

      var primarySource =
          String.join(File.separator, primarySourcePath, data.getIDBLocation(), data.getIDBName());

      var secondarySource =
          String.join(
              File.separator, secondarySourcePath, data.getIDBLocation(), data.getIDBName());

      setDescription(String.format("%s vs %s", data.getIDBName(), data.getIDBName()));

      var primarySourceFile = new File(primarySource);
      var secondarySourceFile = new File(secondarySource);

      String priTargetName =
          createUniqueExportFileName(primarySourceFile, secondarySourceFile, ESide.PRIMARY);
      String secTargetName =
          createUniqueExportFileName(primarySourceFile, secondarySourceFile, ESide.SECONDARY);

      var destinationFolder = new File(destination);

      if (destinationFolder.exists()) {
        var msg =
            String.format(
                "'%s' failed. Reason: Destination folder already exists.",
                data.getDestinationDirectory());
        diffingErrorMessages.add(msg);

        continue;
      }

      if (!destinationFolder.mkdir()) {
        var msg =
            String.format(
                "'%s' failed. Reason: Destination folder cannot be created.",
                data.getDestinationDirectory());
        diffingErrorMessages.add(msg);

        continue;
      }

      // Export primary IDB
      try {
        logger.atInfo().log(
            " - Start exporting primary IDB '%s' to '%s'", primarySource, destination);

        File idaExe = ExternalAppUtils.getIdaExe(primarySourceFile);
        if (idaExe == null || !idaExe.canExecute()) {
          var msg = "Can't start disassembler. Please set correct path in the main settings first.";
          logger.atSevere().log(msg);
          CMessageBox.showError(parentWindow, msg);

          deleteDestinationDirectory(destinationFolder);
          return matchesPaths;
        }

        ExportProcess.startExportProcess(
            idaExe, destinationFolder, primarySourceFile, priTargetName);

        logger.atInfo().log(
            " - Finished exporting primary IDB '%s' to '%s' successfully",
            primarySource, destination);
      } catch (BinExportException e) {
        logger.atInfo().log(
            " - Exporting primary '%s' to '%s' failed. Reason: %s",
            primarySource, destination, e.getMessage());
        var msg =
            String.format(
                "Exporting primary '%s' failed. Reason: %s", primarySource, e.getMessage());
        diffingErrorMessages.add(msg);

        deleteDestinationDirectory(destinationFolder);
        continue;
      }

      // Export secondary IDB
      try {
        logger.atInfo().log(
            " - Start exporting secondary IDB '%s' to '%s'", secondarySource, destination);

        File idaExe = ExternalAppUtils.getIdaExe(secondarySourceFile);
        if (idaExe == null || !idaExe.canExecute()) {
          var msg = "Can't start disassembler. Please set correct path in the main settings first.";
          logger.atSevere().log(msg);
          CMessageBox.showError(parentWindow, msg);

          return matchesPaths;
        }

        ExportProcess.startExportProcess(
            idaExe, destinationFolder, secondarySourceFile, secTargetName);

        logger.atInfo().log(
            " - Finished exporting secondary IDB '%s' to '%s' successfully",
            secondarySource, destination);
      } catch (BinExportException e) {
        logger.atWarning().log(
            " - Exporting secondary '%s' to '%s' failed. Reason: %s",
            secondarySource, destination, e.getMessage());
        var msg =
            String.format(
                "Exporting secondary '%s' failed. Reason: %s", secondarySource, e.getMessage());
        diffingErrorMessages.add(msg);

        deleteDestinationDirectory(destinationFolder);
        continue;
      }

      // Diff
      try {
        String primaryDifferArgument =
            ExportProcess.getExportFilename(priTargetName, destinationFolder);
        String secondaryDifferArgument =
            ExportProcess.getExportFilename(secTargetName, destinationFolder);

        logger.atInfo().log(" - Start diffing '%s'", destinationFolder.getName());
        DiffProcess.startDiffProcess(
            engineExe, primaryDifferArgument, secondaryDifferArgument, destinationFolder);

        String diffBinaryPath =
            DiffProcess.getBinDiffFilename(primaryDifferArgument, secondaryDifferArgument);

        logger.atInfo().log(" - Diffing '%s' done successfully", destinationFolder.getName());

        matchesPaths.add(diffBinaryPath);
      } catch (DifferException e) {
        logger.atWarning().log(
            " - Diffing '%s' failed. Reason: %s", destinationFolder.getName(), e.getMessage());
        var msg =
            String.format(
                "Diffing '%s' failed. Reason: %s", data.getDestinationDirectory(), e.getMessage());
        diffingErrorMessages.add(msg);
      }
    }

    if (diffingErrorMessages.size() == 0) {
      logger.atInfo().log(
          "Finished Directory Diff '%s' vs '%s' successfully",
          primarySourcePath, secondarySourcePath);
    } else {
      logger.atWarning().log(
          "Finished Directory Diff '%s' vs '%s' with errors.",
          primarySourcePath, secondarySourcePath);
    }

    return matchesPaths;
  }

  @Override
  protected void runExpensiveCommand() throws Exception {
    List<String> matchesPaths = directoryDiff();
    if (!matchesPaths.isEmpty()) {
      setGeneralDescription("Preloading diffs...");
    }

    for (String path : matchesPaths) {
      if (path == null) {
        continue;
      }
      var newMatchesDatabase = new File(path);

      try {
        setDescription(String.format("Loading '%s'", newMatchesDatabase.getName()));

        if (newMatchesDatabase.exists()) {
          DiffMetadata preloadedMatches = DiffLoader.preloadDiffMatches(newMatchesDatabase);

          workspace.addDiff(newMatchesDatabase, preloadedMatches, false);
        }
      } catch (Exception e) {
        var msg =
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
