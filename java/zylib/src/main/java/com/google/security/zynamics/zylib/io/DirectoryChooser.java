// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
