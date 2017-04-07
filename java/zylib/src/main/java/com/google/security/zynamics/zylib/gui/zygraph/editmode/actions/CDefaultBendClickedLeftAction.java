// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendClickedLeftState;

import java.awt.event.MouseEvent;


public class CDefaultBendClickedLeftAction implements IStateAction<CBendClickedLeftState> {
  @Override
  public void execute(final CBendClickedLeftState state, final MouseEvent event) {
    CMouseCursorHelper.setDefaultCursor(state.getGraph());
  }
}
