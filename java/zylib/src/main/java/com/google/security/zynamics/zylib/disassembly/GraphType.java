// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

public enum GraphType {
  CALLGRAPH, FLOWGRAPH, MIXED_GRAPH;

  public static String parseString(final GraphType type) {
    switch (type) {
      case CALLGRAPH:
        return "Call graph";
      case FLOWGRAPH:
        return "Flow graph";
      case MIXED_GRAPH:
        return "Mixed Graph";
    }

    return "Unknown";
  }
}
