// Copyright 2011-2024 Google LLC
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

import com.google.security.zynamics.zylib.gui.JCaret.ICaretListener;
import com.google.security.zynamics.zylib.gui.JCaret.JCaret;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JComponent;
import javax.swing.JScrollBar;

/**
 * A general-purpose JComponent to allow the synchronized side-by-side rendering of multiple
 * "columns" of highlighted text. This is useful for all sorts of things: * Write a code editor that
 * displays line numbers at the left-hand side but allows wrapping of lines * An IDA-style display
 * with addresses at the left, opcode bytes to the right of it, instructions, and finally comments
 * to the far right * ... etc.
 *
 * <p>Originally intended to replace some tables in BinNavi, but more widely applicable for all
 * sorts of (monospace) text rendering.
 */
public class CodeDisplay extends JComponent {
  /** The metrics of the font to be used. */
  private FontMetrics fontMetrics;

  /** The font to be used in this component. */
  private Font textFont;

  /** Vertical scroll bar to scroll through the different rows in the display. */
  private final JScrollBar verticalScrollbar =
      new JScrollBar(JScrollBar.VERTICAL, 0 /* value */, 1 /* extent */, 0 /* min */, 1 /* max */);

  /** Horizontal scroll bar that is used to scroll sideways. */
  private final JScrollBar horizontalScrollbar =
      new JScrollBar(
          JScrollBar.HORIZONTAL, 0 /* value */, 1 /* extent */, 0 /* min */, 1 /* max */);

  /** Default internal listener that is used to handle various events. */
  private final InternalListener listener = new InternalListener();

  /** The reference to the underlying data model. */
  private final ICodeDisplayModel codeModel;

  /** Internal double-buffer for drawing. */
  private BufferedImage bufferedImage;

  private Graphics2D bufferedGraphics;

  /** The dimensions of individual characters in the selected font and the height of a text row. */
  private int fontCharWidth = 0;

  private int fontLineHeight = 0;

  /** The caret inside the component. */
  private final JCaret caret = new JCaret();

  private CodeDisplayCoordinate caretPosition = new CodeDisplayCoordinate(0, 0, 0, 0);

  /** Coordinates for the caret in the local FormattedCharacterBuffer. */
  private int caretX = 0;

  private int caretY = 0;

  /** The number of currently visible rows. */
  private int currentlyVisibleLines = 0;

  /** The number of currently visible character columns. */
  private int currentlyVisibleColumns = 0;

  /** The first row to draw onto the screen. */
  private int currentFirstRow = 0;

  /** The first line of the first row to be drawn onto the screen. */
  private int currentFirstLine = 0;

  /**
   * The first column of characters (do not confuse with the columns of the data model) that is
   * displayed on screen.
   */
  private int currentFirstCharColumn = 0;

  /** The formatted character buffer that is used for drawing the component. */
  private FormattedCharacterBuffer charBuffer = new FormattedCharacterBuffer(0, 0);

  /** Used for keeping track of objects listening for events from this code display */
  private List<CodeDisplayEventListener> eventListeners = new ArrayList<>();

  /**
   * The code makes use of the well-orderedness of TreeMap, this map *cannot* simply be replaced
   * with a different map.
   */
  private TreeMap<Integer, CodeDisplayCoordinate> yCoordinateToRowAndLine = new TreeMap<>();

  // Utility function for people using this class.
  public static String padRight(String s, int n) {
    if (n == 0) {
      return "";
    }
    return String.format("%1$-" + n + "s", s);
  }

  public CodeDisplay(ICodeDisplayModel codeDisplayModel) {
    codeModel = codeDisplayModel;
    textFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    // Necessary to receive input
    setFocusable(true);
    setLayout(new BorderLayout());

    initializeListeners();
    initializeScrollbars();
    initializeFontMetrics(textFont);

    // Calculates how many lines and columns will be actually visible at the
    // moment.
    currentlyVisibleLines = getNumberOfVisibleLines();
    currentlyVisibleColumns = getNumberOfVisibleColumns();

    // Initialize the internal graphics buffer.
    initializeGraphicsBuffer();

    setScrollBarMaximum();

    // By default, this component is disabled.
    setEnabled(true);

    // Set the initial caret to the first editable column.
    int xPosition = 0;
    CodeDisplayCoordinate testCoordinate = new CodeDisplayCoordinate(0, 0, 0, 0);
    for (int columnIndex = 0; columnIndex < codeModel.getNumberOfColumns(); columnIndex++) {
      xPosition += codeModel.getColumnWidthInCharacters(columnIndex);
      testCoordinate.setColumn(columnIndex);
      if (codeModel.canHaveCaret(testCoordinate)) {
        setCaretPosition(testCoordinate);
        caretX = xPosition;
        caretY = 1;
      }
    }
  }

