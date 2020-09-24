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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.single.flowgraph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.EMatchType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.ISearchableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.helper.MouseTreeNodeSelectionHandlerSingleBasicBlock;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.popupmenus.FlowGraphPopupMenu;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPopupMenu;

public class SingleFlowGraphBasicBlockTreeNode extends AbstractTreeNode
    implements ISortableTreeNode, ISearchableTreeNode {
  private static final Icon MATCHED_BASICBLOCK =
      ImageUtils.getImageIcon("data/treeicons/matched-basicblock.png");
  private static final Icon CHANGED_BASICBLOCK =
      ImageUtils.getImageIcon("data/treeicons/changed-basicblock.png");
  private static final Icon PRIMARY_UNMATCHED_BASICBLOCK =
      ImageUtils.getImageIcon("data/treeicons/primary-unmatched-basicblock.png");
  private static final Icon SECONDARY_UNMATCHED_BASICBLOCK =
      ImageUtils.getImageIcon("data/treeicons/secondary-unmatched-basicblock.png");

  private static final Icon MATCHED_BASICBLOCK_SELECTED =
      ImageUtils.getImageIcon("data/treeicons/matched-selected-basicblock.png");
  private static final Icon CHANGED_BASICBLOCK_SELECTED =
      ImageUtils.getImageIcon("data/treeicons/changed-selected-basicblock.png");
  private static final Icon PRIMARY_UNMATCHED_BASICBLOCK_SELECTED =
      ImageUtils.getImageIcon("data/treeicons/primary-unmatched-selected-basicblock.png");
  private static final Icon SECONDARY_UNMATCHED_BASICBLOCK_SELECTED =
      ImageUtils.getImageIcon("data/treeicons/secondary-unmatched-selected-basicblock.png");

  private static final Icon MATCHED_BASICBLOCK_INVISIBLE =
      ImageUtils.getImageIcon("data/treeicons/matched-invisible-basicblock.png");
  private static final Icon CHANGED_BASICBLOCK_INVISIBLE =
      ImageUtils.getImageIcon("data/treeicons/changed-invisible-basicblock.png");
  private static final Icon PRIMARY_UNMATCHED_BASICBLOCK_INVISIBLE =
      ImageUtils.getImageIcon("data/treeicons/primary-unmatched-invisible-basicblock.png");
  private static final Icon SECONDARY_UNMATCHED_BASICBLOCK_INVISIBLE =
      ImageUtils.getImageIcon("data/treeicons/secondary-unmatched-invisible-basicblock.png");

  private final SingleDiffNode singleDiffNode;

  private final RawBasicBlock rawBasicblock;

  public SingleFlowGraphBasicBlockTreeNode(
      final SingleFlowGraphRootTreeNode rootNode, final SingleDiffNode graphNode) {
    super(rootNode);

    singleDiffNode = checkNotNull(graphNode);

    rawBasicblock = (RawBasicBlock) singleDiffNode.getRawNode();

    createChildren();
  }

  private boolean isChangedBasicblock() {
    final Diff diff = getDiff();

    final EMatchState matchState = getMatchState();

    if (matchState == EMatchState.PRIMARY_UNMATCHED
        || matchState == EMatchState.SECONDRAY_UNMATCHED) {
      return false;
    }

    final MatchData matches = diff.getMatches();

    if (rawBasicblock.getSide() == ESide.PRIMARY) {
      final IAddress priFunctionAddr = rawBasicblock.getFunctionAddr();
      final IAddress secFunctionAddr = matches.getSecondaryFunctionAddr(priFunctionAddr);

      if (secFunctionAddr != null) {
        final FunctionMatchData functionMatch =
            matches.getFunctionMatch(priFunctionAddr, ESide.PRIMARY);

        if (functionMatch != null) {
          final IAddress priBasicblockAddr = rawBasicblock.getAddress();
          final IAddress secBasicblockAddr =
              functionMatch.getSecondaryBasicblockAddr(priBasicblockAddr);

          if (functionMatch.isBasicblockMatch(priBasicblockAddr, secBasicblockAddr)) {
            final RawBasicBlock secRawBasicblock =
                ((FlowGraphViewData) getView())
                    .getRawGraph(ESide.SECONDARY)
                    .getBasicBlock(secBasicblockAddr);

            final BasicBlockMatchData basicblockMatch =
                functionMatch.getBasicBlockMatch(priBasicblockAddr, ESide.PRIMARY);

            if (basicblockMatch != null) {
              final int matchedInstructionCount = basicblockMatch.getSizeOfMatchedInstructions();

              return rawBasicblock.getInstructions().size() > matchedInstructionCount
                  || secRawBasicblock.getInstructions().size() > matchedInstructionCount;
            }
          }
        }
      }
    } else {
      final IAddress secFunctionAddr = rawBasicblock.getFunctionAddr();
      final IAddress priFunctionAddr = matches.getPrimaryFunctionAddr(secFunctionAddr);

      if (priFunctionAddr != null) {
        final FunctionMatchData functionMatch =
            matches.getFunctionMatch(priFunctionAddr, ESide.PRIMARY);

        if (functionMatch != null) {
          final IAddress secBasicblockAddr = rawBasicblock.getAddress();
          final IAddress priBasicblockAddr =
              functionMatch.getPrimaryBasicblockAddr(secBasicblockAddr);

          if (functionMatch.isBasicblockMatch(priBasicblockAddr, secBasicblockAddr)) {
            final RawBasicBlock priRawBasicblock =
                ((FlowGraphViewData) getView())
                    .getRawGraph(ESide.PRIMARY)
                    .getBasicBlock(priBasicblockAddr);

            final BasicBlockMatchData basicblockMatch =
                functionMatch.getBasicBlockMatch(priBasicblockAddr, ESide.PRIMARY);

            if (basicblockMatch != null) {
              final int matchedInstructionCount = basicblockMatch.getSizeOfMatchedInstructions();

              return rawBasicblock.getInstructions().size() > matchedInstructionCount
                  || priRawBasicblock.getInstructions().size() > matchedInstructionCount;
            }
          }
        }
      }
    }

    return false;
  }

  @Override
  public void createChildren() {
  }

  @Override
  public IAddress getAddress() {
    return rawBasicblock.getAddress();
  }

  @Override
  public IAddress getAddress(final ESide side) {
    return rawBasicblock.getSide() == side ? rawBasicblock.getAddress() : null;
  }

  public RawBasicBlock getBasicblock() {
    return rawBasicblock;
  }

  @Override
  public String getFunctionName() {
    return rawBasicblock.getFunctionName();
  }

  @Override
  public EFunctionType getFunctionType() {
    return null;
  }

  @Override
  public ZyGraphNode<?> getGraphNode() {
    return singleDiffNode;
  }

  @Override
  public ZyGraphNode<?> getGraphNode(final ESide side) {
    if (singleDiffNode.getSide() != side) {
      return null;
    }

    return singleDiffNode;
  }

  @Override
  public Icon getIcon() {
    switch (getMatchState()) {
      case MATCHED: {
      if (!isChangedBasicblock()) {
        if (isSelected()) {
          return MATCHED_BASICBLOCK_SELECTED;
        }

        if (!isVisible()) {
          return MATCHED_BASICBLOCK_INVISIBLE;
        }

        return MATCHED_BASICBLOCK;
      } else {
        if (isSelected()) {
          return CHANGED_BASICBLOCK_SELECTED;
        }

        if (!isVisible()) {
          return CHANGED_BASICBLOCK_INVISIBLE;
        }

        return CHANGED_BASICBLOCK;
      }
    }

      case PRIMARY_UNMATCHED: {
      if (isSelected()) {
        return PRIMARY_UNMATCHED_BASICBLOCK_SELECTED;
      }

      if (!isVisible()) {
        return PRIMARY_UNMATCHED_BASICBLOCK_INVISIBLE;
      }

      return PRIMARY_UNMATCHED_BASICBLOCK;
    }

      case SECONDRAY_UNMATCHED: {
      if (isSelected()) {
        return SECONDARY_UNMATCHED_BASICBLOCK_SELECTED;
      }

      if (!isVisible()) {
        return SECONDARY_UNMATCHED_BASICBLOCK_INVISIBLE;
      }

      return SECONDARY_UNMATCHED_BASICBLOCK;
    }
    }

    throw new IllegalStateException("Unknown match type.");
  }

  @Override
  public EMatchState getMatchState() {
    return rawBasicblock.getMatchState();
  }

  @Override
  public EMatchType getMatchType() {
    if (getMatchState() == EMatchState.MATCHED) {
      if (isChangedBasicblock()) {
        return EMatchType.INSTRUCTIONS_CHANGED;
      }

      return EMatchType.IDENTICAL;
    }

    return EMatchType.UNUMATCHED;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return new FlowGraphPopupMenu(getRootNode().getController(), getGraph(), singleDiffNode);
  }

  @Override
  public SingleFlowGraphRootTreeNode getRootNode() {
    return (SingleFlowGraphRootTreeNode) getAbstractRootNode();
  }

  public SingleDiffNode getSingleDiffNode() {
    return singleDiffNode;
  }

  @Override
  public String getTooltipText() {
    return null;
  }

  @Override
  public void handleMouseEvent(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
      MouseTreeNodeSelectionHandlerSingleBasicBlock.handleMouseSelectionEvent(event, this);
    }

    super.handleMouseEvent(event);
  }

  @Override
  public boolean isSelected() {
    return rawBasicblock.isSelected();
  }

  @Override
  public boolean isVisible() {
    return rawBasicblock.isVisible();
  }

  @Override
  public String toString() {
    return rawBasicblock.getAddress().toHexString();
  }
}
