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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.ICriterionListener;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriterionTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriterionTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.ICriterionTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriterionTreeNode;
import javax.swing.tree.TreePath;

/**
 * This class provides the basic operations of the expression, which are remove, insert and append
 * criterion.
 */
public class ExpressionTreeActionProvider {
  private final JCriterionTree jtree;
  private final CriterionTree ctree;
  private final ICriterionListener internalCriterionListener = new InternalCriterionListener();

  public ExpressionTreeActionProvider(final JCriterionTree jtree, final CriterionTree ctree) {
    this.jtree = jtree;
    this.ctree = ctree;

    jtree.getModel().setActionProvider(this);
  }

  private static ICriterionTreeNode findNode(
      final ICriterionTreeNode node, final Criterion criterion) {
    if (node.getCriterion() == criterion) {
      return node;
    }

    for (final ICriterionTreeNode child : node.getChildren()) {
      final ICriterionTreeNode childNode = findNode(child, criterion);

      if (childNode != null) {
        return childNode;
      }
    }

    return null;
  }

  public void appendCriterion(final Criterion criterion) {
    final TreePath path = jtree.getCurrentCriterionPath();

    if (path == null) {
      return;
    }

    criterion.addListener(internalCriterionListener);

    final AbstractCriterionTreeNode node = (AbstractCriterionTreeNode) path.getLastPathComponent();
    final ICriterionTreeNode appendNode = findNode(ctree.getRoot(), node.getCriterion());

    ctree.appendNode(appendNode, new CriterionTreeNode(criterion));
  }

  public CriterionTree getCriterionTree() {
    return ctree;
  }

  public JCriterionTree getJTree() {
    return jtree;
  }

  public void insertCriterion(final Criterion criterion) {
    final TreePath path = jtree.getCurrentCriterionPath();

    if (path == null) {
      return;
    }

    criterion.addListener(internalCriterionListener);

    final AbstractCriterionTreeNode node = (AbstractCriterionTreeNode) path.getLastPathComponent();
    final ICriterionTreeNode insertNode = findNode(ctree.getRoot(), node.getCriterion());

    ctree.insertNode(insertNode, new CriterionTreeNode(criterion));
  }

  public void remove(final TreePath path) {
    if (path == null) {
      return;
    }

    final AbstractCriterionTreeNode node = (AbstractCriterionTreeNode) path.getLastPathComponent();
    final ICriterionTreeNode removeNode = findNode(ctree.getRoot(), node.getCriterion());

    ctree.removeNode(removeNode);
  }

  public void removeAll() {
    ctree.removeAll();
  }

  private class InternalCriterionListener implements ICriterionListener {
    @Override
    public void criterionChanged() {
      jtree.updateUI();
    }
  }
}
