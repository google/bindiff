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

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateActionFactory;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundClickedLeftState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundClickedRightState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundDraggedLeftState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundDraggedRightState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundPressedLeftState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundPressedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendClickedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendEnterState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendExitState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendHoverState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendPressedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CDefaultState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeClickedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeClickedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeEnterState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeExitState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeHoverState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeLabelEnterState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeLabelExitState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeLabelHoverState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgePressedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgePressedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeClickedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeClickedMiddleState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeClickedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeDraggedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeEditEnterState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeEditExitState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeEditState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeEnterState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeExitState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeHoverState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodePressedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodePressedMiddleState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodePressedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Edge;
import y.base.Node;
import y.view.Bend;
import y.view.EdgeLabel;

import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Factory class for mouse state objects.
 * 
 * @param <NodeType> Type of the nodes in the graph.
 * @param <EdgeType> Type of the edges in the graph.
 */
public final class CStateFactory<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>> {
  /**
   * The graph for which mouse states are processed.
   */
  private final AbstractZyGraph<NodeType, EdgeType> m_graph;

  /**
   * Listeners that are notified about state changes.
   */
  private final List<IZyEditModeListener<NodeType, EdgeType>> m_listeners;

  /**
   * Provides action objects that are executed on state changes.
   */
  private final IStateActionFactory<NodeType, EdgeType> m_factory;

  /**
   * Creates a new factory object.
   * 
   * @param graph The graph for which mouse states are processed.
   * @param listeners Listeners that are notified about state changes.
   * @param factory Provides action objects that are executed on state changes.
   */
  public CStateFactory(final AbstractZyGraph<NodeType, EdgeType> graph,
      final List<IZyEditModeListener<NodeType, EdgeType>> listeners,
      final IStateActionFactory<NodeType, EdgeType> factory) {
    m_graph = graph;
    m_listeners = listeners;
    m_factory = factory;
  }

  /**
   * Creates a new state object after the background has been clicked with the left mouse button
   * 
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createBackgroundClickedLeftState(final MouseEvent event) {
    final CBackgroundClickedLeftState<NodeType> state =
        new CBackgroundClickedLeftState<NodeType>(this, m_graph);

    m_factory.createBackgroundClickedLeftAction().execute(state, event);

    return state;
  }

  public IMouseState createBackgroundClickedRightState(final MouseEvent event) {
    final CBackgroundClickedRightState<NodeType> state =
        new CBackgroundClickedRightState<NodeType>(this, m_graph);

    m_factory.createBackgroundClickedRightAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object after the background has been dragged
   * 
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createBackgroundDraggedLeftState(final MouseEvent event) {
    final CBackgroundDraggedLeftState state = new CBackgroundDraggedLeftState(this, m_graph);

    m_factory.createBackgroundDraggedLeftAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object after the background has been dragged
   * 
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createBackgroundDraggedRightState(final MouseEvent event) {
    final CBackgroundDraggedRightState state = new CBackgroundDraggedRightState(this, m_graph);

    m_factory.createBackgroundDraggedRightAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when the background is pressed with the left mouse button.
   * 
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createBackgroundPressedLeftState(final MouseEvent event) {
    final CBackgroundPressedLeftState state = new CBackgroundPressedLeftState(this, m_graph);

    m_factory.createBackgroundPressedLeftAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when the background is pressed with the left mouse button.
   * 
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createBackgroundPressedRightState(final MouseEvent event) {
    final CBackgroundPressedRightState state = new CBackgroundPressedRightState(this, m_graph);

    m_factory.createBackgroundPressedRightAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when a bend is clicked with the left mouse button.
   * 
   * @param b The bend which is clicked.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createBendClickedLeftState(final Bend b, final MouseEvent event) {
    final CBendClickedLeftState state = new CBendClickedLeftState(this, m_graph, b);

    m_factory.createBendClickedLeftAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when the bend is entered
   * 
   * @param b The bend wich is entered.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createBendEnterState(final Bend b, final MouseEvent event) {
    final CBendEnterState state = new CBendEnterState(this, m_graph, b);

    m_factory.createBendEnterAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when a bend is exited.
   * 
   * @param b The bend which is exited.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createBendExitState(final Bend b, final MouseEvent event) {
    final CBendExitState state = new CBendExitState(this, m_graph, b);

    m_factory.createBendExitAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when the bend enters hover state.
   * 
   * @param b The bend in hover state.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createBendHoverState(final Bend b, final MouseEvent event) {
    final CBendHoverState state = new CBendHoverState(this, m_graph, b);

    m_factory.createBendHoverAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when the bend is pressed with the left mouse button.
   * 
   * @param b The bend pressed.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createBendPressedLeftState(final Bend b, final MouseEvent event) {
    final CBendPressedLeftState state = new CBendPressedLeftState(this, m_graph, b);

    m_factory.createBendPressedLeftAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new default state object.
   * 
   * @return The new default state object.
   */
  public IMouseState createDefaultState() {
    return new CDefaultState(this);
  }

