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

import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultTreeModel;

public class SelectionHistoryTreeGroupNode extends AbstractSelectionHistoryTreeNode {
  private static final Icon ICON_ALL_UNSELECTED_GRAPHNODES =
      ResourceUtils.getImageIcon("data/selectionicons/graph-selection-folder-all-unselected.png");
  private static final Icon ICON_ALL_SELECTED_GRAPHNODES =
      ResourceUtils.getImageIcon("data/selectionicons/graph-selection-folder-open.png");
  private static final Icon ICON_ALL_UNVISIBLE_GRAPHNODES =
      ResourceUtils.getImageIcon(
          "data/selectionicons/graph-selection-folder-all-unselected-gray.png");
  private static final Icon ICON_ALL_UNSELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES =
      ResourceUtils.getImageIcon(
          "data/selectionicons/graph-selection-folder-all-unselected-halfgray.png");
  private static final Icon ICON_ALL_VISIBLE_SOME_SELECTED_SOME_UNSELECTED_GRAPHNODES =
      ResourceUtils.getImageIcon("data/selectionicons/graph-selection-folder-some-unselected.png");
  private static final Icon ICON_SOME_SELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES =
      ResourceUtils.getImageIcon(
          "data/selectionicons/graph-selection-folder-some-unselected-halfgray.png");
  private static final Icon ICON_EMPTY_FOLDER =
      ResourceUtils.getImageIcon("data/selectionicons/graph-selection-folder-empty.png");
  private static final Icon ICON_DUMMY =
      ResourceUtils.getImageIcon("data/selectionicons/graph-selection-folder-closed.png");

  private final ISnapshotListener snapshotListener = new InternalSnapshotListener();

  private final SelectionSnapshot snapshot;

  private final int snapshotCount;

  public SelectionHistoryTreeGroupNode(final SelectionSnapshot snapshot, final int childCount) {
    super(new SelectionHistoryTreeNodeWrapper(snapshot, childCount).toString());

    this.snapshot = snapshot;
    snapshot.addListener(snapshotListener);

    snapshotCount = childCount;
  }

  public void dispose() {
    snapshot.removeListener(snapshotListener);
  }

  @Override
  public Icon getIcon() {
    int countAll = 0;
    int selected = 0;
    int unselected = 0;
    int invisible = 0;

    final Collection<ZyGraphNode<?>> selection = snapshot.getSelection();

    for (final ZyGraphNode<?> graphNode : selection) {
      if (graphNode.isSelected()) {
        selected++;
      } else {
        unselected++;
      }

      if (!graphNode.isVisible()) {
        invisible++;
      }

      countAll++;
    }

    if (countAll == 0) {
      return ICON_EMPTY_FOLDER;
    } else if (invisible == countAll) {
      return ICON_ALL_UNVISIBLE_GRAPHNODES;
    } else if (selected == countAll) {
      return ICON_ALL_SELECTED_GRAPHNODES;
    } else if (unselected == countAll && invisible == 0) {
      return ICON_ALL_UNSELECTED_GRAPHNODES;
    } else if (selected == 0) {
      return ICON_ALL_UNSELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES;
    } else if (invisible == 0) {
      return ICON_ALL_VISIBLE_SOME_SELECTED_SOME_UNSELECTED_GRAPHNODES;
    } else if (invisible != 0 && selected != 0) {
      return ICON_SOME_SELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES;
    }

    return ICON_DUMMY;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    final JPopupMenu popupMenu = new JPopupMenu();
    final BinDiffGraph<?, ?> graph = getRootNode().getGraph();

    if (graph instanceof SingleGraph) {
      if (graph.getGraphType() == EGraphType.CALL_GRAPH) {
        final JMenuItem copyFunctionAddresses =
            new JMenuItem(
                new AbstractAction("Copy Function Addresses") {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                    final StringBuilder text = new StringBuilder();
                    for (final SingleDiffNode node : snapshot.getSingleGraphSelection()) {
                      text.append(((RawFunction) node.getRawNode()).getAddress().toHexString());
                      text.append("\n");
                    }

                    ClipboardHelpers.copyToClipboard(text.toString());
                  }
                });

        final JMenuItem copyFunctionNames =
            new JMenuItem(
                new AbstractAction("Copy Function Names") {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                    final StringBuilder text = new StringBuilder();
                    for (final SingleDiffNode node : snapshot.getSingleGraphSelection()) {
                      text.append(((RawFunction) node.getRawNode()).getName());
                      text.append("\n");
                    }

                    ClipboardHelpers.copyToClipboard(text.toString());
                  }
                });

        popupMenu.add(copyFunctionAddresses);
        popupMenu.add(copyFunctionNames);
      } else if (graph.getGraphType() == EGraphType.FLOW_GRAPH) {
        final JMenuItem copyBasicblockAddresses =
            new JMenuItem(
                new AbstractAction("Copy Basic Block Addresses") {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                    final StringBuilder text = new StringBuilder();
                    for (final SingleDiffNode node : snapshot.getSingleGraphSelection()) {
                      text.append(((RawBasicBlock) node.getRawNode()).getAddress().toHexString());
                      text.append("\n");
                    }

                    ClipboardHelpers.copyToClipboard(text.toString());
                  }
                });

