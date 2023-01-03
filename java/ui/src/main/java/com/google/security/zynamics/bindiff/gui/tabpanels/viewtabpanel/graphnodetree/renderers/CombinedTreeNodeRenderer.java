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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.renderers;

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.callgraph.CombinedCallGraphFunctionTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.flowgraph.CombinedFlowGraphBaseTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.flowgraph.CombinedFlowGraphRootTreeNode;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class CombinedTreeNodeRenderer extends DefaultTreeCellRenderer {

  public CombinedTreeNodeRenderer() {
    setBackgroundSelectionColor(Color.WHITE);
    setBorderSelectionColor(Color.GRAY);
  }

  private static EFunctionType getFunctionType(final RawCombinedFunction combinedFunction) {
    final RawFunction priFunction = combinedFunction.getRawNode(ESide.PRIMARY);
    final RawFunction secFunction = combinedFunction.getRawNode(ESide.SECONDARY);

    return getFunctionType(priFunction, secFunction);
  }

  private static EFunctionType getFunctionType(
      final RawFunction primaryFunction, final RawFunction secondaryFunction) {
    if (primaryFunction != null
        && secondaryFunction != null
        && primaryFunction.getFunctionType() != secondaryFunction.getFunctionType()) {
      return EFunctionType.MIXED;
    }

    RawFunction function = primaryFunction;
    if (function == null) {
      function = secondaryFunction;
    }

    return function.getFunctionType();
  }

  @Override
  public Component getTreeCellRendererComponent(
      final JTree tree,
      final Object value,
      final boolean sel,
      final boolean expanded,
      final boolean leaf,
      final int row,
      final boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    final AbstractTreeNode node = (AbstractTreeNode) value;

    final Icon icon = node.getIcon();

    if (icon != null) {
      setIcon(icon);
    }

    setFont(
        node.isSelected()
            ? GuiHelper.getDefaultFont().deriveFont(Font.BOLD)
            : GuiHelper.getDefaultFont());
    setForeground(node.isVisible() ? Color.BLACK : Color.GRAY);

    Color background = Color.WHITE;

    EFunctionType functionType = null;

    if (node instanceof CombinedCallGraphFunctionTreeNode) {
      final CombinedCallGraphFunctionTreeNode functionTreeNode =
          (CombinedCallGraphFunctionTreeNode) node;
      functionType = getFunctionType(functionTreeNode.getCombinedFunction());
    } else if (node instanceof CombinedFlowGraphBaseTreeNode) {
      final CombinedFlowGraphBaseTreeNode functionTreeNode = (CombinedFlowGraphBaseTreeNode) node;

      final CombinedFlowGraphRootTreeNode rootNode = functionTreeNode.getRootNode();
      final FlowGraphViewData view = rootNode.getView();

      EFunctionType priFunctionType = null;
      EFunctionType secFunctionType = null;

      if (view.getRawGraph(ESide.PRIMARY) != null) {
        priFunctionType = view.getRawGraph(ESide.PRIMARY).getFunctionType();
      }
      if (view.getRawGraph(ESide.SECONDARY) != null) {
        secFunctionType = view.getRawGraph(ESide.SECONDARY).getFunctionType();
      }

      functionType = priFunctionType == secFunctionType ? priFunctionType : EFunctionType.MIXED;
    }

    if (functionType != null) {
      switch (functionType) {
        case NORMAL:
          background = Colors.FUNCTION_TYPE_NORMAL;
          break;
        case IMPORTED:
          background = Colors.FUNCTION_TYPE_IMPORTED;
          break;
        case LIBRARY:
          background = Colors.FUNCTION_TYPE_LIBRARY;
          break;
        case THUNK:
          background = Colors.FUNCTION_TYPE_THUNK;
          break;
        case UNKNOWN:
          background = Colors.FUNCTION_TYPE_UNKNOWN;
          break;
        case MIXED:
          background = Colors.TABLE_CELL_CHANGED_BACKGROUND;
          break;
      }
    }

    setBackgroundSelectionColor(background);
    setBackgroundNonSelectionColor(background);
    setBorderSelectionColor(background);

    setToolTipText(node.getTooltipText());

    return this;
  }
}
