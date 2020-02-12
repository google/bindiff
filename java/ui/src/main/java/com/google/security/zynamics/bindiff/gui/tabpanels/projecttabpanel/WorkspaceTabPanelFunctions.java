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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel;

import com.google.common.base.Preconditions;
import com.google.common.flogger.FluentLogger;
import com.google.common.io.ByteStreams;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.database.MatchesDatabase;
import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.gui.dialogs.AddDiffDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.NewDiffDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.NewWorkspaceDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.ProgressDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.directorydiff.DiffPairTableData;
import com.google.security.zynamics.bindiff.gui.dialogs.directorydiff.DirectoryDiffDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.InitialCallGraphSettingsDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.InitialFlowGraphSettingsDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.mainsettings.MainSettingsDialog;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.detachedviewstabpanel.FunctionDiffViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.implementations.DirectoryDiffImplementation;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.implementations.NewDiffImplementation;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.WorkspaceTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.AbstractTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.AllFunctionDiffViewsNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.FunctionDiffViewsNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.io.matches.DiffRequestMessage;
import com.google.security.zynamics.bindiff.project.IWorkspaceListener;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.WorkspaceLoader;
import com.google.security.zynamics.bindiff.project.diff.CallGraphViewLoader;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffLoader;
import com.google.security.zynamics.bindiff.project.diff.FlowGraphViewLoader;
import com.google.security.zynamics.bindiff.project.diff.FunctionDiffViewLoader;
import com.google.security.zynamics.bindiff.project.diff.IDiffListener;
import com.google.security.zynamics.bindiff.project.matches.DiffMetadata;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.userview.CallGraphViewData;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import com.google.security.zynamics.bindiff.utils.SystemUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.Triple;
import com.google.security.zynamics.zylib.gui.CFileChooser;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CUnlimitedProgressDialog;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

public final class WorkspaceTabPanelFunctions extends TabPanelFunctions {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private MainSettingsDialog mainSettingsDialog = null;
  private InitialCallGraphSettingsDialog callGraphSettingsDialog = null;
  private InitialFlowGraphSettingsDialog flowGraphSettingsDialog = null;

  private WorkspaceTree workspaceTree;

  public WorkspaceTabPanelFunctions(final MainWindow window, final Workspace workspace) {
    super(window, workspace);
  }

  private boolean closeViews(
      final List<ViewTabPanel> viewsToSave, final List<ViewTabPanel> viewsToClose) {
    for (final ViewTabPanel viewPanel : viewsToSave) {
      viewPanel
          .getController()
          .getTabPanelManager()
          .getTabbedPane()
          .setSelectedComponent(viewPanel);

      final Diff diff = viewPanel.getDiff();
      final int answer =
          CMessageBox.showYesNoCancelQuestion(
              getMainWindow(),
              String.format(
                  "Save %s '%s'?",
                  diff.isFunctionDiff() ? "Function Diff View" : "Diff View",
                  viewPanel.getView().getViewName()));

      switch (answer) {
        case JOptionPane.YES_OPTION:
          {
            if (viewPanel.getController().closeView(true)) {
              break;
            }
            return false;
          }
        case JOptionPane.NO_OPTION:
          viewsToClose.add(viewPanel);
          break;
        default:
          return false;
      }
    }

    for (final ViewTabPanel view : viewsToClose) {
      view.getController().closeView(false);
    }

    return true;
  }

  private File copyFileIntoNewDiffDir(final File newDiffDir, final File toCopy) throws IOException {
    final String newFilePath =
        String.format("%s%s%s", newDiffDir, File.separator, toCopy.getName());
    final File newFile = new File(newFilePath);

    ByteStreams.copy(new FileInputStream(toCopy), new FileOutputStream(newFile));
    return newFile;
  }

  private boolean deleteDiff(final Diff diff, final boolean deleteFromDisk) {
    // Remove diff from workspace only
    removeDiffFromWorkspace(diff);

    if (deleteFromDisk) {
      try {
        if (!diff.isFunctionDiff()) {
          BinDiffFileUtils.deleteDirectory(new File(diff.getDiffFolder()));
        } else if (!deleteFunctionDiff(diff)) {
          CMessageBox.showError(
              getMainWindow(),
              String.format("Couldn't delete '%s'.", diff.getMatchesDatabase().toString()));
        }
      } catch (final IOException e) {
        logger.at(Level.SEVERE).withCause(e).log(
            "Delete diff failed. Couldn't delete diff folder.");
        CMessageBox.showError(getMainWindow(), "Delete diff failed. Couldn't delete diff folder.");

        return false;
      }
    }

    return true;
  }

