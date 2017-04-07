// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundDraggedRightState;

import java.awt.event.MouseEvent;


public class CDefaultBackgroundDraggedRightAction implements
    IStateAction<CBackgroundDraggedRightState> {
  @Override
  public void execute(final CBackgroundDraggedRightState state, final MouseEvent event) {
    CMouseCursorHelper.setMoveCursor(state.getGraph());
  }
}
