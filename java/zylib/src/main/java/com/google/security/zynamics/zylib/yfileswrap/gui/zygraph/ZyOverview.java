// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.gui.zygraph.IFineGrainedSloppyGraph2DView;
import y.view.Graph2DView;
import y.view.Overview;

/** Draws a miniature overview of a graph. Used to aid graph navigation. */
public class ZyOverview extends Overview implements IFineGrainedSloppyGraph2DView {
  private int minEdgesForSloppyEdgeHiding;

  public ZyOverview(final Graph2DView graph2d) {
    super(graph2d);
    // Disable the sloppy/non-sloppy decision since we are working with the
    // fine-grained renderer.
    setPaintDetailThreshold(0.0);
    setMinEdgesForSloppyEdgeHiding(1000);
  }

  @Override
  public boolean drawEdges() {
    return getGraph2D().E() < minEdgesForSloppyEdgeHiding;
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
    // Do nothing: Always draw sloppy in the overview
  }

  @Override
  public void setMinEdgesForSloppyEdgeHiding(final int minEdges) {
    minEdgesForSloppyEdgeHiding = minEdges;
  }

  @Override
  public void setNodeSloppyThreshold(final double nodeSloppyThreshold) {
    // Do nothing: Always draw sloppy in the overview
  }

  @Override
  public void setSloppyEdgeHidingThreshold(final double sloppyEdgeHidingThreshold) {
    // Do nothing: Always draw sloppy in the overview
  }
}
