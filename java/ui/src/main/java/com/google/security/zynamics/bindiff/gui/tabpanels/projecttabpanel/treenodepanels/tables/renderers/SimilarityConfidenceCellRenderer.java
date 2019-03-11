package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.BackgroundIcon;
import com.google.security.zynamics.bindiff.resources.Colors;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class SimilarityConfidenceCellRenderer extends AbstractTableCellRenderer {

  /**
   * Calculates a match color based on the value. Uses the same color palette as the C++ plugin
   * code.
   */
  public static Color calcColor(final double value) {
    // See //third_party/zynamics/bindiff/ida/ui.cc.
    final Color[] colorRamp =
        new Color[] {
          new Color(0xff5722), new Color(0xff5722), new Color(0xff5922), new Color(0xff5922),
              new Color(0xff5a22), new Color(0xff5b21), new Color(0xff5b21),
          new Color(0xff5c21), new Color(0xff5d21), new Color(0xff5e21), new Color(0xff5f21),
              new Color(0xff5f21), new Color(0xff5f21), new Color(0xff6120),
          new Color(0xff6120), new Color(0xff6220), new Color(0xff6220), new Color(0xff6320),
              new Color(0xff6420), new Color(0xff6520), new Color(0xff661f),
          new Color(0xff671f), new Color(0xff661f), new Color(0xff671f), new Color(0xff681f),
              new Color(0xff691f), new Color(0xff691f), new Color(0xff6a1e),
          new Color(0xff6b1e), new Color(0xff6c1e), new Color(0xfe6c1e), new Color(0xfe6d1e),
              new Color(0xfe6f1e), new Color(0xfe6f1e), new Color(0xfe701d),
          new Color(0xfe711d), new Color(0xfe701d), new Color(0xfe721d), new Color(0xfe721d),
              new Color(0xfe731d), new Color(0xfe731d), new Color(0xfe741d),
          new Color(0xfd751c), new Color(0xfe751c), new Color(0xfd761c), new Color(0xfd771c),
              new Color(0xfd771c), new Color(0xfd791b), new Color(0xfd791b),
          new Color(0xfd7a1b), new Color(0xfd7a1b), new Color(0xfd7a1b), new Color(0xfc7c1b),
              new Color(0xfc7d1b), new Color(0xfc7d1a), new Color(0xfc7e1a),
          new Color(0xfc7f1a), new Color(0xfc7f1a), new Color(0xfc7f1a), new Color(0xfb811a),
              new Color(0xfb8119), new Color(0xfb8119), new Color(0xfb8319),
          new Color(0xfb8219), new Color(0xfb8419), new Color(0xfb8419), new Color(0xfa8518),
              new Color(0xfa8618), new Color(0xfa8718), new Color(0xfa8718),
          new Color(0xfa8818), new Color(0xfa8718), new Color(0xf98a17), new Color(0xf98917),
              new Color(0xf98b17), new Color(0xf98a17), new Color(0xf98c17),
          new Color(0xf88d17), new Color(0xf88c17), new Color(0xf88d16), new Color(0xf88e16),
              new Color(0xf78f16), new Color(0xf79016), new Color(0xf79015),
          new Color(0xf79115), new Color(0xf79215), new Color(0xf69215), new Color(0xf69215),
              new Color(0xf69315), new Color(0xf69415), new Color(0xf59514),
          new Color(0xf59514), new Color(0xf59614), new Color(0xf49714), new Color(0xf49714),
              new Color(0xf49813), new Color(0xf49813), new Color(0xf49913),
          new Color(0xf39a13), new Color(0xf39a13), new Color(0xf39b13), new Color(0xf29c12),
              new Color(0xf29d12), new Color(0xf29d12), new Color(0xf19e12),
          new Color(0xf19e11), new Color(0xf19f11), new Color(0xf0a011), new Color(0xf1a011),
              new Color(0xf0a011), new Color(0xf0a111), new Color(0xefa210),
          new Color(0xefa210), new Color(0xefa410), new Color(0xeea410), new Color(0xeea510),
              new Color(0xeea50f), new Color(0xeda60f), new Color(0xeda70f),
          new Color(0xeda80f), new Color(0xeca90e), new Color(0xeca80e), new Color(0xeca90e),
              new Color(0xeba90e), new Color(0xebaa0e), new Color(0xebab0e),
          new Color(0xeaac0d), new Color(0xeaac0d), new Color(0xe9ad0d), new Color(0xe9ad0d),
              new Color(0xe8ae0c), new Color(0xe8af0c), new Color(0xe8af0c),
          new Color(0xe7b00b), new Color(0xe7b10b), new Color(0xe6b20b), new Color(0xe6b20b),
              new Color(0xe6b20b), new Color(0xe5b30a), new Color(0xe5b30a),
          new Color(0xe4b50a), new Color(0xe4b50a), new Color(0xe3b609), new Color(0xe3b709),
              new Color(0xe3b709), new Color(0xe2b709), new Color(0xe1b809),
          new Color(0xe1b908), new Color(0xe1ba08), new Color(0xe1b908), new Color(0xdfbb08),
              new Color(0xdfbb08), new Color(0xdebc07), new Color(0xdebc07),
          new Color(0xdebe07), new Color(0xdebd07), new Color(0xddbe07), new Color(0xddbe07),
              new Color(0xdbc006), new Color(0xdbc006), new Color(0xdac206),
          new Color(0xdac106), new Color(0xdac206), new Color(0xd9c205), new Color(0xd8c405),
              new Color(0xd8c405), new Color(0xd7c405), new Color(0xd7c504),
          new Color(0xd7c504), new Color(0xd6c604), new Color(0xd5c804), new Color(0xd5c704),
              new Color(0xd4c904), new Color(0xd3c903), new Color(0xd3ca03),
          new Color(0xd2cb03), new Color(0xd2ca03), new Color(0xd1cc03), new Color(0xd0cd03),
              new Color(0xd0cc03), new Color(0xd0cc03), new Color(0xcfcd02),
          new Color(0xcece02), new Color(0xcdcf02), new Color(0xcbd002), new Color(0xcbd102),
              new Color(0xcbd002), new Color(0xcad202), new Color(0xcad102),
          new Color(0xc9d301), new Color(0xc8d401), new Color(0xc7d401), new Color(0xc7d501),
              new Color(0xc5d601), new Color(0xc5d601), new Color(0xc4d701),
          new Color(0xc3d800), new Color(0xc3d700), new Color(0xc2d800), new Color(0xc2d800),
              new Color(0xc0da00), new Color(0xbfda00), new Color(0xbfda00),
          new Color(0xbedc00), new Color(0xbedb00), new Color(0xbcdd00), new Color(0xbbde00),
              new Color(0xbbdd00), new Color(0xbade00), new Color(0xbade00),
          new Color(0xb8e000), new Color(0xb7e100), new Color(0xb7e100), new Color(0xb5e100),
              new Color(0xb5e100), new Color(0xb4e300), new Color(0xb4e200),
          new Color(0xb2e400), new Color(0xb1e400), new Color(0xb1e400), new Color(0xafe600),
              new Color(0xaee600), new Color(0xade600), new Color(0xace800),
          new Color(0xace700), new Color(0xaae900), new Color(0xaae800), new Color(0xa8e900),
              new Color(0xa6eb00), new Color(0xa6ea00), new Color(0xa4eb00),
          new Color(0xa4ec00), new Color(0xa3ed00), new Color(0xa1ee00), new Color(0xa1ee00),
              new Color(0xa1ed00), new Color(0x9fee00), new Color(0x9def00),
          new Color(0x9def00), new Color(0x9bf000), new Color(0x98f200), new Color(0x98f200),
              new Color(0x96f300), new Color(0x96f300), new Color(0x94f301),
          new Color(0x94f401), new Color(0x92f401), new Color(0x8ff601), new Color(0x8ff601),
              new Color(0x8df601), new Color(0x8cf701), new Color(0x8bf701),
          new Color(0x88f802), new Color(0x88f902), new Color(0x85fa02), new Color(0x84fa02)
        };
    if (value < 0 || value > 1) {
      return Colors.GRAY192;
    }
    return colorRamp[(int) (value * (colorRamp.length - 1))];
  }

  @Override
  public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean selected,
      final boolean focused,
      final int row,
      final int column) {
    buildAndSetToolTip(table, row);

    setFont(!isBoldFont(table, row) ? NORMAL_FONT : BOLD_FONT);

    if (value instanceof Double) {
      final double d = (Double) value;

      setIcon(
          new BackgroundIcon(
              d == -1 ? NON_ACCESSIBLE_TEXT : String.format("%.2f", d),
              SwingConstants.CENTER,
              Colors.GRAY32,
              d == -1 ? NON_ACCESSIBLE_COLOR : calcColor((Double) value),
              table.getSelectionBackground(),
              selected,
              0 - 1,
              0,
              table.getColumnModel().getColumn(column).getWidth() - 1,
              table.getRowHeight() - 1));
    }

    return this;
  }
}
