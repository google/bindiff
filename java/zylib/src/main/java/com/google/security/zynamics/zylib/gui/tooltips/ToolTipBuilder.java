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

package com.google.security.zynamics.zylib.gui.tooltips;

/**
 * This class can be used to build HTML tooltips which can be shown on random Swing objects.
 */
public class ToolTipBuilder {
  private final StringBuilder toolTip = new StringBuilder();

  public ToolTipBuilder() {
    toolTip.append("<html><table border=\"1\" cellpadding=\"1\" cellspacing=\"0f\">");
  }

  public ToolTipBuilder(final int borderWidth) {
    toolTip.append("<html><table border=\"" + borderWidth
        + "\" cellpadding=\"1\" cellspacing=\"0f\">");
  }

  /**
   * Adds a cell to the current row in the tooltip table.
   * 
   * Note: Please make sure to escape the string before adding it. Otherwise the HTML code might get
   * screwed up.
   * 
   * @param cellContent
   */
  public void addCell(final String cellContent) {
    toolTip.append("<td>");
    toolTip.append(cellContent);
    toolTip.append("</td>");
  }

  public void beginNewTable(final int borderWidth) {
    toolTip.append("</table><table border=\"" + borderWidth
        + "\" cellpadding=\"1\" cellspacing=\"0f\">");
  }

  public void beginRow() {
    toolTip.append("<tr>");
  }

  public void endRow() {
    toolTip.append("</tr>");
  }

  public String finish() {
    toolTip.append("</table></html>");

    return toolTip.toString();
  }
}
