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

package com.google.security.zynamics.zylib.gui.JRegisterView;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.JCaret.ICaretListener;
import com.google.security.zynamics.zylib.gui.JCaret.JCaret;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigInteger;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Scrollable;

/** Java component that can be used to display CPU registers and their values. */
public class JRegisterView extends JPanel implements Scrollable {

  /** Font used to draw the data. */
  private final Font font = GuiHelper.getMonospacedFont();

  /** Register model that provides information about the registers to be displayed. */
  private final IRegisterModel registerModel;

  /** Size of the longest register name in the registers array. */
  private int longestRegisterName = 0;

  /** The currently highlighted register */
  private int highlightedRegister = -1;

  /** Width of a single character on the display. */
  private int charWidth;

  /** Color used to draw the values of modified registers. */
  private final Color modifiedColor = Color.RED;

  /** Color used to draw the values of unmodified registers. */
  private final Color textColor = Color.BLACK;

  /**
   * Space between the left border of the component and the first drawn characters of the register
   * names.
   */
  private static final int paddingLeft = 10;

  /** Background color of registers whose value is currently edited. */
  private final Color bgColorEdit = new Color(0xFFD0E0);

  /** Background color of registers that are currently highlighted. */
  private final Color bgColorHighlight = Color.WHITE;

  /** Keeps track of the currently edited register. */
  private int editedRegister = -1;

  /** The blinking caret used by the view. */
  private final JCaret caret = new JCaret();

  /** Keeps track of the caret position during register editing. */
  private int caretPosition = 0;

  /** Value of the currently edited register. */
  private BigInteger editValue = BigInteger.ZERO;

  /** Background color. */
  private final Color backgroundColor = new Color(0xCCCCFF);

  /** Default text color that is used for all output text if the component is disabled. */
  private final Color disabledColor = Color.GRAY;

  private IMenuProvider menuProvider;

  private final InternalListener listener = new InternalListener();

  /**
   * Creates a new register viewer object.
   *
   * @param registerModel the model to use for register access
   */
  public JRegisterView(final IRegisterModel registerModel) {
    Preconditions.checkNotNull(registerModel, "Error: Argument registerModel can't be null");

    registerModel.addListener(listener);

    // Necessary to receive input

    setFocusable(true);
    setOpaque(true);

    // Initialize the listeners that handle mouse and keyboard input
    initializeListeners();

    // Copy the register information from the argument array
    // into an internal extended structure.
    this.registerModel = registerModel;

    updateLongestRegisterName();

    setBackground(backgroundColor);

    // The first time the component is drawn, its size must be set.
    updatePreferredSize();
  }

  /**
   * Draws the caret.
   *
   * @param g
   */
  private void drawCaret(final Graphics g) {

    if (hasFocus() && (editedRegister != -1)) {

      // Get the bounds of the edited register
      final Rectangle r = getRegisterBounds(editedRegister);

      final int characterHeight = font.getSize();
      final int x = (r.x + (caretPosition * charWidth) + (longestRegisterName * charWidth) + 5) - 1;
      final int y = r.y;

      // Draw the caret
      caret.draw(g, x, y, characterHeight);
    }
  }

  /**
   * Draws the background of the highlighted register in a different color than the standard
   * background.
   *
   * @param g
   */
  private void drawHighlightedRegister(final Graphics g) {

    if (!isEnabled()) {
      return;
    }

    if (editedRegister != -1) {

      // Highlight the edited register if applicable.
      g.setColor(bgColorEdit);

      final Rectangle r = getRegisterBounds(editedRegister);
      g.fillRect(r.x, r.y, r.width, r.height);

    } else if (highlightedRegister != -1) {

      // Highlight the highlighted register if applicable.
      g.setColor(bgColorHighlight);

      final Rectangle r = getRegisterBounds(highlightedRegister);
      g.fillRect(r.x, r.y, r.width, r.height);
    }
  }

  /**
   * Draws the names and values of the registers.
   *
   * @param g
   */
  private void drawRegisters(final Graphics g) {

    final int PADDING_TOP = font.getSize() + 5;

    int y = PADDING_TOP;

    final int lineHeight = font.getSize();

    int registerCounter = 0;

    // Draw the name and value of each register
    for (final RegisterInformationInternal register : registerModel.getRegisterInformation()) {

      if (isEnabled()) {
        // Set the text color depending on the register status
        g.setColor(
            register.isModified() || (registerCounter == editedRegister)
                ? modifiedColor
                : textColor);
      } else {
        g.setColor(disabledColor);
      }

      // Draw the register name
      g.drawString(register.getRegisterName(), paddingLeft, y);

      final BigInteger value =
          (registerCounter == editedRegister ? editValue : register.getValue())
              .and(
                  register.getRegisterSize() == 8
                      ? BigInteger.valueOf(9223372036854775807L)
                      : BigInteger.valueOf(4294967295L));

      final String valueString;

      if (register.getRegisterSize() != 0) {
        // Get the register value string
        final String formatMask = "%0" + (2 * register.getRegisterSize()) + "X";
        valueString = String.format(formatMask, value);
      } else {
        valueString = String.valueOf(value.and(BigInteger.ONE));
      }

      // Draw the register value
      g.drawString(valueString, 10 + 5 + (charWidth * longestRegisterName), y);

      ++registerCounter;

      y += lineHeight;
    }
  }

