// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import y.base.Node;
import y.view.HitInfo;

/**
 * This class represents the mouse state that is reached as soon as the user moves the mouse into a
 * node.
 */
public final class CNodeEditState implements IMouseState {
  /** State factory that creates new state objects when necessary. */
  private final CStateFactory<?, ?> m_factory;

  /** The graph the entered node belongs to. */
  private final AbstractZyGraph<?, ?> m_graph;

  /** The entered node. */
  private final Node m_node;

  private boolean m_isDragging = false;

  /**
   * Creates a new state object.
   *
   * @param factory State factory that creates new state objects when necessary.
   * @param graph The graph the entered node belongs to.
   * @param node The entered node.
   */
  public CNodeEditState(
      final CStateFactory<?, ?> factory, final AbstractZyGraph<?, ?> graph, final Node node) {
    m_factory = factory;
    m_graph = graph;
    m_node = node;
  }

  /**
   * Returns the graph the entered node belongs to.
   *
   * @return The graph the entered node belongs to.
   */
  public AbstractZyGraph<?, ?> getGraph() {
    return m_graph;
  }

  /**
   * Returns the entered node.
   *
   * @return The entered node.
   */
  public Node getNode() {
    return m_node;
  }

  @Override
  public CStateFactory<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> getStateFactory() {
    return m_factory;
  }

  @Override
  public IMouseStateChange mouseDragged(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    m_isDragging = true;

    CEditNodeHelper.select(graph, m_node, event);

    return new CStateChange(this, false);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      final Node n = hitInfo.getHitNode();

      if (SwingUtilities.isLeftMouseButton(event) && !event.isAltDown()) {
        if (n == m_node) {
          if (!m_isDragging) {
            // Change caret
            CEditNodeHelper.setCaretStart(graph, n, event);
          } else {
            m_isDragging = false;
          }

          return new CStateChange(this, false);
        } else {
          m_factory.createNodeEditExitState(m_node, event);

          return new CStateChange(m_factory.createNodePressedLeftState(n, event), true);
        }
      } else if (SwingUtilities.isRightMouseButton(event)) {
        if (n == m_node) {
          // Do nothing

          return new CStateChange(this, false);
        } else {
          m_factory.createNodeEditExitState(m_node, event);

          return new CStateChange(m_factory.createNodePressedRightState(n, event), true);
        }
      } else if (SwingUtilities.isMiddleMouseButton(event)
          || (event.isAltDown() && SwingUtilities.isLeftMouseButton(event))) {
        if (n == m_node) {
          // m_factory.createNodeEditExitState(m_node, event);

          if (!m_isDragging) {
            // Change caret
            CEditNodeHelper.setCaretStart(graph, n, event);
          } else {
            m_isDragging = false;
          }

          return new CStateChange(this, false);
        } else {
          m_factory.createNodeEditExitState(m_node, event);

          return new CStateChange(m_factory.createNodePressedMiddleState(n, event), true);
        }
      } else {
        // A button was pressed that does not have any special functionality.

        return new CStateChange(this, false);
      }
    } else if (hitInfo.hasHitNodeLabels()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitEdges()) {
      m_factory.createNodeEditExitState(m_node, event);

      return new CStateChange(
          m_factory.createEdgePressedLeftState(hitInfo.getHitEdge(), event), true);
    } else if (hitInfo.hasHitEdgeLabels()) {
      m_factory.createNodeEditExitState(m_node, event);

      return new CStateChange(
          m_factory.createEdgePressedLeftState(hitInfo.getHitEdgeLabel().getEdge(), event), true);
    } else if (hitInfo.hasHitBends()) {
      m_factory.createNodeEditExitState(m_node, event);

      return new CStateChange(
          m_factory.createBendPressedLeftState(hitInfo.getHitBend(), event), true);
    } else if (hitInfo.hasHitPorts()) {
      m_factory.createNodeEditExitState(m_node, event);

      return new CStateChange(m_factory.createDefaultState(), true);
    } else {
      // User left-pressed the background.

      m_factory.createNodeEditExitState(m_node, event);

      return new CStateChange(m_factory.createBackgroundPressedLeftState(event), true);
    }
  }

  @Override
  public IMouseStateChange mouseReleased(
      final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      final Node n = hitInfo.getHitNode();

      if (SwingUtilities.isLeftMouseButton(event) && !event.isAltDown()) {
        if (n == m_node) {
          if (!m_isDragging) {
            // Change caret
            CEditNodeHelper.setCaretEnd(graph, n, event);
          } else {
            m_isDragging = false;
          }

          return new CStateChange(this, false);
        } else {
          m_factory.createNodeEditExitState(m_node, event);

          return new CStateChange(m_factory.createNodePressedLeftState(n, event), true);
        }
      } else if (SwingUtilities.isRightMouseButton(event)) {
        if (n == m_node) {
          // Show context menu

          // NH 10.08.2010
          // m_factory.editNodeRightClicked(m_node, event, x, y);

          return new CStateChange(this, false);
        } else {
          m_factory.createNodeEditExitState(m_node, event);

          return new CStateChange(m_factory.createNodePressedRightState(n, event), true);
        }
      } else if (SwingUtilities.isMiddleMouseButton(event)
          || (event.isAltDown() && SwingUtilities.isLeftMouseButton(event))) {
        if (n == m_node) {
          // m_factory.createNodeEditExitState(m_node, event);

          if (!m_isDragging) {
            // Change caret
            CEditNodeHelper.setCaretEnd(graph, n, event);
          } else {
            m_isDragging = false;
          }

          return new CStateChange(this, false);
        } else {
          m_factory.createNodeEditExitState(m_node, event);

          return new CStateChange(m_factory.createNodePressedMiddleState(n, event), true);
        }
      } else {
        // A button was pressed that does not have any special functionality.

        return new CStateChange(this, false);
      }
    } else if (hitInfo.hasHitNodeLabels()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitEdges()) {
      m_factory.createNodeEditExitState(m_node, event);

      return new CStateChange(
          m_factory.createEdgePressedLeftState(hitInfo.getHitEdge(), event), true);
    } else if (hitInfo.hasHitEdgeLabels()) {
      m_factory.createNodeEditExitState(m_node, event);

      return new CStateChange(
          m_factory.createEdgePressedLeftState(hitInfo.getHitEdgeLabel().getEdge(), event), true);
    } else if (hitInfo.hasHitBends()) {
      m_factory.createNodeEditExitState(m_node, event);

      return new CStateChange(
          m_factory.createBendPressedLeftState(hitInfo.getHitBend(), event), true);
    } else if (hitInfo.hasHitPorts()) {
      m_factory.createNodeEditExitState(m_node, event);

      return new CStateChange(m_factory.createDefaultState(), true);
    } else {
      // User left-pressed the background.

      m_factory.createNodeEditExitState(m_node, event);

      return new CStateChange(m_factory.createBackgroundPressedLeftState(event), true);
    }
  }
}
