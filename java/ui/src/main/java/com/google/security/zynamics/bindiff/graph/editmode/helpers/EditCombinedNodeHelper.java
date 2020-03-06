// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.graph.editmode.helpers;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.realizers.CombinedNodeRealizer;
import com.google.security.zynamics.zylib.gui.zygraph.CDefaultLabelEventHandler;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyRegenerateableNodeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import java.awt.event.MouseEvent;
import y.base.Node;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeRealizer;

public final class EditCombinedNodeHelper {
  private EditCombinedNodeHelper() {}

  private static boolean isRightSideLabel(
      final CombinedGraph combinedGraph, final Node node, final double mouseX) {
    final CombinedDiffNode combinedNode = combinedGraph.getNode(node);

    if (combinedNode.getPrimaryDiffNode() != null && combinedNode.getSecondaryDiffNode() != null) {
      final IZyNodeRealizer realizer = (IZyNodeRealizer) combinedGraph.getGraph().getRealizer(node);
      final double centerX = realizer.getCenterX();

      return mouseX >= centerX;
    }

    return false;
  }

  public static ZyLabelContent getLabelContent(
      final CombinedGraph combinedGraph, final Node node, final MouseEvent event) {
    final int mouseX = event.getX();

    final CombinedDiffNode combinedNode = combinedGraph.getNode(node);

    if (combinedNode.getPrimaryDiffNode() != null && combinedNode.getSecondaryDiffNode() != null) {
      final Graph2D yGraph = combinedGraph.getGraph();
      final Graph2DView yView = combinedGraph.getView();
      final double viewX = yView.toWorldCoordX(mouseX);

      final NodeRealizer yRealizer = yGraph.getRealizer(node);

      final double cX = yRealizer.getX();
      final double cW = yRealizer.getWidth();

      if (viewX > cX && viewX < cX + cW / 2) {
        return combinedNode.getPrimaryDiffNode().getRealizer().getNodeContent();
      }

      return combinedNode.getSecondaryDiffNode().getRealizer().getNodeContent();
    }

    return combinedNode.getRealizer().getNodeContent();
  }

  public static void removeCaret(final AbstractZyGraph<?, ?> graph) {
    final CDefaultLabelEventHandler labelEventHandler = graph.getEditMode().getLabelEventHandler();

    if (labelEventHandler.isActive()) {
      labelEventHandler.deactivateLabelContent();
    }
  }

  public static void select(
      final AbstractZyGraph<?, ?> graph, final Node node, final MouseEvent event) {
    final double mouseX = graph.getEditMode().translateX(event.getX());
    final double mouseY = graph.getEditMode().translateY(event.getY());

    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getGraph().getRealizer(node);
    final ZyLabelContent labelContent = realizer.getNodeContent();

    final CDefaultLabelEventHandler labelEventHandler = graph.getEditMode().getLabelEventHandler();

    if (labelContent.isSelectable()) {
      final double zoom = graph.getView().getZoom();

      final double nodeX = realizer.getRealizer().getX();
      final double nodeY = realizer.getRealizer().getY();

      if (isRightSideLabel((CombinedGraph) graph, node, mouseX)) {
        final int oldPadding = labelContent.getPaddingLeft();
        labelContent.setPaddingLeft((int) Math.round(oldPadding + realizer.getCenterX()));

        labelEventHandler.handleMouseDraggedEvent(nodeX, nodeY, mouseX, mouseY, zoom);

        labelContent.setPaddingLeft(oldPadding);
      } else {
        labelEventHandler.handleMouseDraggedEvent(nodeX, nodeY, mouseX, mouseY, zoom);
      }
    }
  }

  public static void setActiveLabelContent(
      final CombinedGraph graph, final Node node, final MouseEvent event) {
    final int mouseX = event.getX();

    final CombinedDiffNode combinedNode = graph.getNode(node);

    if (combinedNode.getPrimaryDiffNode() != null && combinedNode.getSecondaryDiffNode() != null) {
      final Graph2D yGraph = graph.getGraph();
      final Graph2DView yView = graph.getView();
      final double viewX = yView.toWorldCoordX(mouseX);

      final NodeRealizer yRealizer = yGraph.getRealizer(node);

      final double cX = yRealizer.getX();
      final double cW = yRealizer.getWidth();

      if (viewX > cX && viewX < cX + cW / 2) {
        ((CombinedNodeRealizer) combinedNode.getRealizer()).setActiveContent(ESide.PRIMARY);
      } else {
        ((CombinedNodeRealizer) combinedNode.getRealizer()).setActiveContent(ESide.SECONDARY);
      }
    }
  }

  public static void setCaretEnd(
      final AbstractZyGraph<?, ?> graph, final Node node, final MouseEvent event) {
    final double mouseX = graph.getEditMode().translateX(event.getX());
    final double mouseY = graph.getEditMode().translateY(event.getY());

    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getGraph().getRealizer(node);
    final ZyLabelContent labelContent = realizer.getNodeContent();

    final CDefaultLabelEventHandler labelEventHandler = graph.getEditMode().getLabelEventHandler();

    if (labelContent.isSelectable()) {
      final double zoom = graph.getView().getZoom();

      final double nodeX = realizer.getRealizer().getX();
      final double nodeY = realizer.getRealizer().getY();

      if (isRightSideLabel((CombinedGraph) graph, node, mouseX)) {
        final int oldPadding = labelContent.getPaddingLeft();
        labelContent.setPaddingLeft((int) Math.round(oldPadding + realizer.getCenterX()));

        labelEventHandler.handleMouseReleasedEvent(
            nodeX, nodeY, mouseX, mouseY, zoom, event.getClickCount());

        labelContent.setPaddingLeft(oldPadding);
      } else {
        labelEventHandler.handleMouseReleasedEvent(
            nodeX, nodeY, mouseX, mouseY, zoom, event.getClickCount());
      }
    }
  }

  public static void setCaretStart(
      final AbstractZyGraph<?, ?> graph, final Node node, final MouseEvent event) {
    final double mouseX = graph.getEditMode().translateX(event.getX());
    final double mouseY = graph.getEditMode().translateY(event.getY());

    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getGraph().getRealizer(node);
    final ZyLabelContent labelContent = realizer.getNodeContent();

    final CDefaultLabelEventHandler labelEventHandler = graph.getEditMode().getLabelEventHandler();

    graph
        .getEditMode()
        .getLabelEventHandler()
        .activateLabelContent(labelContent, new ZyRegenerateableNodeRealizer(realizer));

    if (labelContent.isSelectable()) {
      final double zoom = graph.getView().getZoom();

      final double nodeX = realizer.getRealizer().getX();
      final double nodeY = realizer.getRealizer().getY();

      if (isRightSideLabel((CombinedGraph) graph, node, mouseX)) {
        final int oldPadding = labelContent.getPaddingLeft();
        labelContent.setPaddingLeft((int) Math.round(oldPadding + realizer.getCenterX()));

        labelEventHandler.handleMousePressedEvent(nodeX, nodeY, mouseX, mouseY, zoom);

        labelContent.setPaddingLeft(oldPadding);
      } else {
        labelEventHandler.handleMousePressedEvent(nodeX, nodeY, mouseX, mouseY, zoom);
      }
    }
  }
}
