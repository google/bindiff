// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.date;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.google.common.base.Preconditions;

/**
 * Helper class that provides quick access to common date/time functions.
 */
public class DateHelpers {
  public static String formatDate(final Date date) {
    Preconditions.checkNotNull(date, "Error: Date argument can't be null.");

    return DateFormat.getDateInstance().format(date);
  }

  /**
   * Formats a date string regarding the given locale.
   * 
   * @param date The date to format.
   * @param type Defines the display type of month
   * @param locale The given locale.
   * 
   * @return The data string.
   */
  public static String formatDate(final Date date, final int type, final Locale locale) {
    Preconditions.checkNotNull(date, "Error: Date argument can't be null.");
    Preconditions.checkNotNull(locale, "Error: Locale argument can't be null.");

    final String s =
        String.format("%s %s", DateFormat.getDateInstance(type, locale).format(date), DateFormat
            .getTimeInstance(type, locale).format(date));

    return s;
  }

  /**
   * Formats a date string using the currently selected locale.
   * 
   * @param date The date to format.
   * 
   * @return The data string.
   */
  public static String formatDateTime(final Date date) {
    Preconditions.checkNotNull(date, "Error: Date argument can't be null.");

    return DateFormat.getDateTimeInstance().format(date);
  }

  public static String formatTime(final Date date) {
    Preconditions.checkNotNull(date, "Error: Date argument can't be null.");

    return DateFormat.getTimeInstance().format(date);
  }

  /**
   * Returns the current date.
   * 
   * @return The current date.
   */
  public static Date getCurrentDate() {
    return Calendar.getInstance().getTime();
  }

  /**
   * Returns the current date in string form.
   * 
   * @return A string that contains the current date.
   */
  public static String getCurrentDateString() {
    return formatDateTime(Calendar.getInstance().getTime());
  }

  /**
   * Extracts the date from a user defined date String.
   * 
   * @param dateString The date string to extract the date from.
   * @param format The format of the date string (e.g: MM-DD-YYYY)
   * 
   * @return The current date.
   */
  public static Date getDate(final String dateString, final String format) {
    Preconditions
        .checkArgument(dateString.length() == format.length(),
            "Date string format exception. Format string must have the same length as the date string.");

    String day = "";
    String month = "";
    String year = "";

    for (int i = 0; i < format.length(); ++i) {
      final char chr = format.charAt(i);

      if (chr == 'D') {
        day += dateString.charAt(i);
      } else if (chr == 'M') {
        month += dateString.charAt(i);
      } else if (chr == 'Y') {
        year += dateString.charAt(i);
      }
    }

    Preconditions.checkArgument(day.length() == 2,
        "Date string format exception. Date string's day field must have two chars.");
    Preconditions.checkArgument(month.length() == 2,
        "Date string format exception. Date string's month field must have two chars.");
    Preconditions.checkArgument(year.length() == 4,
        "Date string format exception. Date string's years field must have four chars.");

    final int iday = Integer.parseInt(day);
    final int imonth = Integer.parseInt(month) - 1;
    final int iyear = Integer.parseInt(year);

    final GregorianCalendar calendar = new GregorianCalendar(iyear, imonth, iday);

    return calendar.getTime();
  }
}
