// Copyright 2011 Google Inc. All Rights Reserved.

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
    setShowsRootHandles(false);// to show the root icon
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); // set single
                                                                                    // selection for
                                                                                    // the Tree
    setEditable(false);
  }

  public static DefaultMutableTreeNode makeDeepCopy(final DefaultMutableTreeNode node) {
    final DefaultMutableTreeNode copy = new DefaultMutableTreeNode(node.getUserObject());
    for (final Enumeration<?> e = node.children(); e.hasMoreElements();) {
      copy.add(makeDeepCopy((DefaultMutableTreeNode) e.nextElement()));
    }
    return copy;
  }

  public void autoscroll(final Point cursorLocation) {
    final Insets insets = getAutoscrollInsets();
    final Rectangle outer = getVisibleRect();
    final Rectangle inner =
        new Rectangle(outer.x + insets.left, outer.y + insets.top, outer.width
            - (insets.left + insets.right), outer.height - (insets.top + insets.bottom));
    if (!inner.contains(cursorLocation)) {
      final Rectangle scrollRect =
          new Rectangle(cursorLocation.x - insets.left, cursorLocation.y - insets.top, insets.left
              + insets.right, insets.top + insets.bottom);
      scrollRectToVisible(scrollRect);
    }
  }

  public Insets getAutoscrollInsets() {
    return autoscrollInsets;
  }
}
