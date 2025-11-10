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

package com.google.security.zynamics.zylib.general;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.xml.bind.DatatypeConverter;

/** Helper class that can convert data between different formats. */
public final class Convert {
  private static String[] HEX_ARRAY = {
    "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
    "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
    "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
    "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
    "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F",
    "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
    "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E", "6F",
    "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E", "7F",
    "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
    "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
    "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
    "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
    "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF",
    "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF",
    "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
    "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF"
  };

  /**
   * Converts an ASCII string into a hex string.
   *
   * <p>Example: AAA => 414141
   *
   * @param asciiString The ASCII string to convert.
   * @return The converted hex string.
   */
  public static String asciiToHexString(final String asciiString) {
    final StringBuffer sb = new StringBuffer();

    for (final byte b : asciiString.getBytes()) {
      sb.append(byteToHexString(b));
    }

    return sb.toString();
  }

  public static String byteToHexString(final byte b) {
    return HEX_ARRAY[b & 0xFF];
  }

  public static String colorToHexString(final Color c) {
    final String cs = Integer.toHexString(c.getRGB());
    return cs.substring(2);
  }

  /**
   * Converts a decimal string into a hexadecimal string.
   *
   * <p>Note that the decimal string value must fit into a long value.
   *
   * @param decString The decimal string to convert.
   * @return The hexadecimal string.
   */
  public static String decStringToHexString(final String decString) {
    Preconditions.checkNotNull(decString, "Error: Decimal string can't be null");

    return Long.toHexString(Long.valueOf(decString, 16));
  }

  /**
   * Converts a hex to ASCII. If the hex string has an odd number of characters, a 0 is added at the
   * end of the string.
   *
   * <p>Example: 414141 is converted to AAA
   *
   * @param hexString The string to convert.
   * @return The converted ASCII string.
   */
  public static String hexStringToAsciiString(final String hexString) {
    final String realText = (hexString.length() % 2) == 0 ? hexString : "0" + hexString;

    String asciiString = "";

    for (int i = 0; i < realText.length(); i += 2) {
      final char c1 = realText.charAt(i);
      final char c2 = realText.charAt(i + 1);

      if (!isHexCharacter(c1) || !isHexCharacter(c2)) {
        throw new IllegalArgumentException("Error: Invalid hex string");
      }

      final char code = (char) ((Character.digit(c1, 16) << 4) + Character.digit(c2, 16));

      asciiString += isPrintableCharacter(code) ? code : ".";
    }

    return asciiString;
  }

  /**
   * Converts a hex string to a byte array. If the hex string has an odd number of characters, a 0
   * is added at the end of the string.
   *
   * <p>Example: 414141 => {0x41, 0x41, 0x41}
   *
   * @param hexString The hex string to convert.
   * @return The converted byte array.
   */
  public static byte[] hexStringToBytes(final String hexString) {
    return DatatypeConverter.parseHexBinary(hexString);
  }

  /**
   * Converts an hex string to long.
   *
   * @param hexString string to convert.
   * @return The hex string.
   */
  public static long hexStringToLong(final String hexString) {
    Preconditions.checkNotNull(hexString, "Error: Unicode string can't be null");
    Preconditions.checkArgument(
        isHexString(hexString), "Error: Hex string '%s' is not a valid hex string", hexString);

    if ((hexString.length() == 16) && (hexString.charAt(0) >= 8)) {
      // Reason: Long.parseLong(x, 16) throws NumberFormatException when
      // x is a negative value (x >= 8000.0000.0000.0000)! NH

      final String strAddr1 = hexString.substring(0, hexString.length() - 8);
      final String strAddr2 = hexString.substring(hexString.length() - 8);

      return (Long.parseLong(strAddr1, 16) << 32) + Long.parseLong(strAddr2, 16);
    }

    return Long.parseLong(hexString, 16);
  }

  /**
   * Tests whether a given character is a valid decimal character.
   *
   * @param c The character to test.
   * @return True, if the given character is a valid decimal character.
   */
  public static boolean isDecCharacter(final char c) {
    return CharMatcher.inRange('0', '9').apply(c);
  }

  /**
   * Tests whether a given string is a valid decimal string.
   *
   * @param string The string to check.
   * @return True, if the string is a valid decimal string. False, otherwise.
   */
  public static boolean isDecString(final String string) {
    Preconditions.checkNotNull(string);

    final CharMatcher cm = CharMatcher.inRange('0', '9');
    for (int i = 0; i < string.length(); i++) {
      if (!cm.apply(string.charAt(i))) {
        return false;
      }
    }
    return string.length() != 0;
  }

  /**
   * Tests whether a character is a valid character of a hexadecimal string.
   *
   * @param c The character to test.
   * @return True, if the character is a hex character. False, otherwise.
   */
  public static boolean isHexCharacter(final char c) {
    return isDecCharacter(c) || ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F'));
  }

  /**
   * Tests whether a given string is a valid hexadecimal string.
   *
   * @param string The string to check.
   * @return True, if the string is a valid hexadecimal string. False, otherwise.
   */
  public static boolean isHexString(final String string) {
    Preconditions.checkNotNull(string, "Error: String argument can't be null");

    final CharMatcher cm =
        CharMatcher.inRange('0', '9')
            .or(CharMatcher.inRange('a', 'z'))
            .or(CharMatcher.inRange('A', 'F'));
    for (int i = 0; i < string.length(); i++) {
      if (!cm.apply(string.charAt(i))) {
        return false;
      }
    }

    return string.length() != 0;
  }

  /**
   * Tests whether a given string is a valid MD5 string.
   *
   * @param string The string to check.
   * @return True, if the string is a valid MD5 string. False, otherwise.
   */
  public static boolean isMD5String(final String string) {
    Preconditions.checkNotNull(string, "Error: String argument can't be null");
    return (string.length() == 32) && isHexString(string);
  }

  /**
   * Tests whether a character is a printable ASCII character.
   *
   * @param c The character to test.
   * @return True, if the character is a printable ASCII character. False, otherwise.
   */
  public static boolean isPrintableCharacter(final char c) {
    final Character.UnicodeBlock block = Character.UnicodeBlock.of(c);

    return !Character.isISOControl(c)
        && (c != KeyEvent.CHAR_UNDEFINED)
        && (block != null)
        && (block != Character.UnicodeBlock.SPECIALS);
  }

  /**
   * Tests whether a given string is a valid SHA1 string.
   *
   * @param string The string to check.
   * @return True, if the string is a valid SHA1 string. False, otherwise.
   */
  public static boolean isSha1String(final String string) {
    Preconditions.checkNotNull(string, "Error: String argument can't be null");
    return (string.length() == 40) && isHexString(string);
  }

  /**
   * Converts an unicode string to a hex string.
   *
   * @param unicodeString The unicode string to convert.
   * @return The hex string.
   */
  public static String unicodeToHexString(final String unicodeString) {
    Preconditions.checkNotNull(unicodeString, "Error: Unicode string can't be null");

    final StringBuffer sb = new StringBuffer();

    for (final byte b : unicodeString.getBytes()) {
      sb.append(String.format("%X00", b));
    }

    return sb.toString();
  }
}
