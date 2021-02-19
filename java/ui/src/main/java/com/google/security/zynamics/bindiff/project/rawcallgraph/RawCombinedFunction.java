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

package com.google.security.zynamics.bindiff.project.rawcallgraph;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class RawCombinedFunction extends CombinedViewNode {
  private final RawFunction primaryFunction;
  private final RawFunction secondaryFunction;

  public RawCombinedFunction(
      final RawFunction primaryFunction, final RawFunction secondaryFunction) {
    super();

    checkArgument(
        primaryFunction != null || secondaryFunction != null,
        "Primary function and secondary function cannot both be null");

    this.primaryFunction = primaryFunction;
    this.secondaryFunction = secondaryFunction;
  }

  @Override
  public IAddress getAddress(final ESide side) {
    if (getRawNode(side) == null) {
      return null;
    }

    return getRawNode(side).getAddress();
  }

  public EFunctionType getFunctionType() {
    if (primaryFunction == null) {
      return secondaryFunction.getFunctionType();
    }

    if (secondaryFunction == null) {
      return primaryFunction.getFunctionType();
    }

    if (primaryFunction.getFunctionType() == secondaryFunction.getFunctionType()) {
      return primaryFunction.getFunctionType();
    }

    return EFunctionType.MIXED;
  }

  @Override
  public RawFunction getRawNode(final ESide side) {
    if (side == ESide.PRIMARY) {
      return primaryFunction;
    }

    return secondaryFunction;
  }

  public boolean isChanged() {
    if (primaryFunction != null && secondaryFunction != null) {
      return primaryFunction.isChanged();
    }

    return false;
  }
}
