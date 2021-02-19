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

import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBackgroundClickedLeftAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBackgroundClickedRightAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBackgroundDraggedLeftAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBackgroundDraggedRightAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBackgroundPressedLeftAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBackgroundPressedRightAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBendClickedLeftAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBendEnterAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBendExitAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBendHoverAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultBendPressedLeftAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultEdgeClickedLeftAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultEdgeClickedRightAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultEdgeHoverAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultEdgeLabelHoverAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultEdgePressedLeftAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultEdgePressedRightAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultNodeClickedMiddleAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultNodeEditAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultNodeEditEnterAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultNodeEditExitAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultNodeHoverAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultNodePressedLeftAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultNodePressedMiddleAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.actions.CDefaultNodePressedRightAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundClickedLeftState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundClickedRightState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundDraggedLeftState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundDraggedRightState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundPressedLeftState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundPressedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultEdgeEnterAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultEdgeExitAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultEdgeLabelEnterAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultEdgeLabelExitAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultNodeClickedLeftAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultNodeClickedRightAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultNodeDraggedLeftAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultNodeEnterAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultNodeExitAction;
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
 * This class provides the default action handlers that are executed when mouse state changes are
 * executed.
 */
public class CDefaultActionFactory<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateActionFactory<NodeType, EdgeType> {
  @Override
  public IStateAction<CBackgroundClickedLeftState<NodeType>> createBackgroundClickedLeftAction() {
    return new CDefaultBackgroundClickedLeftAction<NodeType>();
  }

  @Override
  public IStateAction<CBackgroundClickedRightState<NodeType>> createBackgroundClickedRightAction() {
    return new CDefaultBackgroundClickedRightAction<NodeType>();
  }

  @Override
  public IStateAction<CBackgroundDraggedLeftState> createBackgroundDraggedLeftAction() {
    return new CDefaultBackgroundDraggedLeftAction();
  }

  @Override
  public IStateAction<CBackgroundDraggedRightState> createBackgroundDraggedRightAction() {
    return new CDefaultBackgroundDraggedRightAction();
  }

  @Override
  public IStateAction<CBackgroundPressedLeftState> createBackgroundPressedLeftAction() {
    return new CDefaultBackgroundPressedLeftAction();
  }

  @Override
  public IStateAction<CBackgroundPressedRightState> createBackgroundPressedRightAction() {
    return new CDefaultBackgroundPressedRightAction();
  }

  @Override
  public IStateAction<CBendClickedLeftState> createBendClickedLeftAction() {
    return new CDefaultBendClickedLeftAction();
  }

  @Override
  public IStateAction<CBendEnterState> createBendEnterAction() {
    return new CDefaultBendEnterAction();
  }

  @Override
  public IStateAction<CBendExitState> createBendExitAction() {
    return new CDefaultBendExitAction();
  }

  @Override
  public IStateAction<CBendHoverState> createBendHoverAction() {
    return new CDefaultBendHoverAction();
  }

  @Override
  public IStateAction<CBendPressedLeftState> createBendPressedLeftAction() {
    return new CDefaultBendPressedLeftAction();
  }

  @Override
  public IStateAction<CEdgeClickedLeftState> createEdgeClickedLeftAction() {
    return new CDefaultEdgeClickedLeftAction();
  }

  @Override
  public IStateAction<CEdgeClickedRightState<NodeType, EdgeType>> createEdgeClickedRightAction() {
    return new CDefaultEdgeClickedRightAction<NodeType, EdgeType>();
  }

  @Override
  public IStateAction<CEdgeEnterState> createEdgeEnterAction() {
    return new CDefaultEdgeEnterAction();
  }

  @Override
  public IStateAction<CEdgeExitState> createEdgeExitAction() {
    return new CDefaultEdgeExitAction();
  }

  @Override
  public IStateAction<CEdgeHoverState> createEdgeHoverAction() {
    return new CDefaultEdgeHoverAction();
  }

  @Override
  public IStateAction<CEdgeLabelEnterState<NodeType, EdgeType>> createEdgeLabelEnterAction() {
    return new CDefaultEdgeLabelEnterAction<NodeType, EdgeType>();
  }

  @Override
  public IStateAction<CEdgeLabelExitState<NodeType, EdgeType>> createEdgeLabelExitAction() {
    return new CDefaultEdgeLabelExitAction<NodeType, EdgeType>();
  }

  @Override
  public IStateAction<CEdgeLabelHoverState> createEdgeLabelHoverAction() {
    return new CDefaultEdgeLabelHoverAction();
  }

  @Override
  public IStateAction<CEdgePressedLeftState> createEdgePressedLeftAction() {
    return new CDefaultEdgePressedLeftAction();
  }

  @Override
  public IStateAction<CEdgePressedRightState> createEdgePressedRightAction() {
    return new CDefaultEdgePressedRightAction();
  }

  @Override
  public IStateAction<CNodeClickedLeftState<NodeType, EdgeType>> createNodeClickedLeftAction() {
    return new CDefaultNodeClickedLeftAction<NodeType, EdgeType>();
  }

  @Override
  public IStateAction<CNodeClickedMiddleState> createNodeClickedMiddleAction() {
    return new CDefaultNodeClickedMiddleAction();
  }

  @Override
  public IStateAction<CNodeClickedRightState<NodeType, EdgeType>> createNodeClickedRightAction() {
    return new CDefaultNodeClickedRightAction<NodeType, EdgeType>();
  }

  @Override
  public IStateAction<CNodeDraggedLeftState<NodeType, EdgeType>> createNodeDraggedLeftAction() {
    return new CDefaultNodeDraggedLeftAction<NodeType, EdgeType>();
  }

  @Override
  public IStateAction<CNodeEditState> createNodeEditAction() {
    return new CDefaultNodeEditAction();
  }

  @Override
  public IStateAction<CNodeEditEnterState> createNodeEditEnterAction() {
    return new CDefaultNodeEditEnterAction();
  }

  @Override
  public IStateAction<CNodeEditExitState> createNodeEditExitAction() {
    return new CDefaultNodeEditExitAction();
  }

  @Override
  public IStateAction<CNodeEnterState<NodeType, EdgeType>> createNodeEnterAction() {
    return new CDefaultNodeEnterAction<NodeType, EdgeType>();
  }

  @Override
  public IStateAction<CNodeExitState<NodeType, EdgeType>> createNodeExitAction() {
    return new CDefaultNodeExitAction<NodeType, EdgeType>();
  }

  @Override
  public IStateAction<CNodeHoverState<NodeType, EdgeType>> createNodeHoverAction() {
    return new CDefaultNodeHoverAction<NodeType, EdgeType>();
  }

  @Override
  public IStateAction<CNodePressedLeftState<NodeType, EdgeType>> createNodePressedLeftAction() {
    return new CDefaultNodePressedLeftAction<NodeType, EdgeType>();
  }

  @Override
  public IStateAction<CNodePressedMiddleState> createNodePressedMiddleAction() {
    return new CDefaultNodePressedMiddleAction();
  }

  @Override
  public IStateAction<CNodePressedRightState> createNodePressedRightAction() {
    return new CDefaultNodePressedRightAction();
  }
}
