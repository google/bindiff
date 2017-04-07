// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.trees;

import java.util.List;

/**
 * Interface for tree nodes.
 * 
 * @param <ObjectType> Objects stored in the tree nodes.
 */
public interface ITreeNode<ObjectType> {
  /**
   * Adds a child node to the tree node.
   * 
   * @param child The child node.
   */
  void addChild(ITreeNode<ObjectType> child);

  /**
   * Returns the child nodes of the tree node.
   * 
   * @return The child nodes of the tree node.
   */
  List<? extends ITreeNode<ObjectType>> getChildren();

  /**
   * Returns the object stored in the tree node.
   * 
   * @return The object stored in the tree node.
   */
  ObjectType getObject();

  /**
   * Returns the parent node of the tree node.
   * 
   * @return The parent node of the tree node.
   */
  ITreeNode<ObjectType> getParent();

  /**
   * Removes a child node from the node.
   * 
   * @param child The child node to remove.
   */
  void removeChild(ITreeNode<ObjectType> child);

  /**
   * Changes the parent node of the node.
   * 
   * @param parent The new parent node.
   */
  void setParent(ITreeNode<ObjectType> parent);
}
