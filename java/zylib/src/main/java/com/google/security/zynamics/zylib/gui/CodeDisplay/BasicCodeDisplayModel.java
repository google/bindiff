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
  /**
   *  A static class to keep information about a column in one place.
   */
  public static class JCodeDisplayColumnDescription {
    final String name;
    final int width;
    final Color defaultFontColor;
    final Color defaultBackgroundColor;
    final Font defaultHeaderFont;
    final FormattedCharacterBuffer headerLine;

    public JCodeDisplayColumnDescription(String columnName, int columnWidth, Color fontColor,
        Color backgroundColor, Font headerFont) {
      name = columnName;
      width = columnWidth;
      defaultFontColor = fontColor;
      defaultBackgroundColor = backgroundColor;
      defaultHeaderFont = headerFont;
      headerLine = new FormattedCharacterBuffer(CodeDisplay.padRight(name, width),
          defaultHeaderFont, defaultFontColor, defaultBackgroundColor);
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
