package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion;

import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.Icon;
import javax.swing.JPanel;

public interface Criterion {
  void addListener(ICriterionListener listener);

  String getCriterionDescription();

  JPanel getCriterionPanel();

  Icon getIcon();

  CriterionType getType();

  void removeAllListener();

  void removeListener(ICriterionListener listener);

  boolean matches(final ZyGraphNode<? extends CViewNode<?>> node);
}
