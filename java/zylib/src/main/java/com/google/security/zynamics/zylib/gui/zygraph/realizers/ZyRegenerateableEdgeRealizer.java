// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import com.google.common.base.Preconditions;

public class ZyRegenerateableEdgeRealizer implements IZyRegenerateableRealizer {
  private final IZyEdgeRealizer m_realizer;

  public ZyRegenerateableEdgeRealizer(final IZyEdgeRealizer realizer) {
    Preconditions.checkNotNull(realizer, "Error: Edge realizer can't be null.");

    m_realizer = realizer;
  }

  @Override
  public void regenerate() {
    m_realizer.regenerate();
  }

  @Override
  public void repaint() {
    m_realizer.repaint();
  }
}
