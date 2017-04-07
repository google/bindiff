// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.edges;

import java.awt.Color;

public interface IViewEdgeListener {
  void addedBend(IViewEdge<?> edge, CBend path);

  void changedColor(CViewEdge<?> edge, Color color);

  void changedSelection(IViewEdge<?> edge, boolean selected);

  void changedSourceX(CViewEdge<?> edge, double sourceX);

  void changedSourceY(CViewEdge<?> edge, double sourceY);

  void changedTargetX(CViewEdge<?> edge, double targetX);

  void changedTargetY(CViewEdge<?> edge, double targetY);

  void changedType(CViewEdge<?> edge, EdgeType type);

  void changedVisibility(IViewEdge<?> edge, boolean visibility);

  void clearedBends(IViewEdge<?> edge);

  void insertedBend(IViewEdge<?> edge, int index, CBend path);

  void removedBend(CViewEdge<?> edge, int index, CBend path);
}
