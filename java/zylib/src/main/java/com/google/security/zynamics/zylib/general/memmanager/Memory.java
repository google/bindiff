// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general.memmanager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.base.Preconditions;

/**
 * This class can be used to simulate memory.
 * 
 */
public class Memory {
  /**
   * List of memory chunks in the memory.
   */
  private final LinkedList<MemoryChunk> m_chunks = new LinkedList<MemoryChunk>();

  /**
   * List of listeners that are notified about changes in memory.
   */
  private final ArrayList<IMemoryListener> m_listeners = new ArrayList<IMemoryListener>();

  private final ReadWriteLock m_readWriteLock = new ReentrantReadWriteLock();

  private final Lock m_readLock = m_readWriteLock.readLock();

  private final Lock m_writeLock = m_readWriteLock.writeLock();

  /**
   * Concatenates two byte arrays.
   * 
   * @param data1 The first byte array.
   * @param data2 The second byte array.
   * 
   * @return The concatenated byte array.
   * 
   * @throws NullPointerException Thrown if either of the two source arrays is null.
   */
  private byte[] concat(final byte[] data1, final byte[] data2) {
    final byte[] newdata = new byte[data1.length + data2.length];

    System.arraycopy(data1, 0, newdata, 0, data1.length);
    System.arraycopy(data2, 0, newdata, data1.length, data2.length);

    return newdata;
  }

  /**
   * Connects two memory chunks, removes the two old chunks from memory, and inserts the new chunk
   * in their place.
   * 
   * @param firstChunk The first memory chunk.
   * @param secondChunk The second memory chunk.
   * 
   * @return The new memory chunk.
   * 
   * @throws NullPointerException Thrown if either of the two input chunks is null.
   * @throws IllegalArgumentException Thrown if the start address of the second memory chunk is
   *         smaller than or equal to the one of the first memory chunk.
   */
  private MemoryChunk connectChunks(final MemoryChunk firstChunk, final MemoryChunk secondChunk) {
    Preconditions.checkNotNull(firstChunk, "Error: First memory chunk can't be null");
    Preconditions.checkNotNull(secondChunk, "Error: Second memory chunk can't be null");
    Preconditions.checkArgument(secondChunk.getAddress() > firstChunk.getAddress(),
        "Error: Second memory chunk must start after the first memory chunk");

    final long newAddress = firstChunk.getAddress();

    MemoryChunk newChunk;

    if ((firstChunk.getAddress() + firstChunk.getLength()) == secondChunk.getAddress()) {
      // The two chunks fit perfectly together, it's only
      // necessary to concatenate them.

      final byte[] newData = concat(firstChunk.getBytes(), secondChunk.getBytes());

      newChunk = new MemoryChunk(newAddress, newData);
    } else {
      // The two chunks don't fit perfectly together.

      // Find out how many bytes to insert between the chunks.
      final int toFill =
          (int) (secondChunk.getAddress() - firstChunk.getAddress()) - firstChunk.getLength();

      // Create the new data array
      final byte[] newData = new byte[firstChunk.getLength() + toFill + secondChunk.getLength()];

      if (toFill > 0) {
        // If there is a gap between the two input chunks,
        // it is sufficient to copy the old data into the
        // new data array.
        // System.arraycopy(firstChunk.getBytes(), 0, newData, 0, firstChunk.getLength());
        // System.arraycopy(secondChunk.getBytes(), 0, newData, firstChunk.getLength() + toFill,
        // secondChunk.getLength());

        // Do not concatenate memory chunks with gaps
        // between them.

        return null;
      } else if (toFill < 0) {
        // If there is an overlap between the two memory
        // chunks, keep the data in the first chunk intact.

        System.arraycopy(firstChunk.getBytes(), 0, newData, 0, firstChunk.getLength());
        System.arraycopy(secondChunk.getBytes(), -toFill, newData, firstChunk.getLength(),
            secondChunk.getLength() + toFill);
      }

      newChunk = new MemoryChunk(newAddress, newData);
    }

    // Remove the two old chunks and insert the new chunk instead.
    removeChunk(firstChunk);
    removeChunk(secondChunk);

    insertChunk(newChunk);

    return newChunk;
  }

