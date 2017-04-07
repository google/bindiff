// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general.comparators;

import java.io.Serializable;
import java.util.Comparator;

import javax.swing.JCheckBox;

public class JCheckBoxComparator implements Comparator<JCheckBox>, Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2526854370340524821L;

  @Override
  public int compare(final JCheckBox o1, final JCheckBox o2) {
    return Boolean.valueOf(o1.isSelected()).compareTo(Boolean.valueOf(o2.isSelected()));
  }
}
