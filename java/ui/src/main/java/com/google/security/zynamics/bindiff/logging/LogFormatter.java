// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.bindiff.logging;

import com.google.common.base.Ascii;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
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
              b.append(s.length() > 0 ? s.charAt(0) : '?');
            });
    return b.append(className, prefix.length(), className.length()).deleteCharAt(0);
  }

  @Override
  public synchronized String format(final LogRecord record) {
    @SuppressWarnings("JavaTimeDefaultTimeZone")
    final ZonedDateTime time = ZonedDateTime.now();

    // I1030 12:55:44.747944 1407373 c/g/s/z/b/logging.LogFormatter::format]
    final char shortLevel = Ascii.toUpperCase(Logger.levelToString(record.getLevel()).charAt(0));
    final String prefix =
        (Character.isAlphabetic(shortLevel) ? shortLevel : '?')
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
            + "] ";
    final StringBuilder b = new StringBuilder();
    b.append(prefix).append(record.getMessage()).append('\n');

    final Throwable t = record.getThrown();
    if (t != null) {
      final StringWriter sw = new StringWriter();
      t.printStackTrace(new PrintWriter(sw));
      Splitter.on('\n')
          .omitEmptyStrings()
          .splitToList(sw.getBuffer())
          .forEach(s -> b.append(prefix).append(s).append('\n'));
    }
    return b.toString();
  }
}