  /**
   * Finds the chunk that contains a given address.
   * 
   * @param address The address to search for.
   * 
   * @return The chunk the address belongs to or null if no such chunk exists.
   * 
   * @throws IllegalArgumentException Thrown if the address is less than 0.
   */
  private MemoryChunk findChunk(final long address) {
    Preconditions.checkArgument(address >= 0, "Error: Address can't be less than 0");

    for (final MemoryChunk chunk : m_chunks) {
      if ((address >= chunk.getAddress()) && (address < (chunk.getAddress() + chunk.getLength()))) {
        return chunk;
      }
    }

    return null;
  }

  /**
   * Finds the position of a chunk in the sorted list of memory chunks. The returned value is the
   * index of the list where the chunk would fit in.
   * 
   * @param chunk The chunk ins question.
   * 
   * @return The index where the chunk would fit into the list.
   * 
   * @throws NullPointerException Thrown if the memory chunk is null.
   */
  private int findChunkPosition(final MemoryChunk chunk) {
    Preconditions.checkNotNull(chunk, "Error: Memory chunk can't be null");

    final long address = chunk.getAddress();

    for (int i = 0; i < getNumberOfChunks(); i++) {
      if (address < m_chunks.get(i).getAddress()) {
        return i;
      }
    }

    return m_chunks.size();
  }

  private MemoryChunk findNextChunk(final long address) {
    Preconditions.checkArgument(address >= 0, "Error: Address can't be less than 0");

    for (final MemoryChunk chunk : m_chunks) {
      if (chunk.getAddress() >= address) {
        return chunk;
      }
    }

    return null;
  }

  /**
   * Inserts a chunk of memory into the list at the right position.
   * 
   * @param chunk The chunk to insert into the list.
   * 
   * @throws NullPointerException Thrown if the memory chunk is null.
   */
  private void insertChunk(final MemoryChunk chunk) {
    Preconditions.checkNotNull(chunk, "Error: Memory chunk can't be null");

    final int index = findChunkPosition(chunk);

    m_chunks.add(index, chunk);
  }

  /**
   * Notifies all listeners that the memory changed.
   */
  private void notifyListeners(final long address, final int size) {
    for (final IMemoryListener listener : m_listeners) {
      listener.memoryChanged(address, size);
    }
  }

  /**
   * Removes a memory chunk from memory.
   * 
   * @param chunk The memory chunk in question.
   * 
   * @throws NullPointerException Thrown if the memory chunk is null.
   */
  private void removeChunk(final MemoryChunk chunk) {
    Preconditions.checkNotNull(chunk, "Error: Memory chunk can't be null");

    m_chunks.remove(chunk);
  }

  /**
   * Splits a chunk into two chunks.
   * 
   * @param chunk The chunk to split.
   * @param address The split address.
   */
  private void splitChunk(final MemoryChunk chunk, final long address) {
    final byte[] oldData = chunk.getBytes();

    final byte[] newData1 = new byte[(int) (address - chunk.getAddress())];
    final byte[] newData2 = new byte[(chunk.getLength() - newData1.length)];

    System.arraycopy(oldData, 0, newData1, 0, newData1.length);
    System.arraycopy(oldData, oldData.length - newData2.length, newData2, 0, newData2.length);

    final MemoryChunk newChunk1 = new MemoryChunk(chunk.getAddress(), newData1);
    final MemoryChunk newChunk2 =
        new MemoryChunk((chunk.getAddress() + chunk.getLength()) - newData2.length, newData2);

    // Remove the old chunk
    removeChunk(chunk);

    // Insert the new chunks
    insertChunk(newChunk1);
    insertChunk(newChunk2);
  }

