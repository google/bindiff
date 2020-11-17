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

package com.google.security.zynamics.bindiff.config;

/** Debug related settings like whether to display the super-graph, etc. */
public class DebugConfigItem {

  private Boolean showMenu;
  private Boolean showSuperGraph;

  public boolean getShowMenu() {
    if (showMenu == null) {
      showMenu = Config.getInstance().getPreferences().getDebug().getShowDebugMenu();
    }
    return showMenu;
  }

  public void setShowMenu(final boolean showMenu) {
    // Do not store debug settings in proto.
    this.showMenu = showMenu;
  }

  public boolean getShowSuperGraph() {
    if (showSuperGraph == null) {
      showSuperGraph = Config.getInstance().getPreferences().getDebug().getShowSuperGraph();
    }
    return showSuperGraph;
  }

  public void setShowSuperGraph(boolean showSuperGraph) {
    // Do not store debug settings in proto.
    this.showSuperGraph = showSuperGraph;
  }
}
