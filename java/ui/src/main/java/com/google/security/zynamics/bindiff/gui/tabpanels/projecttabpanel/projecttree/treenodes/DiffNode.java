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
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.DiffTreeNodeContextPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffListener;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

public final class DiffNode extends AbstractTreeNode {
  private static final ImageIcon ICON_UNLOADED =
      ResourceUtils.getImageIcon("data/treeicons/diff-unloaded.png");
  private static final ImageIcon ICON_LOADED =
      ResourceUtils.getImageIcon("data/treeicons/diff.png");
  private static final ImageIcon ICON_MISSING_DIFF_BINARAY =
      ResourceUtils.getImageIcon("data/treeicons/missing-diff-db.png");

  private final InternalDiffModelListener diffModelListener = new InternalDiffModelListener();

  private final WorkspaceTabPanelFunctions controller;

  private final NodePopupMenu popupMenu;

  private final DiffTreeNodeContextPanel component;

  public DiffNode(final Diff diff, final WorkspaceTabPanelFunctions controller) {
    super(controller, diff);

    // Cannot be null here, super() checks that
    this.controller = controller;
    popupMenu = new NodePopupMenu(controller);
    component = new DiffTreeNodeContextPanel(diff, controller);

    diff.addListener(diffModelListener);
  }

  @Override
  protected void createChildren() {
    add(new CallGraphNode(getController(), getDiff()));
    add(new MatchedFunctionViewsNode(getController(), getDiff()));
    add(new UnmatchedFunctionViewsNode(getController(), getDiff(), ESide.PRIMARY));
    add(new UnmatchedFunctionViewsNode(getController(), getDiff(), ESide.SECONDARY));
  }

  @Override
  protected void delete() {
    getDiff().removeListener(diffModelListener);
    popupMenu.dispose();

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    controller.loadDiff(getDiff());
  }

  @Override
  public Component getComponent() {
    return component;
  }

  @Override
  public Icon getIcon() {
    if (!getDiff().getMatchesDatabase().exists()
        || !getDiff().getExportFile(ESide.PRIMARY).exists()
        || !getDiff().getExportFile(ESide.SECONDARY).exists()) {
      return ICON_MISSING_DIFF_BINARAY;
    }

    return getDiff().isLoaded() ? ICON_LOADED : ICON_UNLOADED;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return popupMenu;
  }

  @Override
  public String toString() {
    return getDiff().getMatchesDatabase().getParentFile().getName();
  }

  private class InternalDiffModelListener implements DiffListener {
    @Override
    public void loadedDiff(final Diff diff) {
      if (!diff.isFunctionDiff()) {
        createChildren();
        getTree().expandPath(new TreePath(getPath()));
      }
    }

    @Override
    public void removedDiff(final Diff diff) {
      if (!diff.isFunctionDiff()) {
        removeFromParent();
        delete();
        getTree().setSelectionPath(new TreePath(getRoot().getPath()));
        // TODO(nilsheumer): Why isn't the tree updated automatically when the model has changed?
        getTree().updateTree();
      }
    }

    @Override
    public void unloadedDiff(final Diff diff) {
      if (!diff.isFunctionDiff()) {
        deleteChildren();
        getTree().setSelectionPath(new TreePath(getPath()));
        // TODO(nilsheumer): Why isn't the tree updated automatically when the model has changed.?
        getTree().updateTree();
      }
    }
  }
}
