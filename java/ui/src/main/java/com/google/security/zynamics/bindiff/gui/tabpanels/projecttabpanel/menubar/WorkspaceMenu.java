package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.menubar;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.WorkspaceAdapter;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public final class WorkspaceMenu extends JMenu {
  public static final int MAX_RECENT = 4;

  private final List<String> recentWorkspaces = new ArrayList<>(MAX_RECENT);

  private final WorkspaceTabPanelFunctions controller;

  private final InternalWorkspaceListener workspaceModelListener = new InternalWorkspaceListener();

  private JMenuItem newWorkspace;
  private JMenuItem loadWorkspace;
  private JMenuItem closeWorkspace;
  private JMenuItem exit;

  public WorkspaceMenu(final WorkspaceTabPanelFunctions controller) {
    super("File");
    this.controller = Preconditions.checkNotNull(controller);

    setMnemonic('F');

    loadRecentWorkspacesFromConfig();

    initItems();

    controller.getWorkspace().addListener(workspaceModelListener);

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
    add(newWorkspace);
    add(loadWorkspace);
    add(closeWorkspace);

    addRecentWorkspaces();
    add(new JSeparator());

    add(exit);
  }

  private void initItems() {
    final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    newWorkspace =
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

    loadWorkspace =
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

    closeWorkspace =
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

    exit =
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

    addSubmenuEntries();
  }

  private void loadRecentWorkspacesFromConfig() {
    final GeneralSettingsConfigItem mainSettings =
        BinDiffConfig.getInstance().getMainSettings();

    recentWorkspaces.add(
        new File(mainSettings.getLastWorkspaceDirectory1()).exists()
            ? mainSettings.getLastWorkspaceDirectory1()
            : "");
    recentWorkspaces.add(
        new File(mainSettings.getLastWorkspaceDirectory2()).exists()
            ? mainSettings.getLastWorkspaceDirectory2()
            : "");
    recentWorkspaces.add(
        new File(mainSettings.getLastWorkspaceDirectory3()).exists()
            ? mainSettings.getLastWorkspaceDirectory3()
            : "");
    recentWorkspaces.add(
        new File(mainSettings.getLastWorkspaceDirectory4()).exists()
            ? mainSettings.getLastWorkspaceDirectory4()
            : "");
  }

  private String minimizeWorkspacePath(final String workspacePath) {
    // TODO(cblichmann): While workspacePath longer than 25 characters,
    // delete characters from the mid-most path component
    // and replace with '...'. If not possible, delete
    // characters from filename.

    final File workspaceFile = new File(workspacePath);
    final String parentPath = workspaceFile.getParent();

    String minimizedPath = workspaceFile.getPath();
    if (parentPath.length() > 25) {
      minimizedPath = parentPath.substring(0, 3);
      minimizedPath += "...";
      minimizedPath += parentPath.substring(parentPath.length() - 20);
      minimizedPath += File.separator;
      minimizedPath += workspaceFile.getName();
    }

    return minimizedPath;
  }

  private void updateworkspaceMenu() {
    removeAll();
    addSubmenuEntries();
  }

  public void dispose() {
    controller.getWorkspace().removeListener(workspaceModelListener);
  }

  public void enableSubmenus(final boolean enable) {
    closeWorkspace.setEnabled(enable);
  }

  public String[] getRecentWorkspaces() {
    return recentWorkspaces.toArray(new String[0]);
  }

  private class InternalWorkspaceListener extends WorkspaceAdapter {
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
        final String temp = recentWorkspaces.get(0);
        recentWorkspaces.set(0, recentWorkspaces.get(idx));
        recentWorkspaces.set(idx, temp);
      } else {
        recentWorkspaces.remove(MAX_RECENT - 1);
        recentWorkspaces.add(0, path);
      }

      updateworkspaceMenu();
    }
  }
}
