// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu.NodePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.MatchedFunctionsTreeNodeContextPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public final class MatchedFunctionViewsNode extends AbstractTreeNode {
  private static final ImageIcon ICON =
      ResourceUtils.getImageIcon("data/treeicons/matched-functions.png");

  private NodePopupMenu popupMenu;

  private final MatchedFunctionsTreeNodeContextPanel component;

  public MatchedFunctionViewsNode(final WorkspaceTabPanelFunctions controller, final Diff diff) {
    super(controller, diff);

    popupMenu = new NodePopupMenu(controller);

    component = new MatchedFunctionsTreeNodeContextPanel(controller, diff);
  }

  @Override
  protected void createChildren() {
    // no children
  }

  @Override
  protected void delete() {
    component.dispose();
    popupMenu.dispose();
    popupMenu = null;
  }

  @Override
  public void doubleClicked() {}

  @Override
  public Component getComponent() {
    return component;
  }

  @Override
  public Icon getIcon() {
    return ICON;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return popupMenu;
  }

  @Override
  public String toString() {
    return String.format(
        "Matched Functions (%d)", getDiff().getMatches().getSizeOfMatchedFunctions());
  }
}
