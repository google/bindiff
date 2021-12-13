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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel;

import static com.google.common.base.Preconditions.checkArgument;

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
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.WorkspaceListener;
import com.google.security.zynamics.bindiff.project.WorkspaceLoader;
import com.google.security.zynamics.bindiff.project.diff.CallGraphViewLoader;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffListener;
import com.google.security.zynamics.bindiff.project.diff.DiffLoader;
import com.google.security.zynamics.bindiff.project.diff.FlowGraphViewLoader;
import com.google.security.zynamics.bindiff.project.diff.FunctionDiffViewLoader;
import com.google.security.zynamics.bindiff.project.matches.DiffMetadata;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.userview.CallGraphViewData;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.Triple;
import com.google.security.zynamics.zylib.gui.CFileChooser;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CUnlimitedProgressDialog;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.system.SystemHelpers;
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

  public WorkspaceTabPanelFunctions(MainWindow window, Workspace workspace) {
    super(window, workspace);
  }

  private boolean closeViews(List<ViewTabPanel> viewsToSave, List<ViewTabPanel> viewsToClose) {
    for (ViewTabPanel viewPanel : viewsToSave) {
      viewPanel
          .getController()
          .getTabPanelManager()
          .getTabbedPane()
          .setSelectedComponent(viewPanel);

      Diff diff = viewPanel.getDiff();
      int answer =
          CMessageBox.showYesNoCancelQuestion(
              getMainWindow(),
              String.format(
                  "Save %s '%s'?",
                  diff.isFunctionDiff() ? "Function Diff View" : "Diff View",
                  viewPanel.getView().getViewName()));

      switch (answer) {
        case JOptionPane.YES_OPTION:
          if (viewPanel.getController().closeView(true)) {
            break;
          }
          return false;
        case JOptionPane.NO_OPTION:
          viewsToClose.add(viewPanel);
          break;
        default:
          return false;
      }
    }

    for (ViewTabPanel view : viewsToClose) {
      view.getController().closeView(false);
    }

    return true;
  }

  public void closeViews(Set<ViewTabPanel> views) {
    List<ViewTabPanel> viewsToSave = new ArrayList<>();
    List<ViewTabPanel> viewsToClose = new ArrayList<>();

    for (ViewTabPanel viewPanel : views) {
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

  private File copyFileIntoNewDiffDir(File newDiffDir, File toCopy) throws IOException {
    var newFilePath = String.format("%s%s%s", newDiffDir, File.separator, toCopy.getName());
    var newFile = new File(newFilePath);

    ByteStreams.copy(new FileInputStream(toCopy), new FileOutputStream(newFile));
    return newFile;
  }

  private boolean deleteDiff(Diff diff, boolean deleteFromDisk) {
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
      } catch (IOException e) {
        logger.atSevere().withCause(e).log("Delete diff failed. Couldn't delete diff folder.");
        CMessageBox.showError(getMainWindow(), "Delete diff failed. Couldn't delete diff folder.");

        return false;
      }
    }

    return true;
  }

  public boolean deleteDiff(Diff diff) {
    Diff diffRef = diff == null ? getSelectedDiff() : diff;

    Pair<Integer, Boolean> answer =
        CMessageBox.showYesNoQuestionWithCheckbox(
            getParentWindow(),
            String.format("Are you sure you want to remove '%s'?\n\n", diffRef.getDiffName()),
            "Also delete diff contents on disk?");

    if (answer.first() != JOptionPane.YES_OPTION) {
      return false;
    }

    return deleteDiff(diffRef, answer.second());
  }

  private boolean deleteFunctionDiff(Diff diffToDelete) {
    if (!diffToDelete.getMatchesDatabase().delete()) {
      return false;
    }
    var parentFolder = diffToDelete.getMatchesDatabase().getParentFile();
    var priBinExportFile = diffToDelete.getExportFile(ESide.PRIMARY);
    var secBinExportFile = diffToDelete.getExportFile(ESide.SECONDARY);

    var deletePriExport = true;
    var deleteSecExport = true;
    for (Diff diff : getWorkspace().getDiffList(true)) {
        if (parentFolder.equals(diff.getMatchesDatabase().getParentFile())) {
          if (diff.getExportFile(ESide.PRIMARY).equals(priBinExportFile)) {
            deletePriExport = false;
          }
          if (diff.getExportFile(ESide.SECONDARY).equals(secBinExportFile)) {
            deleteSecExport = false;
          }
        }
      }

    if (deletePriExport && !priBinExportFile.delete()) {
          return false;
      }
    if (deleteSecExport && !secBinExportFile.delete()) {
          return false;
      }

    File[] files = parentFolder.listFiles();
      if (files != null && files.length == 0) {
      AllFunctionDiffViewsNode containerNode =
          (AllFunctionDiffViewsNode) workspaceTree.getModel().getRoot().getChildAt(0);

        int removeIndex = -1;
        for (int index = 0; index < containerNode.getChildCount(); ++index) {
        FunctionDiffViewsNode child = (FunctionDiffViewsNode) containerNode.getChildAt(index);
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
        var toSelect = (FunctionDiffViewsNode) containerNode.getChildAt(removeIndex);
        var toSelectPath = new TreePath(toSelect.getPath());
          workspaceTree.expandPath(toSelectPath);
          workspaceTree.setSelectionPath(toSelectPath);
        }

        return parentFolder.delete();
      }
      return true;
  }

  private MainWindow getParentWindow() {
    return (MainWindow) SwingUtilities.getWindowAncestor(workspaceTree);
  }

  private WorkspaceTabPanel getWorkspaceTabPanel() {
    return getMainWindow().getController().getTabPanelManager().getWorkspaceTabPanel();
  }

  private boolean isImportThunkView(
      Diff diff, IAddress primaryFunctionAddr, IAddress secondaryFunctionAddr, boolean infoMsg) {
    RawFunction priFunction = diff.getCallGraph(ESide.PRIMARY).getFunction(primaryFunctionAddr);
    RawFunction secFunction = diff.getCallGraph(ESide.SECONDARY).getFunction(secondaryFunctionAddr);

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

  private void loadWorkspace(File workspaceFile, boolean showProgressDialog) {
    try {
      if (getWorkspace().isLoaded()) {
        getWorkspace().closeWorkspace();
      }

      Workspace workspace = getWorkspace();
      var loader = new WorkspaceLoader(workspaceFile, workspace);

      if (showProgressDialog) {
        ProgressDialog.show(
            getMainWindow(),
            String.format("Loading Workspace '%s'", workspaceFile.getName()),
            loader);
      } else {
        loader.loadMetaData();
      }

      String errorMsg = loader.getErrorMessage();
      if (!"".equals(errorMsg)) {
        logger.atSevere().log("%s", errorMsg);
        CMessageBox.showError(getMainWindow(), errorMsg);
      } else {
        getWorkspace().saveWorkspace();
      }
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Load workspace failed. %s", e.getMessage());
      CMessageBox.showError(
          getMainWindow(), String.format("Load workspace failed. %s", e.getMessage()));
    }
  }

  public void loadWorkspace() {
    String workingDirPath = BinDiffConfig.getInstance().getMainSettings().getWorkspaceDirectory();

    if ("".equals(workingDirPath)) {
      BinDiffConfig.getInstance()
          .getMainSettings()
          .setWorkspaceDirectory(SystemHelpers.getUserDirectory());
    }

    var workingDir =
        new File(BinDiffConfig.getInstance().getMainSettings().getWorkspaceDirectory());

    var openFileDlg =
        new CFileChooser(Constants.BINDIFF_WORKSPACEFILE_EXTENSION, "BinDiff Workspace");
    openFileDlg.setDialogTitle("Open Workspace");
    openFileDlg.setApproveButtonText("Open");
    openFileDlg.setCheckBox("Use as default workspace");

    if (workingDir.exists()) {
      openFileDlg.setCurrentDirectory(workingDir);
    }

    if (JFileChooser.APPROVE_OPTION == openFileDlg.showOpenDialog(getMainWindow())) {
      File workspaceFile = openFileDlg.getSelectedFile();

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

  public void loadWorkspace(String path) {
    var workspaceDir = new File(path);

    if (!workspaceDir.exists()) {
      String msg = "Load workspace failed. Workspace folder does not exist.";
      logger.atSevere().log("%s", msg);
      CMessageBox.showError(getMainWindow(), msg);
      return;
    }

    loadWorkspace(workspaceDir, true);
  }

  private void removeDiffFromWorkspace(Diff diff) {
    assert diff != null;

    Set<Diff> diffSet = new HashSet<>();
    diffSet.add(diff);
    getWorkspace().getDiffList().remove(diff);
    closeDiffs(diffSet);

    diff.removeDiff();

    for (WorkspaceListener listener : getWorkspace().getListeners()) {
      listener.removedDiff(diff);
    }

    try {
      getWorkspace().saveWorkspace();
    } catch (SQLException e) {
      logger.atSevere().withCause(e).log("Couldn't delete temporary files");
      CMessageBox.showError(getMainWindow(), "Couldn't delete temporary files: " + e.getMessage());
    }
  }

  public void addDiff() {
    try {
      var addDiffDialog = new AddDiffDialog(getParentWindow(), getWorkspace());
      if (!addDiffDialog.getAddButtonPressed()) {
        return;
      }
      File matchesDatabase = addDiffDialog.getMatchesBinary();
      File priBinExportFile = addDiffDialog.getBinExportBinary(ESide.PRIMARY);
      File secBinExportFile = addDiffDialog.getBinExportBinary(ESide.SECONDARY);
      File diffDir = addDiffDialog.getDestinationDirectory();
      File newMatchesDatabase = matchesDatabase;

      if (!diffDir.equals(matchesDatabase.getParentFile())) {
        diffDir.mkdir();

        newMatchesDatabase = copyFileIntoNewDiffDir(diffDir, matchesDatabase);

        copyFileIntoNewDiffDir(diffDir, priBinExportFile);
        copyFileIntoNewDiffDir(diffDir, secBinExportFile);
      }

      DiffMetadata matchesMetadata = DiffLoader.preloadDiffMatches(newMatchesDatabase);

      getWorkspace().addDiff(newMatchesDatabase, matchesMetadata, false);
    } catch (IOException | SQLException e) {
      logger.atSevere().withCause(e).log("Add diff failed. Couldn't add diff to workspace");
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

  public boolean closeDiffs(Set<Diff> diffs) {
    List<ViewTabPanel> viewsToSave = new ArrayList<>();
    List<ViewTabPanel> viewsToClose = new ArrayList<>();

    for (ViewTabPanel viewPanel : getOpenViews(diffs)) {
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
      for (Diff diff : diffs) {
        diff.closeDiff();
      }
      return true;
    }

    return false;
  }

  public boolean closeWorkspace() {
    Set<Diff> diffsToClose = new HashSet<>(getWorkspace().getDiffList());

    if (!closeDiffs(diffsToClose)) {
      return false;
    }

    getWorkspace().closeWorkspace();
    return true;
  }

  public boolean deleteFunctionDiffs(List<Diff> diffs) {
    if (diffs.isEmpty()) {
      return false;
    }

    var msg = new StringBuilder();
    msg.append("Are you sure you want to delete this function diff views from disk?\n\n");

    int index = 0;
    for (Diff diff : diffs) {
      if (index != 0) {
        msg.append("\n");
      }
      msg.append(String.format("'%s'", diff.getDiffName()));

      if (index++ == 4 && diffs.size() > 5) {
        msg.append("\n...");
        break;
      }
    }

    int answer = CMessageBox.showYesNoQuestion(getParentWindow(), msg.toString());
    if (answer == JOptionPane.YES_OPTION) {
      boolean success = true;
      for (Diff diff : diffs) {
        boolean t = deleteDiff(diff, true);
        if (success) {
          success = t;
        }
      }

      return success;
    }

    return false;
  }

  public void directoryDiff() {
    // Create a new view
    MainWindow window = getMainWindow();
    Workspace workspace = getWorkspace();

    String workspacePath = workspace.getWorkspaceDir().getPath();
    var diffDialog = new DirectoryDiffDialog(window, new File(workspacePath));
    if (!diffDialog.getDiffButtonPressed()) {
      return;
    }

    String priSourceBasePath = diffDialog.getSourceBasePath(ESide.PRIMARY);
    String secSourceBasePath = diffDialog.getSourceBasePath(ESide.SECONDARY);
    List<DiffPairTableData> selectedIdbPairs = diffDialog.getSelectedIdbPairs();

    var directoryDiffer =
        new DirectoryDiffImplementation(
            window, workspace, priSourceBasePath, secSourceBasePath, selectedIdbPairs);
    try {
      ProgressDialog.show(window, "Directory Diffing...", directoryDiffer);
    } catch (Exception e) {
      logger.atSevere().withCause(e).log(
          "Directory diffing aborted because of an unexpected exception");
      CMessageBox.showError(
          window, "Directory diffing aborted because of an unexpected exception.");
    }

    if (!directoryDiffer.getDiffingErrorMessages().isEmpty()) {
      int counter = 0;
      var errorText = new StringBuilder();
      for (String msg : directoryDiffer.getDiffingErrorMessages()) {
        if (++counter == 10) {
          errorText.append("...");
          break;
        }
          errorText.append(msg).append("\n");
        }

        CMessageBox.showError(window, errorText.toString());
      }

    if (!directoryDiffer.getOpeningDiffErrorMessages().isEmpty()) {
        int counter = 0;
      var errorText = new StringBuilder();
      for (String msg : directoryDiffer.getOpeningDiffErrorMessages()) {
          if (++counter == 10) {
            errorText.append("...");
            break;
          }
          errorText.append(msg).append("\n");
        }

        CMessageBox.showError(window, errorText.toString());
      }
  }

  public LinkedHashSet<ViewTabPanel> getOpenViews(Set<Diff> diffs) {
    var openViews = new LinkedHashSet<ViewTabPanel>();

    MainWindow window = getMainWindow();
    for (ViewTabPanel viewPanel : window.getController().getTabPanelManager().getViewTabPanels()) {
      // Only close views that belong to current diff
      Diff diff = viewPanel.getDiff();
      if (!diffs.contains(diff)) {
        continue;
      }
      openViews.add(viewPanel);
    }
    return openViews;
  }

  public Diff getSelectedDiff() {
    TreePath treePath = getWorkspaceTree().getSelectionModel().getSelectionPath();
    AbstractTreeNode node = (AbstractTreeNode) treePath.getLastPathComponent();
    return node.getDiff();
  }

  public WorkspaceTree getWorkspaceTree() {
    return workspaceTree;
  }

  public void loadDefaultWorkspace() {
    String workspacePath = BinDiffConfig.getInstance().getMainSettings().getDefaultWorkspace();
    if (workspacePath == null || "".equals(workspacePath)) {
      return;
    }

    var workspaceFile = new File(workspacePath);
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

    var diffs = new LinkedHashSet<Diff>();
    diffs.add(diff);
    var diffLoader = new DiffLoader(diffs);

    var progressDialog =
        new CUnlimitedProgressDialog(
            getParentWindow(),
            Constants.DEFAULT_WINDOW_TITLE,
            String.format("Loading '%s'", diff.getDiffName()),
            diffLoader);

    diffLoader.setProgressDescriptionTarget(progressDialog);

    progressDialog.setVisible(true);

    Exception e = progressDialog.getException();
    if (e != null) {
      logger.atSevere().withCause(e).log("%s", e.getMessage());
      CMessageBox.showError(getMainWindow(), e.getMessage());
    }
  }

  public void loadFunctionDiffs() {
    var diffsToLoad = new LinkedHashSet<Diff>();
    for (Diff diff : getWorkspace().getDiffList(true)) {
      if (!diff.isLoaded()) {
        diffsToLoad.add(diff);
      }
    }
    loadFunctionDiffs(diffsToLoad);
  }

  public void loadFunctionDiffs(LinkedHashSet<Diff> diffsToLoad) {
    var diffLoader = new DiffLoader(diffsToLoad);
    var progressDialog =
        new CUnlimitedProgressDialog(
            getParentWindow(),
            Constants.DEFAULT_WINDOW_TITLE,
            "Loading Function Diffs",
            diffLoader);

    diffLoader.setProgressDescriptionTarget(progressDialog);
    progressDialog.setVisible(true);

    Exception e = progressDialog.getException();
    if (e != null) {
      logger.atSevere().withCause(e).log("%s", e.getMessage());
      CMessageBox.showError(getMainWindow(), e.getMessage());
    }
  }

  public void newDiff() {
    MainWindow window = getMainWindow();
    Workspace workspace = getWorkspace();
    String workspacePath = workspace.getWorkspaceDir().getPath();

    var dlg = new NewDiffDialog(window, new File(workspacePath));
    if (dlg.getDiffButtonPressed()) {
      File priIDBFile = dlg.getIdb(ESide.PRIMARY);
      File secIDBFile = dlg.getIdb(ESide.SECONDARY);
      File priCallGraphFile = dlg.getBinExportBinary(ESide.PRIMARY);
      File secCallGraphFile = dlg.getBinExportBinary(ESide.SECONDARY);
      File destinationFile = dlg.getDestinationDirectory();

      var newDiffThread =
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
      } catch (Exception e) {
        logger.atSevere().withCause(e).log("%s", e.getMessage());
        CMessageBox.showError(getMainWindow(), "Unknown error while diffing.");
      }
    }
  }

  public void newWorkspace() {
    var workspaceDlg = new NewWorkspaceDialog(getParentWindow(), "New Workspace");
    workspaceDlg.setVisible(true);
    if (!workspaceDlg.isOkPressed()) {
      return;
    }

    if (getWorkspace().isLoaded() && !closeWorkspace()) {
      return;
    }

    String workspacePath = FileUtils.ensureTrailingSlash(workspaceDlg.getWorkspacePath());
    var workspaceDir = new File(workspacePath);

    if (!workspaceDir.exists()) {
      workspaceDir.mkdir();
    }

    var workspaceFile =
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
    } catch (IOException | SQLException e) {
      logger.atSevere().withCause(e).log();
      CMessageBox.showError(getMainWindow(), e.getMessage());
    }
  }

  public void openCallGraphDiffView(DiffRequestMessage data) {
    MainWindow window = getMainWindow();
    TabPanelManager tabPanelManager = window.getController().getTabPanelManager();

    // Create a new view
    var loader = new CallGraphViewLoader(data, window, tabPanelManager, getWorkspace());

    try {
      ProgressDialog.show(getMainWindow(), "Loading call graph diff", loader);
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Open call graph view failed. Couldn't create graph.");
      CMessageBox.showError(getMainWindow(), "Open call graph view failed. Couldn't create graph.");
    }
  }

  public void openCallGraphView(MainWindow window, Diff diff) {
    try {
      TabPanelManager tabPanelManager = window.getController().getTabPanelManager();

      if (diff.getViewManager().containsView(null, null)) {
        // view is already open
        tabPanelManager.selectTabPanel(null, null, diff);
      } else {
        // Create a new view
        var loader =
            new CallGraphViewLoader(diff, getMainWindow(), tabPanelManager, getWorkspace());

        ProgressDialog.show(
            getMainWindow(), String.format("Loading call graph '%s'", diff.getDiffName()), loader);

        for (DiffListener diffListener : diff.getListener()) {
          diffListener.loadedView(diff);
        }
      }
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Open call graph view failed. Couldn't create graph.");
      CMessageBox.showError(getMainWindow(), "Open call graph view failed. Couldn't create graph.");
    }
  }

  public void openFlowGraphView(
      MainWindow window, Diff diff, IAddress primaryFunctionAddr, IAddress secondaryFunctionAddr) {
    if (isImportThunkView(diff, primaryFunctionAddr, secondaryFunctionAddr, true)) {
      return;
    }

    TabPanelManager tabPanelMgr = window.getController().getTabPanelManager();

    if (diff.getViewManager().containsView(primaryFunctionAddr, secondaryFunctionAddr)) {
      // normal view is already open
      tabPanelMgr.selectTabPanel(primaryFunctionAddr, secondaryFunctionAddr, diff);
      return;
    }

    try {
      // create a new view
      var viewAddrs = new LinkedHashSet<Triple<Diff, IAddress, IAddress>>();
      viewAddrs.add(Triple.make(diff, primaryFunctionAddr, secondaryFunctionAddr));

      var loader = new FlowGraphViewLoader(getMainWindow(), tabPanelMgr, getWorkspace(), viewAddrs);

      ProgressDialog.show(
          getMainWindow(), String.format("Loading flow graph '%s'", diff.getDiffName()), loader);

      for (DiffListener diffListener : diff.getListener()) {
        diffListener.loadedView(diff);
      }
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Open flow graph view failed. Couldn't create graph.");
      CMessageBox.showError(getMainWindow(), "Open flow graph view failed. Couldn't create graph.");
    }
  }

  public void openFlowGraphViews(
      MainWindow window, LinkedHashSet<Triple<Diff, IAddress, IAddress>> viewsAddresses) {
    TabPanelManager tabPanelMgr = window.getController().getTabPanelManager();

    var viewsAddrsToOpen = new LinkedHashSet<Triple<Diff, IAddress, IAddress>>();

    int importedCounter = 0;
    for (Triple<Diff, IAddress, IAddress> viewAddrs : viewsAddresses) {
      Diff diff = viewAddrs.first();
      IAddress priAddr = viewAddrs.second();
      IAddress secAddr = viewAddrs.third();
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
      // Create a new view
      var loader =
          new FlowGraphViewLoader(getMainWindow(), tabPanelMgr, getWorkspace(), viewsAddrsToOpen);

      ProgressDialog.show(getMainWindow(), "Loading flow graph views", loader);

      var diffSet = new HashSet<Diff>();
      for (Triple<Diff, IAddress, IAddress> entry : viewsAddresses) {
        Diff diff = entry.first();
        if (diffSet.add(diff)) {
          for (DiffListener diffListener : diff.getListener()) {
            diffListener.loadedView(diff);
          }
        }
      }
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Open flow graph view failed. Couldn't create graph.");
      CMessageBox.showError(getMainWindow(), "Open flow graph view failed. Couldn't create graph.");
    }
  }

  public void openFunctionDiffView(DiffRequestMessage data) {
    try {
      MainWindow window = getMainWindow();
      TabPanelManager tabPanelManager = window.getController().getTabPanelManager();

      // create a new view or select the tab which contains the already opened view
      var loader = new FunctionDiffViewLoader(data, window, tabPanelManager, getWorkspace());
      ProgressDialog.show(window, "Loading function diff", loader);

      if (data.getDiff() != null) {
        for (DiffListener diffListener : data.getDiff().getListener()) {
          diffListener.loadedView(data.getDiff());
        }
      }
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Open function diff view failed. Couldn't create graph.");
      CMessageBox.showError(
          getMainWindow(),
          "Open function diff view failed. Couldn't create graph: " + e.getMessage());
    }
  }

  public void openFunctionDiffView(MainWindow window, Diff diff) {
    checkArgument(diff.isFunctionDiff());

    IAddress priFunctionAddr = diff.getCallGraph(ESide.PRIMARY).getNodes().get(0).getAddress();
    IAddress secFunctionAddr = diff.getCallGraph(ESide.SECONDARY).getNodes().get(0).getAddress();
    if (isImportThunkView(diff, priFunctionAddr, secFunctionAddr, true)) {
      return;
    }

    // This cannot work because openFunctionDiffView(FunctionDiffSocketXmlData data)
    // creates currently always a new Diff object.
    TabPanelManager tabPanelMgr = window.getController().getTabPanelManager();
    for (TabPanel tabPanel : tabPanelMgr) {
      if (tabPanel instanceof FunctionDiffViewTabPanel) {
        FunctionDiffViewTabPanel functionDiffTabPanel = (FunctionDiffViewTabPanel) tabPanel;
        Diff curDiff = functionDiffTabPanel.getView().getGraphs().getDiff();

        if (curDiff == diff) {
          tabPanelMgr.getTabbedPane().setSelectedComponent(tabPanel);

          return;
        }
      }
    }

    var socketData = new DiffRequestMessage(diff);
    socketData.setBinExportPath(diff.getExportFile(ESide.PRIMARY).getPath(), ESide.PRIMARY);
    socketData.setBinExportPath(diff.getExportFile(ESide.SECONDARY).getPath(), ESide.SECONDARY);
    socketData.setMatchesDBPath(diff.getMatchesDatabase().getPath());

    openFunctionDiffView(socketData);
  }

  public boolean saveDescription(Diff diff, String description) {
    try (var matchesDb = new MatchesDatabase(diff.getMatchesDatabase())) {
      matchesDb.saveDiffDescription(description);
      return true;
    } catch (SQLException e) {
      logger.atSevere().withCause(e).log("Database error. Couldn't save diff description.");
      CMessageBox.showError(
          getMainWindow(), "Database error. Couldn't save diff description: " + e.getMessage());
    }
    return false;
  }

  public void setTreeNodeContextComponent(Component component) {
    if (component == null) {
      return;
    }

    JPanel treeNodeCtxContainer = getWorkspaceTabPanel().getTreeNodeContextContainer();
    treeNodeCtxContainer.removeAll();
    treeNodeCtxContainer.add(component, BorderLayout.CENTER);
    treeNodeCtxContainer.updateUI();
  }

  public void setWorkspaceTree(WorkspaceTree workspaceTree) {
    this.workspaceTree = workspaceTree;
  }

  public void showInCallGraph(Diff diff, Set<Pair<IAddress, IAddress>> viewAddrPairs) {
    if (!diff.getViewManager().containsView(null, null)) {
      openCallGraphView(getMainWindow(), diff);
    } else {
      TabPanelManager tabPanelMgr = getMainWindow().getController().getTabPanelManager();
      tabPanelMgr.selectTabPanel(null, null, diff);
    }

    var nodesToSelect = new ArrayList<CombinedDiffNode>();
    var nodesToUnselect = new ArrayList<CombinedDiffNode>();

    CallGraphViewData viewData = diff.getViewManager().getCallGraphViewData(diff);
    if (viewData != null) {
      CombinedGraph combinedGraph = viewData.getGraphs().getCombinedGraph();

      for (CombinedDiffNode node : combinedGraph.getNodes()) {
        RawCombinedFunction function = (RawCombinedFunction) node.getRawNode();

        IAddress priAddr = function.getAddress(ESide.PRIMARY);
        IAddress secAddr = function.getAddress(ESide.SECONDARY);
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

  public boolean showMainSettingsDialog() {
    if (mainSettingsDialog == null) {
      mainSettingsDialog = new MainSettingsDialog(getMainWindow());
    }
    mainSettingsDialog.setVisible(true);
    return mainSettingsDialog.isCancelled();
  }
}
