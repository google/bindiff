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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity;

import com.google.security.zynamics.zylib.gui.zygraph.AbstractZyGraphSettings;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphVisibilityListener;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeFilter;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.SelectedVisibleFilter;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNodeListener;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.NodeHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettings;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettingsListener;
import com.google.security.zynamics.zylib.gui.zygraph.wrappers.ViewableGraph;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ZyDefaultProximityBrowser<NodeType extends ZyGraphNode<? extends IViewNode<?>>, EdgeType extends ZyGraphEdge<?, ?, ? extends IViewEdge<?>>> {
  private boolean m_internallyDisabled = false;

  private final AbstractZyGraph<NodeType, EdgeType> m_graph;

  private final HashMap<Node, ZyProximityNode<?>> m_proximityMap =
      new HashMap<Node, ZyProximityNode<?>>();

  private final IProximitySettings m_settings;

  private Set<NodeType> m_lastShown = new HashSet<NodeType>();

  private final InternalVisibilityListener m_visibilityListener = new InternalVisibilityListener();

  private final InternalSettingsListener m_settingsListener = new InternalSettingsListener();

  private boolean m_changedProximityBrowsing = false;

  private boolean m_doLayout = true;

  private final Map<ZyProximityNode<?>, InternalNodeListener> m_nodeListeners =
      new HashMap<ZyProximityNode<?>, InternalNodeListener>();

  public ZyDefaultProximityBrowser(final AbstractZyGraph<NodeType, EdgeType> graph,
      final AbstractZyGraphSettings settings) {
    m_graph = graph;

    m_settings = settings.getProximitySettings();

    addVisibilityListener();

    addSettingsListener();
  }

  /**
   * Disables proximity browsing by unhiding all invisible nodes.
   */
  private void disableProximityBrowsing() {
    // ATTENTION: THE FOLLOWING LINE DOES NOT WORK; WHAT IF A NODE WITHOUT CONNECTED
    // EDGES IS SELECTED? RELEVANT CASE: 1241
    // if (!m_proximityMap.isEmpty())
    if (hasAnyHiddenNodes()) {
      unhideEverything();

      deleteProximityBrowsingNodes();

      updateViews();
    }
  }

  private List<NodeType> filterGroupedNodes(final List<NodeType> allNodes) {
    return CollectionHelpers.filter(allNodes, new ICollectionFilter<NodeType>() {
      private boolean isOpenGroupNode(final NodeType node) {
        return (node.getRawNode() instanceof IGroupNode<?, ?>)
            && !((IGroupNode<?, ?>) node.getRawNode()).isCollapsed();
      }

      @Override
      public boolean qualifies(final NodeType node) {
        return (node == getVisibleNode(node)) && !isOpenGroupNode(node);
      }
    });
  }

  private NodeType getVisibleNode(final NodeType node) {
    NodeType previousNode = node;
    IGroupNode<?, ?> parentGroup = ((IViewNode<?>) node.getRawNode()).getParentGroup();

    while ((parentGroup != null) && parentGroup.isCollapsed()) {
      previousNode = m_graph.getNode(parentGroup);
      parentGroup = parentGroup.getParentGroup();
    }

    return previousNode;
  }

  private boolean hasAnyHiddenNodes() {
    return GraphHelpers.any(m_graph, new INodeFilter<NodeType>() {
      @Override
      public boolean qualifies(final NodeType node) {
        return !node.isVisible();
      }
    });
  }

  /**
   * Restarts proximity browsing by setting only the selected nodes visible.
   */
  private void restartProximityBrowsing() {
    // Note that is is not necessary to set the neighborhood of the
    // selected nodes visible. This is handled by the graph object.

    m_lastShown = new HashSet<NodeType>();

    final Collection<NodeType> selectedNodes =
        SelectedVisibleFilter.filter(m_graph.getSelectedNodes());

    if (!selectedNodes.isEmpty()) {
      // Only do this if nodes are selected, otherwise the whole graph
      // disappears and we do not want that behavior.

      final List<NodeType> allNodes = GraphHelpers.getNodes(m_graph);
      allNodes.removeAll(selectedNodes);

      m_graph.showNodes(selectedNodes, allNodes);

      updateViews();
    }
  }

  private void setActive() {
    if (!m_graph.getSettings().getProximitySettings().getProximityBrowsing()) {
      disableProximityBrowsing();
    } else {
      restartProximityBrowsing();
    }
  }

  private void unhideEverything() {
    m_graph.iterate(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        ((IViewNode<?>) node.getRawNode()).setVisible(true);

        return IterationMode.CONTINUE;
      }
    });

    m_graph.iterateEdges(new IEdgeCallback<EdgeType>() {
      @Override
      public IterationMode nextEdge(final EdgeType edge) {
        edge.getRawEdge().setVisible(true);

        return IterationMode.CONTINUE;
      }
    });
  }

  private void updateProximityBrowsing(final Set<NodeType> toShow) {
    if (!m_graph.getSettings().getProximitySettings().getProximityBrowsing()
        || m_internallyDisabled) {
      return;
    }

    if (m_lastShown.equals(toShow)) {
      return;
    }

    m_lastShown = new HashSet<NodeType>(toShow);

    m_internallyDisabled = true;

    // Remove the existing proximity nodes
    deleteProximityBrowsingNodes();

    final List<NodeType> allNodes = GraphHelpers.getNodes(m_graph);

    // Create the new proximity browsing nodes
    createProximityBrowsingNodes(allNodes);

    updateViews();

    m_internallyDisabled = false;
  }

  private void updateViews() {
    if (m_graph.getSettings().getLayoutSettings().getAutomaticLayouting() && m_doLayout) {
      m_graph.doLayout();
    }

    m_graph.updateViews();
  }

  protected void addSettingsListener() {
    m_settings.addListener(m_settingsListener);
  }

  protected void addVisibilityListener() {
    m_graph.addListener(m_visibilityListener);
  }

  protected void createProximityBrowsingNodes(final List<NodeType> allNodes) {
    for (final NodeType node : filterGroupedNodes(allNodes)) {
      if (!node.isVisible()) {
        continue;
      }

      // NH changed: Proximity node label displayed in the past the hidden outgoing edge count, but
      // especially in callgraphs there could be many edges between two nodes.
      // Create the proximity nodes for the incoming edges
      // final int invisibleIndegree = NodeHelpers.countInvisibleIndegree((IViewNode<?>)
      // node.getRawNode());

      // Create the proximity nodes displaying the hidden incoming edge source nodes. NH
      final int invisibleIndegree =
          NodeHelpers.countInvisibleIndegreeNeighbours((IViewNode<?>) node.getRawNode());

      if (invisibleIndegree > 0) {
        final ZyProximityNode<?> infoNode =
            ProximityNodeCreator.createProximityNode(m_graph.getGraph(), node, invisibleIndegree,
                false);

        final InternalNodeListener listener =
            new InternalNodeListener(node.getX(), node.getY(), infoNode);
        ((IViewNode<?>) node.getRawNode()).addListener(listener);
        m_nodeListeners.put(infoNode, listener);

        m_proximityMap.put(infoNode.getNode(), infoNode);

        ProximityNodeCreator
            .insertProximityEdge(m_graph.getGraph(), infoNode, getVisibleNode(node));
      }

      // NH changed: Proximity node label displayed in the past the hidden outgoing edge count, but
      // especially in callgraphs there could be many edges between two nodes.
      // Create the proximity nodes for the outgoing edges
      // final int invisibleOutdegree = NodeHelpers.countInvisibleOutdegree((IViewNode<?>)
      // node.getRawNode());

      // Create the proximity nodes displaying the hidden outgoing edge source nodes. NH
      final int invisibleOutdegree =
          NodeHelpers.countInvisibleOutdegreeNeighbours((IViewNode<?>) node.getRawNode());

      if (invisibleOutdegree > 0) {
        final ZyProximityNode<?> infoNode =
            ProximityNodeCreator.createProximityNode(m_graph.getGraph(), node, invisibleOutdegree,
                true);

        final InternalNodeListener listener =
            new InternalNodeListener(node.getX(), node.getY(), infoNode);
        ((IViewNode<?>) node.getRawNode()).addListener(listener);
        m_nodeListeners.put(infoNode, listener);

        m_proximityMap.put(infoNode.getNode(), infoNode);

        ProximityNodeCreator
            .insertProximityEdge(m_graph.getGraph(), getVisibleNode(node), infoNode);
      }
    }
  }

  /**
   * Removes a single proximity browsing node from the graph. NH: Introduced for BinDiff's remove
   * and add basicblock match function in case proximity nodes are direct neighbors of the affected
   * basicblocks!
   */
  protected void deleteProximityBrowsingNode(final Node yProxyNode) {
    // Note: This listener has to be removed manually before this function is called!
    // final ZyProximityNode<?> zyProxyNode = m_proximityMap.get(yProxyNode);
    // final InternalNodeListener listener = m_nodeListeners.get(zyProxyNode);
    // zyProxyNode.getRawNode().getAttachedNode().removeListener(listener);

    final ZyProximityNode<?> zyProxyNode = m_proximityMap.get(yProxyNode);
    m_nodeListeners.remove(zyProxyNode);

    m_proximityMap.remove(yProxyNode);
    m_graph.getGraph().removeNode(yProxyNode);
  }

  /**
   * Removes all proximity browsing nodes from the graph.
   */
  protected void deleteProximityBrowsingNodes() {
    // each node deletion triggers the selection state listener
    // which updates proximity browsing automatically
    // this is suppressed here by setting m_isActive temporarily to false
    m_internallyDisabled = true;

    for (final Node node : m_proximityMap.keySet()) {
      m_graph.getGraph().removeNode(node);
    }

    m_proximityMap.clear();

    for (final Entry<ZyProximityNode<?>, InternalNodeListener> entry : m_nodeListeners.entrySet()) {
      ((IViewNode<?>) entry.getKey().getRawNode().getAttachedNode()).removeListener(entry
          .getValue());
    }

    m_nodeListeners.clear();

    m_internallyDisabled = false;
  }

  protected void removeSettingsListener() {
    m_settings.removeListener(m_settingsListener);
  }

  protected void removeVisibilityListener() {
    m_graph.removeListener(m_visibilityListener);

  }

  public void dispose() {
    removeVisibilityListener();
    removeSettingsListener();

  }

  public void setEnabled(final boolean enable) {
    if (enable) {
      m_graph.getSettings().getProximitySettings().addListener(m_settingsListener);
    } else {
      m_graph.getSettings().getProximitySettings().removeListener(m_settingsListener);
    }
  }

  private class InternalNodeListener implements IViewNodeListener {
    private double m_x;
    private double m_y;
    private final ZyProximityNode<?> m_infoNode;

    public InternalNodeListener(final double x, final double y, final ZyProximityNode<?> infoNode) {
      m_x = x;
      m_y = y;
      m_infoNode = infoNode;
    }

    @Override
    public void changedBorderColor(final IViewNode<?> node, final Color color) {
    }

    @Override
    public void changedColor(final IViewNode<?> node, final Color color) {
    }

    @Override
    public void changedSelection(final IViewNode<?> node, final boolean selected) {
    }

    @Override
    public void changedVisibility(final IViewNode<?> node, final boolean visible) {
    }

    @Override
    public void heightChanged(final IViewNode<?> node, final double height) {
    }

    @Override
    public void widthChanged(final IViewNode<?> node, final double width) {
    }

    @Override
    public void xposChanged(final IViewNode<?> node, final double xpos) {
      m_infoNode.getRealizer().setX((m_infoNode.getX() + xpos) - m_x);

      m_x = xpos;
    }

    @Override
    public void yposChanged(final IViewNode<?> node, final double ypos) {
      m_infoNode.getRealizer().setY((m_infoNode.getY() + ypos) - m_y);

      m_y = ypos;
    }
  }

  private class InternalSettingsListener implements IProximitySettingsListener {
    @Override
    public void changedProximityBrowsing(final boolean enabled) {
      m_changedProximityBrowsing = true;

      setActive();

      m_changedProximityBrowsing = false;

      m_graph.updateViews();
    }

    @Override
    public void changedProximityBrowsingDepth(final int children, final int parents) {
      if (m_graph.getSettings().getProximitySettings().getProximityBrowsing()) {
        restartProximityBrowsing();
      }
    }

    @Override
    public void changedProximityBrowsingFrozen(final boolean value) {
    }

    @Override
    public void changedProximityBrowsingPreview(final boolean value) {
    }
  }

  /**
   * Refreshes proximity browsing nodes when the visibility of nodes in the graph changes.
   */
  private class InternalVisibilityListener implements IZyGraphVisibilityListener {
    @Override
    public void nodeDeleted() {
      // Force a complete redraw on deleted nodes to make sure
      // proximity nodes are redrawn.
      m_lastShown.clear();

      // Use a temporary variable to work around OpenJDK build problem. Original code is:
      // updateProximityBrowsing(GraphHelpers.getVisibleNodes(ViewableGraph.wrap(m_graph)));
      final Set<NodeType> nodes = GraphHelpers.getVisibleNodes(ViewableGraph.wrap(m_graph));
      updateProximityBrowsing(nodes);
    }

    @Override
    public void visibilityChanged() {
      if (m_changedProximityBrowsing) {
        m_doLayout = false;
      }

      // Use a temporary variable to work around OpenJDK build problem. Original code is:
      // updateProximityBrowsing(GraphHelpers.getVisibleNodes(ViewableGraph.wrap(m_graph)));
      final Set<NodeType> nodes = GraphHelpers.getVisibleNodes(ViewableGraph.wrap(m_graph));
      updateProximityBrowsing(nodes);

      m_doLayout = true;
    }
  }
}
