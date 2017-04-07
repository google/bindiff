// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFormattedTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import com.google.common.base.Preconditions;

public class CFilenameFormatter extends JFormattedTextField.AbstractFormatter {
  private final static int MAX_CHAR = 255;

  private final FilenameFilter filter = new FilenameFilter();

  private final File m_directory;

  /**
   * Filter for JFormattedTextField. Ensures valid filenames. Does not work for directories.
   * 
   * @param directory Directory where a file with the specified filename is temporarily created for
   *        validation purpose.
   *        http://stackoverflow.com/questions/893977/java-how-to-find-out-whether
   *        -a-file-name-is-valid
   */
  public CFilenameFormatter(final File directory) {
    Preconditions.checkArgument(directory.exists(), "Error: Direcctory must exist.");
    m_directory = directory;
  }

  private boolean isValid(final String string, final int selected) {
    if ((((getFormattedTextField().getText().length() - selected) + string.length()) > MAX_CHAR)
        || (string.indexOf("\\") > -1) || (string.indexOf("/") > -1)) {
      invalidEdit();
      return false;
    }

    final File file = new File(m_directory.getPath() + File.separator + string);

    if (!file.exists()) {
      try {
        if (!file.createNewFile()) {
          invalidEdit();
          return false;
        }
        file.delete();
      } catch (final IOException e) {
        invalidEdit();
        return false;
      }
    }

    return true;
  }

  @Override
  protected DocumentFilter getDocumentFilter() {
    return filter;
  }

  @Override
  public Object stringToValue(final String s) {
    return s;
  }

  @Override
  public String valueToString(final Object o) {
    if (o == null) {
      return null;
    }
    return o.toString();
  }

  private class FilenameFilter extends DocumentFilter {
    @Override
    public void insertString(final DocumentFilter.FilterBypass fb, final int offset,
        final String string, final AttributeSet attr) throws BadLocationException {
      if (isValid(string, 0)) {
        super.insertString(fb, offset, string, attr);
      }
    }

    @Override
    public void replace(final DocumentFilter.FilterBypass fb, final int offset, final int length,
        final String string, final AttributeSet attr) throws BadLocationException {
      if (isValid(string, length)) {
        super.replace(fb, offset, length, string, attr);
      }
    }
  }
}
