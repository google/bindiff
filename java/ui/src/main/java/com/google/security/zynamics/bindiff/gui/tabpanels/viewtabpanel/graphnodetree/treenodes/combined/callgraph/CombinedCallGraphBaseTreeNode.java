// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.callgraph;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.ISearchableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractBaseTreeNode;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class CombinedCallGraphBaseTreeNode extends AbstractBaseTreeNode {
  private static final Icon CALLGRAPH_ICON =
      ImageUtils.getImageIcon("data/treeicons/callgraph.png");

  private List<CombinedCallGraphFunctionTreeNode> functionNodes = new ArrayList<>();

  private CombinedDiffNode lastSelectedTreeNode;

  public CombinedCallGraphBaseTreeNode(final CombinedCallGraphRootTreeNode rootNode) {
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

    final List<CombinedCallGraphFunctionTreeNode> treeNodes = new ArrayList<>();

    if (searcher.getUseTemporaryResults() && !"".equals(searcher.getSearchString())) {
      for (int i = 1; i < getChildCount(); ++i) {
        treeNodes.add((CombinedCallGraphFunctionTreeNode) getChildAt(i));
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
          treeNodes.add((CombinedCallGraphFunctionTreeNode) searchedTreeNode);
        }
      } else {
        searcher.search(null);
      }
    }

    final List<CombinedCallGraphFunctionTreeNode> filteredTreeNodes = new ArrayList<>();

    for (final CombinedCallGraphFunctionTreeNode treeNode : treeNodes) {
      if (filter.filterRawCombinedFunction(treeNode.getCombinedFunction())) {
        if (!updateSearch && !"".equals(searcher.getSearchString())) {
          if (searcher.isSearchHit(treeNode.getCombinedDiffNode())) {
            filteredTreeNodes.add(treeNode);
          }
        } else {
          filteredTreeNodes.add(treeNode);
        }
      }
    }

    for (final Comparator<ISortableTreeNode> comparator :
        getRootNode().getSorter().getCombinedFunctionTreeNodeComparatorList()) {
      Collections.sort(filteredTreeNodes, comparator);
    }

    for (final CombinedCallGraphFunctionTreeNode treeNode : filteredTreeNodes) {
      add(treeNode);
    }

    getTree().getModel().nodeStructureChanged(this);

    getTree().updateUI();
  }

  @Override
  public void createChildren() {
    functionNodes.clear();

    for (final CombinedDiffNode node : getRootNode().getGraph().getNodes()) {
      functionNodes.add(new CombinedCallGraphFunctionTreeNode(getRootNode(), node));
    }

    updateTreeNodes(false);
  }

  @Override
  public Icon getIcon() {
    return CALLGRAPH_ICON;
  }

  public CombinedDiffNode getLastSelectedGraphNode() {
    return lastSelectedTreeNode;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    final JPopupMenu popupMenu = new JPopupMenu();

    final JMenuItem copyPriImageNameItem =
        new JMenuItem(
            new AbstractAction("Copy Primary Image Name") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final String imageName = getDiff().getMetadata().getImageName(ESide.PRIMARY);
                ClipboardHelpers.copyToClipboard(imageName);
              }
            });
    final JMenuItem copySecImageNameItem =
        new JMenuItem(
            new AbstractAction("Copy Secondary Image Name") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final String imageName = getDiff().getMetadata().getImageName(ESide.SECONDARY);
                ClipboardHelpers.copyToClipboard(imageName);
              }
            });

    final JMenuItem copyPriImageHashItem =
        new JMenuItem(
            new AbstractAction("Copy Primary Image Hash") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final String imageName = getDiff().getMetadata().getImageHash(ESide.PRIMARY);
                ClipboardHelpers.copyToClipboard(imageName);
              }
            });
    final JMenuItem copySecImageHashItem =
        new JMenuItem(
            new AbstractAction("Copy Secondary Image Hash") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final String imageName = getDiff().getMetadata().getImageHash(ESide.SECONDARY);
                ClipboardHelpers.copyToClipboard(imageName);
              }
            });

    final JMenuItem copyPriIdbNameItem =
        new JMenuItem(
            new AbstractAction("Copy Primary IDB Name") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final String idbName = getDiff().getMetadata().getIdbName(ESide.PRIMARY);
                ClipboardHelpers.copyToClipboard(idbName);
              }
            });
    final JMenuItem copySecIdbNameItem =
        new JMenuItem(
            new AbstractAction("Copy Secondary IDB Name") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final String idbName = getDiff().getMetadata().getIdbName(ESide.SECONDARY);
                ClipboardHelpers.copyToClipboard(idbName);
              }
            });

    popupMenu.add(copyPriImageNameItem);
    popupMenu.add(copyPriImageHashItem);
    popupMenu.add(copyPriIdbNameItem);
    popupMenu.add(new JSeparator());
    popupMenu.add(copySecImageNameItem);
    popupMenu.add(copySecImageHashItem);
    popupMenu.add(copySecIdbNameItem);

    return popupMenu;
  }

  @Override
  public CombinedCallGraphRootTreeNode getRootNode() {
    return (CombinedCallGraphRootTreeNode) getAbstractRootNode();
  }

  public void setLastSelectedGraphNode(final CombinedDiffNode node) {
    lastSelectedTreeNode = node;
  }

  @Override
  public String toString() {
    return String.format("Combined Call Graph (%d / %d)", getChildCount(), functionNodes.size());
  }
}
