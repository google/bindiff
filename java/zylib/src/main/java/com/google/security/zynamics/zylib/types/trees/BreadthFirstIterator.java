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
