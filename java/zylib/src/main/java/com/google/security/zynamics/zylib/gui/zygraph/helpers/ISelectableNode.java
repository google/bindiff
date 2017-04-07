// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;


/**
 * Nodes that implement this interface unlock {@link GraphHelpers} functions that require tests
 * whether a node is selected or not.
 */
public interface ISelectableNode {
  /**
   * Determines whether the node is selected or not.
   * 
   * @return True, if the node is selected. False, if the node is deselected.
   */
  boolean isSelected();
}
