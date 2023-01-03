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

package com.google.security.zynamics.bindiff.project.rawcallgraph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class RawCall extends SingleViewEdge<RawFunction> {
  private final IAddress sourceInstructionAddr;

  private final ESide side;

  // Matched means in this case 'Identically Matched' (Source and target functions are matched as
  // well as the call instruction).
  // Changed calls are defined as unmatched, because target nodes are different. (Only source
  // function and the call address are matched).
  // NOTE: matchedPartnerCall is NOT null in case of a changed call!
  private EMatchState matchState;

  private RawCall matchedPartnerCall;

  public RawCall(
      final RawFunction sourceFunction,
      final RawFunction targetFunction,
      final IAddress sourceInstructionAddr,
      final ESide side) {
    super(sourceFunction, targetFunction);

    this.sourceInstructionAddr = checkNotNull(sourceInstructionAddr);
    this.side = checkNotNull(side);

    this.matchState =
        side == ESide.PRIMARY ? EMatchState.PRIMARY_UNMATCHED : EMatchState.SECONDRAY_UNMATCHED;
    this.matchedPartnerCall = null;
  }

  public RawCall getMatchedCall() {
    // NOTE: There is a matched partner call in case this call is changed!
    return matchedPartnerCall;
  }

  public EMatchState getMatchState() {
    // NOTE: Return primary or secondary unmatched in case of a changed call!
    checkNotNull(matchState);

    return matchState;
  }

  public ESide getSide() {
    return side;
  }

  public IAddress getSourceInstructionAddr() {
    return sourceInstructionAddr;
  }

  public boolean isChanged() {
    if (matchState != EMatchState.MATCHED && matchedPartnerCall != null) {
      final IAddress addr1 = getTarget().getMatchedFunctionAddress();
      if (addr1 == null) {
        return true;
      }

      final IAddress addr2 = matchedPartnerCall.getTarget().getAddress();
      return !addr1.equals(addr2);
    }

    return false;
  }

  public void setMatchState(final boolean matched, final RawCall matchedPartnerCall) {
    // NOTE: matched must be false if it's is a changed call.
    matchState =
        matched
            ? EMatchState.MATCHED
            : side == ESide.PRIMARY
                ? EMatchState.PRIMARY_UNMATCHED
                : EMatchState.SECONDRAY_UNMATCHED;

    this.matchedPartnerCall = matchedPartnerCall;
  }

  @Override
  public String toString() {
    return String.format(
        "%s@%s\u2192%s",
        getSource().getName(), getSourceInstructionAddr().toHexString(), getTarget().getName());
  }
}
