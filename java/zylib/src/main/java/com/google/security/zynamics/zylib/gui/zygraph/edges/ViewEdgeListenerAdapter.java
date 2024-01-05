// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.gui.zygraph.edges;

import java.awt.Color;

public abstract class ViewEdgeListenerAdapter implements IViewEdgeListener {

  @Override
  public void addedBend(final IViewEdge<?> edge, final CBend path) {}

  @Override
  public void changedColor(final CViewEdge<?> edge, final Color color) {}

  @Override
  public void changedSelection(final IViewEdge<?> edge, final boolean selected) {}

  @Override
  public void changedSourceX(final CViewEdge<?> edge, final double sourceX) {}

  @Override
  public void changedSourceY(final CViewEdge<?> edge, final double sourceY) {}

  @Override
  public void changedTargetX(final CViewEdge<?> edge, final double targetX) {}

  @Override
  public void changedTargetY(final CViewEdge<?> edge, final double targetY) {}

  @Override
  public void changedType(final CViewEdge<?> edge, final EdgeType type) {}

  @Override
  public void changedVisibility(final IViewEdge<?> edge, final boolean visibility) {}

  @Override
  public void clearedBends(final IViewEdge<?> edge) {}

  @Override
  public void insertedBend(final IViewEdge<?> edge, final int index, final CBend path) {}

  @Override
  public void removedBend(final CViewEdge<?> edge, final int index, final CBend path) {}
}
