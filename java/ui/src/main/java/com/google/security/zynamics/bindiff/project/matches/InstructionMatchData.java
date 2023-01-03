// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.project.matches;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class InstructionMatchData implements IAddressPair {
  private final IAddressPair addrPair;

  public InstructionMatchData(final long priAddr, final long secAddr) {
    addrPair = new AddressPair(priAddr, secAddr);
  }

  @Override
  public long getAddress(final ESide side) {
    return addrPair.getAddress(side);
  }

  @Override
  public IAddress getIAddress(final ESide side) {
    return new CAddress(getAddress(side));
  }
}
