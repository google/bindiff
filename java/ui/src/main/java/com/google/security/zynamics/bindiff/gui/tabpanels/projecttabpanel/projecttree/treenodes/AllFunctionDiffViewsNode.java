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

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.WorkspaceTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu.FunctionDiffContainerNodePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.FunctionDiffViewsContainerNodeContextPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.FunctionDiffViewsNodeContextPanel;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public class AllFunctionDiffViewsNode extends AbstractTreeNode {
  private static final ImageIcon ICON =
      ResourceUtils.getImageIcon("data/treeicons/function-diffs-container.png");

  private final FunctionDiffViewsContainerNodeContextPanel component;

  public AllFunctionDiffViewsNode(final WorkspaceTabPanelFunctions controller) {
    super(controller, null);

    component = new FunctionDiffViewsContainerNodeContextPanel(getController());
  }

  private Map<File, List<Diff>> fillImageDiffViewsListMap() {
    final Workspace workspace = getController().getWorkspace();
    final Map<File, List<Diff>> imageToDiffViewsListMap = new HashMap<>();

    for (final Diff diff : workspace.getDiffList(true)) {
      final File diffLocation = diff.getMatchesDatabase().getParentFile();

      List<Diff> diffList = imageToDiffViewsListMap.get(diffLocation);
      if (diffList == null) {
        diffList = new ArrayList<>();
      }

      diffList.add(diff);

      imageToDiffViewsListMap.put(diffLocation, diffList);
    }

    return imageToDiffViewsListMap;
  }

  @Override
  protected void createChildren() {
    final Map<File, List<Diff>> imageToDiffViewsListMap = fillImageDiffViewsListMap();

    final Map<File, FunctionDiffViewsNode> existingDirsToDiffNode = new HashMap<>();
    for (int index = 0; index < getChildCount(); ++index) {
      final FunctionDiffViewsNode child = (FunctionDiffViewsNode) getChildAt(index);
      existingDirsToDiffNode.put(child.getViewDirectory(), child);
    }

    for (final Entry<File, List<Diff>> entry : imageToDiffViewsListMap.entrySet()) {
      final FunctionDiffViewsNode child = existingDirsToDiffNode.get(entry.getKey());
      if (child != null) {
        final FunctionDiffViewsNodeContextPanel component = child.getComponent();
        component.getFunctionViewsTableModel().setFunctionDiffList(entry.getValue());
        component.updateUI();
      } else {
        add(new FunctionDiffViewsNode(getController(), entry.getKey(), entry.getValue()));
      }
    }
  }

  @Override
  protected void delete() {
    component.dispose();
  }

  public void addDiff(final Diff diff) {
    if (diff.isFunctionDiff()) {
      if (!WorkspaceTree.hasFunctionDiffRelatives(getTree(), diff) && !isLeaf()) {
        final File diffLocation = new File(diff.getMatchesDatabase().getParent());
        add(new FunctionDiffViewsNode(getController(), diffLocation, new ArrayList<>()));
      }
    }
  }

  @Override
  public void doubleClicked() {
    if (getController().getWorkspace().getDiffList(true).size() > 0) {
      getController().loadFunctionDiffs();

      createChildren();
    }
  }

  @Override
  public FunctionDiffViewsContainerNodeContextPanel getComponent() {
    return component;
  }

  @Override
  public Icon getIcon() {
    return ICON;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return new FunctionDiffContainerNodePopupMenu(getController());
  }

  @Override
  public String toString() {
    return String.format(
        "Single Function Diff Views (%d)", getController().getWorkspace().getDiffList(true).size());
  }
}
