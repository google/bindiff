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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.gui.zygraph.IFineGrainedSloppyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyGroupNodeRealizer;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;
import y.view.hierarchy.HierarchyManager;


public class ZyGraphLayeredRenderer<ViewType extends IFineGrainedSloppyGraph2DView> extends
    ZyGraphFineGrainedRenderer<ViewType> {
  private Node m_node = null;

  public ZyGraphLayeredRenderer(final ViewType inputView) {
    super(inputView);
    setLayeredPainting(true);
  }

  /**
   * Determines whether any of the parent nodes of the given node is selected.
   */
  private boolean isAnyParentNodeSelected(final Node n) {
    final Graph2D graph = (Graph2D) n.getGraph();
    final HierarchyManager hierarchy = graph.getHierarchyManager();

    if (hierarchy == null) {
      return false;
    }

    boolean result = false;
    Node parent = hierarchy.getParentNode(n);
    while (parent != null) {
      if (graph.isSelected(parent)) {
        result = true;
        break;
      }
      parent = hierarchy.getParentNode(parent);
    }
    return result;
  }

  @Override
  protected int getLayer(final Graph2D graph, final Edge edge) {
    return 2;
  }

  @Override
  protected int getLayer(final Graph2D graph, final Node node) {
    final boolean isGroupNode = graph.getRealizer(node) instanceof ZyGroupNodeRealizer<?>;
    if ((graph.isSelected(node) || isAnyParentNodeSelected(node)) && !isGroupNode) {
      return 3;
    } else if ((m_node == node) && !isGroupNode) {
      return 4;
    } else if (isGroupNode) {
      return 1;
    } else {
      return 2;
    }
  }

  public void bringNodeToFront(final Node node) {
    m_node = node;
  }
}
