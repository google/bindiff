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

package com.google.security.zynamics.zylib.general.memmanager;

import com.google.common.base.Preconditions;

/**
 * Used to store a contiguous chunk of memory.
 * 
 */
public class MemoryChunk implements Comparable<MemoryChunk> {
  /**
   * The address of the memory chunk.
   */
  private final long m_address;

  /**
   * The data inside the chunk.
   */
  private byte[] m_data;

  /**
   * Creates a new memory chunk object with the given byte data.
   * 
   * @param address The start address of the chunk.
   * @param data The initial data of the memory chunk.
   * 
   * @throws IllegalArgumentException Thrown if the address is less than 0.
   * @throws NullPointerException Thrown if the initial data is null.
   */
  public MemoryChunk(final long address, final byte[] data) {
    Preconditions.checkArgument(address >= 0, "Error: Chunk addresses can't be less than 0");
    Preconditions.checkNotNull(data, "Error: Initial data can't be null");
    Preconditions.checkArgument(data.length != 0, "Error: Initial data can't be empty");

    m_address = address;
    m_data = data;
  }

  /**
   * Creates a new memory chunk object that is initialized with bytes of value 0.
   * 
   * @param address The start address of the chunk.
   * @param size The size of the chunk.
   */
  public MemoryChunk(final long address, final int size) {
    this(address, new byte[size]);
  }

  /**
   * Used to order chunks according to their address.
   */
  @Override
  public int compareTo(final MemoryChunk chunk) {
    return Long.compare(m_address, chunk.m_address);
  }

  /**
   * Adds a number of 0-bytes at the end of the memory chunk.
   * 
   * @param size The number of 0-bytes to add.
   * 
   * @throws IllegalArgumentException Thrown if the number of bytes to add is not positive.
   */
  public void extend(final int size) {
    Preconditions.checkArgument(size > 0, "Error: The number of bytes to add must be positive");

    final byte[] data = new byte[m_data.length + size];

    System.arraycopy(m_data, 0, data, 0, m_data.length);

    m_data = data;
  }

  /**
   * Returns the start address of the memory chunk.
   * 
   * @return The start address of the memory chunk.
   */
  public long getAddress() {
    return m_address;
  }

  /**
   * Returns the data of the memory chunk.
   * 
   * @return The data of the memory chunk.
   */
  public byte[] getBytes() {
    return m_data;
  }

  /**
   * Returns the length of the memory chunk.
   * 
   * @return The length of the memory chunk.
   */
  public int getLength() {
    return m_data.length;
  }

  /**
   * Loads a single byte from the given address.
   * 
   * @param address The address where the byte is stored.
   * 
   * @return The value of the byte at the given address.
   * 
   * @throws IndexOutOfBoundsException Thrown if the address is not part of the memory chunk.
   */
  public byte loadByte(final long address) {
    return m_data[(int) (address - m_address)];
  }

  /**
   * Prints the content of the memory chunk to stdout.
   */
  public void print() {
    System.out.printf("%08X: ", m_address);

    for (final byte element : m_data) {
      System.out.printf("%02X ", element);
    }

    System.out.println();
  }

  /**
   * Stores a range of bytes in the memory chunk.
   * 
   * @param address The start address of the new data.
   * @param data The new data.
   * 
   * @throws IndexOutOfBoundsException Thrown if the memory chunk is not large enough to hold the
   *         data.
   * @throws NullPointerException Thrown if the passed data is null.
   */
  public void store(final long address, final byte[] data) {
    System.arraycopy(data, 0, m_data, (int) (address - m_address), data.length);
  }

  /**
   * Stores a single byte at the given address.
   * 
   * @param address The address where the byte is stored.
   * @param b The value of the byte.
   * 
   * @throws IndexOutOfBoundsException Thrown if the address is not part of the memory chunk.
   */
  public void storeByte(final long address, final byte b) {
    m_data[(int) (address - m_address)] = b;
  }
}
