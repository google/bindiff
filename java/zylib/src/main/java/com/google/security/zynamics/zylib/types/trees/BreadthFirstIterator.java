// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.trees;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Iterator that can be used to iterate over a tree in breadth-first order.
 * 
 * @param <ObjectType> Types of the objects stored in the tree nodes.
 */
public class BreadthFirstIterator<ObjectType> implements Iterator<ITreeNode<ObjectType>> {
  private final Queue<ITreeNode<ObjectType>> m_path = new LinkedList<ITreeNode<ObjectType>>();

  /**
   * Creates a new iterator object.
   * 
   * @param rootNode Root node where iteration begins.
   */
  public BreadthFirstIterator(final ITreeNode<ObjectType> rootNode) {
    for (final ITreeNode<ObjectType> treeNode : rootNode.getChildren()) {
      m_path.add(treeNode);
    }
  }

  @Override
  public boolean hasNext() {
    return !m_path.isEmpty();
  }

  @Override
  public ITreeNode<ObjectType> next() {
    final ITreeNode<ObjectType> current = m_path.remove();

    for (final ITreeNode<ObjectType> child : current.getChildren()) {
      m_path.add(child);
    }

    return current;
  }

  @Override
  public void remove() {
  }
}