  private void initializeGraphicsBuffer() {
    bufferedImage =
        new BufferedImage(
            ((codeModel.getTotalWidthInCharacters() + 1) * fontCharWidth),
            (currentlyVisibleLines + 10) * fontLineHeight,
            BufferedImage.TYPE_INT_RGB);
    bufferedGraphics = (Graphics2D) bufferedImage.getGraphics();
  }

  private void notifyCaretListeners() {
    for (CodeDisplayEventListener listener : eventListeners) {
      listener.caretChanged(caretPosition);
    }
  }

  /**
   * In order to get the font metrics, a graphics context is required. This function temporarily
   * creates one.
   */
  private void initializeFontMetrics(Font font) {
    final BufferedImage temporaryImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    Graphics2D temporaryGraphics2D = (Graphics2D) temporaryImage.getGraphics();
    temporaryGraphics2D.setFont(font);
    fontMetrics = temporaryGraphics2D.getFontMetrics();
    fontCharWidth = fontMetrics.getMaxAdvance();
    fontLineHeight = fontMetrics.getHeight();
  }

  private void initializeListeners() {
    // Add the input listeners
    addMouseListener(listener);
    addMouseMotionListener(listener);
    addMouseWheelListener(listener);
    addFocusListener(listener);
    addComponentListener(listener);
    addKeyListener(listener);

    caret.addCaretListener(listener);
  }

  /** Creates and initializes the scroll bars that are used to scroll through the data. */
  private void initializeScrollbars() {
    verticalScrollbar.addAdjustmentListener(listener);

    add(verticalScrollbar, BorderLayout.EAST);
    horizontalScrollbar.addAdjustmentListener(listener);
    add(horizontalScrollbar, BorderLayout.SOUTH);
  }

  private int getNumberOfVisibleColumns() {
    final int rawWidth = getWidth() - verticalScrollbar.getWidth();
    return (rawWidth / fontCharWidth) + ((rawWidth % fontCharWidth) == 0 ? 0 : 1);
  }

  private int getNumberOfVisibleLines() {
    final int rawHeight = getHeight() - horizontalScrollbar.getHeight();
    return (rawHeight / fontLineHeight) + ((rawHeight % fontLineHeight) == 0 ? 0 : 1);
  }

  /** Updates the maximum scroll range of the scroll bar depending on the number */
  private void setScrollBarMaximum() {
    final int totalRows = codeModel.getNumberOfRows();
    int scrollRange = totalRows;

    // Disables the vertical scroll bar if all rows are visible.
    if (scrollRange < 0) {
      scrollRange = 0;
      verticalScrollbar.setValue(0);
      verticalScrollbar.setEnabled(false);
    } else {
      verticalScrollbar.setEnabled(true);
    }

    verticalScrollbar.setMaximum(scrollRange);
    final int totalWidth = codeModel.getTotalWidthInCharacters();
    int realWidth = getWidth();
    realWidth -= verticalScrollbar.getWidth();

    // Disables the horizontal scroll bar if everything fits into the component.
    if (realWidth >= (totalWidth * fontCharWidth)) {
      horizontalScrollbar.setValue(0);
      horizontalScrollbar.setEnabled(false);
    } else {
      horizontalScrollbar.setMaximum(totalWidth + 1);
      horizontalScrollbar.setEnabled(true);
    }
  }

