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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.menubar;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.BinDiff;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.WorkspaceListener;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.system.SystemHelpers;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 * The workspace menu that is visible under "File" initially and when managing workspaces in the
 * main window.
 */
public final class WorkspaceMenu extends JMenu implements WorkspaceListener {

  public static final int MAX_RECENT = 4;

  private final List<String> recentWorkspaces = new ArrayList<>(MAX_RECENT);

  private final WorkspaceTabPanelFunctions controller;

  private JMenuItem newWorkspaceMenuItem;
  private JMenuItem loadWorkspaceMenuItem;
  private JMenuItem closeWorkspaceMenuItem;
  private JMenuItem exitMenuItem; // Note: macOS uses a QuitHandler instead

  public WorkspaceMenu(final WorkspaceTabPanelFunctions controller) {
    super("File");
    this.controller = checkNotNull(controller);
    setMnemonic('F');

    loadRecentWorkspacesFromConfig();

    initItems();

    controller.getWorkspace().addListener(this);

    enableSubmenus(false);
  }

  private void addRecentWorkspaces() {
    boolean first = true;
    for (final String workspace : recentWorkspaces) {
      if (workspace.isEmpty()) {
        continue;
      }

      if (first) {
        add(new JSeparator());
        first = false;
      }

      final JMenuItem loadRecentMenuItem =
          GuiUtils.buildMenuItem(
              minimizeWorkspacePath(workspace),
              new AbstractAction() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                  controller.closeWorkspace();
                  controller.loadWorkspace(workspace);
                }
              });
      add(loadRecentMenuItem);
    }
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

  private void initItems() {
    final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    newWorkspaceMenuItem =
        GuiUtils.buildMenuItem(
            "New Workspace...",
            'N',
            KeyEvent.VK_N,
            CTRL_MASK | KeyEvent.SHIFT_MASK,
            new AbstractAction() {
              @Override
              public void actionPerformed(final ActionEvent e) {
                controller.newWorkspace();
              }
            });

    loadWorkspaceMenuItem =
        GuiUtils.buildMenuItem(
            "Open Workspace...",
            'O',
            KeyEvent.VK_O,
            CTRL_MASK,
            new AbstractAction() {
              @Override
              public void actionPerformed(final ActionEvent e) {
                controller.loadWorkspace();
              }
            });

    closeWorkspaceMenuItem =
        GuiUtils.buildMenuItem(
            "Close Workspace",
            'W',
            KeyEvent.VK_W,
            CTRL_MASK,
            new AbstractAction() {
              @Override
              public void actionPerformed(final ActionEvent e) {
                controller.closeWorkspace();
              }
            });

    if (!SystemHelpers.isRunningMacOSX() && BinDiff.isDesktopIntegrationDone()) {
      exitMenuItem =
          GuiUtils.buildMenuItem(
              "Exit",
              'Q',
              KeyEvent.VK_Q,
              CTRL_MASK,
              new AbstractAction() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                  controller.exitBinDiff();
                }
              });
    }
    addSubmenuEntries();
  }

  private void loadRecentWorkspacesFromConfig() {
    final GeneralSettingsConfigItem mainSettings = BinDiffConfig.getInstance().getMainSettings();
    for (final String dir : mainSettings.getRecentWorkspaceDirectories()) {
      if (new File(dir).isFile()) {
        recentWorkspaces.add(dir);
      }
    }
  }

  private String minimizeWorkspacePath(final String workspacePath) {
    // TODO(cblichmann): While workspacePath longer than 25 characters,
    // delete characters from the mid-most path component
    // and replace with '...'. If not possible, delete
    // characters from filename.

    final File workspaceFile = new File(workspacePath);
    final String parentPath = workspaceFile.getParent();

    StringBuilder minimizedPath = new StringBuilder(workspaceFile.getPath());
    if (parentPath.length() > 25) {
      minimizedPath = new StringBuilder(parentPath.substring(0, 3));
      minimizedPath.append("...");
      minimizedPath.append(parentPath, parentPath.length() - 20, parentPath.length());
      minimizedPath.append(File.separator);
      minimizedPath.append(workspaceFile.getName());
    }
    return minimizedPath.toString();
  }

  private void updateWorkspaceMenu() {
    removeAll();
    addSubmenuEntries();
  }

  public void dispose() {
    controller.getWorkspace().removeListener(this);
  }

  public void enableSubmenus(final boolean enable) {
    closeWorkspaceMenuItem.setEnabled(enable);
  }

  public String[] getRecentWorkspaces() {
    return recentWorkspaces.toArray(new String[0]);
  }

  @Override
  public void closedWorkspace() {
    enableSubmenus(false);
  }

  @Override
  public void loadedWorkspace(final Workspace workspace) {
    enableSubmenus(true);

    final String path = workspace.getWorkspaceFilePath();
    if (path.isEmpty()) {
      return;
    }

    final int idx = recentWorkspaces.indexOf(path);
    if (idx >= 0) {
      Collections.swap(recentWorkspaces, 0, idx);
    } else {
      if (!recentWorkspaces.isEmpty()) {
        recentWorkspaces.remove(recentWorkspaces.size() - 1);
      }
      recentWorkspaces.add(0, path);
    }

    updateWorkspaceMenu();
  }
}
