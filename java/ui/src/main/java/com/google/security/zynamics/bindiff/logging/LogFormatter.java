package com.google.security.zynamics.bindiff.logging;

import com.google.common.base.Ascii;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/** Formats log records in a simple, Google-style format. */
public class LogFormatter extends SimpleFormatter {
  private static final String PACKAGE;

  static {
    final String name = LogFormatter.class.getPackage().getName();
    PACKAGE = name.substring(0, name.lastIndexOf('.'));
  }

  private static StringBuilder formatClassName(String className) {
    final StringBuilder b = new StringBuilder();
    final String prefix = Strings.commonPrefix(className, PACKAGE);
    Splitter.on('.')
        .splitToList(prefix)
        .forEach(
            s -> {
              b.append('/');
              b.append(s.charAt(0));
            });
    return b.append(className, prefix.length(), className.length()).deleteCharAt(0);
  }

  @Override
  public synchronized String format(final LogRecord record) {
    // TODO(cblichmann): Use record.getInstant() once Java 11 is default
    final LocalDateTime time = LocalDateTime.now(ZoneId.systemDefault());

    // I1030 12:55:44.747944 1407373 c/g/s/z/b/logging.LogFormatter::format]
    final char shortLevel = Ascii.toUpperCase(Logger.levelToString(record.getLevel()).charAt(0));
    return (Character.isAlphabetic(shortLevel) ? shortLevel : '?')
        + Strings.padStart(Integer.toString(time.getMonthValue()), 2, '0')
        + Strings.padStart(Integer.toString(time.getDayOfMonth()), 2, '0')
        + ' '
        + Strings.padStart(Integer.toString(time.getHour()), 2, '0')
        + ':'
        + Strings.padStart(Integer.toString(time.getMinute()), 2, '0')
        + ':'
        + Strings.padStart(Integer.toString(time.getSecond()), 2, '0')
        + '.'
        + Strings.padStart(Integer.toString(time.getNano() / 1000), 6, '0')
        + ' '
        + record.getThreadID()
        + ' '
        + formatClassName(record.getSourceClassName())
        + "::"
        + record.getSourceMethodName()
        + "] "
        + record.getMessage()
        + '\n';
  }
}