  private boolean deleteFunctionDiff(final Diff diffToDelete) {
    if (diffToDelete.getMatchesDatabase().delete()) {
      final File parentFolder = diffToDelete.getMatchesDatabase().getParentFile();
      final File priBinExortFile = diffToDelete.getExportFile(ESide.PRIMARY);
      final File secBinExportFile = diffToDelete.getExportFile(ESide.SECONDARY);

      boolean deletePriExport = true;
      boolean deleteSecExport = true;
      for (final Diff diff : getWorkspace().getDiffList(true)) {
        if (parentFolder.equals(diff.getMatchesDatabase().getParentFile())) {
          if (diff.getExportFile(ESide.PRIMARY).equals(priBinExortFile)) {
            deletePriExport = false;
          }

          if (diff.getExportFile(ESide.SECONDARY).equals(secBinExportFile)) {
            deleteSecExport = false;
          }
        }
      }

      if (deletePriExport) {
        if (!priBinExortFile.delete()) {
          return false;
        }
      }
      if (deleteSecExport) {
        if (!secBinExportFile.delete()) {
          return false;
        }
      }

      if (parentFolder.listFiles().length == 0) {
        final AllFunctionDiffViewsNode containerNode =
            (AllFunctionDiffViewsNode) workspaceTree.getModel().getRoot().getChildAt(0);

        int removeIndex = -1;
        for (int index = 0; index < containerNode.getChildCount(); ++index) {
          final FunctionDiffViewsNode child =
              (FunctionDiffViewsNode) containerNode.getChildAt(index);
          if (child.getViewDirectory().equals(parentFolder)) {
            removeIndex = index;
            child.delete();
            containerNode.remove(index);
            workspaceTree.updateTree();
          }
        }

        if (removeIndex == containerNode.getChildCount()) {
          --removeIndex;
        }
        if (removeIndex > -1) {
          final FunctionDiffViewsNode toSelect =
              (FunctionDiffViewsNode) containerNode.getChildAt(removeIndex);
          final TreePath toSelectPath = new TreePath(toSelect.getPath());
          workspaceTree.expandPath(toSelectPath);
          workspaceTree.setSelectionPath(toSelectPath);
        }

        return parentFolder.delete();
      }

      return true;
    }

    return false;
  }

  private MainWindow getParentWindow() {
    return (MainWindow) SwingUtilities.getWindowAncestor(workspaceTree);
  }

  private WorkspaceTabPanel getWorkspaceTabPanel() {
    return getMainWindow().getController().getTabPanelManager().getWorkspaceTabPanel();
  }

  private boolean isImportThunkView(
      final Diff diff,
      final IAddress primaryFunctionAddr,
      final IAddress secondaryFunctionAddr,
      final boolean infoMsg) {
    final RawFunction priFunction =
        diff.getCallGraph(ESide.PRIMARY).getFunction(primaryFunctionAddr);
    final RawFunction secFunction =
        diff.getCallGraph(ESide.SECONDARY).getFunction(secondaryFunctionAddr);

    if ((priFunction != null
            && secFunction == null
            && priFunction.getFunctionType() == EFunctionType.IMPORTED)
        || (secFunction != null
            && priFunction == null
            && secFunction.getFunctionType() == EFunctionType.IMPORTED)) {
      if (infoMsg) {
        CMessageBox.showInformation(
            getMainWindow(),
            "Cannot open unmatched import thunk view since it doesn't contain any code.");
      }
      return true;
    }

    if (priFunction != null
        && priFunction.getFunctionType() == EFunctionType.IMPORTED
        && secFunction != null
        && secFunction.getFunctionType() == EFunctionType.IMPORTED) {
      if (infoMsg) {
        CMessageBox.showInformation(
            getMainWindow(),
            "Cannot open matched import thunk view since it doesn't contain any code.");
      }
      return true;
    }

    return false;
  }

