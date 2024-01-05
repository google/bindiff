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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.AbstractZyGraphSettings;
import com.google.security.zynamics.zylib.gui.zygraph.CDefaultLabelEventHandler;
import com.google.security.zynamics.zylib.gui.zygraph.CGraphSettingsSynchronizer;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphSelectionListener;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphVisibilityListener;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeIterableGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IIterableGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.ProximityRangeCalculator;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettings;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.ZyEditMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.LayoutFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.grouping.GroupHelpers;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.ZoomHelpers;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyDefaultProximityBrowser;
import java.awt.Cursor;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import y.base.Edge;
import y.base.Node;
import y.layout.LayoutGraphWriter;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

/**
 * Base class that provides all kinds of management functions for working with yfiles Graph2D
 * graphs.
 *
 * @param <NodeType> Base type of all nodes that are present in the graph.
 * @param <EdgeType> Base type of all edges that are present in the graph.
 */
public abstract class AbstractZyGraph<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IIterableGraph<NodeType>, IEdgeIterableGraph<EdgeType> {
  private static final double STANDARD_ZOOM_FACTOR = 0.8;

  /** List of listeners that are notified about click events. */
  private final ListenerProvider<IZyGraphListener<NodeType, EdgeType>> m_graphListeners =
      new ListenerProvider<IZyGraphListener<NodeType, EdgeType>>();

  private final ListenerProvider<IZyGraphVisibilityListener> m_visibilityListener =
      new ListenerProvider<IZyGraphVisibilityListener>();

  private final AbstractZyGraphSettings m_settings;

  /**
   * The proximity browser that is responsible for hiding and showing elements of the graph if
   * necessary.
   */
  private ZyDefaultProximityBrowser<NodeType, EdgeType> m_proximityBrowser;

  /** The yfiles graph that provides the elements of this graph. */
  private final Graph2D m_graph;

  /** The view where the graph is shown. */
  private final ZyGraph2DView m_view;

  /** The edit mode that describes the GUI behavior of the graph. */
  private final ZyEditMode<NodeType, EdgeType> m_editMode;

  private final ZyGraphSelectionObserver m_selectionObserver = new ZyGraphSelectionObserver();

  private final ZyGraphMappings<NodeType, EdgeType> m_mappings;

  private InternalEditModeListener<NodeType, EdgeType> m_editModeListener;

  private final CGraphSettingsSynchronizer m_settingsSynchronizer;

  /**
   * Creates a new AbstractZyGraph object. Each AbstractZyGraph object is linked to a view. This
   * view is the view where all operations on the graph are executed.
   *
   * @param view The view where the graph is displayed.
   * @param nodeMap A mapping that links the ynode objects of the graph with the node objects.
   * @param edgeMap A mapping that links yedge objects of the graph with the edge objects.
   * @param settings Settings used by the graph.
   */
  protected AbstractZyGraph(
      final ZyGraph2DView view,
      final LinkedHashMap<Node, NodeType> nodeMap,
      final LinkedHashMap<Edge, EdgeType> edgeMap,
      final AbstractZyGraphSettings settings) {
    m_view = Preconditions.checkNotNull(view, "Error: View argument can't be null");
    Preconditions.checkNotNull(nodeMap, "Error: Node map argument can't be null");

    m_graph = m_view.getGraph2D();
    m_view.setGraph2DRenderer(new ZyGraphLayeredRenderer<ZyGraph2DView>(m_view));
    m_settings = settings;

    m_mappings = new ZyGraphMappings<NodeType, EdgeType>(m_graph, nodeMap, edgeMap);

    setProximityBrowser(new ZyDefaultProximityBrowser<NodeType, EdgeType>(this, m_settings));

    m_editMode = createEditMode(); // NOTE: DO NOT MOVE THIS UP

    m_settingsSynchronizer = new CGraphSettingsSynchronizer(m_editMode, m_settings);

    initializeListeners();

    initializeView();

    setupHierarchyManager();
  }

