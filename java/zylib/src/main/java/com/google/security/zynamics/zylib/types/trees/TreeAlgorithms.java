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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

public class TreeAlgorithms {
  /**
   * Tests whether a node dominates another node.
   * 
   * @param node The node of the dominator tree where the search starts.
   * @param target The dominator node.
   * @param source The dominated node.
   * 
   * @return True, if the dominator node dominates the dominated node. False, otherwise.
   */
  public static <ObjectType> boolean dominates(final ITreeNode<ObjectType> node,
      final ITreeNode<ObjectType> target, final ITreeNode<ObjectType> source) {
    if (node.getObject() == source) {
      return false;
    } else if (node.getObject() == target) {
      return true;
    } else {
      for (final ITreeNode<ObjectType> child : node.getChildren()) {
        if (!dominates(child, target, source)) {
          return false;
        }
      }

      return true;
    }
  }

  /**
   * Calculate the dominate relation of a tree node.
   * 
   * @param treeNode The tree node for which the dominate relation is calculated.
   * @return A HashMap of the tree node with all nodes that dominate the tree node.
   */
  public static <NodeType> HashMap<ITreeNode<NodeType>, Set<ITreeNode<NodeType>>> getDominateRelation(
      final ITreeNode<NodeType> treeNode) {
    final HashMap<ITreeNode<NodeType>, Set<ITreeNode<NodeType>>> dominationRelation =
        new HashMap<ITreeNode<NodeType>, Set<ITreeNode<NodeType>>>();

    final HashSet<ITreeNode<NodeType>> hashSet = new HashSet<ITreeNode<NodeType>>();
    hashSet.add(treeNode);
    dominationRelation.put(treeNode, hashSet);

    final DepthFirstIterator<NodeType> iter = new DepthFirstIterator<NodeType>(treeNode);

    while (iter.hasNext()) {
      final ITreeNode<NodeType> currentTreeElement = iter.next();
      final ITreeNode<NodeType> currentTreeElementParent = currentTreeElement.getParent();
      final Set<ITreeNode<NodeType>> currentTreeElementParents =
          Sets.newHashSet(dominationRelation.get(currentTreeElementParent));
      currentTreeElementParents.add(currentTreeElementParent);
      currentTreeElementParents.add(currentTreeElement);
      dominationRelation.put(currentTreeElement, currentTreeElementParents);
    }

    return dominationRelation;
  }

  /**
   * Calculate the dominate relation of a tree node.
   * 
   * @param treeNode The tree node for which the dominate relation is calculated.
   * @return A HashMap of the tree node with all nodes that dominate the tree node.
   */
  // public static <NodeType> HashMap<ITreeNode<NodeType>, List<ITreeNode<NodeType>>>
  // getDominateRelation(final ITreeNode<NodeType> treeNode)
  // {
  // final HashMap<ITreeNode<NodeType>, List<ITreeNode<NodeType>>> dominateRelation = new
  // HashMap<ITreeNode<NodeType>, List<ITreeNode<NodeType>>>();
  //
  // if (treeNode.getChildren().size() == 0)
  // {
  // final ArrayList<ITreeNode<NodeType>> dominates = new ArrayList<ITreeNode<NodeType>>();
  // dominates.add(treeNode);
  // dominateRelation.put(treeNode, dominates);
  // return dominateRelation;
  // }
  //
  // for (final ITreeNode<NodeType> childNode : treeNode.getChildren())
  // {
  // final HashMap<ITreeNode<NodeType>, List<ITreeNode<NodeType>>> tempDominaton =
  // getDominateRelation(childNode);
  // for (final Entry<ITreeNode<NodeType>, List<ITreeNode<NodeType>>> tempDominationEntry :
  // tempDominaton.entrySet())
  // {
  // final List<ITreeNode<NodeType>> dominators = tempDominationEntry.getValue();
  // dominators.add(treeNode);
  // dominateRelation.put(tempDominationEntry.getKey(), dominators);
  // }
  // }
  //
  // final ArrayList<ITreeNode<NodeType>> dominates = new ArrayList<ITreeNode<NodeType>>();
  // dominates.add(treeNode);
  // dominateRelation.put(treeNode, dominates);
  // return dominateRelation;
  // }

}