  private void loadWorkspace(final File workspaceFile, final boolean showProgressDialog) {
    try {
      if (getWorkspace().isLoaded()) {
        getWorkspace().closeWorkspace();
      }

      final Workspace workspace = getWorkspace();
      final WorkspaceLoader loader = new WorkspaceLoader(workspaceFile, workspace);

      if (showProgressDialog) {
        ProgressDialog.show(
            getMainWindow(),
            String.format("Loading Workspace '%s'", workspaceFile.getName()),
            loader);
      } else {
        loader.loadMetaData();
      }

      final String errorMsg = loader.getErrorMessage();
      if (!"".equals(errorMsg)) {
        logger.at(Level.SEVERE).log(errorMsg);
        CMessageBox.showError(getMainWindow(), errorMsg);
      } else {
        getWorkspace().saveWorkspace();
      }
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      logger.at(Level.SEVERE).withCause(e).log("Load workspace failed. %s", e.getMessage());
      CMessageBox.showError(
          getMainWindow(), String.format("Load workspace failed. %s", e.getMessage()));
    }
  }

  private void removeDiffFromWorkspace(final Diff diff) {
    assert diff != null;

    final Set<Diff> diffSet = new HashSet<>();
    diffSet.add(diff);
    getWorkspace().getDiffList().remove(diff);
    closeDiffs(diffSet);

    diff.removeDiff();

    for (final IWorkspaceListener listener : getWorkspace().getListeners()) {
      listener.removedDiff(diff);
    }

    try {
      getWorkspace().saveWorkspace();
    } catch (final SQLException e) {
      logger.at(Level.SEVERE).withCause(e).log("Couldn't delete temporary files");
      CMessageBox.showError(getMainWindow(), "Couldn't delete temporary files: " + e.getMessage());
    }
  }

  public void addDiff() {
    try {
      final AddDiffDialog dlg = new AddDiffDialog(getParentWindow(), getWorkspace());

      if (dlg.getAddButtonPressed()) {
        final File matchesDatabase = dlg.getMatchesBinary();

        final File priBinExportFile = dlg.getBinExportBinary(ESide.PRIMARY);
        final File secBinExportFile = dlg.getBinExportBinary(ESide.SECONDARY);

        final File diffDir = dlg.getDestinationDirectory();

        File newMatchesDatabase = matchesDatabase;
        if (!diffDir.equals(matchesDatabase.getParentFile())) {
          diffDir.mkdir();

          newMatchesDatabase = copyFileIntoNewDiffDir(diffDir, matchesDatabase);

          copyFileIntoNewDiffDir(diffDir, priBinExportFile);
          copyFileIntoNewDiffDir(diffDir, secBinExportFile);
        }

        final DiffMetadata matchesMetadata = DiffLoader.preloadDiffMatches(newMatchesDatabase);

        getWorkspace().addDiff(newMatchesDatabase, matchesMetadata, false);
      }
    } catch (final IOException | SQLException e) {
      logger.at(Level.SEVERE).withCause(e).log("Add diff failed. Couldn't add diff to workspace");
      CMessageBox.showError(
          getMainWindow(), "Add diff failed. Couldn't add diff to workspace: " + e.getMessage());
    }
  }

  public void closeDialogs() {
    if (mainSettingsDialog != null) {
      mainSettingsDialog.dispose();
    }
    if (flowGraphSettingsDialog != null) {
      flowGraphSettingsDialog.dispose();
    }
    if (callGraphSettingsDialog != null) {
      callGraphSettingsDialog.dispose();
    }
  }

  public boolean closeDiffs(final Set<Diff> diffs) {
    final List<ViewTabPanel> viewsToSave = new ArrayList<>();
    final List<ViewTabPanel> viewsToClose = new ArrayList<>();

    for (final ViewTabPanel viewPanel : getOpenViews(diffs)) {
      if (viewPanel.getController().hasChanged()) {
        if (viewPanel.getDiff().isFunctionDiff()) {
          viewsToSave.add(0, viewPanel);
        } else {
          viewsToSave.add(viewPanel);
        }
      } else {
        viewsToClose.add(viewPanel);
      }
    }

    if (closeViews(viewsToSave, viewsToClose)) {
      for (final Diff diff : diffs) {
        diff.closeDiff();
      }

      return true;
    }

    return false;
  }

  public void closeViews(final Set<ViewTabPanel> views) {
    final List<ViewTabPanel> viewsToSave = new ArrayList<>();
    final List<ViewTabPanel> viewsToClose = new ArrayList<>();

    for (final ViewTabPanel viewPanel : views) {
      if (viewPanel.getController().hasChanged()) {
        if (viewPanel.getDiff().isFunctionDiff()) {
          viewsToSave.add(0, viewPanel);
        } else {
          viewsToSave.add(viewPanel);
        }
      } else {
        viewsToClose.add(viewPanel);
      }
    }

    closeViews(viewsToSave, viewsToClose);
  }

