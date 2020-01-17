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

package com.google.security.zynamics.zylib.gui.scripting;

import com.google.security.zynamics.zylib.general.Convert;

import java.awt.Color;
import java.util.HashSet;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


public abstract class SyntaxDocument extends DefaultStyledDocument {
  private static final int DEFAULT_FONT_SIZE = 13;

  private final DefaultStyledDocument doc;

  private final Element rootElement;
  private boolean multiLineComment;

  private final MutableAttributeSet comment = new SimpleAttributeSet();
  private final MutableAttributeSet keyword = new SimpleAttributeSet();
  private final MutableAttributeSet type = new SimpleAttributeSet();
  private final MutableAttributeSet constant = new SimpleAttributeSet();
  private final MutableAttributeSet number = new SimpleAttributeSet();
  private final MutableAttributeSet quote = new SimpleAttributeSet();
  private final boolean m_addBraces;

  protected final MutableAttributeSet normal = new SimpleAttributeSet();

  public SyntaxDocument(final boolean addBraces) {
    doc = this;

    m_addBraces = addBraces;

    rootElement = doc.getDefaultRootElement();
    putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

    StyleConstants.setForeground(normal, Color.black);
    StyleConstants.setFontSize(normal, DEFAULT_FONT_SIZE);

    StyleConstants.setForeground(comment, Color.gray);
    StyleConstants.setItalic(comment, true);
    StyleConstants.setFontSize(comment, DEFAULT_FONT_SIZE);

    StyleConstants.setForeground(keyword, Color.blue.darker());
    StyleConstants.setFontSize(keyword, DEFAULT_FONT_SIZE);

    StyleConstants.setForeground(quote, Color.red);
    StyleConstants.setFontSize(quote, DEFAULT_FONT_SIZE);

    StyleConstants.setForeground(type, Color.PINK.darker());
    StyleConstants.setFontSize(type, DEFAULT_FONT_SIZE);

    StyleConstants.setForeground(number, Color.green.darker());
    StyleConstants.setFontSize(number, DEFAULT_FONT_SIZE);

    StyleConstants.setForeground(constant, Color.red.darker().darker());
    StyleConstants.setFontSize(constant, DEFAULT_FONT_SIZE);
  }

  /*
   * Parse the line to determine the appropriate highlighting
   */
  private void applyHighlighting(final String content, final int line) {
    final int startOffset = rootElement.getElement(line).getStartOffset();
    int endOffset = rootElement.getElement(line).getEndOffset() - 1;

    final int lineLength = endOffset - startOffset;
    final int contentLength = content.length();

    if (endOffset >= contentLength) {
      endOffset = contentLength - 1;
    }

    // check for multi line comments
    // (always set the comment attribute for the entire line)

    if (endingMultiLineComment(content, startOffset, endOffset) || isMultiLineComment()
        || startingMultiLineComment(content, startOffset, endOffset)) {
      doc.setCharacterAttributes(startOffset, (endOffset - startOffset) + 1, comment, false);
      return;
    }

    // set normal attributes for the line

    doc.setCharacterAttributes(startOffset, lineLength, normal, true);

    // check for single line comment

    final int index = content.indexOf(getSingleLineDelimiter(), startOffset);

    if ((index > -1) && (index < endOffset)) {
      doc.setCharacterAttributes(index, (endOffset - index) + 1, comment, false);
      endOffset = index - 1;
    }

    // check for tokens

    checkForTokens(content, startOffset, endOffset);
  }

  /*
   * Parse the line for tokens to highlight
   */
  private void checkForTokens(final String content, int startOffset, final int endOffset) {
    while (startOffset <= endOffset) {
      // skip the delimiters to find the start of a new token

      while (isDelimiter(content.substring(startOffset, startOffset + 1))) {
        if (startOffset < endOffset) {
          startOffset++;
        } else {
          return;
        }
      }

      // Extract and process the entire token

      if (isQuoteDelimiter(content.substring(startOffset, startOffset + 1))) {
        startOffset = getQuoteToken(content, startOffset, endOffset);
      } else {
        startOffset = getOtherToken(content, startOffset, endOffset);
      }
    }
  }

  /*
   * Highlight comment lines to matching end delimiter
   */
  private void commentLinesAfter(final String content, final int line) {
    final int offset = rootElement.getElement(line).getEndOffset();

    // End of comment not found, nothing to do

    final int endDelimiter = indexOf(content, getEndDelimiter(), offset);

    if (endDelimiter < 0) {
      return;
    }

    // Matching start/end of comment found, comment the lines

    final int startDelimiter = lastIndexOf(content, getStartDelimiter(), endDelimiter);

    if ((startDelimiter < 0) || (startDelimiter <= offset)) {
      doc.setCharacterAttributes(offset, (endDelimiter - offset) + 1, comment, false);
    }
  }

