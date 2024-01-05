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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.implementations;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.FluentLogger;
import com.google.common.io.ByteStreams;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.BinExportException;
import com.google.security.zynamics.bindiff.exceptions.DifferException;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Class NewDiffImplementation implements the UI flow for adding new .BinDiff results to the
 * workspace.
 */
public class NewDiffImplementation extends CEndlessHelperThread {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final MainWindow parentWindow;
  private final Workspace workspace;

  private File primaryBinExportFile;
  private File secondaryBinExportFile;

  private final File primaryIdbFile;
  private final File secondaryIdbFile;

  private final File destinationDirectory;

  public NewDiffImplementation(
      MainWindow window,
      Workspace workspace,
      File primaryIdbFile,
      File secondaryIdbFile,
      File primaryBinExportFile,
      File secondaryBinExportFile,
      File destinationDir) {
    parentWindow = checkNotNull(window);
    this.workspace = checkNotNull(workspace);
    destinationDirectory = checkNotNull(destinationDir);

    this.primaryIdbFile = primaryIdbFile;
    this.secondaryIdbFile = secondaryIdbFile;

    this.primaryBinExportFile = primaryBinExportFile;
    this.secondaryBinExportFile = secondaryBinExportFile;
  }

  private String createUniqueExportFileName(ESide side) {
    String priName = "";

    if (primaryBinExportFile != null) {
      priName = primaryBinExportFile.getName();
    } else if (primaryIdbFile != null) {
      priName = primaryIdbFile.getName();
    }
    priName = BinDiffFileUtils.removeFileExtension(priName);

    String secName = "";
    if (secondaryBinExportFile != null) {
      secName = secondaryBinExportFile.getName();
    } else if (secondaryIdbFile != null) {
      secName = secondaryIdbFile.getName();
    }
    secName = BinDiffFileUtils.removeFileExtension(secName);

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

  void deleteDestinationDirectory(File destinationDirectory) {
    try {
      BinDiffFileUtils.deleteDirectory(destinationDirectory);
    } catch (IOException e2) {
      logger.atSevere().withCause(e2).log(
          "Couldn't delete diff folder '%s' after diffing failed", destinationDirectory.getPath());
      CMessageBox.showWarning(
          parentWindow,
          String.format(
              "Couldn't delete diff folder '%s' after diffing failed.\n"
                  + "Please delete this folder manually.",
              destinationDirectory.getPath()));
    }
  }

  boolean exportSingleIdb(File idbFile, File destinationDirectory, String targetName) {
    File idaExe = ExternalAppUtils.getIdaExe(primaryIdbFile);
    if (idaExe == null || !idaExe.canExecute()) {
      var msg = "Cannot start disassembler. Please set the correct path in main settings first.";
      logger.atSevere().log("%s", msg);
      CMessageBox.showError(parentWindow, msg);
      return false;
    }

    try {
      ExportProcess.startExportProcess(idaExe, destinationDirectory, idbFile, targetName);
      return true;
    } catch (BinExportException e) {
      logger.atSevere().withCause(e).log("%s", e.getMessage());
      CMessageBox.showError(parentWindow, e.getMessage());
      deleteDestinationDirectory(destinationDirectory);
      return false;
    }
  }

  File copyBinExport(File binexport, File destinationDirectory, String targetName) {
    try {
      var targetBinExport = Path.of(destinationDirectory.getPath(), targetName).toFile();
      if (!targetBinExport.exists()) {
        ByteStreams.copy(new FileInputStream(binexport), new FileOutputStream(targetBinExport));
      }

      return targetBinExport;
    } catch (IOException e) {
      var message = "Couldn't copy primary BinExport binaries into the new diff directory";
      logger.atSevere().withCause(e).log("%s", message);
      CMessageBox.showError(parentWindow, message + ": " + e.getMessage());

      deleteDestinationDirectory(destinationDirectory);
    }
    return null;
  }

  public String createNewDiff() {
    // TODO(cblichmann): Remove code duplication with DirectoryDiffImplementation.

    setDescription("Creating destination directory...");
    if (!destinationDirectory.isDirectory() && !destinationDirectory.mkdirs()) {
      logger.atSevere().log("Could not create destination directory");
      CMessageBox.showError(parentWindow, "Could not create destination directory.");
      return null;
    }

    String primaryTargetName = createUniqueExportFileName(ESide.PRIMARY);
    String secondaryTargetName = createUniqueExportFileName(ESide.SECONDARY);

    // Export or copy primary side
    if (primaryIdbFile != null) {
      logger.atInfo().log(
          "- Exporting primary IDB '%s' to '%s'",
          primaryIdbFile.getPath(), destinationDirectory.getPath());

      setDescription("Exporting primary IDB...");
      if (!exportSingleIdb(primaryIdbFile, destinationDirectory, primaryTargetName)) {
        return null;
      }
    } else if (primaryBinExportFile != null) {
      setDescription("Copying primary BinExport binary...");
      primaryBinExportFile =
          copyBinExport(primaryBinExportFile, destinationDirectory, primaryTargetName);
      if (primaryBinExportFile == null) {
        return null;
      }
    }

    // Export or copy secondary side
    if (secondaryIdbFile != null) {
      logger.atInfo().log(
          "- Exporting secondary IDB '%s' to '%s'",
          secondaryIdbFile.getPath(), destinationDirectory.getPath());

      setDescription("Exporting secondary IDB...");
      if (!exportSingleIdb(secondaryIdbFile, destinationDirectory, secondaryTargetName)) {
        return null;
      }
    } else if (secondaryBinExportFile != null) {
      setDescription("Copying secondary BinExport binary...");
      secondaryBinExportFile =
          copyBinExport(secondaryBinExportFile, destinationDirectory, secondaryTargetName);
      if (secondaryBinExportFile == null) {
        return null;
      }
    }

    // Perform the actual diff
    try {
      // Use the original file base names instead of createUniqueExportFileName().
      String primaryBinExportPath =
          primaryIdbFile != null
              ? ExportProcess.getExportFilename(primaryTargetName, destinationDirectory)
              : primaryBinExportFile.getPath();
      String secondaryBinExportPath =
          secondaryIdbFile != null
              ? ExportProcess.getExportFilename(secondaryTargetName, destinationDirectory)
              : secondaryBinExportFile.getPath();

      setDescription("Diffing...");
      logger.atInfo().log("- Diffing '%s'", destinationDirectory.getName());

      DiffProcess.getBinDiffFilename(primaryBinExportPath, secondaryBinExportPath);
      DiffProcess.startDiffProcess(
          ExternalAppUtils.getBinDiffEngine(),
          primaryBinExportPath,
          secondaryBinExportPath,
          destinationDirectory);
      String diffBinaryPath =
          DiffProcess.getBinDiffFilename(primaryBinExportPath, secondaryBinExportPath);

      logger.atInfo().log("- Diffing done successfully");
      return diffBinaryPath;
    } catch (DifferException | FileNotFoundException | RuntimeException e) {
      logger.atSevere().withCause(e).log("%s", e.getMessage());
      CMessageBox.showError(parentWindow, e.getMessage());
    }
    return null;
  }

  @Override
  protected void runExpensiveCommand() {
    try {
      String matchedPath = createNewDiff();
      if (matchedPath == null) {
        return;
      }

      var newMatchesDatabase = new File(matchedPath);
      if (newMatchesDatabase.exists()) {
        setDescription("Preloading Diff...");
        DiffMetadata preloadedMatches = DiffLoader.preloadDiffMatches(newMatchesDatabase);

        setDescription("Adding Diff to workspace...");
        workspace.addDiff(newMatchesDatabase, preloadedMatches, false);
      } else {
        logger.atSevere().log(
            "Create and add new Diff to workspace failed. Diff binary does not exist.");
        CMessageBox.showError(
            parentWindow, "Adding new Diff to workspace failed. Diff binary does not exist.");
      }
    } catch (SQLException e) {
      logger.atSevere().withCause(e).log("New Diff failed. Couldn't read matches database.");
      CMessageBox.showError(
          parentWindow, "New Diff failed. Couldn't read matches database: " + e.getMessage());
    }
  }
}
