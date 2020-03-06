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

package com.google.security.zynamics.bindiff.graph.editmode.actions;

import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.editmode.helpers.EditCombinedNodeHelper;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeClickedMiddleState;
import java.awt.event.MouseEvent;

public class CombinedNodeClickedMiddleAction implements IStateAction<CNodeClickedMiddleState> {
  @Override
  public void execute(final CNodeClickedMiddleState state, final MouseEvent event) {
    EditCombinedNodeHelper.setActiveLabelContent(
        (CombinedGraph) state.getGraph(), state.getNode(), event);

    EditCombinedNodeHelper.setCaretStart(state.getGraph(), state.getNode(), event);
    EditCombinedNodeHelper.setCaretEnd(state.getGraph(), state.getNode(), event);
  }
}
