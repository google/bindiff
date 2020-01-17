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
