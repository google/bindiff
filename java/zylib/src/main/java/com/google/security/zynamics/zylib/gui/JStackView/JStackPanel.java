// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.zylib.gui.JStackView;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView.DefinitionStatus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.Timer;

/** This class can be used to display the stack of a target process. */
public final class JStackPanel extends JPanel {
  private static final int PADDING_OFFSETVIEW = 20;

  private static final int PADDING_LEFT = 10;

  private static final int SIZEOF_DWORD = 4;

  private static final int SIZEOF_QWORD = 8;

  /** Provides the stack data that is displayed in the view. */
  private final IStackModel model;

  /** Font used to draw the data. */
  private final Font font = GuiHelper.getMonospacedFont();

  /** Height of individual rows in the display. */
  private int rowHeight;

  /** Height of a single char in the display. */
  private int charHeight;

  /** Width of a single char in the view. */
  private int charWidth;

  /** Width of the offset view. */
  private int offsetViewWidth;

  private boolean firstDraw = true;

  private static final int hexElementWidth = 10;

  /** Default internal listener that is used to handle various events. */
  private final InternalListener listener = new InternalListener();

  /** The first visible row. */
  private int firstRow = 0;

  /** Top-padding of all views in pixels. */
  private static final int paddingTop = 16;

  /** Font color of the offset view. */
  private final Color fontColorOffsets = Color.WHITE;

  private final Color fontColorValues = Color.BLACK;

  /** Color that is used to draw all text in disabled components. */
  private final Color disabledColor = Color.GRAY;

  /** Background color of the offset view. */
  private final Color bgColorOffset = Color.GRAY;

  /** Timer that is used to refresh the component if no data for the selected range is available. */
  private Timer updateTimer;

  private DefinitionStatus status = DefinitionStatus.UNDEFINED;

  private final AddressMode addressMode = AddressMode.BIT32;

  private int firstColumn = 0;

  /**
   * Creates a new stack view.
   *
   * @param model The model that provides the data displayed in the view.
   */
  public JStackPanel(final IStackModel model) {
    super(new BorderLayout());

    Preconditions.checkNotNull(model, "Error: Model argument can not be null");

    this.model = model;

    this.model.addListener(listener);

    // Necessary to receive input
    setFocusable(true);

    // Set the initial font
    setFont(font);

    setPreferredSize(new Dimension(400, 400));
  }

  /**
   * Returns the character size of a single character on the given graphics context.
   *
   * @param g The graphics context.
   * @return The size of a single character.
   */
  private static int getCharacterWidth(final Graphics g) {
    return (int) g.getFontMetrics().getStringBounds("0", g).getWidth();
  }

  /**
   * Determines the height of a character in a graphical context.
   *
   * @param g The graphical context.
   * @return The height of a character in the graphical context.
   */
  private static int getCharHeight(final Graphics g) {
    return g.getFontMetrics().getAscent();
  }

  /**
   * Determines the height of the current font in a graphical context.
   *
   * @param g The graphical context.
   * @return The height of the current font in the graphical context.
   */
  private static int getRowHeight(final Graphics g) {
    return g.getFontMetrics().getHeight();
  }

  /**
   * Calculates current character and row sizes.
   *
   * @param g The graphical context.
   */
  private void calculateSizes(final Graphics g) {
    rowHeight = getRowHeight(g);
    charHeight = getCharHeight(g);
    charWidth = getCharacterWidth(g);
  }

  /**
   * Draws the background of the view.
   *
   * @param g The graphics context of the view.
   */
  private void drawBackground(final Graphics g) {
    // Draw the background of the offset view
    g.setColor(bgColorOffset);
    g.fillRect(-firstColumn * charWidth, 0, offsetViewWidth, getHeight());
  }

  /**
   * Draws the stack values onto the screen.
   *
   * @param g The graphics context to paint on.
   */
  private void drawElements(final Graphics g) {
    if (isEnabled()) {
      // Choose the right color for the offset text
      g.setColor(fontColorValues);
    } else {
      g.setColor(disabledColor != bgColorOffset ? disabledColor : Color.WHITE);
    }

    final int x = (10 + offsetViewWidth) - (charWidth * firstColumn);

    int linesToDraw = getNumberOfVisibleRows();

    if ((firstRow + linesToDraw) >= model.getNumberOfEntries()) {
      linesToDraw = model.getNumberOfEntries() - firstRow; // TODO: This can make linesToDraw
      // negative

      if (linesToDraw < 0) {
        // FIXME: This is a workaround for case 2337. The issue is real
        // but reproducing it can take hours. For this reason I am now
        // implementing this workaround, but in the future the underlying
        // cause of this behavior should be determined and fixed.

        return;
      }
    }

    if (model.getStartAddress() == -1) {
      return;
    }

    final long elementSize = getElementSize();

    if (status == DefinitionStatus.DEFINED) {
      final long startAddress = model.getStartAddress() + (firstRow * elementSize);

      final long numberOfBytes = linesToDraw * elementSize;

      if (!model.hasData(startAddress, numberOfBytes)) {
        setDefinitionStatus(DefinitionStatus.UNDEFINED);
        setEnabled(false);

        if (updateTimer != null) {
          updateTimer.setRepeats(false);
          updateTimer.stop();
        }

        updateTimer = new Timer(1000, new WaitingForDataAction(startAddress, numberOfBytes));
        updateTimer.setRepeats(true);
        updateTimer.start();

        return;
      }

      // Iterate over the data and print the offsets
      for (int i = 0; i < linesToDraw; i++) {
        final long elementAddress = startAddress + (i * elementSize);

        g.drawString(model.getElement(elementAddress), x, paddingTop + (i * rowHeight));
      }
    } else {
      // Iterate over the data and print the offsets
      for (int i = 0; i < linesToDraw; i++) {
        g.drawString(Strings.repeat("?", 2 * getElementSize()), x, paddingTop + (i * rowHeight));
      }
    }
  }

