package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.menubar;

import com.google.security.zynamics.bindiff.gui.tabpanels.menubar.HelpMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class WorkspaceMenuBar extends JMenuBar {
  private final JMenu workspaceMenu;
  private final JMenu diffMenu;
  private final JMenu settingsMenu;
  private final JMenu aboutMenu;

  public WorkspaceMenuBar(final WorkspaceTabPanelFunctions controller) {
    workspaceMenu = new WorkspaceMenu(controller);
    diffMenu = new DiffMenu(controller);
    settingsMenu = new SettingsMenu(controller);
    aboutMenu = new HelpMenu(controller);

    add(workspaceMenu);
    add(diffMenu);
    add(settingsMenu);
    add(aboutMenu);
  }

  public String[] getRecentWorkspaces() {
    return ((WorkspaceMenu) workspaceMenu).getRecentWorkspaces();
  }
}
