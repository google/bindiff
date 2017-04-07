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
