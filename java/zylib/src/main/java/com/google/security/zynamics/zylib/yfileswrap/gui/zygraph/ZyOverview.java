// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.gui.zygraph.IFineGrainedSloppyGraph2DView;

import y.view.Graph2DView;
import y.view.Overview;

/**
 * @author thomasdullien@google.com (Thomas Dullien)
 * 
 */
public class ZyOverview extends Overview implements IFineGrainedSloppyGraph2DView {
  private int _minEdgesForSloppyEdgeHiding;

  /**
   * @param arg0
   */
  public ZyOverview(final Graph2DView arg0) {
    super(arg0);
    // Disable the sloppy/nonsloppy decision since we are working with the fine
    // grained renderer
    setPaintDetailThreshold(0.0);
    setMinEdgesForSloppyEdgeHiding(1000);
  }

  @Override
  public boolean drawEdges() {
    // System.out.println(getGraph2D().E());
    return getGraph2D().E() < _minEdgesForSloppyEdgeHiding;
  }

  @Override
  public boolean isEdgeSloppyPaintMode() {
    return true;
  }

  @Override
  public boolean isNodeSloppyPaintMode() {
    return true;
  }

  @Override
  public void setEdgeSloppyThreshold(final double edgeSloppyThreshold) {
    // This function can be empty: We will always draw sloppy in the overview
  }

  @Override
  public void setMinEdgesForSloppyEdgeHiding(final int minEdges) {
    _minEdgesForSloppyEdgeHiding = minEdges;
  }

  @Override
  public void setNodeSloppyThreshold(final double nodeSloppyThreshold) {
    // This function can be empty: We will always draw sloppy in the overview
  }

  @Override
  public void setSloppyEdgeHidingThreshold(final double sloppyEdgeHidingThreshold) {
    // This function can be empty: We will always draw sloppy in the overview
  }

}