  /**
   * Adds a listener that is notified about changes in the simulated memory.
   * 
   * @param listener The listener to add to the nofification list.
   * 
   * @throws NullPointerException Thrown if the listener is null.
   */
  public void addMemoryListener(final IMemoryListener listener) {
    Preconditions.checkNotNull(listener, "Error: Listener can't be null");

    m_listeners.add(listener);
  }

  /**
   * Clears the simulated memory.
   */
  public void clear() {
    m_writeLock.lock();

    m_chunks.clear();

    m_writeLock.unlock();

    for (final IMemoryListener listener : m_listeners) {
      listener.memoryCleared();
    }
  }

  /**
   * Returns memory data. Note that it is necessary to call the function hasData before to make sure
   * that the data actually exists.
   * 
   * @param address The start address of the memory data.
   * @param length The length of the retrieved data.
   * 
   * @return The retrieved data.
   * 
   * @throws IllegalArgumentException Thrown if the address is negative or if the length is not
   *         positive.
   * @throws IllegalArgumentException Thrown if not all data is available.
   */
  public byte[] getData(final long address, final int length) {
    Preconditions.checkArgument(address >= 0, "Error: Address can't be less than 0");
    Preconditions.checkArgument(length > 0, "Error: Length must be positive");

    m_readLock.lock();

    MemoryChunk nextChunk = findChunk(address);
    int nextLength = length;
    long nextAddress = address;

    final byte[] data = new byte[length];

    int copied = 0;

    try {
      do {
        if (nextChunk == null) {
          // The chunk with the address could not be found
          // => The data is not available.
          throw new IllegalArgumentException("Error: Data is not available");
        } else if (((nextChunk.getAddress() + nextChunk.getLength()) - nextAddress) >= nextLength) {
          // Enough data is available in the chunk.
          // => The data is available.

          final int start = (int) (nextAddress - nextChunk.getAddress());

          System.arraycopy(nextChunk.getBytes(), start, data, copied, nextLength);

          return data;
        } else {
          // There is not enough data available in the
          // current chunk but the data could be in the
          // next chunk.

          // Copy the whole current chunk into the output
          // array.

          final int start = (int) (nextAddress - nextChunk.getAddress());
          final int toCopy = nextChunk.getLength() - start;

          System.arraycopy(nextChunk.getBytes(), start, data, copied, toCopy);

          copied += toCopy;

          // The next length is the required length minus
          // the length of the current chunk that was consumed.
          nextLength -= (nextChunk.getAddress() + nextChunk.getLength()) - nextAddress;

          // The next address is exactly the address behind
          // the current chunk because the whole current chunk
          // was consumed already.
          nextAddress = nextChunk.getAddress() + nextChunk.getLength();

          nextChunk = findChunk(nextAddress);
        }

      } while (true);
    } finally {
      m_readLock.unlock();
    }
  }

  /**
   * Returns the total size of the simulated memory.
   * 
   * @return The total size of the simulated memory.
   */
  public int getMemorySize() {
    m_readLock.lock();

    int size = 0;

    for (final MemoryChunk chunk : m_chunks) {
      size += chunk.getLength();
    }

    m_readLock.unlock();

    return size;
  }

  /**
   * Returns the number of memory chunks in memory.
   * 
   * @return The number of memory chunks in memory.
   */
  public int getNumberOfChunks() {
    m_readLock.lock();

    final int size = m_chunks.size();

    m_readLock.unlock();

    return size;
  }

