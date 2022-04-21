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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTree;

public class SelectionHistoryTree extends JTree {
  private final InternalMouseListener mouseListener = new InternalMouseListener();

  public SelectionHistoryTree(final SelectionHistoryRootNode rootNode) {
    super(rootNode);

    addMouseListener(mouseListener);
    setCellRenderer(new SelectionHistoryTreeCellRenderer(rootNode.getGraph()));
  }

  private void handleMouseEvent(final MouseEvent event) {
    final AbstractSelectionHistoryTreeNode selectedNode =
        (AbstractSelectionHistoryTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode == null) {
      return;
    }

    selectedNode.handleMouseEvent(event);
  }

  public void dispose() {
    removeMouseListener(mouseListener);
  }

  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(final MouseEvent event) {
      handleMouseEvent(event);
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      handleMouseEvent(event);
    }
  }
}