  /*
   * Highlight lines when a multi line comment is still 'open' (ie. matching end delimiter has not
   * yet been encountered)
   */
  private boolean commentLinesBefore(final String content, final int line) {
    final int offset = rootElement.getElement(line).getStartOffset();

    // Start of comment not found, nothing to do

    final int startDelimiter = lastIndexOf(content, getStartDelimiter(), offset - 2);

    if (startDelimiter < 0) {
      return false;
    }

    // Matching start/end of comment found, nothing to do

    final int endDelimiter = indexOf(content, getEndDelimiter(), startDelimiter);

    if ((endDelimiter < offset) & (endDelimiter != -1)) {
      return false;
    }

    // End of comment not found, highlight the lines

    doc.setCharacterAttributes(startDelimiter, (offset - startDelimiter) + 1, comment, false);
    return true;
  }

  /*
   * Does this line contain the end delimiter
   */
  private boolean endingMultiLineComment(final String content, final int startOffset,
      final int endOffset) {
    final int index = indexOf(content, getEndDelimiter(), startOffset);

    if ((index < 0) || (index > endOffset)) {
      return false;
    } else {
      setMultiLineComment(false);
      return true;
    }
  }

  private String getLine(final String content, final int offset) {
    final int line = rootElement.getElementIndex(offset);
    final Element lineElement = rootElement.getElement(line);
    final int start = lineElement.getStartOffset();
    final int end = lineElement.getEndOffset();
    return content.substring(start, end - 1);
  }

  private int getOtherToken(final String content, final int startOffset, final int endOffset) {
    int endOfToken = startOffset + 1;

    while (endOfToken <= endOffset) {
      if (isDelimiter(content.substring(endOfToken, endOfToken + 1))) {
        break;
      }

      endOfToken++;
    }

    final String token = content.substring(startOffset, endOfToken);

    if (isKeyword(token)) {
      doc.setCharacterAttributes(startOffset, endOfToken - startOffset, keyword, false);
    } else if (isType(token)) {
      doc.setCharacterAttributes(startOffset, endOfToken - startOffset, type, false);
    } else if (isConstant(token)) {
      doc.setCharacterAttributes(startOffset, endOfToken - startOffset, constant, false);
    } else if (Convert.isDecString(token)) {
      doc.setCharacterAttributes(startOffset, endOfToken - startOffset, number, false);
    }

    return endOfToken + 1;
  }

  private int getQuoteToken(final String content, final int startOffset, final int endOffset) {
    final String quoteDelimiter = content.substring(startOffset, startOffset + 1);
    final String escapeString = getEscapeString(quoteDelimiter);

    int index;
    int endOfQuote = startOffset;

    // skip over the escape quotes in this quote

    index = content.indexOf(escapeString, endOfQuote + 1);

    while ((index > -1) && (index < endOffset)) {
      endOfQuote = index + 1;
      index = content.indexOf(escapeString, endOfQuote);
    }

    // now find the matching delimiter

    index = content.indexOf(quoteDelimiter, endOfQuote + 1);

    if ((index < 0) || (index > endOffset)) {
      endOfQuote = endOffset;
    } else {
      endOfQuote = index;
    }

    doc.setCharacterAttributes(startOffset, (endOfQuote - startOffset) + 1, quote, false);

    return endOfQuote + 1;
  }

  /*
   * Highlight lines to start or end delimiter
   */
  private void highlightLinesAfter(final String content, final int line) {
    final int offset = rootElement.getElement(line).getEndOffset();

    // Start/End delimiter not found, nothing to do

    int startDelimiter = indexOf(content, getStartDelimiter(), offset);
    int endDelimiter = indexOf(content, getEndDelimiter(), offset);

    if (startDelimiter < 0) {
      startDelimiter = content.length();
    }

    if (endDelimiter < 0) {
      endDelimiter = content.length();
    }

    final int delimiter = Math.min(startDelimiter, endDelimiter);

    if (delimiter < offset) {
      return;
    }

    // Start/End delimiter found, reapply highlighting

    final int endLine = rootElement.getElementIndex(delimiter);

    for (int i = line + 1; i < endLine; i++) {
      final Element branch = rootElement.getElement(i);
      final Element leaf = doc.getCharacterElement(branch.getStartOffset());
      final AttributeSet as = leaf.getAttributes();

      if (as.isEqual(comment)) {
        applyHighlighting(content, i);
      }
    }
  }

