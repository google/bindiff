// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CBendHoverState;

import java.awt.event.MouseEvent;


public class CDefaultBendHoverAction implements IStateAction<CBendHoverState> {
  @Override
  public void execute(final CBendHoverState state, final MouseEvent event) {
    CMouseCursorHelper.setHandCursor(state.getGraph());
  }
}
