// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.jtree;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import com.google.common.base.Preconditions;

public class IconNode extends DefaultMutableTreeNode {
  private static final long serialVersionUID = -7079996631145030853L;

  protected Icon icon = null;
  protected String iconName;

  public IconNode() {
    this(null);
  }

  public IconNode(final Object userObject) {
    this(userObject, true, null);
  }

  public IconNode(final Object userObject, final boolean allowsChildren, final Icon icon) {
    super(userObject, allowsChildren);
    this.icon = icon;
  }

  public Icon getIcon() {
    return icon;
  }

  public String getIconName() {
    if (iconName != null) {
      return iconName;
    } else {
      if (userObject == null) {
        return null;
      } else {
        final String str = userObject.toString();
        int index = str.lastIndexOf(".");
        if (index != -1) {
          return str.substring(++index);
        } else {
          return null;
        }
      }
    }
  }

  public void setIcon(final Icon icon) {
    this.icon = Preconditions.checkNotNull(icon, "Error: icon argument can not be null");
  }

  public void setIconName(final String name) {
    iconName = Preconditions.checkNotNull(name, "Error: name argument can not be null");
  }
}
