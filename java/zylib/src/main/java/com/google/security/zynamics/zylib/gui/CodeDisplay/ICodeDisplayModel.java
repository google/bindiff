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

import java.awt.event.KeyEvent;

/**
 * The interface for the data model for the JCodeDisplay component. The data model is responsible
 * for keeping the data (clearly), but also for providing a FormattedCharacterBuffer for each
 * row/column on request.
 */
public interface ICodeDisplayModel {
  /** Returns total number of rows in the data model. */
  public int getNumberOfRows();

  /**
   * Returns total number of lines in the data model, which are distinct from rows. An individual
   * row in the component can have multiple lines -- think of a disassembled instruction and an
   * associated multi-line comment for example. Because a single "row" (corresponding in the example
   * to a disassembled instruction at one address) can have multiple lines, but the UI needs to know
   * the total size of everything, this method needs to return the sum of all lines.
   */
  public int getTotalNumberOfLines();

  /** Returns total width of the model. */
  public int getTotalWidthInCharacters();

  public int getNumberOfColumns();

  /** Retrieves the number of characters in a given column. */
  public int getColumnWidthInCharacters(int columnIndex);

  /** Returns the name of the column. */
  public String getColumnName(int columnIndex);

  /** Returns the maximum number of text lines in a given row. */
  public int getMaximumLinesForRow(int rowIndex);

  /** Gets a particular field at row/column/line to be rendered. */
  public FormattedCharacterBuffer getLineFormatted(int rowIndex, int columnIndex, int lineIndex);

  /** Is the given coordinate a legitimate target to move the Caret to? */
  public boolean canHaveCaret(CodeDisplayCoordinate coordinate);

  /** Can the content below the given coordinate be edited? */
  public boolean isEditable(CodeDisplayCoordinate coordinate);

  /**
   * Handles the keyboard events forwarded to the model.
   *
   * <p>Warning to people used to "regular" Swing key handling: Swing splits keyboard events into
   * "generate printable characters" and "are just function keys", where the former generate
   * KEY_TYPED events, the latter generate KEY_PRESSED events. In the code here, this distinction
   * does not make quite the same sense - ENTER, BACK_SPACE etc generate regular KEY_TYPED events,
   * but are functionally much closer to HOME/END keys (e.g. they trigger some extra code that
   * "normal" text wouldn't trigger. For this reason, both events are thrown into the same handler
   * by the CodeDisplay - and this one handler then sorts them out (receiving a VK_UNDEFINED event
   * then means the code is getting a KEY_TYPED event). This is admittedly inelegant, and should
   * quite possibly be refactored in the future.
   */
  public void keyPressedOrTyped(CodeDisplayCoordinate coordinate, KeyEvent event);

  /** Should a header row be displayed above all other rows? */
  public boolean hasHeaderRow();

  /** Provides the contents of the header row on request. */
  public FormattedCharacterBuffer getHeader(int columnIndex);
}
