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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyNodeRealizerListener;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Realizer class for default rectangular text nodes.
 * 
 * @param <NodeType>
 */
public class ZyGroupNodeRealizer<NodeType extends ZyGraphNode<?>> extends GroupNodeRealizer
    implements IZyNodeRealizer {
  /**
   * Content that is displayed in the realizer.
   */
  private final ZyLabelContent m_content;

  /**
   * User-specific data that is associated with the realizer.
   */
  private ZyNodeData<?> m_userData;

  /**
   * Node updater class that is used to update the content of the realizer.
   */
  private IRealizerUpdater<?> m_updater;

  /**
   * Listeners that are notified about changes in the node realizer.
   */
  private final ListenerProvider<IZyNodeRealizerListener<?>> m_listeners =
      new ListenerProvider<IZyNodeRealizerListener<?>>();

  public ZyGroupNodeRealizer(final ZyLabelContent content, final boolean collapsed) {
    setShapeType(ShapeNodeRealizer.ROUND_RECT);
    setLineType(LineType.LINE_2);

    m_content = content;

    final Rectangle2D bounds = getNodeContent().getBounds();
    setSize(bounds.getWidth(), bounds.getHeight());

    setGroupClosed(collapsed);
  }

  private void addChildren(final HierarchyManager hm, final Node groupNode,
      final NodeList childNodes) {
    for (final NodeCursor nc = hm.getChildren(groupNode); nc.ok(); nc.next()) {
      final Node n = nc.node();

      if (hm.isGroupNode(n)) {
        addChildren(hm, n, childNodes);
      } else {
        childNodes.add(n);
      }
    }
  }

  private void moveContent(final double dx, final double dy) {
    final Graph2D graph = (Graph2D) m_userData.getNode().getNode().getGraph();

    final HierarchyManager hm = graph.getHierarchyManager();
    // Find the children contained in the given group node.
    final NodeList childNodes = new NodeList();
    addChildren(hm, getNode(), childNodes);
    // Move the children.
    moveNodes(graph, childNodes.nodes(), dx, dy);
  }

  private void moveNodes(final Graph2D graph, final NodeCursor nodes, final double dx,
      final double dy) {
    for (; nodes.ok(); nodes.next()) {
      final NodeRealizer nr = graph.getRealizer(nodes.node());

      if ((nr.getAutoBoundsFeature() != null) && nr.getAutoBoundsFeature().isAutoBoundsEnabled()) {
        continue;
      }

      // Only move the node, if it doesn't have the auto resize feature
      // (enabled).
      nr.moveBy(dx, dy);
    }
  }

  private void notifyLocationChanged(final double x, final double y) {
    for (final IZyNodeRealizerListener<?> listener : m_listeners) {
      try {
        listener.changedLocation(this, x, y);
      } catch (final Exception exception) {
        // TODO: Log this
      }
    }
  }

  @Override
  public void addListener(final IZyNodeRealizerListener<? extends ZyGraphNode<?>> listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the node content that is displayed by the realizer.
   * 
   * @return The node content that is displayed by the realizer.
   */
  @Override
  public ZyLabelContent getNodeContent() {
    return m_content;
  }

  @Override
  public NodeRealizer getRealizer() {
    return this;
  }

  @Override
  public IRealizerUpdater<?> getUpdater() {
    return m_updater;
  }

  /**
   * Returns the user data associated with the realizer.
   * 
   * @return The user data associated with the realizer.
   */
  @Override
  public ZyNodeData<?> getUserData() {
    return m_userData;
  }

  @Override
  public void moveBy(final double x, final double y) {
    moveContent(x, y);

    super.moveBy(x, y);

    notifyLocationChanged(getX(), getY());
  }

  @Override
  public void paintHotSpots(final Graphics2D g) {
    return;
  }

  @Override
  public void paintNode(final Graphics2D gfx) {
    super.paintNode(gfx);

    final IGroupNode<?, ?> rawNode = (IGroupNode<?, ?>) m_userData.getNode().getRawNode();

    if (rawNode.isCollapsed()) {
      getNodeContent().draw(gfx, getX(), getY() + 8);
    }
  }

  @Override
  public int positionToRow(final double y) {
    return 0;
  }

  @Override
  public void regenerate() {
    m_updater.generateContent(this, m_content);

    final Rectangle2D bounds = m_content.getBounds();

    // For some forsaken reason setSize changes
    // the position of the node. So we have to
    // store and restore the position between setting
    // the size.

    final double x = getX();
    final double y = getY();

    setSize(bounds.getWidth(), bounds.getHeight());

    setX(x);
    setY(y);

    for (final IZyNodeRealizerListener<?> listener : m_listeners) {
      try {
        listener.regenerated(this);
      } catch (final Exception exception) {
        // TODO (timkornau): Log this
      }
    }

    repaint();
  }

  @Override
  public void removeListener(final IZyNodeRealizerListener<?> listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public double rowToPosition(final int line) {
    return m_content.getPaddingTop() + (line * m_content.getLineHeight());
  }

  @Override
  public void setCenter(final double x, final double y) {
    super.setCenter(x, y);

    notifyLocationChanged(x, y);
  }

  @Override
  public void setCenterX(final double x) {
    super.setCenterX(x);

    notifyLocationChanged(x, y);
  }

  @Override
  public void setFillColor(final Color color) {
    super.setFillColor(color);

    updateContentSelectionColor();
  }

  @Override
  public void setGroupClosed(final boolean collapsed) {
    super.setGroupClosed(collapsed);

    if (collapsed) {
      final Rectangle2D bounds = m_content.getBounds();

      setSize(bounds.getWidth(), bounds.getHeight());
    }
  }

  @Override
  public void setLocation(final double x, final double y) {
    super.setLocation(x, y);

    notifyLocationChanged(x, y);
  }

  @Override
  public void setSelected(final boolean selected) {
    super.setSelected(selected);

    updateContentSelectionColor();

    for (final IZyNodeRealizerListener<?> listener : m_listeners) {
      try {
        listener.changedSelection(this);
      } catch (final Exception exception) {
        // TODO (timkornau): Log this
      }
    }
  }

  @Override
  public void setUpdater(final IRealizerUpdater<?> updater) {
    m_updater = updater;

    if (updater != null) {
      updater.setRealizer(this);
    }
  }

  @Override
  public void setUserData(final ZyNodeData<?> data) {
    Preconditions.checkNotNull(data, "Error: Invalid node data");
    Preconditions.checkArgument((data.getNode().getRawNode() instanceof IGroupNode),
        "Error: User data does not contain a group node");

    m_userData = data;
  }

  @Override
  public void setVisible(final boolean visible) {
    super.setVisible(visible);

    updateContentSelectionColor();

    for (final IZyNodeRealizerListener<?> listener : m_listeners) {
      try {
        listener.changedVisibility(this);
      } catch (final Exception exception) {
        // TODO (timkornau): Log this
      }
    }
  }

  @Override
  public void updateContentSelectionColor() {
    // TODO (timkornau): This is kind of a hack, should be improved

    if (m_content.isSelectable()) {
      m_content.updateContentSelectionColor(getFillColor(), isSelected());
    }
  }
}
