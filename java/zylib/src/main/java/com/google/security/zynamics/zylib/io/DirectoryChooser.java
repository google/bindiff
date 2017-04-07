// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.io;

import com.google.security.zynamics.zylib.gui.CFileChooser;

import java.io.File;

import javax.swing.JFileChooser;


public class DirectoryChooser extends CFileChooser {
  private static final long serialVersionUID = 5354437749644373707L;

  public DirectoryChooser(final String title) {
    setCurrentDirectory(new File("."));
    setDialogTitle(title);
    setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    setAcceptAllFileFilterUsed(false);
  }
}
