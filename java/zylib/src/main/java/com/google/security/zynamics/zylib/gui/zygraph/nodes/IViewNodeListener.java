// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import java.awt.Color;

public interface IViewNodeListener {
  void changedBorderColor(IViewNode<?> node, Color color);

  void changedColor(IViewNode<?> node, Color color);

  void changedSelection(IViewNode<?> node, boolean selected);

  void changedVisibility(IViewNode<?> node, boolean visible);

  void heightChanged(IViewNode<?> node, double height);

  void widthChanged(IViewNode<?> node, double width);

  void xposChanged(IViewNode<?> node, double xpos);

  void yposChanged(IViewNode<?> node, double ypos);
}
