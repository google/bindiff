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

package com.google.security.zynamics.zylib.gui.scripting.console;

//
// CPythonCodeDocument.java
// CBinNavi
//
// Created by Ero Carrera Ventura on 29/6/06.
// Copyright 2006 Ero Carrera. All rights reserved.
//

import com.google.security.zynamics.zylib.gui.scripting.CodeDocumentPython;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/** Document style for the syntax highlighted Python interpreter. */
public class ConsoleCodeDocument extends CodeDocumentPython {
  private static final long serialVersionUID = -4854242616532427400L;

  /**
   * Vector objects containing the strings to be highlighted and the corresponding
   * SimpleAttributeSet objects for their styles.
   */
  private final SimpleAttributeSet pythonPromptAttr = new SimpleAttributeSet();

  private KeyListener inputKeyListener;
  private String remainingTextString = null;

  private AttributeSet remainingTextAttr = null;

  private final ConsoleHelpers helpers = new ConsoleHelpers(this, normal);

  /**
   * Constructor. Sets up the styles and add the strings to be highlighted into the corresponding
   * vectors.
   */
  public ConsoleCodeDocument() {
    super(false);

    putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

    StyleConstants.setForeground(pythonPromptAttr, Color.LIGHT_GRAY);
    StyleConstants.setBold(pythonPromptAttr, true);
    StyleConstants.setFontSize(pythonPromptAttr, 13);
  }

  /**
   * Insert pending characters.
   *
   * <p>In order to handle multiline pasting, every time a newline character is found in the inserted
   * string, a "Enter" key event is sent and the remaining input string at that point saved in order
   * to be processesed after the key event is handled. Processing of such text is done by calling
   * this method.
   *
   * @param lastPos the last position currently available in the input text pane
   */
  public void flushRemainingText(final int lastPos) {
    if (remainingTextString == null) {
      return;
    }
    try {
      insertString(lastPos, remainingTextString, remainingTextAttr);
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
    remainingTextString = null;
    remainingTextAttr = null;
  }

  public int getCaretOffsetInLine(final int caretPosition) {
    return helpers.getCaretOffsetInLine(caretPosition);
  }

  public String getCurrentLine(final int caretPosition) {
    return helpers.getCurrentLine(caretPosition);
  }

  public int getLineStartOffset(final int caretPosition) {
    return helpers.getLineStartOffset(caretPosition);
  }

  public String getWord(final int currPos) {
    return helpers.getWord(currPos);
  }

  /**
   * Insert a single character into the document. If the character is contained within the
   * delimiters the previous word, supposed to be complete then, is checked against the ones to be
   * syntax highlighted by checkText()
   *
   * @param offs location within the document where to insert the character
   * @param str the character to insert
   */
  public void insertChar(final int offs, final String str) {
    if (offs < 0) {
      return;
    }

    try {
      super.insertString(offs, str, normal);
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Write a specific Python prompt into the given position. The position is nearly always the end
   * of the document.
   *
   * @param pos where to insert the prompt
   * @param prompt String with the prompt to insert
   */
  public void insertPrompt(final int pos, final String prompt) {
    try {
      super.insertString(pos, prompt, pythonPromptAttr);
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Insert a String of source code to be highlighted to the document. The string is then inserted
   * character by character if longer than 1 char.
   *
   * @param offs the position in the document
   * @param str the String containing source code
   * @param attr the attributes to set
   */
  @Override
  public void insertString(final int offs, final String str, final AttributeSet attr) {
    if (offs < 0) {
      return;
    }

    if (str.length() > 1) {
      int i;
      for (i = 0; i < str.length(); i++) {
        if (str.charAt(i) == '\n') {
          remainingTextString = str.substring(i + 1);
          remainingTextAttr = attr;

          inputKeyListener.keyPressed(
              new KeyEvent(new Container(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ENTER, '\n'));

          break;
        } else {
          insertChar(offs + i, "" + str.charAt(i));
        }
      }
    } else if (str.length() == 1) {
      insertChar(offs, str);
    }
  }

  /**
   * Insert a line in the text element containing the passed position skipping a number of character
   * from the beginning of the element. The function of the skipping of characters is to "jump" over
   * the prompt in the beginning of a line.
   *
   * @param pos the position from which to retrieve the element that contains it
   * @param skip how many characters are skipped from the beginning of the element when inserting
   *     the string
   * @param line the line to insert
   */
  public void setCurrentLine(final int pos, final int skip, final String line) {
    final Element element = getParagraphElement(pos);

    final int start = element.getStartOffset();
    final int end = element.getEndOffset();

    try {
      remove(start + skip, end - (start + skip + 1));
      super.insertString(start + skip, line, normal);
    } catch (final BadLocationException e) {
      System.out.println("Bad location!");
      e.printStackTrace();
    }
  }

  /**
   * Set the KeyListener instance used by the parent. This is needed in order for this class to be
   * able to send key events to the input text pane.
   *
   * @param kl KeyListener instance to be added
   */
  public void setInputKeyListener(final KeyListener kl) {
    inputKeyListener = kl;
  }
}
