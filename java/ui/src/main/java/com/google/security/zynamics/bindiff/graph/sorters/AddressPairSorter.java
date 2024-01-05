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

package com.google.security.zynamics.bindiff.graph.sorters;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.types.AddressPairComparator;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressPairSorter {
  public static List<Pair<IAddress, IAddress>> getSortedList(
      final Collection<Pair<IAddress, IAddress>> addrPairs, final ESide sortBy) {
    // to fill up
    final List<Pair<IAddress, IAddress>> sortedAddrPairs = new ArrayList<>();

    // switch primary and secondary pair elements if sort by side is secondary
    final List<Pair<IAddress, IAddress>> tempAddrPairs = new ArrayList<>();
    if (sortBy == ESide.PRIMARY) {
      tempAddrPairs.addAll(addrPairs);
    } else {
      for (final Pair<IAddress, IAddress> addrPair : addrPairs) {
        tempAddrPairs.add(new Pair<>(addrPair.second(), addrPair.first()));
      }
    }

    // get primary and secondary address pair list
    final List<Pair<IAddress, IAddress>> primaryAddrPairs = new ArrayList<>();
    final List<Pair<IAddress, IAddress>> secondaryAddrPairs = new ArrayList<>();

    for (final Pair<IAddress, IAddress> addrPair : addrPairs) {
      if (addrPair.first() == null) {
        secondaryAddrPairs.add(addrPair);
      } else if (addrPair.second() == null) {
        primaryAddrPairs.add(addrPair);
      } else {
        primaryAddrPairs.add(addrPair);
        secondaryAddrPairs.add(addrPair);
      }
    }

    // sort primary and secondary addr pair lists
    Collections.sort(primaryAddrPairs, new AddressPairComparator(ESide.PRIMARY));
    Collections.sort(secondaryAddrPairs, new AddressPairComparator(ESide.SECONDARY));

    if (primaryAddrPairs.size() == addrPairs.size()) {
      sortedAddrPairs.addAll(primaryAddrPairs);
    } else if (secondaryAddrPairs.size() == addrPairs.size()) {
      sortedAddrPairs.addAll(secondaryAddrPairs);
    } else {
      // matched secondary address pair maps to secondary list index
      int index = 0;
      final Map<IAddress, Integer> secondaryAddrToListIndex = new HashMap<>();
      for (final Pair<IAddress, IAddress> pair : secondaryAddrPairs) {
        if (pair.first() != null && pair.second() != null) {
          secondaryAddrToListIndex.put(pair.second(), index);
        }

        index++;
      }

      // fill up sorted addr pairs
      int maxFallbackIndex = 0;
      for (final Pair<IAddress, IAddress> primaryPair : primaryAddrPairs) {
        if (primaryPair.second() == null) { // passes secondary unmatched only
          sortedAddrPairs.add(primaryPair);
        } else { // passes primary unmatched and matched only
          final IAddress secondaryAddr = primaryPair.second();

          final int secondaryListIndex = secondaryAddrToListIndex.get(secondaryAddr);

          for (index = maxFallbackIndex; index < secondaryListIndex; ++index) {
            final Pair<IAddress, IAddress> secondaryAddrPair = secondaryAddrPairs.get(index);

            sortedAddrPairs.add(secondaryAddrPair);
          }

          sortedAddrPairs.add(primaryPair);
          maxFallbackIndex = secondaryListIndex + 1;
        }
      }

      // add unmatched secondary tail
      for (index = maxFallbackIndex; index < secondaryAddrPairs.size(); ++index) {
        final Pair<IAddress, IAddress> secondaryAddrPair = secondaryAddrPairs.get(index);

        sortedAddrPairs.add(secondaryAddrPair);
      }
    }

    // re-switch primary and secondary pair elements if sort by side is secondary
    if (sortBy == ESide.SECONDARY) {
      final List<Pair<IAddress, IAddress>> tempSortedAddrPairs = new ArrayList<>();
      for (final Pair<IAddress, IAddress> addrPair : sortedAddrPairs) {
        tempSortedAddrPairs.add(new Pair<>(addrPair.second(), addrPair.second()));
      }

      return tempSortedAddrPairs;
    }

    return sortedAddrPairs;
  }
}
