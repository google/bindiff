// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

import java.math.BigInteger;

/**
 * Interface that abstracts the concept of (memory) addresses.
 */
public interface IAddress extends Comparable<IAddress> {
  /**
   * Returns a {@code BigInteger} with the same numerical value as this address.
   * 
   * @return this address as a {@code BigInteger}
   */
  BigInteger toBigInteger();

  /**
   * Returns the value of this {@code IAddress} as a hexadecimal string left-padded with 0. The
   * string returned has no prefixes or suffixes. Depending on the size of the address, the length
   * of the returned address is either 8 or 16 characters.
   * 
   * @return this address in hexadecimal.
   */
  String toHexString();

  /**
   * Returns a representation of this {@code IAddress} as a Java {@code long}. The value returned
   * will be negative for addresses larger than 0x7FFFFFFFFFFFFFFF, since {@code long} is signed.
   * 
   * @return a Java {@code long} representing this address.
   */
  long toLong();
}
