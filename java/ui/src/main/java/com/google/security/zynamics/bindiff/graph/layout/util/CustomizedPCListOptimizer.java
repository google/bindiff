// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.bindiff.graph.layout.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.ListCell;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.geom.YPoint;
import y.layout.LayoutGraph;
import y.layout.PortCandidate;
import y.layout.PortConstraint;
import y.layout.hierarchic.incremental.EdgeData;
import y.layout.hierarchic.incremental.ItemFactory;
import y.layout.hierarchic.incremental.Layer;
import y.layout.hierarchic.incremental.Layers;
import y.layout.hierarchic.incremental.LayoutDataProvider;
import y.layout.hierarchic.incremental.NodeData;
import y.layout.hierarchic.incremental.PortConstraintOptimizer;
import y.util.Maps;

public class CustomizedPCListOptimizer implements PortConstraintOptimizer {
  private boolean backloopRouting = false;

  private static boolean isBackwardEdge(final Edge e, final NodeMap node2LayerId) {
    return node2LayerId.getInt(e.source()) > node2LayerId.getInt(e.target());
  }

  // assign the sorted edges to ports
  private void assignEdgePorts(
      final Node node,
      final EdgeList sortedEdges,
      final LayoutGraph graph,
      final boolean atSource,
      final ItemFactory itemFactory) {
    // split edges according to the three positions (left, middle and right)
    final EdgeList leftEdges = new EdgeList();
    final EdgeList middleEdges = new EdgeList();
    final EdgeList rightEdges = new EdgeList();

    for (final EdgeCursor ec = sortedEdges.edges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      final double xOffset =
          // we assigned this offset in method optimizeAfterLayering
          atSource ? graph.getSourcePointRel(e).getX() : graph.getTargetPointRel(e).getX();
      if (xOffset < 0) {
        leftEdges.add(e);
      } else if (xOffset > 0) {
        rightEdges.add(e);
      } else {
        middleEdges.add(e);
      }
    }

    // distribute edges
    final double halfNodeWidth = graph.getWidth(node) * 0.5;
    final double halfNodeWidthThird = halfNodeWidth / 3.0;
    distributeEdges(
        leftEdges, -halfNodeWidth, -halfNodeWidthThird, node, graph, itemFactory, atSource);
    distributeEdges(
        middleEdges, -halfNodeWidthThird, halfNodeWidthThird, node, graph, itemFactory, atSource);
    distributeEdges(
        rightEdges, halfNodeWidthThird, halfNodeWidth, node, graph, itemFactory, atSource);
  }

  // distribute edges between leftX and rightX
  private void distributeEdges(
      final EdgeList edges,
      final double leftX,
      final double rightX,
      final Node n,
      final LayoutGraph graph,
      final ItemFactory itemFactory,
      final boolean atSource) {
    final double halfHeight = graph.getHeight(n) * 0.5;
    final double stepWidth = (rightX - leftX) / (edges.size() + 1);
    double xOffset = leftX + stepWidth;
    for (final EdgeCursor ec = edges.edges(); ec.ok(); ec.next(), xOffset += stepWidth) {
      final Edge e = ec.edge();
      if (atSource) {
        graph.setSourcePointRel(e, new YPoint(xOffset, halfHeight));
      } else {
        graph.setTargetPointRel(e, new YPoint(xOffset, -halfHeight));
      }
      final byte side = atSource ? PortConstraint.SOUTH : PortConstraint.NORTH;
      itemFactory.setTemporaryPortConstraint(e, atSource, PortConstraint.create(side, true));
    }
  }

  // returns the first candidate with the given direction
  private PortCandidate getCandidateWithDirection(
      final Collection<PortCandidate> candidates, final byte direction) {
    if (candidates == null) {
      return null;
    }

    for (final PortCandidate cand : candidates) {
      if (cand.getDirection() == direction) {
        return cand;
      }
    }

    return null; // no candidate found
  }

  private PortConstraint transformToPortConstraint(
      final PortCandidate candidate,
      final Edge e,
      final boolean atSource,
      final LayoutGraph graph) {
    if (candidate.isFixed()) {
      // we have to update the source/target point
      final YPoint offset = new YPoint(candidate.getXOffset(), candidate.getYOffset());
      if (atSource) {
        graph.setSourcePointRel(e, offset);
      } else {
        graph.setTargetPointRel(e, offset);
      }
    }

    return candidate.toPortConstraint();
  }

