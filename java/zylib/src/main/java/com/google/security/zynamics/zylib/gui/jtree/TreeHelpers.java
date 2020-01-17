// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.zylib.gui.jtree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Helper class that provides JTree functions that are often used.
 */
public class TreeHelpers {
  private static void expandAll(final JTree tree, final TreePath parent, final boolean expand) {
    // Traverse children
    final TreeNode node = (TreeNode) parent.getLastPathComponent();

    if (node.getChildCount() >= 0) {
      for (final Enumeration<?> e = node.children(); e.hasMoreElements();) {
        final TreeNode n = (TreeNode) e.nextElement();
        final TreePath path = parent.pathByAddingChild(n);
        expandAll(tree, path, expand);
      }
    }

    // Expansion or collapse must be done bottom-up
    if (expand) {
      tree.expandPath(parent);
    } else {
      tree.collapsePath(parent);
    }
  }

  // is path1 descendant of path2
  private static boolean isDescendant(TreePath path1, final TreePath path2) {
    int count1 = path1.getPathCount();
    final int count2 = path2.getPathCount();
    if (count1 <= count2) {
      return false;
    }
    while (count1 != count2) {
      path1 = path1.getParentPath();
      count1--;
    }
    return path1.equals(path2);
  }

  /**
   * Tests whether a tree contains a given node.
   * 
   * @param tree The tree to search.
   * @param node The node to search for.
   * 
   * @return True, if the node is part of the tree. False, otherwise.
   */
  public static boolean contains(final JTree tree, final TreeNode node) {
    return isAncestor(node, (TreeNode) tree.getModel().getRoot());
  }

  public static void expandAll(final JTree tree, final boolean expand) {
    final TreeNode root = (TreeNode) tree.getModel().getRoot();

    // Traverse tree from root
    expandAll(tree, new TreePath(root), expand);
  }

  // the functions getExpansionState(...) and restoreExpanstionState(...)
  // can be used to keep the JTree expansion state when subtree folders were
  // moved by drag and drop see the example an the web site mentioned below
  // Swing: Retaining JTree Expansion State
  // http://www.javalobby.org/java/forums/t19857.html
  public static String getExpansionState(final JTree tree, final int row) {
    final TreePath rowPath = tree.getPathForRow(row);
    final StringBuffer buf = new StringBuffer();
    final int rowCount = tree.getRowCount();

    for (int i = row; i < rowCount; i++) {
      final TreePath path = tree.getPathForRow(i);
      if ((i == row) || isDescendant(path, rowPath)) {
        if (tree.isExpanded(path)) {
          buf.append(",");
          buf.append(String.valueOf(i - row));
        }
      } else {
        break;
      }
    }
    return buf.toString();
  }

  public static List<DefaultMutableTreeNode> getLastExpandedNodes(final JTree tree) {
    final List<DefaultMutableTreeNode> nodeList = new ArrayList<DefaultMutableTreeNode>();

    final int rowCount = tree.getRowCount();
    for (int i = 0; i < rowCount; i++) {
      final TreePath path = tree.getPathForRow(i);
      DefaultMutableTreeNode lastPathComponent = null;

      try {
        lastPathComponent = (DefaultMutableTreeNode) path.getLastPathComponent();
      } catch (final Exception e) {
        throw new IllegalArgumentException(
            "Cast failed! JTree must contain DefaultMuteableTreeNode or derived instances.");
      }

      if (lastPathComponent.isLeaf() || !tree.isExpanded(path)) {
        nodeList.add((DefaultMutableTreeNode) lastPathComponent.getParent());
      }
    }
    return nodeList;
  }

  /**
   * Finds the node at a given mouse cursor position.
   * 
   * @param tree The tree where the node is located.
   * @param x The x coordinate of the mouse position.
   * @param y The y coordinate of the mouse position.
   * 
   * @return The node at the given cursor position or null if there is no node at that position.
   */
  public static Object getNodeAt(final JTree tree, final int x, final int y) {
    final TreePath selPath = tree.getPathForLocation(x, y);
    return selPath != null ? selPath.getLastPathComponent() : null;
  }

  /**
   * Tests whether a given node is an ancestor node of another node.
   * 
   * @param node The node to search for.
   * @param parent The parent node where the search begins.
   * 
   * @return True, if the node is an ancestor of parent. False, otherwise.
   */
  public static boolean isAncestor(final TreeNode node, final TreeNode parent) {
    if (parent == node) {
      return true;
    }

    for (int i = 0; i < parent.getChildCount(); i++) {
      if (isAncestor(node, parent.getChildAt(i))) {
        return true;
      }
    }

    return false;
  }

  public static void restoreExpansionState(final JTree tree, final int row,
      final String expansionState) {
    final StringTokenizer stok = new StringTokenizer(expansionState, ",");

    while (stok.hasMoreTokens()) {
      final int token = row + Integer.parseInt(stok.nextToken());
      tree.expandRow(token);
    }
  }
}