  public boolean closeWorkspace() {
    final Set<Diff> diffsToClose = new HashSet<>();
    diffsToClose.addAll(getWorkspace().getDiffList());

    if (!closeDiffs(diffsToClose)) {
      return false;
    }

    getWorkspace().closeWorkspace();

    return true;
  }

  public boolean deleteDiff(final Diff diff) {
    final Diff diffRef = diff == null ? getSelectedDiff() : diff;

    final Pair<Integer, Boolean> answer =
        CMessageBox.showYesNoQuestionWithCheckbox(
            getParentWindow(),
            String.format("Are you sure you want to remove '%s'?\n\n", diffRef.getDiffName()),
            "Also delete diff contents on disk?");

    if (answer.first() != JOptionPane.YES_OPTION) {
      return false;
    }

    return deleteDiff(diffRef, answer.second());
  }

  public boolean deleteFunctionDiffs(final List<Diff> diffs) {
    if (diffs.isEmpty()) {
      return false;
    }

    final StringBuilder msg = new StringBuilder();
    msg.append("Are you sure you want to delete this function diff views from disk?\n\n");

    int index = 0;
    for (final Diff diff : diffs) {
      if (index != 0) {
        msg.append("\n");
      }
      msg.append(String.format("'%s'", diff.getDiffName()));

      if (index++ == 4 && diffs.size() > 5) {
        msg.append("\n...");
        break;
      }
    }

    final int answer = CMessageBox.showYesNoQuestion(getParentWindow(), msg.toString());
    if (answer == JOptionPane.YES_OPTION) {
      boolean success = true;
      for (final Diff diff : diffs) {
        final boolean t = deleteDiff(diff, true);
        if (success) {
          success = t;
        }
      }

      return success;
    }

    return false;
  }

  public void directoryDiff() {
    // create a new view
    final MainWindow window = getMainWindow();
    final Workspace workspace = getWorkspace();

    final String workspacePath = workspace.getWorkspaceDir().getPath();
    final DirectoryDiffDialog dlg = new DirectoryDiffDialog(window, new File(workspacePath));

    if (dlg.getDiffButtonPressed()) {
      final String priSourceBasePath = dlg.getSourceBasePath(ESide.PRIMARY);
      final String secSourceBasePath = dlg.getSourceBasePath(ESide.SECONDARY);
      final List<DiffPairTableData> selectedIdbPairs = dlg.getSelectedIdbPairs();

      final DirectoryDiffImplementation directoryDiffer =
          new DirectoryDiffImplementation(
              window, workspace, priSourceBasePath, secSourceBasePath, selectedIdbPairs);
      try {
        ProgressDialog.show(window, "Directory Diffing...", directoryDiffer);
      } catch (final Exception e) {
        logger.at(Level.SEVERE).withCause(e).log(
            "Directory diffing was canceled because of an unexpected exception");
        CMessageBox.showError(
            window, "Directory diffing was canceled because of an unexpected exception!");
      }

      if (directoryDiffer.getDiffingErrorMessages().size() != 0) {
        int counter = 0;
        final StringBuilder errorText = new StringBuilder();
        for (final String msg : directoryDiffer.getDiffingErrorMessages()) {
          if (++counter == 10) {
            errorText.append("...");
            break;
          }
          errorText.append(msg).append("\n");
        }

        CMessageBox.showError(window, errorText.toString());
      }

      if (directoryDiffer.getOpeningDiffErrorMessages().size() != 0) {
        int counter = 0;
        final StringBuilder errorText = new StringBuilder();
        for (final String msg : directoryDiffer.getOpeningDiffErrorMessages()) {
          if (++counter == 10) {
            errorText.append("...");
            break;
          }
          errorText.append(msg).append("\n");
        }

        CMessageBox.showError(window, errorText.toString());
      }
    }
  }

  public LinkedHashSet<ViewTabPanel> getOpenViews(final Set<Diff> diffs) {
    final LinkedHashSet<ViewTabPanel> openViews = new LinkedHashSet<>();

    final MainWindow window = getMainWindow();
    final List<ViewTabPanel> tabPanels =
        new ArrayList<>(window.getController().getTabPanelManager().getViewTabPanels());
    for (final ViewTabPanel viewPanel : tabPanels) {
      // Only close views that belong to current diff
      final Diff diff = viewPanel.getDiff();
      if (!diffs.contains(diff)) {
        continue;
      }

      openViews.add(viewPanel);
    }
    return openViews;
  }

  public Diff getSelectedDiff() {
    final TreePath treePath = getWorkspaceTree().getSelectionModel().getSelectionPath();
    final AbstractTreeNode node = (AbstractTreeNode) treePath.getLastPathComponent();

    return node.getDiff();
  }

