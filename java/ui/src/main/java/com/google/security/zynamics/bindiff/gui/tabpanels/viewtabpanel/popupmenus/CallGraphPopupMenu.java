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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.popupmenus;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.OpenFlowGraphsViewAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.CopyFunctionAddressAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.CopyFunctionNameAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ZoomToNodeAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class CallGraphPopupMenu extends JPopupMenu {
  public CallGraphPopupMenu(
      final ViewTabPanelFunctions controller,
      final BinDiffGraph<?, ?> graph,
      final ZyGraphNode<?> node) {
    checkNotNull(controller);
    checkNotNull(graph);
    checkNotNull(node);

    final JMenuItem openFlowgraphViewsMenuItem =
        new JMenuItem(new OpenFlowGraphsViewAction(controller, node));
    final JMenuItem zoomToFunctionNodeItem = new JMenuItem(new ZoomToNodeAction(graph, node));

    add(openFlowgraphViewsMenuItem);
    add(new JSeparator());
    add(zoomToFunctionNodeItem);
    add(new JSeparator());

    if (node instanceof CombinedDiffNode) {
      final CombinedDiffNode combinedNode = (CombinedDiffNode) node;
      if (combinedNode.getPrimaryDiffNode() != null) {
        final JMenuItem copyPrimaryFunctionAddressItem =
            new JMenuItem(new CopyFunctionAddressAction(combinedNode, ESide.PRIMARY));
        final JMenuItem copyPrimaryFunctionNameItem =
            new JMenuItem(new CopyFunctionNameAction(combinedNode, ESide.PRIMARY));
        add(copyPrimaryFunctionAddressItem);
        add(copyPrimaryFunctionNameItem);
      }
      if (combinedNode.getSecondaryDiffNode() != null) {
        final JMenuItem copySecondaryFunctionAddressItem =
            new JMenuItem(new CopyFunctionAddressAction(combinedNode, ESide.SECONDARY));
        final JMenuItem copySecondaryFunctionNameItem =
            new JMenuItem(new CopyFunctionNameAction(combinedNode, ESide.SECONDARY));
        add(copySecondaryFunctionAddressItem);
        add(copySecondaryFunctionNameItem);
      }
    } else if (node instanceof SingleDiffNode) {
      final JMenuItem copyFunctionAddressItem =
          new JMenuItem(new CopyFunctionAddressAction((SingleDiffNode) node));
      final JMenuItem copyFunctioNameItem =
          new JMenuItem(new CopyFunctionNameAction((SingleDiffNode) node));

      add(copyFunctionAddressItem);
      add(copyFunctioNameItem);
    }
  }
}
