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

/*
 * A small example to illustrate the proper use of the CodeDisplay component.
 */
package com.google.security.zynamics.zylib.gui.CodeDisplay;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * The code display is a large array of rows to be displayed. Each row is an array of fields of
 * length "Columns". Each field is an array of lines.
 */
public class CodeDisplayModelExample implements ICodeDisplayModel {
  // In this simple example we keep 10000 rows with 3 columns each. The first
  // two columns always contain one line each, the third column contains two
  // lines.
  private ArrayList< // Rows
      ArrayList< // Fields
          ArrayList<String>>> data = new ArrayList<>();
  private int totalNumberOfLines = 0;

  private Font fontColumnA = new Font(Font.MONOSPACED, Font.BOLD, 12);
  private Font fontColumnB = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  private Font fontColumnC = new Font(Font.MONOSPACED, Font.ITALIC, 12);

  public CodeDisplayModelExample() {
    for (int rows = 0; rows < 10000; rows++) {
      // Create a new row:
      ArrayList<String> field1 = new ArrayList<>();
      ArrayList<String> field2 = new ArrayList<>();
      ArrayList<String> field3 = new ArrayList<>();
      field1.add(String.format("* %d *", rows));
      field2.add(String.format("Code for line %d", rows));
      field3.add(String.format("First comment in line %d", rows));
      field3.add(String.format("Second comment in line %d", rows));

      totalNumberOfLines += 2;

      ArrayList<ArrayList<String>> fields = new ArrayList<>();
      fields.add(field1);
      fields.add(field2);
      fields.add(field3);
      data.add(fields);
    }
  }

  @Override
  public int getNumberOfRows() {
    return data.size();
  }

  @Override
  public int getTotalNumberOfLines() {
    return totalNumberOfLines;
  }

  @Override
  public int getColumnWidthInCharacters(int columnIndex) {
    // This simple example arbitrarily chooses to have columns of width 15, 40, and 40.
    switch (columnIndex) {
      case 0:
        return 15;
      case 1:
        return 40;
      case 2:
        return 40;
    }
    throw new ArrayIndexOutOfBoundsException();
  }

  @Override
  public int getTotalWidthInCharacters() {
    int sum = 0;
    for (int index = 0; index < getNumberOfColumns(); index++) {
      sum += getColumnWidthInCharacters(index);
    }
    return sum;
  }

  @Override
  public int getMaximumLinesForRow(int rowIndex) {
    int max = 0;
    for (int columnIndex = 0; columnIndex < getNumberOfColumns(); columnIndex++) {
      max = Math.max(data.get(rowIndex).get(columnIndex).size(), max);
    }
    return max;
  }

  @Override
  public int getNumberOfColumns() {
    return 3;
  }

  @Override
  public String getColumnName(int columnIndex) {
    switch (columnIndex) {
      case 0:
        return "Line:";
      case 1:
        return "Code:";
      case 2:
        return "Comment:";
    }
    throw new ArrayIndexOutOfBoundsException();
  }

  public static String padRight(String s, int n) {
    return String.format("%1$-" + n + "s", s);
  }

  @Override
  public FormattedCharacterBuffer getLineFormatted(int rowIndex, int columnIndex, int lineIndex) {
    ArrayList<String> field = data.get(rowIndex).get(columnIndex);
    String data;
    if (lineIndex >= field.size()) {
      // Return an empty string of the right size.
      data = padRight("", getColumnWidthInCharacters(columnIndex));
    } else {
      data = padRight(field.get(lineIndex), getColumnWidthInCharacters(columnIndex));
    }
    data = data.substring(0, Math.min(data.length(), getColumnWidthInCharacters(columnIndex)));
    switch (columnIndex) {
      case 0:
        return new FormattedCharacterBuffer(data, fontColumnA, Color.BLACK, Color.LIGHT_GRAY);
      case 1:
        return new FormattedCharacterBuffer(data, fontColumnB, Color.WHITE, Color.DARK_GRAY);
      case 2:
        return new FormattedCharacterBuffer(data, fontColumnC, Color.LIGHT_GRAY, Color.BLACK);
    }
    return null;
  }

  @Override
  public boolean canHaveCaret(CodeDisplayCoordinate coordinate) {
    return coordinate.getColumn() == 0 ? false : true;
  }

  @Override
  public boolean isEditable(CodeDisplayCoordinate coordinate) {
    return coordinate.getColumn() == 2 ? true : false;
  }

  @Override
  public void keyPressedOrTyped(CodeDisplayCoordinate coordinate, KeyEvent event) {
    if (!event.isActionKey()) {
      switch (event.getKeyCode()) {
        case KeyEvent.VK_ENTER:
          break;
        default:
          String stringToEdit =
              data.get(coordinate.getRow()).get(coordinate.getColumn()).get(coordinate.getLine());
          if (stringToEdit.length() < coordinate.getFieldIndex()) {
            stringToEdit = padRight(stringToEdit, coordinate.getFieldIndex());
          }
          String newString = stringToEdit.substring(0, coordinate.getFieldIndex())
              + event.getKeyChar() + stringToEdit.substring(coordinate.getFieldIndex());
          data.get(coordinate.getRow()).get(coordinate.getColumn())
              .set(coordinate.getLine(), newString);
          coordinate.setFieldIndex(coordinate.getFieldIndex() + 1);
          break;
      }
    } else {
      switch (event.getKeyCode()) {
        case KeyEvent.VK_DOWN:
          break;
        case KeyEvent.VK_UP:
          break;
        case KeyEvent.VK_LEFT:
          coordinate.setFieldIndex(coordinate.getFieldIndex() - 1);
          break;
        case KeyEvent.VK_RIGHT:
          coordinate.setFieldIndex(coordinate.getFieldIndex() + 1);
          break;
      }
    }
  }

  @Override
  public boolean hasHeaderRow() {
    return false;
  }

  @Override
  public FormattedCharacterBuffer getHeader(int columnIndex) {
    // Does not need to return anything - no header row required.
    return null;
  }
}
