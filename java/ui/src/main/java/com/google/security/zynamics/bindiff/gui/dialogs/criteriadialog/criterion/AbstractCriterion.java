package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion;

import com.google.security.zynamics.zylib.general.ListenerProvider;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;

public abstract class AbstractCriterion implements Criterion {
  private final ListenerProvider<ICriterionListener> listeners = new ListenerProvider<>();

  protected void notifyListeners() {
    for (final ICriterionListener listener : listeners) {
      listener.criterionChanged();
    }
  }

  @Override
  public void addListener(final ICriterionListener listener) {
    listeners.addListener(listener);
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public void removeAllListener() {
    final List<ICriterionListener> cache = new ArrayList<>();

    for (final ICriterionListener l : listeners) {
      cache.add(l);
    }

    for (final ICriterionListener l : cache) {
      removeListener(l);
    }
  }

  @Override
  public void removeListener(final ICriterionListener listener) {
    listeners.removeListener(listener);
  }
}
