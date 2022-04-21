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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.flowgraph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.EMatchType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.ISearchableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.helper.MouseTreeNodeSelectionHandlerCombinedBasicBlock;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.popupmenus.FlowGraphPopupMenu;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPopupMenu;

public class CombinedFlowGraphBasicBlockTreeNode extends AbstractTreeNode
    implements ISortableTreeNode, ISearchableTreeNode {
  private static final Icon MATCHED_BASICBLOCK =
      ResourceUtils.getImageIcon("data/treeicons/matched-basicblock.png");
  private static final Icon CHANGED_BASICBLOCK =
      ResourceUtils.getImageIcon("data/treeicons/changed-basicblock.png");
  private static final Icon PRIMARY_UNMATCHED_BASICBLOCK =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-basicblock.png");
  private static final Icon SECONDARY_UNMATCHED_BASICBLOCK =
      ResourceUtils.getImageIcon("data/treeicons/secondary-unmatched-basicblock.png");
  private static final Icon MATCHED_BASICBLOCK_SELECTED =
      ResourceUtils.getImageIcon("data/treeicons/matched-selected-basicblock.png");
  private static final Icon CHANGED_BASICBLOCK_SELECTED =
      ResourceUtils.getImageIcon("data/treeicons/changed-selected-basicblock.png");
  private static final Icon PRIMARY_UNMATCHED_BASICBLOCK_SELECTED =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-selected-basicblock.png");
  private static final Icon SECONDARY_UNMATCHED_BASICBLOCK_SELECTED =
      ResourceUtils.getImageIcon("data/treeicons/secondary-unmatched-selected-basicblock.png");
  private static final Icon MATCHED_BASICBLOCK_INVISIBLE =
      ResourceUtils.getImageIcon("data/treeicons/matched-invisible-basicblock.png");
  private static final Icon CHANGED_BASICBLOCK_INVISIBLE =
      ResourceUtils.getImageIcon("data/treeicons/changed-invisible-basicblock.png");
  private static final Icon PRIMARY_UNMATCHED_BASICBLOCK_INVISIBLE =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-invisible-basicblock.png");
  private static final Icon SECONDARY_UNMATCHED_BASICBLOCK_INVISIBLE =
      ResourceUtils.getImageIcon("data/treeicons/secondary-unmatched-invisible-basicblock.png");

  private final CombinedDiffNode combinedDiffNode;

  private final RawCombinedBasicBlock combinedBasicblock;

  public CombinedFlowGraphBasicBlockTreeNode(
      final CombinedFlowGraphRootTreeNode rootNode, final CombinedDiffNode combinedDiffNode) {
    super(rootNode);

    checkNotNull(combinedDiffNode);

    this.combinedDiffNode = combinedDiffNode;

    combinedBasicblock = (RawCombinedBasicBlock) combinedDiffNode.getRawNode();

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

    final IAddress priFunctionAddr = combinedBasicblock.getPrimaryFunctionAddress();

    final FunctionMatchData functionMatch =
        matches.getFunctionMatch(priFunctionAddr, ESide.PRIMARY);

    if (functionMatch != null) {
      final RawBasicBlock priBasicblock = combinedBasicblock.getRawNode(ESide.PRIMARY);
      final RawBasicBlock secBasicblock = combinedBasicblock.getRawNode(ESide.SECONDARY);

      final IAddress priBasicblockAddr = combinedBasicblock.getAddress(ESide.PRIMARY);
      final IAddress secBasicblockAddr = combinedBasicblock.getAddress(ESide.SECONDARY);

      if (functionMatch.isBasicblockMatch(priBasicblockAddr, secBasicblockAddr)) {
        final BasicBlockMatchData basicblockMatch =
            functionMatch.getBasicBlockMatch(priBasicblockAddr, ESide.PRIMARY);

        if (basicblockMatch != null) {
          final int matchedInstructionCount = basicblockMatch.getSizeOfMatchedInstructions();

          return priBasicblock.getInstructions().size() > matchedInstructionCount
              || secBasicblock.getInstructions().size() > matchedInstructionCount;
        }
      }
    }

    return false;
  }

  @Override
  public IAddress getAddress() {
    return null;
  }

  @Override
  public IAddress getAddress(final ESide side) {
    return combinedBasicblock.getAddress(side);
  }

  public RawCombinedBasicBlock getCombinedBasicblock() {
    return combinedBasicblock;
  }

  public CombinedDiffNode getCombinedDiffNode() {
    return combinedDiffNode;
  }

  @Override
  public String getFunctionName() {
    return null;
  }

  @Override
  public EFunctionType getFunctionType() {
    return null;
  }

  @Override
  public ZyGraphNode<?> getGraphNode() {
    return combinedDiffNode;
  }

  @Override
  public ZyGraphNode<?> getGraphNode(final ESide side) {
    return side == ESide.PRIMARY ? combinedDiffNode.getPrimaryDiffNode() : combinedDiffNode
        .getSecondaryDiffNode();
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
    return combinedBasicblock.getMatchState();
  }

  @Override
  public EMatchType getMatchType() {
    if (getMatchState() != EMatchState.MATCHED) {
      return EMatchType.UNUMATCHED;
    }

    return isChangedBasicblock() ? EMatchType.INSTRUCTIONS_CHANGED : EMatchType.IDENTICAL;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return new FlowGraphPopupMenu(getRootNode().getController(), getGraph(), combinedDiffNode);
  }

  @Override
  public CombinedFlowGraphRootTreeNode getRootNode() {
    return (CombinedFlowGraphRootTreeNode) getAbstractRootNode();
  }

  @Override
  public void handleMouseEvent(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
      MouseTreeNodeSelectionHandlerCombinedBasicBlock.handleMouseSelectionEvent(event, this);
    }

    super.handleMouseEvent(event);
  }

  @Override
  public boolean isSelected() {
    return combinedBasicblock.isSelected();
  }

  @Override
  public boolean isVisible() {
    return combinedBasicblock.isVisible();
  }

  @Override
  public String toString() {
    final RawBasicBlock priBasicblock = combinedBasicblock.getRawNode(ESide.PRIMARY);
    final RawBasicBlock secBasicblock = combinedBasicblock.getRawNode(ESide.SECONDARY);

    return String.format("%s \u2194 %s",
        priBasicblock == null ? "missing" : priBasicblock.getAddress().toHexString(),
        secBasicblock == null ? "missing" : secBasicblock.getAddress().toHexString());
  }
}