  /**
   * Updates/rewrites the internal representation of the character buffer (that will be drawn) from
   * the data model. Updating the character buffer from the data model requires a bit of care: The
   * model provides a sequence of rows, each divided into a fixed number of columns, and each cell
   * (identified by a row/column combination) can have multiple lines of text. To properly copy this
   * into a FormattedCharacterBuffer, the code iterates over all rows first. For each row, it
   * determines what the maximum number of text lines is (over all columns) - this is needed to
   * determine the overall height of the row. The individual cells are then filled into the right
   * row, and everything advances to the next row.
   */
  private void updateCharacterBufferFromModel() {
    charBuffer.clear();

    charBuffer.setBackgroundColor(java.awt.Color.LIGHT_GRAY.brighter());
    currentFirstRow = verticalScrollbar.getValue();
    currentFirstCharColumn = horizontalScrollbar.getValue();

    // Draw the header, if requested.
    int totalCopiedLines = 0;
    if (codeModel.hasHeaderRow()) {
      totalCopiedLines = 1;
      int currentColumnIndex = 0;
      for (int fieldIndex = 0;
          fieldIndex < codeModel.getNumberOfColumns();
          currentColumnIndex += codeModel.getColumnWidthInCharacters(fieldIndex), fieldIndex++) {
        charBuffer.copyInto(0, currentColumnIndex, codeModel.getHeader(fieldIndex));
      }
    }

    // Iterates over the rows. Since each row has a height of at least one line,
    // the for() loop iterates over more than what is strictly required and
    // aborts early.
    for (int rowIndex = currentFirstRow;
        rowIndex < Math.min(currentFirstRow + currentlyVisibleLines, codeModel.getNumberOfRows());
        rowIndex++) {
      for (int lineIndex = (rowIndex == currentFirstRow) ? currentFirstLine : 0;
          lineIndex < codeModel.getMaximumLinesForRow(rowIndex);
          lineIndex++) {
        // Iterate over all the columns that need to be drawn.
        int currentColumnIndex = 0;
        for (int fieldIndex = 0;
            fieldIndex < codeModel.getNumberOfColumns();
            currentColumnIndex += codeModel.getColumnWidthInCharacters(fieldIndex), fieldIndex++) {
          // Update the current X/Y position of the caret in terms of this buffer.
          if ((caretPosition.getRow() == rowIndex)
              && (caretPosition.getLine() == lineIndex)
              && (caretPosition.getColumn() == fieldIndex)) {
            caretX = currentColumnIndex + caretPosition.getFieldIndex();
            caretY = totalCopiedLines;
          }

          FormattedCharacterBuffer line =
              codeModel.getLineFormatted(rowIndex, fieldIndex, lineIndex);

          if (line != null) {
            charBuffer.copyInto(totalCopiedLines, currentColumnIndex, line);
          }

          // Update the map that allows quick mapping of pixel positions to rows
          // and lines within rows.
          int linestart = totalCopiedLines * fontLineHeight + (fontLineHeight / 2);
          if (!yCoordinateToRowAndLine.containsKey(linestart)) {
            yCoordinateToRowAndLine.put(
                linestart, new CodeDisplayCoordinate(rowIndex, lineIndex, 0, 0));
          } else {
            CodeDisplayCoordinate coordinate = yCoordinateToRowAndLine.get(linestart);
            coordinate.setRow(rowIndex);
            coordinate.setLine(lineIndex);
          }
        }
        totalCopiedLines++;
      }
    }
    setScrollBarMaximum();
  }

  @Override
  protected void paintComponent(final Graphics gx) {
    super.paintComponent(gx);

    bufferedGraphics.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    updateVisibleLinesAndColumns();
    updateCharacterBufferFromModel();

    currentFirstRow = verticalScrollbar.getValue();
    currentFirstCharColumn = horizontalScrollbar.getValue();
    charBuffer.paintBuffer(bufferedGraphics, 0, 0, currentFirstCharColumn);
    gx.drawImage(bufferedImage, 2, 2, this);

    caret.draw(gx, 2 + (caretX * fontCharWidth), 6 + (caretY * fontLineHeight), fontLineHeight - 1);
  }

  void updateVisibleLinesAndColumns() {
    // Calculates how many lines and columns will be actually visible at the
    // moment.
    int currentVisibleLines = getNumberOfVisibleLines();
    int currentVisibleColumns = getNumberOfVisibleColumns();
    if ((currentlyVisibleLines != currentVisibleLines)
        || (currentlyVisibleColumns != currentVisibleColumns)) {
      currentlyVisibleLines = currentVisibleLines;
      currentlyVisibleColumns = currentVisibleColumns;

      // Include room for the newline characters.
      int properWidth = codeModel.getTotalWidthInCharacters();
      int properLines = currentlyVisibleLines + 1;
      charBuffer = new FormattedCharacterBuffer(properLines, properWidth);
      bufferedImage =
          new BufferedImage(
              properWidth * fontCharWidth,
              properLines * fontLineHeight,
              BufferedImage.TYPE_INT_RGB);
      bufferedGraphics = (Graphics2D) bufferedImage.getGraphics();
    }
  }

  void setSelectionStart() {}

  private boolean fillColumnAndFieldIndexFromX(int x, CodeDisplayCoordinate coordinate) {
    x = x + currentFirstCharColumn * fontCharWidth;
    int columnstart = 0;
    int characterIndex = 0;
    for (int index = 0; index < codeModel.getNumberOfColumns(); index++) {
      int columnend = columnstart + codeModel.getColumnWidthInCharacters(index) * fontCharWidth;
      if ((x >= columnstart) && (x < columnend)) {
        x = x - columnstart;
        characterIndex = x / fontCharWidth;
        coordinate.setColumn(index);
        coordinate.setFieldIndex(characterIndex);
        return true;
      }
      columnstart = columnend;
    }
    return false;
  }

