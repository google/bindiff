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

package com.google.security.zynamics.zylib.disassembly;

import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.primitives.Longs;
import java.math.BigInteger;

/**
 * This class represents (memory) addresses. It implements the {@code IAddress} interface and
 * provides additional typed overloads for performance.
 *
 * @author cblichmann@google.com (Christian Blichmann)
 */
public class CAddress implements IAddress {
  private final long address;

  public CAddress(final BigInteger address) {
    Preconditions.checkNotNull(address, "Address argument can not be null");

    // longValue() does a narrowing conversion and returns the lower 64 bit
    // verbatim (i.e. without respect to signedness)
    this.address = address.longValue();
  }

  public CAddress(final CAddress address) {
    Preconditions.checkNotNull(address, "Address argument can not be null");
    this.address = address.address;
  }

  public CAddress(final IAddress address) {
    Preconditions.checkNotNull(address, "Address argument can not be null");
    this.address = address.toLong();
  }

  public CAddress(final long address) {
    this.address = address;
  }

  /**
   * Parses the given address {@link String} into a BigInteger and uses this as input for the actual
   * {@link CAddress}.
   *
   * @param address The {@link String} representation of an address.
   * @param base The base to which the String will be parsed.
   */
  public CAddress(final String address, final int base) {
    Preconditions.checkNotNull(address, "Address argument can not be null");
    Preconditions.checkArgument(base > 0, "Base must be positive");
    this.address = new BigInteger(address, base).longValue();
  }

  /**
   * Compares two Java {@code long} values for order, treating the values as unsigned. Returns a
   * negative integer, zero, or a positive integer as the first specified value is less than, equal
   * to, or greater than the second specified value.
   *
   * @param a the first address to compare
   * @param b the second address to compare
   * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
   *     or greater than the specified object.
   */
  public static int compare(final long a, final long b) {
    // Perform arithmetic comparison first, resulting in -1, 0 or 1 if
    // the second value is less than, equal or larger than the first,
    // respectively.
    final int result = Long.compare(a, b);

    // If both values have the same sign value, return the result.
    // Otherwise, one of the two values was negative and we need to "flip"
    // the result.
    return (a & 0x8000000000000000L) == (b & 0x8000000000000000L) ? result : -result;
  }

  /** See {@link #compareTo(IAddress)}. */
  public int compareTo(final CAddress o) {
    return compare(address, o.address);
  }

  @Override
  public int compareTo(final IAddress o) {
    return compare(address, o.toLong());
  }

  /** See {@link #compareTo(IAddress)}. */
  public int compareTo(final long o) {
    return compare(address, o);
  }

  /** See {@link #equals(Object)}. */
  @SuppressWarnings("NonOverridingEquals")
  public boolean equals(final CAddress address) {
    return (address != null) && (this.address == address.address);
  }

  /** See {@link #equals(Object)}. */
  @SuppressWarnings("NonOverridingEquals")
  public boolean equals(final IAddress address) {
    return (address != null) && (this.address == address.toLong());
  }

  /** See {@link #equals(Object)}. */
  @SuppressWarnings("NonOverridingEquals")
  public boolean equals(final long address) {
    return this.address == address;
  }

  @Override
  public boolean equals(final Object address) {
    return (address instanceof IAddress) && (this.address == ((IAddress) address).toLong());
  }

  @Override
  public int hashCode() {
    return Longs.hashCode(address);
  }

  @Override
  public BigInteger toBigInteger() {
    // Use valueOf() to provide caching for frequently used values
    if ((address & 0x8000000000000000L) == 0) {
      return BigInteger.valueOf(address);
    }

    // Long.toHexString() interprets its argument unsigned
    return new BigInteger(Long.toHexString(address), 16);
  }

  @Override
  public String toHexString() {
    // Long.toHexString() interprets its argument unsigned
    return Strings.padStart(
        Ascii.toUpperCase(Long.toHexString(address)),
        (address & 0x7fffffffffffffffL) < 0x100000000L ? 8 : 16,
        '0');
  }

  @Override
  public long toLong() {
    return address;
  }

  @Override
  public String toString() {
    return toHexString();
  }
}
