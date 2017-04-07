// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.trees;

import java.util.ArrayList;

/**
 * Can be used to sort all tree nodes of a tree in depth-first order.
 */
public final class DepthFirstSorter {
  private DepthFirstSorter() {
    // You are not supposed to instantiate this class
  }

  /**
   * Given the root node of a tree, returns a list of all tree nodes below the root node in
   * depth-first order.
   * 
   * @param <ObjectType> Type of the objects stored in the tree nodes.
   * 
   * @param rootNode The root node where the search begins.
   * 
   * @return A list of tree nodes sorted in depth-first order.
   */
  public static <ObjectType> ArrayList<ITreeNode<ObjectType>> getSortedList(
      final ITreeNode<ObjectType> rootNode) {
    final DepthFirstIterator<ObjectType> iter = new DepthFirstIterator<ObjectType>(rootNode);

    final ArrayList<ITreeNode<ObjectType>> list = new ArrayList<ITreeNode<ObjectType>>();

    while (iter.hasNext()) {
      list.add(iter.next());
    }

    return list;
  }
}
