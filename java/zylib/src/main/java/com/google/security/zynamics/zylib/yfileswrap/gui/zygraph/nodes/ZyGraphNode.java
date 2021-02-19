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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.SwingInvoker;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.ISelectableNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IViewableNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNodeListener;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.NodeHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyNodeRealizerListener;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.List;
import y.base.Node;
import y.view.Graph2D;

/**
 * This class connects a raw graph node with a yfiles node.
 *
 * @param <RawNodeType> The raw node type.
 */
public abstract class ZyGraphNode<RawNodeType extends IViewNode<?>>
    implements
    ISelectableNode,
    IViewableNode {

  /**
   * The raw node that was used to generate the yfiles node.
   */
  private RawNodeType m_rawNode;

  /**
   * The yfiles node that was generated from the information of the raw node.
   */
  private final Node m_node;

  private final IZyNodeRealizer m_realizer;

  private final InternalListener m_listener;

  /**
   * Creates a new mapping between raw node and yfiles node.
   *
   * @param node The yfiles node.
   * @param rawNode The raw node.
   */
  public ZyGraphNode(final Node node, final IZyNodeRealizer realizer, final RawNodeType rawNode) {
    m_node = Preconditions.checkNotNull(node, "Node argument cannot be null");
    m_realizer = Preconditions.checkNotNull(realizer, "Node realizer argument cannot be null");
    m_rawNode = Preconditions.checkNotNull(rawNode, "Raw node argument cannot be null");

    m_realizer.setX(m_rawNode.getX());
    m_realizer.setY(m_rawNode.getY());
    m_realizer.setFillColor(m_rawNode.getColor());
    m_realizer.setLineColor(m_rawNode.getBorderColor());
    m_realizer.setSelected(m_rawNode.isSelected());

    getGraph().setRealizer(m_node, realizer.getRealizer());

    m_listener = new InternalListener(getGraph());
    realizer.addListener(m_listener);
    rawNode.addListener(m_listener);
  }

  private Graph2D getGraph() {
    return (Graph2D) m_node.getGraph();
  }

  private void updateViews() {
    if (getGraph() == null) {
      return;
    }

    getGraph().updateViews();
  }

  protected void onSelectionChanged() {
    // do nothing, just a hook which can be overwritten by derived classes
  }

  public void addNodeModifier(
      final IZyNodeRealizerListener<? extends ZyGraphNode<RawNodeType>> listener) {
    m_realizer.addListener(listener);
  }

  @Override
  public void calcUnionRect(final Rectangle2D rectangle) {
    m_realizer.calcUnionRect(rectangle);
  }

  public void clearHighlighting(final int level, final int line) {
    final ZyLineContent lineContent = m_realizer.getNodeContent().getLineContent(line);

    if (lineContent.clearHighlighting(level)) {
      updateViews();
    }
  }

  /**
   * Calculates the bounding box of the node.
   *
   * @return The bounding box of the node.
   */
  @Override
  public Double getBoundingBox() {
    return m_realizer.getBoundingBox();
  }

  public abstract List<? extends ZyGraphNode<?>> getChildren();

  /**
   * Returns the yfiles node that was passed into the constructor.
   *
   * @return The yfiles node.
   */
  public Node getNode() {
    return m_node;
  }

  public abstract List<? extends ZyGraphNode<?>> getParents();

  /** Returns the raw node that was passed into the constructor. */
  public RawNodeType getRawNode() {
    // This used to return Object, due to a Java compiler bug
    // (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6738538). This has since been fixed.
    return m_rawNode;
  }

  public IZyNodeRealizer getRealizer() {
    return m_realizer;
  }

  public double getX() {
    return m_realizer.getX();
  }

  public double getY() {
    return m_realizer.getY();
  }

  /**
   * Returns the indegree of the node.
   *
   * @return The indegree of the node.
   */
  public int indegree() {
    return m_node.inDegree();
  }

  /**
   * Determines whether the node is selected or not.
   *
   * @return True, if the node is selected. False, otherwise.
   */
  @Override
  public boolean isSelected() {
    return m_realizer.isSelected();
  }

  /**
   * Determines whether the node is visible or not.
   *
   * @return True, if the node is visible. False, otherwise.
   */
  public boolean isVisible() {
    return getGraph() != null;
  }

  /**
   * Returns the outdegree of the node.
   *
   * @return The outdegree of the node.
   */
  public int outdegree() {
    return m_node.outDegree();
  }

  public int positionToRow(final double y) {
    return m_realizer.positionToRow(y);
  }

  public void removeNodeModifier(
      final IZyNodeRealizerListener<? extends ZyGraphNode<RawNodeType>> listener) {
    m_realizer.removeListener(listener);
  }

  /**
   * Colorizes a complete line of the node in a given color.
   *
   * @param line The index of the line.
   * @param color The color of the line.
   */
  public void setBackgroundColor(final int line, final Color color) {
    final ZyLineContent lineContent = m_realizer.getNodeContent().getLineContent(line);

    lineContent.setBackgroundColor(color);

    updateViews();
  }

  /**
   * Colorizes a part of a line of the node in a given color.
   *
   * @param line The index of the line.
   * @param position The index of the first character to colorize.
   * @param length The number of characters to colorize.
   * @param color The color used to colorize the characters.
   */
  public void setBackgroundColor(final int line, final int position, final int length,
      final Color color) {
    m_realizer.getNodeContent().getLineContent(line).setBackgroundColor(position, length, color);

    updateViews();
  }

  public void setColor(final int line, final Color color) {
    final ZyLineContent lineContent = m_realizer.getNodeContent().getLineContent(line);

    lineContent.setTextColor(color);

    updateViews();
  }

  public void setColor(final int line, final int index, final int size, final Color color) {
    final ZyLineContent lineContent = m_realizer.getNodeContent().getLineContent(line);

    lineContent.setTextColor(index, size, color);

    updateViews();
  }

  public void setHeight(final double value) {
    m_realizer.setHeight(value);
    updateViews();
  }

  public void setHighlighting(final int level, final int line, final Color color) {
    final ZyLineContent lineContent = m_realizer.getNodeContent().getLineContent(line);

    if (lineContent.setHighlighting(level, color)) {
      updateViews();
    }
  }

  public void setHighlighting(final int level, final int line, final int start, final int length,
      final Color color) {
    final ZyLineContent lineContent = m_realizer.getNodeContent().getLineContent(line);

    lineContent.setHighlighting(start, length, level, color);

    updateViews();
  }

  public void setRawNode(final RawNodeType rawNode) {
    Preconditions.checkArgument(rawNode.getClass().equals(m_rawNode.getClass()),
        "Error: Old node and new node have different types");
    m_rawNode.removeListener(m_listener);
    m_rawNode = rawNode;
    m_rawNode.addListener(m_listener);
  }

  public void setWidth(final double value) {
    m_realizer.setWidth(value);
    updateViews();
  }

  private class InternalListener implements IViewNodeListener,
      IZyNodeRealizerListener<ZyGraphNode<RawNodeType>> {
    private final Graph2D m_graph;

    private Node m_pnode;

    public InternalListener(final Graph2D graph) {
      m_graph = graph;
    }

    private void showNode(final IViewNode<?> node, final boolean visible) {
      if (visible) {
        m_graph.reInsertNode(m_node);

        if ((node.getParentGroup() != null) && !node.getParentGroup().isVisible()) {
          node.getParentGroup().setVisible(true);
        }

        if (node instanceof IGroupNode<?, ?>) {
          final IGroupNode<?, ?> gnode = (IGroupNode<?, ?>) node;

          if (gnode.isCollapsed()) {
            m_graph.getHierarchyManager().convertToFolderNode(m_node);
          } else {
            m_graph.getHierarchyManager().convertToGroupNode(m_node);
          }

          // We need to make all of the nodes inside the group visible
          // because we need to connect their outgoing edges to the other
          // visible edges of the graph. This is only necessary if the
          // group node is collapsed though. If it is expanded, the element
          // nodes of the group node are visible anyway.
          if (gnode.isCollapsed()) {
            for (final IViewNode<?> element : gnode.getElements()) {
              element.setVisible(true);
            }
          }
        }

        if (m_pnode != null) {
          m_graph.getHierarchyManager().setParentNode(m_node, m_pnode);
        }
      } else {
        // Save the parent node of the node for later re-use when the
        // node becomes visible again.
        m_pnode = m_graph.getHierarchyManager().getParentNode(m_node);

        if ((m_pnode == null) && (node.getParentGroup() != null)) {
          throw new IllegalStateException("Error");
        }

        if (node instanceof IGroupNode<?, ?>) {
          final IGroupNode<?, ?> gnode = (IGroupNode<?, ?>) node;

          if (gnode.isCollapsed()) {
            for (final IViewNode<?> element : gnode.getElements()) {
              element.setVisible(false);
            }
          }
        }

        if ((node.getParentGroup() == null) || !node.getParentGroup().isCollapsed()) {
          m_graph.removeNode(m_node);
        }
      }

      for (final IViewEdge<? extends IViewNode<?>> edge : node.getIncomingEdges()) {
        if (node.getParentGroup() == null) {
          edge.setVisible(edge.getSource().isVisible() && edge.getTarget().isVisible()
              && NodeHelpers.getVisibleNode(edge.getTarget()).isVisible()
              && NodeHelpers.getVisibleNode(edge.getSource()).isVisible());
        } else if (node.getParentGroup().isCollapsed()) {
          edge.setVisible(node.getParentGroup().isVisible() && edge.getSource().isVisible());
        } else {
          edge.setVisible(edge.getSource().isVisible() && edge.getTarget().isVisible()
              && NodeHelpers.getVisibleNode(edge.getTarget()).isVisible()
              && NodeHelpers.getVisibleNode(edge.getSource()).isVisible());
        }
      }

      for (final IViewEdge<? extends IViewNode<?>> edge : node.getOutgoingEdges()) {
        if (node.getParentGroup() == null) {
          edge.setVisible(edge.getSource().isVisible() && edge.getTarget().isVisible()
              && NodeHelpers.getVisibleNode(edge.getTarget()).isVisible()
              && NodeHelpers.getVisibleNode(edge.getSource()).isVisible());
        } else if (node.getParentGroup().isCollapsed()) {
          edge.setVisible(node.getParentGroup().isVisible() && edge.getTarget().isVisible());
        } else {
          edge.setVisible(edge.getSource().isVisible() && edge.getTarget().isVisible()
              && NodeHelpers.getVisibleNode(edge.getTarget()).isVisible()
              && NodeHelpers.getVisibleNode(edge.getSource()).isVisible());
        }
      }
    }

    @Override
    public void changedBorderColor(final IViewNode<?> node, final Color color) {
      m_realizer.setLineColor(color);

      updateViews();
    }

    @Override
    public void changedColor(final IViewNode<?> node, final Color color) {
      m_realizer.setFillColor(color);

      updateViews();
    }

    @Override
    public void changedLocation(final IZyNodeRealizer realizer, final double x, final double y) {
      m_rawNode.setX(x);
      m_rawNode.setY(y);
    }

    @Override
    public void changedSelection(final IViewNode<?> node, final boolean selected) {
      m_realizer.setSelected(selected);

      onSelectionChanged();

      updateViews();
    }

    @Override
    public void changedSelection(final IZyNodeRealizer realizer) {
      m_rawNode.setSelected(realizer.isSelected());
    }

    @Override
    public void changedSize(final IZyNodeRealizer realizer, final double x, final double y) {
      m_rawNode.setWidth(x);
      m_rawNode.setHeight(y);
    }

    @Override
    public void changedVisibility(final IViewNode<?> node, final boolean visible) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          showNode(node, visible);
        }
      }.invokeAndWait();
    }

    @Override
    public void changedVisibility(final IZyNodeRealizer realizer) {
      m_rawNode.setVisible(realizer.isVisible());
    }

    @Override
    public void heightChanged(final IViewNode<?> node, final double height) {
      if ((m_realizer.getHeight() != height) && (height != 0)) {
        m_realizer.setHeight(height);
        updateViews();
      }
    }

    @Override
    public void regenerated(final IZyNodeRealizer realizer) {}

    @Override
    public void widthChanged(final IViewNode<?> node, final double width) {
      if ((m_realizer.getWidth() != width) && (width != 0)) {
        m_realizer.setWidth(width);
        updateViews();
      }
    }

    @Override
    public void xposChanged(final IViewNode<?> node, final double xpos) {
      if (m_realizer.getX() != xpos) {
        m_realizer.setX(xpos);
        updateViews();
      }
    }

    @Override
    public void yposChanged(final IViewNode<?> node, final double ypos) {
      if (m_realizer.getY() != ypos) {
        m_realizer.setY(ypos);
        updateViews();
      }
    }
  }
}
