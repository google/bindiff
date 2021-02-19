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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.single.callgraph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.EMatchType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.ISearchableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.helper.MouseTreeNodeSelectionHandlerSingleFunction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.popupmenus.CallGraphPopupMenu;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPopupMenu;

public class SingleCallGraphFunctionTreeNode extends AbstractTreeNode
    implements ISortableTreeNode, ISearchableTreeNode {
  private static final Icon PRIMARY_UNMATCHED_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-functions.png");
  private static final Icon PRIMARY_UNMATCHED_FUNCTION_SELECTED_ICON =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-functions-selected.png");
  private static final Icon PRIMARY_UNMATCHED_FUNCTION_INVISIBLE_ICON =
      ResourceUtils.getImageIcon("data/treeicons/primary-unmatched-functions-invisible.png");

  private static final Icon SECONDARY_UNMATCHED_FUNCTION_ICON =
      ResourceUtils.getImageIcon("data/treeicons/secondary-unmatched-functions.png");
  private static final Icon SECONDARY_UNMATCHED_FUNCTION_SELECTED_ICON =
      ResourceUtils.getImageIcon("data/treeicons/secondary-unmatched-functions-selected.png");
  private static final Icon SECONDARY_UNMATCHED_FUNCTION_INVISIBLE_ICON =
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

  private SingleDiffNode singleDiffNode;

  public SingleCallGraphFunctionTreeNode(
      final SingleCallGraphRootTreeNode rootNode, final SingleDiffNode function) {
    super(rootNode);

    singleDiffNode = checkNotNull(function);

    createChildren();
  }

  public SingleCallGraphFunctionTreeNode(final SingleCallGraphRootTreeNode rootNode,
      final SingleDiffNode function, final boolean isLeaf) {
    super(rootNode);

    singleDiffNode = checkNotNull(function);

    if (!isLeaf) {
      createChildren();
    }
  }

  @Override
  protected void delete() {
    super.delete();

    singleDiffNode = null;
  }

  @Override
  public void createChildren() {
  }

  @Override
  public IAddress getAddress() {
    return getFunction().getAddress();
  }

  @Override
  public IAddress getAddress(final ESide side) {
    if (getFunction().getSide() != side) {
      return null;
    }

    return getAddress();
  }

  public RawFunction getFunction() {
    return (RawFunction) singleDiffNode.getRawNode();
  }

  @Override
  public String getFunctionName() {
    return getFunction().getName();
  }

  @Override
  public EFunctionType getFunctionType() {
    return getFunction().getFunctionType();
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
    final RawFunction function = getFunction();
    switch (function.getMatchState()) {
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
      if (isSelected()) {
        return PRIMARY_UNMATCHED_FUNCTION_SELECTED_ICON;
      } else if (!isVisible()) {
        return PRIMARY_UNMATCHED_FUNCTION_INVISIBLE_ICON;
      }
      return PRIMARY_UNMATCHED_FUNCTION_ICON;
    }
      case SECONDRAY_UNMATCHED: {
      if (isSelected()) {
        return SECONDARY_UNMATCHED_FUNCTION_SELECTED_ICON;
      } else if (!isVisible()) {
        return SECONDARY_UNMATCHED_FUNCTION_INVISIBLE_ICON;
      }
      return SECONDARY_UNMATCHED_FUNCTION_ICON;
    }
    }

    throw new IllegalStateException("Unknown match type.");
  }

  @Override
  public EMatchState getMatchState() {
    return getFunction().getMatchState();
  }

  @Override
  public EMatchType getMatchType() {
    final RawFunction function = (RawFunction) singleDiffNode.getRawNode();

    if (function == null) {
      return EMatchType.UNUMATCHED;
    }

    if (function.isIdenticalMatch()) {
      return EMatchType.IDENTICAL;
    } else if (function.isChangedInstructionsOnlyMatch()) {
      return EMatchType.INSTRUCTIONS_CHANGED;
    }

    return EMatchType.STRUCTURAL_CHANGED;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return new CallGraphPopupMenu(getRootNode().getController(), getGraph(), singleDiffNode);
  }

  @Override
  public SingleCallGraphRootTreeNode getRootNode() {
    return (SingleCallGraphRootTreeNode) getAbstractRootNode();
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
      MouseTreeNodeSelectionHandlerSingleFunction.handleMouseSelectionEvent(event, this);
    }

    super.handleMouseEvent(event);
  }

  @Override
  public boolean isSelected() {
    return getFunction().isSelected();
  }

  @Override
  public boolean isVisible() {
    return getFunction().isVisible();
  }

  @Override
  public String toString() {
    return String.format(
        "%s %s", getFunction().getAddress().toHexString(), getFunction().getName());
  }
}
