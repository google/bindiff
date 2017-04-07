package com.google.security.zynamics.bindiff.gui.components.viewsearchfield;

import java.util.List;

public interface IViewSearchFieldListener {
  void reset();

  void searched(List<Integer> rowIndices, boolean selectResultsOnly);
}
