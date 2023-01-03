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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyInfoEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyProximityNodeRealizer;
import java.awt.Font;
import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;
import y.view.LineType;

/** This class is used to create proximity browsing nodes and edges. */
public final class ProximityNodeCreator {
  /**
   * Creates a proximity browsing node.
   *
   * @param graph The graph where the proximity node is added to.
   * @param attachedNode The graph node the proximity node is attached to.
   * @param degree The edge degree of the attached node (this is the number shown in the proximity
   *     node).
   * @param isIncoming True, to signal that the proximity node is incoming. False, if it is
   *     outcoming.
   * @param <NodeType> Raw node type of the real (e.g. not proximity nodes) nodes in the graph.
   * @return The created proximity node.
   */
  public static <NodeType extends IViewNode<?>> ZyProximityNode<?> createProximityNode(
      final Graph2D graph,
      final ZyGraphNode<?> attachedNode,
      final int degree,
      final boolean isIncoming) {
    Preconditions.checkNotNull(graph, "Graph argument can not be null");
    Preconditions.checkNotNull(attachedNode, "Target node argument can not be null");

    final ZyLabelContent labelcontent = new ZyLabelContent(null);
    labelcontent.addLineContent(
        new ZyLineContent(String.valueOf(degree), new Font("New Courier", Font.PLAIN, 12), null));

    final ZyProximityNodeRealizer<NodeType> r = new ZyProximityNodeRealizer<NodeType>(labelcontent);

    final Node node = graph.createNode(r);

    @SuppressWarnings("unchecked")
    final ZyProximityNode<NodeType> infoNode =
        new ZyProximityNode<NodeType>(node, r, (ZyGraphNode<NodeType>) attachedNode, isIncoming);

    final ZyNodeData<ZyProximityNode<NodeType>> data =
        new ZyNodeData<ZyProximityNode<NodeType>>(infoNode);
    r.setUserData(data);

    return infoNode;
  }

  /**
   * Inserts a proximity edge between two nodes. One of the two input nodes must be a proximity
   * browsing node or an exception is thrown.
   *
   * @param graph The graph where the node is inserted.
   * @param sourceNode The source node of the edge.
   * @param targetNode The target node of the edge.
   * @return The inserted edge.
   */
  public static ZyInfoEdge insertProximityEdge(
      final Graph2D graph, final ZyGraphNode<?> sourceNode, final ZyGraphNode<?> targetNode) {
    Preconditions.checkNotNull(graph, "Graph argument can not be null");
    Preconditions.checkNotNull(sourceNode, "Source node argument can not be null");
    Preconditions.checkNotNull(targetNode, "Target node argument can not be null");

    Preconditions.checkArgument(
        (sourceNode instanceof ZyProximityNode<?>) || (targetNode instanceof ZyProximityNode<?>),
        "One of the two arguments must be a proximity browsing node");

    final ZyEdgeRealizer<ZyInfoEdge> r =
        new ZyEdgeRealizer<ZyInfoEdge>(new ZyLabelContent(null), null);
    r.setLineType(LineType.LINE_2);

    final Edge edge = graph.createEdge(sourceNode.getNode(), targetNode.getNode(), r);

    final ZyInfoEdge infoEdge = new ZyInfoEdge(sourceNode, targetNode, edge, r);

    final ZyEdgeData<ZyInfoEdge> data = new ZyEdgeData<ZyInfoEdge>(infoEdge);

    r.setUserData(data);

    return infoEdge;
  }
}
