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

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Contains view data for each opened view of a Diff object. */
public class ViewManager {
  private final Set<ViewData> views = new HashSet<>();

  public void addView(final ViewData view) {
    views.add(view);
  }

  public boolean containsView(final IAddress priFunctionAddr, final IAddress secFunctionAddr) {
    for (final ViewData view : views) {
      if (view.isFlowGraphView()) {
        final FlowGraphViewData flowGraphView = (FlowGraphViewData) view;

        boolean priIsEqual =
            priFunctionAddr == null && flowGraphView.getRawGraph(ESide.PRIMARY) == null;
        IAddress priAddr = null;
        if (flowGraphView.getRawGraph(ESide.PRIMARY) != null) {
          priAddr = flowGraphView.getRawGraph(ESide.PRIMARY).getAddress();
        }

        if (priFunctionAddr != null && priAddr != null) {
          priIsEqual = priFunctionAddr.equals(priAddr);
        }

        boolean secIsEqual =
            secFunctionAddr == null && flowGraphView.getRawGraph(ESide.SECONDARY) == null;
        IAddress secAddr = null;
        if (flowGraphView.getRawGraph(ESide.SECONDARY) != null) {
          secAddr = flowGraphView.getRawGraph(ESide.SECONDARY).getAddress();
        }

        if (secFunctionAddr != null && secAddr != null) {
          secIsEqual = secFunctionAddr.equals(secAddr);
        }

        if (priIsEqual && secIsEqual) {
          return true;
        }
      }
      if (view.isCallGraphView()) {
        if (view.getAddress(ESide.PRIMARY) == null
            && priFunctionAddr == null
            && view.getAddress(ESide.SECONDARY) == null
            && secFunctionAddr == null) {
          return true;
        }
      }
    }
    return false;
  }

  public CallGraphViewData getCallGraphViewData(final Diff diff) {
    for (final CallGraphViewData viewData : getCallGraphViewsData()) {
      if (viewData.getGraphs().getDiff() == diff) {
        return viewData;
      }
    }
    return null;
  }

  public List<CallGraphViewData> getCallGraphViewsData() {
    final ArrayList<CallGraphViewData> callGraphViews = new ArrayList<>();

    for (final ViewData viewData : views) {
      if (viewData instanceof CallGraphViewData) {
        callGraphViews.add((CallGraphViewData) viewData);
      }
    }
    return callGraphViews;
  }

  public FlowGraphViewData getFlowGraphViewData(
      final IAddress priFunctionAddr, final IAddress secFunctionAddr) {
    for (final ViewData viewData : views) {
      if (viewData.isFlowGraphView() && viewData.getMatchState() == EMatchState.MATCHED) {
        final FlowGraphViewData flowgraphViewData = (FlowGraphViewData) viewData;

        final IAddress priViewAddr = flowgraphViewData.getAddress(ESide.PRIMARY);
        final IAddress secViewAddr = flowgraphViewData.getAddress(ESide.SECONDARY);

        if (priViewAddr.equals(priFunctionAddr) && secViewAddr.equals(secFunctionAddr)) {
          return flowgraphViewData;
        }
      }
    }
    return null;
  }

  public List<FlowGraphViewData> getFlowGraphViewsData() {
    final ArrayList<FlowGraphViewData> flowGraphViews = new ArrayList<>();

    for (final ViewData viewData : views) {
      if (viewData instanceof FlowGraphViewData) {
        flowGraphViews.add((FlowGraphViewData) viewData);
      }
    }
    return flowGraphViews;
  }

  public void removeView(final ViewData view) {
    views.remove(view);
  }
}
