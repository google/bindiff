// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.MouseWheelAction;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeFilter;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.ISelectableNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IViewableNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.MoveFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.ZoomHelpers;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseWheelEvent;
import java.util.Set;
import y.view.Graph2DViewMouseWheelZoomListener;

/**
 * Listens to mouse wheel events in edit mode.
 */
public class ZyEditModeMouseWheelListener<
        NodeType extends ZyGraphNode<?> & ISelectableNode & IViewableNode,
        EdgeType extends ZyGraphEdge<?, ?, ?>>
    extends Graph2DViewMouseWheelZoomListener {
  private static final double ZOOM_SUB_FACTOR = 0.02;
  private static final double SCROLL_SUB_FACTOR = 0.6;

  private final AbstractZyGraph<NodeType, EdgeType> m_zyGraph;

  public ZyEditModeMouseWheelListener(final AbstractZyGraph<NodeType, EdgeType> graph) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can't be null");

    m_zyGraph = graph;

    setCenterZooming(false);
  }

  private void centerZoom(final double factor, final boolean centerSelected) {
    if (centerSelected) {
      final Set<NodeType> selectedNodes = m_zyGraph.getSelectedNodes();

      if (selectedNodes.size() != 0) {
        MoveFunctions.centerNodes(m_zyGraph, selectedNodes);
      }
    }

    m_zyGraph.zoom(factor);
  }

  private void handleInMoveMode(final MouseWheelEvent event) {
    final boolean scrollDirection = event.getUnitsToScroll() > 0;

    if (event.isAltDown()) {
      moveVertical(scrollDirection);
    } else {
      moveHorizontal(scrollDirection);
    }
  }

  private void handleInZoomMode(final MouseWheelEvent event) {
    zoom(event, event.getUnitsToScroll() > 0, event.isShiftDown());
  }

  private boolean hasSelectedNode() {
    return GraphHelpers.any(m_zyGraph, new INodeFilter<NodeType>() {
      @Override
      public boolean qualifies(final NodeType node) {
        return node.isSelected();
      }
    });
  }

  private void moveHorizontal(final boolean zoomOut) {
    if (zoomOut) {
      MoveFunctions.pan(m_zyGraph, 0, SCROLL_SUB_FACTOR
          * m_zyGraph.getSettings().getMouseSettings().getScrollSensitivity());
    } else {
      MoveFunctions.pan(m_zyGraph, 0, -SCROLL_SUB_FACTOR
          * m_zyGraph.getSettings().getMouseSettings().getScrollSensitivity());
    }
  }

  private void moveVertical(final boolean scrollDirection) {
    if (scrollDirection) {
      MoveFunctions.pan(m_zyGraph, SCROLL_SUB_FACTOR
          * m_zyGraph.getSettings().getMouseSettings().getScrollSensitivity(), 0);
    } else {
      MoveFunctions.pan(m_zyGraph, -SCROLL_SUB_FACTOR
          * m_zyGraph.getSettings().getMouseSettings().getScrollSensitivity(), 0);
    }
  }

  private void zoom(final MouseWheelEvent event, final boolean zoomOut, final boolean centerSelected) {
    if (!hasSelectedNode() || !centerSelected) {
      // Zoom to cursor in the absence of selected nodes
      super.mouseWheelMoved(event);
    } else if (zoomOut) {
      centerZoom(
          1 - (m_zyGraph.getSettings().getMouseSettings().getZoomSensitivity() * ZOOM_SUB_FACTOR),
          centerSelected);
    } else {
      centerZoom(
          1 + (m_zyGraph.getSettings().getMouseSettings().getZoomSensitivity() * ZOOM_SUB_FACTOR),
          centerSelected);
    }
  }

  @Override
  protected double calcZoom(final double zoom, final int amount) {
    if (amount > 0) {
      final double minzoom = ZoomHelpers.getMinimumZoom(m_zyGraph.getView());

      setMinimumZoom(minzoom);

      final double zoomfactor =
          1 - (m_zyGraph.getSettings().getMouseSettings().getZoomSensitivity() * ZOOM_SUB_FACTOR);

      if (minzoom > 0.5) {
        super.setMinimumZoom(0.75);

        return m_zyGraph.getView().getZoom() * zoomfactor;
      }

      return Math.max(m_zyGraph.getView().getZoom() * zoomfactor, minzoom);
    }

    final double zoomfactor =
        1 + (m_zyGraph.getSettings().getMouseSettings().getZoomSensitivity() * ZOOM_SUB_FACTOR);

    // zoom out
    setMaximumZoom(ZoomHelpers.MAX_ZOOM);

    return m_zyGraph.getView().getZoom() * zoomfactor;
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent event) {
    final int ticks = Math.abs(event.getUnitsToScroll());

    // When the user holds down control, we switch between the two modes
    final boolean changeMode = event.isControlDown();

    for (int i = 0; i < ticks; ++i) {
      if (((m_zyGraph.getSettings().getMouseSettings().getMouseWheelAction() == MouseWheelAction.ZOOM) && !changeMode)
          || ((m_zyGraph.getSettings().getMouseSettings().getMouseWheelAction() == MouseWheelAction.SCROLL) && changeMode)) {
        handleInZoomMode(event);
      } else {
        handleInMoveMode(event);
      }
    }

    m_zyGraph.updateViews();
  }
}
