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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.single.flowgraph;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.ISearchableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractBaseTreeNode;
import com.google.security.zynamics.bindiff.project.matches.IMatchesChangeListener;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
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

public class SingleFlowGraphBaseTreeNode extends AbstractBaseTreeNode {
  private static final Icon MATCHED_IDENTICAL_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/matched-functions.png");
  private static final Icon MATCHED_INSTRUCTIONCHANGED_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/instructions-changed-function.png");
  private static final Icon MATCHED_STRUCTURALCHANGED_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/structural-changed-function.png");

  private static final Icon PRIMARY_UNMATCHED_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-functions.png");
  private static final Icon SECONDRAY_UNMATCHED_FUNCNTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/secondary-unmatched-functions.png");

  private final IMatchesChangeListener matchesChangeListener = new InternalMatchesChangeListener();

  private final List<SingleFlowGraphBasicBlockTreeNode> basicBlockTreeNodes = new ArrayList<>();

  private SingleDiffNode lastSelectedTreeNode;

  public SingleFlowGraphBaseTreeNode(final SingleFlowGraphRootTreeNode rootNode) {
    super(rootNode);

    createChildren();

    getDiff().getMatches().addListener(matchesChangeListener);
  }

  private void replaceBasicblockTreeNode(final IAddress address) {
    SingleDiffNode newDiffNode = null;
    for (final SingleDiffNode diffNode : getRootNode().getGraph().getNodes()) {
      if (address.equals(diffNode.getRawNode().getAddress())) {
        newDiffNode = diffNode;

        break;
      }
    }

    if (newDiffNode != null) {
      for (int index = 0; index < getChildCount(); ++index) {
        final SingleFlowGraphBasicBlockTreeNode treeNode =
            (SingleFlowGraphBasicBlockTreeNode) getChildAt(index);

        final RawBasicBlock oldRawBasicblock = treeNode.getBasicblock();
        if (address.equals(oldRawBasicblock.getAddress())) {
          final SingleFlowGraphBasicBlockTreeNode newTreeNode =
              new SingleFlowGraphBasicBlockTreeNode(getRootNode(), newDiffNode);
          insert(newTreeNode, index);

          final int insertIndex = basicBlockTreeNodes.indexOf(treeNode);
          basicBlockTreeNodes.set(insertIndex, newTreeNode);

          break;
        }
      }

      updateTreeNodes(true);
    }
  }

  @Override
  protected void delete() {
    getDiff().getMatches().removeListener(matchesChangeListener);

    super.delete();
  }