        popupMenu.add(copyBasicblockAddresses);
      }
    } else if (getRootNode().getGraph() instanceof CombinedGraph) {
      if (graph.getGraphType() == EGraphType.CALL_GRAPH) {
        final JMenuItem copyPriFunctionAddresses =
            new JMenuItem(
                new AbstractAction("Copy Primary Function Addresses") {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                    final StringBuilder text = new StringBuilder();
                    for (final CombinedDiffNode node : snapshot.getCombinedGraphSelection()) {
                      final RawFunction priNode = (RawFunction) node.getPrimaryRawNode();
                      if (priNode != null) {
                        text.append(priNode.getAddress().toHexString());
                        text.append("\n");
                      }
                    }

                    ClipboardHelpers.copyToClipboard(text.toString());
                  }
                });

        final JMenuItem copySecFunctionAddresses =
            new JMenuItem(
                new AbstractAction("Copy Secondary Function Addresses") {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                    final StringBuilder text = new StringBuilder();
                    for (final CombinedDiffNode node : snapshot.getCombinedGraphSelection()) {
                      final RawFunction secNode = (RawFunction) node.getSecondaryRawNode();
                      if (secNode != null) {
                        text.append(secNode.getAddress().toHexString());
                        text.append("\n");
                      }
                    }

                    ClipboardHelpers.copyToClipboard(text.toString());
                  }
                });

        final JMenuItem copyPriFunctionNames =
            new JMenuItem(
                new AbstractAction("Copy Primary Function Names") {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                    final StringBuilder text = new StringBuilder();
                    for (final CombinedDiffNode node : snapshot.getCombinedGraphSelection()) {
                      final RawFunction priNode = (RawFunction) node.getPrimaryRawNode();
                      if (priNode != null) {
                        text.append(priNode.getAddress().toHexString());
                        text.append("\n");
                      }
                    }

                    ClipboardHelpers.copyToClipboard(text.toString());
                  }
                });

        final JMenuItem copySecFunctionNames =
            new JMenuItem(
                new AbstractAction("Copy Secondary Function Names") {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                    final StringBuilder text = new StringBuilder();
                    for (final CombinedDiffNode node : snapshot.getCombinedGraphSelection()) {
                      final RawFunction secNode = (RawFunction) node.getSecondaryRawNode();
                      if (secNode != null) {
                        text.append(secNode.getAddress().toHexString());
                        text.append("\n");
                      }
                    }

                    ClipboardHelpers.copyToClipboard(text.toString());
                  }
                });

        popupMenu.add(copyPriFunctionAddresses);
        popupMenu.add(copyPriFunctionNames);
        popupMenu.add(copySecFunctionAddresses);
        popupMenu.add(copySecFunctionNames);

      } else if (graph.getGraphType() == EGraphType.FLOW_GRAPH) {
        final JMenuItem copyPriBasicblockAddresses =
            new JMenuItem(
                new AbstractAction("Copy Primary Basic Block Addresses") {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                    final StringBuilder text = new StringBuilder();
                    for (final CombinedDiffNode node : snapshot.getCombinedGraphSelection()) {
                      final RawBasicBlock priNode = (RawBasicBlock) node.getPrimaryRawNode();
                      if (priNode != null) {
                        text.append(priNode.getAddress().toHexString());
                        text.append("\n");
                      }
                    }

                    ClipboardHelpers.copyToClipboard(text.toString());
                  }
                });

        final JMenuItem copySecBasicblockAddresses =
            new JMenuItem(
                new AbstractAction("Copy Secondary Basic Block Addresses") {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                    final StringBuilder text = new StringBuilder();
                    for (final CombinedDiffNode node : snapshot.getCombinedGraphSelection()) {
                      final RawBasicBlock secNode = (RawBasicBlock) node.getSecondaryRawNode();
                      if (secNode != null) {
                        text.append(secNode.getAddress().toHexString());
                        text.append("\n");
                      }
                    }

                    ClipboardHelpers.copyToClipboard(text.toString());
                  }
                });

        popupMenu.add(copyPriBasicblockAddresses);
        popupMenu.add(copySecBasicblockAddresses);
      }
    }

    return popupMenu;
  }

  public SelectionSnapshot getSnapshot() {
    return snapshot;
  }

  private final class InternalSnapshotListener implements ISnapshotListener {
    @Override
    public void addedNode(final ZyGraphNode<? extends IViewNode<?>> node) {
      final SelectionHistoryTreeNode newTreeNode = new SelectionHistoryTreeNode(node);
      add(newTreeNode);

      setUserObject(new SelectionHistoryTreeNodeWrapper(snapshot, snapshotCount));
    }

    @Override
    public void finished() {
      ((DefaultTreeModel) getTree().getModel())
          .nodeStructureChanged(SelectionHistoryTreeGroupNode.this);
      getTree().updateUI();
    }

    @Override
    public void removedNode(final ZyGraphNode<? extends IViewNode<?>> removedNode) {
      for (int j = 0; j < getChildCount(); ++j) {
        final SelectionHistoryTreeNode leaf = (SelectionHistoryTreeNode) getChildAt(j);
        if (leaf.getNode() == removedNode) {
          ((DefaultTreeModel) getTree().getModel()).removeNodeFromParent(leaf);
          leaf.removeFromParent();

          setUserObject(new SelectionHistoryTreeNodeWrapper(snapshot, snapshotCount));

          break;
        }
      }
    }
  }
}
