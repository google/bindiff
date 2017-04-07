// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general.undo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UndoStack implements Iterable<IUndoable> {
  private final List<IUndoable> undoStack = new ArrayList<IUndoable>();

  private int undoPosition = 0;

  public void add(final IUndoable operation) {
    final int toRemove = undoStack.size() - undoPosition;

    for (int i = 0; i < toRemove; i++) {
      undoStack.remove(undoPosition);
    }

    undoStack.add(operation);

    undoPosition++;
  }

  public boolean canRedo() {
    return undoPosition < undoStack.size();
  }

  public boolean canUndo() {
    return undoPosition > 0;
  }

  @Override
  public Iterator<IUndoable> iterator() {
    return undoStack.iterator();
  }

  public void redo() {
    final IUndoable operationToUndo = undoStack.get(undoPosition);

    operationToUndo.revertToSnapshot();

    undoPosition++;
  }

  public void undo() {
    final IUndoable operationToUndo = undoStack.get(undoPosition - 1);

    operationToUndo.undo();

    undoPosition--;
  }
}
