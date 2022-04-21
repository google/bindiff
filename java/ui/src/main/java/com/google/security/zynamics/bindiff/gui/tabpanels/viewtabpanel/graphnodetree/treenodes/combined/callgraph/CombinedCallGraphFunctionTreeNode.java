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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.callgraph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.EMatchType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.ISearchableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.helper.MouseTreeNodeSelectionHandlerCombinedFunction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.popupmenus.CallGraphPopupMenu;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPopupMenu;

public class CombinedCallGraphFunctionTreeNode extends AbstractTreeNode
    implements ISortableTreeNode, ISearchableTreeNode {
  private static final Icon PRIMARY_UNMATCHED_FUNCTION =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-functions.png");
  private static final Icon PRIMARY_UNMATCHED_FUNCTION_SELECTED =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-functions-selected.png");
  private static final Icon PRIMARY_UNMATCHED_FUNCTION_INVISIBLE =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-functions-invisible.png");

  private static final Icon SECONDARY_UNMATCHED_FUNCTION =
      ResourceUtils.getImageIcon("data/treeicons/secondary-unmatched-functions.png");
  private static final Icon SECONDARY_UNMATCHED_FUNCTION_SELECTED =
      ResourceUtils.getImageIcon("data/treeicons/secondary-unmatched-functions-selected.png");
  private static final Icon SECONDARY_UNMATCHED_FUNCTION_INVISIBLE =
      ResourceUtils.getImageIcon("data/treeicons/secondary-unmatched-functions-invisible.png");

  private static final Icon MATCHED_IDENTICAL_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/matched-functions.png");
  private static final Icon MATCHED_IDENTICAL_FUNCTION_SELECTED_ICON =
      ResourceUtils.getImageIcon("data/treeicons/matched-functions-selected.png");
  private static final Icon MATCHED_IDENTICAL_FUNCTION_INVISIBLE_ICON =
      ResourceUtils.getImageIcon("data/treeicons/matched-functions-invisible.png");

  private static final Icon MATCHED_STRUCTURALCHANGED_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/structural-changed-function.png");
  private static final Icon MATCHED_STRUCTURALCHANGED_FUNCTION_SELECTED_ICON =
      ResourceUtils.getImageIcon("data/treeicons/structural-changed-function-selected.png");
  private static final Icon MATCHED_STRUCTURALCHANGED_FUNCTION_INVISIBLE_ICON =
      ResourceUtils.getImageIcon("data/treeicons/structural-changed-function-invisible.png");

  private static final Icon MATCHED_INSTRUCTIONCHANGED_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/instructions-changed-function.png");
  private static final Icon MATCHED_INSTRUCTIONCHANGED_FUNCTION_SELECTED_ICON =
      ResourceUtils.getImageIcon("data/treeicons/instructions-changed-function-selected.png");
  private static final Icon MATCHED_INSTRUCTIONCHANGED_FUNCTION_INVISIBLE_ICON =
      ResourceUtils.getImageIcon("data/treeicons/instructions-changed-function-invisible.png");

  private CombinedDiffNode combinedDiffNode;

  public CombinedCallGraphFunctionTreeNode(
      final CombinedCallGraphRootTreeNode rootNode, final CombinedDiffNode combinedFunction) {
    super(rootNode);
    checkNotNull(combinedFunction);

    combinedDiffNode = combinedFunction;

    createChildren();
  }

  @Override
  protected void delete() {
    super.delete();

    combinedDiffNode = null;
  }

  @Override
  public void createChildren() {
  }

  @Override
  public IAddress getAddress() {
    return null;
  }

  @Override
  public IAddress getAddress(final ESide side) {
    return getCombinedFunction().getAddress(side);
  }

  public CombinedDiffNode getCombinedDiffNode() {
    return combinedDiffNode;
  }

  public RawCombinedFunction getCombinedFunction() {
    return (RawCombinedFunction) combinedDiffNode.getRawNode();
  }

  @Override
  public String getFunctionName() {
    return null;
  }

  @Override
  public EFunctionType getFunctionType() {
    return getCombinedFunction().getFunctionType();
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
    RawFunction function = (RawFunction) combinedDiffNode.getPrimaryRawNode();
    if (function == null) {
      function = (RawFunction) combinedDiffNode.getPrimaryRawNode();
    }

    switch (combinedDiffNode.getRawNode().getMatchState()) {
      case MATCHED: {
      if (function.isIdenticalMatch()) {
        if (isSelected()) {
          return MATCHED_IDENTICAL_FUNCTION_SELECTED_ICON;
        } else if (!isVisible()) {
          return MATCHED_IDENTICAL_FUNCTION_INVISIBLE_ICON;
        }

        return MATCHED_IDENTICAL_FUNCTION_ICON;
      } else if (function.isChangedInstructionsOnlyMatch()) {
        if (isSelected()) {
          return MATCHED_INSTRUCTIONCHANGED_FUNCTION_SELECTED_ICON;
        } else if (!isVisible()) {
          return MATCHED_INSTRUCTIONCHANGED_FUNCTION_INVISIBLE_ICON;
        }

        return MATCHED_INSTRUCTIONCHANGED_FUNCTION_ICON;

      } else if (function.isChangedStructuralMatch()) {
        if (isSelected()) {
          return MATCHED_STRUCTURALCHANGED_FUNCTION_SELECTED_ICON;
        } else if (!isVisible()) {
          return MATCHED_STRUCTURALCHANGED_FUNCTION_INVISIBLE_ICON;
        }

        return MATCHED_STRUCTURALCHANGED_FUNCTION_ICON;

      }
      break;
    }
      case PRIMARY_UNMATCHED: {
      if (combinedDiffNode != null) {
        if (combinedDiffNode.isSelected()) {
          return PRIMARY_UNMATCHED_FUNCTION_SELECTED;
        } else if (!combinedDiffNode.isVisible()) {
          return PRIMARY_UNMATCHED_FUNCTION_INVISIBLE;
        }
      }

      return PRIMARY_UNMATCHED_FUNCTION;
    }
      case SECONDRAY_UNMATCHED: {
      if (combinedDiffNode != null) {
        if (combinedDiffNode.isSelected()) {
          return SECONDARY_UNMATCHED_FUNCTION_SELECTED;
        } else if (!combinedDiffNode.isVisible()) {
          return SECONDARY_UNMATCHED_FUNCTION_INVISIBLE;
        }
      }

      return SECONDARY_UNMATCHED_FUNCTION;
    }
    }

    throw new IllegalStateException("Unknown match type.");
  }

  @Override
  public EMatchState getMatchState() {
    return getCombinedFunction().getMatchState();
  }

  @Override
  public EMatchType getMatchType() {
    final RawFunction function = (RawFunction) combinedDiffNode.getPrimaryRawNode();
    if (function == null) {
      return EMatchType.UNUMATCHED;
    }

    if (function.isIdenticalMatch()) {
      return EMatchType.IDENTICAL;
    } else if (function.isIdenticalMatch()) {
      return EMatchType.INSTRUCTIONS_CHANGED;
    }

    return EMatchType.STRUCTURAL_CHANGED;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return new CallGraphPopupMenu(getRootNode().getController(), getGraph(), combinedDiffNode);
  }

  @Override
  public CombinedCallGraphRootTreeNode getRootNode() {
    return (CombinedCallGraphRootTreeNode) getAbstractRootNode();
  }

  @Override
  public void handleMouseEvent(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
      MouseTreeNodeSelectionHandlerCombinedFunction.handleMouseSelectionEvent(event, this);
      return;
    }

    super.handleMouseEvent(event);
  }

  @Override
  public boolean isSelected() {
    return combinedDiffNode.isSelected();
  }

  @Override
  public boolean isVisible() {
    return combinedDiffNode.isVisible();
  }

  @Override
  public String toString() {
    String nodetext = "";

    final EMatchState matchState = combinedDiffNode.getRawNode().getMatchState();

    if (matchState == EMatchState.MATCHED) {
      nodetext = String.format(
          "%s \u2194 %s", combinedDiffNode.getPrimaryRawNode().getAddress().toHexString(),
          combinedDiffNode.getSecondaryRawNode().getAddress().toHexString());
    } else if (matchState == EMatchState.PRIMARY_UNMATCHED) {
      nodetext = String.format(
          "%s \u2194 %s", combinedDiffNode.getPrimaryRawNode().getAddress().toHexString(),
          "missing");
    } else if (matchState == EMatchState.SECONDRAY_UNMATCHED) {
      nodetext = String.format("%s \u2194 %s", "missing",
          combinedDiffNode.getSecondaryRawNode().getAddress().toHexString());
    }

    return nodetext;
  }
}
