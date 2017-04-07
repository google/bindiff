// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.jtree;

import java.awt.Component;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class IconNodeRenderer extends DefaultTreeCellRenderer {

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object value,
      final boolean sel, final boolean expanded, final boolean leaf, final int row,
      final boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    if (!(value instanceof IconNode)) {
      return this;
    }

    Icon icon = ((IconNode) value).getIcon();

    if (icon == null) {
      final Hashtable<?, ?> icons = (Hashtable<?, ?>) tree.getClientProperty("JTree.icons");
      final String name = ((IconNode) value).getIconName();
      if ((icons != null) && (name != null)) {
        icon = (Icon) icons.get(name);
        if (icon != null) {
          setIcon(icon);
        }
      }
    } else {
      setIcon(icon);
    }

    return this;
  }
}
