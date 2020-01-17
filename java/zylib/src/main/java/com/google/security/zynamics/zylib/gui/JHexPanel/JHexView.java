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

package com.google.security.zynamics.zylib.gui.JHexPanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.Convert;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.JCaret.ICaretListener;
import com.google.security.zynamics.zylib.gui.JCaret.JCaret;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * The JHexView component is a Java component that can be used to display data in hexadecimal
 * format.
 */
public final class JHexView extends JComponent {

  private static final long serialVersionUID = -2402458562501988128L;

  /** Two characters are needed to display a byte in the hex window. */
  private static final int CHARACTERS_PER_BYTE = 2;

  /** Lookup table to convert byte values into printable strings. */
  private static final String[] HEX_BYTES = {
    "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
    "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
    "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
    "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
    "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F",
    "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
    "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E", "6F",
    "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E", "7F",
    "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
    "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
    "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
    "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
    "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF",
    "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF",
    "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
    "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF",
  };

  private static final int PADDING_OFFSETVIEW = 20;

  private static final int NIBBLES_PER_BYTE = 2;

  /** List of listeners that are notified if something happens in the hex panel component. */
  private final ArrayList<IHexPanelListener> listeners = new ArrayList<IHexPanelListener>();

  /** The data set that is displayed in the component. */
  private IDataProvider dataProvider;

  /** Number of bytes shown per row. */
  private int bytesPerRow = 16;

  /** Font used to draw the data. */
  private Font font = GuiHelper.getMonospacedFont();

  /**
   * Currently selected position. Note that this field is twice as large as the length of data
   * because nibbles can be selected.
   */
  private long selectionStart = 0;

  /**
   * Current selection length in nibbles. This value can be negative if nibbles before the current
   * position are selected.
   */
  private long m_selectionLength = 0;

  /** Determines the window where the caret is shown. */
  private Views activeView = Views.HEX_VIEW;

  /** Width of the hex view in pixels. */
  private int hexViewWidth = 270;

  /** Width of the space between columns in pixels. */
  private int columnSpacing = 4;

  /** Number of bytes per column. */
  private int bytesPerColumn = 2;

  /** Background color of the offset view. */
  private Color bgColorOffset = Color.GRAY;

  /** Background color of the hex view. */
  private Color bgColorHex = Color.WHITE;

  /** Background color of the ASCII view. */
  private Color bgColorAscii = Color.WHITE;

  /** Font color of the offset view. */
  private Color fontColorOffsets = Color.WHITE;

  /** Font color of the hex view. */
  private Color fontColorHex1 = Color.BLUE;

  /** Font color of the hex view. */
  private Color fontColorHex2 = new Color(0x3399FF);

  /** Font color of the ASCII view. */
  private Color fontColorAscii = new Color(0x339900);

  /**
   * Used to store the height of a single row. This value is updated every time the component is
   * drawn.
   */
  private int rowHeight = 12;

  /**
   * Used to store the width of a single character. This value is updated every time the component
   * is drawn.
   */
  private int charWidth = 8;

