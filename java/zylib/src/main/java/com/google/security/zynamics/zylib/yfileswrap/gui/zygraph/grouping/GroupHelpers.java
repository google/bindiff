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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.grouping;

import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.view.Graph2D;

public class GroupHelpers {
  public static void expandParents(final IViewNode<?> node) {
    if (node.getParentGroup() != null) {
      expandParents(node.getParentGroup());
    }

    if (node instanceof IGroupNode<?, ?>) {
      final IGroupNode<?, ?> gnode = (IGroupNode<?, ?>) node;

      if (gnode.isCollapsed()) {
        gnode.setCollapsed(false);
      }
    }
  }

  public static void extractFolder(final Graph2D graph, final Node folderNode) {
    final Graph innerGraph = graph.getHierarchyManager().getInnerGraph(folderNode);
    final NodeList subNodes = new NodeList(innerGraph.nodes());
    graph.getHierarchyManager().unfoldSubgraph(innerGraph, subNodes);
  }

  public static void extractGroup(final Graph2D graph, final Node groupNode) {
    for (final NodeCursor nc = graph.getHierarchyManager().getChildren(groupNode);
        nc.ok();
        nc.next()) {
      graph
          .getHierarchyManager()
          .setParentNode(nc.node(), graph.getHierarchyManager().getParentNode(groupNode));
    }
  }

  public static boolean isExpanded(final IGroupNode<?, ?> group) {
    return !group.isCollapsed()
        && ((group.getParentGroup() == null) || isExpanded(group.getParentGroup()));
  }
}
