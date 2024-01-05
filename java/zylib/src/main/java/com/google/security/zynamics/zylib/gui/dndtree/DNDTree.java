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

package com.google.security.zynamics.zylib.gui.dndtree;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class DNDTree extends JTree {
  private static final long serialVersionUID = -2933192344665054732L;

  Insets autoscrollInsets = new Insets(20, 20, 20, 20); // insets

  public DNDTree() {
    super();

    setAutoscrolls(true);
    setRootVisible(false);
    setShowsRootHandles(false); // to show the root icon
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); // set single
    // selection for
    // the Tree
    setEditable(false);
  }

  public static DefaultMutableTreeNode makeDeepCopy(final DefaultMutableTreeNode node) {
    final DefaultMutableTreeNode copy = new DefaultMutableTreeNode(node.getUserObject());
    for (final Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
      copy.add(makeDeepCopy((DefaultMutableTreeNode) e.nextElement()));
    }
    return copy;
  }

  public void autoscroll(final Point cursorLocation) {
    final Insets insets = getAutoscrollInsets();
    final Rectangle outer = getVisibleRect();
    final Rectangle inner =
        new Rectangle(
            outer.x + insets.left,
            outer.y + insets.top,
            outer.width - (insets.left + insets.right),
            outer.height - (insets.top + insets.bottom));
    if (!inner.contains(cursorLocation)) {
      final Rectangle scrollRect =
          new Rectangle(
              cursorLocation.x - insets.left,
              cursorLocation.y - insets.top,
              insets.left + insets.right,
              insets.top + insets.bottom);
      scrollRectToVisible(scrollRect);
    }
  }

  public Insets getAutoscrollInsets() {
    return autoscrollInsets;
  }
}
