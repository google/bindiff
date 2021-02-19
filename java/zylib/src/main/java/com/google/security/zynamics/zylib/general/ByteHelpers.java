// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.general;

import java.util.List;

/**
 * This class contains functions for commonly used byte operations.
 */
public final class ByteHelpers {
  /**
   * Combines a list of byte arrays into one big byte array.
   * 
   * @param dataChunks The list of byte arrays.
   * 
   * @return The one big byte array.
   */
  public static byte[] combine(final List<byte[]> dataChunks) {
    int totalSize = 0;

    for (final byte[] dataPart : dataChunks) {
      totalSize += dataPart.length;
    }

    final byte[] data = new byte[totalSize];

    int index = 0;

    for (final byte[] dataPart : dataChunks) {
      System.arraycopy(dataPart, 0, data, index, dataPart.length);

      index += dataPart.length;
    }

    return data;
  }

  /**
   * Reads a Big Endian DWORD value from a byte array.
   * 
   * @param data The byte array from which the DWORD value is read.
   * @param offset The index of the array element where DWORD reading begins.
   * 
   * @return The DWORD value read from the array.
   */
  public static long readDwordBigEndian(final byte[] data, final int offset) {
    return ((data[offset + 0] & 0xFFL) * 0x100 * 0x100 * 0x100)
        + ((data[offset + 1] & 0xFFL) * 0x100 * 0x100) + ((data[offset + 2] & 0xFFL) * 0x100)
        + (data[offset + 3] & 0xFFL);
  }

  /**
   * Reads a Little Endian DWORD value from a byte array.
   * 
   * @param data The byte array from which the DWORD value is read.
   * @param offset The index of the array element where DWORD reading begins.
   * 
   * @return The DWORD value read from the array.
   */
  public static long readDwordLittleEndian(final byte[] data, final int offset) {
    return ((data[offset + 3] & 0xFFL) * 0x100 * 0x100 * 0x100)
        + ((data[offset + 2] & 0xFFL) * 0x100 * 0x100) + ((data[offset + 1] & 0xFFL) * 0x100)
        + (data[offset + 0] & 0xFFL);
  }

  /**
   * Reads a Big Endian QWORD value from a byte array.
   * 
   * @param data The byte array from which the QWORD value is read.
   * @param offset The index of the array element where QWORD reading begins.
   * 
   * @return The QWORD value read from the array.
   */
  public static long readQwordBigEndian(final byte[] data, final int offset) {
    return ((data[offset + 0] & 0xFFL) * 0x100 * 0x100 * 0x100 * 0x100 * 0x100 * 0x100 * 0x100)
        + ((data[offset + 1] & 0xFFL) * 0x100 * 0x100 * 0x100 * 0x100 * 0x100 * 0x100)
        + ((data[offset + 2] & 0xFFL) * 0x100 * 0x100 * 0x100 * 0x100 * 0x100)
        + ((data[offset + 3] & 0xFFL) * 0x100 * 0x100 * 0x100 * 0x100)
        + ((data[offset + 4] & 0xFFL) * 0x100 * 0x100 * 0x100)
        + ((data[offset + 5] & 0xFFL) * 0x100 * 0x100) + ((data[offset + 6] & 0xFFL) * 0x100)
        + (data[offset + 7] & 0xFFL);
  }

  /**
   * Reads a Little Endian QWORD value from a byte array.
   * 
   * @param data The byte array from which the QWORD value is read.
   * @param offset The index of the array element where QWORD reading begins.
   * 
   * @return The QWORD value read from the array.
   */
  public static long readQwordLittleEndian(final byte[] data, final int offset) {
    return ((data[offset + 7] & 0xFFL) * 0x100 * 0x100 * 0x100 * 0x100 * 0x100 * 0x100 * 0x100)
        + ((data[offset + 6] & 0xFFL) * 0x100 * 0x100 * 0x100 * 0x100 * 0x100 * 0x100)
        + ((data[offset + 5] & 0xFFL) * 0x100 * 0x100 * 0x100 * 0x100 * 0x100)
        + ((data[offset + 4] & 0xFFL) * 0x100 * 0x100 * 0x100 * 0x100)
        + ((data[offset + 3] & 0xFFL) * 0x100 * 0x100 * 0x100)
        + ((data[offset + 2] & 0xFFL) * 0x100 * 0x100) + ((data[offset + 1] & 0xFFL) * 0x100)
        + (data[offset + 0] & 0xFFL);
  }

  /**
   * Reads a Big Endian WORD value from a byte array.
   * 
   * @param data The byte array from which the WORD value is read.
   * @param offset The index of the array element where WORD reading begins.
   * 
   * @return The WORD value read from the array.
   */
  public static long readWordBigEndian(final byte[] data, final int offset) {
    return ((data[offset + 0] & 0xFFL) * 0x100) + (data[offset + 1] & 0xFFL);
  }

  /**
   * Reads a Little Endian WORD value from a byte array.
   * 
   * @param data The byte array from which the WORD value is read.
   * @param offset The index of the array element where WORD reading begins.
   * 
   * @return The WORD value read from the array.
   */
  public static long readWordLittleEndian(final byte[] data, final int offset) {
    return ((data[offset + 1] & 0xFFL) * 0x100) + (data[offset + 0] & 0xFFL);
  }

  /**
   * Converts a list of bytes into a byte array.
   * 
   * @param list The list to convert.
   * 
   * @return The created byte array.
   */
  public static byte[] toArray(final List<Byte> list) {
    final byte[] output = new byte[list.size()];

    for (int i = 0; i < output.length; i++) {
      output[i] = list.get(i);
    }

    return output;
  }

  public static byte[] toBigEndianDword(final long value) {
    return new byte[] {(byte) ((value & 0xFF000000L) >>> 24),
        (byte) ((value & 0x00FF0000L) >>> 16), (byte) ((value & 0x0000FF00L) >>> 8),
        (byte) (value & 0x000000FFL)};
  }

  public static byte[] toBigEndianWord(final long value) {
    return new byte[] {(byte) ((value & 0xFF00L) >>> 8), (byte) (value & 0xFF)};
  }

  public static byte[] toLittleEndianDword(final long value) {
    return new byte[] {(byte) (value & 0x000000FFL), (byte) ((value & 0x0000FF00L) >>> 8),
        (byte) ((value & 0x00FF0000L) >>> 16), (byte) ((value & 0xFF000000L) >>> 24)};
  }

  public static byte[] toLittleEndianWord(final long value) {
    return new byte[] {(byte) (value & 0xFF), (byte) ((value & 0xFF00L) >>> 8)};
  }
}