  public long getSectionSize(final long address) {
    m_readLock.lock();

    final long start = getSectionStart(address);

    try {
      if (hasData(start, 1)) {
        MemoryChunk chunk = findChunk(start);

        long accusize = 0;

        do {
          if (!hasData(chunk.getAddress() + chunk.getLength(), 1)) {
            return (int) (accusize + chunk.getLength());
          } else {
            accusize += chunk.getLength();
            chunk = findChunk(chunk.getAddress() + chunk.getLength());
          }
        } while (true);
      } else if (m_chunks.size() == 0) {
        return 0x100000000L;
      } else {
        final MemoryChunk chunk = new MemoryChunk(start, 1);

        final int cpos = findChunkPosition(chunk);

        if (cpos == m_chunks.size()) {
          final MemoryChunk lc = m_chunks.get(cpos - 1);
          return 0x100000000L - lc.getAddress() - lc.getLength();
        } else {
          return m_chunks.get(cpos).getAddress() - start;
        }
      }
    } finally {
      m_readLock.unlock();
    }
  }

  public long getSectionStart(final long address) {
    try {
      m_readLock.lock();

      final MemoryChunk nextChunk = findChunk(address);

      if (nextChunk != null) {
        final long start = nextChunk.getAddress();

        if (start == 0) {
          return 0;
        } else {
          final MemoryChunk c2 = findChunk(start - 1);

          if (c2 != null) {
            return getSectionStart(start - 1);
          } else {
            return start;
          }
        }
      } else {
        final MemoryChunk mem = new MemoryChunk(address, 1);

        final int cpos = findChunkPosition(mem);

        if (cpos == 0) {
          return 0;
        } else {
          final MemoryChunk chunkBefore = m_chunks.get(cpos - 1);

          return chunkBefore.getAddress() + chunkBefore.getLength();
        }
      }
    } finally {
      m_readLock.unlock();
    }
  }

  /**
   * Determines whether the memory has length bytes starting from the given address.
   * 
   * @param address The start address.
   * @param length The length of the data.
   * 
   * @return True, if all bytes in the given range are available. False, otherwise.
   * 
   * @throws IllegalArgumentException Thrown if the address is negative or if the length is not
   *         positive.
   */
  public boolean hasData(final long address, final int length) {
    Preconditions.checkArgument(address >= 0, "Error: Address can't be less than 0");
    Preconditions.checkArgument(length > 0, "Error: Length must be positive");

    try {
      m_readLock.lock();

      MemoryChunk nextChunk = findChunk(address);
      int nextLength = length;
      long nextAddress = address;

      do {
        if (nextChunk == null) {
          // The chunk with the address could not be found
          // => The data is not available.
          return false;
        } else if (((nextChunk.getAddress() + nextChunk.getLength()) - nextAddress) >= nextLength) {
          // Enough data is available in the chunk.
          // => The data is available.
          return true;
        } else {
          // There is not enough data available in the
          // current chunk but the data could be in the
          // next chunk.

          // The next length is the required length minus
          // the length of the current chunk that was consumed.
          nextLength -= (nextChunk.getAddress() + nextChunk.getLength()) - nextAddress;

          // The next address is exactly the address behind
          // the current chunk because the whole current chunk
          // was consumed already.
          nextAddress = nextChunk.getAddress() + nextChunk.getLength();

          nextChunk = findChunk(nextAddress);
        }

      } while (true);
    } finally {
      m_readLock.unlock();
    }
  }

  /**
   * Prints the content of the memory to stdout.
   */
  public void printMemory() {
    m_readLock.lock();

    for (final MemoryChunk chunk : m_chunks) {
      chunk.print();
    }

    m_readLock.unlock();
  }