  /**
   * Given a position on the screen, calculate what row / cell / line a given coordinate obtained
   * from an event falls into.
   */
  private boolean fillCoordinateFromXY(int x, int y, CodeDisplayCoordinate newCoordinate) {
    Map.Entry<Integer, CodeDisplayCoordinate> coordinate = yCoordinateToRowAndLine.floorEntry(y);
    if (coordinate == null) {
      // If the click went outside the last row or behind the last column, return an appropriate
      // coordinate.
      newCoordinate.setRow(2);
      newCoordinate.setColumn(1);
      return true;
    }
    newCoordinate.setRow(coordinate.getValue().getRow());
    newCoordinate.setLine(coordinate.getValue().getLine());
    return fillColumnAndFieldIndexFromX(x, newCoordinate);
  }

  public CodeDisplayCoordinate getCaretPosition() {
    return caretPosition;
  }

  // Sets the position of the caret, and then figures out what the X and Y coordinates of the new
  // caret in the buffer need to be.
  public void setCaretPosition(CodeDisplayCoordinate coordinate) {
    caretPosition = coordinate;
  }

  /**
   * Code that is used to interacting with JTables often requires a method like this to function -
   * mapping a given point (the site of an event etc.) to a proper table row.
   */
  public int rowAtPoint(Point point) {
    CodeDisplayCoordinate coordinate = new CodeDisplayCoordinate(0, 0, 0, 0);
    fillCoordinateFromXY(point.x, point.y, coordinate);
    return coordinate.getRow();
  }

  public int columnAtPoint(Point point) {
    CodeDisplayCoordinate coordinate = new CodeDisplayCoordinate(0, 0, 0, 0);
    fillCoordinateFromXY(point.x, point.y, coordinate);
    return coordinate.getColumn();
  }

  public int lineAtPoint(Point point) {
    CodeDisplayCoordinate coordinate = new CodeDisplayCoordinate(0, 0, 0, 0);
    fillCoordinateFromXY(point.x, point.y, coordinate);
    return coordinate.getLine();
  }

  public void addCaretChangedListener(CodeDisplayEventListener e) {
    eventListeners.add(e);
  }

  public void removeCaretChangedListener(CodeDisplayEventListener e) {
    eventListeners.remove(e);
  }

