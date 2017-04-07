package com.google.security.zynamics.bindiff.project.diff;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.ViewManager;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.CFileUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.io.File;
import java.io.IOException;

public final class Diff {
  private final ListenerProvider<IDiffListener> listenerProvider = new ListenerProvider<>();

  private File matchesDatabaseFile;
  private File primaryExportFile;
  private File secondaryExportFile;

  private String priExportMD5 = null;
  private String secExportMD5 = null;

  private final ViewManager viewManager = new ViewManager();

  private RawCallGraph primaryCallgraph = null;
  private RawCallGraph secondaryCallgraph = null;

  private final DiffMetaData metaData;

  private final boolean isFunctionDiff;

  private MatchData matches;
  private boolean loaded = false;

  public Diff(
      final DiffMetaData preloadedMatches,
      final File binDiffBinary,
      final File primaryExportFile,
      final File secondaryExportFile,
      final boolean isFunctionDiff) {
    this.metaData = Preconditions.checkNotNull(preloadedMatches);
    this.matchesDatabaseFile = Preconditions.checkNotNull(binDiffBinary);
    this.primaryExportFile = Preconditions.checkNotNull(primaryExportFile);
    this.secondaryExportFile = Preconditions.checkNotNull(secondaryExportFile);
    this.isFunctionDiff = isFunctionDiff;
  }

  private void close() {
    primaryCallgraph = null;
    secondaryCallgraph = null;

    if (matches != null) {
      matches.close();
    }

    loaded = false;
  }

  protected Diff cloneDiffObjectOnSaveAs(
      final File binDiffBinary,
      final File primaryExportFile,
      final File secondaryExportFile,
      final FlowGraphViewData view) {
    viewManager.removeView(view);

    final Diff cloneDiff =
        new Diff(metaData, binDiffBinary, primaryExportFile, secondaryExportFile, true);

    cloneDiff.primaryExportFile = primaryExportFile;
    cloneDiff.secondaryExportFile = secondaryExportFile;
    cloneDiff.priExportMD5 = priExportMD5;
    cloneDiff.secExportMD5 = secExportMD5;
    cloneDiff.loaded = loaded;
    cloneDiff.viewManager.addView(view);
    cloneDiff.primaryCallgraph = primaryCallgraph;
    cloneDiff.secondaryCallgraph = secondaryCallgraph;
    cloneDiff.matches = matches;

    view.setViewName(
        CFileUtils.forceFilenameEndsNotWithExtension(
            binDiffBinary.getName(), Constants.BINDIFF_MATCHES_DB_EXTENSION));
    view.getGraphs().setDiff(cloneDiff);

    return cloneDiff;
  }

  public void addListener(final IDiffListener listener) {
    listenerProvider.addListener(listener);
  }

  public void closeDiff() {
    Logger.logInfo("Unloading Diff '" + getDiffName() + "'");
    if (matches == null) {
      return;
    }

    close();

    for (final IDiffListener listener : listenerProvider) {
      listener.unloadedDiff(this);
    }
  }

  public String getBinExportMD5(final ESide side) throws IOException {
    if (priExportMD5 == null) {
      priExportMD5 = FileUtils.calcMD5(primaryExportFile);
    }
    if (secExportMD5 == null) {
      secExportMD5 = FileUtils.calcMD5(secondaryExportFile);
    }

    return ESide.PRIMARY == side ? priExportMD5 : secExportMD5;
  }

  public RawCallGraph getCallgraph(final ESide side) {
    return side == ESide.PRIMARY ? primaryCallgraph : secondaryCallgraph;
  }

  public String getDiffFolder() {
    return getMatchesDatabase().getParent();
  }

  public DiffMetaData getDiffMetaData() {
    return metaData;
  }

  public String getDiffName() {
    return matchesDatabaseFile.getName();
  }

  public File getExportFile(final ESide side) {
    return side == ESide.PRIMARY ? primaryExportFile : secondaryExportFile;
  }

  public RawFunction getFunction(final IAddress functionAddr, final ESide side) {
    return getCallgraph(side).getFunction(functionAddr);
  }

  public ListenerProvider<IDiffListener> getListener() {
    return listenerProvider;
  }

  public MatchData getMatches() {
    return matches;
  }

  public File getMatchesDatabase() {
    return matchesDatabaseFile;
  }

  public DiffMetaData getMetaData() {
    return metaData;
  }

  public ViewManager getViewManager() {
    return viewManager;
  }

  public boolean isFunctionDiff() {
    return isFunctionDiff;
  }

  public boolean isLoaded() {
    return loaded;
  }

  public void removeDiff() {
    Logger.logInfo("Removing Diff '" + getDiffName() + "'");

    close();

    for (final IDiffListener listener : listenerProvider) {
      listener.unloadedDiff(this);
    }

    for (final IDiffListener listener : listenerProvider) {
      listener.removedDiff(this);
    }
  }

  public void removeListener(final IDiffListener listener) {
    listenerProvider.removeListener(listener);
  }

  public void setCallgraph(final RawCallGraph callgraph, final ESide side) {
    if (side == ESide.PRIMARY) {
      primaryCallgraph = callgraph;
    } else {
      secondaryCallgraph = callgraph;
    }
  }

  public void setExportFile(final File newExportFile, final ESide side) {
    if (!isFunctionDiff()) {
      throw new UnsupportedOperationException(
          "Unsupported operation: Export file names can only be updated if the diff is a function diff.");
    }

    if (side == ESide.PRIMARY) {
      primaryExportFile = newExportFile;
      priExportMD5 = null;
    } else {
      secondaryExportFile = newExportFile;
      secExportMD5 = null;
    }
  }

  public void setLoaded(final boolean loaded) {
    this.loaded = loaded;
  }

  public void setMatches(final MatchData matches) {
    this.matches = matches;
  }

  public void setMatchesDatabase(final File newMatchesDatabaseFile) {
    if (!isFunctionDiff()) {
      throw new UnsupportedOperationException(
          "Matches database file can only be updated if the diff is a function diff.");
    }

    matchesDatabaseFile = newMatchesDatabaseFile;
  }

  public void willOverwriteDiff(final String overwritePath) {
    for (final IDiffListener listener : listenerProvider) {
      listener.willOverwriteDiff(overwritePath);
    }
  }
}