  /**
   * Removes a memory region from the memory.
   * 
   * @param address Start of the memory region to remove.
   * @param length Length of the memory region to remove.
   */
  public void remove(final long address, final int length) {
    Preconditions.checkArgument(address >= 0, "Error: Address can't be less than 0");
    Preconditions.checkArgument(length > 0, "Error: Length must be positive");

    try {
      m_writeLock.lock();

      final MemoryChunk chunk = findChunk(address);

      if (chunk == null) {
        // There is no chunk with the selected address. Now we have
        // to find out whether the gap to the next chunk is small enough
        // to delete some data from the next chunk.

        final MemoryChunk nextChunk = findNextChunk(address);

        if (nextChunk == null) {
          // There is no next chunk => nothing to delete
          return;
        } else if (nextChunk.getAddress() < (address + length)) {
          // There is some data in the next chunk that must be removed
          final int toRemove = (int) ((address + length) - nextChunk.getAddress());

          remove(nextChunk.getAddress(), toRemove);
        } else {
          // There is a next chunk but it's too far away. No data
          // must be deleted there.
          return;
        }
      } else if (chunk.getAddress() == address) {
        if (chunk.getLength() <= length) {
          // The chunk has the same address as the memory region to remove
          // and the memory region to remove is at least as big as the chunk.

          // Remove the chunk itself because it's completely covered by the memory region
          removeChunk(chunk);

          // If there are bytes left to delete, continue in the next chunk
          final int toDelete = length - chunk.getLength();

          if (toDelete > 0) {
            remove(address + chunk.getLength(), toDelete);
          }
        } else
        // if (chunk.getLength() > length)
        {
          // The chunk has the same address as the memory region
          // to remove but the chunk is larger. Get rid of the first
          // part of the chunk but keep the second part.

          // Split the chunk into a chunk that's deleted and a chunk that's kept
          splitChunk(chunk, address + length);

          // Remove the first chunk of the split operation
          removeChunk(findChunk(address));
        }
      } else {
        if ((chunk.getAddress() + chunk.getLength()) <= (address + length)) {
          // The chunk does not have the same address as the memory
          // region to delete but the chunk ends at least at the address
          // of the memory region.
          // In this case we keep the first part of the chunk and
          // delete the last part.

          // Split the chunk into a chunk that's kept and a chunk that's deleted
          splitChunk(chunk, address);

          // Remove the second chunk of the split operation
          final MemoryChunk deleteChunk = findChunk(address);
          removeChunk(deleteChunk);

          // If there are bytes left to delete, continue in the next chunk
          final int toRemove = length - deleteChunk.getLength();

          if (toRemove > 0) {
            // Remove the remaining bytes from the next chunk
            remove(address + deleteChunk.getLength(), toRemove);
          }
        } else
        // if (chunk.getAddress() + chunk.getLength() > address + length)
        {
          // The chunk does not have the same address as the memory region
          // and the chunk has a later end address. This means some part from
          // the middle of the chunk must be deleted.

          // Split the chunk into one part that's kept and one part that's partially removed
          splitChunk(chunk, address);

          // Split the second chunk again because it's partially deleted too
          final MemoryChunk secondChunk = findChunk(address);
          splitChunk(secondChunk, address + length);

          // The middle part can now be deleted
          removeChunk(findChunk(address));
        }
      }
    } finally {
      m_writeLock.unlock();
    }
  }

  public void removeMemoryListener(final IMemoryListener listener) {
    m_listeners.remove(listener);
  }

  /**
   * Stores new data at a given memory address.
   * 
   * @param address The address where the data is stored.
   * @param data The data to store.
   * 
   * @throws IllegalArgumentException Thrown if the address is less than 0.
   * @throws NullPointerException Thrown if the data object is null.
   */
  public void store(final long address, final byte[] data) {
    Preconditions.checkArgument(address >= 0, "Error: Address can't be less than 0");
    Preconditions.checkNotNull(data, "Error: Data can't be null");

    try {
      m_writeLock.lock();

      // Simply delete the old data before writing it, this makes things very simple.
      remove(address, data.length);

      final MemoryChunk chunk = new MemoryChunk(address, data);
      insertChunk(chunk);

      final MemoryChunk nextChunk = findChunk(address + chunk.getLength());

      if (nextChunk != null) {
        connectChunks(chunk, nextChunk);
      }
    } finally {
      m_writeLock.unlock();
    }

    notifyListeners(address, data.length);
  }
}
