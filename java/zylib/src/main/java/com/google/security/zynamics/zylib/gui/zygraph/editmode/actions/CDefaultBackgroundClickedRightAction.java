// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundClickedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.awt.event.MouseEvent;


/**
 * Default action that is executed on
 * 
 * @param <T> Type of the nodes in the graph.
 */
public class CDefaultBackgroundClickedRightAction<T extends ZyGraphNode<?>> implements
    IStateAction<CBackgroundClickedRightState<T>> {
  @Override
  public void execute(final CBackgroundClickedRightState<T> state, final MouseEvent event) {
    CMouseCursorHelper.setDefaultCursor(state.getGraph());
  }
}
