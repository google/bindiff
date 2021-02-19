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

package com.google.security.zynamics.zylib.yfileswrap.yfiles;

import com.google.common.base.Preconditions;

import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.geom.YPoint;
import y.layout.LayoutTool;
import y.view.Graph2D;
import y.view.hierarchy.HierarchyManager;

import java.awt.geom.Rectangle2D;

/**
 * Contains small helper functions for working with the yFiles graph library.
 */
public final class YHelpers {
  private YHelpers() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Closes a group node.
   * 
   * @param graph The graph the group node belongs to.
   * @param groupNode The group node to be closed.
   */
  public static void closeGroup(final Graph2D graph, final Node groupNode) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");
    Preconditions.checkNotNull(groupNode, "Error: Group node argument can not be null");

    final HierarchyManager hierarchy = graph.getHierarchyManager();

    final double w = graph.getWidth(groupNode);
    final double h = graph.getHeight(groupNode);

    final NodeList groupNodes = new NodeList();
    groupNodes.add(groupNode);

    graph.firePreEvent();
    for (final NodeCursor nc = groupNodes.nodes(); nc.ok(); nc.next()) {
      hierarchy.closeGroup(nc.node());
    }
    graph.firePostEvent();

    // if the node size has changed, delete source ports of out-edges
    // and target ports of in-edges to ensure that all edges still connect
    // to the node
    if ((w != graph.getWidth(groupNode)) || (h != graph.getHeight(groupNode))) {
      for (final EdgeCursor ec = groupNode.outEdges(); ec.ok(); ec.next()) {
        graph.setSourcePointRel(ec.edge(), YPoint.ORIGIN);
      }
      for (final EdgeCursor ec = groupNode.inEdges(); ec.ok(); ec.next()) {
        graph.setTargetPointRel(ec.edge(), YPoint.ORIGIN);
      }
    }

    graph.updateViews();

  }

  /**
   * Opens a folder node.
   * 
   * @param graph The graph the folder node belongs to.
   * @param folderNode The folder node to be opened.
   */
  public static void openFolder(final Graph2D graph, final Node folderNode) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");
    Preconditions.checkNotNull(folderNode, "Error: Folder node argument can not be null");

    final HierarchyManager hierarchy = graph.getHierarchyManager();

    final double w = graph.getWidth(folderNode);
    final double h = graph.getHeight(folderNode);

    final NodeList folderNodes = new NodeList();
    folderNodes.add(folderNode);

    graph.firePreEvent();

    for (final NodeCursor nc = folderNodes.nodes(); nc.ok(); nc.next()) {
      // get original location of folder node
      final Graph2D innerGraph = (Graph2D) hierarchy.getInnerGraph(nc.node());
      final YPoint folderP = graph.getLocation(nc.node());
      final NodeList innerNodes = new NodeList(innerGraph.nodes());

      hierarchy.openFolder(nc.node());

      // get new location of group node
      final Rectangle2D.Double gBox = graph.getRealizer(nc.node()).getBoundingBox();
      // move grouped nodes to former location of folder node
      LayoutTool.moveSubgraph(graph, innerNodes.nodes(), folderP.x - gBox.x, folderP.y - gBox.y);
    }
    graph.firePostEvent();

    graph.unselectAll();
    for (final NodeCursor nc = folderNodes.nodes(); nc.ok(); nc.next()) {
      graph.setSelected(nc.node(), true);
    }

    // if the node size has changed, delete source ports of out-edges
    // and target ports of in-edges to ensure that all edges still connect
    // to the node
    if ((w != graph.getWidth(folderNode)) || (h != graph.getHeight(folderNode))) {
      for (final EdgeCursor ec = folderNode.outEdges(); ec.ok(); ec.next()) {
        graph.setSourcePointRel(ec.edge(), YPoint.ORIGIN);
      }
      for (final EdgeCursor ec = folderNode.inEdges(); ec.ok(); ec.next()) {
        graph.setTargetPointRel(ec.edge(), YPoint.ORIGIN);
      }
    }

    graph.updateViews();
  }
}
