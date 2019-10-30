package com.google.security.zynamics.bindiff.config;

import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPathException;
import org.w3c.dom.Document;

/** Sets and gets the syntax highlighting color value and font settings for the UI. */
public class ThemeConfigItem extends ConfigItem {

  private static final String USE_THEME = "/bindiff/preferences/use-theme/@name";
  private static final String USE_THEME_DEFAULT = "Google Material";
  private String useTheme = USE_THEME_DEFAULT;

  // Read-only setting that allows to create themes based on other themes.
  private static final String BASED_ON_FMT = "/bindiff/theme[@name='%s']/@based-on";
  private String basedOn;

  private static final String SIMILARITY_COLOR_RAMP_FMT =
      "/bindiff/theme[@name='%s']/ramp[@for='similarity']/c/@v";
  private static final Color[] SIMILARITY_COLOR_RAMP_DEFAULT =
      new Color[] { // See //third_party/zynamics/bindiff/ida/ui.cc.
        new Color(0xff5722), new Color(0xff5722), new Color(0xff5922), new Color(0xff5922),
            new Color(0xff5a22), new Color(0xff5b21), new Color(0xff5b21),
        new Color(0xff5c21), new Color(0xff5d21), new Color(0xff5e21), new Color(0xff5f21),
            new Color(0xff5f21), new Color(0xff5f21), new Color(0xff6120),
        new Color(0xff6120), new Color(0xff6220), new Color(0xff6220), new Color(0xff6320),
            new Color(0xff6420), new Color(0xff6520), new Color(0xff661f),
        new Color(0xff671f), new Color(0xff661f), new Color(0xff671f), new Color(0xff681f),
            new Color(0xff691f), new Color(0xff691f), new Color(0xff6a1e),
        new Color(0xff6b1e), new Color(0xff6c1e), new Color(0xfe6c1e), new Color(0xfe6d1e),
            new Color(0xfe6f1e), new Color(0xfe6f1e), new Color(0xfe701d),
        new Color(0xfe711d), new Color(0xfe701d), new Color(0xfe721d), new Color(0xfe721d),
            new Color(0xfe731d), new Color(0xfe731d), new Color(0xfe741d),
        new Color(0xfd751c), new Color(0xfe751c), new Color(0xfd761c), new Color(0xfd771c),
            new Color(0xfd771c), new Color(0xfd791b), new Color(0xfd791b),
        new Color(0xfd7a1b), new Color(0xfd7a1b), new Color(0xfd7a1b), new Color(0xfc7c1b),
            new Color(0xfc7d1b), new Color(0xfc7d1a), new Color(0xfc7e1a),
        new Color(0xfc7f1a), new Color(0xfc7f1a), new Color(0xfc7f1a), new Color(0xfb811a),
            new Color(0xfb8119), new Color(0xfb8119), new Color(0xfb8319),
        new Color(0xfb8219), new Color(0xfb8419), new Color(0xfb8419), new Color(0xfa8518),
            new Color(0xfa8618), new Color(0xfa8718), new Color(0xfa8718),
        new Color(0xfa8818), new Color(0xfa8718), new Color(0xf98a17), new Color(0xf98917),
            new Color(0xf98b17), new Color(0xf98a17), new Color(0xf98c17),
        new Color(0xf88d17), new Color(0xf88c17), new Color(0xf88d16), new Color(0xf88e16),
            new Color(0xf78f16), new Color(0xf79016), new Color(0xf79015),
        new Color(0xf79115), new Color(0xf79215), new Color(0xf69215), new Color(0xf69215),
            new Color(0xf69315), new Color(0xf69415), new Color(0xf59514),
        new Color(0xf59514), new Color(0xf59614), new Color(0xf49714), new Color(0xf49714),
            new Color(0xf49813), new Color(0xf49813), new Color(0xf49913),
        new Color(0xf39a13), new Color(0xf39a13), new Color(0xf39b13), new Color(0xf29c12),
            new Color(0xf29d12), new Color(0xf29d12), new Color(0xf19e12),
        new Color(0xf19e11), new Color(0xf19f11), new Color(0xf0a011), new Color(0xf1a011),
            new Color(0xf0a011), new Color(0xf0a111), new Color(0xefa210),
        new Color(0xefa210), new Color(0xefa410), new Color(0xeea410), new Color(0xeea510),
            new Color(0xeea50f), new Color(0xeda60f), new Color(0xeda70f),
        new Color(0xeda80f), new Color(0xeca90e), new Color(0xeca80e), new Color(0xeca90e),
            new Color(0xeba90e), new Color(0xebaa0e), new Color(0xebab0e),
        new Color(0xeaac0d), new Color(0xeaac0d), new Color(0xe9ad0d), new Color(0xe9ad0d),
            new Color(0xe8ae0c), new Color(0xe8af0c), new Color(0xe8af0c),
        new Color(0xe7b00b), new Color(0xe7b10b), new Color(0xe6b20b), new Color(0xe6b20b),
            new Color(0xe6b20b), new Color(0xe5b30a), new Color(0xe5b30a),
        new Color(0xe4b50a), new Color(0xe4b50a), new Color(0xe3b609), new Color(0xe3b709),
            new Color(0xe3b709), new Color(0xe2b709), new Color(0xe1b809),
        new Color(0xe1b908), new Color(0xe1ba08), new Color(0xe1b908), new Color(0xdfbb08),
            new Color(0xdfbb08), new Color(0xdebc07), new Color(0xdebc07),
        new Color(0xdebe07), new Color(0xdebd07), new Color(0xddbe07), new Color(0xddbe07),
            new Color(0xdbc006), new Color(0xdbc006), new Color(0xdac206),
        new Color(0xdac106), new Color(0xdac206), new Color(0xd9c205), new Color(0xd8c405),
            new Color(0xd8c405), new Color(0xd7c405), new Color(0xd7c504),
        new Color(0xd7c504), new Color(0xd6c604), new Color(0xd5c804), new Color(0xd5c704),
            new Color(0xd4c904), new Color(0xd3c903), new Color(0xd3ca03),
        new Color(0xd2cb03), new Color(0xd2ca03), new Color(0xd1cc03), new Color(0xd0cd03),
            new Color(0xd0cc03), new Color(0xd0cc03), new Color(0xcfcd02),
        new Color(0xcece02), new Color(0xcdcf02), new Color(0xcbd002), new Color(0xcbd102),
            new Color(0xcbd002), new Color(0xcad202), new Color(0xcad102),
        new Color(0xc9d301), new Color(0xc8d401), new Color(0xc7d401), new Color(0xc7d501),
            new Color(0xc5d601), new Color(0xc5d601), new Color(0xc4d701),
        new Color(0xc3d800), new Color(0xc3d700), new Color(0xc2d800), new Color(0xc2d800),
            new Color(0xc0da00), new Color(0xbfda00), new Color(0xbfda00),
        new Color(0xbedc00), new Color(0xbedb00), new Color(0xbcdd00), new Color(0xbbde00),
            new Color(0xbbdd00), new Color(0xbade00), new Color(0xbade00),
        new Color(0xb8e000), new Color(0xb7e100), new Color(0xb7e100), new Color(0xb5e100),
            new Color(0xb5e100), new Color(0xb4e300), new Color(0xb4e200),
        new Color(0xb2e400), new Color(0xb1e400), new Color(0xb1e400), new Color(0xafe600),
            new Color(0xaee600), new Color(0xade600), new Color(0xace800),
        new Color(0xace700), new Color(0xaae900), new Color(0xaae800), new Color(0xa8e900),
            new Color(0xa6eb00), new Color(0xa6ea00), new Color(0xa4eb00),
        new Color(0xa4ec00), new Color(0xa3ed00), new Color(0xa1ee00), new Color(0xa1ee00),
            new Color(0xa1ed00), new Color(0x9fee00), new Color(0x9def00),
        new Color(0x9def00), new Color(0x9bf000), new Color(0x98f200), new Color(0x98f200),
            new Color(0x96f300), new Color(0x96f300), new Color(0x94f301),
        new Color(0x94f401), new Color(0x92f401), new Color(0x8ff601), new Color(0x8ff601),
            new Color(0x8df601), new Color(0x8cf701), new Color(0x8bf701),
        new Color(0x88f802), new Color(0x88f902), new Color(0x85fa02), new Color(0x84fa02)
      };
  private static Color[] similarityColorRamp = SIMILARITY_COLOR_RAMP_DEFAULT;

