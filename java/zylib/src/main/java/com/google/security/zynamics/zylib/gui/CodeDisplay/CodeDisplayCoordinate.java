package com.google.security.zynamics.zylib.gui.CodeDisplay;

import com.google.common.base.Preconditions;

/**
 * A simple PoD class that encapsulates a caret position inside a code display (which is referred to
 * by row index, column index, line index, field index).
 */
public class CodeDisplayCoordinate {
  private int rowIndex;
  private int columnIndex;
  private int lineIndex;
  private int fieldIndex;

  public CodeDisplayCoordinate(int row, int line, int column, int indexIntoField) {
    rowIndex = row;
    lineIndex = line;
    columnIndex = column;
    fieldIndex = indexIntoField;
  }

  CodeDisplayCoordinate(CodeDisplayCoordinate coordinate) {
    rowIndex = coordinate.getRow();
    lineIndex = coordinate.getLine();
    columnIndex = coordinate.getColumn();
    fieldIndex = coordinate.getFieldIndex();
  }

  public int getRow() {
    return rowIndex;
  }

  public int getColumn() {
    return columnIndex;
  }

  public int getLine() {
    return lineIndex;
  }

  public int getFieldIndex() {
    return fieldIndex;
  }

  public void setRow(int row) {
    Preconditions.checkArgument(row >= 0, "Row should be >= 0: %s", row);
    rowIndex = row;
  }

  public void setColumn(int column) {
    Preconditions.checkArgument(column >= 0, "Column should be >= 0: %s",
        column);
    columnIndex = column;
  }

  public void setLine(int line) {
    Preconditions.checkArgument(line >= 0, "Line should be >= 0: %s", line);
    lineIndex = line;
  }

  public void setFieldIndex(int index) {
    Preconditions.checkArgument(index >= 0, "Index should be >= 0: %s",
        index);
    fieldIndex = index;
  }

  @Override
  public String toString() {
    return "Row: " + Integer.toString(rowIndex) + ", Column: " + Integer.toString(columnIndex)
        + ", Line: " + Integer.toString(lineIndex) + "," + Integer.toString(fieldIndex);
  }
}
