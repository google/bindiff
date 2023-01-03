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

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundClickedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;

/**
 * Default action that is executed on
 *
 * @param <T> Type of the nodes in the graph.
 */
public class CDefaultBackgroundClickedLeftAction<T extends ZyGraphNode<?>>
    implements IStateAction<CBackgroundClickedLeftState<T>> {
  /**
   * Unselects all nodes in the graph.
   *
   * @param graph The graph whose nodes are unselected.
   */
  protected void unselectAll(final AbstractZyGraph<T, ?> graph) {
    graph.selectNodes(graph.getNodes(), false);
  }

  @Override
  public void execute(final CBackgroundClickedLeftState<T> state, final MouseEvent event) {
    unselectAll(state.getGraph());
  }
}
