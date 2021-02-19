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

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class FunctionDiffMetadata extends DiffMetadata {
  private IAddress priFunctionAddr;
  private IAddress secFunctionAddr;
  private String priFunctionName;
  private String secFunctionName;
  private EFunctionType priFunctionType;
  private EFunctionType secFunctionType;

  public FunctionDiffMetadata(
      final DiffMetadata metaData,
      final IAddress priFunctionAddr,
      final IAddress secFunctionAddr,
      final String priFunctionName,
      final String secFunctionName,
      final EFunctionType priFunctionType,
      final EFunctionType secFunctionType) {
    super(
        metaData.getVersion(),
        metaData.getDiffDescription(),
        metaData.getDate(),
        metaData.getTotalSimilarity(),
        metaData.getTotalConfidence(),
        metaData.getIdbName(ESide.PRIMARY),
        metaData.getIdbName(ESide.SECONDARY),
        metaData.getImageName(ESide.PRIMARY),
        metaData.getImageName(ESide.SECONDARY),
        metaData.getImageHash(ESide.PRIMARY),
        metaData.getImageHash(ESide.SECONDARY),
        metaData.getSimilarityIntervalCounts(),
        metaData.getSizeOfMatchedFunctions(),
        metaData.getSizeOfUnmatchedFunctions(ESide.PRIMARY),
        metaData.getSizeOfUnmatchedFunctions(ESide.SECONDARY),
        metaData.getSizeOfCalls(ESide.PRIMARY),
        metaData.getSizeOfCalls(ESide.SECONDARY),
        metaData.getSizeOfBasicBlocks(ESide.PRIMARY),
        metaData.getSizeOfBasicBlocks(ESide.SECONDARY),
        metaData.getSizeOfJumps(ESide.PRIMARY),
        metaData.getSizeOfJumps(ESide.SECONDARY),
        metaData.getSizeOfInstructions(ESide.PRIMARY),
        metaData.getSizeOfInstructions(ESide.SECONDARY));
    this.priFunctionAddr = priFunctionAddr;
    this.secFunctionAddr = secFunctionAddr;
    this.priFunctionName = priFunctionName;
    this.secFunctionName = secFunctionName;
    this.priFunctionType = priFunctionType;
    this.secFunctionType = secFunctionType;
  }

  public IAddress getFunctionAddress(final ESide side) {
    return side == ESide.PRIMARY ? priFunctionAddr : secFunctionAddr;
  }

  public String getFunctionName(final ESide side) {
    return side == ESide.PRIMARY ? priFunctionName : secFunctionName;
  }

  public EFunctionType getFunctionType(final ESide side) {
    return side == ESide.PRIMARY ? priFunctionType : secFunctionType;
  }

  public void setFunctionAddr(final IAddress addr, final ESide side) {
    if (side == ESide.PRIMARY) {
      priFunctionAddr = addr;
    } else {
      secFunctionAddr = addr;
    }
  }

  public void setFunctionName(final String name, final ESide side) {
    if (side == ESide.PRIMARY) {
      priFunctionName = name;
    } else {
      secFunctionName = name;
    }
  }

  public void setFunctionType(final EFunctionType type, final ESide side) {
    if (side == ESide.PRIMARY) {
      priFunctionType = type;
    } else {
      secFunctionType = type;
    }
  }
}