  public WorkspaceTree getWorkspaceTree() {
    return workspaceTree;
  }

  public void loadDefaultWorkspace() {
    final String workspacePath =
        BinDiffConfig.getInstance().getMainSettings().getDefaultWorkspace();
    if (workspacePath == null || "".equals(workspacePath)) {
      return;
    }

    final File workspaceFile = new File(workspacePath);

    if (workspaceFile.exists() && workspaceFile.canWrite()) {
      loadWorkspace(workspaceFile, false);
    }
  }

  public void loadDiff(Diff diff) {
    if (diff == null) {
      diff = getSelectedDiff();
    }

    if (diff.isLoaded()) {
      return;
    }

    final LinkedHashSet<Diff> diffs = new LinkedHashSet<>();
    diffs.add(diff);
    final DiffLoader diffLoader = new DiffLoader(diffs);

    final CUnlimitedProgressDialog progressDialog =
        new CUnlimitedProgressDialog(
            getParentWindow(),
            Constants.DEFAULT_WINDOW_TITLE,
            String.format("Loading '%s'", diff.getDiffName()),
            diffLoader);

    diffLoader.setProgressDescriptionTarget(progressDialog);

    progressDialog.setVisible(true);

    final Exception e = progressDialog.getException();
    if (e != null) {
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
      CMessageBox.showError(getMainWindow(), e.getMessage());
    }
  }

  public void loadFunctionDiffs() {
    final LinkedHashSet<Diff> diffsToLoad = new LinkedHashSet<>();
    for (final Diff diff : getWorkspace().getDiffList(true)) {
      if (!diff.isLoaded()) {
        diffsToLoad.add(diff);
      }
    }

    loadFunctionDiffs(diffsToLoad);
  }

  public void loadFunctionDiffs(final LinkedHashSet<Diff> diffsToLoad) {
    final DiffLoader diffLoader = new DiffLoader(diffsToLoad);

    final CUnlimitedProgressDialog progressDialog =
        new CUnlimitedProgressDialog(
            getParentWindow(),
            Constants.DEFAULT_WINDOW_TITLE,
            "Loading Function Diffs",
            diffLoader);

    diffLoader.setProgressDescriptionTarget(progressDialog);
    progressDialog.setVisible(true);

    final Exception e = progressDialog.getException();
    if (e != null) {
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
      CMessageBox.showError(getMainWindow(), e.getMessage());
    }
  }

  public void loadWorkspace() {
    final String workingDirPath =
        BinDiffConfig.getInstance().getMainSettings().getWorkspaceDirectory();

    if ("".equals(workingDirPath)) {
      BinDiffConfig.getInstance()
          .getMainSettings()
          .setWorkspaceDirectory(SystemUtils.getCurrentUserPersonalFolder());
    }

    final File workingDir =
        new File(BinDiffConfig.getInstance().getMainSettings().getWorkspaceDirectory());

    final CFileChooser openFileDlg =
        new CFileChooser(Constants.BINDIFF_WORKSPACEFILE_EXTENSION, "BinDiff Workspace");
    openFileDlg.setDialogTitle("Open Workspace");
    openFileDlg.setApproveButtonText("Open");
    openFileDlg.setCheckBox("Use as default workspace");

    if (workingDir.exists()) {
      openFileDlg.setCurrentDirectory(workingDir);
    }

    if (JFileChooser.APPROVE_OPTION == openFileDlg.showOpenDialog(getMainWindow())) {
      final File workspaceFile = openFileDlg.getSelectedFile();

      loadWorkspace(workspaceFile, true);

      // TODO(cblichmann): Ensure that a new default workspace can only be set after it has been
      //                   loaded successfully.
      // Set default workspace?
      if (openFileDlg.isSelectedCheckBox()) {
        BinDiffConfig.getInstance()
            .getMainSettings()
            .setDefaultWorkspace(workspaceFile.getAbsolutePath());
      }
    }
  }

  public void loadWorkspace(final String path) {
    final File workspaceDir = new File(path);

    if (!workspaceDir.exists()) {
      final String msg = "Load workspace failed. Workspace folder does not exist.";
      logger.at(Level.SEVERE).log(msg);
      CMessageBox.showError(getMainWindow(), msg);
      return;
    }

    loadWorkspace(workspaceDir, true);
  }