  /*
   * Assume the needle will the found at the start/end of the line
   */
  private int indexOf(final String content, final String needle, int offset) {
    int index;

    while ((index = content.indexOf(needle, offset)) != -1) {
      final String text = getLine(content, index).trim();

      if (text.startsWith(needle) || text.endsWith(needle)) {
        break;
      } else {
        offset = index + 1;
      }
    }

    return index;
  }

  /*
   * We have found a start delimiter and are still searching for the end delimiter
   */
  private boolean isMultiLineComment() {
    return multiLineComment;
  }

  /*
   * Assume the needle will the found at the start/end of the line
   */
  private int lastIndexOf(final String content, final String needle, int offset) {
    int index;

    while ((index = content.lastIndexOf(needle, offset)) != -1) {
      final String text = getLine(content, index).trim();

      if (text.startsWith(needle) || text.endsWith(needle)) {
        break;
      } else {
        offset = index - 1;
      }
    }

    return index;
  }

  private void setMultiLineComment(final boolean value) {
    multiLineComment = value;
  }

  /*
   * Does this line contain the start delimiter
   */
  private boolean startingMultiLineComment(final String content, final int startOffset,
      final int endOffset) {
    final int index = indexOf(content, getStartDelimiter(), startOffset);

    if ((index < 0) || (index > endOffset)) {
      return false;
    } else {
      setMultiLineComment(true);
      return true;
    }
  }

  protected String addMatchingBrace(final int offset) throws BadLocationException {
    final StringBuffer whiteSpace = new StringBuffer();
    final int line = rootElement.getElementIndex(offset);
    int i = rootElement.getElement(line).getStartOffset();

    while (true) {
      final String temp = doc.getText(i, 1);

      if (temp.equals(" ") || temp.equals("\t")) {
        whiteSpace.append(temp);
        i++;
      } else {
        break;
      }
    }

    return "{\n" + whiteSpace.toString() + "\t\n" + whiteSpace.toString() + "}";
  }

  /*
   * Override for other languages
   */
  protected abstract String getEndDelimiter();

  /*
   * Override for other languages
   */
  protected abstract String getEscapeString(final String quoteDelimiter);

  /*
   * Override for other languages
   */
  protected abstract String getSingleLineDelimiter();

  /*
   * Override for other languages
   */
  protected abstract String getStartDelimiter();

  protected abstract HashSet<String> getTabCompletionWords();

  protected abstract boolean isConstant(final String token);

  /*
   * Override for other languages
   */
  protected abstract boolean isKeyword(final String token);

  /*
   * Override for other languages
   */
  protected abstract boolean isQuoteDelimiter(final String character);

  protected abstract boolean isType(final String token);

  /*
   * Override to apply syntax highlighting after the document has been updated
   */
  @Override
  public void insertString(final int offset, String str, final AttributeSet a)
      throws BadLocationException {
    if (m_addBraces && str.equals("{")) {
      str = addMatchingBrace(offset);
    }

    super.insertString(offset, str, a);
    processChangedLines(offset, str.length());
  }

  /*
   * Override for other languages
   */
  public abstract boolean isDelimiter(final String character);

  /*
   * Determine how many lines have been changed, then apply highlighting to each line
   */
  public void processChangedLines(final int offset, final int length) throws BadLocationException {
    final String content = doc.getText(0, doc.getLength());

    // The lines affected by the latest document update

    final int startLine = rootElement.getElementIndex(offset);
    final int endLine = rootElement.getElementIndex(offset + length);

    // Make sure all comment lines prior to the start line are commented
    // and determine if the start line is still in a multi line comment

    setMultiLineComment(commentLinesBefore(content, startLine));

    // Do the actual highlighting

    for (int i = startLine; i <= endLine; i++) {
      applyHighlighting(content, i);
    }

    // Resolve highlighting to the next end multi line delimiter

    if (isMultiLineComment()) {
      commentLinesAfter(content, endLine);
    } else {
      highlightLinesAfter(content, endLine);
    }
  }

  /*
   * Override to apply syntax highlighting after the document has been updated
   */
  @Override
  public void remove(final int offset, final int length) throws BadLocationException {
    super.remove(offset, length);
    processChangedLines(offset, 0);
  }
}
