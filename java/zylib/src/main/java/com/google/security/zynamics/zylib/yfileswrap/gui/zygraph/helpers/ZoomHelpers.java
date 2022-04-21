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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers;

import y.view.Graph2DView;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ZoomHelpers {
  public static final double MAX_ZOOM = 3.;

  /**
   * Makes sure that the current zoom level does not exceed the maximum zoom level.
   */
  private static void decreaseToMaxZoom(final Graph2DView view) {
    if (view.getZoom() > MAX_ZOOM) {
      view.setZoom(MAX_ZOOM);
    }
  }

  /**
   * Makes sure that the current zoom level does not exceed the minimum zoom level.
   */
  private static void increaseToMinZoom(final Graph2DView view) {
    final double minZoom = getMinimumZoom(view);

    if (view.getZoom() < minZoom) {
      view.setZoom(minZoom);
    }
  }

  /**
   * Calculates the smallest valid zoom level.
   * 
   * @return The smallest valid zoom level.
   */
  public static double getMinimumZoom(final Graph2DView view) {
    double zoomlevel = view.getZoom();

    if (zoomlevel < 0) {
      zoomlevel = 1;
    }

    final Point2D viewPoint = view.getViewPoint2D();
    final Rectangle2D box = view.getGraph2D().getBoundingBox();
    view.zoomToArea(box.getX(), box.getY(), box.getWidth(), box.getHeight()); // why? => to gain the
                                                                              // minimum zoom level
                                                                              // in the next line
    final double minZoom = view.getZoom();
    view.setZoom(zoomlevel); // why? => to reset the original zoom level after the minimum zoom
                             // level was calulated
    view.setViewPoint2D(viewPoint.getX(), viewPoint.getY()); // why? => to reset the original view
                                                             // point
    return minZoom - (minZoom / 2);
  }

  /**
   * Keeps the zoom level within valid bounds.
   */
  public static void keepZoomValid(final Graph2DView view) {
    decreaseToMaxZoom(view);
    increaseToMinZoom(view);
  }

}
