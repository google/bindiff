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

package com.google.security.zynamics.bindiff.graph.listeners;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.synchronizer.GraphViewCanvasSynchronizer;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SingleViewCanvasListener implements PropertyChangeListener {
  private final SingleGraph graph;

  private final ViewTabPanelFunctions viewPanelController;

  private boolean suppressUpdateGraph = false;

  protected SingleViewCanvasListener(
      final ViewTabPanelFunctions controller, final SingleGraph graph) {
    viewPanelController = checkNotNull(controller);
    this.graph = checkNotNull(graph);

    addListener();
  }

  public void addListener() {
    graph.getView().getCanvasComponent().addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (graph.getGraphType() == EGraphType.FLOW_GRAPH && graph.getFunctionAddress() == null) {
      // don't sync zoom and view point if it's is an unmatched view;
      return;
    }

    if ("Zoom".equals(event.getPropertyName())) {
      GraphViewCanvasSynchronizer.adoptZoom(viewPanelController.getGraphListenerManager(), graph);
    } else if ("ViewPoint".equals(event.getPropertyName())) {
      GraphViewCanvasSynchronizer.adoptViewPoint(
          viewPanelController.getGraphListenerManager(),
          graph,
          (Point2D.Double) event.getOldValue(),
          suppressUpdateGraph);
    }
  }

  public void removeListener() {
    graph.getView().getCanvasComponent().removePropertyChangeListener(this);
  }

  public void suppressUpdateGraph(final boolean suppress) {
    suppressUpdateGraph = suppress;
  }
}
