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

package com.google.security.zynamics.zylib.gui.license;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessProgressDialog;
import com.google.security.zynamics.zylib.io.StreamUtils;
import java.awt.Window;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Helper class that provides a common starting point for update checks in zynamics products. */
public class UpdateCheckHelper {
  /** Base URL prefix for the update check mechanism. Make sure it does not end with a slash. */
  public static final String UPDATE_CHECK_BASE_URL = "https://www.zynamics.com/updates";

  private UpdateCheckHelper() {
    // Static methods only
  }

  /**
   * Checks for available product updates in the "stable" channel and displays the results in a user
   * interface.
   *
   * @param parent a {@link Window} to be used as a parent for modal dialogs. Can be {@code null}.
   * @param productName the name of the product to check for updates. Should not include a leading
   *     "zynamics ".
   * @param currentVersion the version string of the product that denotes the current version (i.e.
   *     "4.0.0").
   */
  public static void checkForUpdatesWithUi(
      final Window parent, final String productName, final String currentVersion) {
    checkForUpdatesWithUi(parent, productName, currentVersion, "stable");
  }

  /**
   * Checks for available product updates and displays the results in a user interface.
   *
   * @param parent a {@link Window} to be used as a parent for modal dialogs. Can be {@code null}.
   * @param productName the name of the product to check for updates. Should not include a leading
   *     "zynamics ".
   * @param currentVersion the version string of the product that denotes the current version (i.e.
   *     "4.0.0").
   * @param currentChannel the release channel to check for updates. This can be used to distinguish
   *     beta versions ("testing") from release versions ("stable").
   */
  public static void checkForUpdatesWithUi(
      final Window parent,
      final String productName,
      final String currentVersion,
      final String currentChannel) {
    final URL checkUrl;
    try {
      checkUrl =
          new URL(
              String.format(
                  "%s/%s/%s",
                  UPDATE_CHECK_BASE_URL, productName.toLowerCase(), currentChannel.toLowerCase()));
    } catch (final MalformedURLException e) {
      // Should never happen
      throw new RuntimeException("Malformed URL template", e);
    }

    final List<String> lines = new ArrayList<>();
    final CEndlessHelperThread helper =
        new CEndlessHelperThread() {
          @Override
          protected void runExpensiveCommand() throws Exception {
            final long startTime = new Date().getTime();

            // Read the actual version info
            lines.addAll(
                StreamUtils.readLinesFromReader(
                    new InputStreamReader(checkUrl.openStream(), UTF_8)));

            // Make this dialog visible for at least 400 msec so that users have a chance to read
            // the "Checking for updates..." text.
            final long duration = new Date().getTime() - startTime;
            if (duration < 400) {
              Thread.sleep(400 - duration);
            }
          }
        };
    CEndlessProgressDialog.show(
        parent,
        System.getProperty(CMessageBox.DEFAULT_WINDOW_TITLE_PROPERTY),
        "Checking for updates...",
        helper);
    final Exception e = helper.getException();
    if (e instanceof FileNotFoundException) {
      CMessageBox.showWarning(
          parent, "Could not check for updates. The update site is unavailable.");
      return;
    }
    if (e != null) {
      CMessageBox.showWarning(parent, "Could not check for updates: " + e.getMessage());
      return;
    }

    if (lines.isEmpty()) {
      CMessageBox.showWarning(
          parent, "Could not check for updates. The update site returned no data.");
      return;
    }

    // Make sure we have at least 2 lines and the second one is a date of the form YYYY-MM-DD (to be
    // used later)
    if ((lines.size() < 2) || !lines.get(1).matches("\\d{4}-\\d\\d-\\d\\d")) {
      CMessageBox.showWarning(parent, "Could not check for updates. Could not parse the response.");
      return;
    }

    final String remoteVersion = lines.get(0);
    final int result = versionCompare(currentVersion, remoteVersion);
    if (result < 0) {
      CMessageBox.showInformation(
          parent, String.format("A newer version (%s) is available.", remoteVersion));
      return;
    }
    CMessageBox.showInformation(
        parent, String.format("Your version of zynamics %s is up to date.", productName));
  }

  /**
   * Compares two version strings component-wise. Use to compare componentized version strings like
   * "3.2.2 beta 1" and "10.1". Both strings are split using the regular expression "\\.|-|\\s+".
   *
   * @param current the source version string
   * @param remote the target version string to compare against
   * @return -1, 0 or 1 as the specified current version string is less than, equal to or greater
   *     than the specified target version string
   */
  private static int versionCompare(final String current, final String remote) {
    final String[] curComp = current.trim().split("\\.|-|\\s+");
    final String[] remComp = remote.trim().split("\\.|-|\\s+");
    int result = 0;
    for (int i = 0; (result == 0) && (i < Math.min(curComp.length, remComp.length)); i++) {
      try {
        // Try numerical comparison first
        result = Integer.parseInt(curComp[i]) - Integer.parseInt(remComp[i]);
      } catch (final NumberFormatException e) {
        // Fall back to lexical comparison (for version components like "beta")
        result = curComp[i].compareToIgnoreCase(remComp[i]);
      }
    }

    // Clamp result to -1, 0 and 1 respectively
    return result < -1 ? -1 : (result > 1 ? 1 : result);
  }
}