  /**
   * Initializes the graph selection listeners that convert internal selection events into proper
   * selection events.
   */
  private void initializeListeners() {
    m_graph.addGraph2DSelectionListener(m_selectionObserver);
    m_graph.addGraphListener(m_selectionObserver);
  }

  /** Initializes various things like proximity browsing, edit mode, ... */
  private void initializeView() {
    // Initialize the default edit mode.
    getView().addViewMode(m_editMode);

    m_editModeListener = new InternalEditModeListener<NodeType, EdgeType>(m_graphListeners);

    m_editMode.addListener(m_editModeListener);

    // Make sure the painted view looks nice.
    getView().setAntialiasedPainting(true);
  }

  private void notifyVisibilityListeners() {
    for (final IZyGraphVisibilityListener listener : m_visibilityListener) {
      // ESCA-JAVA0166: Catch Exception because we are calling a listener function
      try {
        listener.visibilityChanged();
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  private void setupHierarchyManager() {
    if (m_graph.getHierarchyManager() == null) {
      final HierarchyManager hierarchyManager = new HierarchyManager(m_graph);
      m_graph.setHierarchyManager(hierarchyManager);
      hierarchyManager.addHierarchyListener(new GroupNodeRealizer.StateChangeListener());
    }
  }

  private void showNeighbors(final Collection<NodeType> toShow) {
    final Set<NodeType> all =
        ProximityRangeCalculator.getNeighbors(
            this,
            toShow,
            getSettings().getProximitySettings().getProximityBrowsingChildren(),
            getSettings().getProximitySettings().getProximityBrowsingParents());

    showNodesInternal(all);
  }

  private void showNodesInternal(final Collection<NodeType> all) {
    for (final NodeType node : all) {
      Preconditions.checkNotNull(
          node, "Error: The list of nodes to show contained an invalid node");

      if (!((IViewNode<?>) node.getRawNode()).isVisible()) {
        ((IViewNode<?>) node.getRawNode()).setVisible(true);
      }
    }
  }

  private Collection<NodeType> sortLayers(final Collection<NodeType> nodes) {
    final List<NodeType> sortedNodes = new ArrayList<NodeType>(nodes);

    Collections.sort(
        sortedNodes,
        new Comparator<NodeType>() {
          private boolean isInsideGroup(final IViewNode<?> node, final IGroupNode<?, ?> group) {
            final IGroupNode<?, ?> parentGroup = node.getParentGroup();

            if (parentGroup == null) {
              return false;
            }
            if (parentGroup == group) {
              return true;
            }
            return isInsideGroup(node, group.getParentGroup());
          }

          @Override
          public int compare(final NodeType lhs, final NodeType rhs) {
            final IViewNode<?> rawLhs = lhs.getRawNode();
            final IViewNode<?> rawRhs = rhs.getRawNode();

            if ((rawLhs instanceof IGroupNode<?, ?>) && (rawRhs instanceof IGroupNode<?, ?>)) {
              if (isInsideGroup(rawRhs, (IGroupNode<?, ?>) rawLhs)) {
                // RHS is in group LHS => RHS must be hidden first
                return 1;
              }
              if (isInsideGroup(rawLhs, (IGroupNode<?, ?>) rawRhs)) {
                // LHS is in group RHS => LHS must be hidden first
                return -1;
              }
              return 0;
            }
            if (rawLhs instanceof IGroupNode<?, ?>) {
              // If the node is inside the group node, the node must be hidden first
              return isInsideGroup(rawRhs, (IGroupNode<?, ?>) rawLhs) ? 1 : 0;
            }
            if (rhs instanceof IGroupNode<?, ?>) {
              // If the node is inside the group node, the node must be hidden first
              return isInsideGroup(rawLhs, (IGroupNode<?, ?>) rawRhs) ? 1 : 0;
            }
            return 0;
          }
        });

    return sortedNodes;
  }

  protected ZyEditMode<NodeType, EdgeType> createEditMode() {
    return new ZyEditMode<NodeType, EdgeType>(this);
  }

  protected void notifyDeletionListeners() {
    for (final IZyGraphVisibilityListener listener : m_visibilityListener) {
      // ESCA-JAVA0166: Catch Exception because we are calling a listener function
      try {
        listener.nodeDeleted();
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  protected void removeNode(final NodeType node) {
    if (node.getNode().getGraph() == null) {
      m_graph.reInsertNode(node.getNode());
    }

    final HierarchyManager manager = m_graph.getHierarchyManager();
    final Node n = node.getNode();

    if (manager.isNormalNode(n)) {
      m_graph.removeNode(node.getNode());
    } else if (getGraph().getHierarchyManager().isFolderNode(node.getNode())) {
      GroupHelpers.extractFolder(m_graph, node.getNode());

      m_graph.removeNode(node.getNode());
    } else if (getGraph().getHierarchyManager().isGroupNode(node.getNode())) {
      GroupHelpers.extractGroup(m_graph, node.getNode());

      m_graph.removeNode(node.getNode());
    }

    m_mappings.removeNode(node);
  }

  /**
   * Adds a graph listener that is notified when something in the graph changes.
   *
   * @param listener The listener to add.
   */
  public void addListener(final IZyGraphListener<NodeType, EdgeType> listener) {
    m_graphListeners.addListener(listener);
  }

  /**
   * Adds a graph selection listener that is notified when the selection of the graph changes.
   *
   * @param listener The listener to add.
   */
  public void addListener(final IZyGraphSelectionListener listener) {
    m_selectionObserver.addListener(listener);
  }

  public void addListener(final IZyGraphVisibilityListener listener) {
    m_visibilityListener.addListener(listener);
  }

  public void dispose() {
    m_settingsSynchronizer.dispose();
  }

  /** Layouts the graph using the last set layouter that was passed to setLayouter. */
  public void doLayout() {
    LayoutFunctions.doLayout(this, m_settings.getLayoutSettings().getCurrentLayouter());
  }

  public EdgeType getEdge(final Edge edge) {
    return m_mappings.getEdge(edge);
  }

  public EdgeType getEdge(final Object rawEdge) {
    return m_mappings.getEdge(rawEdge);
  }

  // TODO(cblichmann): iterateEdges() can do the same thing
  public Collection<EdgeType> getEdges() {
    return getMappings().getEdges();
  }

  public ZyEditMode<NodeType, ?> getEditMode() {
    return m_editMode;
  }

  /**
   * Returns the Graph2D object managed by the AbstractZyGraph. Be careful with this function - do
   * not use it from code that lives outside of the yfileswrap/ folders, because it introduces
   * dependency on yFiles.
   *
   * @return The Graph2D object.
   */
  public Graph2D getGraph() {
    return m_graph;
  }

  public void updateGraphViews() {
    m_graph.updateViews();
  }

  public int getEdgeCount() {
    return m_graph.edgeCount();
  }

  public ZyGraphMappings<NodeType, EdgeType> getMappings() {
    return m_mappings;
  }

  /**
   * Given a ynode object, this function returns the corresponding node.
   *
   * @param node The ynode object.
   * @return The node object that corresponds to the ynode object.
   */
  public NodeType getNode(final Node node) {
    return m_mappings.getNode(node);
  }

  public NodeType getNode(final Object rawNode) {
    return m_mappings.getNode(rawNode);
  }

  public Collection<NodeType> getNodes() {
    return getMappings().getNodes();
  }

  public ZyDefaultProximityBrowser<NodeType, EdgeType> getProximityBrowser() {
    return m_proximityBrowser;
  }

  public abstract Set<NodeType> getSelectedNodes();

  public AbstractZyGraphSettings getSettings() {
    return m_settings;
  }

  public Graph2DView getView() {
    return m_view;
  }

  // A set of methods implemented to allow other files to talk to the Graph2DView without
  // introducting an explicit yFiles dependency.

  public Cursor getViewCursor() {
    return m_view.getCursor();
  }

  public void removeViewFocusListener(FocusListener focusListener) {
    m_view.removeFocusListener(focusListener);
  }

  public void addViewFocusListener(FocusListener focusListener) {
    m_view.addFocusListener(focusListener);
  }

  public void setViewCursor(Cursor cursor) {
    m_view.setCursor(cursor);
  }

  public void addViewCanvasKeyListener(CDefaultLabelEventHandler handler) {
    m_view.getCanvasComponent().addKeyListener(handler);
  }

  public void removeViewCanvasKeyListener(CDefaultLabelEventHandler handler) {
    m_view.getCanvasComponent().removeKeyListener(handler);
  }

  public Edge getYEdge(final Object rawEdge) {
    return m_mappings.getYEdge(rawEdge);
  }

  public Node getYNode(final Object rawNode) {
    return m_mappings.getYNode(rawNode);
  }

  /**
   * Iterates over all nodes in the graph.
   *
   * @param callback Callback object that is invoked once for each node in the graph.
   */
  @Override
  public void iterate(final INodeCallback<NodeType> callback) {
    Preconditions.checkNotNull(callback, "Callback argument can't be null");

    for (final NodeType node : m_mappings.getNodes()) {
      if (callback.next(node) == IterationMode.STOP) {
        return;
      }
    }
  }

  /**
   * Iterates over all edges in the graph.
   *
   * @param callback Callback object that is invoked once for each edge in the graph.
   */
  @Override
  public void iterateEdges(final IEdgeCallback<EdgeType> callback) {
    Preconditions.checkNotNull(callback, "Callback argument can't be null");

    for (final EdgeType edge : m_mappings.getEdges()) {
      if (callback.nextEdge(edge) == IterationMode.STOP) {
        return;
      }
    }
  }

  public void removeListener(final IZyGraphListener<NodeType, EdgeType> listener) {
    m_graphListeners.removeListener(listener);
  }

  /**
   * Removes a selection listener from the graph.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final IZyGraphSelectionListener listener) {
    m_selectionObserver.removeListener(listener);
  }

  public void removeListener(final IZyGraphVisibilityListener listener) {
    m_visibilityListener.removeListener(listener);
  }

  public void saveGraphAsGML(final File file) throws IOException {
    final LayoutGraphWriter layoutGraphWriter = new LayoutGraphWriter(m_graph);
    String path = file.getPath();
    if (path.toLowerCase().endsWith(".gml")) {
      path += ".gml";
    }

    layoutGraphWriter.write(path);
  }

  /**
   * Selects or unselects a single node.
   *
   * @param node The node to select or unselect.
   * @param selected True, to select the node. False, to unselect the node.
   */
  public void selectNode(final NodeType node, final boolean selected) {
    Preconditions.checkNotNull(node, "Node argument can't be null");

    m_graph.firePreEvent();

    ((IViewNode<?>) node.getRawNode()).setSelected(selected);

    m_graph.firePostEvent();
  }

  /**
   * Selects or unselects a list of nodes.
   *
   * @param nodes The nodes to select or unselect.
   * @param selected True, to select the nodes. False, to unselect the nodes.
   */
  public void selectNodes(final Collection<NodeType> nodes, final boolean selected) {
    Preconditions.checkNotNull(nodes, "Nodes argument can't be null");

    m_graph.firePreEvent();

    for (final NodeType node : nodes) {
      Preconditions.checkNotNull(
          node, "Error: The list of nodes to select contained an invalid node.");

      ((IViewNode<?>) node.getRawNode()).setSelected(selected);
    }

    m_graph.firePostEvent();
  }

  /**
   * Selects a list of nodes and unselects another list of nodes.
   *
   * @param toSelect The list of nodes to select.
   * @param toUnselect The list of nodes to unselect.
   */
  public void selectNodes(
      final Collection<NodeType> toSelect, final Collection<NodeType> toUnselect) {
    m_graph.firePreEvent();

    for (final NodeType node : toSelect) {
      Preconditions.checkNotNull(
          node, "Error: The list of nodes to select contained an invalid node");

      ((IViewNode<?>) node.getRawNode()).setSelected(true);
    }

    for (final NodeType node : toUnselect) {
      Preconditions.checkNotNull(
          node, "Error: The list of nodes to unselect contained an invalid node");

      ((IViewNode<?>) node.getRawNode()).setSelected(false);
    }

    m_graph.firePostEvent();
  }

  public void setProximityBrowser(
      final ZyDefaultProximityBrowser<NodeType, EdgeType> proximityBrowser) {
    if (m_proximityBrowser != null) {
      m_proximityBrowser.dispose();
    }

    m_proximityBrowser = proximityBrowser;
  }

  public void showNode(final NodeType node, final boolean show) {
    Preconditions.checkNotNull(node, "Node argument can't be null");

    ((IViewNode<?>) node.getRawNode()).setVisible(show);

    if (show) {
      @SuppressWarnings("unchecked")
      final ArrayList<NodeType> nodes = Lists.newArrayList(node);
      showNeighbors(nodes);
    }

    notifyVisibilityListeners();
  }

  public void showNodes(final Collection<NodeType> toShow, final boolean addNeighbours) {
    for (final NodeType node : toShow) {
      Preconditions.checkNotNull(node, "The list of nodes to show contained an invalid node");

      ((IViewNode<?>) node.getRawNode()).setVisible(true);
    }

    if (addNeighbours) {
      showNeighbors(toShow);
    }

    notifyVisibilityListeners();
  }

  public void showNodes(final Collection<NodeType> toShow, final Collection<NodeType> toHide) {
    final IProximitySettings proxiSettings = getSettings().getProximitySettings();
    final Set<NodeType> neighbors =
        ProximityRangeCalculator.getNeighbors(
            this,
            toShow,
            proxiSettings.getProximityBrowsingChildren(),
            proxiSettings.getProximityBrowsingParents());

    toHide.removeAll(neighbors);

    for (final NodeType node : sortLayers(toHide)) {
      Preconditions.checkNotNull(
          node, "Error: The list of nodes to hide contained an invalid node");

      ((IViewNode<?>) node.getRawNode()).setVisible(false);
    }

    showNeighbors(toShow);

    notifyVisibilityListeners();
  }

  public void showNodes(
      final Collection<NodeType> toShow,
      final Collection<NodeType> toHide,
      final boolean addNeighbours) {
    if (addNeighbours) {
      final IProximitySettings proxiSettings = getSettings().getProximitySettings();
      final Set<NodeType> neighbors =
          ProximityRangeCalculator.getNeighbors(
              this,
              toShow,
              proxiSettings.getProximityBrowsingChildren(),
              proxiSettings.getProximityBrowsingParents());

      toHide.removeAll(neighbors);
    }

    for (final NodeType node : sortLayers(toHide)) {
      Preconditions.checkNotNull(
          node, "Error: The list of nodes to hide contained an invalid node");

      ((IViewNode<?>) node.getRawNode()).setVisible(false);
    }

    if (addNeighbours) {
      showNeighbors(toShow);
    } else {
      for (final NodeType node : toShow) {
        Preconditions.checkNotNull(node, "The list of nodes to show contained an invalid node");

        ((IViewNode<?>) node.getRawNode()).setVisible(true);
      }
    }

    notifyVisibilityListeners();
  }

  public void updateViews() {
    m_graph.updateViews();
  }

  public void zoom(final double factor) {
    final double zoom = m_view.getZoom() * factor;
    m_view.setZoom(zoom);

    ZoomHelpers.keepZoomValid(m_view);

    m_graph.updateViews();
  }

  /** Zooms the graph in by the standard zoom factor. */
  public void zoomIn() {
    m_view.setZoom((m_view.getZoom() * 1.0) / STANDARD_ZOOM_FACTOR);

    ZoomHelpers.keepZoomValid(m_view);

    m_graph.updateViews();
  }

  /** Zooms the graph out by the standard zoom factor. */
  public void zoomOut() {
    m_view.setZoom(m_view.getZoom() * STANDARD_ZOOM_FACTOR);

    ZoomHelpers.keepZoomValid(m_view);

    m_graph.updateViews();
  }
}
