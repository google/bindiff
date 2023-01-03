// Copyright 2011-2023 Google LLC
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

public class PostorderIterator {
  private final Stack<Pair<IZyTreeNode, Integer>> traversalStack =
      new Stack<Pair<IZyTreeNode, Integer>>();
  private final IZyTreeNode m_root;
  private boolean m_started = false;

  public PostorderIterator(final IZyTreeNode root) {
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

      do {
        if (traversalStack.empty()) {
          return false;
        }

        final Pair<IZyTreeNode, Integer> parentElement = traversalStack.pop();

        final int childrenProcessed = parentElement.second() + 1;

        if (childrenProcessed > parentElement.first().getChildren().size()) {
          // The parent node was already processed

          continue;
        } else if (childrenProcessed == parentElement.first().getChildren().size()) {
          // Processing finished => Process the parent node now
          traversalStack.push(
              new Pair<IZyTreeNode, Integer>(parentElement.first(), childrenProcessed));

          return true;
        } else {
          traversalStack.push(
              new Pair<IZyTreeNode, Integer>(parentElement.first(), childrenProcessed));

          pushLongestPathFrom(parentElement.first().getChildren().get(childrenProcessed));

          return true;
        }
      } while (true);
    }

    return !traversalStack.empty();
  }
}
