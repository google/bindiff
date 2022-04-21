// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
