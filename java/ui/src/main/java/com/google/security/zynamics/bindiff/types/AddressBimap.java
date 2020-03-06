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

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.matches.AddressPairComparator;
import com.google.security.zynamics.bindiff.project.matches.IAddressPair;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO(cblichmann): Check whether we can use Guava's Bimap here.
public class AddressBimap {
  private long[] primarySortedValues;
  private long[] secondaryAssignedValues;

  private long[] secondarySortedValues;
  private long[] primaryAssignedValues;

  public AddressBimap(final List<IAddressPair> addrPairs) {
    checkNotNull(addrPairs);

    final int size = addrPairs.size();

    primarySortedValues = new long[size];
    secondaryAssignedValues = new long[size];

    Collections.sort(addrPairs, new AddressPairComparator(ESide.PRIMARY));

    int index = 0;
    for (final IAddressPair addrPair : addrPairs) {
      primarySortedValues[index] = addrPair.getAddress(ESide.PRIMARY);
      secondaryAssignedValues[index] = addrPair.getAddress(ESide.SECONDARY);

      ++index;
    }

    secondarySortedValues = new long[size];
    primaryAssignedValues = new long[size];

    Collections.sort(addrPairs, new AddressPairComparator(ESide.SECONDARY));

    index = 0;
    for (final IAddressPair addrPair : addrPairs) {
      primaryAssignedValues[index] = addrPair.getAddress(ESide.PRIMARY);
      secondarySortedValues[index] = addrPair.getAddress(ESide.SECONDARY);

      ++index;
    }
  }

  private Long getPrimaryAddress(final long secondaryAddress) {
    final int index = getIndex(secondaryAddress, ESide.SECONDARY);

    return index >= 0 ? primaryAssignedValues[index] : null;
  }

  private Long getSecondaryAddress(final long primaryAddress) {
    final int index = getIndex(primaryAddress, ESide.PRIMARY);

    return index >= 0 ? secondaryAssignedValues[index] : null;
  }

  public boolean contains(final IAddress address, final ESide side) {
    return getIndex(address.toLong(), side) >= 0;
  }

  public boolean contains(final IAddress primaryAddress, final IAddress secondaryAddress) {
    if (primaryAddress == null || secondaryAddress == null) {
      return false;
    }

    final int index = getIndex(primaryAddress, ESide.PRIMARY);

    if (index >= 0) {
      return primarySortedValues[index] == primaryAddress.toLong()
          && secondaryAssignedValues[index] == secondaryAddress.toLong();
    }
    return false;
  }

  public int getIndex(final IAddress address, final ESide side) {
    return address != null ? getIndex(address.toLong(), side) : -1;
  }

  public int getIndex(final long address, final ESide side) {
    return side == ESide.PRIMARY
        ? Arrays.binarySearch(primarySortedValues, address)
        : Arrays.binarySearch(secondarySortedValues, address);
  }

  public IAddress getPrimaryAddress(final IAddress secondaryAddress) {
    final Long addr = getPrimaryAddress(secondaryAddress.toLong());

    return addr != null ? new CAddress(addr) : null;
  }

  public IAddress getSecondaryAddress(final IAddress primaryAddress) {
    final Long addr = getSecondaryAddress(primaryAddress.toLong());

    return addr != null ? new CAddress(addr) : null;
  }

  public boolean remove(final IAddress primaryAddress, final IAddress secondaryAddress) {
    final int priRemoveIndex = getIndex(primaryAddress, ESide.PRIMARY);
    final int secRemoveIndex = getIndex(secondaryAddress, ESide.SECONDARY);

    if (priRemoveIndex < 0 || secRemoveIndex < 0) {
      return false;
    }
    final int length = primarySortedValues.length;
    final long[] priTempSortedValues = new long[length - 1];
    final long[] secTempAssignedValues = new long[length - 1];

    final long[] secTempSortedValues = new long[length - 1];
    final long[] priTempAssignedValues = new long[length - 1];

    int priToSecIndex = 0;
    int secToPriIndex = 0;

    for (int i = 0; i < length; ++i) {
      if (i != priRemoveIndex) {
        priTempSortedValues[priToSecIndex] = primarySortedValues[i];
        secTempAssignedValues[priToSecIndex] = secondaryAssignedValues[i];

        ++priToSecIndex;
      }

      if (i != secRemoveIndex) {
        secTempSortedValues[secToPriIndex] = secondarySortedValues[i];
        priTempAssignedValues[secToPriIndex] = primaryAssignedValues[i];

        ++secToPriIndex;
      }
    }

    primarySortedValues = priTempSortedValues;
    secondaryAssignedValues = secTempAssignedValues;

    secondarySortedValues = secTempSortedValues;
    primaryAssignedValues = priTempAssignedValues;

    return true;
  }
}
