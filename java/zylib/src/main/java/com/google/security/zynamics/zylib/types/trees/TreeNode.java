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

import java.util.ArrayList;
import java.util.List;

/**
 * Default tree node implementation.
 * 
 * @param <ObjectType> Type of the objects stored in the tree.
 */
public class TreeNode<ObjectType> implements ITreeNode<ObjectType> {
  /**
   * Parent node of the tree node.
   */
  private ITreeNode<ObjectType> m_parent = null;

  /**
   * Children of the tree node.
   */
  private final List<ITreeNode<ObjectType>> m_children = new ArrayList<ITreeNode<ObjectType>>();

  /**
   * Object stored in the tree node.
   */
  private final ObjectType m_object;

  /**
   * Creates a new tree node.
   * 
   * @param object The object stored in the tree node.
   */
  public TreeNode(final ObjectType object) {
    m_object = object;
  }

  @Override
  public void addChild(final ITreeNode<ObjectType> child) {
    m_children.add(child);
  }

  @Override
  public List<ITreeNode<ObjectType>> getChildren() {
    return new ArrayList<ITreeNode<ObjectType>>(m_children);
  }

  @Override
  public ObjectType getObject() {
    return m_object;
  }

  @Override
  public ITreeNode<ObjectType> getParent() {
    return m_parent;
  }

  @Override
  public void removeChild(final ITreeNode<ObjectType> node) {
    m_children.remove(node);
  }

  @Override
  public void setParent(final ITreeNode<ObjectType> node) {
    m_parent = node;
  }

  @Override
  public String toString() {
    return "<" + m_object.toString() + ">";
  }
}
