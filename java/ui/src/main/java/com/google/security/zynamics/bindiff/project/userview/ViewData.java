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

package com.google.security.zynamics.bindiff.project.userview;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.EViewType;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import java.util.Date;

/** Base class for storing view related metadata. */
public abstract class ViewData {
  private final GraphsContainer graphs;
  private String viewName;
  private String viewComment;
  private Date creationDate;
  private Date modificationDate;
  private final EViewType viewType;

  public ViewData(final GraphsContainer graphs, final String name, final EViewType viewType) {
    this.graphs = checkNotNull(graphs);
    this.viewName = checkNotNull(name);
    this.viewType = viewType;
    this.viewComment = "";
    this.creationDate = null;
    this.modificationDate = null;
  }

  public static String getViewName(final GraphsContainer graphs) {
    final Diff diff = graphs.getDiff();

    final IAddress priFunctionAddr = graphs.getPrimaryGraph().getFunctionAddress();
    final IAddress secFunctionAddr = graphs.getSecondaryGraph().getFunctionAddress();

    String priName = diff.getMetadata().getDisplayName(ESide.PRIMARY);
    if (priFunctionAddr != null) {
      final RawFunction function = diff.getFunction(priFunctionAddr, ESide.PRIMARY);
      if (function != null) {
        priName = function.getName();
      }
    } else if (secFunctionAddr != null) {
      priName = null;
    }

    String secName = diff.getMetadata().getDisplayName(ESide.SECONDARY);
    if (secFunctionAddr != null) {
      final RawFunction function = diff.getFunction(secFunctionAddr, ESide.SECONDARY);
      if (function != null) {
        secName = function.getName();
      }
    } else if (priFunctionAddr != null) {
      secName = null;
    }

    if (priName == null) {
      return secName;
    }

    if (secName == null) {
      return priName;
    }

    return String.format("%s vs %s", priName, secName);
  }

  public abstract IAddress getAddress(ESide side);

  public Date getCreationDate() {
    return creationDate;
  }

  public GraphsContainer getGraphs() {
    return graphs;
  }

  public EMatchState getMatchState() {
    if (isCallGraphView()) {
      return null;
    }

    if (getAddress(ESide.PRIMARY) != null && getAddress(ESide.SECONDARY) != null) {
      return EMatchState.MATCHED;
    }
    if (getAddress(ESide.PRIMARY) != null && getAddress(ESide.SECONDARY) == null) {
      return EMatchState.PRIMARY_UNMATCHED;
    }
    if (getAddress(ESide.PRIMARY) == null && getAddress(ESide.SECONDARY) != null) {
      return EMatchState.SECONDRAY_UNMATCHED;
    }

    return null;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public abstract MutableDirectedGraph<?, ?> getRawGraph(ESide side);

  public String getViewComment() {
    return viewComment;
  }

  public String getViewName() {
    return viewName;
  }

  public abstract boolean isCallGraphView();

  public abstract boolean isFlowGraphView();

  public boolean isSingleFunctionDiffView() {
    return viewType == EViewType.SINGLE_FUNCTION_DIFF_VIEW;
  }

  public void setCreationDate(final Date date) {
    creationDate = date;
  }

  public void setModificationDate(final Date date) {
    modificationDate = date;
  }

  public void setViewComment(final String comment) {
    viewComment = comment;
  }

  public void setViewName(final String viewName) {
    this.viewName = viewName;
  }
}
