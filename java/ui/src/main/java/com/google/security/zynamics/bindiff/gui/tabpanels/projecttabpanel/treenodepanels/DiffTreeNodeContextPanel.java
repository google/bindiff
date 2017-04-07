package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.database.MatchesDatabase;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.FunctionMatchesPie3dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.SimilarityBarChart2dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageTwoBarCellData;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageTwoBarLabel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.CPathLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class DiffTreeNodeContextPanel extends AbstractTreeNodeContextPanel {
  private static final int TEXTFIELDHEIGHT = 25;
  private static final int TEXTFIELDLABELWIDTH = 100;

  private final InternalDescriptionFocusListener focusListener =
      new InternalDescriptionFocusListener();
  private final InternalKeyListener keyListener = new InternalKeyListener();
  private final InternalTimerListener timerListener = new InternalTimerListener();

  private final CPathLabel diffPath;
  private final JTextField creationDate;

  private final JTextField primaryIDBName;
  private final JTextField primaryImageName;
  private final JTextField primaryImageHash;
  private final JTextField primaryArchitectureName;

  private final JTextField secondaryIDBName;
  private final JTextField secondaryImageName;
  private final JTextField secondaryImageHash;
  private final JTextField secondaryArchitectureName;

  private final JTextArea description;

  private final PercentageTwoBarLabel primaryFunctions;
  private final PercentageTwoBarLabel secondaryFunctions;

  private final Timer timer = new Timer(3500, timerListener);

  private final Diff diff;

  private final TitledBorder descriptionBorder = new TitledBorder("Description");

  private final WorkspaceTabPanelFunctions controller;

  public DiffTreeNodeContextPanel(final Diff diff, final WorkspaceTabPanelFunctions controller) {
    this.diff = Preconditions.checkNotNull(diff);
    this.controller = Preconditions.checkNotNull(controller);

    final DiffMetaData metaData = diff.getMetaData();

    creationDate = new JTextField(metaData.getDateString());
    primaryIDBName = new JTextField(metaData.getIdbName(ESide.PRIMARY));
    primaryImageName = new JTextField(metaData.getImageName(ESide.PRIMARY));
    primaryImageHash = new JTextField(metaData.getImageHash(ESide.PRIMARY));
    primaryArchitectureName = new JTextField(metaData.getArchitectureName(ESide.PRIMARY));
    secondaryIDBName = new JTextField(metaData.getIdbName(ESide.SECONDARY));
    secondaryImageName = new JTextField(metaData.getImageName(ESide.SECONDARY));
    secondaryImageHash = new JTextField(metaData.getImageHash(ESide.SECONDARY));
    secondaryArchitectureName = new JTextField(metaData.getArchitectureName(ESide.SECONDARY));

    final int matchedFunctionsCount = metaData.getSizeOfMatchedFunctions();
    final PercentageTwoBarCellData primaryMatchedFunctionsData =
        new PercentageTwoBarCellData(
            matchedFunctionsCount, metaData.getSizeOfUnmatchedFunctions(ESide.PRIMARY));
    final PercentageTwoBarCellData secondaryMatchedFunctionsData =
        new PercentageTwoBarCellData(
            matchedFunctionsCount, metaData.getSizeOfUnmatchedFunctions(ESide.SECONDARY));

    primaryFunctions =
        new PercentageTwoBarLabel(
            primaryMatchedFunctionsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_PRIMARY_LABEL_BAR,
            TEXTFIELDHEIGHT);
    secondaryFunctions =
        new PercentageTwoBarLabel(
            secondaryMatchedFunctionsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELDHEIGHT);

    diffPath = new CPathLabel(diff.getMatchesDatabase().getPath());
    description = new JTextArea(metaData.getDiffDescription());

    description.addFocusListener(focusListener);
    description.addKeyListener(keyListener);

    initComponents();

    loadDescription();
  }

  private JPanel createDescriptionPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(descriptionBorder);
    panel.add(new JScrollPane(description), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createDiffInfoPanel() {
    final JPanel panel = new JPanel(new GridLayout(1, 2, 2, 2));

    final JPanel primaryPanel = new JPanel(new BorderLayout());
    final JPanel secondaryPanel = new JPanel(new BorderLayout());

    final JPanel primary = new JPanel(new GridLayout(5, 1, 2, 2));
    final JPanel secondary = new JPanel(new GridLayout(5, 1, 2, 2));
    primaryPanel.setBorder(new TitledBorder("Primary Image"));
    secondaryPanel.setBorder(new TitledBorder("Secondary Image"));
    primary.setBorder(new LineBorder(Color.GRAY));
    secondary.setBorder(new LineBorder(Color.GRAY));
    primary.setBackground(Color.WHITE);
    secondary.setBackground(Color.WHITE);

    primary.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "IDB Name", TEXTFIELDLABELWIDTH, primaryIDBName, TEXTFIELDHEIGHT));
    primary.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Image Name", TEXTFIELDLABELWIDTH, primaryImageName, TEXTFIELDHEIGHT));
    primary.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Hash", TEXTFIELDLABELWIDTH, primaryImageHash, TEXTFIELDHEIGHT));
    primary.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Architecture", TEXTFIELDLABELWIDTH, primaryArchitectureName, TEXTFIELDHEIGHT));
    primary.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Functions", TEXTFIELDLABELWIDTH, primaryFunctions, TEXTFIELDHEIGHT));

    secondary.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "IDB Name", TEXTFIELDLABELWIDTH, secondaryIDBName, TEXTFIELDHEIGHT));
    secondary.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Image Name", TEXTFIELDLABELWIDTH, secondaryImageName, TEXTFIELDHEIGHT));
    secondary.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Hash", TEXTFIELDLABELWIDTH, secondaryImageHash, TEXTFIELDHEIGHT));
    secondary.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Architecture", TEXTFIELDLABELWIDTH, secondaryArchitectureName, TEXTFIELDHEIGHT));
    secondary.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Functions", TEXTFIELDLABELWIDTH, secondaryFunctions, TEXTFIELDHEIGHT));

    primaryPanel.add(primary, BorderLayout.CENTER);
    secondaryPanel.add(secondary, BorderLayout.CENTER);

    panel.add(primaryPanel);
    panel.add(secondaryPanel);

    return panel;
  }

  private JPanel createOverviewPanel() {
    final DiffMetaData metadata = diff.getMetaData();

    // Outer panel with title and frame
    final JPanel overviewBorderPanel = new JPanel(new BorderLayout(0, 0));
    overviewBorderPanel.setBorder(
        new CompoundBorder(new TitledBorder("Overview"), new LineBorder(Color.GRAY)));

    // Panel that contains the actual chart panels
    final JPanel chartsPanel = new JPanel(new GridLayout(1, 1, 0, 0));
    overviewBorderPanel.add(chartsPanel, BorderLayout.CENTER);

    chartsPanel.add(new FunctionMatchesPie3dPanel(metadata));
    chartsPanel.add(new SimilarityBarChart2dPanel(metadata));

    // Panel with filename and date
    final JPanel metadataPanel = new JPanel(new GridLayout(2, 1, 2, 2));
    metadataPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    metadataPanel.setBackground(Color.WHITE);

    metadataPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Diff Path", TEXTFIELDLABELWIDTH, diffPath, TEXTFIELDHEIGHT));
    metadataPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "File Date", TEXTFIELDLABELWIDTH, creationDate, TEXTFIELDHEIGHT));

    final JPanel outerMetadataPanel = new JPanel(new BorderLayout());
    outerMetadataPanel.setBorder(new TitledBorder("Diff Info"));
    outerMetadataPanel.add(metadataPanel, BorderLayout.CENTER);

    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(overviewBorderPanel, BorderLayout.CENTER);
    panel.add(outerMetadataPanel, BorderLayout.SOUTH);

    return panel;
  }

  private void initComponents() {
    diffPath.setPreferredSize(new Dimension(diffPath.getPreferredSize().width, TEXTFIELDHEIGHT));

    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(createOverviewPanel(), BorderLayout.CENTER);
    panel.add(createDiffInfoPanel(), BorderLayout.SOUTH);

    add(panel, BorderLayout.NORTH);
    add(createDescriptionPanel(), BorderLayout.CENTER);
  }

  private void loadDescription() {
    MatchesDatabase matchesDb = null;
    try {
      matchesDb = new MatchesDatabase(diff.getMatchesDatabase());
      description.setText(matchesDb.loadDiffDescription());
    } catch (final SQLException e) {
      // No message box (many diffs can be batch loaded)
      Logger.logException(e, "Load diff description failed.");
    }
  }

  private void saveDescription() {
    if (controller.saveDescription(diff, description.getText())) {
      descriptionBorder.setTitleColor(Colors.JUMP_CONDITIONAL_FALSE);
      descriptionBorder.setTitle("Saved description");
      this.updateUI();

      timer.start();
    }
  }

  public void dispose() {
    description.removeFocusListener(focusListener);
    description.removeKeyListener(keyListener);
  }

  @Override
  public List<AbstractTable> getTables() {
    return null;
  }

  private class InternalDescriptionFocusListener implements FocusListener {
    @Override
    public void focusGained(final FocusEvent event) {
      // do nothing
    }

    @Override
    public void focusLost(final FocusEvent event) {
      saveDescription();
    }
  }

  private final class InternalKeyListener implements KeyListener {
    private static final int KEY_TYPED_SAVE_THRESHOLD = 50;

    private int typedCount = 0;

    @Override
    public void keyPressed(final KeyEvent e) {}

    @Override
    public void keyReleased(final KeyEvent e) {}

    @Override
    public void keyTyped(final KeyEvent e) {
      typedCount++;

      if (typedCount > KEY_TYPED_SAVE_THRESHOLD) {
        saveDescription();
        typedCount = 0;
      }
    }
  }

  private final class InternalTimerListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      descriptionBorder.setTitleColor(Color.BLACK);
      descriptionBorder.setTitle("Description");
      DiffTreeNodeContextPanel.this.updateUI();

      timer.stop();
    }
  }
}
