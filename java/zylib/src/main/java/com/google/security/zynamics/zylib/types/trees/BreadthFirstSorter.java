// Copyright 2011-2024 Google LLC
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

import java.util.ArrayList;

/** Can be used to sort all tree nodes of a tree in breadth-first order. */
public class BreadthFirstSorter {
  private BreadthFirstSorter() {
    // You are not supposed to instantiate this class
  }

  /**
   * Given the root node of a tree, returns a list of all tree nodes below the root node in
   * breadth-first order.
   *
   * @param <ObjectType> Type of the objects stored in the tree nodes.
   * @param rootNode The root node where the search begins.
   * @return A list of tree nodes sorted in breadth-first order.
   */
  public static <ObjectType> ArrayList<ITreeNode<ObjectType>> getSortedList(
      final ITreeNode<ObjectType> rootNode) {
    final BreadthFirstIterator<ObjectType> iter = new BreadthFirstIterator<ObjectType>(rootNode);

    final ArrayList<ITreeNode<ObjectType>> list = new ArrayList<ITreeNode<ObjectType>>();

    while (iter.hasNext()) {
      list.add(iter.next());
    }

    return list;
  }
}
