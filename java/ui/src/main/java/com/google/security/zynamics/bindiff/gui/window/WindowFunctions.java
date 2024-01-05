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

package com.google.security.zynamics.bindiff.gui.window;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.Config;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;
import javax.swing.JTabbedPane;

/** Holder class for the actual controller class TabPanelManager. */
public class WindowFunctions {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final TabPanelManager tabPanelManager;

  private final MainWindow window;

  public WindowFunctions(MainWindow window, Workspace workspace) {
    checkNotNull(window);
    checkNotNull(workspace);

    tabPanelManager = new TabPanelManager(window, workspace);

    var workspaceTab = new WorkspaceTabPanel(window, workspace);
    tabPanelManager.addTab(workspaceTab);

    this.window = window;
  }

  private void saveConfigFile() {
    BinDiffConfig config = BinDiffConfig.getInstance();
    GeneralSettingsConfigItem settings = config.getMainSettings();

    Point location = window.getLocation();
    settings.setWindowXPos((int) location.getX());
    settings.setWindowYPos((int) location.getY());
    settings.setWindowWidth(window.getWidth());
    settings.setWindowHeight(window.getHeight());
    settings.setWindowStateWasMaximized(window.getExtendedState() == Frame.MAXIMIZED_BOTH);
    settings.setWorkspaceTreeDividerPosition(
        tabPanelManager.getWorkspaceTabPanel().getDividerLocation());
    settings.setScreenWidth(Toolkit.getDefaultToolkit().getScreenSize().width);
    settings.setScreenHeight(Toolkit.getDefaultToolkit().getScreenSize().height);

    try {
      BinDiffConfig.getInstance().write();
    } catch (IOException e) {
      logger.atSevere().withCause(e).log("Couldn't save configuration file");
      CMessageBox.showError(window, "Couldn't save configuration file.");
    }
  }

  public boolean askDisassemblerDirectoryIfUnset() {
    if (!Config.getInstance().getIda().getDirectory().isEmpty()) {
      return true;
    }
    CMessageBox.showWarning(
        window, "Please set the path to your IDA Pro installation in the main settings.");
    WorkspaceTabPanelFunctions controller =
        window.getController().getTabPanelManager().getWorkspaceTabPanel().getController();
    return controller.showMainSettingsDialog();
  }

  public void exitBinDiff() {
    WorkspaceTabPanelFunctions workspaceController =
        tabPanelManager.getWorkspaceTabPanel().getController();
    if (workspaceController.closeWorkspace()) {
      workspaceController.closeDialogs();

      saveConfigFile();

      logger.atInfo().log("BinDiff closed normally");
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