  /** Internal event listener. */
  private class InternalListener extends MouseAdapter
      implements AdjustmentListener, FocusListener, ICaretListener, ComponentListener, KeyListener {
    @Override
    public void adjustmentValueChanged(final AdjustmentEvent event) {
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
      updateVisibleLinesAndColumns();
      updateCharacterBufferFromModel();

      setScrollBarMaximum();
    }

    @Override
    public void componentShown(final ComponentEvent event) {}

    @Override
    public void focusGained(final FocusEvent event) {
      repaint();
    }

    @Override
    public void focusLost(final FocusEvent event) {
      repaint();
    }

    @Override
    public void keyPressed(final KeyEvent event) {
      if (!event.isActionKey()) {
        return;
      }

      CodeDisplayCoordinate newCoordinate = new CodeDisplayCoordinate(caretPosition);
      int line = newCoordinate.getLine();
      int row = newCoordinate.getRow();
      int fieldIndex = newCoordinate.getFieldIndex();
      int column = newCoordinate.getColumn();
      switch (event.getKeyCode()) {
        case KeyEvent.VK_UP:
          if (line == 0) {
            int newRow = row - 1;
            newCoordinate.setRow(Math.max(newRow, 0));
            int maximumLines = codeModel.getMaximumLinesForRow(newRow);
            if (maximumLines > 0) {
              newCoordinate.setLine(codeModel.getMaximumLinesForRow(newRow) - 1);
            }
          } else {
            newCoordinate.setLine(line - 1);
          }
          break;
        case KeyEvent.VK_DOWN:
          if (line == codeModel.getMaximumLinesForRow(row) - 1) {
            newCoordinate.setRow(Math.min(row + 1, codeModel.getNumberOfRows()));
            newCoordinate.setLine(0);
          } else {
            newCoordinate.setLine(line + 1);
          }
          break;
        case KeyEvent.VK_LEFT:
          if (fieldIndex == 0) {
            // Skip one column to the left if it is editable.
            newCoordinate.setColumn(column - 1);
            newCoordinate.setFieldIndex(codeModel.getColumnWidthInCharacters(column - 1) - 1);
          } else {
            newCoordinate.setFieldIndex(fieldIndex - 1);
          }
          break;
        case KeyEvent.VK_RIGHT:
          fieldIndex = newCoordinate.getFieldIndex();
          if (fieldIndex == codeModel.getColumnWidthInCharacters(column) - 1) {
            // Skip one column to the right if it is editable.
            newCoordinate.setColumn(column + 1);
            newCoordinate.setFieldIndex(0);
          } else {
            newCoordinate.setFieldIndex(fieldIndex + 1);
          }
          break;
        case KeyEvent.VK_PAGE_DOWN:
          // Shift the caret down by as many lines as are currently displayed.
          for (int count = 0; count < currentlyVisibleLines - 2; count++) {
            if (newCoordinate.getLine() >= codeModel.getMaximumLinesForRow(row) - 1) {
              newCoordinate.setRow(
                  Math.min(newCoordinate.getRow() + 1, codeModel.getNumberOfRows()));
              newCoordinate.setLine(0);
            } else {
              newCoordinate.setLine(newCoordinate.getLine() + 1);
            }
          }
          // getCaretXYFromCoordinate()
          verticalScrollbar.setValue(newCoordinate.getRow());
          break;
        case KeyEvent.VK_PAGE_UP:
          // Shift the caret down by as many lines as are currently displayed.
          for (int count = 0; count < currentlyVisibleLines - 2; count++) {
            if (newCoordinate.getLine() == 0) {
              int newRow = Math.max(newCoordinate.getRow() - 1, 0);
              newCoordinate.setRow(Math.max(newRow, 0));
              newCoordinate.setLine(codeModel.getMaximumLinesForRow(newRow) - 1);
            } else {
              newCoordinate.setLine(Math.max(newCoordinate.getLine() - 1, 0));
            }
          }
          verticalScrollbar.setValue(newCoordinate.getRow());
          break;
        case KeyEvent.VK_HOME:
          // Delegate the handling of this to the data model.
          codeModel.keyPressedOrTyped(newCoordinate, event);
          break;
        case KeyEvent.VK_END:
          // Delegate the handling of this to the data model.
          codeModel.keyPressedOrTyped(newCoordinate, event);
          break;
        case KeyEvent.VK_BACK_SPACE:
          codeModel.keyPressedOrTyped(newCoordinate, event);
          break;
        default:
          throw new IllegalArgumentException();
      }
      if (codeModel.canHaveCaret(newCoordinate)) {
        setCaretPosition(newCoordinate);
        updateCharacterBufferFromModel();
        notifyCaretListeners();
        repaint();
      }
    }

    @Override
    public void keyReleased(final KeyEvent event) {}

    @Override
    public void keyTyped(final KeyEvent event) {
      if (codeModel.isEditable(caretPosition)) {
        CodeDisplayCoordinate before = new CodeDisplayCoordinate(caretPosition);
        codeModel.keyPressedOrTyped(caretPosition, event);
        if (!before.equals(caretPosition)) {
          notifyCaretListeners();
        }
        updateCharacterBufferFromModel();
        repaint();
      }
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
      // Set the caret
      requestFocusInWindow();
    }

    @Override
    public void mouseDragged(final MouseEvent event) {}

    @Override
    public void mouseEntered(final MouseEvent event) {}

    @Override
    public void mouseExited(final MouseEvent event) {}

    @Override
    public void mouseMoved(final MouseEvent event) {}

    @Override
    public void mousePressed(final MouseEvent event) {
      CodeDisplayCoordinate coordinate = new CodeDisplayCoordinate(0, 0, 0, 0);
      if (!fillCoordinateFromXY(event.getX(), event.getY(), coordinate)) {
        return;
      }
      if (codeModel.canHaveCaret(coordinate)) {
        // Set the position of the caret accordingly.
        setCaretPosition(coordinate);
        updateCharacterBufferFromModel();
        notifyCaretListeners();
        repaint();
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {}

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
      final int notches = e.getWheelRotation();
      verticalScrollbar.setValue(verticalScrollbar.getValue() + notches);
    }
  }

  @Override
  public java.awt.Dimension getPreferredSize() {
    // The preferred size is as wide as the columns dictate, and 40 (arbitrary number) of rows.
    // Override if different dimensions are needed.
    return new java.awt.Dimension(
        fontCharWidth * codeModel.getTotalWidthInCharacters(), fontLineHeight * 40);
  }
}
