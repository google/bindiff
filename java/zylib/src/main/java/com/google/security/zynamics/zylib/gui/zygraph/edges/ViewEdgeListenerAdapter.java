// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.edges;

import java.awt.Color;

public abstract class ViewEdgeListenerAdapter implements IViewEdgeListener {

  @Override
  public void addedBend(final IViewEdge<?> edge, final CBend path) {
  }

  @Override
  public void changedColor(final CViewEdge<?> edge, final Color color) {
  }

  @Override
  public void changedSelection(final IViewEdge<?> edge, final boolean selected) {
  }

  @Override
  public void changedSourceX(final CViewEdge<?> edge, final double sourceX) {
  }

  @Override
  public void changedSourceY(final CViewEdge<?> edge, final double sourceY) {
  }

  @Override
  public void changedTargetX(final CViewEdge<?> edge, final double targetX) {
  }

  @Override
  public void changedTargetY(final CViewEdge<?> edge, final double targetY) {
  }

  @Override
  public void changedType(final CViewEdge<?> edge, final EdgeType type) {
  }

  @Override
  public void changedVisibility(final IViewEdge<?> edge, final boolean visibility) {
  }

  @Override
  public void clearedBends(final IViewEdge<?> edge) {
  }

  @Override
  public void insertedBend(final IViewEdge<?> edge, final int index, final CBend path) {
  }

  @Override
  public void removedBend(final CViewEdge<?> edge, final int index, final CBend path) {
  }
}
