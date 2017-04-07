package com.google.security.zynamics.bindiff.gui.dialogs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class BaseDialog extends JDialog {
  public BaseDialog(final Window parent, final String title) {
    super(parent, ModalityType.APPLICATION_MODAL);
    init();

    setTitle(title);
  }

  public BaseDialog(
      final Window parent, final String title, final int minWidth, final int minHeight) {
    this(parent, title);

    setSize(minWidth, minHeight);
    setMinimumSize(new Dimension(minWidth, minHeight));
  }

  private void init() {
    getRootPane()
        .registerKeyboardAction(
            new ActionListener() {
              @Override
              public void actionPerformed(final ActionEvent unused) {
                setVisible(false);
                dispose();
              }
            },
            "doEscape",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
  }

  @Override
  public void setVisible(final boolean visible) {
    if (visible) {
      UIManager.put("TabbedPane.tabAreaInsets", new Insets(2, 2, 0, 2));
      UIManager.put("TabbedPane.contentBorderInsets", new Insets(4, 2, 3, 2));

      SwingUtilities.updateComponentTreeUI(this);
    }
    super.setVisible(visible);
  }
}
