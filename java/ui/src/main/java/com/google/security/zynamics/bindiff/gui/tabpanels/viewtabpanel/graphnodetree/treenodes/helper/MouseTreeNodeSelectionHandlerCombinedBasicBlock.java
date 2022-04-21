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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.helper;

import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.flowgraph.CombinedFlowGraphBaseTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.flowgraph.CombinedFlowGraphBasicBlockTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.flowgraph.CombinedFlowGraphRootTreeNode;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MouseTreeNodeSelectionHandlerCombinedBasicBlock {
  private static void selectGraphNode(final CombinedFlowGraphBasicBlockTreeNode clickedNode) {
    final CombinedFlowGraphRootTreeNode rootTreeNode = clickedNode.getRootNode();
    final CombinedFlowGraphBaseTreeNode baseTreeNode =
        (CombinedFlowGraphBaseTreeNode) clickedNode.getParent();

    final CombinedGraph combinedGraph = rootTreeNode.getGraph();
    final CombinedDiffNode combinedGraphNode = clickedNode.getCombinedDiffNode();

    final Collection<CombinedDiffNode> selectedGraphNodes = new ArrayList<>();
    final Collection<CombinedDiffNode> unselectedGraphNodes = new ArrayList<>();

    for (final CombinedDiffNode graphNode : combinedGraph.getNodes()) {
      unselectedGraphNodes.add(graphNode);
    }

    if (!combinedGraphNode.isSelected()) {
      baseTreeNode.setLastSelectedGraphNode(combinedGraphNode);

      unselectedGraphNodes.remove(combinedGraphNode);
      selectedGraphNodes.add(combinedGraphNode);
    } else {
      baseTreeNode.setLastSelectedGraphNode(null);
    }

    combinedGraph.selectNodes(selectedGraphNodes, unselectedGraphNodes);
  }

  private static void selectGraphNodeCtrl(final CombinedFlowGraphBasicBlockTreeNode clickedNode) {
    final CombinedFlowGraphRootTreeNode rootTreeNode = clickedNode.getRootNode();
    final CombinedFlowGraphBaseTreeNode baseTreeNode =
        (CombinedFlowGraphBaseTreeNode) clickedNode.getParent();

    final CombinedGraph combinedGraph = rootTreeNode.getGraph();
    final CombinedDiffNode combinedGraphNode = clickedNode.getCombinedDiffNode();

    final Collection<CombinedDiffNode> selectedGraphNodes = new ArrayList<>();
    final Collection<CombinedDiffNode> unselectedGraphNodes = new ArrayList<>();

    for (final CombinedDiffNode graphNode : combinedGraph.getNodes()) {
      if (graphNode.isSelected()) {
        selectedGraphNodes.add(graphNode);
      } else {
        unselectedGraphNodes.add(graphNode);
      }
    }

    if (!combinedGraphNode.isSelected()) {
      baseTreeNode.setLastSelectedGraphNode(combinedGraphNode);

      selectedGraphNodes.add(combinedGraphNode);
      unselectedGraphNodes.remove(combinedGraphNode);
    } else {
      baseTreeNode.setLastSelectedGraphNode(null);

      unselectedGraphNodes.add(combinedGraphNode);
      selectedGraphNodes.remove(combinedGraphNode);
    }

    combinedGraph.selectNodes(selectedGraphNodes, unselectedGraphNodes);
  }

  private static void selectGraphNodeCtrlShift(
      final CombinedFlowGraphBasicBlockTreeNode clickedNode) {
    final CombinedFlowGraphRootTreeNode rootTreeNode = clickedNode.getRootNode();
    final CombinedFlowGraphBaseTreeNode baseTreeNode =
        (CombinedFlowGraphBaseTreeNode) clickedNode.getParent();

    final CombinedGraph combinedGraph = rootTreeNode.getGraph();
    final CombinedDiffNode combinedGraphNode = clickedNode.getCombinedDiffNode();

    final Collection<CombinedDiffNode> selectedGraphNodes = new ArrayList<>();
    final Collection<CombinedDiffNode> unselectedGraphNodes = new ArrayList<>();

    final CombinedDiffNode lastSelectedGraphNode = baseTreeNode.getLastSelectedGraphNode();

    if (lastSelectedGraphNode == null) {
      selectGraphNode(clickedNode);
    } else if (combinedGraphNode.equals(lastSelectedGraphNode)) {
      selectGraphNodeCtrl(clickedNode);
    } else {
      for (final CombinedDiffNode graphNode : combinedGraph.getNodes()) {
        if (graphNode.isSelected()) {
          selectedGraphNodes.add(graphNode);
        } else {
          unselectedGraphNodes.add(graphNode);
        }
      }

      final List<CombinedDiffNode> nodesToSelect = new ArrayList<>();

      boolean started = false;

      for (int i = 1; i < baseTreeNode.getChildCount(); ++i) {
        final CombinedFlowGraphBasicBlockTreeNode treeNode =
            (CombinedFlowGraphBasicBlockTreeNode) baseTreeNode.getChildAt(i);

        final CombinedDiffNode graphNode = treeNode.getCombinedDiffNode();

        if (graphNode.equals(lastSelectedGraphNode)) {
          started = true;

          if (nodesToSelect.size() != 0) {
            nodesToSelect.add(treeNode.getCombinedDiffNode());
            break;
          }
        }

        if (graphNode.equals(combinedGraphNode)) {
          started = true;

          if (nodesToSelect.size() != 0) {
            nodesToSelect.add(treeNode.getCombinedDiffNode());
            break;
          }
        }

        if (started) {
          nodesToSelect.add(treeNode.getCombinedDiffNode());
        }
      }

      for (final CombinedDiffNode node : nodesToSelect) {
        selectedGraphNodes.add(node);
        unselectedGraphNodes.remove(node);
      }

      combinedGraph.selectNodes(selectedGraphNodes, unselectedGraphNodes);
    }
  }

  private static void selectGraphNodeShift(final CombinedFlowGraphBasicBlockTreeNode clickedNode) {
    final CombinedFlowGraphRootTreeNode rootTreeNode = clickedNode.getRootNode();
    final CombinedFlowGraphBaseTreeNode baseTreeNode =
        (CombinedFlowGraphBaseTreeNode) clickedNode.getParent();

    final CombinedGraph combinedGraph = rootTreeNode.getGraph();
    final CombinedDiffNode combinedGraphNode = clickedNode.getCombinedDiffNode();

    final Collection<CombinedDiffNode> selectedGraphNodes = new ArrayList<>();
    final Collection<CombinedDiffNode> unselectedGraphNodes = new ArrayList<>();

    final CombinedDiffNode lastSelectedGraphNode = baseTreeNode.getLastSelectedGraphNode();

    selectedGraphNodes.clear();
    unselectedGraphNodes.clear();

    if (lastSelectedGraphNode == null || combinedGraphNode.equals(lastSelectedGraphNode)) {
      selectGraphNode(clickedNode);
    } else {
      for (final CombinedDiffNode graphNode : combinedGraph.getNodes()) {
        unselectedGraphNodes.add(graphNode);
      }

      final List<CombinedDiffNode> nodesToSelect = new ArrayList<>();

      boolean started = false;

      for (int i = 1; i < baseTreeNode.getChildCount(); ++i) {
        final CombinedFlowGraphBasicBlockTreeNode treeNode =
            (CombinedFlowGraphBasicBlockTreeNode) baseTreeNode.getChildAt(i);

        final CombinedDiffNode combinedDiffNode = treeNode.getCombinedDiffNode();

        if (combinedDiffNode.equals(lastSelectedGraphNode)) {
          started = true;

          if (nodesToSelect.size() != 0) {
            nodesToSelect.add(treeNode.getCombinedDiffNode());
            break;
          }
        }

        if (combinedDiffNode.equals(combinedGraphNode)) {
          started = true;

          if (nodesToSelect.size() != 0) {
            nodesToSelect.add(treeNode.getCombinedDiffNode());
            break;
          }
        }

        if (started) {
          nodesToSelect.add(treeNode.getCombinedDiffNode());
        }
      }

      for (final CombinedDiffNode node : nodesToSelect) {
        selectedGraphNodes.add(node);
        unselectedGraphNodes.remove(node);
      }

      combinedGraph.selectNodes(selectedGraphNodes, unselectedGraphNodes);
    }
  }

  // Note: This is NOT optimal. Probably unnecessary code blow up. Unify with other
  // handleMouseSelectionEvent method if time.
  public static void handleMouseSelectionEvent(
      final MouseEvent event, final CombinedFlowGraphBasicBlockTreeNode clickedNode) {
    final boolean shift = event.isShiftDown();
    final boolean ctrl = event.isControlDown();
    final boolean pressed = event.getID() == MouseEvent.MOUSE_PRESSED;
    final boolean released = event.getID() == MouseEvent.MOUSE_RELEASED;

    if (pressed && shift && ctrl) {
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
