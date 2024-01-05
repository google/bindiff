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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.WorkspaceTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.DefaultTreeNodeContextPanel;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JPopupMenu;

public final class RootNode extends AbstractTreeNode {
  private Workspace workspace = null;

  private final WorkspaceTree tree;

  private final DefaultTreeNodeContextPanel component = new DefaultTreeNodeContextPanel();

  public RootNode(final WorkspaceTree tree, final WorkspaceTabPanelFunctions controller) {
    super(controller, null);

    this.tree = checkNotNull(tree);

    createChildren();
  }

  @Override
  protected void createChildren() {
    if (workspace != null) {
      add(new AllFunctionDiffViewsNode(getController()));

      for (final Diff diff : workspace.getDiffList()) {
        if (diff == null) {
          throw new RuntimeException("Diff cannot be null.");
        }

        if (!diff.isFunctionDiff()) {
          add(new DiffNode(diff, getController()));
        }
      }
    }
  }

  @Override
  protected void delete() {
    deleteChildren();
  }

  @Override
  protected WorkspaceTree getTree() {
    return tree;
  }

  public void addDiff(final Diff diff) {
    if (!diff.isFunctionDiff()) {
      add(new DiffNode(diff, getController()));
    }
  }

  @Override
  public void doubleClicked() {
    // do nothing
  }

  @Override
  public Component getComponent() {
    return component;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return null;
  }

  public void setWorkspace(final Workspace project) {
    workspace = project;
    createChildren();
  }

  @Override
  public String toString() {
    return "";
  }
}