  public void newDiff() {
    final MainWindow window = getMainWindow();
    final Workspace workspace = getWorkspace();
    final String workspacePath = workspace.getWorkspaceDir().getPath();

    final NewDiffDialog dlg = new NewDiffDialog(window, new File(workspacePath));

    if (dlg.getDiffButtonPressed()) {
      final File priIDBFile = dlg.getIdb(ESide.PRIMARY);
      final File secIDBFile = dlg.getIdb(ESide.SECONDARY);
      final File priCallGraphFile = dlg.getBinExportBinary(ESide.PRIMARY);
      final File secCallGraphFile = dlg.getBinExportBinary(ESide.SECONDARY);
      final File destinationFile = dlg.getDestinationDirectory();

      final NewDiffImplementation newDiffThread =
          new NewDiffImplementation(
              window,
              workspace,
              priIDBFile,
              secIDBFile,
              priCallGraphFile,
              secCallGraphFile,
              destinationFile);

      try {
        ProgressDialog.show(
            getMainWindow(),
            String.format("New single Diff '%s'", destinationFile.getName()),
            newDiffThread);
      } catch (final Exception e) {
        // FIXME: Never catch all exceptions!
        logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
        CMessageBox.showError(getMainWindow(), "Unknown error while diffing.");
      }
    }
  }

  public void newWorkspace() {
    final NewWorkspaceDialog workspaceDlg =
        new NewWorkspaceDialog(getParentWindow(), "New Workspace");
    workspaceDlg.setVisible(true);

    if (!workspaceDlg.isOkPressed()) {
      return;
    }

    if (getWorkspace().isLoaded() && !closeWorkspace()) {
      return;
    }

    final String workspacePath = FileUtils.ensureTrailingSlash(workspaceDlg.getWorkspacePath());
    final File workspaceDir = new File(workspacePath);

    if (!workspaceDir.exists()) {
      workspaceDir.mkdir();
    }

    final File workspaceFile =
        new File(
            String.format(
                "%s%s.%s",
                workspacePath,
                workspaceDlg.getWorkspaceName(),
                Constants.BINDIFF_WORKSPACEFILE_EXTENSION));
    try {
      getWorkspace().newWorkspace(workspaceFile);
      if (workspaceDlg.isDefaultWorkspace()) {
        BinDiffConfig.getInstance()
            .getMainSettings()
            .setDefaultWorkspace(workspaceFile.getAbsolutePath());
      }
    } catch (final IOException | SQLException e) {
      logger.at(Level.SEVERE).withCause(e).log();
      CMessageBox.showError(getMainWindow(), e.getMessage());
    }
  }

  public void openCallGraphDiffView(final DiffRequestMessage data) {
    final MainWindow window = getMainWindow();
    final TabPanelManager tabPanelManager = window.getController().getTabPanelManager();

    // Create a new view
    final CallGraphViewLoader loader =
        new CallGraphViewLoader(data, window, tabPanelManager, getWorkspace());

    try {
      ProgressDialog.show(getMainWindow(), "Loading call graph diff", loader);
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      logger.at(Level.SEVERE).withCause(e).log(
          "Open call graph view failed. Couldn't create graph.");
      CMessageBox.showError(getMainWindow(), "Open call graph view failed. Couldn't create graph.");
    }
  }

  public void openCallGraphView(final MainWindow window, final Diff diff) {
    try {
      final TabPanelManager tabPanelManager = window.getController().getTabPanelManager();

      if (diff.getViewManager().containsView(null, null)) {
        // view is already open
        tabPanelManager.selectTabPanel(null, null, diff);
      } else {
        // Create a new view
        CallGraphViewLoader loader =
            new CallGraphViewLoader(diff, getMainWindow(), tabPanelManager, getWorkspace());

        ProgressDialog.show(
            getMainWindow(), String.format("Loading call graph '%s'", diff.getDiffName()), loader);

        for (final IDiffListener diffListener : diff.getListener()) {
          diffListener.loadedView(diff);
        }
      }
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      logger.at(Level.SEVERE).withCause(e).log(
          "Open call graph view failed. Couldn't create graph.");
      CMessageBox.showError(getMainWindow(), "Open call graph view failed. Couldn't create graph.");
    }
  }

