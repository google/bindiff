// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.trees;

import java.util.Iterator;
import java.util.Stack;

import com.google.common.base.Preconditions;

/**
 * Iterator that can be used to iterate over a tree in depth-first order.
 * 
 * @param <ObjectType> Types of the objects stored in the tree nodes.
 */
public class DepthFirstIterator<ObjectType> implements Iterator<ITreeNode<ObjectType>> {
  private final Stack<ITreeNode<ObjectType>> m_path = new Stack<ITreeNode<ObjectType>>();

  /**
   * Creates a new iterator object.
   * 
   * @param rootNode Root node where iteration begins.
   */
  public DepthFirstIterator(final ITreeNode<ObjectType> rootNode) {
    Preconditions.checkNotNull(rootNode, "Error: Root node argument can not be null");

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
    final ITreeNode<ObjectType> current = m_path.pop();

    for (final ITreeNode<ObjectType> child : current.getChildren()) {
      m_path.add(child);
    }

    return current;
  }

  @Override
  public void remove() {
  }
}
