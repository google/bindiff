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

import javax.xml.xpath.XPathException;
import org.w3c.dom.Document;

/** Debug related settings like whether to display the super-graph, etc. */
public class DebugConfigItem extends ConfigItem {

  private static final String SHOW_MENU = "/bindiff/preferences/debug/@show-menu";
  private static final boolean SHOW_MENU_DEFAULT = false;
  private boolean showMenu = SHOW_MENU_DEFAULT;

  private static final String SHOW_SUPER_GRAPH = "/bindiff/preferences/debug/@show-super-graph";
  private static final boolean SHOW_SUPER_GRAPH_DEFAULT = false;
  private boolean showSuperGraph = SHOW_SUPER_GRAPH_DEFAULT;

  @Override
  public void load(final Document doc) throws XPathException {
    showMenu = getBoolean(doc, SHOW_MENU, SHOW_MENU_DEFAULT);
    showSuperGraph = getBoolean(doc, SHOW_SUPER_GRAPH, SHOW_SUPER_GRAPH_DEFAULT);
  }

  @Override
  public void store(final Document doc) throws XPathException {
    // Do not store debug settings. Instead, these can be selected in the debug menu.
  }

  public boolean getShowMenu() {
    return showMenu;
  }

  public void setShowMenu(final boolean showMenu) {
    this.showMenu = showMenu;
  }

  public boolean getShowSuperGraph() {
    return showSuperGraph;
  }

  public void setShowSuperGraph(boolean showSuperGraph) {
    this.showSuperGraph = showSuperGraph;
  }
}
