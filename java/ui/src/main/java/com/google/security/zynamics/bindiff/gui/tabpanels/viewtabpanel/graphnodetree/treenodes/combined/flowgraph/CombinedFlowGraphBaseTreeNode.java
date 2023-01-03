// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.flowgraph;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.ISearchableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractBaseTreeNode;
import com.google.security.zynamics.bindiff.project.helpers.GraphGetter;
import com.google.security.zynamics.bindiff.project.matches.IMatchesChangeListener;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
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

public class CombinedFlowGraphBaseTreeNode extends AbstractBaseTreeNode {
  private static final Icon MATCHED_IDENTICAL_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/matched-functions.png");
  private static final Icon MATCHED_INSTRUCTIONCHANGED_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/instructions-changed-function.png");
  private static final Icon MATCHED_STRUCTURALCHANGED_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/structural-changed-function.png");

  private static final Icon PRIMARY_UNMATCHED_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-functions.png");
  private static final Icon SECONDRAY_UNMATCHED_FUNCNTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-functions.png");

  private final IMatchesChangeListener matchesChangeListener = new InternalMatchesChangeListener();

  private final List<CombinedFlowGraphBasicBlockTreeNode> basicblockTreeNodes = new ArrayList<>();

  private CombinedDiffNode lastSelectedTreeNode;

