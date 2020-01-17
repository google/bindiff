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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.CDefaultLabelEventHandler;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CDefaultActionFactory;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateActionFactory;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.ISelectableNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IViewableNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CTooltipUpdater;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import y.base.Edge;
import y.base.Node;
import y.view.EditMode;
import y.view.MagnifierViewMode;

/**
 * Default edit mode for ZyGraphs.
 *
 * @param <NodeType> Type of all nodes in the ZyGraph
 * @param <EdgeType> Type of all edges in the ZyGraph
 */
public class ZyEditMode<
        NodeType extends ZyGraphNode<?> & IViewableNode & ISelectableNode,
        EdgeType extends ZyGraphEdge<?, ?, ?>>
    extends EditMode {
  /**
   * Provides the default actions that are executed on changes in mouse states.
   */
  private final IStateActionFactory<NodeType, EdgeType> m_actionFactory;

  /**
   * Provides the available mouse states.
   */
  private final CStateFactory<NodeType, EdgeType> m_stateFactory;

  /**
   * The current mouse state. This is transformed into a new state on every significant mouse event.
   */
  private IMouseState m_state;

  /**
   * The owning graph of the edit mode.
   */
  private final AbstractZyGraph<NodeType, EdgeType> m_graph;

  /**
   * Used to display the magnifying glass in the graph window.
   */
  private final MagnifierViewMode m_magifierViewMode = new MagnifierViewMode();

  /** Listeners that are notified about import edit mode events. */
  private final List<IZyEditModeListener<NodeType, EdgeType>> m_listeners = new ArrayList<>();

  private final CDefaultLabelEventHandler m_labelKeyHandler;

  private boolean m_inMouseMoved;

  private boolean m_inMouseDragged;

  private boolean m_inMousePressed;

  private boolean m_inMouseDraggedRight;

  /**
   * Creates a new edit mode object which is tied to the given graph.
   * 
   * @param graph The owning graph of the edit mode.
   */
  public ZyEditMode(final AbstractZyGraph<NodeType, EdgeType> graph) {
    m_graph = Preconditions.checkNotNull(graph);
    m_labelKeyHandler = createNodeKeyHandler(graph);
    m_actionFactory = createStateActionFactory();
    m_stateFactory = new CStateFactory<>(graph, m_listeners, m_actionFactory);
    m_state = m_stateFactory.createDefaultState();

    setDefaultBehaviour();
  }

  /**
   * Determines whether magnifying mode is active or not.
   * 
   * @return True, if magnifying mode is active. False, otherwise.
   */
  private boolean getMagnifyingMode() {
    // ESCA-JAVA0254: Can not improve the loop to for-each
    for (final Iterator<?> iter = m_graph.getView().getViewModes(); iter.hasNext();) {
      if (iter.next() == m_magifierViewMode) {
        return true;
      }
    }

    return false;
  }

  /**
   * Initializes the default behavior of the edit mode.
   */
  private void setDefaultBehaviour() {
    allowBendCreation(false);
    allowEdgeCreation(false);
    allowNodeCreation(false);

    allowMoveLabels(true);
    allowMovePorts(true);
    allowNodeEditing(true);
    allowMoving(true);
    allowMoveSelection(true);

    showEdgeTips(true);
    showNodeTips(true);

    setSelectionBoxMode(new CSelectionMode<NodeType>(m_graph));

    m_graph.getView().getCanvasComponent()
        .addMouseWheelListener(new ZyEditModeMouseWheelListener<NodeType, EdgeType>(m_graph));
  }

  protected CDefaultLabelEventHandler createNodeKeyHandler(
      final AbstractZyGraph<NodeType, EdgeType> graph) {
    return new CDefaultLabelEventHandler(graph);
  }

  protected IStateActionFactory<NodeType, EdgeType> createStateActionFactory() {
    return new CDefaultActionFactory<NodeType, EdgeType>();
  }

  @Override
  protected String getEdgeTip(final Edge edge) {
    return CTooltipUpdater.updateEdgeTooltip(m_graph, edge);
  }

  protected AbstractZyGraph<NodeType, EdgeType> getGraph() {
    return m_graph;
  }

  @Override
  protected String getNodeTip(final Node node) {
    return CTooltipUpdater.updateNodeTooltip(m_graph, node);
  }

  /**
   * Adds a listener object that is notified about changes in the edit mode.
   * 
   * @param listener The listener object to add.
   */
  public void addListener(final IZyEditModeListener<NodeType, EdgeType> listener) {
    m_listeners.add(listener);
  }

  public CDefaultLabelEventHandler getLabelEventHandler() {
    return m_labelKeyHandler;
  }

  public boolean isInMouseDragged() {
    return m_inMouseDragged;
  }

  public boolean isInMouseDraggedRight() {
    return m_inMouseDraggedRight;
  }

  public boolean isInMouseMoved() {
    return m_inMouseMoved;
  }

  public boolean isInMousePressed() {
    return m_inMousePressed;
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    Preconditions.checkNotNull(e, "Error: mouse event can not be null");

    final IMouseStateChange result = m_state.mouseDragged(e, m_graph);

    m_state = result.getNextState();

    if (result.notifyYFiles()) {
      m_inMouseDragged = true;
      try {
        super.mouseDragged(e);
      } finally {
        m_inMouseDragged = false;
      }
    }
  }

  @Override
  public void mouseDraggedRight(final double x, final double y) {
    m_inMouseDraggedRight = true;

    try {
      super.mouseDraggedRight(x, y);
    } finally {
      m_inMouseDraggedRight = false;
    }
  }

  @Override
  public void mouseMoved(final double x, final double y) {
    m_inMouseMoved = true;
    try {
      super.mouseMoved(x, y);
    } finally {
      m_inMouseMoved = false;
    }
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    Preconditions.checkNotNull(e, "Error: mouse event can not be null");

    final IMouseStateChange result = m_state.mouseMoved(e, m_graph);

    m_state = result.getNextState();

    if (result.notifyYFiles()) {
      m_inMouseMoved = true;
      try {
        super.mouseMoved(e);
      } finally {
        m_inMouseMoved = false;
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    final IMouseStateChange result = m_state.mousePressed(e, m_graph);

    m_state = result.getNextState();

    if (result.notifyYFiles()) {
      m_inMousePressed = true;
      try {
        super.mousePressed(e);
      } finally {
        m_inMousePressed = false;
      }
    }
  }

  @Override
  public void mouseReleased(final double x, final double y) {
    super.mouseReleased(x, y);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    final IMouseStateChange result = m_state.mouseReleased(e, m_graph);

    m_state = result.getNextState();

    if (result.notifyYFiles()) {
      super.mouseReleased(e);
    }
  }

  /**
   * Enables or disables magnifying mode.
   * 
   * @param active True, to enable magnifying mode. False, to disable it.
   */
  public void setMagnifyingMode(final boolean active) {
    if (getMagnifyingMode() == active) {
      return;
    }

    if (active) {
      m_graph.getView().addViewMode(m_magifierViewMode);
    } else {
      m_graph.getView().removeViewMode(m_magifierViewMode);
    }
  }

  // ESCA-JAVA0059: Making this more visible
  @Override
  public double translateX(final int x) {
    return super.translateX(x);
  }

  // ESCA-JAVA0059: Making this more visible
  @Override
  public double translateY(final int y) {
    return super.translateY(y);
  }
}
