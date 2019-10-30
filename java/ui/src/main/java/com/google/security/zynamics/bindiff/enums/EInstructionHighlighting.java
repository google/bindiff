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

  public static final int ENUM_ENTRY_COUNT = 15;

  public static int convertExporterOrdinal(final int exporterOrdinal) {
    // MNEMONIC => Exporter = 0;
    // SYMBOL => Exporter = 1;
    // IMMEDIATE => Exporter = 2 & 3;
    // OPERATOR => Exporter = 4
    // REGISTER => Exporter = 5;
    // SIZEPREFIX => Exporter = 6;
    // DEREFERENCE => Exporter = 7;
    // OPERANDSEPARATOR => Exporter = 8;
    // STACKVAR => Exporter = 9;
    // GLOBALVAR => Exporter = 10;
    // JUMPLABEL => Exporter = 11;
    // FUNCTION => Exporter = 12;

    switch (exporterOrdinal) {
      case 0:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_MNEMONIC);
      case 1:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_SYMBOL);
      case 2:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_IMMEDIATE);
      case 3:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_IMMEDIATE);
      case 4:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_OPERATOR);
      case 5:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_REGISTER);
      case 6:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_SIZEPREFIX);
      case 7:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_DEREFERENCE);
      case 8:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_NEWOPERAND_COMMA);
      case 9:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_STACKVARIABLE);
      case 10:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_GLOBALVARIABLE);
      case 11:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_JUMPLABEL);
      case 12:
        return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_FUNCTION);
      default: // fall out
    }

    return EInstructionHighlighting.getOrdinal(EInstructionHighlighting.TYPE_DEFAULT);
  }

  public static Color getColor(final EInstructionHighlighting operandHighlighting) {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    final ThemeConfigItem colors = config.getThemeSettings();

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

    return config.getThemeSettings().getDefaultColor();
  }

  public static Color getColor(final int operandHighlighting) {
    return getColor(getEnum(operandHighlighting));
  }

  public static EInstructionHighlighting getEnum(final int operandHighlighting) {
    switch (operandHighlighting) {
      case 0:
        return TYPE_DEFAULT;
      case 1:
        return TYPE_ADDRESS;
      case 2:
        return TYPE_MNEMONIC;
      case 3:
        return TYPE_SYMBOL;
      case 4:
        return TYPE_IMMEDIATE;
      case 5:
        return TYPE_OPERATOR;
      case 6:
        return TYPE_REGISTER;
      case 7:
        return TYPE_SIZEPREFIX;
      case 8:
        return TYPE_DEREFERENCE;
      case 9:
        return TYPE_NEWOPERAND_COMMA;
      case 10:
        return TYPE_STACKVARIABLE;
      case 11:
        return TYPE_GLOBALVARIABLE;
      case 12:
        return TYPE_JUMPLABEL;
      case 13:
        return TYPE_FUNCTION;
      case 14:
        return TYPE_COMMENT;
      default: // fall out
    }
    throw new IllegalStateException("Unknown operand highlighting type.");
  }

  public static int getOrdinal(final EInstructionHighlighting operandHighlighting) {
    switch (operandHighlighting) {
      case TYPE_DEFAULT:
        return 0;
      case TYPE_ADDRESS:
        return 1;
      case TYPE_MNEMONIC:
        return 2;
      case TYPE_SYMBOL:
        return 3;
      case TYPE_IMMEDIATE:
        return 4;
      case TYPE_OPERATOR:
        return 5;
      case TYPE_REGISTER:
        return 6;
      case TYPE_SIZEPREFIX:
        return 7;
      case TYPE_DEREFERENCE:
        return 8;
      case TYPE_NEWOPERAND_COMMA:
        return 9;
      case TYPE_STACKVARIABLE:
        return 10;
      case TYPE_GLOBALVARIABLE:
        return 11;
      case TYPE_JUMPLABEL:
        return 12;
      case TYPE_FUNCTION:
        return 13;
      case TYPE_COMMENT:
        return 14;
    }

    throw new IllegalStateException("Unknown operand highlighting type.");
  }

  public static int getSize() {
    return ENUM_ENTRY_COUNT;
  }
}
