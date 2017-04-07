package com.google.security.zynamics.bindiff.io.matches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgorithmNameCache {
  private final List<String> names = new ArrayList<>();

  private final Map<String, Byte> nameToIndexMap = new HashMap<>();

  private byte lastIndex = 0;

  public String get(final String name) {
    final Byte index = nameToIndexMap.get(name);
    if (index == null) {
      names.add(name);
      nameToIndexMap.put(name, lastIndex++);
    }
    return names.get(index);
  }
}
