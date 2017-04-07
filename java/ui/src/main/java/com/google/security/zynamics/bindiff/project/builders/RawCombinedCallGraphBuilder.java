package com.google.security.zynamics.bindiff.project.builders;

import com.google.security.zynamics.bindiff.exceptions.GraphCreationException;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCall;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedCall;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RawCombinedCallGraphBuilder {
  private static List<RawCombinedCall> buildCombinedCalls(
      final RawCallGraph priCallgraph,
      final RawCallGraph secCallgraph,
      final Map<Pair<IAddress, IAddress>, RawCombinedFunction> helpMap) {
    final List<RawCombinedCall> combinedCalls = new ArrayList<>();

    for (final RawCall priCall : priCallgraph.getEdges()) {
      final RawFunction priSrcFunction = priCall.getSource();
      final RawFunction priTarFunction = priCall.getTarget();
      final RawFunction secSrcFunction = priSrcFunction.getMatchedFunction();
      final RawFunction secTarFunction = priTarFunction.getMatchedFunction();

      final IAddress priSrcAddr = priSrcFunction.getAddress();
      final IAddress priTarAddr = priTarFunction.getAddress();
      final IAddress secSrcAddr = secSrcFunction == null ? null : secSrcFunction.getAddress();
      final IAddress secTarAddr = secTarFunction == null ? null : secTarFunction.getAddress();

      RawCall secCall = priCall.getMatchedCall();
      if (secCall != null && secCall.isChanged()) {
        secCall = null;
      }

      final RawCombinedFunction combinedSource = helpMap.get(new Pair<>(priSrcAddr, secSrcAddr));
      final RawCombinedFunction combinedTarget = helpMap.get(new Pair<>(priTarAddr, secTarAddr));

      RawCombinedCall combinedCall =
          new RawCombinedCall(combinedSource, combinedTarget, priCall, secCall);

      combinedCalls.add(combinedCall);
    }

    for (final RawCall secCall : secCallgraph.getEdges()) {
      if (secCall.getMatchedCall() == null || secCall.getMatchedCall().isChanged()) {
        final RawFunction secSrcFunction = secCall.getSource();
        final RawFunction secTarFunction = secCall.getTarget();
        final RawFunction priSrcFunction = secSrcFunction.getMatchedFunction();
        final RawFunction priTarFunction = secTarFunction.getMatchedFunction();

        final IAddress secSrcAddr = secSrcFunction.getAddress();
        final IAddress secTarAddr = secTarFunction.getAddress();
        final IAddress priSrcAddr = priSrcFunction == null ? null : priSrcFunction.getAddress();
        final IAddress priTarAddr = priTarFunction == null ? null : priTarFunction.getAddress();

        final RawCombinedFunction combinedSource = helpMap.get(new Pair<>(priSrcAddr, secSrcAddr));
        final RawCombinedFunction combinedTarget = helpMap.get(new Pair<>(priTarAddr, secTarAddr));

        RawCombinedCall combinedCall =
            new RawCombinedCall(combinedSource, combinedTarget, null, secCall);

        combinedCalls.add(combinedCall);
      }
    }

    return combinedCalls;
  }

  private static List<RawCombinedFunction> buildCombinedFunctions(
      final MatchData matches,
      final RawCallGraph priCallgraph,
      final RawCallGraph secCallgraph,
      final Map<Pair<IAddress, IAddress>, RawCombinedFunction> helpMap) {
    // build combined primary and secondary matched functions add secondary unmatched functions
    final List<RawCombinedFunction> combinedFunctions = new ArrayList<>();

    for (final RawFunction primaryfunction : priCallgraph.getNodes()) {
      final IAddress primaryAddr = primaryfunction.getAddress();
      final IAddress secondaryAddr = matches.getSecondaryFunctionAddr(primaryAddr);

      RawFunction secondaryFunction = null;
      if (secondaryAddr != null) {
        secondaryFunction = secCallgraph.getFunction(secondaryAddr);
      }

      final RawCombinedFunction combinedFunction =
          new RawCombinedFunction(primaryfunction, secondaryFunction);
      helpMap.put(new Pair<>(primaryAddr, secondaryAddr), combinedFunction);

      combinedFunctions.add(combinedFunction);
    }

    // build combined secondary unmatched functions
    for (final RawFunction secondaryFunction : secCallgraph.getNodes()) {
      final IAddress secondaryAddr = secondaryFunction.getAddress();
      final IAddress primaryAddr = matches.getPrimaryFunctionAddr(secondaryAddr);

      if (primaryAddr == null) {
        final RawCombinedFunction combinedFunction =
            new RawCombinedFunction(null, secondaryFunction);
        helpMap.put(new Pair<>(null, secondaryAddr), combinedFunction);

        combinedFunctions.add(combinedFunction);
      }
    }

    return combinedFunctions;
  }

  public static RawCombinedCallGraph buildCombinedCallgraph(
      final MatchData matches,
      final RawCallGraph primaryRawCallgraph,
      final RawCallGraph secondaryRawCallgraph)
      throws GraphCreationException {
    Logger.logInfo(" - Building combined call graph");

    try {
      // TODO: Use AddressPair here
      final Map<Pair<IAddress, IAddress>, RawCombinedFunction> helpMap = new HashMap<>();

      final List<RawCombinedFunction> combinedFunctions;
      combinedFunctions =
          buildCombinedFunctions(matches, primaryRawCallgraph, secondaryRawCallgraph, helpMap);

      List<RawCombinedCall> combinedCalls =
          buildCombinedCalls(primaryRawCallgraph, secondaryRawCallgraph, helpMap);

      helpMap.clear();

      return new RawCombinedCallGraph(combinedFunctions, combinedCalls);
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      throw new GraphCreationException(
          "Combined call graph creation failed." + " " + e.getMessage());
    }
  }
}
