// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