  /** Scrollbar that is used to scroll through the dataset. */
  private final JScrollBar scrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 1);

  /** Horizontal scrollbar that is used to scroll through the dataset. */
  private final JScrollBar horizontalScrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 1);

  /** The first visible row. */
  private int firstRow = 0;

  /** The first visible column. */
  private int firstColumn = 0;

  /** Address of the first offset in the data set. */
  private long baseAddress = 0;

  /** Last x-coordinate of the mouse cursor in the component. */
  private int lastMouseX = 0;

  /** Last y-coordinate of the mouse cursor in the component. */
  private int lastMouseY = 0;

  /** Flag that determines whether the component reacts to user input or not. */
  private boolean enabled = false;

  /** Color that is used to draw all text in disabled components. */
  private final Color disabledColor = Color.GRAY;

  /** Blinking caret of the component. */
  private final JCaret caret = new JCaret();

  /** Left-padding of the hex view in pixels. */
  private static final int paddingHexLeft = 10;

  /** Left-padding of the ASCII view in pixels. */
  private static final int paddingAsciiLeft = 10;

  /** Top-padding of all views in pixels. */
  private static final int paddingTop = 16;

  /** Height of a drawn character in the component. */
  private int charHeight = 8;

  /** Color that is used to highlight data when the mouse cursor hovers of the data. */
  private final Color colorHighlight = Color.LIGHT_GRAY;

  /** Start with an undefined definition status. */
  private DefinitionStatus status = DefinitionStatus.UNDEFINED;

  /**
   * The menu creator is used to create popup menus when the user right-clicks on the hex view
   * control.
   */
  private IMenuCreator menuCreator;

  /** Current addressing mode (32bit or 64bit) */
  private AddressMode addressMode = AddressMode.BIT32;

  /** Width of the offset view part of the component. */
  private int offsetViewWidth;

  /** Manager that keeps track of specially colored byte ranges. */
  private final ColoredRangeManager[] coloredRanges = new ColoredRangeManager[10];

  /** Used for double buffering the graphical output. */
  private Graphics bufferGraphics;

  /** Used for double buffering the graphical output. */
  private BufferedImage img;

  /** Timer that is used to refresh the component if no data for the selected range is available. */
  private Timer updateTimer;

  /** Flag that indicates whether the component is being drawn for the first time. */
  private boolean firstDraw = true;

  /** Default internal listener that is used to handle various events. */
  private final InternalListener listener = new InternalListener();

  /** Action that's executed when the user presses the left arrow key. */
  private final LeftAction leftAction = new LeftAction();

  /** Action that's executed when the user presses the right arrow key. */
  private final RightAction rightAction = new RightAction();

  /** Action that's executed when the user presses the up arrow key. */
  private final UpAction upAction = new UpAction();

  /** Action that's executed when the user presses the down arrow key. */
  private final DownAction downAction = new DownAction();

  /** Action that's executed when the user presses the page up key. */
  private final PageUpAction pageUpAction = new PageUpAction();

  /** Action that's executed when the user presses the page down key. */
  private final PageDownAction pageDownAction = new PageDownAction();

  /** Action that's executed when the user presses the tab key. */
  private final TabAction tabAction = new TabAction();

  private int lastHighlightedNibble;

  private IColormap colormap;

  private Color selectionColor = Color.YELLOW;

  /** Determines whether the bytes inside a column are flipped or not. */
  private boolean flipBytes = false;

  /** Creates a new hex viewer panel. */
  public JHexView() {
    for (int i = 0; i < coloredRanges.length; i++) {
      coloredRanges[i] = new ColoredRangeManager();
    }

    // Necessary to receive input
    setFocusable(true);

    setLayout(new BorderLayout());

    // Set the initial font
    setFont(font);

    initListeners();

    initHotkeys();

    initScrollbar();

    img =
        new BufferedImage(
            (getWidth() + 1) - scrollbar.getWidth(),
            (getHeight() + 1) - horizontalScrollbar.getHeight(),
            BufferedImage.TYPE_INT_RGB);
    bufferGraphics = img.getGraphics();

    updateOffsetViewWidth();

    // By default, this component is disabled.
    setEnabled(false);
  }

  /** Calculates current character and row sizes. */
  private void calculateSizes() {
    rowHeight = getRowHeight(bufferGraphics);
    charHeight = getCharHeight(bufferGraphics);
    charWidth = getCharacterWidth(bufferGraphics);
  }

  private void changeBy(final ActionEvent event, final int length) {
    if (event.getModifiers() == ActionEvent.SHIFT_MASK) {
      if ((getSelectionStart() + getSelectionLength() + length) < 0) {
        setSelectionLength(-getSelectionStart());
      } else {
        if ((getSelectionStart() + getSelectionLength() + length)
            < (2 * dataProvider.getDataLength())) {
          setSelectionLength(getSelectionLength() + length);
        } else {
          setSelectionLength((2 * dataProvider.getDataLength()) - getSelectionStart());
        }
      }
    } else {
      if ((getSelectionStart() + getSelectionLength() + length) < 0) {
        setSelectionStart(0);
      } else if ((getSelectionStart() + getSelectionLength() + length)
          < (2 * dataProvider.getDataLength())) {
        setSelectionStart(getSelectionStart() + getSelectionLength() + length);
      } else {
        setSelectionStart(2 * dataProvider.getDataLength());
      }

      setSelectionLength(0);
    }

    final long newPosition = getSelectionStart() + getSelectionLength();

    if (newPosition < (2 * getFirstVisibleByte())) {
      scrollToPosition(newPosition);
    } else if (newPosition >= (2 * (getFirstVisibleByte() + getMaximumVisibleBytes()))) {
      final long invisibleNibbles =
          newPosition - (2 * (getFirstVisibleByte() + getMaximumVisibleBytes()));

      final long scrollpos = (2 * getFirstVisibleByte()) + (2 * bytesPerRow) + invisibleNibbles;

      scrollToPosition(scrollpos);
    }

    caret.setVisible(true);
    repaint();
  }

  /**
   * Draws the content of the ASCII panel.
   *
   * @param g The graphics context of the hex panel.
   */
  private void drawAsciiPanel(final Graphics g) {
    if (isEnabled()) {
      // Choose the right color for the ASCII view
      g.setColor(fontColorAscii);
    } else {
      g.setColor(disabledColor != bgColorAscii ? disabledColor : Color.WHITE);
    }

    final int characterWidth = getCharacterWidth(g);

    final int initx = getAsciiViewLeft() + paddingAsciiLeft;

    int x = initx;
    int y = paddingTop;

    byte[] data = null;
    int bytesToDraw;

    if (status == DefinitionStatus.DEFINED) {
      bytesToDraw = getBytesToDraw();
      data = dataProvider.getData(getFirstVisibleOffset(), bytesToDraw);
    } else {
      bytesToDraw = getMaximumVisibleBytes();
    }

    long currentOffset = getFirstVisibleOffset();

    for (int i = 0; i < bytesToDraw; i++, currentOffset++) {
      ColoredRange range = findColoredRange(currentOffset);

      if ((range != null) && ((currentOffset + bytesToDraw) < range.getStart())) {
        range = null;
      }

      if ((i != 0) && ((i % bytesPerRow) == 0)) {
        // If the end of a row is reached, reset the
        // x-coordinate and increase the y-coordinate.
        x = initx;
        y += rowHeight;
      }

      if (status == DefinitionStatus.DEFINED) {
        char c = (char) data[i];

        c = Convert.isPrintableCharacter(c) ? c : '.';

        final String dataString = String.valueOf(c);

        if (isEnabled()) {
          final long normalizedOffset =
              flipBytes
                  ? ((currentOffset & -bytesPerColumn) + bytesPerColumn)
                      - (currentOffset % bytesPerColumn)
                      - 1
                  : currentOffset;

          if (isSelectedOffset(normalizedOffset)) {
            g.setColor(selectionColor);
            g.fillRect(x, y - charHeight, charWidth, charHeight + 2);
            g.setColor(fontColorAscii);
          } else if ((range != null) && range.containsOffset(currentOffset)) {
            final Color bgColor = range.getBackgroundColor();

            if (bgColor != null) {
              g.setColor(bgColor);
            } else {
              System.out.println("FOO");
            }

            g.fillRect(x, y - charHeight, charWidth, charHeight + 2);
            g.setColor(range.getColor());
          } else if ((colormap != null) && colormap.colorize(data, i)) {
            final Color backgroundColor = colormap.getBackgroundColor(data, i);
            final Color foregroundColor = colormap.getForegroundColor(data, i);

            if (backgroundColor != null) {
              g.setColor(backgroundColor);
              g.fillRect(x, y - charHeight, charWidth, charHeight + 2);
            }

            if (foregroundColor != null) {
              g.setColor(foregroundColor);
            }
          } else {
            g.setColor(fontColorAscii);
          }

        } else {
          g.setColor(disabledColor != bgColorAscii ? disabledColor : Color.WHITE);
        }

        g.drawString(dataString, x, y);
      } else {
        g.drawString("?", x, y);
      }

      x += characterWidth;

      if ((range != null) && ((range.getStart() + range.getSize()) <= currentOffset)) {
        range = findColoredRange(currentOffset);

        if ((range != null) && ((currentOffset + bytesToDraw) < range.getStart())) {
          range = null;
        }
      }
    }
  }

  /**
   * Draws the background of the hex panel.
   *
   * @param g The graphics context of the hex panel.
   */
  private void drawBackground(final Graphics g) {
    // Draw the background of the offset view
    g.setColor(bgColorOffset);
    g.fillRect(-firstColumn * charWidth, 0, offsetViewWidth, getHeight());

    // Draw the background of the hex view
    g.setColor(bgColorHex);
    g.fillRect((-firstColumn * charWidth) + offsetViewWidth, 0, hexViewWidth, getHeight());

    // Draw the background of the ASCII view
    g.setColor(bgColorAscii);
    g.fillRect(
        (-firstColumn * charWidth) + hexViewWidth + offsetViewWidth,
        0,
        ((firstColumn * charWidth) + getWidth())
            - (hexViewWidth + offsetViewWidth)
            - scrollbar.getWidth(),
        getHeight() - horizontalScrollbar.getHeight());

    // Draw the lines that separate the individual views
    g.setColor(Color.BLACK);
    g.drawLine(
        (-firstColumn * charWidth) + offsetViewWidth,
        0,
        (-firstColumn * charWidth) + offsetViewWidth,
        getHeight());
    g.drawLine(
        (-firstColumn * charWidth) + offsetViewWidth + hexViewWidth,
        0,
        (-firstColumn * charWidth) + offsetViewWidth + hexViewWidth,
        getHeight());
  }

  /**
   * Draws the caret.
   *
   * @param g
   */
  private void drawCaret(final Graphics g) {
    if (!isEnabled()) {
      return;
    }

    if ((getCurrentOffset() < getFirstVisibleByte())
        || (getCurrentColumn() > (getFirstVisibleByte() + getMaximumVisibleBytes()))) {
      return;
    }

    final int characterSize = getCharacterWidth(g);

    if (activeView == Views.HEX_VIEW) {
      drawCaretHexWindow(g, characterSize, rowHeight);
    } else {
      drawCaretAsciiWindow(g, characterSize, rowHeight);
    }
  }

  /**
   * Draws the caret in the ASCII window.
   *
   * @param g The graphic context of the hex panel.
   * @param characterWidth The width of a single character.
   * @param characterHeight The height of a single character.
   */
  private void drawCaretAsciiWindow(
      final Graphics g, final int characterWidth, final int characterHeight) {

    final int currentRow = getCurrentRow() - firstRow;
    final int currentColumn = getCurrentColumn();
    final int currentCharacter = currentColumn / 2;

    // Calculate the position of the first character in the row
    final int startLeft = 9 + offsetViewWidth + hexViewWidth;

    // Calculate the position of the current character in the row
    final int x = (-firstColumn * charWidth) + startLeft + (currentCharacter * characterWidth);

    // Calculate the position of the row
    final int y = ((3 + paddingTop) - characterHeight) + (characterHeight * currentRow);

    caret.draw(g, x, y, characterHeight);
  }

  /**
   * Draws the caret in the hex window.
   *
   * @param g The graphic context of the hex panel.
   * @param characterWidth The width of a single character.
   * @param characterHeight The height of a single character.
   */
  private void drawCaretHexWindow(
      final Graphics g, final int characterWidth, final int characterHeight) {
    final int currentRow = getCurrentRow() - firstRow;
    final int currentColumn = getCurrentColumn();

    // Calculate the position of the first character in the row.
    final int startLeft = 9 + offsetViewWidth;

    // Calculate the extra padding between columns.
    final int paddingColumns = (currentColumn / (2 * bytesPerColumn)) * columnSpacing;

    // Calculate the position of the character in the row.
    final int x =
        (-firstColumn * charWidth) + startLeft + (currentColumn * characterWidth) + paddingColumns;

    // Calculate the position of the row.
    final int y = ((3 + paddingTop) - characterHeight) + (characterHeight * currentRow);

    caret.draw(g, x, y, characterHeight);
  }

  /**
   * Draws the content of the hex view.
   *
   * @param g The graphics context of the hex panel.
   */
  private void drawHexView(final Graphics g) {
    final int standardSize = 2 * getCharacterWidth(g);

    final int firstX = (-firstColumn * charWidth) + paddingHexLeft + offsetViewWidth;

    int x = firstX;
    int y = paddingTop;

    boolean evenColumn = true;

    byte[] data = null;
    int bytesToDraw;

    if (status == DefinitionStatus.DEFINED) {
      bytesToDraw = getBytesToDraw();
      data = dataProvider.getData(getFirstVisibleOffset(), bytesToDraw);
    } else {
      bytesToDraw = getMaximumVisibleBytes();
    }

    long currentOffset = getFirstVisibleOffset();

    // Iterate over all bytes in the data set and
    // print their hex value to the hex view.
    for (int i = 0; i < bytesToDraw; i++, currentOffset++) {
      final ColoredRange range = findColoredRange(currentOffset);

      if (i != 0) {
        if ((i % bytesPerRow) == 0) {
          // If the end of a row was reached, reset the x-coordinate
          // and set the y-coordinate to the next row.

          x = firstX;
          y += rowHeight;

          evenColumn = true;
        } else if ((i % bytesPerColumn) == 0) {
          // Add some spacing after each column.
          x += columnSpacing;

          evenColumn = !evenColumn;
        }
      }

      if (isEnabled()) {
        if (isSelectedOffset(currentOffset)) {
          g.setColor(selectionColor);
          g.fillRect(x, y - charHeight, 2 * charWidth, charHeight + 2);

          // Choose the right color for the hex view
          g.setColor(evenColumn ? fontColorHex1 : fontColorHex2);
        } else if ((range != null) && range.containsOffset(currentOffset)) {
          final Color bgColor = range.getBackgroundColor();

          if (bgColor != null) {
            g.setColor(bgColor);
          }

          g.fillRect(x, y - charHeight, 2 * charWidth, charHeight + 2);
          if (range.getColor() != null) {
            g.setColor(range.getColor());
          } else {
            g.setColor(evenColumn ? fontColorHex1 : fontColorHex2);
          }
        } else {
          if ((colormap != null) && colormap.colorize(data, i)) {
            final Color backgroundColor = colormap.getBackgroundColor(data, i);
            final Color foregroundColor = colormap.getForegroundColor(data, i);

            if (backgroundColor != null) {
              g.setColor(backgroundColor);
              g.fillRect(x, y - charHeight, 2 * charWidth, charHeight + 2);
            }

            if (foregroundColor != null) {
              g.setColor(foregroundColor);
            }
          } else {
            // Choose the right color for the hex view
            g.setColor(evenColumn ? fontColorHex1 : fontColorHex2);
          }
        }
      } else {
        g.setColor(disabledColor != bgColorHex ? disabledColor : Color.WHITE);
      }

      if (status == DefinitionStatus.DEFINED) {
        // Number of bytes shown in the current column
        final int columnBytes = Math.min(dataProvider.getDataLength() - i, bytesPerColumn);

        final int dataPosition =
            flipBytes
                ? ((i / bytesPerColumn) * bytesPerColumn) + (columnBytes - (i % columnBytes) - 1)
                : i;

        // Print the data
        g.drawString(HEX_BYTES[data[dataPosition] & 0xFF], x, y);
      } else {
        g.drawString("??", x, y);
      }

      // Update the position of the x-coordinate
      x += standardSize;
    }
  }

  /**
   * Draws highlighting of bytes when the mouse hovers over them.
   *
   * @param g The graphics context where the highlighting is drawn.
   */
  private void drawMouseOverHighlighting(final Graphics g) {
    g.setColor(colorHighlight);

    lastHighlightedNibble = getNibbleAtCoordinate(lastMouseX, lastMouseY);

    if (lastHighlightedNibble == -1) {
      return;
    }

    // Find out in which view the mouse currently resides.
    final Views lastHighlightedView =
        lastMouseX >= getAsciiViewLeft() ? Views.ASCII_VIEW : Views.HEX_VIEW;

    if (lastHighlightedView == Views.HEX_VIEW) {
      // If the mouse is in the hex view just one nibble must be highlighted.
      final Rectangle r = getNibbleBoundsHex(lastHighlightedNibble);
      g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
    } else if (lastHighlightedView == Views.ASCII_VIEW) {

      // If the mouse is in the ASCII view it is necessary
      // to highlight two nibbles.

      final int first = (2 * lastHighlightedNibble) / 2; // Don't change.

      Rectangle r = getNibbleBoundsHex(first);
      g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());

      r = getNibbleBoundsHex(first + 1);
      g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
    }

    // Highlight the byte in the ASCII panel too.
    final Rectangle r = getByteBoundsAscii(lastHighlightedNibble);
    g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
  }

  /**
   * Draws the offsets in the offset view.
   *
   * @param g The graphics context of the hex panel.
   */
  private void drawOffsets(final Graphics g) {
    if (isEnabled()) {
      // Choose the right color for the offset text
      g.setColor(fontColorOffsets);
    } else {
      g.setColor(disabledColor != bgColorOffset ? disabledColor : Color.WHITE);
    }

    final int x = (-firstColumn * charWidth) + 10;

    final int bytesToDraw = getMaximumVisibleBytes();

    final String formatString = addressMode == AddressMode.BIT32 ? "%08X" : "%016X";

    // Iterate over the data and print the offsets
    for (int i = 0; i < bytesToDraw; i += bytesPerRow) {
      final long address = baseAddress + (firstRow * bytesPerRow) + i;

      final String offsetString = String.format(formatString, address);
      final int currentRow = i / bytesPerRow;

      g.drawString(offsetString, x, paddingTop + (currentRow * rowHeight));
    }
  }

  private ColoredRange findColoredRange(final long currentOffset) {
    for (final ColoredRangeManager element : coloredRanges) {
      final ColoredRange range = element.findRangeWith(currentOffset);

      if (range != null) {
        return range;
      }
    }

    return null;
  }

  /**
   * Returns the left coordinate of the ASCII view.
   *
   * @return The left coordinate of the ASCII view.
   */
  private int getAsciiViewLeft() {
    return getHexViewLeft() + getHexViewWidth();
  }

  /**
   * Returns the bounds of a byte in the ASCII view.
   *
   * @param position The index of one of the nibbles that belong to the byte.
   * @return The bounds of the byte in the ASCII view.
   */
  private Rectangle getByteBoundsAscii(final int position) {

    if (position < (2 * getFirstVisibleByte())) {
      return new Rectangle(-1, -1, -1, -1);
    }

    if (position > ((2 * getFirstVisibleByte()) + (2 * getMaximumVisibleBytes()))) {
      return new Rectangle(-1, -1, -1, -1);
    }

    final int relativePosition = (position - (2 * getFirstVisibleByte())) / 2;

    final int row = relativePosition / bytesPerRow;
    final int character = relativePosition % bytesPerRow;

    final int x = getAsciiViewLeft() + paddingAsciiLeft + (character * charWidth);
    final int y = (paddingTop - charHeight) + (row * rowHeight);

    return new Rectangle(x, y, charWidth, charHeight);
  }

  /**
   * Returns the number of bytes that need to be displayed.
   *
   * @return The number of bytes that need to be displayed.
   */
  private int getBytesToDraw() {

    final int firstVisibleByte = getFirstVisibleByte();

    final int maxBytes = getMaximumVisibleBytes();

    final int restBytes = dataProvider.getDataLength() - firstVisibleByte;

    return Math.min(maxBytes, restBytes);
  }

  /**
   * Returns the character size of a single character on the given graphics context.
   *
   * @param g The graphics context.
   * @return The size of a single character.
   */
  private int getCharacterWidth(final Graphics g) {
    return (int) g.getFontMetrics().getStringBounds("0", g).getWidth();
  }

  /**
   * Determines the height of a character in a graphical context.
   *
   * @param g The graphical context.
   * @return The height of a character in the graphical context.
   */
  private int getCharHeight(final Graphics g) {
    return g.getFontMetrics().getAscent();
  }

  /**
   * Returns the size of a hex view column in pixels (includes column spacing).
   *
   * @return The size of a hex view column in pixels.
   */
  private int getColumnSize() {
    return (NIBBLES_PER_BYTE * bytesPerColumn * charWidth) + columnSpacing;
  }

  /**
   * Returns the column of the byte at the current position.
   *
   * @return The column of the byte at the current position.
   */
  private int getCurrentColumn() {
    return (int) getCurrentNibble() % (NIBBLES_PER_BYTE * bytesPerRow);
  }

  /**
   * Returns the nibble at the caret position.
   *
   * @return The nibble at the care position.
   */
  private long getCurrentNibble() {
    return getSelectionStart() + getSelectionLength();
  }

  /**
   * Returns the row of the byte at the current position.
   *
   * @return The row of the byte at the current position.
   */
  private int getCurrentRow() {
    return (int) getCurrentNibble() / (NIBBLES_PER_BYTE * bytesPerRow);
  }

  /**
   * Returns the number of bytes before the first visible byte.
   *
   * @return The number of bytes before the first visible byte.
   */
  private int getEarlierBytes() {
    return firstRow * bytesPerRow;
  }

  /**
   * Returns the first visible byte.
   *
   * @return The first visible byte.
   */
  private int getFirstVisibleByte() {
    return firstRow * bytesPerRow;
  }

  /**
   * Returns the left position of the hex view.
   *
   * @return The left position of the hex view.
   */
  private int getHexViewLeft() {
    return (-firstColumn * charWidth) + offsetViewWidth;
  }

  /**
   * Returns the maximum number of visible bytes.
   *
   * @return The maximum number of visible bytes.
   */
  private int getMaximumVisibleBytes() {
    return getNumberOfVisibleRows() * bytesPerRow;
  }

  /**
   * Returns the index of the nibble below given coordinates.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The nibble index at the coordinates or -1 if there is no nibble at the coordinates.
   */
  private int getNibbleAtCoordinate(final int x, final int y) {

    if (dataProvider == null) {
      return -1;
    }

    if (x < (getHexViewLeft() + paddingHexLeft)) {
      return -1;
    }

    if (y >= (paddingTop - font.getSize())) {

      if ((x >= getHexViewLeft()) && (x < (getHexViewLeft() + getHexViewWidth()))) {
        // Cursor is in hex view
        return getNibbleAtCoordinatesHex(x, y);
      } else if (x >= getAsciiViewLeft()) {
        // Cursor is in ASCII view
        return getNibbleAtCoordinatesAscii(x, y);
      }
    }

    return -1;
  }

  /**
   * Returns the index of the nibble below given coordinates in the ASCII view.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The nibble index at the coordinates or -1 if there is no nibble at the coordinates.
   */
  private int getNibbleAtCoordinatesAscii(final int x, final int y) {

    // Normalize the x coordinate to inside the ASCII view
    final int normalizedX = x - (getAsciiViewLeft() + paddingAsciiLeft);

    if (normalizedX < 0) {
      return -1;
    }

    // Find the row at the coordinate
    final int row = (y - (paddingTop - charHeight)) / rowHeight;

    final int earlierPositions = 2 * getEarlierBytes();

    if ((normalizedX / charWidth) >= bytesPerRow) {
      return -1;
    }

    final int character = 2 * (normalizedX / charWidth);

    final int position = earlierPositions + (2 * row * bytesPerRow) + character;

    if (position >= (2 * dataProvider.getDataLength())) {
      return -1;
    } else {
      return position;
    }
  }

  /**
   * Returns the index of the nibble below given coordinates in the hex view.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The nibble index at the coordinates or -1 if there is no nibble at the coordinates.
   */
  private int getNibbleAtCoordinatesHex(final int x, final int y) {

    // Normalize the x coordinate to inside the hex view
    final int normalizedX = x - (getHexViewLeft() + paddingHexLeft);

    final int columnSize = getColumnSize();

    // Find the column at the specified coordinate.
    final int column = normalizedX / columnSize;

    // Return if the cursor is at the spacing at the end of a line.
    if (column >= (bytesPerRow / bytesPerColumn)) {
      return -1;
    }

    // Find the coordinate relative to the beginning of the column.
    final int xInColumn = normalizedX % columnSize;

    // Find the nibble inside the column.
    final int nibbleInColumn = xInColumn / charWidth;

    // Return if the cursor is in the spacing between columns.
    if (nibbleInColumn >= (2 * bytesPerColumn)) {
      return -1;
    }

    // Find the row at the coordinate
    final int row = (y - (paddingTop - charHeight)) / rowHeight;

    final int earlierPositions = 2 * getEarlierBytes();

    final int position =
        earlierPositions + (2 * ((row * bytesPerRow) + (column * bytesPerColumn))) + nibbleInColumn;

    if (position >= (2 * dataProvider.getDataLength())) {
      return -1;
    } else {
      return position;
    }
  }

  /**
   * Returns the bounds of a nibble in the hex view.
   *
   * @param position The index of the nibble.
   * @return The bounds of the nibble in the hex view.
   */
  private Rectangle getNibbleBoundsHex(final int position) {
    if (position < (2 * getFirstVisibleByte())) {
      return new Rectangle(-1, -1, -1, -1);
    }

    if (position > ((2 * getFirstVisibleByte()) + (2 * getMaximumVisibleBytes()))) {
      return new Rectangle(-1, -1, -1, -1);
    }

    final int relativePosition = position - (2 * getFirstVisibleByte());

    final int columnSize = getColumnSize();

    final int row = relativePosition / (2 * bytesPerRow);
    final int column = (relativePosition % (2 * bytesPerRow)) / (2 * bytesPerColumn);
    final int nibble = relativePosition % (2 * bytesPerRow) % (2 * bytesPerColumn);

    final int x = getHexViewLeft() + paddingHexLeft + (column * columnSize) + (nibble * charWidth);
    final int y = (paddingTop - charHeight) + (row * rowHeight);

    return new Rectangle(x, y, charWidth, charHeight);
  }

  /**
   * Returns the number of visible rows.
   *
   * @return The number of visible rows.
   */
  private int getNumberOfVisibleRows() {
    final int rawHeight = getHeight() - paddingTop - horizontalScrollbar.getHeight();
    return (rawHeight / rowHeight) + ((rawHeight % rowHeight) == 0 ? 0 : 1);
  }

  /**
   * Determines the height of the current font in a graphical context.
   *
   * @param g The graphical context.
   * @return The height of the current font in the graphical context.
   */
  private int getRowHeight(final Graphics g) {
    return g.getFontMetrics().getHeight();
  }

  private long getSelectionStart() {
    return selectionStart;
  }

  /** Initializes the keys that can be used by the user inside the component. */
  private void initHotkeys() {

    // Don't change focus on TAB
    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, new HashSet<KeyStroke>());

    final InputMap inputMap = this.getInputMap();
    final ActionMap actionMap = getActionMap();

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
    actionMap.put("LEFT", leftAction);

    inputMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), "shift LEFT");
    actionMap.put("shift LEFT", leftAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
    actionMap.put("RIGHT", rightAction);

    inputMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), "shift RIGHT");
    actionMap.put("shift RIGHT", rightAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
    actionMap.put("UP", upAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), "shift UP");
    actionMap.put("shift UP", upAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
    actionMap.put("DOWN", downAction);

    inputMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), "shift DOWN");
    actionMap.put("shift DOWN", downAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "PAGE_DOWN");
    actionMap.put("PAGE_DOWN", pageDownAction);

    inputMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.SHIFT_DOWN_MASK),
        "shift PAGE_DOWN");
    actionMap.put("shift PAGE_DOWN", pageDownAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "PAGE_UP");
    actionMap.put("PAGE_UP", pageUpAction);

    inputMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.SHIFT_DOWN_MASK), "shift PAGE_UP");
    actionMap.put("shift PAGE_UP", pageUpAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "TAB");
    actionMap.put("TAB", tabAction);
  }

  /** Initializes all internal listeners. */
  private void initListeners() {

    // Add the input listeners
    addMouseListener(listener);
    addMouseMotionListener(listener);
    addMouseWheelListener(listener);
    addFocusListener(listener);
    addComponentListener(listener);
    addKeyListener(listener);

    caret.addCaretListener(listener);
  }

  /** Creates and initializes the scroll bar that is used to scroll through the data. */
  private void initScrollbar() {
    scrollbar.addAdjustmentListener(listener);

    add(scrollbar, BorderLayout.EAST);
    horizontalScrollbar.addAdjustmentListener(listener);
    add(horizontalScrollbar, BorderLayout.SOUTH);
  }

  /**
   * Determines whether data to be displayed is available.
   *
   * @return True, if data is available. False, otherwise.
   */
  private boolean isDataAvailable() {
    return dataProvider != null;
  }

  private boolean isInsideAsciiView(final int x, final int y) {
    return (y >= (paddingTop - font.getSize())) && (x >= getAsciiViewLeft());
  }

  private boolean isInsideHexView(final int x, final int y) {
    return (y >= (paddingTop - font.getSize()))
        && (x >= getHexViewLeft())
        && (x < (getHexViewLeft() + getHexViewWidth()));
  }

  /**
   * Determines whether a certain position is visible in the view.
   *
   * @param position The position in question.
   * @return True, if the position is visible. False, otherwise.
   */
  private boolean isPositionVisible(final long position) {

    final int firstVisible = getFirstVisibleByte();
    final int lastVisible = firstVisible + getMaximumVisibleBytes();

    return (position >= (2 * firstVisible)) && (position <= (2 * lastVisible));
  }

  private boolean isSelectedOffset(long currentOffset) {
    currentOffset = currentOffset - baseAddress;

    if (getSelectionLength() == 0) {
      return false;
    } else if (getSelectionLength() > 0) {
      return (currentOffset >= (getSelectionStart() / 2))
          && ((2 * currentOffset) < (getSelectionStart() + getSelectionLength()));
    } else {
      return (currentOffset >= ((getSelectionStart() + getSelectionLength()) / 2))
          && ((2 * currentOffset) < getSelectionStart());
    }
  }

  /** Resets the current graphic buffer and prepares it for another round of drawing. */
  private void resetBufferedGraphic() {
    bufferGraphics.clearRect(0, 0, getWidth(), getHeight());
    bufferGraphics.setFont(font);
  }

  /**
   * Scrolls the scroll bar so that it matches the given position.
   *
   * @param position The position to scroll to.
   */
  private void scrollToPosition(final long position) {
    scrollbar.setValue((int) position / (2 * bytesPerRow));
  }

  /**
   * Moves the current position of the caret and notifies the listeners about the position change.
   *
   * @param newPosition The new position of the caret.
   */
  private void setCurrentPosition(final long newPosition) {
    selectionStart = newPosition; // Avoid notifying twice

    if (!isPositionVisible(getSelectionStart())) {
      scrollToPosition(getSelectionStart());
    }

    for (final IHexPanelListener listener : listeners) {
      listener.selectionChanged(getSelectionStart(), 1);
    }
  }

  /**
   * Updates the maximum scroll range of the scroll bar depending on the number of bytes in the
   * current data set.
   */
  private void setScrollBarMaximum() {
    if (dataProvider == null) {
      scrollbar.setMaximum(1);
      horizontalScrollbar.setMaximum(1);
    } else {
      final int visibleRows = getNumberOfVisibleRows();

      final int totalRows = dataProvider.getDataLength() / bytesPerRow;
      int scrollRange = (2 + totalRows) - visibleRows;

      if (scrollRange < 0) {
        scrollRange = 0;
        scrollbar.setValue(0);
        scrollbar.setEnabled(false);
      } else {
        scrollbar.setEnabled(true);
      }

      scrollbar.setMaximum(scrollRange);

      final int totalWidth = getAsciiViewLeft() + paddingAsciiLeft + (charWidth * bytesPerRow);

      final int realWidth = getWidth() - scrollbar.getWidth();

      if (realWidth >= totalWidth) {
        horizontalScrollbar.setValue(0);
        horizontalScrollbar.setEnabled(false);
      } else {
        horizontalScrollbar.setMaximum(((totalWidth - realWidth) / charWidth) + 1);
        horizontalScrollbar.setEnabled(true);
      }
    }
  }

  private void setSelectionStart(final long selectionStart) {
    this.selectionStart = selectionStart;

    for (final IHexPanelListener listener : listeners) {
      listener.selectionChanged(this.selectionStart, m_selectionLength);
    }
  }

  private void updateHexViewWidth() {
    hexViewWidth = 15 + ((getColumnSize() * getBytesPerRow()) / getBytesPerColumn());
  }

  /**
   * Calculates and sets the size of the offset view depending on the currently selected address
   * mode.
   */
  private void updateOffsetViewWidth() {
    final int addressBytes = addressMode == AddressMode.BIT32 ? 8 : 16;
    offsetViewWidth = PADDING_OFFSETVIEW + (charWidth * addressBytes);
  }

  /** Calculates and sets the preferred size of the component. */
  private void updatePreferredSize() {
    // TODO (timkornau): Improve this
    final int width = offsetViewWidth + hexViewWidth + (18 * charWidth) + scrollbar.getWidth();
    setPreferredSize(new Dimension(width, getHeight()));
    revalidate();
  }

  /** Paints the hex window. */
  @Override
  protected void paintComponent(final Graphics gx) {
    super.paintComponent(gx);

    // Make room for a new graphic
    resetBufferedGraphic();

    // Calculate current sizes of characters and rows
    calculateSizes();

    updateOffsetViewWidth();

    if (firstDraw) {
      firstDraw = false;

      // The first time the component is drawn, its size must be set.
      updateHexViewWidth();
      updatePreferredSize();
    }

    // Draw the background of the hex panel
    drawBackground(bufferGraphics);

    // Draw the offsets column
    drawOffsets(bufferGraphics);

    if (isEnabled()) {
      // Only draw the cursor "shadow" if the component is enabled.
      drawMouseOverHighlighting(bufferGraphics);
    }

    // If the component has defined data, it can be drawn.
    if ((status == DefinitionStatus.DEFINED) && (dataProvider != null)) {
      final int bytesToDraw = getBytesToDraw();

      if ((bytesToDraw != 0) && !dataProvider.hasData(getFirstVisibleOffset(), bytesToDraw)) {
        // At this point the component wants to draw data but the data
        // provider does not have the data yet. The hope is that the data
        // provider can reload the data. Until this happens, set the
        // component's status to UNDEFINED and create a timer that
        // periodically rechecks if the missing data is finally available.

        setDefinitionStatus(DefinitionStatus.UNDEFINED);
        setEnabled(false);

        if (updateTimer != null) {
          updateTimer.setRepeats(false);
          updateTimer.stop();
        }

        updateTimer =
            new Timer(1000, new WaitingForDataAction(getFirstVisibleOffset(), bytesToDraw));
        updateTimer.setRepeats(true);
        updateTimer.start();

        return;
      }
    }

    if (isDataAvailable() || (status == DefinitionStatus.UNDEFINED)) {
      // Draw the hex data
      drawHexView(bufferGraphics);

      // Draw the ASCII data
      drawAsciiPanel(bufferGraphics);

      // Show the caret if necessary
      if (caret.isVisible() && hasFocus()) {
        drawCaret(bufferGraphics);
      }
    }

    gx.drawImage(img, 0, 0, this);
  }

  /**
   * Adds a new event listener to the list of event listeners.
   *
   * @param listener The new event listener.
   * @throws NullPointerException Thrown if the listener argument is null.
   */
  public void addHexListener(final IHexPanelListener listener) {

    Preconditions.checkNotNull(listener, "Error: Listener can't be null");

    // Avoid duplicates
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Colorizes a range of bytes in special colors. To keep the default text or background color, it
   * is possible to pass null as these colors.
   *
   * @param level
   * @param offset The start offset of the byte range.
   * @param size The number of bytes in the range.
   * @param color The text color that is used to color that range.
   * @param bgcolor The background color that is used to color that range.
   * @throws IllegalArgumentException Thrown if offset is negative or size is not positive.
   */
  public void colorize(
      final int level, final long offset, final int size, final Color color, final Color bgcolor) {
    Preconditions.checkArgument(
        offset >= 0, "Error: offset argument must be greater or equal to zero");
    Preconditions.checkArgument(size >= 0, "Error: size argument must be greater or equal to zero");
    Preconditions.checkArgument(
        (level >= 0) && (level < coloredRanges.length),
        "Error: level argument must be greater or equal to zero");
    coloredRanges[level].addRange(new ColoredRange(offset, size, color, bgcolor));

    repaint();
  }

  public void dispose() {
    removeMouseListener(listener);
    removeMouseMotionListener(listener);
    removeMouseWheelListener(listener);
    removeFocusListener(listener);
    removeComponentListener(listener);
    removeKeyListener(listener);

    caret.removeListener(listener);

    caret.stop();
  }

  /**
   * Returns a a flag that indicates whether the bytes inside a column are flipped or not.
   *
   * @return True, if the bytes are flipped. False, otherwise.
   */
  public boolean doFlipBytes() {
    return flipBytes;
  }

  /**
   * Returns the currently used address mode.
   *
   * @return The currently used address mode.
   */
  public AddressMode getAddressMode() {
    return addressMode;
  }

  /**
   * Returns the current background color of the ASCII view.
   *
   * @return The current background color of the ASCII view.
   */
  public Color getBackgroundColorAsciiView() {
    return bgColorAscii;
  }

  /**
   * Returns the current background color of the hex view.
   *
   * @return The current background color of the hex view.
   */
  public Color getBackgroundColorHexView() {
    return bgColorHex;
  }

  /**
   * Returns the current background color of the offset view.
   *
   * @return The current background color of the offset view.
   */
  public Color getBackgroundColorOffsetView() {
    return bgColorOffset;
  }

  /**
   * Returns the current base address.
   *
   * @return The current base address.
   */
  public long getBaseAddress() {
    return baseAddress;
  }

  /**
   * Returns the number of bytes displayed per column.
   *
   * @return The number of bytes displayed per column.
   */
  public int getBytesPerColumn() {
    return bytesPerColumn;
  }

  /**
   * Returns the current number of bytes displayed per row.
   *
   * @return The current number of bytes displayed per row.
   */
  public int getBytesPerRow() {
    return bytesPerRow;
  }

  /**
   * Returns the spacing between columns in pixels.
   *
   * @return The spacing between columns.
   */
  public int getColumnSpacing() {
    return columnSpacing;
  }

  /**
   * Returns the offset at the current caret position.
   *
   * @return The offset at the current caret position.
   */
  public long getCurrentOffset() {
    final long currentOffset = baseAddress + (getCurrentNibble() / 2);

    return flipBytes
        ? ((currentOffset & -bytesPerColumn) + bytesPerColumn)
            - (currentOffset % bytesPerColumn)
            - 1
        : currentOffset;
  }

  /**
   * Returns the currently used data provider.
   *
   * @return The currently used data provider.
   */
  public IDataProvider getData() {
    return dataProvider;
  }

  /**
   * Returns the current definition status.
   *
   * @return The current definition status.
   */
  public DefinitionStatus getDefinitionStatus() {
    return status;
  }

  /**
   * Returns the first selected offset.
   *
   * @return The first selected offset.
   */
  public long getFirstSelectedOffset() {
    if (m_selectionLength >= 0) {
      return (baseAddress + selectionStart) / 2;
    } else {
      return (baseAddress + selectionStart + m_selectionLength) / 2;
    }
  }

  /**
   * Returns the first visible offset.
   *
   * @return The first visible offset.
   */
  public long getFirstVisibleOffset() {
    return getBaseAddress() + getFirstVisibleByte();
  }

  /**
   * Returns the current font color of the ASCII view.
   *
   * @return The current font color of the ASCII view.
   */
  public Color getFontColorAsciiView() {
    return fontColorAscii;
  }

  /**
   * Returns the current font color of even columns in the hex view.
   *
   * @return The current font color of even columns in the hex view.
   */
  public Color getFontColorHexView1() {
    return fontColorHex1;
  }

  /**
   * Returns the current font color of odd columns in the hex view.
   *
   * @return The current font color of odd columns in the hex view.
   */
  public Color getFontColorHexView2() {
    return fontColorHex2;
  }

  /**
   * Returns the current font color of the offset view.
   *
   * @return The current font color of the offset view.
   */
  public Color getFontColorOffsetView() {
    return fontColorOffsets;
  }

  /**
   * Returns the size of the font that is used to draw all data.
   *
   * @return The size of the font that is used to draw all data.
   */
  public int getFontSize() {
    return font.getSize();
  }

  /**
   * Returns the current width of the hex view.
   *
   * @return The current width of the hex view.
   */
  public int getHexViewWidth() {
    return hexViewWidth;
  }

  public long getLastOffset() {
    return getBaseAddress() + dataProvider.getDataLength();
  }

  /**
   * Returns the last selected offset.
   *
   * @return The last selected offset.
   */
  public long getLastSelectedOffset() {

    // In this method it is necessary to round up. This is because
    // half a selected byte counts as a fully selected byte.

    if (m_selectionLength >= 0) {
      return ((baseAddress + selectionStart + m_selectionLength) / 2)
          + ((baseAddress + selectionStart + m_selectionLength) % 2);
    } else {
      return ((baseAddress + selectionStart) / 2) + ((baseAddress + selectionStart) % 2);
    }
  }

  public long getSelectionLength() {
    return m_selectionLength;
  }

  public int getVisibleBytes() {
    final int visibleBytes = getMaximumVisibleBytes();

    if ((dataProvider.getDataLength() - getFirstVisibleByte()) >= visibleBytes) {
      return visibleBytes;
    } else {
      return dataProvider.getDataLength() - getFirstVisibleByte();
    }
  }

  /**
   * Scrolls to a given offset.
   *
   * @param offset The offset to scroll to.
   * @throws IllegalArgumentException Thrown if the offset is out of bounds.
   */
  public void gotoOffset(final long offset) {
    Preconditions.checkNotNull(dataProvider, "Error: No data provider active");

    if (getCurrentOffset() == offset) {
      if (!isPositionVisible(getSelectionStart())) {
        scrollToPosition(getSelectionStart());
      }
      return;
    }

    long realOffset;
    if (offset < baseAddress) {
      realOffset = offset;
    } else {
      realOffset = offset - baseAddress;
    }

    if ((realOffset < 0) || (realOffset >= dataProvider.getDataLength())) {
      throw new IllegalArgumentException("Error: Invalid offset");
    }

    setCurrentPosition(2 * realOffset);
  }

  /**
   * Returns the status of the component.
   *
   * @return True, if the component is enabled. False, otherwise.
   */
  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public void removeHexListener(final IHexPanelListener listener) {
    Preconditions.checkNotNull(listener, "Internal Error: Listener can't be null");

    if (!listeners.remove(listener)) {
      throw new IllegalArgumentException("Internal Error: Listener was not listening on object");
    }
  }

  /**
   * Sets the currently used address mode.
   *
   * @param mode The new address mode.
   * @throws NullPointerException Thrown if the new address mode is null.
   */
  public void setAddressMode(final AddressMode mode) {
    addressMode = Preconditions.checkNotNull(mode, "Error: Address mode can't be null");
    updateOffsetViewWidth();
    updatePreferredSize();
  }

  /**
   * Sets the current background color of the ASCII view.
   *
   * @param color The new background color of the ASCII view.
   * @throws NullPointerException Thrown if the new color is null.
   */
  public void setBackgroundColorAsciiView(final Color color) {
    bgColorAscii = Preconditions.checkNotNull(color, "Error: Color can't be null");
    repaint();
  }

  /**
   * Sets the current background color of the hex view.
   *
   * @param color The new background color of the hex view.
   * @throws NullPointerException Thrown if the new color is null.
   */
  public void setBackgroundColorHexView(final Color color) {
    bgColorHex = Preconditions.checkNotNull(color, "Error: Color can't be null");
    repaint();
  }

  /**
   * Sets the current background color of the offset view.
   *
   * @param color The new background color of the offset view.
   * @throws NullPointerException Thrown if the new color is null.
   */
  public void setBackgroundColorOffsetView(final Color color) {
    bgColorOffset = Preconditions.checkNotNull(color, "Error: Color can't be null");
    repaint();
  }

  /**
   * Sets the current base address.
   *
   * @param baseAddress The current base address.
   * @throws IllegalArgumentException Thrown if the new base address is negative.
   */
  public void setBaseAddress(final long baseAddress) {
    Preconditions.checkArgument(baseAddress >= 0, "Error: Base address can't be negative");
    this.baseAddress = baseAddress;
    repaint();
  }

  /**
   * Sets the number of bytes displayed per column.
   *
   * @param bytes The new number of bytes per column.
   * @throws IllegalArgumentException Thrown if the new number of bytes is smaller than 1 or bigger
   *     than the number of bytes per row.
   */
  public void setBytesPerColumn(final int bytes) {
    Preconditions.checkArgument(bytes > 0, "Error: Number of bytes must be positive");
    Preconditions.checkArgument(
        bytes <= bytesPerRow,
        "Error: Number of bytes can't be more than the number of bytes per row");

    bytesPerColumn = bytes;

    updateHexViewWidth();
    updatePreferredSize();

    repaint();
  }

  /**
   * Sets the current number of bytes displayed per row.
   *
   * @param value The new number of bytes displayed per row.
   * @throws IllegalArgumentException Thrown if the new number is smaller than 1.
   */
  public void setBytesPerRow(final int value) {
    Preconditions.checkArgument(value > 0, "Error: Value must be positive");
    bytesPerRow = value;
    repaint();
  }

  /**
   * Sets the {@link IColormap}.
   *
   * @param colormap The {@link IColormap} to set.
   */
  public void setColormap(final IColormap colormap) {
    this.colormap =
        Preconditions.checkNotNull(colormap, "Error: colormap argument can not be null");
    repaint();
  }

  /**
   * Sets the spacing between columns.
   *
   * @param spacing The spacing between columns in pixels.
   * @throws IllegalArgumentException Thrown if the new spacing is smaller than 1.
   */
  public void setColumnSpacing(final int spacing) {
    Preconditions.checkArgument(spacing > 0, "Error: Spacing must be positive");
    columnSpacing = spacing;
    repaint();
  }

  /**
   * Sets the caret to a new offset.
   *
   * @param offset The new offset.
   */
  public void setCurrentOffset(final long offset) {
    if (dataProvider == null) {
      return;
    }
    Preconditions.checkArgument(
        (offset >= getBaseAddress())
            || (offset <= (getBaseAddress() + dataProvider.getDataLength())),
        "Error: Invalid offset");
    setCurrentPosition(CHARACTERS_PER_BYTE * (offset - baseAddress));
  }

  /**
   * Sets the current data provider.
   *
   * <p>It is valid to pass null as the new data provider. This clears the display.
   *
   * @param data The new data provider.
   */
  public void setData(final IDataProvider data) {

    if (dataProvider != null) {
      dataProvider.removeListener(listener);
    }

    dataProvider = data;

    if (data != null) {
      dataProvider.addListener(listener);
    }

    setCurrentPosition(0);
    setScrollBarMaximum();
    repaint();
  }

  /**
   * Changes the definition status of the JHexView component. This flag determines whether real data
   * or ?? are displayed.
   *
   * @param status The new definition status.
   * @throws NullPointerException Thrown if the new definition status is null.
   */
  public void setDefinitionStatus(final DefinitionStatus status) {
    this.status = Preconditions.checkNotNull(status, "Error: Definition status can't be null");
    repaint();
  }

  /**
   * Enables or disables the component.
   *
   * @param enabled True to enable the component, false to disable it.
   */
  @Override
  public void setEnabled(final boolean enabled) {
    if (enabled == this.enabled) {
      return;
    }

    this.enabled = enabled;
    repaint();
  }

  public void setFlipBytes(final boolean flip) {
    if (flipBytes == flip) {
      return;
    }

    flipBytes = flip;
    repaint();
  }

  /**
   * Sets the current font color of the ASCII view.
   *
   * @param color The new font color of the ASCII view.
   * @throws NullPointerException Thrown if the new color is null.
   */
  public void setFontColorAsciiView(final Color color) {
    fontColorAscii = Preconditions.checkNotNull(color, "Error: Color can't be null");
    repaint();
  }

  /**
   * Sets the current font color of even columns in the hex view.
   *
   * @param color The new font color of even columns in the hex view.
   * @throws NullPointerException Thrown if the new color is null.
   */
  public void setFontColorHexView1(final Color color) {
    fontColorHex1 = Preconditions.checkNotNull(color, "Error: Color can't be null");
    repaint();
  }

  /**
   * Sets the current font color of odd columns in the hex view.
   *
   * @param color The new font color of odd columns in the hex view.
   * @throws NullPointerException Thrown if the new color is null.
   */
  public void setFontColorHexView2(final Color color) {
    fontColorHex2 = Preconditions.checkNotNull(color, "Error: Color can't be null");
    repaint();
  }

  /**
   * Sets the current font color of the offset view.
   *
   * @param color The new font color of the offset view.
   * @throws NullPointerException Thrown if the new color is null.
   */
  public void setFontColorOffsetView(final Color color) {
    fontColorOffsets = Preconditions.checkNotNull(color, "Error: Color can't be null");
    repaint();
  }

  /**
   * Sets the size of the font that is used to draw all data.
   *
   * @param size The size of the font that is used to draw all data.
   * @throws IllegalArgumentException Thrown if the new font size is smaller than 1.
   */
  public void setFontSize(final int size) {
    Preconditions.checkArgument(size > 0, "Error: Font size must be positive");
    font = GuiHelper.getMonospacedFont().deriveFont(size);
    setFont(font);

    // The proportions of the hex window change significantly.
    // Just start over when the next repaint event comes.
    firstDraw = true;

    repaint();
  }

  /**
   * Sets the width of the hex view.
   *
   * @param width The new width of the offset view.
   * @throws IllegalArgumentException Thrown if the new width is smaller than 1.
   */
  public void setHexViewWidth(final int width) {
    Preconditions.checkArgument(width > 0, "Error: Width must be positive");
    hexViewWidth = width;

    repaint();
  }

  /**
   * Sets the menu creator of the hex view control.
   *
   * @param creator The new menu creator. If this parameter is null, no context menu is shown in the
   *     component.
   */
  public void setMenuCreator(final IMenuCreator creator) {
    menuCreator = creator;
  }

  public void setSelectionColor(final Color color) {
    selectionColor = Preconditions.checkNotNull(color, "Error: Color can't be null");
    repaint();
  }

  public void setSelectionLength(final long selectionLength) {
    m_selectionLength = selectionLength;

    for (final IHexPanelListener listener : listeners) {
      listener.selectionChanged(selectionStart, m_selectionLength);
    }

    repaint();
  }

  /**
   * Removes special colorization from a range of bytes.
   *
   * @param offset The start offset of the byte range.
   * @param size The number of bytes in the byte range.
   * @throws IllegalArgumentException Thrown if offset is negative or size is not positive.
   */
  public void uncolorize(final int level, final long offset, final int size) {
    Preconditions.checkArgument(offset >= 0, "Error: Offset can't be negative");
    Preconditions.checkArgument(size > 0, "Error: Size must be positive");
    Preconditions.checkArgument(
        (level >= 0) && (level < coloredRanges.length), "Error: Invalid level");
    coloredRanges[level].removeRange(offset, size);
    repaint();
  }

  public void uncolorizeAll() {
    for (final ColoredRangeManager coloredRange : coloredRanges) {
      coloredRange.clear();
    }
  }

  /** Removes all special range colorizations. */
  public void uncolorizeAll(final int level) {
    coloredRanges[level].clear();

    repaint();
  }

  private class DownAction extends AbstractAction {
    private static final long serialVersionUID = -6501310447863685486L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      changeBy(event, 2 * bytesPerRow);
    }
  }

  /**
   * Event listeners are moved into an internal class to avoid publishing the listener methods in
   * the public interface of the JHexPanel.
   */
  private class InternalListener
      implements AdjustmentListener,
          MouseListener,
          MouseMotionListener,
          FocusListener,
          ICaretListener,
          IDataChangedListener,
          ComponentListener,
          KeyListener,
          MouseWheelListener {
    private void keyPressedInAsciiView(final KeyEvent event) {
      final byte[] data = dataProvider.getData(getCurrentOffset(), 1);

      if (getSelectionStart() >= (dataProvider.getDataLength() * 2)) {
        return;
      }

      data[0] = (byte) event.getKeyChar();

      dataProvider.setData(getCurrentOffset(), data);

      setSelectionStart(getSelectionStart() + 2);
    }

    private void keyPressedInHexView(final KeyEvent key) {
      final byte[] data = dataProvider.getData(getCurrentOffset(), 1);

      final long pos = baseAddress + getSelectionStart();

      if (getSelectionStart() >= (dataProvider.getDataLength() * 2)) {
        return;
      }

      final int value = Character.digit(key.getKeyChar(), 16);

      if (value == -1) {
        return;
      }

      if ((pos % 2) == 0) {
        data[0] = (byte) ((data[0] & 0x0F) | (value << 4));
      } else {
        data[0] = (byte) ((data[0] & 0xF0) | value);
      }

      dataProvider.setData(getCurrentOffset(), data);

      setSelectionStart(getSelectionStart() + 1);
    }

    @Override
    public void adjustmentValueChanged(final AdjustmentEvent event) {
      if (event.getSource() == scrollbar) {
        firstRow = event.getValue();
      } else {
        firstColumn = event.getValue();
      }

      repaint();
    }

    @Override
    public void caretStatusChanged(final JCaret source) {
      repaint();
    }

    @Override
    public void componentHidden(final ComponentEvent event) {}

    @Override
    public void componentMoved(final ComponentEvent event) {}

    @Override
    public void componentResized(final ComponentEvent event) {
      setScrollBarMaximum();

      int width = (getWidth() + 1) - scrollbar.getWidth();
      int height = (getHeight() + 1) - horizontalScrollbar.getHeight();

      width = Math.max(1, width);
      height = Math.max(1, height);

      img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

      bufferGraphics = img.getGraphics();
    }

    @Override
    public void componentShown(final ComponentEvent event) {}

    @Override
    public void dataChanged() {
      setScrollBarMaximum();
      repaint();
    }

    @Override
    public void focusGained(final FocusEvent event) {
      caret.setVisible(true);
      repaint();
    }

    @Override
    public void focusLost(final FocusEvent event) {
      repaint();
    }

    @Override
    public void keyPressed(final KeyEvent event) {
      if (!isEnabled()) {
        return;
      }

      if (activeView == Views.HEX_VIEW) {
        if (dataProvider.isEditable() && Convert.isHexCharacter(event.getKeyChar())) {
          keyPressedInHexView(event);
        }
      } else {
        if (dataProvider.isEditable() && Convert.isPrintableCharacter(event.getKeyChar())) {
          keyPressedInAsciiView(event);
        }
      }

      repaint();
    }

    @Override
    public void keyReleased(final KeyEvent event) {}

    @Override
    public void keyTyped(final KeyEvent event) {}

    @Override
    public void mouseClicked(final MouseEvent event) {}

    @Override
    public void mouseDragged(final MouseEvent event) {
      if (!isEnabled()) {
        return;
      }

      final int x = event.getX();
      final int y = event.getY();

      if (y < (paddingTop - (rowHeight - charHeight))) {
        scrollToPosition((2 * getFirstVisibleByte()) - (2 * bytesPerRow));

        if ((getSelectionLength() - (2 * bytesPerRow)) < 0) {
          return;
        }

        setSelectionLength(getSelectionLength() - (2 * bytesPerRow));
      } else if (y >= (rowHeight * getNumberOfVisibleRows())) {
        scrollToPosition((2 * getFirstVisibleByte()) + (2 * bytesPerRow));

        if ((getSelectionLength() + (2 * bytesPerRow))
            > (2 * (dataProvider.getDataLength() - getSelectionStart()))) {
          return;
        }

        setSelectionLength(getSelectionLength() + (2 * bytesPerRow));
      } else {
        final int position = getNibbleAtCoordinate(x, y);

        if (position != -1) {
          setSelectionLength(position - getSelectionStart());
          repaint();
        }
      }
    }

    @Override
    public void mouseEntered(final MouseEvent event) {}

    @Override
    public void mouseExited(final MouseEvent event) {}

    @Override
    public void mouseMoved(final MouseEvent event) {
      lastMouseX = event.getX();
      lastMouseY = event.getY();

      repaint();
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (!isEnabled()) {
        return;
      }

      if ((event.getButton() == MouseEvent.BUTTON1) || (event.getButton() == MouseEvent.BUTTON3)) {

        m_selectionLength = 0; // We don't want the notifiers to kick in here.
        // setSelectionLength(0);

        requestFocusInWindow();

        final int x = event.getX();
        final int y = event.getY();

        final int position = getNibbleAtCoordinate(x, y);

        if (position != -1) {

          caret.setVisible(true);
          setCurrentPosition(position);

          if (isInsideHexView(x, y)) {
            activeView = Views.HEX_VIEW;
          } else if (isInsideAsciiView(x, y)) {
            activeView = Views.ASCII_VIEW;
          }

          repaint();
        } else {
          // m_selectionLength = 0 must be notified in case the click position is invalid.

          for (final IHexPanelListener listener : listeners) {
            listener.selectionChanged(selectionStart, m_selectionLength);
          }

          repaint();
        }
      }

      if (event.getButton() == MouseEvent.BUTTON3) {
        final int x = event.getX();
        final int y = event.getY();

        final int position = getNibbleAtCoordinate(x, y);

        if (position != -1) {

          caret.setVisible(true);

          if (menuCreator != null) {
            final JPopupMenu menu = menuCreator.createMenu(getCurrentOffset());

            if (menu != null) {
              menu.show(JHexView.this, x, y);
            }
          }
          repaint();
        }
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {}

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
      // Mouse wheel support for scrolling

      if (!isEnabled()) {
        return;
      }

      final int notches = e.getWheelRotation();
      scrollbar.setValue(scrollbar.getValue() + notches);
    }
  }

  private class LeftAction extends AbstractAction {
    private static final long serialVersionUID = -9032577023548944503L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      changeBy(event, activeView == Views.HEX_VIEW ? -1 : -2);
    }
  }

  private class PageDownAction extends AbstractAction {
    private static final long serialVersionUID = 490837791577654025L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      changeBy(event, getNumberOfVisibleRows() * bytesPerRow * 2);
    }
  }

  private class PageUpAction extends AbstractAction {
    private static final long serialVersionUID = -7424423002191015929L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      changeBy(event, -getNumberOfVisibleRows() * bytesPerRow * 2);
    }
  }

  private class RightAction extends AbstractAction {
    private static final long serialVersionUID = 3857972387525998636L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      changeBy(event, activeView == Views.HEX_VIEW ? 1 : 2);
    }
  }

  private class TabAction extends AbstractAction {
    private static final long serialVersionUID = -3265020583339369531L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      // Switch between hex and ASCII view

      if (activeView == Views.HEX_VIEW) {
        activeView = Views.ASCII_VIEW;
        setSelectionStart(getSelectionStart() - (getSelectionStart() % 2));
      } else {
        activeView = Views.HEX_VIEW;
      }

      caret.setVisible(true);
      repaint();
    }
  }

  private class UpAction extends AbstractAction {
    private static final long serialVersionUID = -3513103611571283106L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      changeBy(event, -2 * bytesPerRow);
    }
  }

  /** Enumeration that is used to decide which view of the component has the focus. */
  private enum Views {
    HEX_VIEW,
    ASCII_VIEW
  }

  private class WaitingForDataAction extends AbstractAction {
    private static final long serialVersionUID = -610823391617272365L;
    private final long m_offset;
    private final int m_size;

    private WaitingForDataAction(final long offset, final int size) {
      m_offset = offset;
      m_size = size;
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
      if (dataProvider.hasData(m_offset, m_size)) {
        JHexView.this.setEnabled(true);
        setDefinitionStatus(DefinitionStatus.DEFINED);

        ((Timer) arg0.getSource()).stop();
      } else if (!dataProvider.keepTrying()) {
        ((Timer) arg0.getSource()).stop();
      }
    }
  }

  /**
   * Enumeration that is used to switch the output format of the offsets between 32 bit mode and 64
   * bit mode.
   */
  public enum AddressMode {
    BIT32,
    BIT64
  }

  /** Enumeration that is used to decided whether real data or ??? is shown. */
  public enum DefinitionStatus {
    DEFINED,
    UNDEFINED
  }
}
