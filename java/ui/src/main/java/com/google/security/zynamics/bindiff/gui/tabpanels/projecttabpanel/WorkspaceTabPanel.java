// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.menubar.WorkspaceMenuBar;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.WorkspaceTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.WorkspaceTreePanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.DefaultTreeNodeContextPanel;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class WorkspaceTabPanel extends TabPanel {
  private static final Icon ICON = ResourceUtils.getImageIcon("data/tabicons/workspace-tab.png");

  private final WorkspaceMenuBar menuBar;

  private final JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);

  private final JPanel treeNodeContextContainer = new JPanel(new BorderLayout());

  private final WorkspaceTreePanel workspaceTreePanel;

  private final WorkspaceTabPanelFunctions controller;

  public WorkspaceTabPanel(final MainWindow window, final Workspace workspace) {
    super();

    checkNotNull(workspace);

    controller = new WorkspaceTabPanelFunctions(checkNotNull(window), workspace);

    workspaceTreePanel = new WorkspaceTreePanel(controller);
    menuBar = new WorkspaceMenuBar(controller);

    window.setJMenuBar(menuBar);

    initPanel();

    controller.loadDefaultWorkspace();
  }

  private void initPanel() {
    setBorder(new LineBorder(Color.GRAY));
    splitPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

    treeNodeContextContainer.add(new DefaultTreeNodeContextPanel());

    splitPanel.setOneTouchExpandable(true);
    splitPanel.setRightComponent(treeNodeContextContainer);
    splitPanel.setLeftComponent(workspaceTreePanel);

    splitPanel.setDividerLocation(
        BinDiffConfig.getInstance().getMainSettings().getWorkspaceTreeDividerPosition());

    add(splitPanel, BorderLayout.CENTER);
  }

  public WorkspaceTabPanelFunctions getController() {
    return controller;
  }

  public int getDividerLocation() {
    return splitPanel.getDividerLocation();
  }

  @Override
  public Icon getIcon() {
    return ICON;
  }

  @Override
  public JMenuBar getMenuBar() {
    return menuBar;
  }

  @Override
  public String getTitle() {
    return "Workspace";
  }

  public JPanel getTreeNodeContextContainer() {
    return treeNodeContextContainer;
  }

  public WorkspaceTree getWorkspaceTree() {
    return workspaceTreePanel.getWorkspaceTree();
  }
}