  /**
   * Creates a new state event object when the edge is clicked with the left mouse button.
   * 
   * @param e The edge which has been clicked.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createEdgeClickedLeftState(final Edge e, final MouseEvent event) {
    final CEdgeClickedLeftState state = new CEdgeClickedLeftState(this, m_graph, e);

    m_factory.createEdgeClickedLeftAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state event object when the edge is clicked with the right mouse button.
   * 
   * @param e The edge which is clicked.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createEdgeClickedRightState(final Edge e, final MouseEvent event) {
    final CEdgeClickedRightState<NodeType, EdgeType> state =
        new CEdgeClickedRightState<NodeType, EdgeType>(this, m_graph, e);

    m_factory.createEdgeClickedRightAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state event object when the edge is entered.
   * 
   * @param e The edge which is entered.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createEdgeEnterState(final Edge e, final MouseEvent event) {
    final CEdgeEnterState state = new CEdgeEnterState(this, m_graph, e);

    m_factory.createEdgeEnterAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state event object when the edge is exited.
   * 
   * @param e The edge which is exited.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createEdgeExitState(final Edge e, final MouseEvent event) {
    final CEdgeExitState state = new CEdgeExitState(this, m_graph, e);

    m_factory.createEdgeExitAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state event object when the edge enters hover state.
   * 
   * @param e The edge which is in hover state.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createEdgeHoverState(final Edge e, final MouseEvent event) {
    final CEdgeHoverState state = new CEdgeHoverState(this, m_graph, e);

    m_factory.createEdgeHoverAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state event object when the edge label is entered.
   * 
   * @param l The edge label which is entered.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createEdgeLabelEnterState(final EdgeLabel l, final MouseEvent event) {
    final CEdgeLabelEnterState<NodeType, EdgeType> state =
        new CEdgeLabelEnterState<NodeType, EdgeType>(this, m_graph, l);

    m_factory.createEdgeLabelEnterAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when the edge label is exited.
   * 
   * @param l The edge label which is exited.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createEdgeLabelExitState(final EdgeLabel l, final MouseEvent event) {
    final CEdgeLabelExitState<NodeType, EdgeType> state =
        new CEdgeLabelExitState<NodeType, EdgeType>(this, m_graph, l);

    m_factory.createEdgeLabelExitAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when the edge label is in hover mode.
   * 
   * @param l The label which is in hover mode.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createEdgeLabelHoverState(final EdgeLabel l, final MouseEvent event) {
    final CEdgeLabelHoverState state = new CEdgeLabelHoverState(this, m_graph, l);

    m_factory.createEdgeLabelHoverAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when an edge is pressed with the left mouse button.
   * 
   * @param e The edge which was pressed.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createEdgePressedLeftState(final Edge e, final MouseEvent event) {
    final CEdgePressedLeftState state = new CEdgePressedLeftState(this, m_graph, e);

    m_factory.createEdgePressedLeftAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when an edge is pressed with the right mouse button.
   * 
   * @param e The edge which was pressed.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createEdgePressedRightState(final Edge e, final MouseEvent event) {
    final CEdgePressedRightState state = new CEdgePressedRightState(this, m_graph, e, event);

    m_factory.createEdgePressedRightAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when a node is clicked with the left mouse button.
   * 
   * @param n The node which was clicked.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodeClickedLeftState(final Node n, final MouseEvent event) {
    final CNodeClickedLeftState<NodeType, EdgeType> state =
        new CNodeClickedLeftState<NodeType, EdgeType>(this, m_graph, n);

    m_factory.createNodeClickedLeftAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when a node is clicked with the middle mouse button.
   * 
   * @param n The node which was clicked.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodeClickedMiddleState(final Node n, final MouseEvent event) {
    final CNodeClickedMiddleState state = new CNodeClickedMiddleState(this, m_graph, n);

    m_factory.createNodeClickedMiddleAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when the node is clicked with the right mouse button.
   * 
   * @param n The node which was clicked.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodeClickedRightState(final Node n, final MouseEvent event) {
    final CNodeClickedRightState<NodeType, EdgeType> state =
        new CNodeClickedRightState<NodeType, EdgeType>(this, m_graph, n);

    m_factory.createNodeClickedRightAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object when a node is dragged.
   * 
   * @param n The node which is dragged
   * @param xDist The x distance which the node will be dragged by.
   * @param yDist The y distance which the node will be dragged by.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodeDraggedLeftState(final Node n, final MouseEvent event,
      final double xDist, final double yDist) {
    final CNodeDraggedLeftState<NodeType, EdgeType> state =
        new CNodeDraggedLeftState<NodeType, EdgeType>(this, m_graph, n, event, xDist, yDist);

    m_factory.createNodeDraggedLeftAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object after we entered edit mode.
   * 
   * @param n The node which has entered edit mode.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodeEditEnterState(final Node n, final MouseEvent event) {
    final CNodeEditEnterState state = new CNodeEditEnterState(this, m_graph, n);

    m_factory.createNodeEditEnterAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object after we exit edit mode.
   * 
   * @param n The node which was in edit mode.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodeEditExitState(final Node n, final MouseEvent event) {
    final CNodeEditExitState state = new CNodeEditExitState(this, m_graph, n);

    m_factory.createNodeEditExitAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object after we enter edit mode.
   * 
   * @param n The node in edit mode
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodeEditState(final Node n, final MouseEvent event) {
    final CNodeEditState state = new CNodeEditState(this, m_graph, n);

    m_factory.createNodeEditAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object after the cursor enters a node.
   * 
   * @param n The node that was entered.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodeEnterState(final Node n, final MouseEvent event) {
    final CNodeEnterState<NodeType, EdgeType> state =
        new CNodeEnterState<NodeType, EdgeType>(this, m_graph, n);

    m_factory.createNodeEnterAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object after the cursor exits a node.
   * 
   * @param n The node that was exited.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodeExitState(final Node n, final MouseEvent event) {
    final CNodeExitState<NodeType, EdgeType> state =
        new CNodeExitState<NodeType, EdgeType>(this, m_graph, n);

    m_factory.createNodeExitAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object after the cursor hovers over a node.
   * 
   * @param n The node that was entered.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodeHoverState(final Node n, final MouseEvent event) {
    final CNodeHoverState<NodeType, EdgeType> state =
        new CNodeHoverState<NodeType, EdgeType>(this, m_graph, n);

    m_factory.createNodeHoverAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object after the mouse left button has been pressed.
   * 
   * @param n The node that was entered.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodePressedLeftState(final Node n, final MouseEvent event) {
    final CNodePressedLeftState<NodeType, EdgeType> state =
        new CNodePressedLeftState<NodeType, EdgeType>(this, m_graph, n, event);

    m_factory.createNodePressedLeftAction().execute(state, event);

    return state;
  }

  /**
   * Creates a new state object after the mouse middle button has been pressed.
   * 
   * @param n The node that was pressed.
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodePressedMiddleState(final Node n, final MouseEvent event) {
    final CNodePressedMiddleState state = new CNodePressedMiddleState(this, m_graph, n);

    m_factory.createNodePressedMiddleAction().execute(state, event);

    return state;
  }

  /**
   * 
   * @param n
   * @param event The mouse event that caused the state change.
   * 
   * @return The state object that describes the mouse state.
   */
  public IMouseState createNodePressedRightState(final Node n, final MouseEvent event) {
    final CNodePressedRightState state = new CNodePressedRightState(this, m_graph, n, event);

    m_factory.createNodePressedRightAction().execute(state, event);

    return state;
  }

  /**
   * gets all listeners.
   * 
   * @return the currently listening listeners.
   */
  public List<IZyEditModeListener<NodeType, EdgeType>> getListeners() {
    return m_listeners;
  }
}
