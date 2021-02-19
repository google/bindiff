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

package com.google.security.zynamics.bindiff.project.matches;

import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

// TODO(cblichmann): Replace this class with a class that holds a regular address pair and a single
//                   address. The way this class is currently used is like a struct.
public class AddressTriple {
  private final long firstAddr;
  private final long secondAddr;
  private final long thirdAddr;

  public AddressTriple(final long priAddr, final long secAddr, final long terAddr) {
    firstAddr = priAddr;
    secondAddr = secAddr;
    thirdAddr = terAddr;
  }

  @Override
  public boolean equals(final Object obj) {
    return (firstAddr == ((AddressTriple) obj).getAddress(EIndex.FIRST)
        && secondAddr == ((AddressTriple) obj).getAddress(EIndex.SECOND)
        && thirdAddr == ((AddressTriple) obj).getAddress(EIndex.THIRD));
  }

  public long getAddress(final EIndex index) {
    switch (index) {
      case FIRST:
        return firstAddr;
      case SECOND:
        return secondAddr;
      case THIRD:
        return thirdAddr;
    }

    throw new IllegalArgumentException("Illegal argument.");
  }

  public IAddress getIAddress(final EIndex index) {
    return new CAddress(getAddress(index));
  }

  @Override
  public int hashCode() {
    return (int)
        ((firstAddr ^ firstAddr >>> 32)
            * (firstAddr ^ secondAddr >>> 32)
            * (thirdAddr ^ thirdAddr >>> 32));
  }

  public enum EIndex {
    FIRST,
    SECOND,
    THIRD
  }
}
