// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeClickedMiddleState;

import java.awt.event.MouseEvent;


public class CDefaultNodeClickedMiddleAction implements IStateAction<CNodeClickedMiddleState> {
  @Override
  public void execute(final CNodeClickedMiddleState state, final MouseEvent event) {
    CEditNodeHelper.setCaretStart(state.getGraph(), state.getNode(), event);
    CEditNodeHelper.setCaretEnd(state.getGraph(), state.getNode(), event);
  }
}
