package com.google.security.zynamics.bindiff.gui.window;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.menubar.WorkspaceMenuBar;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTabbedPane;

/** Holder class for the actual controller class TabPanelManager. */
public class WindowFunctions {
  private final TabPanelManager tabPanelManager;

  private final MainWindow window;

  public WindowFunctions(final MainWindow window, final Workspace workspace) {
    Preconditions.checkNotNull(window);
    Preconditions.checkNotNull(workspace);

    tabPanelManager = new TabPanelManager(window, workspace);

    final WorkspaceTabPanel workspaceTab = new WorkspaceTabPanel(window, workspace);
    tabPanelManager.addTab(workspaceTab);

    this.window = window;
  }

  private void saveConfigFile() {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    final GeneralSettingsConfigItem settings = config.getMainSettings();

    final Point location = window.getLocation();
    settings.setWindowXPos((int) location.getX());
    settings.setWindowYPos((int) location.getY());
    settings.setWindowWidth(window.getWidth());
    settings.setWindowHeight(window.getHeight());
    settings.setWindowStateWasMaximized(window.getExtendedState() == Frame.MAXIMIZED_BOTH);
    settings.setWorkspaceTreeDividerPosition(
        tabPanelManager.getWorkspaceTabPanel().getDividerLocation());
    settings.setScreenWidth(Toolkit.getDefaultToolkit().getScreenSize().width);
    settings.setScreenHeight(Toolkit.getDefaultToolkit().getScreenSize().height);

    final WorkspaceMenuBar menuBar =
        (WorkspaceMenuBar) tabPanelManager.getWorkspaceTabPanel().getMenuBar();

    List<String> recents = new ArrayList<>();
    for (final String recent : menuBar.getRecentWorkspaces()) {
      if (new File(recent).isFile()) {
        recents.add(recent);
      }
    }
    settings.setRecentWorkspaceDirectories(recents);

    try {
      BinDiffConfig.getInstance().write();
    } catch (final IOException e) {
      Logger.logException(e, "Couldn't save configuration file.");
      CMessageBox.showError(window, "Couldn't save configuration file.");
    }
  }

  public void exitBinDiff() {
    final WorkspaceTabPanelFunctions workspaceController =
        tabPanelManager.getWorkspaceTabPanel().getController();
    if (workspaceController.closeWorkspace()) {
      workspaceController.closeDialogs();

      saveConfigFile();

      Logger.logInfo("BinDiff closed normally.");
    }

    System.exit(0);
  }

  public JTabbedPane getTabbedPanel() {
    return tabPanelManager.getTabbedPane();
  }

  public TabPanelManager getTabPanelManager() {
    return tabPanelManager;
  }
}
