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

package com.google.security.zynamics.zylib.gui;

import com.google.security.zynamics.zylib.general.Pair;
import java.awt.Component;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

/**
 * Class that can be used to show simple message windows to the user, implemented as a simple
 * wrapper around <code>JOptionPane</code>. <br>
 * By default, this class uses a system property with the key <code>
 * com.google.security.zynamics.zylib.gui.CMessageBox.DEFAULT_WINDOW_TITLE</code> as the title of
 * the message dialog box.
 */
public final class CMessageBox {
  public static final String DEFAULT_WINDOW_TITLE_PROPERTY =
      CMessageBox.class.getCanonicalName() + ".DEFAULT_WINDOW_TITLE";

  /** You are not supposed to instantiate this class. */
  private CMessageBox() {}

  /**
   * Checks whether a <code>Component</code> is iconified. Always returns <code>false</code> on
   * <code>Component</code>s that are not descendants of <code>Frame</code>.
   *
   * @param c the <code>Component</code> to check
   * @return <code>true</code> if <code>c</code> is non-null and a descendant of <code>Frame</code>
   *     and <code>c</code> is currently iconified, <code>false</code> otherwise.
   */
  private static boolean isIconified(final Component c) {
    if ((c != null) && (c instanceof Frame)) {
      return (((Frame) c).getExtendedState() & Frame.ICONIFIED) != 0;
    }
    return false;
  }

  public static int showCustomQuestionMessageBox(
      final Component parent,
      final String description,
      final String title,
      final JButton[] buttons,
      final JButton defaultButton) {
    return JOptionPane.showOptionDialog(
        parent,
        description,
        title,
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        buttons,
        defaultButton);
  }

  /**
   * Shows an error message.
   *
   * @param parent Parent window of the message box.
   * @param msg Message shown in the message box.
   */
  public static void showError(final Component parent, final String msg) {
    JOptionPane.showMessageDialog(
        isIconified(parent) ? null : parent,
        msg,
        System.getProperty(DEFAULT_WINDOW_TITLE_PROPERTY),
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Shows an informational message.
   *
   * @param parent Parent window of the message box.
   * @param msg Message shown in the message box.
   */
  public static void showInformation(final Component parent, final String msg) {
    JOptionPane.showMessageDialog(
        isIconified(parent) ? null : parent,
        msg,
        System.getProperty(DEFAULT_WINDOW_TITLE_PROPERTY),
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Shows a warning message.
   *
   * @param parent Parent window of the message box.
   * @param msg Message shown in the message box.
   */
  public static void showWarning(final Component parent, final String msg) {
    JOptionPane.showMessageDialog(
        isIconified(parent) ? null : parent,
        msg,
        System.getProperty(DEFAULT_WINDOW_TITLE_PROPERTY),
        JOptionPane.WARNING_MESSAGE);
  }

  /**
   * Shows a question the user can answer with Yes/No/Cancel.
   *
   * @param parent Parent window of the message box.
   * @param msg Message shown in the message box.
   * @return An integer indicating the option selected by the user.
   */
  public static int showYesNoCancelQuestion(final Component parent, final String msg) {
    return JOptionPane.showConfirmDialog(
        isIconified(parent) ? null : parent,
        msg,
        System.getProperty(DEFAULT_WINDOW_TITLE_PROPERTY),
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE);
  }

  /**
   * Shows an error message with a question the user can answer with Yes/No.
   *
   * @param parent Parent window of the message box.
   * @param msg Message shown in the message box.
   * @return An integer indicating the option selected by the user.
   */
  public static int showYesNoError(final Component parent, final String msg) {
    return JOptionPane.showConfirmDialog(
        isIconified(parent) ? null : parent,
        msg,
        System.getProperty(DEFAULT_WINDOW_TITLE_PROPERTY),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Shows a question the user can answer with Yes/No.
   *
   * @param parent Parent window of the message box.
   * @param msg Message shown in the message box.
   * @return An integer indicating the option selected by the user.
   */
  public static int showYesNoQuestion(final Component parent, final String msg) {
    return showYesNoQuestionWithCheckbox(parent, msg, null).first();
  }

  /**
   * Shows a question the user can answer with Yes/No with an additional checkbox below.
   *
   * @param parent Parent window of the message box.
   * @param msg Message shown in the message box.
   * @param checkBoxTitle Title shown in the checkbox. If <code>null</code>, no checkbox is shown.
   * @return A <code>Pair</code>, where the first field indicates the button selected by the user
   *     and the second field indicates the selection state of the checkbox.
   */
  public static Pair<Integer, Boolean> showYesNoQuestionWithCheckbox(
      final Component parent, final String msg, final String checkBoxTitle) {
    JCheckBox checkbox = null;
    Object params;
    if (checkBoxTitle != null) {
      checkbox = new JCheckBox(checkBoxTitle);
      params = new Object[] {msg, checkbox};
    } else {
      params = msg;
    }

    final int option =
        JOptionPane.showConfirmDialog(
            isIconified(parent) ? null : parent,
            params,
            System.getProperty(DEFAULT_WINDOW_TITLE_PROPERTY),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
    return Pair.make(option, checkBoxTitle != null ? checkbox.isSelected() : false);
  }

  /**
   * Shows an warning message with a question the user can answer with Yes/No.
   *
   * @param parent Parent window of the message box.
   * @param msg Message shown in the message box.
   * @return An integer indicating the option selected by the user.
   */
  public static int showYesNoWarning(final Component parent, final String msg) {
    return JOptionPane.showConfirmDialog(
        isIconified(parent) ? null : parent,
        msg,
        System.getProperty(DEFAULT_WINDOW_TITLE_PROPERTY),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
  }
}
