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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeFilter;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeFilter.Criterion;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphSelectionListener;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphVisibilityListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class SelectionHistoryTreePanel extends JPanel {
  private SelectionHistoryTree tree;
  private DefaultTreeModel model;

  private final InternalGraphSelectionListener graphSelectionListener =
      new InternalGraphSelectionListener();
  private final InternalGraphVisibilityListener graphVisibilityListener =
      new InternalGraphVisibilityListener();

  private final InternalSelectionHistoryListener selectionHistoryListener =
      new InternalSelectionHistoryListener();

  private final InternalTreeSelectionListener treeSelectionListener =
      new InternalTreeSelectionListener();
  private final InternalTreeMouseListener treeMouseListener = new InternalTreeMouseListener();

  private int childCount = 0;

  private BinDiffGraph<?, ?> graph;

  private final SelectionHistory selectionHistory;

  public SelectionHistoryTreePanel(
      final ViewTabPanelFunctions controller,
      final BinDiffGraph<?, ?> graph,
      final SelectionHistory selectionHistory) {
    super(new BorderLayout());

    checkNotNull(controller);
    this.graph = checkNotNull(graph);
    this.selectionHistory = checkNotNull(selectionHistory);
    tree = createTree(controller);

    createPanel();

    this.selectionHistory.addHistoryListener(selectionHistoryListener);

    graph.getIntermediateListeners().addIntermediateListener(graphSelectionListener);
    graph.getIntermediateListeners().addIntermediateListener(graphVisibilityListener);

    // Listen for when the selection changes
    tree.addTreeSelectionListener(treeSelectionListener);
    tree.addMouseListener(treeMouseListener);

    ToolTipManager.sharedInstance().registerComponent(tree);
  }

  private void createPanel() {
    tree.setRootVisible(true);
    tree.setSelectionModel(null);
    add(new JScrollPane(tree), BorderLayout.CENTER);
  }

  private SelectionHistoryTree createTree(final ViewTabPanelFunctions controller) {
    final SelectionHistoryRootNode rootNode =
        new SelectionHistoryRootNode(controller, graph, "Selection History");

    final SelectionHistoryTree tree = new SelectionHistoryTree(rootNode);
    rootNode.setTree(tree);

    model = new DefaultTreeModel(rootNode);

    model.nodeStructureChanged(rootNode);

    tree.setRootVisible(true);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    return tree;
  }

  private void insertSnapshot(final SelectionSnapshot s) {
    // New Group node
    final SelectionHistoryTreeGroupNode selection =
        new SelectionHistoryTreeGroupNode(s, childCount);

    // Add the selected node addresses to the group node
    for (final ZyGraphNode<?> node : s.getSelection()) {
      final SelectionHistoryTreeNode childNode = new SelectionHistoryTreeNode(node);

      selection.add(childNode);
    }

    childCount++;

    // Add the new group node to the
    model.insertNodeInto(selection, (SelectionHistoryRootNode) model.getRoot(), 0);
  }

  private void selectCombinedNodes(final Collection<CombinedDiffNode> selection) {
    graph.getIntermediateListeners().removeIntermediateListener(graphSelectionListener);

    if (graph instanceof CombinedGraph) {
      final List<CombinedDiffNode> selectedNodes =
          GraphNodeFilter.filterNodes((CombinedGraph) graph, Criterion.SELECTED);

      ((CombinedGraph) graph).selectNodes(selectedNodes, false);
      ((CombinedGraph) graph).selectNodes(selection, true);
    }

    graph.getIntermediateListeners().addIntermediateListener(graphSelectionListener);
  }

  private void selectSingleNodes(final Collection<SingleDiffNode> selection) {
    graph.getIntermediateListeners().removeIntermediateListener(graphSelectionListener);

    if (graph instanceof SingleGraph) {
      final List<SingleDiffNode> selectedNodes =
          GraphNodeFilter.filterNodes((SingleGraph) graph, Criterion.SELECTED);

      ((SingleGraph) graph).selectNodes(selectedNodes, false);
      ((SingleGraph) graph).selectNodes(selection, true);
    }

    graph.getIntermediateListeners().addIntermediateListener(graphSelectionListener);
  }

  private void unselectCombinedNodes(final Collection<CombinedDiffNode> selection) {
    graph.getIntermediateListeners().removeIntermediateListener(graphSelectionListener);

    if (graph instanceof CombinedGraph) {
      ((CombinedGraph) graph).selectNodes(selection, false);
    }
    graph.getIntermediateListeners().addIntermediateListener(graphSelectionListener);
  }

  private void unselectSingleNodes(final Collection<SingleDiffNode> selection) {
    graph.getIntermediateListeners().removeIntermediateListener(graphSelectionListener);

    if (graph instanceof SingleGraph) {
      ((SingleGraph) graph).selectNodes(selection, false);
    }
    graph.getIntermediateListeners().addIntermediateListener(graphSelectionListener);
  }

  public void dispose() {
    selectionHistory.removeHistoryListener(selectionHistoryListener);

    graph.getIntermediateListeners().removeIntermediateListener(graphSelectionListener);
    graph.getIntermediateListeners().removeIntermediateListener(graphVisibilityListener);

    tree.removeMouseListener(treeMouseListener);
    tree.removeTreeSelectionListener(treeSelectionListener);

    tree.setSelectionModel(new DefaultTreeSelectionModel());

    tree.setCellRenderer(null);

    tree.dispose();

    tree = null;
    graph = null;
  }

  public int getChildCount() {
    return childCount;
  }

  private class InternalGraphSelectionListener implements IZyGraphSelectionListener {
    private SelectionSnapshot lastSnapshot;

    @Override
    public void selectionChanged() {
      // Graph selection changed => Add a new selection snapshot
      @SuppressWarnings("unchecked") // This is safe, actually
      final SelectionSnapshot snapshot =
          new SelectionSnapshot(
              (Collection<ZyGraphNode<?>>) GraphNodeFilter.filterNodes(graph, Criterion.SELECTED));

      if (snapshot.getNumberOfSelectedNodes() != 0 && !snapshot.equals(lastSnapshot)) {
        selectionHistory.addSnapshot(snapshot);

        lastSnapshot = snapshot;
      }

      updateUI();
    }
  }

  private class InternalGraphVisibilityListener implements IZyGraphVisibilityListener {
    @Override
    public void nodeDeleted() {
      updateUI();
    }

    @Override
    public void visibilityChanged() {
      updateUI();
    }
  }

  private class InternalSelectionHistoryListener implements ISelectionHistoryListener {
    @Override
    public void finishedRedo() {
      graph.getIntermediateListeners().addIntermediateListener(graphSelectionListener);

      tree.updateUI();
    }

    @Override
    public void finishedUndo() {
      graph.getIntermediateListeners().addIntermediateListener(graphSelectionListener);

      tree.updateUI();
    }

    @Override
    public void snapshotAdded(final SelectionSnapshot undoSelection) {
      // Snapshot added to history => Add a new node to the tree

      insertSnapshot(undoSelection);
    }

    @Override
    public void snapshotRemoved() {
      // A snapshot was removed from the history => Remove the corresponding node
      // from the tree.

      final SelectionHistoryRootNode root = (SelectionHistoryRootNode) model.getRoot();

      final int children = root.getChildCount();

      final SelectionHistoryTreeGroupNode nodeToDelete =
          (SelectionHistoryTreeGroupNode) model.getChild(root, children - 1);
      nodeToDelete.dispose();

      model.removeNodeFromParent(nodeToDelete);
    }

    @Override
    public void startedRedo() {
      graph.getIntermediateListeners().removeIntermediateListener(graphSelectionListener);
    }

    @Override
    public void startedUndo() {
      graph.getIntermediateListeners().removeIntermediateListener(graphSelectionListener);
    }
  }

  private class InternalTreeMouseListener extends MouseAdapter {
    private TreePath getTreePath(final MouseEvent event) {
      return tree.getPathForLocation(event.getX(), event.getY());
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      final TreePath path = getTreePath(event);

      if (path == null) {
        return;
      }

      final Object treeNode = path.getLastPathComponent();

      if (event.getButton() == 1) {
        // Select all nodes of the snapshot. If all nodes
        // are already selected, then deselect them.

        graph.getPrimaryGraph().getSelectionHistory().setEnabled(false);
        graph.getSecondaryGraph().getSelectionHistory().setEnabled(false);
        graph.getCombinedGraph().getSelectionHistory().setEnabled(false);

        if (treeNode instanceof SelectionHistoryTreeGroupNode) {
          final SelectionHistoryTreeGroupNode treenode = (SelectionHistoryTreeGroupNode) treeNode;

          if (graph instanceof SingleGraph) {
            final Collection<SingleDiffNode> selection =
                treenode.getSnapshot().getSingleGraphSelection();

            final Collection<SingleDiffNode> alreadySelected =
                GraphNodeFilter.filterNodes((SingleGraph) graph, Criterion.SELECTED);

            // TODO(cblichmann): Dedupe without the extra HashSet. Maybe deduping is not even
            // necessary, since any node can be selected only once. Same below.
            if (new HashSet<>(selection).equals(new HashSet<>(alreadySelected))) {
              unselectSingleNodes(selection);
            } else {
              selectSingleNodes(selection);
            }
          } else if (graph instanceof CombinedGraph) {
            final Collection<CombinedDiffNode> selection =
                treenode.getSnapshot().getCombinedGraphSelection();

            final Collection<CombinedDiffNode> alreadySelected =
                GraphNodeFilter.filterNodes((CombinedGraph) graph, Criterion.SELECTED);

            if (new HashSet<>(selection).equals(new HashSet<>(alreadySelected))) {
              unselectCombinedNodes(selection);
            } else {
              selectCombinedNodes(selection);
            }
          }
        } else if (treeNode instanceof SelectionHistoryTreeNode) {
          final SelectionHistoryTreeNode treenode = (SelectionHistoryTreeNode) treeNode;

          final ZyGraphNode<?> graphNode = treenode.getNode();

          final boolean graphNodeSelected = graphNode.isSelected();

          graph.getIntermediateListeners().removeIntermediateListener(graphSelectionListener);

          if (graphNode instanceof SingleDiffNode) {
            ((SingleGraph) graph).selectNode((SingleDiffNode) graphNode, !graphNodeSelected);
          } else if (graphNode instanceof CombinedDiffNode) {
            ((CombinedGraph) graph).selectNode((CombinedDiffNode) graphNode, !graphNodeSelected);
          }

          graph.getIntermediateListeners().addIntermediateListener(graphSelectionListener);
        }

        graph.getPrimaryGraph().getSelectionHistory().setEnabled(true);
        graph.getSecondaryGraph().getSelectionHistory().setEnabled(true);
        graph.getCombinedGraph().getSelectionHistory().setEnabled(true);
      }
    }
  }

  private class InternalTreeSelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(final TreeSelectionEvent event) {
      // Every time a group node was selected, the corresponding
      // snapshot must be reloaded.

      final SelectionHistoryTreeGroupNode node =
          (SelectionHistoryTreeGroupNode) tree.getLastSelectedPathComponent();

      if (node == null || node.getParent() != model.getRoot()) {
        return;
      }

      final int index =
          selectionHistory.getNumberOfSnapshots()
              - model.getIndexOfChild(model.getRoot(), node)
              - 1;

      final SelectionSnapshot snapshot = selectionHistory.getSnapshot(index);

      // Listener must be removed; otherwise the snapshot restoration would
      // cause new entries to be added to the snapshot history.
      if (graph instanceof SingleGraph) {
        selectSingleNodes(snapshot.getSingleGraphSelection());
      } else if (graph instanceof CombinedGraph) {
        selectCombinedNodes(snapshot.getCombinedGraphSelection());
      }

      // TODO: If possible remove all updateUI() calls because it does set the look and feel and
      // just a redraw is what we want.
      tree.updateUI();
    }
  }
}
