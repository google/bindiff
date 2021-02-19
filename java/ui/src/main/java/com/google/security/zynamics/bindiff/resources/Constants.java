// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.resources;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.zylib.system.SystemHelpers;

/** Application-global constants. */
public final class Constants {
  // Application Generals
  public static final String PRODUCT_NAME = "BinDiff";
  public static final String PRODUCT_VERSION = "7";
  public static final String PRODUCT_NAME_VERSION = "BinDiff " + PRODUCT_VERSION;
  public static final String DEFAULT_WINDOW_TITLE = "BinDiff";
  public static final String LOG_FILE_NAME = "bindiff_ui.log";
  public static final String LOG_DIRECTORYNAME = "logs";

  // AppIcon Paths
  public static final String APP_ICON_PATH_16X16 = "data/appicons/bindiff-16x16-rgba.png";
  public static final String APP_ICON_PATH_32X32 = "data/appicons/bindiff-32x32-rgba.png";
  public static final String APP_ICON_PATH_48X48 = "data/appicons/bindiff-48x48-rgba.png";

  // Image Paths
  public static final String DEFAULT_BACKGROUND_IMAGE_PATH =
      "data/appimages/bindiff-background-image.png";
  public static final String ABOUT_BINDIFF_IMAGE_PATH = "data/appimages/bindiff-about-image.png";

  // About, License Help and Support Dialog
  public static final String MANUAL_URL = "https://www.zynamics.com/bindiff/manual/index.html";
  public static final String COPYRIGHT_TEXT =
      "\nCopyright \u00a92004-2011 zynamics GmbH\nCopyright \u00a92011-2021 Google LLC.\n";
  public static final String ZYNAMICS_HOME_URL = "https://www.zynamics.com";
  public static final String ZYNAMICS_BINDIFF_PRODUCT_SITE_URL =
      "http://www.zynamics.com/bindiff.html";
  public static final String ZYNAMICS_SUPPORT_MAIL_URL = "mailto:zynamics-support@google.com";
  public static final String BUG_REPORT_URL = "https://bugs.zynamics.com/BinDiff";

  // File Extensions
  public static final String BINDIFF_WORKSPACEFILE_EXTENSION = "BinDiffWorkspace";
  public static final String BINDIFF_MATCHES_DB_EXTENSION = "BinDiff";
  public static final String BINDIFF_BINEXPORT_EXTENSION = "BinExport";

  public static final String IDB32_EXTENSION = "idb";
  public static final String IDB64_EXTENSION = "i64";

  // BinDiff Engine Executable Name
  public static final String BINDIFF_ENGINE_EXECUTABLE =
      SystemHelpers.isRunningWindows() ? "bindiff.exe" : "bindiff";

  // File format Versions
  public static final int WORKSPACE_DATABASE_FORMAT_VERSION = 2;

  // Config File
  public static final String CONFIG_FILENAME = "bindiff.json";
  public static final int CONFIG_FILEVERSION = 7;

  private static final int SOCKET_SERVER_PORT = 2000;

  // Functions
  public static int getSocketPort() {
    final int port = BinDiffConfig.getInstance().getMainSettings().getSocketPort();
    if (port <= 0 || port > 65535) {
      return SOCKET_SERVER_PORT;
    }
    return port;
  }
}