  /**
   * Enters the register edit mode.
   *
   * @param register The register that is edited.
   */
  private void enterEditMode(final int register) {
    requestFocusInWindow();

    caret.setVisible(true);
    editedRegister = register;
    caretPosition = 0;
    editValue = registerModel.getRegisterInformation(register).getValue();
    repaint();
  }

  /**
   * Calculates the pixel bounds of a register.
   *
   * @param registerNumber The number of the register.
   * @return The pixel bounds of the register on the screen.
   */
  private Rectangle getRegisterBounds(final int registerNumber) {

    final RegisterInformation register = registerModel.getRegisterInformation(registerNumber);

    final int x = 10;
    final int y = 7 + (registerNumber * font.getSize());
    final int width = 5 + (charWidth * (longestRegisterName + (register.getRegisterSize() * 2)));
    final int height = font.getSize() - 1;

    return new Rectangle(x, y, width, height);
  }

  /**
   * Returns the number of the register at the given coordinates.
   *
   * @param x The x position.
   * @param y The y position.
   * @return The register at the coordinate (x, y) or -1 if there is no such register.
   */
  private int getRegisterNumber(final int x, final int y) {
    final int lineHeight = font.getSize();

    if ((y >= 7) && (y <= (7 + (lineHeight * registerModel.getNumberOfRegisters())))) {
      int registerNumber = (y - 7) / lineHeight;

      registerNumber = Math.min(registerNumber, registerModel.getNumberOfRegisters() - 1);

      if (registerNumber == -1) {
        return -1;
      }

      final RegisterInformation r = registerModel.getRegisterInformation(registerNumber);
      final int valuePrintSize = r.getRegisterSize() == 0 ? 1 : r.getRegisterSize() * 2;
      final int maxWidth = 10 + 5 + (charWidth * (longestRegisterName + valuePrintSize));

      if ((x >= 10) && (x <= maxWidth)) {
        return registerNumber;
      }
    }

    return -1;
  }

  /** Initializes the listeners that handle keyboard and mouse input. */
  private void initializeListeners() {
    addMouseListener(listener);
    addMouseMotionListener(listener);
    addKeyListener(listener);
    addFocusListener(listener);

    caret.addCaretListener(listener);
  }

  /** Leaves the register edit mode. */
  private void leaveEditMode(final boolean update) {
    if (update) {

      final RegisterInformationInternal editedRegister =
          registerModel.getRegisterInformation(this.editedRegister);

      if (!Objects.equals(editValue, editedRegister.getValue())) {
        beginRegisterUpdate();
        registerModel.setValue(editedRegister.getRegisterName(), editValue);
        endRegisterUpdate();
      }
    }

    caret.setVisible(false);
    editedRegister = -1;
    caretPosition = 0;

    repaint();
  }

  /** Updates the member variable that keeps track of the register with the longest name. */
  private void updateLongestRegisterName() {

    longestRegisterName = Integer.MIN_VALUE;

    for (final RegisterInformation register : registerModel.getRegisterInformation()) {
      if (register.getRegisterName().length() > longestRegisterName) {
        longestRegisterName = register.getRegisterName().length();
      }
    }
  }

  private void updatePreferredSize() {
    final int PADDING_TOP = font.getSize() + 5;

    final int lineHeight = font.getSize();

    final int height = PADDING_TOP + (registerModel.getNumberOfRegisters() * lineHeight);

    setPreferredSize(new Dimension(200, height));

    revalidate();

    updateUI();
  }

