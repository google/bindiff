package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.root.RootCriterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ICriterionTreeListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import java.util.logging.Level;

/** Class that represents the non-visible model of a criterion tree. */
public class CriterionTree {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final ListenerProvider<ICriterionTreeListener> listeners = new ListenerProvider<>();

  /** Root node of the criterion tree. */
  private final CriterionTreeNode rootNode = new CriterionTreeNode(new RootCriterion());

  public void addListener(final ICriterionTreeListener listener) {
    listeners.addListener(listener);
  }

  public void appendNode(final ICriterionTreeNode node) {
    appendNode(rootNode, node);
  }

  public void appendNode(final ICriterionTreeNode parent, final ICriterionTreeNode child) {
    CriterionTreeNode.append(parent, child);

    for (final ICriterionTreeListener listener : listeners) {
      try {
        listener.nodeAppended(this, parent, child);
      } catch (final Exception e) {
        // FIXME: Never catch all exceptions!
        logger.at(Level.SEVERE).withCause(e).log("Append tree node");
      }
    }
  }

  public ICriterionTreeNode getRoot() {
    return rootNode;
  }

  public Criterion getRootCriterion() {
    return rootNode.getCriterion();
  }

  public void insertNode(final ICriterionTreeNode node) {
    insertNode(rootNode, node);
  }

  public void insertNode(final ICriterionTreeNode parent, final ICriterionTreeNode child) {
    CriterionTreeNode.insert(parent, child);

    for (final ICriterionTreeListener listener : listeners) {
      try {
        listener.nodeInserted(this, parent, child);
      } catch (final Exception e) {
        // FIXME: Never catch all exceptions!
        logger.at(Level.SEVERE).withCause(e).log("Insert tree node");
      }
    }
  }

  public void removeAll() {
    if (rootNode.getChildren().size() == 1) {
      removeNode(rootNode.getChildren().get(0));
    }

    for (final ICriterionTreeListener listener : listeners) {
      try {
        listener.removedAll(this);
      } catch (final Exception e) {
        // FIXME: Never catch all exceptions!
        logger.at(Level.SEVERE).withCause(e).log("Remove all tree Nodes");
      }
    }
  }

  public void removeListener(final ICriterionTreeListener criterionListener) {
    listeners.removeListener(criterionListener);
  }

  public void removeNode(final ICriterionTreeNode node) {
    CriterionTreeNode.remove(node);

    final ICriterionTreeNode parent = node.getParent();
    parent.getChildren().remove(node);

    for (final ICriterionTreeListener listener : listeners) {
      try {
        listener.nodeRemoved(this, node);
      } catch (final Exception e) {
        // FIXME: Never catch all exceptions!
        logger.at(Level.SEVERE).withCause(e).log("Remove tree node");
      }
    }
  }
}
