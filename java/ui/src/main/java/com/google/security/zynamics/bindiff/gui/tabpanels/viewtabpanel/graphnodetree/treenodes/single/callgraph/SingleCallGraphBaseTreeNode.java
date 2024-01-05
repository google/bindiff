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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.single.callgraph;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.ISearchableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractBaseTreeNode;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class SingleCallGraphBaseTreeNode extends AbstractBaseTreeNode {
  private static final Icon CALLGRAPH_ICON =
      ResourceUtils.getImageIcon("data/treeicons/callgraph.png");

  private List<SingleCallGraphFunctionTreeNode> functionNodes = new ArrayList<>();

  private SingleDiffNode lastSelectedTreeNode;

  public SingleCallGraphBaseTreeNode(final SingleCallGraphRootTreeNode rootNode) {
    super(rootNode);

    createChildren();
  }

  @Override
  protected void delete() {
    super.delete();

    functionNodes.clear();
    functionNodes = null;
    lastSelectedTreeNode = null;
  }

  @Override
  protected void updateTreeNodes(final boolean updateSearch) {
    final TreeNodeSearcher searcher = getSearcher();
    final GraphNodeMultiFilter filter = getFilter();

    final List<SingleCallGraphFunctionTreeNode> treeNodes = new ArrayList<>();

    if (searcher.getUseTemporaryResults() && !"".equals(searcher.getSearchString())) {
      for (int i = 1; i < getChildCount(); ++i) {
        treeNodes.add((SingleCallGraphFunctionTreeNode) getChildAt(i));
      }
    } else {
      treeNodes.addAll(functionNodes);
    }

    removeAllChildren();

    List<? extends ISearchableTreeNode> searchedTreeNodes = new ArrayList<>();

    if (updateSearch) {
      if (!"".equals(searcher.getSearchString())) {
        searchedTreeNodes = searcher.search(treeNodes);
        treeNodes.clear();

        for (final ISearchableTreeNode searchedTreeNode : searchedTreeNodes) {
          treeNodes.add((SingleCallGraphFunctionTreeNode) searchedTreeNode);
        }
      } else {
        searcher.search(null);
      }
    }

    final List<SingleCallGraphFunctionTreeNode> filteredTreeNodes = new ArrayList<>();

    for (final SingleCallGraphFunctionTreeNode treeNode : treeNodes) {
      if (filter.filterRawFunction(treeNode.getFunction())) {
        if (!updateSearch && !"".equals(searcher.getSearchString())) {
          if (searcher.isSearchHit(treeNode.getSingleDiffNode())) {
            filteredTreeNodes.add(treeNode);
          }
        } else {
          filteredTreeNodes.add(treeNode);
        }
      }
    }

    for (final Comparator<ISortableTreeNode> comparator :
        getRootNode().getSorter().getSingleFunctionTreeNodeComparatorList()) {
      Collections.sort(filteredTreeNodes, comparator);
    }

    for (final SingleCallGraphFunctionTreeNode treeNode : filteredTreeNodes) {
      add(treeNode);
    }

    getTree().getModel().nodeStructureChanged(this);
    getTree().updateUI();
  }

  @Override
  public void createChildren() {
    functionNodes.clear();

    for (final SingleDiffNode diffNode : getRootNode().getGraph().getNodes()) {
      functionNodes.add(new SingleCallGraphFunctionTreeNode(getRootNode(), diffNode));
    }

    updateTreeNodes(false);
  }

  @Override
  public Icon getIcon() {
    return CALLGRAPH_ICON;
  }

  public SingleDiffNode getLastSelectedGraphNode() {
    return lastSelectedTreeNode;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    final JPopupMenu popupMenu = new JPopupMenu();

    final ESide side = getRootNode().getSide();

    final JMenuItem copyImageNameItem =
        new JMenuItem(
            new AbstractAction("Copy Image Name") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final String imageName = getDiff().getMetadata().getImageName(side);
                ClipboardHelpers.copyToClipboard(imageName);
              }
            });
    final JMenuItem copyImageHashItem =
        new JMenuItem(
            new AbstractAction("Copy Image Hash") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final String imageName = getDiff().getMetadata().getImageName(side);
                ClipboardHelpers.copyToClipboard(imageName);
              }
            });
    final JMenuItem copyIdbNameItem =
        new JMenuItem(
            new AbstractAction("Copy IDB Name") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final String idbName = getDiff().getMetadata().getIdbName(side);
                ClipboardHelpers.copyToClipboard(idbName);
              }
            });

    popupMenu.add(copyImageNameItem);
    popupMenu.add(copyImageHashItem);
    popupMenu.add(copyIdbNameItem);

    return popupMenu;
  }

  @Override
  public SingleCallGraphRootTreeNode getRootNode() {
    return (SingleCallGraphRootTreeNode) getAbstractRootNode();
  }

  @Override
  public String getTooltipText() {
    return null;
  }

  @Override
  public void handleMouseEvent(final MouseEvent event) {
    if (event.isPopupTrigger()) {
      getPopupMenu().show(getTree(), event.getX(), event.getY());
    }
  }

  @Override
  public boolean isSelected() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  public void setLastSelectedGraphNode(final SingleDiffNode diffNode) {
    lastSelectedTreeNode = diffNode;
  }

  @Override
  public String toString() {
    return String.format("Call Graph (%d / %d)", getChildCount(), functionNodes.size());
  }
}
