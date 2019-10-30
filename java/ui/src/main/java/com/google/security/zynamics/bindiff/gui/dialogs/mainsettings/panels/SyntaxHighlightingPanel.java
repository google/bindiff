package com.google.security.zynamics.bindiff.gui.dialogs.mainsettings.panels;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.ThemeConfigItem;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.ColorPanel.ColorPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class SyntaxHighlightingPanel extends JPanel {
  private static final int COLORPANEL_WIDTH = 200;
  private static final int COLORPANEL_HEIGHT = 25;

  private static final int NUMMBER_OF_ROWS = 15;

  private ColorPanel defaultColor;
  private ColorPanel addressColor;
  private ColorPanel mnemonicColor;
  private ColorPanel symbolColor;
  private ColorPanel immediateColor;
  private ColorPanel operatorColor;
  private ColorPanel registerColor;
  private ColorPanel sizePrefixColor;
  private ColorPanel dereferenceColor;
  private ColorPanel operandSeparatorColor;
  private ColorPanel stackVariableColor;
  private ColorPanel globalVariableColor;
  private ColorPanel jumpLabelColor;
  private ColorPanel functionColor;
  private ColorPanel commentColor;

  Set<Color> defaultColors = new HashSet<>();

  public SyntaxHighlightingPanel() {
    super(new BorderLayout());

    init();
  }

  private JPanel createInstructionColorsPanel() {
    final ThemeConfigItem settings = BinDiffConfig.getInstance().getThemeSettings();

    final JPanel panel = new JPanel(new GridLayout(NUMMBER_OF_ROWS, 1, 5, 5));
    panel.setBorder(new TitledBorder("Syntax Highlighting"));

    defaultColors.add(settings.getDefaultColor());
    defaultColors.add(settings.getAddressColor());
    defaultColors.add(settings.getMnemonicColor());
    defaultColors.add(settings.getRegisterColor());
    defaultColors.add(settings.getFunctionColor());
    defaultColors.add(settings.getImmediateColor());
    defaultColors.add(settings.getGlobalVariableColor());
    defaultColors.add(settings.getStackVariableColor());
    defaultColors.add(settings.getJumpLabelColor());
    defaultColors.add(settings.getDereferenceColor());
    defaultColors.add(settings.getOperatorSeparatorColor());
    defaultColors.add(settings.getSymbolColor());
    defaultColors.add(settings.getSizePrefixColor());
    defaultColors.add(settings.getDefaultColor());
    defaultColors.add(settings.getDefaultColor());
    defaultColors.add(settings.getCommentColor());

    defaultColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Default color:",
            new ColorPanel(settings.getDefaultColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    addressColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Address color:",
            new ColorPanel(settings.getAddressColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    mnemonicColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Mnemonic color:",
            new ColorPanel(settings.getMnemonicColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    registerColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Register color:",
            new ColorPanel(settings.getRegisterColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    functionColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Function color:",
            new ColorPanel(settings.getFunctionColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    immediateColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Integer immediate color:",
            new ColorPanel(settings.getImmediateColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    globalVariableColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Global variable color:",
            new ColorPanel(settings.getGlobalVariableColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    stackVariableColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Stack variable color:",
            new ColorPanel(settings.getStackVariableColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    jumpLabelColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Jump label color:",
            new ColorPanel(settings.getJumpLabelColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    operatorColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Operator color:",
            new ColorPanel(settings.getOperatorColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    dereferenceColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Dereference color:",
            new ColorPanel(settings.getDereferenceColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    operandSeparatorColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Operand separator color:",
            new ColorPanel(settings.getOperatorSeparatorColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    symbolColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Symbol color:",
            new ColorPanel(settings.getSymbolColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    sizePrefixColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Size prefix color:",
            new ColorPanel(settings.getSizePrefixColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);
    commentColor =
        GuiUtils.createHorizontalNamedComponent(
            panel,
            "Comment color:",
            new ColorPanel(settings.getCommentColor(), true, true, defaultColors),
            COLORPANEL_WIDTH,
            COLORPANEL_HEIGHT,
            false);

    return panel;
  }

  private void init() {
    final JPanel panel = new JPanel(new BorderLayout());

    final JPanel innerMainPanel = new JPanel(new GridBagLayout());

    final GridBagConstraints cbc = new GridBagConstraints();

    cbc.gridx = 0;
    cbc.gridy = 0;
    cbc.anchor = GridBagConstraints.FIRST_LINE_START;
    cbc.weightx = 1;
    cbc.fill = GridBagConstraints.HORIZONTAL;

    innerMainPanel.add(createInstructionColorsPanel(), cbc);

    panel.add(innerMainPanel, BorderLayout.NORTH);

    add(new JScrollPane(panel));
  }

  public Color getAddressColor() {
    return addressColor.getColor();
  }

  public Color getCommentColor() {
    return commentColor.getColor();
  }

  public Color getDefaultColor() {
    return defaultColor.getColor();
  }

  public Color getDereferenceColor() {
    return dereferenceColor.getColor();
  }

  public Color getFunctionColor() {
    return functionColor.getColor();
  }

  public Color getGlobalVariableColor() {
    return globalVariableColor.getColor();
  }

  public Color getImmediateColor() {
    return immediateColor.getColor();
  }

  public Color getJumpLabelColor() {
    return jumpLabelColor.getColor();
  }

  public Color getMnemonicColor() {
    return mnemonicColor.getColor();
  }

  public Color getOperandSeparatorColor() {
    return operandSeparatorColor.getColor();
  }

  public Color getOperatorColor() {
    return operatorColor.getColor();
  }

  public Color getRegisterColor() {
    return registerColor.getColor();
  }

  public Color getSizePrefixColor() {
    return sizePrefixColor.getColor();
  }

  public Color getStackVariableColor() {
    return stackVariableColor.getColor();
  }

  public Color getSymbolColor() {
    return symbolColor.getColor();
  }

  public void setCurrentValues() {
    final ThemeConfigItem settings = BinDiffConfig.getInstance().getThemeSettings();

    defaultColor.setColor(settings.getDefaultColor());
    addressColor.setColor(settings.getAddressColor());
    mnemonicColor.setColor(settings.getMnemonicColor());
    registerColor.setColor(settings.getRegisterColor());
    functionColor.setColor(settings.getFunctionColor());
    immediateColor.setColor(settings.getImmediateColor());
    globalVariableColor.setColor(settings.getGlobalVariableColor());
    stackVariableColor.setColor(settings.getStackVariableColor());
    jumpLabelColor.setColor(settings.getJumpLabelColor());
    operatorColor.setColor(settings.getOperatorColor());
    dereferenceColor.setColor(settings.getDereferenceColor());
    operandSeparatorColor.setColor(settings.getOperatorSeparatorColor());
    symbolColor.setColor(settings.getSymbolColor());
    sizePrefixColor.setColor(settings.getSizePrefixColor());
    commentColor.setColor(settings.getCommentColor());
  }
}