  public void openFlowGraphView(
      final MainWindow window,
      final Diff diff,
      final IAddress primaryFunctionAddr,
      final IAddress secondaryFunctionAddr) {
    if (isImportThunkView(diff, primaryFunctionAddr, secondaryFunctionAddr, true)) {
      return;
    }

    final TabPanelManager tabPanelMgr = window.getController().getTabPanelManager();

    if (diff.getViewManager().containsView(primaryFunctionAddr, secondaryFunctionAddr)) {
      // normal view is already open
      tabPanelMgr.selectTabPanel(primaryFunctionAddr, secondaryFunctionAddr, diff);
      return;
    }

    try {
      // create a new view
      final LinkedHashSet<Triple<Diff, IAddress, IAddress>> viewAddrs = new LinkedHashSet<>();
      viewAddrs.add(Triple.make(diff, primaryFunctionAddr, secondaryFunctionAddr));

      final FlowGraphViewLoader loader =
          new FlowGraphViewLoader(getMainWindow(), tabPanelMgr, getWorkspace(), viewAddrs);

      ProgressDialog.show(
          getMainWindow(), String.format("Loading flow graph '%s'", diff.getDiffName()), loader);

      for (final IDiffListener diffListener : diff.getListener()) {
        diffListener.loadedView(diff);
      }
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      logger.at(Level.SEVERE).withCause(e).log(
          "Open flow graph view failed. Couldn't create graph.");
      CMessageBox.showError(getMainWindow(), "Open flow graph view failed. Couldn't create graph.");
    }
  }

  public void openFlowGraphViews(
      final MainWindow window,
      final LinkedHashSet<Triple<Diff, IAddress, IAddress>> viewsAddresses) {
    final TabPanelManager tabPanelMgr = window.getController().getTabPanelManager();

    final LinkedHashSet<Triple<Diff, IAddress, IAddress>> viewsAddrsToOpen = new LinkedHashSet<>();

    int importedCounter = 0;
    for (final Triple<Diff, IAddress, IAddress> viewAddrs : viewsAddresses) {
      final Diff diff = viewAddrs.first();
      final IAddress priAddr = viewAddrs.second();
      final IAddress secAddr = viewAddrs.third();
      if (!diff.getViewManager().containsView(priAddr, secAddr)) {
        if (isImportThunkView(diff, priAddr, secAddr, false)) {
          ++importedCounter;
        } else {
          viewsAddrsToOpen.add(viewAddrs);
        }
      }
    }

    if (importedCounter > 0) {
      CMessageBox.showInformation(
          getParentWindow(),
          String.format(
              "%d import thunk views have not been opened since they do not contain any code.",
              importedCounter));

      if (viewsAddrsToOpen.size() == 0) {
        return;
      }
    }

    try {
      // create a new view
      final FlowGraphViewLoader loader =
          new FlowGraphViewLoader(getMainWindow(), tabPanelMgr, getWorkspace(), viewsAddrsToOpen);

      ProgressDialog.show(getMainWindow(), "Loading flow graph views", loader);

      final Set<Diff> diffSet = new HashSet<>();
      for (final Triple<Diff, IAddress, IAddress> entry : viewsAddresses) {
        final Diff diff = entry.first();
        if (!diffSet.contains(diff)) {
          diffSet.add(diff);
          for (final IDiffListener diffListener : diff.getListener()) {
            diffListener.loadedView(diff);
          }
        }
      }
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      logger.at(Level.SEVERE).withCause(e).log(
          "Open flow graph view failed. Couldn't create graph.");
      CMessageBox.showError(getMainWindow(), "Open flow graph view failed. Couldn't create graph.");
    }
  }

  public void openFunctionDiffView(final DiffRequestMessage data) {
    try {
      final MainWindow window = getMainWindow();
      final TabPanelManager tabPanelManager = window.getController().getTabPanelManager();

      // create a new view or select the tab which contains the already opened view
      final FunctionDiffViewLoader loader =
          new FunctionDiffViewLoader(data, window, tabPanelManager, getWorkspace());
      ProgressDialog.show(window, "Loading function diff", loader);

      if (data.getDiff() != null) {
        for (final IDiffListener diffListener : data.getDiff().getListener()) {
          diffListener.loadedView(data.getDiff());
        }
      }
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      logger.at(Level.SEVERE).withCause(e).log(
          "Open function diff view failed. Couldn't create graph.");
      CMessageBox.showError(
          getMainWindow(), "Open function diff view failed. Couldn't create graph.");
    }
  }

