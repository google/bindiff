// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.zylib.disassembly;

import java.math.BigInteger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * This class represents (memory) addresses. It implements the {@code IAddress} interface and
 * provides additional typed overloads for performance.
 * 
 * @author cblichmann@google.com (Christian Blichmann)
 */
public class CAddress implements IAddress {
  private final long m_address;

  public CAddress(final BigInteger address) {
    Preconditions.checkNotNull(address, "Address argument can not be null");

    // longValue() does a narrowing conversion and returns the lower 64 bit
    // verbatim (i.e. without respect to signedness)
    m_address = address.longValue();
  }

  public CAddress(final CAddress address) {
    Preconditions.checkNotNull(address, "Address argument can not be null");
    m_address = address.m_address;
  }

  public CAddress(final IAddress address) {
    Preconditions.checkNotNull(address, "Address argument can not be null");
    m_address = address.toLong();
  }

  public CAddress(final long address) {
    m_address = address;
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
    m_address = new BigInteger(address, base).longValue();
  }

  /**
   * Compares two Java {@code long} values for order, treating the values as unsigned. Returns a
   * negative integer, zero, or a positive integer as the first specified value is less than, equal
   * to, or greater than the second specified value.
   * 
   * @param addr1 the first address to compare
   * @param addr2 the second address to compare
   * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
   *         or greater than the specified object.
   */
  public static int compare(final long addr1, final long addr2) {
    // Perform arithmetic comparison first, resulting in -1, 0 or 1 if
    // the second value is less than, equal or larger than the first,
    // respectively.
    final int result = addr1 < addr2 ? -1 : (addr1 > addr2 ? 1 : 0);

    // If both values have the same sign value, return the result.
    // Otherwise, one of the two values was negative and we need to "flip"
    // the result.
    return (addr1 & 0x8000000000000000L) == (addr2 & 0x8000000000000000L) ? result : -result;
  }

  /**
   * @see #compareTo(IAddress)
   */
  public int compareTo(final CAddress addr) {
    return compare(m_address, addr.m_address);
  }

  @Override
  public int compareTo(final IAddress addr) {
    return compare(m_address, addr.toLong());
  }

  /**
   * @see #compareTo(IAddress)
   */
  public int compareTo(final long addr) {
    return compare(m_address, addr);
  }

  /**
   * @see #equals(Object)
   */
  public boolean equals(final CAddress address) {
    return (address != null) && (m_address == address.m_address);
  }

  /**
   * @see #equals(Object)
   */
  public boolean equals(final IAddress address) {
    return (address != null) && (m_address == address.toLong());
  }

  /**
   * @see #equals(Object)
   */
  public boolean equals(final long address) {
    return m_address == address;
  }

  @Override
  public boolean equals(final Object address) {
    return (address instanceof IAddress) && (m_address == ((IAddress) address).toLong());
  }

  @Override
  public int hashCode() {
    return Long.valueOf(m_address).hashCode();
  }

  @Override
  public BigInteger toBigInteger() {
    // Use valueOf() to provide caching for frequently used values
    if ((m_address & 0x8000000000000000L) == 0) {
      return BigInteger.valueOf(m_address);
    }

    // Long.toHexString() interprets its argument unsigned
    return new BigInteger(Long.toHexString(m_address), 16);
  }

  @Override
  public String toHexString() {
    // Long.toHexString() interprets its argument unsigned
    return Strings.padStart(Long.toHexString(m_address).toUpperCase(),
        (m_address & 0x7fffffffffffffffL) < 0x100000000L ? 8 : 16, '0');
  }

  @Override
  public long toLong() {
    return m_address;
  }

  @Override
  public String toString() {
    return toHexString();
  }
}