  public boolean isBackloopRouting() {
    return backloopRouting;
  }

  @Override
  public void optimizeAfterLayering(
      final LayoutGraph graph,
      final Layers layers,
      final LayoutDataProvider ldp,
      final ItemFactory itemFactory) {
    // we create a node map that stores the layer ids of the nodes
    final NodeMap node2LayerID = Maps.createHashedNodeMap();
    for (int i = 0; i < layers.size(); i++) {
      final Layer layer = layers.getLayer(i);
      for (final NodeCursor nc = layer.getList().nodes(); nc.ok(); nc.next()) {
        node2LayerID.setInt(nc.node(), i);
      }
    }

    // now, we determine if we take the candidate of the top or bottom side
    for (final EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      if (!e.isSelfLoop()) {
        final EdgeData eData = ldp.getEdgeData(e);

        final byte sourceDirection =
            !backloopRouting && isBackwardEdge(e, node2LayerID)
                ? PortConstraint.NORTH
                : PortConstraint.SOUTH;
        @SuppressWarnings("unchecked")
        final PortCandidate sourceCand =
            getCandidateWithDirection(eData.getSourceCandidates(), sourceDirection);
        if (sourceCand != null) {
          // we take this candidate
          itemFactory.setTemporaryPortConstraint(
              e, true, transformToPortConstraint(sourceCand, e, true, graph));
        } else {
          itemFactory.setTemporaryPortConstraint(e, true, PortConstraint.create(sourceDirection));
        }

        final byte targetDirection =
            !backloopRouting && isBackwardEdge(e, node2LayerID)
                ? PortConstraint.SOUTH
                : PortConstraint.NORTH;
        @SuppressWarnings("unchecked")
        final PortCandidate targetCand =
            getCandidateWithDirection(eData.getTargetCandidates(), targetDirection);
        if (targetCand != null) {
          // we take this candidate
          itemFactory.setTemporaryPortConstraint(
              e, false, transformToPortConstraint(targetCand, e, false, graph));
        } else {
          itemFactory.setTemporaryPortConstraint(e, false, PortConstraint.create(targetDirection));
        }
      }
    }
  }

