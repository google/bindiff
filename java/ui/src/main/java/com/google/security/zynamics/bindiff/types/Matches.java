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

package com.google.security.zynamics.bindiff.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.matches.IAddressPair;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Dual indexed container for storing BinDiff matches and optional associated metadata. This is a
 * thin wrapper around Guava's Table class and indexes primary and secondary addresses.
 *
 * @param <Match> type of the match to store. Useful for associating additional data to matches.
 */
public class Matches<Match extends IAddressPair> {
  private final Table<Long, Long, Match> store = HashBasedTable.create();

  public Matches(final List<Match> basicBlockMatches) {
    for (final Match match : checkNotNull(basicBlockMatches)) {
      put(match);
    }
  }

  public void put(final Match match) {
    if (store.put(match.getAddress(ESide.PRIMARY), match.getAddress(ESide.SECONDARY), match)
        != null) {
      throw new IllegalArgumentException("Attempt to insert duplicate basic block match");
    }
  }

  public Match remove(final IAddress primaryAddr, final IAddress secondaryAddr) {
    return store.remove(primaryAddr.toLong(), secondaryAddr.toLong());
  }

  public void clear() {
    store.clear();
  }

  public int size() {
    return store.size();
  }

  public Match get(final IAddress addr, final ESide side) {
    if (addr == null) {
      return null;
    }
    final long plainAddress = addr.toLong();
    final Map<Long, Match> rowOrColumn =
        ESide.PRIMARY.equals(side) ? store.row(plainAddress) : store.column(plainAddress);
    final Collection<Match> matches = rowOrColumn.values();
    return !matches.isEmpty() ? matches.iterator().next() : null;
  }

  public Collection<Match> getMatches() {
    return Collections.unmodifiableCollection(store.values());
  }
}
