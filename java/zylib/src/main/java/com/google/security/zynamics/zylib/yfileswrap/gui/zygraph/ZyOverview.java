// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
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
