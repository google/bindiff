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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class OpenFlowGraphsViewAction extends AbstractAction {
  private final ViewTabPanelFunctions controller;
  private final ZyGraphNode<?> node;

  public OpenFlowGraphsViewAction(
      final ViewTabPanelFunctions controller, final ZyGraphNode<?> node) {
    super("Open Flow Graphs View");

    this.controller = checkNotNull(controller);
    this.node = checkNotNull(node);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    controller.openFlowgraphsViews(node);
  }
}
