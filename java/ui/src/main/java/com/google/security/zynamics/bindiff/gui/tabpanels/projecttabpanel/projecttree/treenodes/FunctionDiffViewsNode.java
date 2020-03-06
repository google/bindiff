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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu.FunctionDiffNodePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.FunctionDiffViewsNodeContextPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import java.io.File;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public class FunctionDiffViewsNode extends AbstractTreeNode {
  private static final ImageIcon ICON =
      ImageUtils.getImageIcon("data/treeicons/function-diff-views.png");

  private final FunctionDiffViewsNodeContextPanel component;

  private final File viewDirectory;

  public FunctionDiffViewsNode(
      final WorkspaceTabPanelFunctions controller,
      final File viewsDirectory,
      final List<Diff> functionDiffList) {
    super(controller, null);

    viewDirectory = checkNotNull(viewsDirectory);

    checkNotNull(functionDiffList);
    component = new FunctionDiffViewsNodeContextPanel(getController(), functionDiffList);
  }

  @Override
  protected void createChildren() {}

  @Override
  public void delete() {
    component.dispose();
  }

  @Override
  public void doubleClicked() {}

  @Override
  public FunctionDiffViewsNodeContextPanel getComponent() {
    return component;
  }

  @Override
  public Icon getIcon() {
    return ICON;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return new FunctionDiffNodePopupMenu(this);
  }

  public File getViewDirectory() {
    return viewDirectory;
  }

  @Override
  public String toString() {
    return String.format(
        "%s (%d)", viewDirectory.getName(), component.getFunctionViewsTableModel().getRowCount());
  }
}
