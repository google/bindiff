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

package com.google.security.zynamics.zylib.gui.zygraph.editmode;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundClickedLeftState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundClickedRightState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundDraggedLeftState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundDraggedRightState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundPressedLeftState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundPressedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendClickedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendEnterState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendExitState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendHoverState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendPressedLeftState;
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

/**
 * Interface for factory classes that create actions.
 */
public interface IStateActionFactory<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>> {
  /**
   * Creates a new Background Left Clicked action object.
   * 
   * @return The created object.
   */
  IStateAction<CBackgroundClickedLeftState<NodeType>> createBackgroundClickedLeftAction();

  /**
   * Creates a new Background Right Clicked action object.
   * 
   * @return The created object.
   */
  IStateAction<CBackgroundClickedRightState<NodeType>> createBackgroundClickedRightAction();

  /**
   * Creates a new Background Dragged action object.
   * 
   * @return The created object.
   */
  IStateAction<CBackgroundDraggedLeftState> createBackgroundDraggedLeftAction();

  /**
   * Creates a new Background Dragged action object.
   * 
   * @return The created object.
   */
  IStateAction<CBackgroundDraggedRightState> createBackgroundDraggedRightAction();

  /**
   * Creates a new Background Pressed action object.
   * 
   * @return The created object.
   */
  IStateAction<CBackgroundPressedLeftState> createBackgroundPressedLeftAction();

  IStateAction<CBackgroundPressedRightState> createBackgroundPressedRightAction();

  IStateAction<CBendClickedLeftState> createBendClickedLeftAction();

  /**
   * Creates a new Bend Enter action object.
   * 
   * @return The created object.
   */
  IStateAction<CBendEnterState> createBendEnterAction();

  /**
   * Creates a new Bend Exit action object.
   * 
   * @return The created object.
   */
  IStateAction<CBendExitState> createBendExitAction();

  IStateAction<CBendHoverState> createBendHoverAction();

  IStateAction<CBendPressedLeftState> createBendPressedLeftAction();

  IStateAction<CEdgeClickedLeftState> createEdgeClickedLeftAction();

  IStateAction<CEdgeClickedRightState<NodeType, EdgeType>> createEdgeClickedRightAction();

  /**
   * Creates a new Edge Enter action object.
   * 
   * @return The created object.
   */
  IStateAction<CEdgeEnterState> createEdgeEnterAction();

  /**
   * Creates a new Edge Exit action object.
   * 
   * @return The created object.
   */
  IStateAction<CEdgeExitState> createEdgeExitAction();

  /**
   * Creates a new Edge Hover action object.
   * 
   * @return The created object.
   */
  IStateAction<CEdgeHoverState> createEdgeHoverAction();

  /**
   * Creates a new EdgeLabel Entered action object.
   * 
   * @return the created object.
   */

  IStateAction<CEdgeLabelEnterState<NodeType, EdgeType>> createEdgeLabelEnterAction();

  /**
   * Creates a new EdgeLabel Exit action object.
   * 
   * @return The created object.
   */
  IStateAction<CEdgeLabelExitState<NodeType, EdgeType>> createEdgeLabelExitAction();

  IStateAction<CEdgeLabelHoverState> createEdgeLabelHoverAction();

  IStateAction<CEdgePressedLeftState> createEdgePressedLeftAction();

  IStateAction<CEdgePressedRightState> createEdgePressedRightAction();

  /**
   * Creates a new Node Clicked Left action object.
   * 
   * @return The created object.
   */
  IStateAction<CNodeClickedLeftState<NodeType, EdgeType>> createNodeClickedLeftAction();

  IStateAction<CNodeClickedMiddleState> createNodeClickedMiddleAction();

  /**
   * Creates a new Node Clicked Right action object.
   * 
   * @return The created object.
   */
  IStateAction<CNodeClickedRightState<NodeType, EdgeType>> createNodeClickedRightAction();

  /**
   * Creates a new Node Dragged Left action object.
   * 
   * @return The created object.
   */
  IStateAction<CNodeDraggedLeftState<NodeType, EdgeType>> createNodeDraggedLeftAction();

  IStateAction<CNodeEditState> createNodeEditAction();

  IStateAction<CNodeEditEnterState> createNodeEditEnterAction();

  IStateAction<CNodeEditExitState> createNodeEditExitAction();

  /**
   * Creates a new Node Enter action object.
   * 
   * @return The created object.
   */
  IStateAction<CNodeEnterState<NodeType, EdgeType>> createNodeEnterAction();

  /**
   * Creates a new Node Exit action object.
   * 
   * @return The created object.
   */
  IStateAction<CNodeExitState<NodeType, EdgeType>> createNodeExitAction();

  /**
   * Creates a new Node Hover action object.
   * 
   * @return The created object.
   */
  IStateAction<CNodeHoverState<NodeType, EdgeType>> createNodeHoverAction();

  /**
   * Creates a new Node Pressed Left action object.
   * 
   * @return The created object.
   */
  IStateAction<CNodePressedLeftState<NodeType, EdgeType>> createNodePressedLeftAction();

  IStateAction<CNodePressedMiddleState> createNodePressedMiddleAction();

  /**
   * Creates a new Node Pressed Right action object.
   * 
   * @return The created object.
   */
  IStateAction<CNodePressedRightState> createNodePressedRightAction();
}
