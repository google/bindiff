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

import com.google.security.zynamics.bindiff.enums.ESide;

public class FunctionMatchCounts {
  private final int matchedBasicblocksCount;
  private final int priBasicblocksCount;
  private final int secBasicblockCount;

  private final int matchedJumpsCount;
  private final int priJumpsCount;
  private final int secJumpsCount;

  private final int matchedInstructionsCount;
  private final int priInstructionsCount;
  private final int secInstructionsCount;

  public FunctionMatchCounts(
      final int matchedBasicblocks,
      final int priBasicblocks,
      final int secBasicblocks,
      final int matchedJumps,
      final int priJumps,
      final int secJumps,
      final int matchedInstructions,
      final int priInstructions,
      final int secInstructions) {
    matchedBasicblocksCount = matchedBasicblocks;
    priBasicblocksCount = priBasicblocks;
    secBasicblockCount = secBasicblocks;

    matchedJumpsCount = matchedJumps;
    priJumpsCount = priJumps;
    secJumpsCount = secJumps;

    matchedInstructionsCount = matchedInstructions;
    priInstructionsCount = priInstructions;
    secInstructionsCount = secInstructions;
  }

  public int getBasicblocksCount(final ESide side) {
    return side == ESide.PRIMARY ? priBasicblocksCount : secBasicblockCount;
  }

  public int getInstructionsCount(final ESide side) {
    return side == ESide.PRIMARY ? priInstructionsCount : secInstructionsCount;
  }

  public int getJumpsCount(final ESide side) {
    return side == ESide.PRIMARY ? priJumpsCount : secJumpsCount;
  }

  public int getMatchedBasicblocksCount() {
    return matchedBasicblocksCount;
  }

  public int getMatchedInstructionsCount() {
    return matchedInstructionsCount;
  }

  public int getMatchedJumpsCount() {
    return matchedJumpsCount;
  }
}
