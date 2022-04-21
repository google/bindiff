// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.bindiff.config;

import com.google.common.flogger.FluentLogger;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.security.zynamics.bindiff.BinDiffProtos;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Sets and gets the syntax highlighting color values and font settings for the UI. */
public class ThemeConfigItem {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final Descriptor THEME_DESCRIPTOR = BinDiffProtos.Config.UiTheme.getDescriptor();
  private static final BinDiffProtos.Config.UiTheme DEFAULT_THEME =
      Config.DEFAULTS.getThemesOrThrow("Google Material");

  private String currentTheme;
  private BinDiffProtos.Config.UiTheme.Builder theme;

  private final Map<String, Color> colorCache = new HashMap<>();
  private final Map<String, Font> fontCache = new HashMap<>();
  private static Color[] similarityColorRamp;

  private void ensureTheme() {
    if (theme != null) {
      return;
    }
    final BinDiffProtos.Config.Builder config = Config.getInstance();
    currentTheme = config.getPreferences().getUseTheme();

    theme =
        BinDiffProtos.Config.UiTheme.newBuilder(
            config.getThemesOrDefault(currentTheme, DEFAULT_THEME));
    colorCache.clear();
    fontCache.clear();
    similarityColorRamp = null;
  }

  public void apply() {
    ensureTheme();
    Config.getInstance().putThemes(currentTheme, theme.build());
  }

  public final Color[] getSimilarityColorRamp() {
    ensureTheme();
    if (similarityColorRamp != null) {
      return similarityColorRamp;
    }

    List<String> ramp = theme.getSimilarityRampList();
    if (ramp.isEmpty()) {
      ramp = DEFAULT_THEME.getSimilarityRampList();
    }

    List<Color> colors;
    try {
      colors = ramp.stream().map(Color::decode).collect(Collectors.toList());
    } catch (final NumberFormatException e) {
      logger.atWarning().withCause(e).log(
          "Color hex value failed to parse, reverting to default color ramp");
      colors =
          DEFAULT_THEME.getSimilarityRampList().stream()
              .map(Color::decode)
              .collect(Collectors.toList());
    }
    if (!colors.isEmpty()) {
      similarityColorRamp = colors.toArray(new Color[0]);
    }
    return similarityColorRamp;
  }

  public void setSimilarityColorRamp(final Color[] colors) {
    ensureTheme();
    similarityColorRamp = colors;

    theme.clearSimilarityRamp();
    Arrays.stream(colors).forEach(color -> theme.addSimilarityRamp(Config.formatColor(color)));
  }

  private Color getThemeColor(final String fieldName) {
    ensureTheme();
    return colorCache.computeIfAbsent(
        fieldName,
        (k) -> {
          final FieldDescriptor fd = THEME_DESCRIPTOR.findFieldByName(k);
          final String color = (String) theme.getField(fd);
          if (color.isEmpty()) {
            logger.atInfo().log("%s", k);
            return Color.decode((String) DEFAULT_THEME.getField(fd));
          }
          try {
            return Color.decode(color);
          } catch (NumberFormatException e) {
            logger.atWarning().withCause(e).log("Color hex value failed to parse, using default");
            return Color.decode((String) DEFAULT_THEME.getField(fd));
          }
        });
  }

  private void setThemeColor(final String fieldName, final Color color) {
    if (!color.equals(colorCache.get(fieldName))) {
      ensureTheme();
      colorCache.put(fieldName, color);

      final FieldDescriptor fd = THEME_DESCRIPTOR.findFieldByName(fieldName);
      theme.setField(fd, Config.formatColor(color));
    }
  }

