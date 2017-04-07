package com.google.security.zynamics.bindiff.gui.components;

import com.google.security.zynamics.bindiff.resources.Constants;
import java.awt.Component;
import java.awt.Frame;
import javax.swing.JOptionPane;

// TODO(cblichmann): Use ZyLib functionality here, we don't need this class.
public class MessageBox {
  private static Component validateParent(final Component parent) {
    if (isIconified(parent)) {
      return null;
    }
    return parent;
  }

  public static boolean isIconified(final Component parent) {
    if (parent != null) {
      if (parent instanceof Frame) {
        if (((Frame) parent).getExtendedState() != Frame.ICONIFIED) {
          if (((Frame) parent).getExtendedState() != (Frame.ICONIFIED | Frame.MAXIMIZED_BOTH)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  public static void showError(final Component parent, final String msg) {
    JOptionPane.showMessageDialog(
        validateParent(parent), msg, Constants.DEFAULT_WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
  }

  public static void showInformation(final Component parent, final String msg) {
    JOptionPane.showMessageDialog(
        validateParent(parent),
        msg,
        Constants.DEFAULT_WINDOW_TITLE,
        JOptionPane.INFORMATION_MESSAGE);
  }

  public static void showWarning(final Component parent, final String msg) {
    JOptionPane.showMessageDialog(
        validateParent(parent), msg, Constants.DEFAULT_WINDOW_TITLE, JOptionPane.WARNING_MESSAGE);
  }

  public static int showYesNoCancelQuestion(final Component parent, final String msg) {
    return JOptionPane.showConfirmDialog(
        validateParent(parent),
        msg,
        Constants.DEFAULT_WINDOW_TITLE,
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE);
  }

  public static int showYesNoError(final Component parent, final String msg) {
    return JOptionPane.showConfirmDialog(
        validateParent(parent),
        msg,
        Constants.DEFAULT_WINDOW_TITLE,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.ERROR_MESSAGE);
  }

  public static int showYesNoQuestion(final Component parent, final String msg) {
    return JOptionPane.showConfirmDialog(
        validateParent(parent),
        msg,
        Constants.DEFAULT_WINDOW_TITLE,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
  }

  public static int showYesNoWarning(final Component parent, final String msg) {
    return JOptionPane.showConfirmDialog(
        validateParent(parent),
        msg,
        Constants.DEFAULT_WINDOW_TITLE,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
  }
}
