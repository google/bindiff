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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.RootNode;
import javax.swing.tree.DefaultTreeModel;

public final class WorkspaceTreeModel extends DefaultTreeModel {
  private final RootNode rootNode;

  private final WorkspaceTree tree;

  public WorkspaceTreeModel(final WorkspaceTree tree, final RootNode rootNode) {
    super(rootNode);

    if (tree == null) {
      throw new IllegalArgumentException("Project tree cannot be null.)");
    }

    this.tree = tree;
    this.rootNode = rootNode;
  }

  @Override
  public RootNode getRoot() {
    return rootNode;
  }

  public WorkspaceTree getTree() {
    return tree;
  }
}
