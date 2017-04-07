// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.trees;

/**
 * Simple tree class.
 * 
 * @param <ObjectType> Type of the objects stored in the tree.
 */
public class Tree<ObjectType> {
  /**
   * Root node of the tree.
   */
  private final ITreeNode<ObjectType> m_rootNode;

  /**
   * Creates a new tree.
   * 
   * @param rootNode Root node of the tree.
   */
  public Tree(final ITreeNode<ObjectType> rootNode) {
    m_rootNode = rootNode;
  }

  /**
   * Returns the root node of the tree.
   * 
   * @return The root node of the tree.
   */
  public ITreeNode<ObjectType> getRootNode() {
    return m_rootNode;
  }
}
