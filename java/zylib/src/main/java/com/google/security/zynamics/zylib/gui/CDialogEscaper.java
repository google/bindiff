package com.google.security.zynamics.zylib.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import com.google.common.base.Preconditions;

// TODO(cblichmann): Using this class is weird, it should be just a static method.
public class CDialogEscaper {
  /**
   * Registers a keyboard action for the passed dialog such that it will close when the "ESC" key is
   * pressed.
   *
   * @param dialog The Dialog which can now be closed by the "ESC" Key.
   */
  public CDialogEscaper(final JDialog dialog) {
    Preconditions.checkNotNull(dialog, "Error: dialog argument can not be null");

    // Allow the user to close the dialog with the ESC key.
    dialog.getRootPane().registerKeyboardAction(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent unused) {
        dialog.setVisible(false);
        dialog.dispose();
      }
    }, "doEscape", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
        JComponent.WHEN_IN_FOCUSED_WINDOW);
  }
}
