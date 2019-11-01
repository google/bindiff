package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.implementations;

import com.google.common.base.Preconditions;
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
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import com.google.security.zynamics.bindiff.utils.ExternalAppUtils;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

public final class NewDiffImplementation extends CEndlessHelperThread {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final MainWindow parentWindow;
  private final Workspace workspace;

  private File primaryBinExportFile;
  private File secondaryBinExportFile;

  private final File primaryIdbFile;
  private final File secondaryIdbFile;

  private final File destinationDirectory;

  public NewDiffImplementation(
      final MainWindow window,
      final Workspace workspace,
      final File primaryIdbFile,
      final File secondaryIdbFile,
      final File primaryBinExportFile,
      final File secondaryBinExportFile,
      final File destinationDir) {
    parentWindow = Preconditions.checkNotNull(window);
    this.workspace = Preconditions.checkNotNull(workspace);
    destinationDirectory = Preconditions.checkNotNull(destinationDir);

    this.primaryIdbFile = primaryIdbFile;
    this.secondaryIdbFile = secondaryIdbFile;

    this.primaryBinExportFile = primaryBinExportFile;
    this.secondaryBinExportFile = secondaryBinExportFile;
  }

  private String createUniqueExportFileName(final ESide side) {
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

  @Override
  protected void runExpensiveCommand() throws Exception {
    newDiff();
  }

  public String createNewDiff() {
    // TODO(cblichmann): Refactor copy-paste code into new methods

    try {
      setDescription("Creating destination directory...");

      if (!destinationDirectory.isDirectory() && !destinationDirectory.mkdirs()) {
        logger.at(Level.SEVERE).log("Could not create destination directory");
        CMessageBox.showError(parentWindow, "Could not create destination directory.");

        return null;
      }

      final String priTargetName = createUniqueExportFileName(ESide.PRIMARY);
      final String secTargetName = createUniqueExportFileName(ESide.SECONDARY);

      // Export primary side
      if (primaryIdbFile != null) {
        try {
          logger.at(Level.INFO).log(
              "- Exporting primary IDB '%s' to '%s'",
              primaryIdbFile.getPath(), destinationDirectory.getPath());

          setDescription("Exporting primary IDB...");

          final File idaExe = ExternalAppUtils.getIdaExe(primaryIdbFile);

          if (idaExe == null || !idaExe.canExecute()) {
            final String msg =
                "Can't start disassembler. Please set correct path in the main settings first.";
            logger.at(Level.SEVERE).log(msg);
            CMessageBox.showError(parentWindow, msg);
            return null;
          }

          ExportProcess.startExportProcess(
              idaExe, destinationDirectory, primaryIdbFile, priTargetName);
        } catch (final BinExportException e) {
          logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
          CMessageBox.showError(parentWindow, e.getMessage());

          try {
            BinDiffFileUtils.deleteDirectory(destinationDirectory);
          } catch (final IOException e2) {
            logger.at(Level.SEVERE).withCause(e2).log(
                "Couldn't delete diff folder '%s' after diffing failed",
                destinationDirectory.getPath());
            CMessageBox.showWarning(
                parentWindow,
                String.format(
                    "Couldn't delete diff folder '%s' after diffing failed.\n"
                        + "Please delete this folder manually.",
                    destinationDirectory.getPath()));
          }

          return null;
        }
      } else if (primaryBinExportFile != null) {
        try {
          final File copiedBinExportFile =
              new File(
                  String.format(
                      "%s%s%s", destinationDirectory.getPath(), File.separator, priTargetName));

          if (!copiedBinExportFile.exists()) {
            setDescription("Copying primary BinExport binary...");

            ByteStreams.copy(
                new FileInputStream(primaryBinExportFile),
                new FileOutputStream(copiedBinExportFile));
          }

          primaryBinExportFile = copiedBinExportFile;
        } catch (final IOException e) {
          logger.at(Level.SEVERE).withCause(e).log(
              "New Diff failed. Couldn't copy already exported primary binaries "
                  + "into the new diff directory.");
          CMessageBox.showError(
              parentWindow,
              "New Diff failed. Couldn't copy already exported primary binaries into the new diff "
                  + "directory.");

          try {
            BinDiffFileUtils.deleteDirectory(destinationDirectory);
          } catch (final IOException e2) {
            logger.at(Level.SEVERE).withCause(e2).log(
                "Couldn't delete diff folder '%s' after diffing failed",
                destinationDirectory.getPath());
            CMessageBox.showWarning(
                parentWindow,
                String.format(
                    "Couldn't delete diff folder '%s' after diffing failed.\n"
                        + "Please delete this folder manually.",
                    destinationDirectory.getPath()));
          }

          return null;
        }
      }

      // export or copy secondary side
      if (secondaryIdbFile != null) {
        try {
          logger.at(Level.INFO).log(
              "- Exporting secondary IDB '%s' to '%s'",
              secondaryIdbFile.getPath(), destinationDirectory.getPath());

          setDescription("Exporting secondary IDB...");

          final File idaExe = ExternalAppUtils.getIdaExe(secondaryIdbFile);

          if (idaExe == null || !idaExe.canExecute()) {
            final String msg =
                "Can't start disassembler. Please set correct path in the main settings first.";
            logger.at(Level.SEVERE).log(msg);
            CMessageBox.showError(parentWindow, msg);

            return null;
          }

          ExportProcess.startExportProcess(
              idaExe, destinationDirectory, secondaryIdbFile, secTargetName);
        } catch (final BinExportException e) {
          logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
          CMessageBox.showError(parentWindow, e.getMessage());

          try {
            BinDiffFileUtils.deleteDirectory(destinationDirectory);
          } catch (final IOException e2) {
            logger.at(Level.SEVERE).withCause(e2).log(
                "Couldn't delete diff folder '%s' after exporting failed",
                destinationDirectory.getPath());
            CMessageBox.showWarning(
                parentWindow,
                String.format(
                    "Couldn't delete diff folder '%s' after exporting failed.\n"
                        + "Please delete this folder manually.",
                    destinationDirectory.getPath()));
          }

          return null;
        }
      } else if (secondaryBinExportFile != null) {
        try {
          final File copiedSecBinExportFile =
              new File(
                  String.format(
                      "%s%s%s", destinationDirectory.getPath(), File.separator, secTargetName));

          if (!copiedSecBinExportFile.exists()) {
            setDescription("Copying primary BinExport binary...");
            ByteStreams.copy(
                new FileInputStream(secondaryBinExportFile),
                new FileOutputStream(copiedSecBinExportFile));
          }

          secondaryBinExportFile = copiedSecBinExportFile;
        } catch (final IOException e) {
          logger.at(Level.SEVERE).withCause(e).log(
              "New Diff failed. Couldn't copy secondary BinExport binaries into "
                  + "the new diff directory.");
          CMessageBox.showError(
              parentWindow,
              "New Diff failed. Couldn't copy secondary "
                  + "BinExport binaries into the new diff directory.");

          try {
            BinDiffFileUtils.deleteDirectory(destinationDirectory);
          } catch (final IOException e2) {
            logger.at(Level.SEVERE).withCause(e2).log(
                "Couldn't delete diff folder '%s' after exporting failed",
                destinationDirectory.getPath());
            CMessageBox.showWarning(
                parentWindow,
                String.format(
                    "Couldn't delete diff folder '%s' after exporting failed.\n"
                        + "Please delete this folder manually.",
                    destinationDirectory.getPath()));
          }

          return null;
        }
      }

      // diff
      try {
        final String engineExe = ExternalAppUtils.getCommandLineDiffer();

        String primaryDifferArgument = "";
        if (primaryIdbFile != null) {
          primaryDifferArgument =
              ExportProcess.getExportFilename(priTargetName, destinationDirectory);
        } else {
          primaryDifferArgument = primaryBinExportFile.getPath();
        }

        String secondaryDifferArgument = "";
        if (secondaryIdbFile != null) {
          secondaryDifferArgument =
              ExportProcess.getExportFilename(secTargetName, destinationDirectory);
        } else {
          secondaryDifferArgument = secondaryBinExportFile.getPath();
        }

        logger.at(Level.INFO).log("- Diffing '%s'", destinationDirectory.getName());

        setDescription("Diffing...");

        DiffProcess.startDiffProcess(
            engineExe, primaryDifferArgument, secondaryDifferArgument, destinationDirectory);

        final String diffBinaryPath =
            DiffProcess.getBinDiffFilename(primaryDifferArgument, secondaryDifferArgument);

        logger.at(Level.INFO).log("- Diffing done successfully");

        return diffBinaryPath;
      } catch (final DifferException e) {
        logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
        CMessageBox.showError(parentWindow, e.getMessage());
      }

      return null;
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      logger.at(Level.SEVERE).withCause(e).log("New Diff failed..");
      CMessageBox.showError(parentWindow, "New Diff failed.");
    }

    return null;
  }

  public void newDiff() {
    try {
      String matchedPath = createNewDiff();
      if (matchedPath == null) {
        return;
      }

      final File newMatchesDatabase = new File(matchedPath);

      if (newMatchesDatabase.exists()) {
        setDescription("Preloading Diff...");

        final DiffMetaData preloadedMatches = DiffLoader.preloadDiffMatches(newMatchesDatabase);

        setDescription("Adding Diff to workspace...");

        workspace.addDiff(newMatchesDatabase, preloadedMatches, false);
      } else {
        logger.at(Level.SEVERE).log(
            "Create and add new Diff to workspace failed. Diff binary does not exist.");
        CMessageBox.showError(
            parentWindow, "Adding new Diff to workspace failed. Diff binary does not exist.");
      }
    } catch (final SQLException e) {
      logger.at(Level.SEVERE).withCause(e).log("New Diff failed. Couldn't read matches database.");
      CMessageBox.showError(
          parentWindow, "New Diff failed. Couldn't read matches database: " + e.getMessage());
    }
  }
}
