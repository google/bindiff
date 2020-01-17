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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractSelectionHistoryTreeNode extends DefaultMutableTreeNode {
  public AbstractSelectionHistoryTreeNode(final String name) {
    super(name);
  }

  public ViewTabPanelFunctions getController() {
    return getRootNode().getController();
  }

  public abstract Icon getIcon();

  public abstract JPopupMenu getPopupMenu();

  public SelectionHistoryRootNode getRootNode() {
    return (SelectionHistoryRootNode) getRoot();
  }

  public JTree getTree() {
    return getRootNode().getTree();
  }

  public void handleMouseEvent(final MouseEvent event) {
    if (event.getButton() != MouseEvent.BUTTON3 || !event.isPopupTrigger()) {
      return;
    }
    final JPopupMenu popup = getPopupMenu();
    if (popup != null) {
      popup.show(getTree(), event.getX(), event.getY());
    }
  }
}
