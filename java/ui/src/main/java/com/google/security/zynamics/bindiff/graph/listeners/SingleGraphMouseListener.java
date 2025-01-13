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

package com.google.security.zynamics.bindiff.graph.listeners;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.eventhandlers.GraphLayoutEventHandler;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.synchronizer.GraphMouseHoverSynchronizer;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.popupmenus.CallGraphPopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.popupmenus.FlowGraphPopupMenu;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyGraphListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import y.view.EdgeLabel;

// TODO(cblichmann): This code is almost exactly the same as in CombinedGraphMouseListener.
//                   Merge/add a common base class.
public class SingleGraphMouseListener implements IZyGraphListener<SingleDiffNode, SingleDiffEdge> {
  private final SingleGraph graph;
  private final ViewTabPanelFunctions controller;

  protected SingleGraphMouseListener(
      final ViewTabPanelFunctions controller, final SingleGraph graph) {
    this.controller = checkNotNull(controller);
    this.graph = checkNotNull(graph);

    addListener();
  }

  public void addListener() {
    graph.addListener(this);
  }

  @Override
  public void edgeClicked(
      final SingleDiffEdge edge, final MouseEvent event, final double x, final double y) {}

  @Override
  public void edgeLabelEntered(final EdgeLabel label, final MouseEvent event) {}

  @Override
  public void edgeLabelExited(final EdgeLabel label) {}

  @Override
  public void nodeClicked(
      final SingleDiffNode node, final MouseEvent event, final double x, final double y) {
    if (SwingUtilities.isRightMouseButton(event)) {
      if (node.getRawNode() instanceof RawFunction) {
        final JPopupMenu menu = new CallGraphPopupMenu(controller, graph, node);
        menu.show(graph.getView(), event.getX(), event.getY());
      } else if (node.getRawNode() instanceof RawBasicBlock) {
        final JPopupMenu menu = new FlowGraphPopupMenu(controller, graph, node);
        menu.show(graph.getView(), event.getX(), event.getY());
      }
      return;
    }

    if (SwingUtilities.isLeftMouseButton(event)
        && event.getClickCount() == 2
        && graph.getGraphType() == EGraphType.CALL_GRAPH) {
      controller.openFlowgraphsViews(node);
    }
  }

  @Override
  public void nodeEntered(final SingleDiffNode node, final MouseEvent event) {
    if (node != null) {
      GraphMouseHoverSynchronizer.adoptHoveredNodeState(graph, node);
    }
  }

  @Override
  public void nodeHovered(final SingleDiffNode node, final double x, final double y) {}

  @Override
  public void nodeLeft(final SingleDiffNode node) {
    if (node != null) {
      GraphMouseHoverSynchronizer.adoptHoveredNodeState(graph, node);
    }
  }

  @Override
  public void proximityBrowserNodeClicked(
      final ZyProximityNode<?> proximityNode,
      final MouseEvent event,
      final double x,
      final double y) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      GraphLayoutEventHandler.handleProximityNodeClickedEvent(graph, proximityNode);
    }
  }

  public void removeListener() {
    graph.removeListener(this);
  }
}
