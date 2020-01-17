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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.helper;

import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.single.flowgraph.SingleFlowGraphBaseTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.single.flowgraph.SingleFlowGraphBasicBlockTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.single.flowgraph.SingleFlowGraphRootTreeNode;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MouseTreeNodeSelectionHandlerSingleBasicBlock {

  private static void selectGraphNode(final SingleFlowGraphBasicBlockTreeNode clickedNode) {
    final SingleFlowGraphRootTreeNode rootTreeNode = clickedNode.getRootNode();
    final SingleFlowGraphBaseTreeNode baseTreeNode =
        (SingleFlowGraphBaseTreeNode) clickedNode.getParent();

    final SingleGraph singleGraph = rootTreeNode.getGraph();
    final SingleDiffNode singleGraphNode = clickedNode.getSingleDiffNode();

    final Collection<SingleDiffNode> selectedGraphNodes = new ArrayList<>();
    final Collection<SingleDiffNode> unselectedGraphNodes = new ArrayList<>();

    for (final SingleDiffNode graphNode : singleGraph.getNodes()) {
      unselectedGraphNodes.add(graphNode);
    }

    if (!singleGraphNode.isSelected()) {
      baseTreeNode.setLastSelectedGraphNode(singleGraphNode);

      unselectedGraphNodes.remove(singleGraphNode);
      selectedGraphNodes.add(singleGraphNode);
    } else {
      baseTreeNode.setLastSelectedGraphNode(null);
    }

    singleGraph.selectNodes(selectedGraphNodes, unselectedGraphNodes);
  }

  private static void selectGraphNodeCtrl(final SingleFlowGraphBasicBlockTreeNode clickedNode) {
    final SingleFlowGraphRootTreeNode rootTreeNode = clickedNode.getRootNode();
    final SingleFlowGraphBaseTreeNode baseTreeNode =
        (SingleFlowGraphBaseTreeNode) clickedNode.getParent();

    final SingleGraph singleGraph = rootTreeNode.getGraph();
    final SingleDiffNode singleGraphNode = clickedNode.getSingleDiffNode();

    final Collection<SingleDiffNode> selectedGraphNodes = new ArrayList<>();
    final Collection<SingleDiffNode> unselectedGraphNodes = new ArrayList<>();

    for (final SingleDiffNode graphNode : singleGraph.getNodes()) {
      if (graphNode.isSelected()) {
        selectedGraphNodes.add(graphNode);
      } else {
        unselectedGraphNodes.add(graphNode);
      }
    }

    if (!singleGraphNode.isSelected()) {
      baseTreeNode.setLastSelectedGraphNode(singleGraphNode);

      selectedGraphNodes.add(singleGraphNode);
      unselectedGraphNodes.remove(singleGraphNode);
    } else {
      baseTreeNode.setLastSelectedGraphNode(null);

      unselectedGraphNodes.add(singleGraphNode);
      selectedGraphNodes.remove(singleGraphNode);
    }

    singleGraph.selectNodes(selectedGraphNodes, unselectedGraphNodes);
  }

  private static void selectGraphNodeCtrlShift(
      final SingleFlowGraphBasicBlockTreeNode clickedNode) {
    final SingleFlowGraphRootTreeNode rootTreeNode = clickedNode.getRootNode();
    final SingleFlowGraphBaseTreeNode baseTreeNode =
        (SingleFlowGraphBaseTreeNode) clickedNode.getParent();

    final SingleGraph singleGraph = rootTreeNode.getGraph();
    final SingleDiffNode singleGraphNode = clickedNode.getSingleDiffNode();

    final Collection<SingleDiffNode> selectedGraphNodes = new ArrayList<>();
    final Collection<SingleDiffNode> unselectedGraphNodes = new ArrayList<>();

    final SingleDiffNode lastSelectedGraphNode = baseTreeNode.getLastSelectedGraphNode();

    if (lastSelectedGraphNode == null) {
      selectGraphNode(clickedNode);
    } else if (singleGraphNode.equals(lastSelectedGraphNode)) {
      selectGraphNodeCtrl(clickedNode);
    } else {
      for (final SingleDiffNode graphNode : singleGraph.getNodes()) {
        if (graphNode.isSelected()) {
          selectedGraphNodes.add(graphNode);
        } else {
          unselectedGraphNodes.add(graphNode);
        }
      }

      final List<SingleDiffNode> nodesToSelect = new ArrayList<>();

      boolean started = false;

      for (int i = 1; i < baseTreeNode.getChildCount(); ++i) {
        final SingleFlowGraphBasicBlockTreeNode treeNode =
            (SingleFlowGraphBasicBlockTreeNode) baseTreeNode.getChildAt(i);

        final SingleDiffNode graphNode = treeNode.getSingleDiffNode();

        if (graphNode.equals(lastSelectedGraphNode)) {
          started = true;

          if (nodesToSelect.size() != 0) {
            nodesToSelect.add(treeNode.getSingleDiffNode());
            break;
          }
        }

        if (graphNode.equals(singleGraphNode)) {
          started = true;

          if (nodesToSelect.size() != 0) {
            nodesToSelect.add(treeNode.getSingleDiffNode());
            break;
          }
        }

        if (started) {
          nodesToSelect.add(treeNode.getSingleDiffNode());
        }
      }

      for (final SingleDiffNode node : nodesToSelect) {
        selectedGraphNodes.add(node);
        unselectedGraphNodes.remove(node);
      }

      singleGraph.selectNodes(selectedGraphNodes, unselectedGraphNodes);
    }
  }

  private static void selectGraphNodeShift(final SingleFlowGraphBasicBlockTreeNode clickedNode) {
    final SingleFlowGraphRootTreeNode rootTreeNode = clickedNode.getRootNode();
    final SingleFlowGraphBaseTreeNode baseTreeNode =
        (SingleFlowGraphBaseTreeNode) clickedNode.getParent();

    final SingleGraph singleGraph = rootTreeNode.getGraph();
    final SingleDiffNode singleGraphNode = clickedNode.getSingleDiffNode();

    final Collection<SingleDiffNode> selectedGraphNodes = new ArrayList<>();
    final Collection<SingleDiffNode> unselectedGraphNodes = new ArrayList<>();

    final SingleDiffNode lastSelectedGraphNode = baseTreeNode.getLastSelectedGraphNode();

    selectedGraphNodes.clear();
    unselectedGraphNodes.clear();

    if (lastSelectedGraphNode == null || singleGraphNode.equals(lastSelectedGraphNode)) {
      selectGraphNode(clickedNode);
    } else {
      for (final SingleDiffNode graphNode : singleGraph.getNodes()) {
        unselectedGraphNodes.add(graphNode);
      }

      final List<SingleDiffNode> nodesToSelect = new ArrayList<>();

      boolean started = false;

      for (int i = 1; i < baseTreeNode.getChildCount(); ++i) {
        final SingleFlowGraphBasicBlockTreeNode treeNode =
            (SingleFlowGraphBasicBlockTreeNode) baseTreeNode.getChildAt(i);

        final SingleDiffNode singleDiffNode = treeNode.getSingleDiffNode();

        if (singleDiffNode.equals(lastSelectedGraphNode)) {
          started = true;

          if (nodesToSelect.size() != 0) {
            nodesToSelect.add(treeNode.getSingleDiffNode());
            break;
          }
        }

        if (singleDiffNode.equals(singleGraphNode)) {
          started = true;

          if (nodesToSelect.size() != 0) {
            nodesToSelect.add(treeNode.getSingleDiffNode());
            break;
          }
        }

        if (started) {
          nodesToSelect.add(treeNode.getSingleDiffNode());
        }
      }

      for (final SingleDiffNode node : nodesToSelect) {
        selectedGraphNodes.add(node);
        unselectedGraphNodes.remove(node);
      }

      singleGraph.selectNodes(selectedGraphNodes, unselectedGraphNodes);
    }
  }

  public static void handleMouseSelectionEvent(
      final MouseEvent event, final SingleFlowGraphBasicBlockTreeNode clickedNode) {
    final boolean shift = event.isShiftDown();
    final boolean ctrl = event.isControlDown();
    final boolean pressed = event.getID() == MouseEvent.MOUSE_PRESSED;
    final boolean released = event.getID() == MouseEvent.MOUSE_RELEASED;

    if (pressed && ctrl && shift) {
      selectGraphNodeCtrlShift(clickedNode);
    } else if (released && ctrl && !shift) {
      selectGraphNodeCtrl(clickedNode);
    } else if (pressed && !ctrl && shift) {
      selectGraphNodeShift(clickedNode);
    } else if (pressed && !ctrl && !shift) {
      selectGraphNode(clickedNode);
    }
  }
}
