// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEdgeClickHandler;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeClickedLeftState;

import java.awt.event.MouseEvent;


public class CDefaultEdgeClickedLeftAction implements IStateAction<CEdgeClickedLeftState> {
  protected void handleClick(final CEdgeClickedLeftState state, final MouseEvent event) {
    CEdgeClickHandler.handleEdgeClicks(state.getGraph(), state.getEdge(), event);
  }

  @Override
  public void execute(final CEdgeClickedLeftState state, final MouseEvent event) {
    handleClick(state, event);
  }
}
