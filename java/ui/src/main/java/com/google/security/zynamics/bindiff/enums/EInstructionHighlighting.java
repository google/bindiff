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

package com.google.security.zynamics.bindiff.enums;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.ThemeConfigItem;
import java.awt.Color;

public enum EInstructionHighlighting {
  TYPE_DEFAULT,
  TYPE_ADDRESS,
  TYPE_MNEMONIC,
  TYPE_SYMBOL,
  TYPE_IMMEDIATE,
  TYPE_OPERATOR,
  TYPE_REGISTER,
  TYPE_SIZEPREFIX,
  TYPE_DEREFERENCE,
  TYPE_NEWOPERAND_COMMA,
  TYPE_STACKVARIABLE,
  TYPE_GLOBALVARIABLE,
  TYPE_JUMPLABEL,
  TYPE_FUNCTION,
  TYPE_COMMENT;

  private static final EInstructionHighlighting[] values = values();

  public static Color getColor(int ordinal) {
    return values[ordinal].getColor();
  }

  private static Color getColor(final EInstructionHighlighting operandHighlighting) {
    final ThemeConfigItem colors = BinDiffConfig.getInstance().getThemeSettings();
    switch (operandHighlighting) {
      case TYPE_DEFAULT:
        return colors.getDefaultColor();
      case TYPE_ADDRESS:
        return colors.getAddressColor();
      case TYPE_MNEMONIC:
        return colors.getMnemonicColor();
      case TYPE_SYMBOL:
        return colors.getSymbolColor();
      case TYPE_IMMEDIATE:
        return colors.getImmediateColor();
      case TYPE_OPERATOR:
        return colors.getOperatorColor();
      case TYPE_REGISTER:
        return colors.getRegisterColor();
      case TYPE_SIZEPREFIX:
        return colors.getSizePrefixColor();
      case TYPE_DEREFERENCE:
        return colors.getDereferenceColor();
      case TYPE_NEWOPERAND_COMMA:
        return colors.getOperatorSeparatorColor();
      case TYPE_STACKVARIABLE:
        return colors.getStackVariableColor();
      case TYPE_GLOBALVARIABLE:
        return colors.getGlobalVariableColor();
      case TYPE_JUMPLABEL:
        return colors.getJumpLabelColor();
      case TYPE_FUNCTION:
        return colors.getFunctionColor();
      case TYPE_COMMENT:
        return colors.getCommentColor();
    }
    return colors.getDefaultColor();
  }

  public Color getColor() {
    return getColor(this);
  }

  public static boolean validOrdinal(int ordinal) {
    return ordinal >= 0 && ordinal < values.length;
  }
}
