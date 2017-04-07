// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgePressedLeftState;

import java.awt.event.MouseEvent;


public class CDefaultEdgePressedLeftAction implements IStateAction<CEdgePressedLeftState> {
  @Override
  public void execute(final CEdgePressedLeftState state, final MouseEvent event) {
    if (state.getGraph().getEditMode().getLabelEventHandler().isActive()) {
      CEditNodeHelper.removeCaret(state.getGraph());
    }
  }
}