  /** Draws the register information. */
  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);

    g.setFont(font);

    charWidth = (int) g.getFontMetrics().getStringBounds("0", g).getWidth();

    if (registerModel != null) {
      drawHighlightedRegister(g);

      drawRegisters(g);

      if (caret.isVisible()) {
        drawCaret(g);
      }
    }
  }

  /** This method must be called before batch updating of registers. */
  public void beginRegisterUpdate() {

    for (final RegisterInformationInternal register : registerModel.getRegisterInformation()) {
      register.setModified(false);
    }
  }

  public void dispose() {
    removeMouseListener(listener);
    removeMouseMotionListener(listener);
    removeKeyListener(listener);
    removeFocusListener(listener);

    caret.removeListener(listener);

    caret.stop();
  }

  /** This method must be called after batch updating of registers. */
  public void endRegisterUpdate() {
    repaint();
  }

  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  @Override
  public int getScrollableBlockIncrement(
      final Rectangle visibleRect, final int orientation, final int direction) {
    return 5 * font.getSize();
  }

  @Override
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  @Override
  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  @Override
  public int getScrollableUnitIncrement(
      final Rectangle visibleRect, final int orientation, final int direction) {
    return font.getSize();
  }

  @Override
  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);

    repaint();
  }

  public void setMenuProvider(final IMenuProvider provider) {
    menuProvider = provider;
  }

  /**
   * Helper class that is used to hide the callback methods of the necessary listeners from the
   * public interface.
   */
  private class InternalListener
      implements MouseMotionListener,
          MouseListener,
          KeyListener,
          FocusListener,
          ICaretListener,
          IRegistersChangedListener {

    /**
     * Converts valid hex input values to int values.
     *
     * @param c The valid hex input value.
     * @return The int value.
     */
    private int hexToValue(final char c) {

      if ((c >= 'A') && (c <= 'F')) {
        return (c - 'A') + 10;
      } else if ((c >= 'a') && (c <= 'f')) {
        return (c - 'a') + 10;
      } else {
        return c - '0';
      }
    }

    /**
     * Tests whether a character is a valid hex input value.
     *
     * @param c The character to test.
     * @return True, if the character is a valid hex input value. False, otherwise.
     */
    private boolean isHexChar(final char c) {
      return ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F')) || ((c >= '0') && (c <= '9'));
    }

    @Override
    public void caretStatusChanged(final JCaret source) {
      repaint();
    }

    @Override
    public void focusGained(final FocusEvent event) {}

    @Override
    public void focusLost(final FocusEvent event) {

      if (editedRegister != 1) {
        leaveEditMode(false);
      }
    }

    @Override
    public void keyPressed(final KeyEvent event) {
      if (event.getKeyCode() == KeyEvent.VK_RIGHT) {

        if (caretPosition
            != (2 * registerModel.getRegisterInformation(editedRegister).getRegisterSize())) {
          caretPosition++;
        }

        event.consume(); // Consume the event to avoid scrolling

        caret.setVisible(true);
        repaint();
      } else if (event.getKeyCode() == KeyEvent.VK_LEFT) {

        if (caretPosition != 0) {
          caretPosition--;
        }

        event.consume(); // Consume the event to avoid scrolling

        caret.setVisible(true);
        repaint();
      } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {

        if (editedRegister == -1) {
          if (highlightedRegister != -1) {
            enterEditMode(highlightedRegister);
          }
        } else {
          leaveEditMode(true);
        }

        repaint();
      } else if (isHexChar(event.getKeyChar())) {
        final int regSize = registerModel.getRegisterInformation(editedRegister).getRegisterSize();

        if (caretPosition == (2 * regSize)) {
          return;
        }

        // Get the input value
        final long val = hexToValue(event.getKeyChar());

        // Four bits are relevant for each input character.
        final long relevantBits = (regSize * 8) - 4 - (caretPosition * 4);

        // Find a mask for the relevant bits
        final long mask = 0xFL << relevantBits;

        // Shift the input value into the correct bits.
        final long shiftedNew = val << relevantBits;

        // Calculate the new value.
        editValue = editValue.and(BigInteger.valueOf(~mask)).or(BigInteger.valueOf(shiftedNew));

        caretPosition++;

        caret.setVisible(true);
        repaint();
      }
    }

    @Override
    public void keyReleased(final KeyEvent event) {
      // Do nothing
    }

    @Override
    public void keyTyped(final KeyEvent event) {
      // Do nothing
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
      final int registerNumber = getRegisterNumber(event.getX(), event.getY());

      if (event.getButton() == MouseEvent.BUTTON1) {

        if (event.getClickCount() == 1) {
          if ((editedRegister != -1) && (registerNumber != editedRegister)) {
            leaveEditMode(false);
          }
        } else if (event.getClickCount() == 2) {
          // Switch to edit mode after a double click.
          if ((registerNumber != -1) && (editedRegister == -1)) {
            final RegisterInformationInternal register =
                registerModel.getRegisterInformation(registerNumber);

            if (register.getRegisterSize() == 0) {
              registerModel.setValue(
                  register.getRegisterName(), register.getValue().xor(BigInteger.ONE));
            } else {
              enterEditMode(highlightedRegister);
            }
          }
        }
      } else if (event.getButton() == MouseEvent.BUTTON3) {
        if (event.getClickCount() == 1) {
          if ((editedRegister != -1) && (registerNumber != editedRegister)) {
            leaveEditMode(false);
          }

          final JPopupMenu menu = menuProvider.getRegisterMenu(registerNumber);

          if (menu != null) {
            menu.show(JRegisterView.this, event.getX(), event.getY());
          }
        }
      }
    }

    @Override
    public void mouseDragged(final MouseEvent event) {
      // Do nothing
    }

    @Override
    public void mouseEntered(final MouseEvent event) {
      // Do nothing
    }

    @Override
    public void mouseExited(final MouseEvent event) {
      // No highlighting if the mouse left the component.
      if (editedRegister == -1) {
        highlightedRegister = -1;
        repaint();
      }
    }

    @Override
    public void mouseMoved(final MouseEvent event) {
      // Keep track of the register below the mouse.
      highlightedRegister = getRegisterNumber(event.getX(), event.getY());

      repaint();
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      // Do nothing
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      // Do nothing
    }

    @Override
    public void registerDataChanged() {
      updatePreferredSize();
      updateLongestRegisterName();
      repaint();
    }
  }
}
