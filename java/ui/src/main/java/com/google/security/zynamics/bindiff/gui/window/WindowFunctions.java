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

package com.google.security.zynamics.bindiff.gui.window;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.Config;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.menubar.WorkspaceMenuBar;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JTabbedPane;

/** Holder class for the actual controller class TabPanelManager. */
public class WindowFunctions {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final TabPanelManager tabPanelManager;

  private final MainWindow window;

  public WindowFunctions(final MainWindow window, final Workspace workspace) {
    checkNotNull(window);
    checkNotNull(workspace);

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
      logger.at(Level.SEVERE).withCause(e).log("Couldn't save configuration file");
      CMessageBox.showError(window, "Couldn't save configuration file.");
    }
  }

  public boolean askDisassemblerDirectoryIfUnset() {
    if (!Config.getInstance().getIda().getDirectory().isEmpty()) {
      return true;
    }
    CMessageBox.showWarning(
        window, "Please set the path to your IDA Pro installation in the main settings.");
    final WorkspaceTabPanelFunctions controller =
        window.getController().getTabPanelManager().getWorkspaceTabPanel().getController();
    return controller.showMainSettingsDialog();
  }

  public void exitBinDiff() {
    final WorkspaceTabPanelFunctions workspaceController =
        tabPanelManager.getWorkspaceTabPanel().getController();
    if (workspaceController.closeWorkspace()) {
      workspaceController.closeDialogs();

      saveConfigFile();

      logger.at(Level.INFO).log("BinDiff closed normally");
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
