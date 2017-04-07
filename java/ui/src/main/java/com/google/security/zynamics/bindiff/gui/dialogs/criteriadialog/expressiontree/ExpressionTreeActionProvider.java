package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.ICriterium;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.ICriteriumListener;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriteriumTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriteriumTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.ICriteriumTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriteriumTreeNode;

import javax.swing.tree.TreePath;

/*
 * This class provides the basic operations of the expression, which are remove, insert and append
 * criterium.
 */
public class ExpressionTreeActionProvider {
  private final JCriteriumTree jtree;
  private final CriteriumTree ctree;
  private final ICriteriumListener internalCriteriumListener = new InternalCriteriumListener();

  public ExpressionTreeActionProvider(final JCriteriumTree jtree, final CriteriumTree ctree) {
    this.jtree = jtree;
    this.ctree = ctree;

    jtree.getModel().setActionProvider(this);
  }

  private ICriteriumTreeNode findNode(final ICriteriumTreeNode node, final ICriterium criterium) {
    if (node.getCriterium() == criterium) {
      return node;
    }

    for (final ICriteriumTreeNode child : node.getChildren()) {
      final ICriteriumTreeNode childNode = findNode(child, criterium);

      if (childNode != null) {
        return childNode;
      }
    }

    return null;
  }

  public void appendCriterium(final ICriterium criterium) {
    final TreePath path = jtree.getCurrentCriteriumPath();

    if (path == null) {
      return;
    }

    criterium.addListener(internalCriteriumListener);

    final AbstractCriteriumTreeNode node = (AbstractCriteriumTreeNode) path.getLastPathComponent();
    final ICriteriumTreeNode appendNode = findNode(ctree.getRoot(), node.getCriterium());

    ctree.appendNode(appendNode, new CriteriumTreeNode(criterium));
  }

  public CriteriumTree getCriteriumTree() {
    return ctree;
  }

  public JCriteriumTree getJTree() {
    return jtree;
  }

  public void insertCriterium(final ICriterium criterium) {
    final TreePath path = jtree.getCurrentCriteriumPath();

    if (path == null) {
      return;
    }

    criterium.addListener(internalCriteriumListener);

    final AbstractCriteriumTreeNode node = (AbstractCriteriumTreeNode) path.getLastPathComponent();
    final ICriteriumTreeNode insertNode = findNode(ctree.getRoot(), node.getCriterium());

    ctree.insertNode(insertNode, new CriteriumTreeNode(criterium));
  }

  public void remove(final TreePath path) {
    if (path == null) {
      return;
    }

    final AbstractCriteriumTreeNode node = (AbstractCriteriumTreeNode) path.getLastPathComponent();
    final ICriteriumTreeNode removeNode = findNode(ctree.getRoot(), node.getCriterium());

    ctree.removeNode(removeNode);
  }

  public void removeAll() {
    ctree.removeAll();
  }

  private class InternalCriteriumListener implements ICriteriumListener {
    @Override
    public void criteriumChanged() {
      jtree.updateUI();
    }
  }
}
