// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.JHexPanel;

import javax.swing.JPopupMenu;

/**
 * This interface must be implemented by all classes that want to provide context menus for the
 * JHexView control.
 * 
 */
public interface IMenuCreator {

  /**
   * This function is called to generate a popup menu after the user right-clicked somewhere in the
   * hex control.
   * 
   * @param offset The offset of the right-click.
   * 
   * @return The popup menu suitable for that offset or null if no popup menu should be shown.
   */
  JPopupMenu createMenu(long offset);
}
