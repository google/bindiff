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

package com.google.security.zynamics.zylib.gui.CodeDisplay;

import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Color;
import java.awt.Font;

/**
 * An intermediate class (in the class hierarchy sense) that provides some convenience when
 * implementing instances of the code display.
 */
public abstract class BasicCodeDisplayModel implements ICodeDisplayModel {
  public static final Font HEADER_FONT_BOLD =
      GuiHelper.getMonospacedFont().deriveFont(java.awt.Font.BOLD);
  public static final Font STANDARD_FONT = GuiHelper.getMonospacedFont();
  /** A static class to keep information about a column in one place. */
  public static class JCodeDisplayColumnDescription {
    final String name;
    final int width;
    final Color defaultFontColor;
    final Color defaultBackgroundColor;
    final Font defaultHeaderFont;
    final FormattedCharacterBuffer headerLine;

    public JCodeDisplayColumnDescription(
        String columnName,
        int columnWidth,
        Color fontColor,
        Color backgroundColor,
        Font headerFont) {
      name = columnName;
      width = columnWidth;
      defaultFontColor = fontColor;
      defaultBackgroundColor = backgroundColor;
      defaultHeaderFont = headerFont;
      headerLine =
          new FormattedCharacterBuffer(
              CodeDisplay.padRight(name, width),
              defaultHeaderFont,
              defaultFontColor,
              defaultBackgroundColor);
    }

    public String getName() {
      return name;
    }

    public int getWidth() {
      return width;
    }

    public Color getDefaultFontColor() {
      return defaultFontColor;
    }

    public Color getDefaultBackgroundColor() {
      return defaultBackgroundColor;
    }

    public FormattedCharacterBuffer getHeader() {
      return headerLine;
    }
  }
}