  private static final String DEFAULT_COLOR_FMT = "/bindiff/theme[@name='%s']/c[@for='default']/@v";
  private static final Color DEFAULT_COLOR_DEFAULT = new Color(-16777216);
  private Color defaultColor = DEFAULT_COLOR_DEFAULT;

  private static final String ADDRESS_COLOR_FMT = "/bindiff/theme[@name='%s']/c[@for='address']/@v";
  private static final Color ADDRESS_COLOR_DEFAULT = new Color(-16777216);
  private Color addressColor = ADDRESS_COLOR_DEFAULT;

  private static final String MNEMONIC_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='mnemonic']/@v";
  private static final Color MNEMONIC_COLOR_DEFAULT = new Color(-16777088);
  private Color mnemonicColor = MNEMONIC_COLOR_DEFAULT;

  private static final String SYMBOL_COLOR_FMT = "/bindiff/theme[@name='%s']/c[@for='symbol']/@v";
  private static final Color SYMBOL_COLOR_DEFAULT = new Color(-7076089);
  private Color symbolColor = SYMBOL_COLOR_DEFAULT;

  private static final String IMMEDIATE_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='immediate']/@v";
  private static final Color IMMEDIATE_COLOR_DEFAULT = new Color(-7602176);
  private Color immediateColor = IMMEDIATE_COLOR_DEFAULT;

  private static final String OPERATOR_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='operator']/@v";
  private static final Color OPERATOR_COLOR_DEFAULT = new Color(-16711423);
  private Color operatorColor = OPERATOR_COLOR_DEFAULT;

  private static final String REGISTER_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='register']/@v";
  private static final Color REGISTER_COLOR_DEFAULT = new Color(-16750615);
  private Color registerColor = REGISTER_COLOR_DEFAULT;

  private static final String SIZE_PREFIX_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='size-prefix']/@v";
  private static final Color SIZE_PREFIX_COLOR_DEFAULT = new Color(-6316386);
  private Color sizePrefixColor = SIZE_PREFIX_COLOR_DEFAULT;

  private static final String DEREFERENCE_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='dereference']/@v";
  private static final Color DEREFERENCE_COLOR_DEFAULT = new Color(-16711423);
  private Color dereferenceColor = DEREFERENCE_COLOR_DEFAULT;

  private static final String OPERATOR_SEPARATOR_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='operator-separator']/@v";
  private static final Color OPERATOR_SEPARATOR_COLOR_DEFAULT = new Color(-16777216);
  private Color operatorSeparatorColor = OPERATOR_SEPARATOR_COLOR_DEFAULT;

  private static final String STACK_VARIABLE_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='stack-var']/@v";
  private static final Color STACK_VARIABLE_COLOR_DEFAULT = new Color(-7602176);
  private Color stackVariableColor = STACK_VARIABLE_COLOR_DEFAULT;

  private static final String GLOBAL_VARIABLE_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='global-var']/@v";
  private static final Color GLOBAL_VARIABLE_COLOR_DEFAULT = new Color(-7602176);
  private Color globalVariableColor = GLOBAL_VARIABLE_COLOR_DEFAULT;

  private static final String JUMP_LABEL_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='jump-label']/@v";
  private static final Color JUMP_LABEL_COLOR_DEFAULT = new Color(-6291296);
  private Color jumpLabelColor = JUMP_LABEL_COLOR_DEFAULT;

  private static final String FUNCTION_COLOR_FMT =
      "/bindiff/theme[@name='%s']/c[@for='function']/@v";
  private static final Color FUNCTION_COLOR_DEFAULT = new Color(-7602176);
  private Color functionColor = FUNCTION_COLOR_DEFAULT;

  private static final String COMMENT_COLOR_FMT = "/bindiff/theme[@name='%s']/c[@for='comment']/@v";
  private static final Color COMMENT_COLOR_DEFAULT = new Color(-10197916);
  private Color commentColor = COMMENT_COLOR_DEFAULT;

  private static final String UI_FONT_NAME_FMT = "/bindiff/theme[@name='%s']/f[@for='ui']/@v";
  private static final String UI_FONT_SIZE_FMT = "/bindiff/theme[@name='%s']/f[@for='ui']/@s";
  private static final String UI_FONT_NAME_DEFAULT = "Roboto";
  private Font uiFont;

  private static final String CODE_FONT_NAME_FMT = "/bindiff/theme[@name='%s']/f[@for='code']/@v";
  private static final String CODE_FONT_SIZE_FMT = "/bindiff/theme[@name='%s']/f[@for='code']/@s";
  private static final String CODE_FONT_NAME_DEFAULT = "Roboto Mono";
  private Font codeFont;

  private Color getThemeColor(final Document doc, final String colorFmt, final Color defaultValue)
      throws XPathException {
    final Color result =
        getColor(doc, String.format(colorFmt, useTheme), basedOn.isEmpty() ? defaultValue : null);
    return result == null ? getColor(doc, String.format(colorFmt, basedOn), defaultValue) : result;
  }

  private void setThemeColor(final Document doc, final String colorFmt, final Color value)
      throws XPathException {
    setColor(doc, String.format(colorFmt, useTheme), value);
  }

  private Font getThemeFont(
      final Document doc, final String fontFmt, final String fontSizeFmt, final String defaultName)
      throws XPathException {
    String fontName =
        getString(doc, String.format(fontFmt, useTheme), basedOn.isEmpty() ? defaultName : null);
    if (fontName == null) {
      fontName = getString(doc, String.format(fontFmt, basedOn), defaultName);
    }
    int fontSize =
        getInteger(
            doc,
            String.format(fontSizeFmt, useTheme),
            basedOn.isEmpty() ? GuiHelper.DEFAULT_FONTSIZE : -1);
    if (fontSize == -1) {
      fontName = getString(doc, String.format(fontSizeFmt, basedOn), defaultName);
    }
    return new Font(fontName, Font.PLAIN, fontSize);
  }

  private void setThemeFont(
      final Document doc, final String fontFmt, final String fontSizeFmt, final Font value)
      throws XPathException {
    setString(doc, String.format(fontFmt, useTheme), value.getFontName());
    setInteger(doc, String.format(fontSizeFmt, useTheme), value.getSize());
  }

  @Override
  public void load(final Document doc) throws XPathException {
    useTheme = getString(doc, USE_THEME, USE_THEME_DEFAULT);
    basedOn = getString(doc, String.format(BASED_ON_FMT, useTheme), "");

    final List<String> rampStrs =
        getStrings(doc, String.format(SIMILARITY_COLOR_RAMP_FMT, useTheme), null);
    if (rampStrs != null) {
      final List<Color> colors = new ArrayList<>();
      try {
        for (final String colorStr : rampStrs) {
          colors.add(Color.decode(colorStr));
        }
        if (!colors.isEmpty()) {
          similarityColorRamp = colors.toArray(new Color[0]);
        }
      } catch (final NumberFormatException e) {
        // Keep default
      }
    }

    defaultColor = getThemeColor(doc, DEFAULT_COLOR_FMT, DEFAULT_COLOR_DEFAULT);
    addressColor = getThemeColor(doc, ADDRESS_COLOR_FMT, ADDRESS_COLOR_DEFAULT);
    mnemonicColor = getThemeColor(doc, MNEMONIC_COLOR_FMT, MNEMONIC_COLOR_DEFAULT);
    symbolColor = getThemeColor(doc, SYMBOL_COLOR_FMT, SYMBOL_COLOR_DEFAULT);
    immediateColor = getThemeColor(doc, IMMEDIATE_COLOR_FMT, IMMEDIATE_COLOR_DEFAULT);
    operatorColor = getThemeColor(doc, OPERATOR_COLOR_FMT, OPERATOR_COLOR_DEFAULT);
    registerColor = getThemeColor(doc, REGISTER_COLOR_FMT, REGISTER_COLOR_DEFAULT);
    sizePrefixColor = getThemeColor(doc, SIZE_PREFIX_COLOR_FMT, SIZE_PREFIX_COLOR_DEFAULT);
    dereferenceColor = getThemeColor(doc, DEREFERENCE_COLOR_FMT, DEREFERENCE_COLOR_DEFAULT);
    operatorSeparatorColor =
        getThemeColor(doc, OPERATOR_SEPARATOR_COLOR_FMT, OPERATOR_SEPARATOR_COLOR_DEFAULT);
    stackVariableColor = getThemeColor(doc, STACK_VARIABLE_COLOR_FMT, STACK_VARIABLE_COLOR_DEFAULT);
    globalVariableColor =
        getThemeColor(doc, GLOBAL_VARIABLE_COLOR_FMT, GLOBAL_VARIABLE_COLOR_DEFAULT);
    jumpLabelColor = getThemeColor(doc, JUMP_LABEL_COLOR_FMT, JUMP_LABEL_COLOR_DEFAULT);
    functionColor = getThemeColor(doc, FUNCTION_COLOR_FMT, FUNCTION_COLOR_DEFAULT);
    commentColor = getThemeColor(doc, COMMENT_COLOR_FMT, COMMENT_COLOR_DEFAULT);

    uiFont = getThemeFont(doc, UI_FONT_NAME_FMT, UI_FONT_SIZE_FMT, UI_FONT_NAME_DEFAULT);
    codeFont = getThemeFont(doc, CODE_FONT_NAME_FMT, CODE_FONT_SIZE_FMT, CODE_FONT_NAME_DEFAULT);
  }

  @Override
  public void store(final Document doc) throws XPathException {
    setString(doc, USE_THEME, useTheme);

    final List<String> rampStrs = new ArrayList<>();
    for (final Color color : similarityColorRamp) {
      rampStrs.add(
          String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
    }
    setStrings(doc, String.format(SIMILARITY_COLOR_RAMP_FMT, useTheme), rampStrs);

    setThemeColor(doc, DEFAULT_COLOR_FMT, defaultColor);
    setThemeColor(doc, ADDRESS_COLOR_FMT, addressColor);
    setThemeColor(doc, MNEMONIC_COLOR_FMT, mnemonicColor);
    setThemeColor(doc, SYMBOL_COLOR_FMT, symbolColor);
    setThemeColor(doc, IMMEDIATE_COLOR_FMT, immediateColor);
    setThemeColor(doc, OPERATOR_COLOR_FMT, operatorColor);
    setThemeColor(doc, REGISTER_COLOR_FMT, registerColor);
    setThemeColor(doc, SIZE_PREFIX_COLOR_FMT, sizePrefixColor);
    setThemeColor(doc, DEREFERENCE_COLOR_FMT, dereferenceColor);
    setThemeColor(doc, OPERATOR_SEPARATOR_COLOR_FMT, operatorSeparatorColor);
    setThemeColor(doc, STACK_VARIABLE_COLOR_FMT, stackVariableColor);
    setThemeColor(doc, GLOBAL_VARIABLE_COLOR_FMT, globalVariableColor);
    setThemeColor(doc, JUMP_LABEL_COLOR_FMT, jumpLabelColor);
    setThemeColor(doc, FUNCTION_COLOR_FMT, functionColor);
    setThemeColor(doc, COMMENT_COLOR_FMT, commentColor);

    setThemeFont(doc, UI_FONT_NAME_FMT, UI_FONT_SIZE_FMT, uiFont);
    setThemeFont(doc, CODE_FONT_NAME_FMT, CODE_FONT_SIZE_FMT, codeFont);
  }

  public final Color[] getSimilarityColorRamp() {
    return similarityColorRamp;
  }

  public final Color getDefaultColor() {
    return defaultColor;
  }

  public final Color getAddressColor() {
    return addressColor;
  }

  public final Color getMnemonicColor() {
    return mnemonicColor;
  }

  public final Color getSymbolColor() {
    return symbolColor;
  }

  public final Color getImmediateColor() {
    return immediateColor;
  }

  public final Color getOperatorColor() {
    return operatorColor;
  }

  public final Color getRegisterColor() {
    return registerColor;
  }

  public final Color getSizePrefixColor() {
    return sizePrefixColor;
  }

  public final Color getDereferenceColor() {
    return dereferenceColor;
  }

  public final Color getOperatorSeparatorColor() {
    return operatorSeparatorColor;
  }

  public final Color getStackVariableColor() {
    return stackVariableColor;
  }

  public final Color getGlobalVariableColor() {
    return globalVariableColor;
  }

  public final Color getJumpLabelColor() {
    return jumpLabelColor;
  }

  public final Color getFunctionColor() {
    return functionColor;
  }

  public final Color getCommentColor() {
    return commentColor;
  }

  public Font getUiFont() {
    return uiFont;
  }

  public Font getCodeFont() {
    return codeFont;
  }

  public final void setDefaultColor(final Color defaultColor) {
    this.defaultColor = defaultColor;
  }

  public final void setAddressColor(final Color addressColor) {
    this.addressColor = addressColor;
  }

  public final void setMnemonicColor(final Color mnemonicColor) {
    this.mnemonicColor = mnemonicColor;
  }

  public final void setSymbolColor(final Color symbolColor) {
    this.symbolColor = symbolColor;
  }

  public final void setImmediateColor(final Color immediateColor) {
    this.immediateColor = immediateColor;
  }

  public final void setOperatorColor(final Color operatorColor) {
    this.operatorColor = operatorColor;
  }

  public final void setRegisterColor(final Color registerColor) {
    this.registerColor = registerColor;
  }

  public final void setSizePrefixColor(final Color sizePrefixColor) {
    this.sizePrefixColor = sizePrefixColor;
  }

  public final void setDereferenceColor(final Color dereferenceColor) {
    this.dereferenceColor = dereferenceColor;
  }

  public final void setOperatorSeparatorColor(final Color operatorSeparatorColor) {
    this.operatorSeparatorColor = operatorSeparatorColor;
  }

  public final void setStackVariableColor(final Color stackVariableColor) {
    this.stackVariableColor = stackVariableColor;
  }

  public final void setGlobalVariableColor(final Color globalVariableColor) {
    this.globalVariableColor = globalVariableColor;
  }

  public final void setJumpLabelColor(final Color jumpLabelColor) {
    this.jumpLabelColor = jumpLabelColor;
  }

  public final void setFunctionColor(final Color functionColor) {
    this.functionColor = functionColor;
  }

  public final void setCommentColor(final Color commentColor) {
    this.commentColor = commentColor;
  }

  public void setUiFont(final Font uiFont) {
    this.uiFont = uiFont;
  }

  public void setCodeFont(final Font codeFont) {
    this.codeFont = codeFont;
  }
}
