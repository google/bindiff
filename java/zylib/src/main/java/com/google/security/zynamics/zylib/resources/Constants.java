// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.resources;

import java.util.ResourceBundle;

public class Constants {
  public static final String OK;
  public static final String CANCEL;
  public static final String OK2;
  public static final String CANCEL2;

  public static final String MENU_FILE;
  public static final String MENU_CLOSE_ACTIVE_TAB;
  public static final String MENU_OPEN_NEW_TAB;
  public static final String MENU_LOAD_SCRIPT;
  public static final String MENU_SAVE_SCRIPT;
  public static final String MENU_SAVE_SCRIPT_AS;

  public static final String MENU_EXECUTE_SCRIPT;
  public static final String MENU_EXECUTE_AGAIN_SCRIPT;

  public static final String MESSAGE_RUNNING_SCRIPT;

  public static final String MENU_EDIT;
  public static final String MENU_EDIT_UNDO;
  public static final String MENU_EDIT_REDO;
  public static final String MENU_EDIT_CUT;
  public static final String MENU_EDIT_COPY;
  public static final String MENU_EDIT_PASTE;
  public static final String MENU_SCRIPTING_CONSOLE;

  public static final String COLOR_CHOOSER;
  public static final String RGB;
  public static final String RED;
  public static final String GREEN;
  public static final String BLUE;
  public static final String PREVIEW;

  public static final String HIDE_NEVER;
  public static final String HIDE_ALWAYS;
  public static final String THRESHOLD;

  public static final String ASK_FILE_OVERWRITE;

  static {
    final ResourceBundle resBundle =
        ResourceBundle.getBundle("com.google.security.zynamics.zylib.resources.Strings");

    OK = resBundle.getString("OK");
    CANCEL = resBundle.getString("CANCEL");
    OK2 = resBundle.getString("OK2");
    CANCEL2 = resBundle.getString("CANCEL2");

    PREVIEW = resBundle.getString("PREVIEW");


    MENU_FILE = resBundle.getString("MENU_FILE");
    MENU_CLOSE_ACTIVE_TAB = resBundle.getString("MENU_CLOSE_ACTIVE_TAB");
    MENU_OPEN_NEW_TAB = resBundle.getString("MENU_OPEN_NEW_TAB");
    MENU_LOAD_SCRIPT = resBundle.getString("MENU_LOAD_SCRIPT");
    MENU_SAVE_SCRIPT = resBundle.getString("MENU_SAVE_SCRIPT");
    MENU_SAVE_SCRIPT_AS = resBundle.getString("MENU_SAVE_SCRIPT_AS");

    MENU_EXECUTE_SCRIPT = resBundle.getString("MENU_EXECUTE_SCRIPT");
    MENU_EXECUTE_AGAIN_SCRIPT = resBundle.getString("MENU_EXECUTE_AGAIN_SCRIPT");

    MENU_EDIT = resBundle.getString("MENU_EDIT");
    MENU_EDIT_UNDO = resBundle.getString("MENU_EDIT_UNDO");
    MENU_EDIT_REDO = resBundle.getString("MENU_EDIT_REDO");
    MENU_EDIT_CUT = resBundle.getString("MENU_EDIT_CUT");
    MENU_EDIT_COPY = resBundle.getString("MENU_EDIT_COPY");
    MENU_EDIT_PASTE = resBundle.getString("MENU_EDIT_PASTE");
    MENU_SCRIPTING_CONSOLE = resBundle.getString("MENU_SCRIPTING_CONSOLE");

    MESSAGE_RUNNING_SCRIPT = resBundle.getString("MESSAGE_RUNNING_SCRIPT");

    COLOR_CHOOSER = resBundle.getString("COLOR_CHOOSER");
    RGB = resBundle.getString("RGB");
    RED = resBundle.getString("RED");
    GREEN = resBundle.getString("GREEN");
    BLUE = resBundle.getString("BLUE");

    HIDE_NEVER = resBundle.getString("HIDE_NEVER");
    HIDE_ALWAYS = resBundle.getString("HIDE_ALWAYS");
    THRESHOLD = resBundle.getString("THRESHOLD");

    ASK_FILE_OVERWRITE = resBundle.getString("ASK_FILE_OVERWRITE");
  }
}
