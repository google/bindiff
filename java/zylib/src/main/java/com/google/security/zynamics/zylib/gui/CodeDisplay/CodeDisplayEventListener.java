package com.google.security.zynamics.zylib.gui.CodeDisplay;

import java.util.EventListener;

/**
 * Eventlistener for events specific to the CodeDisplay.
 */
public interface CodeDisplayEventListener extends EventListener {
  void caretChanged(CodeDisplayCoordinate caret);
}