  @Override
  protected void updateTreeNodes(final boolean updateSearch) {
    final TreeNodeSearcher searcher = getSearcher();
    final GraphNodeMultiFilter filter = getFilter();

    final List<SingleFlowGraphBasicBlockTreeNode> treeNodes = new ArrayList<>();

    if (searcher.getUseTemporaryResults() && !"".equals(searcher.getSearchString())) {
      for (int i = 0; i < getChildCount(); ++i) {
        treeNodes.add((SingleFlowGraphBasicBlockTreeNode) getChildAt(i));
      }
    } else {
      treeNodes.addAll(basicBlockTreeNodes);
    }

    removeAllChildren();

    List<? extends ISearchableTreeNode> searchedTreeNodes = new ArrayList<>();

    if (updateSearch) {
      if (!"".equals(searcher.getSearchString())) {
        searchedTreeNodes = searcher.search(treeNodes);

        treeNodes.clear();

        for (final ISearchableTreeNode searchedTreeNode : searchedTreeNodes) {
          treeNodes.add((SingleFlowGraphBasicBlockTreeNode) searchedTreeNode);
        }
      } else {
        searcher.search(null);
      }
    }

    final List<SingleFlowGraphBasicBlockTreeNode> filteredTreeNodes = new ArrayList<>();

    for (final SingleFlowGraphBasicBlockTreeNode treeNode : treeNodes) {
      if (filter.filterRawBasicBlock(treeNode.getBasicblock())) {
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
        getRootNode().getSorter().getSingleBasicBlockTreeNodeComparatorList()) {
      Collections.sort(filteredTreeNodes, comparator);
    }

    for (final SingleFlowGraphBasicBlockTreeNode treeNode : filteredTreeNodes) {
      add(treeNode);
    }

    getTree().getModel().nodeStructureChanged(this);

    getTree().updateUI();
  }

  @Override
  public void createChildren() {
    basicBlockTreeNodes.clear();

    final SingleFlowGraphRootTreeNode rootNode = getRootNode();
    for (final SingleDiffNode diffNode : getRootNode().getGraph().getNodes()) {
      basicBlockTreeNodes.add(new SingleFlowGraphBasicBlockTreeNode(rootNode, diffNode));
    }

    updateTreeNodes(false);
  }

  @Override
  public Icon getIcon() {
    final FlowGraphViewData view = getRootNode().getView();
    final ESide side = getRootNode().getSide();
    final RawFlowGraph flowgraph = view.getRawGraph(side);

    final IAddress functionAddr = flowgraph.getAddress();

    final RawFunction function = getDiff().getCallGraph(side).getFunction(functionAddr);
    final RawFunction priFunction;

    final EMatchState matchState = function.getMatchState();

    if (matchState == EMatchState.MATCHED) {
      final IAddress otherSideAddr = function.getMatchedFunctionAddress();

      if (function.getSide() == ESide.PRIMARY) {
        priFunction = function;
      } else {
        priFunction = getDiff().getFunction(otherSideAddr, ESide.PRIMARY);
      }

      if (priFunction.isIdenticalMatch()) {
        return MATCHED_IDENTICAL_FUNCTION_ICON;
      } else if (priFunction.isChangedInstructionsOnlyMatch()) {
        return MATCHED_INSTRUCTIONCHANGED_FUNCTION_ICON;

      } else if (priFunction.isChangedStructuralMatch()) {
        return MATCHED_STRUCTURALCHANGED_FUNCTION_ICON;
      }
    } else if (matchState == EMatchState.PRIMARY_UNMATCHED) {
      return PRIMARY_UNMATCHED_FUNCTION_ICON;
    }

    return SECONDRAY_UNMATCHED_FUNCNTION_ICON;
  }

  public SingleDiffNode getLastSelectedGraphNode() {
    return lastSelectedTreeNode;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    final JPopupMenu popupMenu = new JPopupMenu();

    final ESide side = getRootNode().getSide();

    final JMenuItem copyFunctionAddressItem =
        new JMenuItem(new AbstractAction("Copy Function Address") {
          @Override
          public void actionPerformed(final ActionEvent e) {
            final FlowGraphViewData view = (FlowGraphViewData) getView();
            ClipboardHelpers.copyToClipboard(view.getAddress(side).toHexString());
          }
        });
    final JMenuItem copyFunctionNameItem = new JMenuItem(new AbstractAction("Copy Function Name") {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final FlowGraphViewData view = (FlowGraphViewData) getView();
        ClipboardHelpers.copyToClipboard(view.getFunctionName(side));
      }
    });

    popupMenu.add(copyFunctionAddressItem);
    popupMenu.add(copyFunctionNameItem);

    return popupMenu;

  }

  @Override
  public SingleFlowGraphRootTreeNode getRootNode() {
    return (SingleFlowGraphRootTreeNode) getAbstractRootNode();
  }

  @Override
  public String getTooltipText() {
    return null;
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
    final FlowGraphViewData view = getRootNode().getView();
    final ESide side = getRootNode().getSide();
    final RawFlowGraph flowgraph = view.getRawGraph(side);

    return String.format("%s (%d / %d)", flowgraph.getName(), getChildCount(),
        basicBlockTreeNodes.size());
  }

  private class InternalMatchesChangeListener implements IMatchesChangeListener {
    private void updateTree(final IAddress priFunctionAddr, final IAddress secFunctionAddr,
        final IAddress priBasicblockAddr, final IAddress secBasicblockAddr) {
      final IAddress priFctAddr = getRootNode().getView().getAddress(ESide.PRIMARY);
      final IAddress secFctAddr = getRootNode().getView().getAddress(ESide.SECONDARY);

      if (priFctAddr.equals(priFunctionAddr) && secFctAddr.equals(secFunctionAddr)) {
        IAddress address = priBasicblockAddr;
        if (getRootNode().getGraph().getSide() == ESide.SECONDARY) {
          address = secBasicblockAddr;
        }

        replaceBasicblockTreeNode(address);
      }
    }

    @Override
    public void addedBasicBlockMatch(
        final IAddress priFunctionAddr,
        final IAddress secFunctionAddr,
        final IAddress priBasicblockAddr,
        final IAddress secBasiblockAddr) {
      updateTree(priFunctionAddr, secFunctionAddr, priBasicblockAddr, secBasiblockAddr);
    }

    @Override
    public void removedBasicBlockMatch(
        final IAddress priFunctionAddr,
        final IAddress secFunctionAddr,
        final IAddress priBasicblockAddr,
        final IAddress secBasiblockAddr) {
      updateTree(priFunctionAddr, secFunctionAddr, priBasicblockAddr, secBasiblockAddr);
    }
  }
}
