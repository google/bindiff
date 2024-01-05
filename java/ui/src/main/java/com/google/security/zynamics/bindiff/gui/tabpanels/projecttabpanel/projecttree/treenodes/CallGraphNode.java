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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu.NodePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.CallGraphsTreeNodeContextPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public final class CallGraphNode extends AbstractTreeNode {
  private static final ImageIcon ICON = ResourceUtils.getImageIcon("data/treeicons/callgraph.png");

  private final NodePopupMenu popupMenu;

  private final CallGraphsTreeNodeContextPanel component;

  public CallGraphNode(final WorkspaceTabPanelFunctions controller, final Diff diff) {
    super(controller, diff);

    popupMenu = new NodePopupMenu(controller);

    component = new CallGraphsTreeNodeContextPanel(getDiff(), getController());
  }

  @Override
  protected void createChildren() {
    // no children
  }

  @Override
  protected void delete() {
    popupMenu.dispose();
    component.dipose();
  }

  @Override
  public void doubleClicked() {
    final WorkspaceTabPanelFunctions controller = getController();
    controller.openCallGraphView(controller.getMainWindow(), getDiff());
  }

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

    final MatchData matches = getDiff().getMatches();

    return String.format(
        "Call Graph (%d/%d)",
        matches.getSizeOfFunctions(ESide.PRIMARY), matches.getSizeOfFunctions(ESide.SECONDARY));
  }
}
