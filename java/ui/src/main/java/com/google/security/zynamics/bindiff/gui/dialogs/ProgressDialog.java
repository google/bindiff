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

package com.google.security.zynamics.bindiff.gui.dialogs;

import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessProgressDialog;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CStandardHelperThread;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CStandardProgressDialog;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

public class ProgressDialog {
  /** You are not supposed to instantiate this class. */
  private ProgressDialog() {}

  private static void initializeWindowIcons(final JDialog dlg) {
    final List<Image> imageList = new ArrayList<>();
    imageList.add(ImageUtils.getImageIcon(Constants.APP_ICON_PATH_16X16).getImage());
    imageList.add(ImageUtils.getImageIcon(Constants.APP_ICON_PATH_32X32).getImage());
    imageList.add(ImageUtils.getImageIcon(Constants.APP_ICON_PATH_48X48).getImage());

    dlg.setIconImages(imageList);
  }

  /**
   * Shows a new progress dialog.
   *
   * @param parent Parent window of the progress dialog. This argument can be null.
   * @param description Description shown in the progress dialog.
   * @param thread Background worker thread executed while the progress dialog is visible.
   * @return The progress dialog itself.
   */
  // TODO(cblichmann): Consider using the CEndlessProgressDialog directly
  // instead of providing this method
  public static CEndlessProgressDialog show(
      final Window parent, final String description, final CEndlessHelperThread thread)
      throws Exception {
    final CEndlessProgressDialog dlg =
        new CEndlessProgressDialog(parent, Constants.DEFAULT_WINDOW_TITLE, description, thread);

    initializeWindowIcons(dlg);

    dlg.setSize(400, dlg.getPreferredSize().height);
    dlg.setMinimumSize(new Dimension(400, dlg.getPreferredSize().height));
    dlg.setMaximumSize(
        new Dimension(Math.max(400, dlg.getPreferredSize().width), dlg.getPreferredSize().height));
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    thread.start();

    dlg.setVisible(true);

    // TODO(cblichmann): Remove this and fix all call sites to work with
    // the getException() method directly.
    if (thread.getException() != null) {
      throw thread.getException();
    }

    return dlg;
  }

  /**
   * Shows a new standard dialog.
   *
   * @param parent Parent window of the progress dialog. This argument can be null.
   * @param description Description shown in the progress dialog.
   * @param thread Background worker thread executed while the progress dialog is visible.
   * @return The progress dialog itself.
   */
  public static CStandardProgressDialog show(
      final Window parent, final String description, final CStandardHelperThread thread) {
    final CStandardProgressDialog dlg =
        new CStandardProgressDialog(parent, Constants.DEFAULT_WINDOW_TITLE, description, thread);

    initializeWindowIcons(dlg);

    dlg.setSize(400, 122);
    dlg.setMinimumSize(new Dimension(400, 122));

    thread.start();

    dlg.setVisible(true);

    return dlg;
  }
}
