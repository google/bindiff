package com.google.security.zynamics.bindiff.gui.components.closeablebuttontab;

import com.google.common.base.Preconditions;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

public class TabLabel extends JLabel {
  private final JTabbedPane pane;

  private final TabButtonComponent buttonComponent;

  public TabLabel(final JTabbedPane tabbedPane, final TabButtonComponent buttonComponent) {
    Preconditions.checkNotNull(tabbedPane);
    Preconditions.checkNotNull(buttonComponent);

    pane = tabbedPane;
    this.buttonComponent = buttonComponent;
  }

  @Override
  public String getText() {
    if (buttonComponent != null) {
      final int i = pane.indexOfTabComponent(buttonComponent);

      if (i != -1) {
        return pane.getTitleAt(i);
      }
    }

    return null;
  }

  @Override
  public void setText(final String text) {
    if (buttonComponent != null) {
      final int i = pane.indexOfTabComponent(buttonComponent);

      if (i != -1) {
        pane.setTitleAt(i, text);
      }
    }
  }
}
