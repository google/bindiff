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

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.detachedviewstabpanel.FunctionDiffViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.WorkspaceAdapter;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** This class represents the BinDiff main window. */
public class MainWindow extends JFrame {

  private final WindowFunctions controller;

  private String titlePath;

  public MainWindow(final Workspace workspace) {
    checkNotNull(workspace);

    workspace.addListener(
        new WorkspaceAdapter() {
          @Override
          public void closedWorkspace() {
            setTitle("");
          }

          @Override
          public void loadedWorkspace(Workspace workspace) {
            titlePath = workspace.getWorkspaceFilePath();
            setTitle(titlePath);
            updateEllipsis();
          }
        });

    controller = new WindowFunctions(this, workspace);

    initWindow();

    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            // This grab focus ensures that the diff description is saved.
            final WorkspaceTabPanelFunctions workspaceController =
                controller.getTabPanelManager().getWorkspaceTabPanel().getController();
            workspaceController.getWorkspaceTree().grabFocus();

            // Do not remove invokeLater, otherwise BinDiff exits before grabFocus() has been
            // executed.
            SwingUtilities.invokeLater(controller::exitBinDiff);
          }
        });

    // Add a resize listener so we can add path ellipses to the window
    // title if necessary
    addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentResized(final ComponentEvent e) {
            updateEllipsis();
          }
        });

    workspace.setParentWindow(this);
  }

  private static boolean screenResolutionChanged() {
    final GeneralSettingsConfigItem settings = BinDiffConfig.getInstance().getMainSettings();
    final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    settings.setScreenWidth(screen.width);
    settings.setScreenHeight(screen.height);
    return settings.getScreenWidth() != screen.width || settings.getScreenHeight() != screen.height;
  }

  private void initWindow() {
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    GuiUtils.setWindowIcons(
        this,
        Constants.APP_ICON_PATH_16X16,
        Constants.APP_ICON_PATH_32X32,
        Constants.APP_ICON_PATH_48X48);

    final GeneralSettingsConfigItem settings = BinDiffConfig.getInstance().getMainSettings();
    final boolean wasMaximized = settings.getWindowStateWasMaximized();
    if (!wasMaximized && !screenResolutionChanged()) {
      final int winX = settings.getWindowXPos();
      final int winY = settings.getWindowYPos();
      final int winWidth = settings.getWindowWidth();
      final int winHeight = settings.getWindowHeight();

      setLocation(winX, winY);
      setSize(winWidth, winHeight);
      setPreferredSize(new Dimension(winWidth, winHeight));
    } else {
      setLocation(100, 100);
      setSize(800, 600);
      setPreferredSize(new Dimension(800, 600));

      setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    add(controller.getTabbedPanel(), BorderLayout.CENTER);

    pack();
  }

  private void updateEllipsis() {
    if (titlePath == null || getGraphics() == null) {
      return;
    }
    final FontMetrics fm = getGraphics().getFontMetrics();
    int maxLen = titlePath.length();
    final Insets in = getInsets();
    // Calculate default title with that also takes 100px window
    // decorations and border widths into account
    final int defTitleW =
        in.right - in.left + 100 + fm.stringWidth(" - " + Constants.DEFAULT_WINDOW_TITLE);
    String newValue = titlePath;
    while (maxLen >= 12 && fm.stringWidth(newValue) > getWidth() - defTitleW) {
      newValue = FileUtils.getPathEllipsis(titlePath, maxLen);
      maxLen--;
    }
    setTitle(newValue);
  }

  public WindowFunctions getController() {
    return controller;
  }

  @Override
  public void setTitle(final String text) {
    super.setTitle((!text.isEmpty() ? text + " - " : "") + Constants.DEFAULT_WINDOW_TITLE);
  }

  public void updateTitle(final Workspace workspace, final TabPanel tabPanel) {
    titlePath = null;
    if (tabPanel instanceof WorkspaceTabPanel) {
      if (workspace == null || !workspace.isLoaded()) {
        setTitle("");
      } else {
        setTitle(workspace.getWorkspaceFilePath());
        titlePath = workspace.getWorkspaceFilePath();
        updateEllipsis();
      }
    } else if (tabPanel instanceof FunctionDiffViewTabPanel) {
      final String title = tabPanel.getTitle();

      setTitle(title);
    } else if (tabPanel instanceof ViewTabPanel) {
      final String title =
          String.format(
              "%s - %s",
              tabPanel.getTitle(),
              ((ViewTabPanel) tabPanel).getView().getGraphs().getDiff().getDiffName());

      setTitle(title);
    }
  }
}