  public CombinedFlowGraphBaseTreeNode(final CombinedFlowGraphRootTreeNode rootNode) {
    super(rootNode);

    createChildren();

    getDiff().getMatches().addListener(matchesChangeListener);
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

    final List<CombinedFlowGraphBasicBlockTreeNode> treeNodes = new ArrayList<>();

    if (searcher.getUseTemporaryResults() && !"".equals(searcher.getSearchString())) {
      for (int i = 0; i < getChildCount(); ++i) {
        treeNodes.add((CombinedFlowGraphBasicBlockTreeNode) getChildAt(i));
      }
    } else {
      treeNodes.addAll(basicblockTreeNodes);
    }

    removeAllChildren();

    List<? extends ISearchableTreeNode> searchedTreeNodes = new ArrayList<>();

    if (updateSearch) {
      if (!"".equals(searcher.getSearchString())) {
        searchedTreeNodes = searcher.search(treeNodes);

        treeNodes.clear();

        for (final ISearchableTreeNode searchedTreeNode : searchedTreeNodes) {
          treeNodes.add((CombinedFlowGraphBasicBlockTreeNode) searchedTreeNode);
        }
      } else {
        searcher.search(null);
      }
    }

    final List<CombinedFlowGraphBasicBlockTreeNode> filteredTreeNodes = new ArrayList<>();

    for (final CombinedFlowGraphBasicBlockTreeNode treeNode : treeNodes) {
      if (filter.filterRawCombinedBasicBlock(treeNode.getCombinedBasicblock())) {
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
        getRootNode().getSorter().getCombinedBasicBlockTreeNodeComparatorList()) {
      Collections.sort(filteredTreeNodes, comparator);
    }

    for (final CombinedFlowGraphBasicBlockTreeNode treeNode : filteredTreeNodes) {
      add(treeNode);
    }

    getTree().getModel().nodeStructureChanged(this);

    getTree().updateUI();
  }

  @Override
  public void createChildren() {
    final CombinedFlowGraphRootTreeNode rootNode = getRootNode();

    basicblockTreeNodes.clear();

    for (final CombinedDiffNode diffNode : rootNode.getGraph().getNodes()) {
      basicblockTreeNodes.add(new CombinedFlowGraphBasicBlockTreeNode(rootNode, diffNode));
    }

    updateTreeNodes(false);
  }

  @Override
  public Icon getIcon() {
    final CombinedFlowGraphRootTreeNode rootNode = getRootNode();

    final FlowGraphViewData view = rootNode.getView();

    final RawFlowGraph priFlowgraph = view.getRawGraph(ESide.PRIMARY);
    final RawFlowGraph secFlowgraph = view.getRawGraph(ESide.SECONDARY);

    final RawFunction priFunction = GraphGetter.getFunction(getDiff(), priFlowgraph);
    final RawFunction secFunction = GraphGetter.getFunction(getDiff(), secFlowgraph);

    if (secFunction == null) {
      return PRIMARY_UNMATCHED_FUNCTION_ICON;
    } else if (priFunction == null) {
      return SECONDRAY_UNMATCHED_FUNCNTION_ICON;
    }

    if (priFunction.isChangedInstructionsOnlyMatch()) {
      return MATCHED_INSTRUCTIONCHANGED_FUNCTION_ICON;

    } else if (priFunction.isChangedStructuralMatch()) {
      return MATCHED_STRUCTURALCHANGED_FUNCTION_ICON;
    }

    return MATCHED_IDENTICAL_FUNCTION_ICON;
  }

  public CombinedDiffNode getLastSelectedGraphNode() {
    return lastSelectedTreeNode;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    final JPopupMenu popupMenu = new JPopupMenu();

    final JMenuItem copyPriFunctionAddressItem =
        new JMenuItem(
            new AbstractAction("Copy Primary Function Address") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final FlowGraphViewData view = (FlowGraphViewData) getView();
                ClipboardHelpers.copyToClipboard(view.getAddress(ESide.PRIMARY).toHexString());
              }
            });
    final JMenuItem copyPriFunctionNameItem =
        new JMenuItem(
            new AbstractAction("Copy Primary Function Name") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final FlowGraphViewData view = (FlowGraphViewData) getView();
                ClipboardHelpers.copyToClipboard(view.getFunctionName(ESide.PRIMARY));
              }
            });

    final JMenuItem copySecFunctionAddressItem =
        new JMenuItem(
            new AbstractAction("Copy Secondary Function Address") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final FlowGraphViewData view = (FlowGraphViewData) getView();
                ClipboardHelpers.copyToClipboard(view.getAddress(ESide.SECONDARY).toHexString());
              }
            });
    final JMenuItem copySecFunctionNameItem =
        new JMenuItem(
            new AbstractAction("Copy Secondary Function Name") {
              @Override
              public void actionPerformed(final ActionEvent e) {
                final FlowGraphViewData view = (FlowGraphViewData) getView();
                ClipboardHelpers.copyToClipboard(view.getFunctionName(ESide.SECONDARY));
              }
            });

    popupMenu.add(copyPriFunctionAddressItem);
    popupMenu.add(copyPriFunctionNameItem);
    popupMenu.add(copySecFunctionAddressItem);
    popupMenu.add(copySecFunctionNameItem);

    return popupMenu;
  }

  @Override
  public CombinedFlowGraphRootTreeNode getRootNode() {
    return (CombinedFlowGraphRootTreeNode) getAbstractRootNode();
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

  public void setLastSelectedGraphNode(final CombinedDiffNode node) {
    lastSelectedTreeNode = node;
  }

  @Override
  public String toString() {
    final FlowGraphViewData view = getRootNode().getView();
    final RawFlowGraph priFlowgraph = view.getRawGraph(ESide.PRIMARY);
    final RawFlowGraph secFlowgraph = view.getRawGraph(ESide.SECONDARY);

    String priFunctionName = "missing";
    String secFunctionName = "missing";

    if (priFlowgraph != null) {
      priFunctionName = priFlowgraph.getName();
    }

    if (secFlowgraph != null) {
      secFunctionName = secFlowgraph.getName();
    }

    return String.format(
        "%s \u2194 %s (%d / %d)",
        priFunctionName, secFunctionName, getChildCount(), basicblockTreeNodes.size());
  }

  private class InternalMatchesChangeListener implements IMatchesChangeListener {
    private boolean isAffected(final IAddress priFunctionAddr, final IAddress secFunctionAddr) {
      final IAddress priFctAddr = getRootNode().getView().getAddress(ESide.PRIMARY);
      final IAddress secFctAddr = getRootNode().getView().getAddress(ESide.SECONDARY);

      return priFunctionAddr.equals(priFctAddr) && secFunctionAddr.equals(secFctAddr);
    }

    @Override
    public void addedBasicBlockMatch(
        final IAddress priFunctionAddr,
        final IAddress secFunctionAddr,
        final IAddress priBasicblockAddr,
        final IAddress secBasicblockAddr) {
      if (isAffected(priFunctionAddr, secFunctionAddr)) {
        CombinedDiffNode newCombinedDiffNode = null;

        for (final CombinedDiffNode diffNode : getRootNode().getGraph().getNodes()) {
          final IAddress priDiffAddr =
              diffNode.getPrimaryRawNode() == null
                  ? null
                  : diffNode.getPrimaryRawNode().getAddress();
          final IAddress secDiffAddr =
              diffNode.getSecondaryRawNode() == null
                  ? null
                  : diffNode.getSecondaryRawNode().getAddress();

          if (priBasicblockAddr.equals(priDiffAddr) && secBasicblockAddr.equals(secDiffAddr)) {
            newCombinedDiffNode = diffNode;

            break;
          }
        }

        if (newCombinedDiffNode != null) {
          boolean halfDone = false;
          for (int index = 1; index < getChildCount(); ++index) {
            final CombinedFlowGraphBasicBlockTreeNode treeNode =
                (CombinedFlowGraphBasicBlockTreeNode) getChildAt(index);

            final RawCombinedBasicBlock oldUnmatchedRawCombinedBasicblock =
                treeNode.getCombinedBasicblock();

            if (priBasicblockAddr.equals(
                oldUnmatchedRawCombinedBasicblock.getAddress(ESide.PRIMARY))) {
              basicblockTreeNodes.remove(treeNode);
              remove(treeNode);

              if (halfDone) {
                final CombinedFlowGraphBasicBlockTreeNode newCombinedTreeNode;
                newCombinedTreeNode =
                    new CombinedFlowGraphBasicBlockTreeNode(getRootNode(), newCombinedDiffNode);

                basicblockTreeNodes.add(newCombinedTreeNode);
                add(newCombinedTreeNode);

                break;
              }

              halfDone = true;
            } else if (secBasicblockAddr.equals(
                oldUnmatchedRawCombinedBasicblock.getAddress(ESide.SECONDARY))) {
              basicblockTreeNodes.remove(treeNode);
              remove(treeNode);

              if (halfDone) {
                final CombinedFlowGraphBasicBlockTreeNode newCombinedTreeNode;
                newCombinedTreeNode =
                    new CombinedFlowGraphBasicBlockTreeNode(getRootNode(), newCombinedDiffNode);

                basicblockTreeNodes.add(newCombinedTreeNode);
                add(newCombinedTreeNode);

                break;
              }

              halfDone = true;
            }
          }

          updateTreeNodes(true);
        }
      }
    }

    @Override
    public void removedBasicBlockMatch(
        final IAddress priFunctionAddr,
        final IAddress secFunctionAddr,
        final IAddress priBasicblockAddr,
        final IAddress secBasicblockAddr) {
      if (isAffected(priFunctionAddr, secFunctionAddr)) {
        CombinedDiffNode newPriCombinedDiffNode = null;
        CombinedDiffNode newSecCombinedDiffNode = null;

        for (final CombinedDiffNode diffNode : getRootNode().getGraph().getNodes()) {
          final IAddress priDiffAddr =
              diffNode.getPrimaryRawNode() == null
                  ? null
                  : diffNode.getPrimaryRawNode().getAddress();
          final IAddress secDiffAddr =
              diffNode.getSecondaryRawNode() == null
                  ? null
                  : diffNode.getSecondaryRawNode().getAddress();

          if (priBasicblockAddr.equals(priDiffAddr) && secDiffAddr == null) {
            newPriCombinedDiffNode = diffNode;

            if (newPriCombinedDiffNode != null && newSecCombinedDiffNode != null) {
              break;
            }
          }

          if (secBasicblockAddr.equals(secDiffAddr) && priDiffAddr == null) {
            newSecCombinedDiffNode = diffNode;

            if (newPriCombinedDiffNode != null && newSecCombinedDiffNode != null) {
              break;
            }
          }
        }

        if (newPriCombinedDiffNode != null && newSecCombinedDiffNode != null) {
          for (int index = 0; index < getChildCount(); ++index) {
            final CombinedFlowGraphBasicBlockTreeNode treeNode =
                (CombinedFlowGraphBasicBlockTreeNode) getChildAt(index);

            final RawCombinedBasicBlock oldRawCombinedBasicblock = treeNode.getCombinedBasicblock();
            if (priBasicblockAddr.equals(oldRawCombinedBasicblock.getAddress(ESide.PRIMARY))
                && secBasicblockAddr.equals(oldRawCombinedBasicblock.getAddress(ESide.SECONDARY))) {
              final CombinedFlowGraphBasicBlockTreeNode newPriTreeNode =
                  new CombinedFlowGraphBasicBlockTreeNode(getRootNode(), newPriCombinedDiffNode);
              final CombinedFlowGraphBasicBlockTreeNode newSecTreeNode =
                  new CombinedFlowGraphBasicBlockTreeNode(getRootNode(), newSecCombinedDiffNode);
              insert(newPriTreeNode, index); // TODO: Test wether the insert call does call the
              // substituted tree node's delete function. If not all
              // listeneres attached to this old tree node are
              // leaked.

              final int insertIndex = basicblockTreeNodes.indexOf(treeNode);
              basicblockTreeNodes.set(insertIndex, newPriTreeNode);
              basicblockTreeNodes.add(newSecTreeNode);

              break;
            }
          }

          updateTreeNodes(true);
        }
      }
    }
  }
}
