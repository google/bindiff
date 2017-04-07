package com.google.security.zynamics.bindiff.project.userview;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewManager {
  // Note: Each Diff object has it's own instance of ViewManager containing all opened views
  // belong to that Diff object

  private final Set<ViewData> views = new HashSet<>();

  public void addView(final ViewData view) {
    views.add(view);
  }

  public boolean containsView(final IAddress priFunctionAddr, final IAddress secFunctionAddr) {
    for (final ViewData view : views) {
      if (view.isFlowgraphView()) {
        final FlowGraphViewData flowgraphView = (FlowGraphViewData) view;

        boolean priIsEqual =
            priFunctionAddr == null && flowgraphView.getRawGraph(ESide.PRIMARY) == null;
        IAddress priAddr = null;
        if (flowgraphView.getRawGraph(ESide.PRIMARY) != null) {
          priAddr = flowgraphView.getRawGraph(ESide.PRIMARY).getAddress();
        }

        if (priFunctionAddr != null && priAddr != null) {
          priIsEqual = priFunctionAddr.equals(priAddr);
        }

        boolean secIsEqual =
            secFunctionAddr == null && flowgraphView.getRawGraph(ESide.SECONDARY) == null;
        IAddress secAddr = null;
        if (flowgraphView.getRawGraph(ESide.SECONDARY) != null) {
          secAddr = flowgraphView.getRawGraph(ESide.SECONDARY).getAddress();
        }

        if (secFunctionAddr != null && secAddr != null) {
          secIsEqual = secFunctionAddr.equals(secAddr);
        }

        if (priIsEqual && secIsEqual) {
          return true;
        }
      }
      if (view.isCallgraphView()) {
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

  public CallGraphViewData getCallgraphViewData(final Diff diff) {
    for (final CallGraphViewData viewData : getCallgraphViewsData()) {
      if (viewData.getGraphs().getDiff() == diff) {
        return viewData;
      }
    }

    return null;
  }

  public List<CallGraphViewData> getCallgraphViewsData() {
    final ArrayList<CallGraphViewData> callgraphViews = new ArrayList<>();

    for (final ViewData viewData : views) {
      if (viewData instanceof CallGraphViewData) {
        callgraphViews.add((CallGraphViewData) viewData);
      }
    }

    return callgraphViews;
  }

  public FlowGraphViewData getFlowgraphViewData(
      final IAddress priFunctionAddr, final IAddress secFunctionAddr) {
    for (final ViewData viewData : views) {
      if (viewData.isFlowgraphView() && viewData.getMatchState() == EMatchState.MATCHED) {
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

  public List<FlowGraphViewData> getFlowgraphViewsData() {
    final ArrayList<FlowGraphViewData> flowgraphViews = new ArrayList<>();

    for (final ViewData viewData : views) {
      if (viewData instanceof FlowGraphViewData) {
        flowgraphViews.add((FlowGraphViewData) viewData);
      }
    }

    return flowgraphViews;
  }

  public void removeView(final ViewData view) {
    views.remove(view);
  }
}
