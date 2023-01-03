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

package com.google.security.zynamics.zylib.disassembly;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.Pair;
import java.util.Stack;

/**
 * Tree iterator that can be used to iterate over trees in the order that gives you a printable
 * operand string assuming the tree is a standard Zynamics operand tree.
 *
 * <p>Usage:
 *
 * <p>OperandOrderIterator iterator = new OperandOrderIterator(rootNode);
 *
 * <p>while (iterator.next()) { IZyTreeNode currentNode = iterator.current(); // Do stuff with
 * currentNode }
 */
public class OperandOrderIterator {
  /** The stack is used to create the proper iteration sequence. */
  private final Stack<Pair<IOperandTreeNode, Integer>> m_traversalStack =
      new Stack<Pair<IOperandTreeNode, Integer>>();

  /** Root node of the tree to traverse. */
  private final IOperandTreeNode m_root;

  /** Flag that indicates whether the iteration process was started or not. */
  private boolean m_started = false;

  /**
   * Creates a new operand order iterator that can be used to iterate over trees.
   *
   * @param root The root node of the tree.
   */
  public OperandOrderIterator(final IOperandTreeNode root) {
    Preconditions.checkNotNull(root, "Internal Error: Root node can't be null");

    m_root = root;
  }

  /**
   * Pushes a path of nodes in correct traversal order onto the stack. The path stops either when a
   * leaf is reached or when a node with one child is reached since nodes with one child need to be
   * processed before its children are processed.
   *
   * <p>If a node with more than one child is found the left-most child is chosen because so far all
   * operands are commutative and it does not matter which child to chose first.
   *
   * @param node The start node of the path.
   */
  private void pushLongestPathFrom(final IOperandTreeNode node) {
    IOperandTreeNode current = node;

    do {
      m_traversalStack.push(new Pair<IOperandTreeNode, Integer>(current, 0));

      if ((current.getChildren().size() <= 1)
          || (current.getType() == ExpressionType.EXPRESSION_LIST)) {
        break;
      }

      current = current.getChildren().get(0);

    } while (true);
  }

  /**
   * Returns the node the iterator currently points to.
   *
   * @return The node the iterator currently points to.
   */
  public IOperandTreeNode current() {
    return m_traversalStack.lastElement().first();
  }

  /**
   * Moves the iterator to the next node in the proper iteration order.
   *
   * @return False, if the iteration process is finished. True, otherwise.
   */
  public boolean next() {
    if (!m_started) {
      // At the start of the iteration process it is necessary
      // to push all nodes up to the first node to process
      // onto the stack.

      pushLongestPathFrom(m_root);

      m_started = true;
    } else {
      if (m_traversalStack.empty()) {
        // If the user hits next when the stack is already empty (as indicated
        // by the return value of the previous call to next) he gets a runtime
        // exception that tells him that he used the iterator improperly.

        throw new RuntimeException("Internal Error: Traversal already finished");
      }

      // The top of the stack always contains the last processed node.
      // To find out what to do next, we pop the last processed node off the
      // stack and examine it.

      final Pair<IOperandTreeNode, Integer> lastProcessed = m_traversalStack.pop();

      // The node that was last processed.
      final IOperandTreeNode lastProcessedNode = lastProcessed.first();

      // The number of children of the last processed node that were
      // already processed.
      final int lastProcessedChildrenProcessed = lastProcessed.second();

      if (lastProcessedChildrenProcessed < lastProcessedNode.getChildren().size()) {
        // If we're here, there are more children to process. Now we have
        // to distinguish between nodes with two or more children and nodes
        // with just one child. Those nodes with two or more children are
        // infix operators, meaning that we have to process them after
        // each but the last of their children. Nodes with just one child
        // are prefix operators and were already handled when moving down
        // through the tree.

        if (lastProcessed.first().getChildren().size() > 1) {
          // We're dealing with an infix operator. The infix operator
          // must be revisited after each of its children is processed.
          // That's why it needs to go back onto the stack.

          m_traversalStack.add(lastProcessed);
        }

        pushLongestPathFrom(lastProcessedNode.getChildren().get(lastProcessedChildrenProcessed));

        return true;
      } else {
        if (m_traversalStack.empty()) {
          // That's it. The stack is empty and all children of the last
          // processed node were processed previously. We're done.

          return false;
        } else {
          // If the stack is not empty we need to pop all completely
          // processed nodes off the stack. Those are the nodes whose
          // children were all completely processed before.

          do {
            final Pair<IOperandTreeNode, Integer> parent = m_traversalStack.pop();

            if (parent.second() < (parent.first().getChildren().size() - 1)) {
              // We found a node that still needs processing. Increase
              // its number of processed children and push it back onto
              // the stack.
              m_traversalStack.push(
                  new Pair<IOperandTreeNode, Integer>(parent.first(), parent.second() + 1));

              return true;
            }
          } while (!m_traversalStack.empty());
        }
      }
    }

    return !m_traversalStack.empty();
  }
}
