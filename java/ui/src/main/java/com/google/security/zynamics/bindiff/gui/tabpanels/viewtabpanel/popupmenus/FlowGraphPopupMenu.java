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
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.AddNodeMatchAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.CopyBasicBlockAddressAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.DeleteNodeMatchAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ZoomToNodeAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class FlowGraphPopupMenu extends JPopupMenu {
  public FlowGraphPopupMenu(
      final ViewTabPanelFunctions controller,
      final BinDiffGraph<?, ?> graph,
      final ZyGraphNode<?> node) {
    checkNotNull(graph);
    checkNotNull(node);

    final JMenuItem addBasicblockMatchItem =
        new JMenuItem(new AddNodeMatchAction(controller, graph, node));
    final JMenuItem deleteBasicblockMatchItem =
        new JMenuItem(new DeleteNodeMatchAction(controller, graph, node));
    final JMenuItem zoomToNodeItem = new JMenuItem(new ZoomToNodeAction(graph, node));

    addBasicblockMatchItem.setEnabled(
        ViewTabPanelFunctions.isNodeSelectionMatchAddable(graph, node));
    deleteBasicblockMatchItem.setEnabled(
        ViewTabPanelFunctions.isNodeSelectionMatchDeletable(graph, node));

    add(addBasicblockMatchItem);
    add(deleteBasicblockMatchItem);
    add(new JSeparator());
    add(zoomToNodeItem);
    add(new JSeparator());

    if (node instanceof CombinedDiffNode) {
      final CombinedDiffNode combinedNode = (CombinedDiffNode) node;
      if (combinedNode.getPrimaryDiffNode() != null) {
        final JMenuItem copyPrimaryBasicblockAddressItem =
            new JMenuItem(new CopyBasicBlockAddressAction(combinedNode, ESide.PRIMARY));
        add(copyPrimaryBasicblockAddressItem);
      }
      if (combinedNode.getSecondaryDiffNode() != null) {
        final JMenuItem copySecondaryBasicblockAddressItem =
            new JMenuItem(new CopyBasicBlockAddressAction(combinedNode, ESide.SECONDARY));
        add(copySecondaryBasicblockAddressItem);
      }
    } else if (node instanceof SingleDiffNode) {
      final JMenuItem copyBasicblockAddressItem =
          new JMenuItem(new CopyBasicBlockAddressAction((SingleDiffNode) node));
      add(copyBasicblockAddressItem);
    }
  }
}