  /**
   * Draws the offsets in the offset view.
   *
   * @param g The graphics context of the hex panel.
   */
  private void drawOffsets(final Graphics g) {
    final int linesToDraw = getNumberOfVisibleRows();

    final String formatString = addressMode == AddressMode.BIT32 ? "%08X" : "%016X";
    final long elementSize = getElementSize();

    final long baseAddress = model.getStartAddress() == -1 ? 0 : model.getStartAddress();

    // Iterate over the data and print the offsets
    for (int i = 0; i < linesToDraw; i++) {
      final int elementIndex = firstRow + i;

      final long elementAddress = baseAddress + (elementIndex * elementSize);

      final String offsetString = String.format(formatString, elementAddress);

      if (elementAddress == model.getStackPointer()) {
        highlightStackPointer(g, i);
      }

      if (isEnabled()) {
        g.setColor(fontColorOffsets);
      } else {
        g.setColor(disabledColor != bgColorOffset ? disabledColor : Color.WHITE);
      }

      g.drawString(
          offsetString, PADDING_LEFT - (charWidth * firstColumn), paddingTop + (i * rowHeight));
    }
  }

  /**
   * Returns the size in bytes of a single stack element.
   *
   * @return The size in bytes of a single stack element.
   */
  private int getElementSize() {
    return addressMode == AddressMode.BIT32 ? SIZEOF_DWORD : SIZEOF_QWORD;
  }

  /**
   * Highlights the address of the stack pointer.
   *
   * @param g The graphics context to draw to.
   * @param row Row where the stack pointer is shown.
   */
  private void highlightStackPointer(final Graphics g, final int row) {
    g.setColor(Color.RED);

    final double width =
        g.getFontMetrics().getStringBounds(Strings.repeat("0", 2 * getElementSize()), g).getWidth();

    g.fillRect(
        PADDING_LEFT - 2 - (charWidth * firstColumn),
        (paddingTop + (row * rowHeight)) - charHeight,
        (int) width + 4,
        charHeight + 2);
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
    // TODO: Improve this
    final int width = offsetViewWidth + hexElementWidth + (18 * charWidth);
    setPreferredSize(new Dimension(width, getHeight()));
    revalidate();
  }

  /**
   * Returns the number of visible rows.
   *
   * @return The number of visible rows.
   */
  protected int getNumberOfVisibleRows() {
    if (rowHeight == 0) {
      return 0;
    }

    final int rawHeight = getHeight() - paddingTop;
    return (rawHeight / rowHeight) + ((rawHeight % rowHeight) == 0 ? 0 : 1);
  }

  protected void setFirstRow(final int value) {
    firstRow = value;

    repaint();
  }

  public int getCharWidth() {
    return charWidth;
  }

  public int getOffsetViewWidth() {
    return offsetViewWidth;
  }

  public String getValueAt(final Point point) {
    final int line = ((point.y - paddingTop) + rowHeight) / rowHeight;

    final long elementSize = getElementSize();

    final long startAddress = model.getStartAddress() + (firstRow * elementSize);

    final long elementAddress = startAddress + (line * elementSize);

    return model.hasData(elementAddress, elementSize) ? model.getElement(elementAddress) : null;
  }

  /**
   * Scrolls to the given offset.
   *
   * @param offset The offset to scroll to.
   */
  public void gotoOffset(final long offset) {
    // setCurrentPosition(offset);
  }

  @Override
  public void paint(final Graphics g) {
    super.paint(g);

    // Calculate current sizes of characters and rows
    calculateSizes(g);

    updateOffsetViewWidth();

    if (firstDraw) {
      firstDraw = false;

      // The first time the component is drawn, its size must be set.
      updatePreferredSize();
    }

    // Draw the background of the hex panel
    drawBackground(g);

    // Draw the offsets column
    drawOffsets(g);

    drawElements(g);
  }

  /**
   * Changes the definition status of the view data.
   *
   * @param status The new definition status.
   */
  public void setDefinitionStatus(final DefinitionStatus status) {
    Preconditions.checkNotNull(status, "Error: Status argument can not be null");

    this.status = status;

    repaint();
  }

  public void setFirstColumn(final int value) {
    firstColumn = value;
  }

  private class InternalListener implements IStackModelListener {
    @Override
    public void dataChanged() {
      repaint();
    }
  }

  private class WaitingForDataAction extends AbstractAction {
    private static final long serialVersionUID = -610823391617272365L;
    private final long m_startAddress;
    private final long m_numberOfBytes;

    private WaitingForDataAction(final long startAddress, final long numberOfBytes) {
      m_startAddress = startAddress;
      m_numberOfBytes = numberOfBytes;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (model.hasData(m_startAddress, m_numberOfBytes)) {
        JStackPanel.this.setEnabled(true);
        setDefinitionStatus(DefinitionStatus.DEFINED);

        ((Timer) event.getSource()).stop();
      } else if (!model.keepTrying()) {
        ((Timer) event.getSource()).stop();
      }
    }
  }
}
