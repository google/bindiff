// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphSelectionListener;

import y.base.GraphEvent;
import y.view.Graph2D;
import y.view.Graph2DSelectionEvent;
import y.view.Selections;


public class ZyGraphSelectionObserver extends Selections.SelectionStateObserver {
  /**
   * List of listeners that are notified about selection events.
   */
  private final ListenerProvider<IZyGraphSelectionListener> m_selectionListeners =
      new ListenerProvider<IZyGraphSelectionListener>();

  private int sequenceCounter = 0;

  private boolean fireFlag = false;

  // In this class we're trying to cache a multi-selection event
  // to generate just a single graph selection event.
  //
  // Single-Selection events have the following form:
  //
  // 1. onGraphEvent comes with event.getType() == PreEvent
  // 2. onGraphEvent comes with event.getType() == PostEvent
  // 3. The actual events arrive at onGraph2DSelectionEvent
  //
  // Multi-Selection events work differently.
  //
  // 1. onGraphEvent comes with event.getType() == PreEvent
  // 2. onGraphEvent comes with event.getType() == PreEvent
  // 3. onGraphEvent comes with event.getType() == PostEvent
  // 4. The actual events arrive at onGraph2DSelectionEvent
  // 5. onGraphEvent comes with event.getType() == PostEvent
  //
  // We try to convert the multi-selection event into a single-
  // selection event by counting the events as they arrive.

  /**
   * Notifies all listeners about selection changes.
   */
  private void notifySelectionListeners() {
    for (final IZyGraphSelectionListener listener : m_selectionListeners) {
      // ESCA-JAVA0166: Catch Exception because we are calling a listener function
      try {
        listener.selectionChanged();
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  protected void updateSelectionState(final Graph2D graph) {
    notifySelectionListeners();
  }

  /**
   * Adds a graph selection listener that is notified when the selection of the graph changes.
   * 
   * @param listener The listener to add.
   */
  public void addListener(final IZyGraphSelectionListener listener) {
    m_selectionListeners.addListener(listener);
  }

  @Override
  public void onGraph2DSelectionEvent(final Graph2DSelectionEvent e) {
    // Node: Animated layout could select bends. Bends should be ignored here.
    // Bend selection should not trigger any action of the listeners.
    if (e.isBendSelection() || e.isEdgeSelection()) {
      return;
    }

    // This function is called for each single event in an
    // event sequence.

    // In this function we fire the event if all event
    // sequences finished.
    if ((sequenceCounter == 0) && fireFlag) {
      updateSelectionState(e.getGraph2D());

      // We have to make sure that this event is only
      // sent once. When the next event sequence starts
      // this flag is reset.
      fireFlag = false;
    }
  }

  @Override
  public void onGraphEvent(final GraphEvent graphevent) {
    // This function receives only multi-selection events.

    if (graphevent.getType() == GraphEvent.PRE_EVENT) {
      // Count the started event sequences
      sequenceCounter++;
    } else if (graphevent.getType() == GraphEvent.POST_EVENT) {
      // Finish the counting of an event sequence

      sequenceCounter--;

      // If all event sequences finished, we can fire the event.
      if (sequenceCounter == 0) {
        updateSelectionState((Graph2D) graphevent.getGraph());

        // This flag makes sure to fire the event for single
        // selection events in onGraph2DSelectionEvent
        fireFlag = true;
      }
    }
  }

  /**
   * Removes a selection listener from the graph.
   * 
   * @param listener The listener to remove.
   */
  public void removeListener(final IZyGraphSelectionListener listener) {
    m_selectionListeners.removeListener(listener);
  }
}
