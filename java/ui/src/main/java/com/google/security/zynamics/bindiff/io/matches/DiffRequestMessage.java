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

package com.google.security.zynamics.bindiff.io.matches;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.diff.Diff;

/** Simple holder class storing the data for visual diff views requested from the disassemblers. */
public class DiffRequestMessage {
  private String matchesDBPath = "";
  private String primaryBinExport = "";
  private String secondaryBinExport = "";
  private long primaryFunctionAddress;
  private long secondaryFunctionAddress;

  private final Diff diff;

  public DiffRequestMessage() {
    diff = null;
  }

  public DiffRequestMessage(final Diff diff) {
    this.diff = diff;
  }

  public Diff getDiff() {
    return diff;
  }

  public String getBinExportPath(final ESide side) {
    return side == ESide.PRIMARY ? primaryBinExport : secondaryBinExport;
  }

  public long getFunctionAddress(final ESide side) {
    return side == ESide.PRIMARY ? primaryFunctionAddress : secondaryFunctionAddress;
  }

  public String getMatchesDBPath() {
    return matchesDBPath;
  }

  public void setBinExportPath(final String path, final ESide side) {
    if (side == ESide.PRIMARY) {
      primaryBinExport = path;
    } else {
      secondaryBinExport = path;
    }
  }

  public void setFunctionAddress(final long address, final ESide side) {
    if (side == ESide.PRIMARY) {
      primaryFunctionAddress = address;
    } else {
      secondaryFunctionAddress = address;
    }
  }

  public void setMatchesDBPath(final String path) {
    matchesDBPath = path;
  }
}