  public final Font getThemeFont(final String fieldName) {
    ensureTheme();
    return fontCache.computeIfAbsent(
        fieldName,
        k -> {
          final FieldDescriptor fd = THEME_DESCRIPTOR.findFieldByName(k);
          final BinDiffProtos.Config.UiTheme.ThemeFont themeFont =
              (BinDiffProtos.Config.UiTheme.ThemeFont) theme.getField(fd);
          final BinDiffProtos.Config.UiTheme.ThemeFont defaultFont =
              (BinDiffProtos.Config.UiTheme.ThemeFont) DEFAULT_THEME.getField(fd);
          return new Font(
              !themeFont.getFamily().isEmpty() ? themeFont.getFamily() : defaultFont.getFamily(),
              Font.PLAIN,
              themeFont.getSize() != 0 ? themeFont.getSize() : defaultFont.getSize());
        });
  }

  public void setThemeFont(final String fieldName, final Font font) {
    if (!font.equals(fontCache.get(fieldName))) {
      ensureTheme();
      fontCache.put(fieldName, font);

      final FieldDescriptor fd = THEME_DESCRIPTOR.findFieldByName(fieldName);
      theme.setField(
          fd,
          BinDiffProtos.Config.UiTheme.ThemeFont.newBuilder()
              .setFamily(font.getFamily())
              .setSize(font.getSize()));
    }
  }

  public final Color getDefaultColor() {
    return getThemeColor("text");
  }

  public final Color getAddressColor() {
    return getThemeColor("address");
  }

  public final Color getMnemonicColor() {
    return getThemeColor("mnemonic");
  }

  public final Color getSymbolColor() {
    return getThemeColor("symbol");
  }

  public final Color getImmediateColor() {
    return getThemeColor("immediate");
  }

  public final Color getOperatorColor() {
    return getThemeColor("operator");
  }

  public final Color getRegisterColor() {
    return getThemeColor("register");
  }

  public final Color getSizePrefixColor() {
    return getThemeColor("size_prefix");
  }

  public final Color getDereferenceColor() {
    return getThemeColor("dereference");
  }

  public final Color getOperatorSeparatorColor() {
    return getThemeColor("operator_separator");
  }

  public final Color getStackVariableColor() {
    return getThemeColor("stack_variable");
  }

  public final Color getGlobalVariableColor() {
    return getThemeColor("global_variable");
  }

  public final Color getJumpLabelColor() {
    return getThemeColor("jump_label");
  }

  public final Color getFunctionColor() {
    return getThemeColor("function");
  }

  public final Color getCommentColor() {
    return getThemeColor("comment");
  }

  public Font getUiFont() {
    return getThemeFont("ui");
  }

  public Font getCodeFont() {
    return getThemeFont("code");
  }

  public final void setDefaultColor(final Color color) {
    setThemeColor("text", color);
  }

  public final void setAddressColor(final Color color) {
    setThemeColor("address", color);
  }

  public final void setMnemonicColor(final Color color) {
    setThemeColor("mnemonic", color);
  }

  public final void setSymbolColor(final Color color) {
    setThemeColor("symbol", color);
  }

  public final void setImmediateColor(final Color color) {
    setThemeColor("immediate", color);
  }

  public final void setOperatorColor(final Color color) {
    setThemeColor("operator", color);
  }

  public final void setRegisterColor(final Color color) {
    setThemeColor("register", color);
  }

  public final void setSizePrefixColor(final Color color) {
    setThemeColor("size_prefix", color);
  }

  public final void setDereferenceColor(final Color color) {
    setThemeColor("dereference", color);
  }

  public final void setOperatorSeparatorColor(final Color color) {
    setThemeColor("operator_separator", color);
  }

  public final void setStackVariableColor(final Color color) {
    setThemeColor("stack_variable", color);
  }

  public final void setGlobalVariableColor(final Color color) {
    setThemeColor("global_variable", color);
  }

  public final void setJumpLabelColor(final Color color) {
    setThemeColor("jump_label", color);
  }

  public final void setFunctionColor(final Color color) {
    setThemeColor("function", color);
  }

  public final void setCommentColor(final Color color) {
    setThemeColor("comment", color);
  }

  public void setUiFont(final Font font) {
    setThemeFont("ui", font);
  }

  public void setCodeFont(final Font font) {
    setThemeFont("code", font);
  }
}