  public void openFunctionDiffView(final MainWindow window, final Diff diff) {
    Preconditions.checkArgument(diff.isFunctionDiff());

    final IAddress priFunctionAddr =
        diff.getCallGraph(ESide.PRIMARY).getNodes().get(0).getAddress();
    final IAddress secFunctionAddr =
        diff.getCallGraph(ESide.SECONDARY).getNodes().get(0).getAddress();
    if (isImportThunkView(diff, priFunctionAddr, secFunctionAddr, true)) {
      return;
    }

    // This cannot work because openFunctionDiffView(final FunctionDiffSocketXmlData data)
    // creates currently always a new Diff object.
    final TabPanelManager tabPanelMgr = window.getController().getTabPanelManager();
    for (final TabPanel tabPanel : tabPanelMgr) {
      if (tabPanel instanceof FunctionDiffViewTabPanel) {
        final FunctionDiffViewTabPanel functionDiffTabPanel = (FunctionDiffViewTabPanel) tabPanel;
        final Diff curDiff = functionDiffTabPanel.getView().getGraphs().getDiff();

        if (curDiff == diff) {
          tabPanelMgr.getTabbedPane().setSelectedComponent(tabPanel);

          return;
        }
      }
    }

    final DiffRequestMessage socketData = new DiffRequestMessage(diff);
    socketData.setBinExportPath(diff.getExportFile(ESide.PRIMARY).getPath(), ESide.PRIMARY);
    socketData.setBinExportPath(diff.getExportFile(ESide.SECONDARY).getPath(), ESide.SECONDARY);
    socketData.setMatchesDBPath(diff.getMatchesDatabase().getPath());

    openFunctionDiffView(socketData);
  }

  public boolean saveDescription(final Diff diff, final String description) {
    try (final MatchesDatabase matchesDb = new MatchesDatabase(diff.getMatchesDatabase())) {
      matchesDb.saveDiffDescription(description);
      return true;
    } catch (final SQLException e) {
      logger.at(Level.SEVERE).withCause(e).log("Database error. Couldn't save diff description.");
      CMessageBox.showError(
          getMainWindow(), "Database error. Couldn't save diff description: " + e.getMessage());
    }
    return false;
  }

  public void setTreeNodeContextComponent(final Component component) {
    if (component == null) {
      return;
    }

    final JPanel treeNodeCtxContainer = getWorkspaceTabPanel().getTreeNodeContextContainer();
    treeNodeCtxContainer.removeAll();
    treeNodeCtxContainer.add(component, BorderLayout.CENTER);
    treeNodeCtxContainer.updateUI();
  }

  public void setWorkspaceTree(final WorkspaceTree workspaceTree) {
    this.workspaceTree = workspaceTree;
  }

  public void showInCallGraph(final Diff diff, final Set<Pair<IAddress, IAddress>> viewAddrPairs) {
    if (!diff.getViewManager().containsView(null, null)) {
      openCallGraphView(getMainWindow(), diff);
    } else {
      final TabPanelManager tabPanelMgr = getMainWindow().getController().getTabPanelManager();
      tabPanelMgr.selectTabPanel(null, null, diff);
    }

    final List<CombinedDiffNode> nodesToSelect = new ArrayList<>();
    final List<CombinedDiffNode> nodesToUnselect = new ArrayList<>();

    final CallGraphViewData viewData = diff.getViewManager().getCallGraphViewData(diff);
    if (viewData != null) {
      final CombinedGraph combinedGraph = viewData.getGraphs().getCombinedGraph();

      for (final CombinedDiffNode node : combinedGraph.getNodes()) {
        final RawCombinedFunction function = (RawCombinedFunction) node.getRawNode();

        final IAddress priAddr = function.getAddress(ESide.PRIMARY);
        final IAddress secAddr = function.getAddress(ESide.SECONDARY);
        if (viewAddrPairs.contains(Pair.make(priAddr, secAddr))) {
          nodesToSelect.add(node);
        } else {
          nodesToUnselect.add(node);
        }
      }

      combinedGraph.selectNodes(nodesToSelect, nodesToUnselect);
    }
  }

  public void showInitialCallGraphSettingsDialog() {
    if (callGraphSettingsDialog == null) {
      callGraphSettingsDialog = new InitialCallGraphSettingsDialog(getMainWindow());
    }
    callGraphSettingsDialog.setVisible(true);
  }

  public void showInitialFlowGraphSettingsDialog() {
    if (flowGraphSettingsDialog == null) {
      flowGraphSettingsDialog = new InitialFlowGraphSettingsDialog(getMainWindow());
    }
    flowGraphSettingsDialog.setVisible(true);
  }

  public void showMainSettingsDialog() {
    if (mainSettingsDialog == null) {
      mainSettingsDialog = new MainSettingsDialog(getMainWindow());
    }
    mainSettingsDialog.setVisible(true);
  }
}
