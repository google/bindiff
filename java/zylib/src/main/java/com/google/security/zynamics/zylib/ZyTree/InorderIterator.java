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

package com.google.security.zynamics.zylib.ZyTree;

import com.google.security.zynamics.zylib.general.Pair;

import java.util.Stack;


public class InorderIterator {
  private final Stack<Pair<IZyTreeNode, Integer>> traversalStack =
      new Stack<Pair<IZyTreeNode, Integer>>();
  private final IZyTreeNode m_root;
  private boolean m_started = false;

  public InorderIterator(final IZyTreeNode root) {
    m_root = root;
  }

  private void pushLongestPathFrom(final IZyTreeNode node) {
    IZyTreeNode current = node;

    do {
      traversalStack.push(new Pair<IZyTreeNode, Integer>(current, 0));

      if (current.getChildren().size() == 0) {
        break;
      }

      current = current.getChildren().get(0);
    } while (true);
  }

  public IZyTreeNode current() {
    return traversalStack.lastElement().first();
  }

  public boolean next() {
    if (!m_started) {
      pushLongestPathFrom(m_root);

      m_started = true;
    } else {
      if (traversalStack.empty()) {
        throw new RuntimeException("Internal Error: Traversal already finished");
      }

      final Pair<IZyTreeNode, Integer> justProcessed = traversalStack.pop();

      final IZyTreeNode justProcessedNode = justProcessed.first();
      final int justProcessedChildrenProcessed = justProcessed.second();

      if (traversalStack.empty()) {
        if (justProcessedChildrenProcessed == justProcessedNode.getChildren().size()) {
          // At this point we're done
          return false;
        } else {
          if (justProcessedNode.getChildren().size() == 0) {
            throw new RuntimeException("Error");
          } else if (justProcessedNode.getChildren().size() == 1) {
            pushLongestPathFrom(justProcessed.first().getChildren()
                .get(justProcessedChildrenProcessed));
          } else {
            traversalStack.push(new Pair<IZyTreeNode, Integer>(justProcessed.first().getChildren()
                .get(justProcessedChildrenProcessed), 0));
          }

        }
      } else {
        if (justProcessedChildrenProcessed == justProcessedNode.getChildren().size()) {
          // At this point we've handled all the children of the node. The node
          // can be removed and we continue with the next node on the stack.

          // We have to adjust the parent node though.
          final Pair<IZyTreeNode, Integer> parentProcessed = traversalStack.pop();

          traversalStack.push(new Pair<IZyTreeNode, Integer>(parentProcessed.first(),
              parentProcessed.second() + 1));
        } else {
          if (justProcessedNode.getChildren().size() == 0) {
            throw new RuntimeException("Error");
          } else if (justProcessedNode.getChildren().size() == 1) {
            pushLongestPathFrom(justProcessed.first().getChildren()
                .get(justProcessedChildrenProcessed));
          } else {
            traversalStack.push(new Pair<IZyTreeNode, Integer>(justProcessed.first().getChildren()
                .get(justProcessedChildrenProcessed), 0));
          }
        }
      }
    }

    return !traversalStack.empty();
  }

}
