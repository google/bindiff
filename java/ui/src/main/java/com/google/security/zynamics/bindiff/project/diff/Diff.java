// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.project.diff;

import com.google.common.base.Preconditions;
import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.matches.DiffMetadata;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.ViewManager;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class Diff {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final ListenerProvider<IDiffListener> listenerProvider = new ListenerProvider<>();

  private File matchesDatabaseFile;
  private File primaryExportFile;
  private File secondaryExportFile;

  private String priExportMD5 = null;
  private String secExportMD5 = null;

  private final ViewManager viewManager = new ViewManager();

  private RawCallGraph primaryCallGraph = null;
  private RawCallGraph secondaryCallGraph = null;

  private final DiffMetadata metadata;

  private final boolean isFunctionDiff;

  private MatchData matches;
  private boolean loaded = false;

  public Diff(
      final DiffMetadata preloadedMatches,
      final File binDiffBinary,
      final File primaryExportFile,
      final File secondaryExportFile,
      final boolean isFunctionDiff) {
    this.metadata = Preconditions.checkNotNull(preloadedMatches);
    this.matchesDatabaseFile = Preconditions.checkNotNull(binDiffBinary);
    this.primaryExportFile = Preconditions.checkNotNull(primaryExportFile);
    this.secondaryExportFile = Preconditions.checkNotNull(secondaryExportFile);
    this.isFunctionDiff = isFunctionDiff;
  }

  private void close() {
    primaryCallGraph = null;
    secondaryCallGraph = null;

    if (matches != null) {
      matches.close();
    }

    loaded = false;
  }

  Diff cloneDiffObjectOnSaveAs(
      final File binDiffBinary,
      final File primaryExportFile,
      final File secondaryExportFile,
      final FlowGraphViewData view) {
    viewManager.removeView(view);

    final Diff cloneDiff =
        new Diff(metadata, binDiffBinary, primaryExportFile, secondaryExportFile, true);

    cloneDiff.primaryExportFile = primaryExportFile;
    cloneDiff.secondaryExportFile = secondaryExportFile;
    cloneDiff.priExportMD5 = priExportMD5;
    cloneDiff.secExportMD5 = secExportMD5;
    cloneDiff.loaded = loaded;
    cloneDiff.viewManager.addView(view);
    cloneDiff.primaryCallGraph = primaryCallGraph;
    cloneDiff.secondaryCallGraph = secondaryCallGraph;
    cloneDiff.matches = matches;

    view.setViewName(
        BinDiffFileUtils.forceFilenameEndsNotWithExtension(
            binDiffBinary.getName(), Constants.BINDIFF_MATCHES_DB_EXTENSION));
    view.getGraphs().setDiff(cloneDiff);

    return cloneDiff;
  }

  public void addListener(final IDiffListener listener) {
    listenerProvider.addListener(listener);
  }

  public void closeDiff() {
    logger.at(Level.INFO).log("Unloading Diff '%s'", getDiffName());
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

  public RawCallGraph getCallGraph(final ESide side) {
    return side == ESide.PRIMARY ? primaryCallGraph : secondaryCallGraph;
  }

  public String getDiffFolder() {
    return getMatchesDatabase().getParent();
  }

  public DiffMetadata getDiffMetaData() {
    return metadata;
  }

  public String getDiffName() {
    return matchesDatabaseFile.getName();
  }

  public File getExportFile(final ESide side) {
    return side == ESide.PRIMARY ? primaryExportFile : secondaryExportFile;
  }

  public RawFunction getFunction(final IAddress functionAddr, final ESide side) {
    return getCallGraph(side).getFunction(functionAddr);
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

  public DiffMetadata getMetadata() {
    return metadata;
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
    logger.at(Level.INFO).log("Removing Diff '%s'", getDiffName());

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

  public void setCallGraph(final RawCallGraph callGraph, final ESide side) {
    if (side == ESide.PRIMARY) {
      primaryCallGraph = callGraph;
    } else {
      secondaryCallGraph = callGraph;
    }
  }

  public void setExportFile(final File newExportFile, final ESide side) {
    if (!isFunctionDiff()) {
      throw new UnsupportedOperationException(
          "Unsupported operation: Export file names can only be updated if the diff is a function"
              + " diff.");
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