  @Override
  public void optimizeAfterSequencing(
      final LayoutGraph graph,
      final Layers layers,
      final LayoutDataProvider ldp,
      final ItemFactory itemFactory) {
    // insert same layer edges (backloops are modeled by means of same layer edges)
    final List<Node> sameLayerNodes = new ArrayList<>(64);
    final NodeMap node2IsSameLayerDummy = Maps.createHashedNodeMap();

    int sameLayerEdgeCount = 0;
    for (final NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final NodeData nd = ldp.getNodeData(nc.node());
      sameLayerEdgeCount += nd.sameLayerEdgeCount();
    }

    final int sameLayerEdgeOffset = graph.N();
    final Edge[] originalEdge = new Edge[sameLayerEdgeCount / 2];

    int maxDegree = 2;
    for (int layerIndex = 0; layerIndex < layers.size(); layerIndex++) {
      final Layer layer = layers.getLayer(layerIndex);
      for (ListCell cell = layer.getList().firstCell(); cell != null; cell = cell.succ()) {
        final Node node = (Node) cell.getInfo();
        final NodeData nd = ldp.getNodeData(node);

        // insert same layer edge construction for each same layer edge
        if (nd.sameLayerEdgeCount() > 0) {
          for (ListCell sameLayerEdgeCell = nd.getFirstSameLayerEdgeCell();
              sameLayerEdgeCell != null;
              sameLayerEdgeCell = sameLayerEdgeCell.succ()) {
            final Edge sameLayerEdge = (Edge) sameLayerEdgeCell.getInfo();
            // insert each same layer edge only once....
            if (sameLayerEdge.source() == node) {
              final Node sameLayerDummy = graph.createNode();
              node2IsSameLayerDummy.setBool(sameLayerDummy, true);
              final EdgeData sleData = ldp.getEdgeData(sameLayerEdge);
              sameLayerNodes.add(sameLayerDummy);
              if (sleData.isUpperSameLayerEdge()) {
                itemFactory.createReverseDummyEdge(
                    sameLayerDummy, node, sameLayerEdge, false, true);
                itemFactory.createDummyEdge(
                    sameLayerDummy, sameLayerEdge.target(), sameLayerEdge, false, true);
                maxDegree = Math.max(maxDegree, sameLayerEdge.target().inDegree());
              } else {
                itemFactory.createDummyEdge(node, sameLayerDummy, sameLayerEdge, true, false);
                itemFactory.createReverseDummyEdge(
                    sameLayerEdge.target(), sameLayerDummy, sameLayerEdge, true, false);
                maxDegree = Math.max(maxDegree, sameLayerEdge.target().outDegree());
              }
              maxDegree = Math.max(2, maxDegree);
              originalEdge[sameLayerDummy.index() - sameLayerEdgeOffset] = sameLayerEdge;
            }
          }
        }
        maxDegree = Math.max(maxDegree, Math.max(node.inDegree(), node.outDegree()));
      }
    }

    // for each node we assign the incoming edges to the top side and the outgoing edges to the
    // bottom side considering the specified position
    for (final NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      if (!node2IsSameLayerDummy.getBool(node)
          && ldp.getNodeData(node).getType() == NodeData.TYPE_NORMAL) {
        final EdgeList incomingEdges = new EdgeList(node.inEdges());
        incomingEdges.sort(new EdgeOrderComparator(node2IsSameLayerDummy, node, ldp));
        assignEdgePorts(node, incomingEdges, graph, false, itemFactory);

        final EdgeList outgoingEdges = new EdgeList(node.outEdges());
        outgoingEdges.sort(new EdgeOrderComparator(node2IsSameLayerDummy, node, ldp));
        assignEdgePorts(node, outgoingEdges, graph, true, itemFactory);
      }
    }

    // destroy same layer edge construct and write back the changes.
    for (final Node sameLayerNode : sameLayerNodes) {
      final Edge sameLayerEdge = originalEdge[sameLayerNode.index() - sameLayerEdgeOffset];
      graph.unhide(sameLayerEdge);
      EdgeData ed = ldp.getEdgeData(sameLayerEdge);
      if (sameLayerNode.outDegree() > 0) {
        final Edge d1 = sameLayerNode.firstOutEdge();
        final Edge d2 = sameLayerNode.lastOutEdge();
        final EdgeData d1ed = ldp.getEdgeData(d1);
        final EdgeData d2ed = ldp.getEdgeData(d2);
        if (d1.target() == sameLayerEdge.source()) {
          if (!d1ed.getTPC().equals(ed.getSPC())) {
            ed = itemFactory.setTemporaryPortConstraint(sameLayerEdge, true, d1ed.getTPC());
          }
          if (!d2ed.getTPC().equals(ed.getTPC())) {
            itemFactory.setTemporaryPortConstraint(sameLayerEdge, false, d2ed.getTPC());
          }
          graph.setSourcePointRel(sameLayerEdge, graph.getTargetPointRel(d1));
          graph.setTargetPointRel(sameLayerEdge, graph.getTargetPointRel(d2));
        } else {
          if (!d2ed.getTPC().equals(ed.getSPC())) {
            ed = itemFactory.setTemporaryPortConstraint(sameLayerEdge, true, d2ed.getTPC());
          }
          if (!d1ed.getTPC().equals(ed.getTPC())) {
            itemFactory.setTemporaryPortConstraint(sameLayerEdge, false, d1ed.getTPC());
          }
          graph.setSourcePointRel(sameLayerEdge, graph.getTargetPointRel(d2));
          graph.setTargetPointRel(sameLayerEdge, graph.getTargetPointRel(d1));
        }
      } else {
        final Edge d1 = sameLayerNode.firstInEdge();
        final Edge d2 = sameLayerNode.lastInEdge();
        final EdgeData d1ed = ldp.getEdgeData(d1);
        final EdgeData d2ed = ldp.getEdgeData(d2);
        if (d1.source() == sameLayerEdge.source()) {
          if (!d1ed.getSPC().equals(ed.getSPC())) {
            ed = itemFactory.setTemporaryPortConstraint(sameLayerEdge, true, d1ed.getSPC());
          }
          if (!d2ed.getSPC().equals(ed.getTPC())) {
            itemFactory.setTemporaryPortConstraint(sameLayerEdge, false, d2ed.getSPC());
          }
          graph.setSourcePointRel(sameLayerEdge, graph.getSourcePointRel(d1));
          graph.setTargetPointRel(sameLayerEdge, graph.getSourcePointRel(d2));
        } else {
          if (!d2ed.getSPC().equals(ed.getSPC())) {
            ed = itemFactory.setTemporaryPortConstraint(sameLayerEdge, true, d2ed.getSPC());
          }
          if (!d1ed.getSPC().equals(ed.getTPC())) {
            itemFactory.setTemporaryPortConstraint(sameLayerEdge, false, d1ed.getSPC());
          }
          graph.setSourcePointRel(sameLayerEdge, graph.getSourcePointRel(d2));
          graph.setTargetPointRel(sameLayerEdge, graph.getSourcePointRel(d1));
        }
      }
      graph.hide(sameLayerEdge);
    }
    for (final Node sameLayerNode : sameLayerNodes) {
      graph.removeNode(sameLayerNode);
    }
  }

