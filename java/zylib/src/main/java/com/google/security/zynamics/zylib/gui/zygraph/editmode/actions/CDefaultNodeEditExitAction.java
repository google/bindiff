// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeEditExitState;

import java.awt.event.MouseEvent;


public class CDefaultNodeEditExitAction implements IStateAction<CNodeEditExitState> {
  @Override
  public void execute(final CNodeEditExitState state, final MouseEvent event) {
    CEditNodeHelper.removeCaret(state.getGraph());
  }
}
