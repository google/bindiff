// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

import java.util.Date;

@SuppressWarnings("hiding")
public interface IViewListener<ViewType> {
  /**
   * Invoked when the description of a view is changed.
   * 
   * @param view The view where the description is changed.
   * @param description The description which is the new view description.
   */
  void changedDescription(ViewType view, String description);

  /**
   * Invoked when the modification date of a view is changed.
   * 
   * @param view The view whose modification date is changed.
   * @param modificationDate The new modification date.
   */
  void changedModificationDate(ViewType view, Date modificationDate);

  /**
   * Invoked if the name of a view has been changed.
   * 
   * @param view The view whose name has been changed.
   * @param name The new name of the view.
   */
  void changedName(ViewType view, String name);

  /**
   * Invoked if the view is in the closing state.
   * 
   * @return true = view can be closed.
   */
  boolean closingView(ViewType view);

  /**
   * Invoked if a view has been loaded.
   * 
   * @param view The view that has just been loaded.
   */
  void loadedView(ViewType view);
}