  public void setBackloopRouting(final boolean backloopRouting) {
    this.backloopRouting = backloopRouting;
  }

  // Sort the edges of a node from left to right. The resulting order is optimal with respect to the
  // calculated layout.
  private class EdgeOrderComparator implements Comparator<Edge> {
    private final Node node;
    private final DataProvider node2IsSameLayerDummy;
    private final int nodePos;
    private final LayoutDataProvider ldp;

    private EdgeOrderComparator(
        final DataProvider node2IsSameLayerDummy, final Node node, final LayoutDataProvider ldp) {
      this.node2IsSameLayerDummy = node2IsSameLayerDummy;
      this.node = node;
      this.ldp = ldp;
      this.nodePos = ldp.getNodeData(node).getPosition();
    }

    // returns the position of the opposite of node within its layer
    private int getPositionOfOpposite(final Edge e) {
      final Node opposite = e.opposite(node);
      if (node2IsSameLayerDummy.getBool(opposite)) {
        // e is a same layer edge
        Edge other = null;
        for (final EdgeCursor ec = opposite.edges(); ec.ok(); ec.next()) {
          if (ec.edge() != e) {
            other = ec.edge(); // there is exactly one such edge!
            break;
          }
        }
        final NodeData otherNodeData = ldp.getNodeData(other.opposite(opposite)); // this is the
        // original
        // opposite of
        // node

        return otherNodeData.getPosition();
      } else {
        final NodeData otherNodeData = ldp.getNodeData(opposite);
        return otherNodeData.getPosition();
      }
    }

    private boolean isSameLayerEdge(final Edge e) {
      return node2IsSameLayerDummy.getBool(e.opposite(node));
    }

    @Override
    public int compare(final Edge o1, final Edge o2) {
      final int e1OppositePos = getPositionOfOpposite(o1);
      final int e2OppositePos = getPositionOfOpposite(o2);

      if (isSameLayerEdge(o1) && isSameLayerEdge(o2)) {
        if (nodePos > e1OppositePos && nodePos > e2OppositePos
            || nodePos < e1OppositePos && nodePos < e2OppositePos) {
          return e2OppositePos - e1OppositePos; // the edge with the higher pos value comes first
        } else {
          if (e1OppositePos < e2OppositePos) {
            // the same layer edge to the left comes first
            return -1;
          } else {
            return 1;
          }
        }
      } else if (isSameLayerEdge(o1)) {
        if (nodePos > e1OppositePos) {
          // e1 is a same layer edge to the left (and thus comes first)
          return -1;
        } else {
          return 1;
        }
      } else if (isSameLayerEdge(o2)) {
        if (nodePos > e2OppositePos) {
          // e2 is a same layer edge to the left (and thus comes first)
          return 1;
        } else {
          return -1;
        }
      } else {
        return e1OppositePos - e2OppositePos; // the edge opposite with the lower pos value comes
        // first
      }
    }
  }
}
