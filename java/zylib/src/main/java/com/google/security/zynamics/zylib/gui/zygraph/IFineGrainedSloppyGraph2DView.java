// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph;

public interface IFineGrainedSloppyGraph2DView {
  public boolean drawEdges();

  public boolean isEdgeSloppyPaintMode();

  public boolean isNodeSloppyPaintMode();

  public void setEdgeSloppyThreshold(double edgeSloppyThreshold);

  public void setMinEdgesForSloppyEdgeHiding(int minEdges);

  public void setNodeSloppyThreshold(double nodeSloppyThreshold);

  public void setSloppyEdgeHidingThreshold(double sloppyEdgeHidingThreshold);
}
