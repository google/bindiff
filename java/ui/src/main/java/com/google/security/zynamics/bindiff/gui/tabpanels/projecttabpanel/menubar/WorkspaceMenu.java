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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.menubar;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.min;

import com.google.protobuf.ProtocolStringList;
import com.google.security.zynamics.bindiff.BinDiff;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.UiPreferences.HistoryOptions;
import com.google.security.zynamics.bindiff.config.Config;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.WorkspaceListener;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.system.SystemHelpers;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 * The workspace menu that is visible under "File" initially and when managing workspaces in the
 * main window.
 */
public class WorkspaceMenu extends JMenu implements WorkspaceListener {

  public static final int MAX_RECENT = 4;

  private final WorkspaceTabPanelFunctions controller;

  private JMenuItem newWorkspaceMenuItem;
  private JMenuItem loadWorkspaceMenuItem;
  private JMenuItem closeWorkspaceMenuItem;
  private JMenuItem exitMenuItem; // Note: macOS uses a QuitHandler instead

  public WorkspaceMenu(WorkspaceTabPanelFunctions controller) {
    super("File");
    this.controller = checkNotNull(controller);
    setMnemonic('F');

    initItems();

    controller.getWorkspace().addListener(this);
  }

  private ProtocolStringList getRecentWorkspaces() {
    return Config.getInstance()
        .getPreferencesBuilder()
        .getHistoryBuilder()
        .getRecentWorkspaceList();
  }

  private void addRecentWorkspaces() {
    boolean first = true;
    for (String workspace : getRecentWorkspaces()) {
      if (workspace.isEmpty()) {
        continue;
      }

      if (first) {
        add(new JSeparator());
        first = false;
      }

      add(
          GuiUtils.buildMenuItem(
              minimizeWorkspacePath(workspace),
              e -> {
                controller.closeWorkspace();
                controller.loadWorkspace(workspace);
              }));
    }
  }

  private void initItems() {
    int ctrlDownMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    newWorkspaceMenuItem =
        GuiUtils.buildMenuItem(
            "New Workspace...",
            'N',
            KeyEvent.VK_N,
            ctrlDownMask | InputEvent.SHIFT_DOWN_MASK,
            e -> controller.newWorkspace());

    loadWorkspaceMenuItem =
        GuiUtils.buildMenuItem(
            "Open Workspace...",
            'O',
            KeyEvent.VK_O,
            ctrlDownMask,
            e -> controller.loadWorkspace());

    closeWorkspaceMenuItem =
        GuiUtils.buildMenuItem(
            "Close Workspace",
            'W',
            KeyEvent.VK_W,
            ctrlDownMask,
            e -> controller.closeWorkspace());
    closeWorkspaceMenuItem.setEnabled(false); // Initial state: no workspace loaded

    if (!SystemHelpers.isRunningMacOSX() || !BinDiff.isDesktopIntegrationDone()) {
      exitMenuItem =
          GuiUtils.buildMenuItem(
              "Exit", 'Q', KeyEvent.VK_Q, ctrlDownMask, e -> controller.exitBinDiff());
    }
    addSubmenuEntries();
  }

  private void addSubmenuEntries() {
    add(newWorkspaceMenuItem);
    add(loadWorkspaceMenuItem);
    add(closeWorkspaceMenuItem);

    addRecentWorkspaces();

    if (exitMenuItem != null) {
      add(new JSeparator());
      add(exitMenuItem);
    }
  }

  private String minimizeWorkspacePath(String workspacePath) {
    // TODO(cblichmann): While workspacePath longer than 25 characters,
    // delete characters from the mid-most path component
    // and replace with '...'. If not possible, delete
    // characters from filename.

    var workspaceFile = new File(workspacePath);
    String parentPath = workspaceFile.getParent();

    var minimizedPath = new StringBuilder(workspaceFile.getPath());
    if (parentPath.length() > 25) {
      minimizedPath = new StringBuilder(parentPath.substring(0, 3));
      minimizedPath.append("...");
      minimizedPath.append(parentPath, parentPath.length() - 20, parentPath.length());
      minimizedPath.append(File.separator);
      minimizedPath.append(workspaceFile.getName());
    }
    return minimizedPath.toString();
  }

  public void dispose() {
    controller.getWorkspace().removeListener(this);
  }

  @Override
  public void closedWorkspace() {
    closeWorkspaceMenuItem.setEnabled(false);
  }

  @Override
  public void loadedWorkspace(Workspace workspace) {
    closeWorkspaceMenuItem.setEnabled(true);

    String path = workspace.getWorkspaceFilePath();
    if (path.isEmpty()) {
      return;
    }

    HistoryOptions.Builder history =
        Config.getInstance().getPreferencesBuilder().getHistoryBuilder();
    ProtocolStringList recentWorkspace = history.getRecentWorkspaceList();
    if (!recentWorkspace.contains(path)) {
      var workspaces = new ArrayList<>(recentWorkspace);
      workspaces.add(path);
      history.clearRecentWorkspace();
      history.addAllRecentWorkspace(workspaces.subList(0, min(MAX_RECENT, workspaces.size())));

      removeAll();
      addSubmenuEntries();
    }
  }
}
