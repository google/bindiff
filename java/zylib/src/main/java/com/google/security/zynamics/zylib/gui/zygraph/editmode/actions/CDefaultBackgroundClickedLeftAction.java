// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.states.CBackgroundClickedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.awt.event.MouseEvent;


/**
 * Default action that is executed on
 * 
 * @param <T> Type of the nodes in the graph.
 */
public class CDefaultBackgroundClickedLeftAction<T extends ZyGraphNode<?>> implements
    IStateAction<CBackgroundClickedLeftState<T>> {
  /**
   * Unselects all nodes in the graph.
   * 
   * @param graph The graph whose nodes are unselected.
   */
  protected void unselectAll(final AbstractZyGraph<T, ?> graph) {
    graph.selectNodes(graph.getNodes(), false);
  }

  @Override
  public void execute(final CBackgroundClickedLeftState<T> state, final MouseEvent event) {
    unselectAll(state.getGraph());
  }
}
