package com.google.security.zynamics.bindiff.resources;

import java.awt.Color;

public class Colors {
  // Base colors
  public static final Color PRIMARY_BASE = new Color(245, 210, 210);
  public static final Color SECONDARY_BASE = new Color(230, 235, 240);
  public static final Color MATCHED_BASE = new Color(211, 251, 222);
  public static final Color CHANGED_BASE = new Color(240, 241, 200);
  public static final Color MIXED_BASE_COLOR = new Color(160, 160, 0);

  public static final Color INSTRUCTIONS_ONLY_CHANGED_COLOR = new Color(235, 251, 211);

  // Chart colors
  public static final Color MATCHED_LABEL_BAR = new Color(176, 213, 176);
  public static final Color UNMATCHED_PRIMARY_LABEL_BAR = new Color(213, 176, 173);
  public static final Color UNMATCHED_SECONDARY_LABEL_BAR = new Color(183, 195, 205);

  public static final Color PIE_MATCHED = new Color(150, 200, 150);
  public static final Color PIE_PRIMARY_UNMATCHED = new Color(200, 150, 150);
  public static final Color PIE_SECONDARY_UNMATCHED = new Color(160, 175, 190);

  public static final Color LINECHART_SIMILARITY = new Color(128, 194, 0);
  public static final Color LINECHART_CONFIDENCE = new Color(96, 96, 160);

  // Table colors
  public static final Color TABLE_CELL_PRIMARY_DEFAULT_BACKGROUND = new Color(253, 240, 235);
  public static final Color TABLE_CELL_SECONDARY_DEFAULT_BACKGROUND = new Color(235, 240, 245);
  public static final Color TABLE_CELL_MATCHEDUSERVIEW_DEFAULT_BACKGROUND =
      new Color(242, 255, 242);

  public static final Color TABLE_CELL_MATCHED_BACKGROUND = MATCHED_LABEL_BAR;
  public static final Color TABLE_CELL_CHANGED_BACKGROUND = new Color(220, 220, 150);
  public static final Color TABLE_CELL_PRIMARY_UNMATCHED_BACKGROUND = UNMATCHED_PRIMARY_LABEL_BAR;
  public static final Color TABLE_CELL_SECONDARY_UNMATCHED_BACKGROUND =
      UNMATCHED_SECONDARY_LABEL_BAR;

  public static final Color TABLE_CELL_CALLS_LIBRARAY_BAR_COLOR = new Color(200, 230, 230);
  public static final Color TABLE_CELL_CALLS_NON_LIBRARAY_BAR_COLOR = new Color(210, 210, 245);

  // Graph colors
  public static final Color JUMP_CONDITIONAL_TRUE = new Color(0, 160, 0);
  public static final Color JUMP_CONDITIONAL_FALSE = new Color(160, 0, 0);
  public static final Color JUMP_SWITCH = new Color(0, 0, 160);
  public static final Color JUMP_UNCONDITIONAL = new Color(0, 0, 0);

  public static final Color CALL_PRIMARY_UNMATCHED = new Color(160, 80, 80);
  public static final Color CALL_SECONDRAY_UNMATCHED = new Color(95, 120, 160);
  public static final Color CALL_MATCHED = new Color(80, 130, 95);

  public static final Color GRAY32 = new Color(32, 32, 32);
  public static final Color GRAY64 = new Color(64, 64, 64);
  public static final Color GRAY128 = new Color(128, 128, 128);
  public static final Color GRAY160 = new Color(160, 160, 160);
  public static final Color GRAY192 = new Color(192, 192, 192);
  public static final Color GRAY224 = new Color(224, 224, 224);
  public static final Color GRAY240 = new Color(240, 240, 240);
  public static final Color GRAY250 = new Color(250, 250, 250);

  // Function type colors
  public static final Color FUNCTION_TYPE_NORMAL = new Color(245, 245, 245);
  public static final Color FUNCTION_TYPE_IMPORTED = new Color(250, 225, 190);
  public static final Color FUNCTION_TYPE_LIBRARY = new Color(220, 240, 200);
  public static final Color FUNCTION_TYPE_THUNK = new Color(230, 220, 245);
  public static final Color FUNCTION_TYPE_UNKNOWN = new Color(210, 210, 210);

  // Search highlight colors
  public static final Color SEARCH_HIGHLIGHT_COLOR = new Color(255, 128, 0);

  // Diff tab colors
  public static final Color DIFF_A_VIEW_TABS_COLOR = new Color(255, 255, 235);
  public static final Color DIFF_B_VIEW_TABS_COLOR = new Color(240, 255, 235);
  public static final Color DIFF_C_VIEW_TABS_COLOR = new Color(255, 245, 240);
  public static final Color DIFF_D_VIEW_TABS_COLOR = new Color(240, 245, 250);
}
