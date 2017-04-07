// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IBendListener;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdgeListener;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEdgeRealizerListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.base.Edge;
import y.base.Node;
import y.geom.YPoint;
import y.view.Bend;
import y.view.Graph2D;
import y.view.hierarchy.HierarchyManager;

import java.awt.Color;
import java.util.ArrayList;

/**
 * This class connects raw edges with with yfiles edges.
 *
 * @param <RawEdgeTypeT> The type of the raw edge.
 */
public class ZyGraphEdge<NodeTypeT extends ZyGraphNode<?>,
    EdgeTypeT extends ZyGraphEdge<?, ?, ?>, RawEdgeTypeT extends IViewEdge<?>> {

  private Edge m_edge;
  private RawEdgeTypeT m_rawEdge;

  private final InternalListener m_listener = new InternalListener();
  private final ZyEdgeRealizer<EdgeTypeT> m_realizer;
  private final Graph2D m_graph;

  private final NodeTypeT m_targetNode;
  private final NodeTypeT m_sourceNode;

  /**
   * Creates a new mapping between raw edges and yfiles edges.
   *
   * @param edge The yfiles edge.
   * @param rawEdge The raw edge.
   */
  public ZyGraphEdge(final NodeTypeT source, final NodeTypeT target, final Edge edge,
      final ZyEdgeRealizer<EdgeTypeT> realizer, final RawEdgeTypeT rawEdge) {
    Preconditions.checkNotNull(source, "Source node cannot be null");
    Preconditions.checkNotNull(target, "Target node cannot be null");
    Preconditions.checkNotNull(edge, "Edge argument cannot be null");
    Preconditions.checkNotNull(rawEdge, "Raw edge argument cannot be null");

    m_sourceNode = source;
    m_targetNode = target;

    m_edge = edge;
    m_rawEdge = rawEdge;
    m_realizer = realizer;

    m_realizer.setSelected(rawEdge.isSelected());

    m_realizer.setSourcePoint(new YPoint(rawEdge.getX1(), rawEdge.getY1()));
    m_realizer.setTargetPoint(new YPoint(rawEdge.getX2(), rawEdge.getY2()));

    m_graph = (Graph2D) edge.getGraph();

    m_graph.setRealizer(edge, realizer);

    for (final CBend bend : m_rawEdge.getBends()) {
      m_realizer.addPoint(bend.getX(), bend.getY());

      bend.addListener(m_listener);
    }

    realizer.addListener(m_listener);
    rawEdge.addListener(m_listener);

  }

  private static Graph2D getGraph(final Edge edge) {
    return (Graph2D) edge.getGraph();
  }

  /***
   * Returns the {@code ZyEdgeRealizer} bound to the given edge.
   *
   * @param edge the edge to get the realizer for
   * @return the {@code ZyEdgeRealizer} bound to {@code edge}
   * @throws IllegalStateException if the node does not have a realizer
   */
  private ZyEdgeRealizer<EdgeTypeT> getRealizer(final Edge edge) {
    @SuppressWarnings("unchecked")
    final ZyEdgeRealizer<EdgeTypeT> realizer =
        (ZyEdgeRealizer<EdgeTypeT>) getGraph(edge).getRealizer(edge);
    Preconditions.checkState(realizer != null, "Node does not have a realizer");

    return realizer;
  }

  private void updateViews() {
    if (getGraph(m_edge) != null) {
      getGraph(m_edge).updateViews();
    }
  }

  public void addPath(final double x, final double y) {
    getRealizer(m_edge).addPoint(x, y);
  }

  public void dispose() {
    for (final CBend bend : m_rawEdge.getBends()) {
      bend.removeListener(m_listener);
    }

    m_realizer.removeListener(m_listener);
    m_rawEdge.removeListener(m_listener);
  }

  /**
   * Returns the yfiles edge that was passed into the constructor.
   *
   * @return The yfiles edge.
   */
  public Edge getEdge() {
    return m_edge;
  }

  public ArrayList<Pair<Double, Double>> getPaths() {
    final int points = getRealizer(m_edge).pointCount();

    final ArrayList<Pair<Double, Double>> pointsList = new ArrayList<Pair<Double, Double>>();

    for (int i = 0; i < points; i++) {
      final YPoint point = getRealizer(m_edge).getPoint(i);

      pointsList.add(new Pair<Double, Double>(point.x, point.y));
    }

    return pointsList;
  }

  /**
   * Returns the raw edge that was passed into the constructor.
   *
   * @return The raw edge.
   */
  public RawEdgeTypeT getRawEdge() {
    return m_rawEdge;
  }

  public ZyEdgeRealizer<EdgeTypeT> getRealizer() {
    return m_realizer;
  }

  public NodeTypeT getSource() {
    return m_sourceNode;
  }

  public NodeTypeT getTarget() {
    return m_targetNode;
  }

  public double getX1() {
    return getRealizer(m_edge).getSourcePoint().x;
  }

  public double getX2() {
    return getRealizer(m_edge).getTargetPoint().x;
  }

  public double getY1() {
    return getRealizer(m_edge).getSourcePoint().y;
  }

  public double getY2() {
    return getRealizer(m_edge).getTargetPoint().y;
  }

  public boolean isSelected() {
    return m_realizer.isSelected();
  }

  public boolean isVisible() {
    return m_edge.getGraph() != null;
  }

  public void setRawEdge(final RawEdgeTypeT rawEdge) {
    m_rawEdge.removeListener(m_listener);

    m_rawEdge = rawEdge;

    m_rawEdge.addListener(m_listener);
  }

  public void setX1(final double x1) {
    getRealizer(m_edge).setSourcePoint(new YPoint(x1, getY1()));
  }

  public void setX2(final double x2) {
    getRealizer(m_edge).setTargetPoint(new YPoint(x2, getY2()));
  }

  public void setY1(final double y1) {
    getRealizer(m_edge).setSourcePoint(new YPoint(getX1(), y1));
  }

  public void setY2(final double y2) {
    getRealizer(m_edge).setTargetPoint(new YPoint(getX2(), y2));
  }

  private class InternalListener implements IZyEdgeRealizerListener<EdgeTypeT>, IViewEdgeListener,
      IBendListener {
    @Override
    public void addedBend(final double x, final double y) {
      // Bend was added to the realizer

      m_rawEdge.addBend(x, y);
    }

    @Override
    public void addedBend(final IViewEdge<?> edge, final CBend bend) {
      // Bend was added to the model

      bend.addListener(m_listener);

      if (m_realizer.bendCount() == edge.getBendCount()) {
        return;
      }

      m_realizer.appendBend(bend.getX(), bend.getY());
    }

    @Override
    public void bendChanged(final int index, final double x, final double y) {
      final CBend path = m_rawEdge.getBends().get(index);

      path.setX(x);
      path.setY(y);
    }

    @Override
    public void changedColor(final CViewEdge<?> edge, final Color color) {
      m_realizer.setLineColor(color);

      updateViews();
    }

    @Override
    public void changedLocation(final ZyEdgeRealizer<EdgeTypeT> realizer) {
      final YPoint sourcePoint = realizer.getSourcePoint();

      m_rawEdge.setX1(sourcePoint.x);
      m_rawEdge.setY1(sourcePoint.y);

      final YPoint targetPoint = realizer.getTargetPoint();

      m_rawEdge.setX2(targetPoint.x);
      m_rawEdge.setY2(targetPoint.y);
    }

    @Override
    public void changedSelection(final IViewEdge<?> edge, final boolean selected) {
      m_realizer.setSelected(selected);

      updateViews();
    }

    @Override
    public void changedSourceX(final CViewEdge<?> edge, final double sourceX) {
      final YPoint point = m_realizer.getSourcePoint();

      m_realizer.setSourcePoint(new YPoint(sourceX, point.y));

      updateViews();
    }

    @Override
    public void changedSourceY(final CViewEdge<?> edge, final double sourceY) {
      final YPoint point = m_realizer.getSourcePoint();

      m_realizer.setSourcePoint(new YPoint(point.x, sourceY));

      updateViews();
    }

    @Override
    public void changedTargetX(final CViewEdge<?> edge, final double targetX) {
      final YPoint point = m_realizer.getTargetPoint();

      m_realizer.setTargetPoint(new YPoint(targetX, point.y));

      updateViews();
    }

    @Override
    public void changedTargetY(final CViewEdge<?> edge, final double targetY) {
      final YPoint point = m_realizer.getTargetPoint();

      m_realizer.setTargetPoint(new YPoint(point.x, targetY));

      updateViews();
    }

    @Override
    public void changedType(final CViewEdge<?> edge,
        final com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType type) {
    }

    @Override
    public void changedVisibility(final IViewEdge<?> edge, final boolean visible) {
      if (visible) {
        final Node source = m_sourceNode.getNode();
        final Node target = m_targetNode.getNode();

        // Make sure "all endpoints lie in a graph"
        if ((source.getGraph() == null) || (target.getGraph() == null)) {
          return;
        }

        // Note: "m_graph.reInsertEdge(m_edge)" won't work here, as
        // edges can lose target nodes when group nodes are
        // collapsed
        final HierarchyManager hm = m_graph.getHierarchyManager();
        final Edge yedge = hm.createEdge(source, target);

        m_graph.setRealizer(yedge, m_realizer);

        m_edge = yedge;
      } else if (m_edge.getGraph() == m_graph) {
        m_graph.removeEdge(m_edge);
      }
    }

    @Override
    public void changedVisibility(final ZyEdgeRealizer<EdgeTypeT> realizer) {
      m_rawEdge.setVisible(realizer.isVisible());
    }

    @Override
    public void changedX(final CBend bend, final double x) {
      final int index = m_rawEdge.getBends().indexOf(bend);

      final Bend rbend = m_realizer.getBend(index);

      rbend.setLocation(x, rbend.getY());
    }

    @Override
    public void changedY(final CBend bend, final double y) {
      final int index = m_rawEdge.getBends().indexOf(bend);

      final Bend rbend = m_realizer.getBend(index);

      rbend.setLocation(rbend.getX(), y);
    }

    @Override
    public void clearedBends() {
      for (final CBend bend : m_rawEdge.getBends()) {
        bend.removeListener(this);
      }

      m_rawEdge.clearBends();
    }

    @Override
    public void clearedBends(final IViewEdge<?> edge) {
      if (m_realizer.bendCount() != 0) {
        m_realizer.clearBends();
      }
    }

    @Override
    public void insertedBend(final int index, final double x, final double y) {
      // Bend was added to the realizer

      if (m_realizer.bendCount() == m_rawEdge.getBendCount()) {
        return;
      }

      m_rawEdge.insertBend(index, x, y);
    }

    @Override
    public void insertedBend(final IViewEdge<?> edge, final int index, final CBend bend) {
      Preconditions.checkNotNull(edge, "Edge cannot be null");
      Preconditions.checkNotNull(bend, "Error: bend argument can not be null");

      // Bend was added to the model
      bend.addListener(m_listener);

      if (m_realizer.bendCount() == edge.getBendCount()) {
        return;
      }

      if (edge == this) {
        m_realizer.insertBend(bend.getX(), bend.getY());
      }
    }

    @Override
    public void regenerated(final ZyEdgeRealizer<EdgeTypeT> realizer) {
    }

    @Override
    public void removedBend(final CViewEdge<?> edge, final int index, final CBend bend) {
      // Removed a bend from the model.
      bend.removeListener(m_listener);

      if (m_realizer.bendCount() == m_rawEdge.getBendCount()) {
        return;
      }

      m_realizer.removeBend(m_realizer.getBend(index));
    }

    @Override
    public void removedBend(final ZyEdgeRealizer<EdgeTypeT> realizer, final int position) {
      // Removed a bend from the realizer

      m_rawEdge.removeBend(position);
    }
  }
}
