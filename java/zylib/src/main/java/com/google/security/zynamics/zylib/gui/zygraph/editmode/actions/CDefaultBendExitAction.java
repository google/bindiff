// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendExitState;

import java.awt.event.MouseEvent;


public class CDefaultBendExitAction implements IStateAction<CBendExitState> {
  @Override
  public void execute(final CBendExitState state, final MouseEvent event) {
    CMouseCursorHelper.setDefaultCursor(state.getGraph());
  }
}
