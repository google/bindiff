// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.resources.Constants;
import com.google.security.zynamics.zylib.system.SystemHelpers;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

public class CFileChooser extends JFileChooser {

  private final List<FileNameExtensionFilter> fileFilters =
      new ArrayList<FileNameExtensionFilter>();

  private final JCheckBox checkBox = new JCheckBox();

  /**
   * Whether to ask for an existing file to be overwritten. Only makes sense in dialogs of type
   * <code>JFileChooser.SAVE_DIALOG</code> or <code>JFileChooser.CUSTOM_DIALOG</code>.
   * 
   * @see JFileChooser#setDialogType(int)
   */
  private boolean askFileOverwrite = false;

  public CFileChooser() {
    this("");
  }

  public CFileChooser(final FileExtension... extensions) {
    super();
    Preconditions.checkNotNull(extensions, "File extensions can't be null.");

    for (final FileExtension ext : extensions) {
      final FileNameExtensionFilter fileFilter =
          new FileNameExtensionFilter(Arrays.asList(ext.extensions), ext.description);

      fileFilters.add(fileFilter);
      addChoosableFileFilter(fileFilter);
    }
    if (!fileFilters.isEmpty()) {
      setFileFilter(0);
    }
  }

  public CFileChooser(final List<String> fileExtensions, final String fileDescription) {
    this(
        new FileExtension[] {
          new FileExtension(
              Preconditions.checkNotNull(fileDescription),
              Preconditions.checkNotNull(fileExtensions).toArray(new String[0]))
        });
  }

  public CFileChooser(final String extension) {
    this(extension, "");
  }

  public CFileChooser(final String fileExtension, final String fileDescription) {
    this(Arrays.asList(new String[]{fileExtension}), fileDescription);
  }

  private static int showNativeFileDialog(final JFileChooser chooser) {
    final FileDialog result = new FileDialog((Frame) chooser.getParent());

    result.setDirectory(chooser.getCurrentDirectory().getPath());

    final File selected = chooser.getSelectedFile();
    result.setFile(selected == null ? "" : selected.getPath());
    result.setFilenameFilter(new FilenameFilter() {
      @Override
      public boolean accept(final File dir, final String name) {
        return chooser.getFileFilter().accept(new File(dir.getPath() + File.pathSeparator + name));
      }
    });

    if (chooser.getDialogType() == SAVE_DIALOG) {
      result.setMode(FileDialog.SAVE);
    } else {
      // The native dialog only support Open and Save
      result.setMode(FileDialog.LOAD);
    }

    if (chooser.getFileSelectionMode() == DIRECTORIES_ONLY) {
      System.setProperty("apple.awt.fileDialogForDirectories", "true");
    }

    // Display dialog
    result.setVisible(true);

    System.setProperty("apple.awt.fileDialogForDirectories", "false");
    if (result.getFile() == null) {
      return CANCEL_OPTION;
    }

    final String selectedDir = result.getDirectory();
    chooser
        .setSelectedFile(new File(FileUtils.ensureTrailingSlash(selectedDir) + result.getFile()));
    return APPROVE_OPTION;
  }

  @Override
  public void approveSelection() {
    if (askFileOverwrite && getSelectedFile().exists()) {
      if (CMessageBox.showYesNoQuestion(this, Constants.ASK_FILE_OVERWRITE) == JOptionPane.NO_OPTION) {
        return;
      }
    }
    super.approveSelection();
  }

  public boolean getAskFileOverwrite() {
    return askFileOverwrite;
  }

  public boolean isSelectedCheckBox() {
    return checkBox.isSelected();
  }

  public void setAskFileOverwrite(final boolean value) {
    askFileOverwrite = value;
  }

  public void setCheckBox(final String checkBoxText) {
    checkBox.setText(checkBoxText);
    checkBox.setBorder(new EmptyBorder(0, 0, 0, 0));

    final String approve = getApproveButtonText();
    final JButton approveButton =
        (JButton) GuiHelper.findComponentByPredicate(this, new GuiHelper.ComponentFilter() {
          @Override
          public boolean accept(final JComponent c) {
            if (!(c instanceof JButton)) {
              return false;
            }

            final String text = ((JButton) c).getText();
            if (text == null) {
              return approve == null;
            }
            if (approve == null) {
              return text == null;
            }
            return ((JButton) c).getText().equals(approve);
          }
        });

    JComponent parent = null;
    if (approveButton != null) {
      final Container approveParent = approveButton.getParent();
      if (approveParent instanceof JComponent) {
        parent = (JComponent) approveParent;
      }
    }

    if (parent == null) {
      // Fallback to using setAccessory() (ugly, but at least it works)
      setAccessory(checkBox);
      return;
    }

    // OS X is using SpringLayout, so we can just add the component and
    // are done.
    if (SystemHelpers.isRunningMacOSX()) {
      parent.add(checkBox, 0);
      return;
    }

    // Move the original buttons to a separate button panel while keeping
    // the original layout manager
    final JPanel buttonPanel = new JPanel(parent.getLayout());
    for (final Component c : parent.getComponents()) {
      buttonPanel.add(c);
    }

    // Set a new layout and add our checkbox
    parent.setLayout(new BorderLayout(0, 0));
    parent.add(checkBox, BorderLayout.LINE_START);
    parent.add(buttonPanel, BorderLayout.CENTER);
  }

  public void setFileFilter(final int index) {
    setFileFilter(fileFilters.get(index));
  }

  @Override
  public int showOpenDialog(final Component parent) throws HeadlessException {
    if (!SystemHelpers.isRunningMacOSX()) {
      return super.showOpenDialog(parent);
    }

    setDialogType(OPEN_DIALOG);
    return showNativeFileDialog(this);
  }

  private static class FileNameExtensionFilter extends FileFilter {
    private final List<String> fileExtensions;
    private final String fileDescription;

    public FileNameExtensionFilter(final List<String> fileExtensions, final String fileDescription) {
      this.fileExtensions = fileExtensions;
      this.fileDescription = fileDescription;
    }

    @Override
    public boolean accept(final File f) {
      boolean accept = false;
      final String filenameLower = f.getName().toLowerCase();

      for (final String ext : fileExtensions) {
        accept = filenameLower.endsWith(ext.toLowerCase());
        if (accept) {
          break;
        }
      }

      return accept || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return fileDescription;
    }
  }

  public static class FileExtension {
    private final String description;
    private final String[] extensions;

    public FileExtension(final String description, final String... extensions) {
      this.description = description;
      this.extensions = extensions;
    }
  }
}
