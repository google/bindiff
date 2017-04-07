// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.trees;

/**
 * Provides small helper functions for working with tree structures defined in this package.
 */
public class TreeHelpers {
  private TreeHelpers() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Tests whether a given node is an ancestor node of another node.
   * 
   * @param node The node to search for.
   * @param parent The parent node where the search begins.
   * 
   * @return True, if the node is an ancestor of parent. False, otherwise.
   */
  public static boolean isAncestor(final ITreeNode<?> node, final ITreeNode<?> parent) {
    if (node == null) {
      return false;
    } else if (parent == node) {
      return true;
    } else {
      return isAncestor(node.getParent(), parent);
    }
  }
}
