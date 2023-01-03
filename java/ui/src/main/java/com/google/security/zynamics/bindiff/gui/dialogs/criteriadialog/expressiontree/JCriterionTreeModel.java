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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionType;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriterionTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.ICriterionTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriterionTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.CriterionTreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class JCriterionTreeModel extends DefaultTreeModel {

  private final List<CriterionCreator> criteria;
  private ExpressionTreeActionProvider actionProvider;
  private final JTree jtree;

  public JCriterionTreeModel(
      final JTree jtree, final CriterionTree criterionTree, final List<CriterionCreator> criteria) {
    super(null);

    this.jtree = jtree;
    this.criteria = criteria;
    ICriterionTreeListener internalTreeListener = new CriterionTreeListener();
    criterionTree.addListener(internalTreeListener);
  }

  private static AbstractCriterionTreeNode findParentNode(
      final AbstractCriterionTreeNode node, final Criterion criterion) {
    if (node.getCriterion() == criterion) {
      return node;
    }

    for (int i = 0; i < node.getChildCount(); i++) {
      final AbstractCriterionTreeNode child = (AbstractCriterionTreeNode) node.getChildAt(i);

      final AbstractCriterionTreeNode parent = findParentNode(child, criterion);

      if (parent != null) {
        return parent;
      }
    }

    return null;
  }

  @Override
  public void nodeStructureChanged(final TreeNode node) {
    final Set<Criterion> criterionSet = new HashSet<>();

    final Enumeration<TreePath> expandedPaths =
        jtree.getExpandedDescendants(new TreePath(getRoot()));
    if (expandedPaths != null) {
      while (expandedPaths.hasMoreElements()) {
        final TreePath path = expandedPaths.nextElement();
        final AbstractCriterionTreeNode expandedNode =
            (AbstractCriterionTreeNode) path.getLastPathComponent();
        criterionSet.add(expandedNode.getCriterion());
      }
    }

    super.nodeStructureChanged(node);

    final Enumeration<?> nodes = ((AbstractCriterionTreeNode) getRoot()).breadthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      final AbstractCriterionTreeNode n = (AbstractCriterionTreeNode) nodes.nextElement();
      if (criterionSet.contains(n.getCriterion())) {
        jtree.expandPath(new TreePath(n.getPath()));
      }
    }

    jtree.updateUI();
  }

  public void setActionProvider(final ExpressionTreeActionProvider actionProvider) {
    this.actionProvider = actionProvider;
  }

  public void sortChildren(final AbstractCriterionTreeNode parentNode) {
    final List<AbstractCriterionTreeNode> operators = new ArrayList<>();
    final List<AbstractCriterionTreeNode> conditions = new ArrayList<>();
    final List<AbstractCriterionTreeNode> minus = new ArrayList<>();

    final Enumeration<?> children = parentNode.children();
    while (children.hasMoreElements()) {
      final AbstractCriterionTreeNode child = (AbstractCriterionTreeNode) children.nextElement();
      final CriterionType type = child.getCriterion().getType();

      if (type == CriterionType.CONDITION) {
        conditions.add(child);
      } else {
        operators.add(child);
      }
    }

    parentNode.removeAllChildren();

    for (final AbstractCriterionTreeNode child : operators) {
      parentNode.add(child);
      child.setParent(parentNode);
    }
    for (final AbstractCriterionTreeNode child : conditions) {
      parentNode.add(child);
      child.setParent(parentNode);
    }
    for (final AbstractCriterionTreeNode child : minus) {
      parentNode.add(child);
      child.setParent(parentNode);
    }
  }

  private class CriterionTreeListener implements ICriterionTreeListener {
    @Override
    public void nodeAppended(
        final CriterionTree criterionTree,
        final ICriterionTreeNode parent,
        final ICriterionTreeNode child) {
      final AbstractCriterionTreeNode parentNode =
          findParentNode((AbstractCriterionTreeNode) getRoot(), parent.getCriterion());

      final CriterionTreeNode childNode =
          new CriterionTreeNode(child.getCriterion(), criteria, actionProvider);

      parentNode.add(childNode);
      childNode.setParent(parentNode);

      sortChildren(parentNode);

      nodeStructureChanged(parentNode);

      jtree.setSelectionPath(new TreePath(childNode.getPath()));
    }

    @Override
    public void nodeInserted(
        final CriterionTree criterionTree,
        final ICriterionTreeNode parent,
        final ICriterionTreeNode child) {
      final AbstractCriterionTreeNode parentNode =
          findParentNode((CriterionTreeNode) getRoot(), parent.getCriterion());

      final CriterionTreeNode newNode =
          new CriterionTreeNode(child.getCriterion(), criteria, actionProvider);

      final List<AbstractCriterionTreeNode> grandChildren = new ArrayList<>();

      final Enumeration<?> enumeration = parentNode.children();

      // has to be cached, other wise hasMoreElements returns false when child count / 2 is reached
      while (enumeration.hasMoreElements()) {
        grandChildren.add((AbstractCriterionTreeNode) enumeration.nextElement());
      }

      // cannot be done within the above while loop
      for (final AbstractCriterionTreeNode grandChild : grandChildren) {
        newNode.add(grandChild);
        grandChild.setParent(newNode);
      }

      parentNode.removeAllChildren();

      parentNode.add(newNode);
      newNode.setParent(parentNode);

      nodeStructureChanged(parentNode);

      jtree.setSelectionPath(new TreePath(newNode.getPath()));
    }

    @Override
    public void nodeRemoved(
        final CriterionTree criterionTree, final ICriterionTreeNode criterionNode) {
      final AbstractCriterionTreeNode treeNode =
          findParentNode((CriterionTreeNode) getRoot(), criterionNode.getCriterion());

      treeNode.deleteChildren();

      final AbstractCriterionTreeNode parent = (AbstractCriterionTreeNode) treeNode.getParent();

      parent.remove(treeNode);

      nodeStructureChanged(treeNode);

      jtree.setSelectionPath(new TreePath(parent.getPath()));
    }

    @Override
    public void removedAll(final CriterionTree criterionTree) {
      final AbstractCriterionTreeNode rootNode = (CriterionTreeNode) getRoot();

      rootNode.deleteChildren();

      nodeStructureChanged(rootNode);

      jtree.setSelectionPath(new TreePath(rootNode.getPath()));
    }
  }
}
