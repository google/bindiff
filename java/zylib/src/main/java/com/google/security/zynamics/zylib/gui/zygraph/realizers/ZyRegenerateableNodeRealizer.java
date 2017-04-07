// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

public class ZyRegenerateableNodeRealizer implements IZyRegenerateableRealizer {
  private final IZyNodeRealizer realizer;

  public ZyRegenerateableNodeRealizer(final IZyNodeRealizer realizer) {
    this.realizer = Preconditions.checkNotNull(realizer, "Error: Node realizer can't be null.");
  }

  @Override
  public void regenerate() {
    realizer.regenerate();
  }

  @Override
  public void repaint() {
    realizer.repaint();
  }
}
