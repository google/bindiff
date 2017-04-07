// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.awt.event.MouseEvent;


/**
 * Interface for all objects that describe mouse state.
 */
public interface IMouseState {
  CStateFactory<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> getStateFactory();

  /**
   * Executes a mouse dragged event on the mouse state.
   * 
   * @param event The mouse event object that describes the mouse drag.
   * @param graph The graph the mouse state is handled for.
   * 
   * @return An object that describes the result of the event.
   */
  IMouseStateChange mouseDragged(MouseEvent event, AbstractZyGraph<?, ?> graph);

  /**
   * Executes a mouse moved event on the mouse state.
   * 
   * @param event The mouse event object that describes the mouse move.
   * @param graph The graph the mouse state is handled for.
   * 
   * @return An object that describes the result of the event.
   */
  IMouseStateChange mouseMoved(MouseEvent event, AbstractZyGraph<?, ?> graph);

  /**
   * Executes a mouse pressed event on the mouse state.
   * 
   * @param event The mouse event object that describes the mouse press.
   * @param graph The graph the mouse state is handled for.
   * 
   * @return An object that describes the result of the event.
   */
  IMouseStateChange mousePressed(MouseEvent event, AbstractZyGraph<?, ?> graph);

  /**
   * Executes a mouse released event on the mouse state.
   * 
   * @param event The mouse event object that describes the mouse release.
   * @param graph The graph the mouse state is handled for.
   * 
   * @return An object that describes the result of the event.
   */
  IMouseStateChange mouseReleased(MouseEvent event, AbstractZyGraph<?, ?> graph);
}
