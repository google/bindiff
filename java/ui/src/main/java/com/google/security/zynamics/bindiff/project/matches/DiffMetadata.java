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

package com.google.security.zynamics.bindiff.project.matches;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.builders.ViewCodeNodeBuilder;
import com.google.security.zynamics.bindiff.project.diff.CountsChangedListener;
import com.google.security.zynamics.zylib.date.DateHelpers;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;

public class DiffMetadata {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final String version;
  private final String description;

  private final String primaryIdbName;
  private final String secondaryIdbName;

  private final String primaryImageName;
  private final String secondaryImageName;

  private final String primaryHash;
  private final String secondaryHash;

  private String primaryArchitectureName;
  private String secondaryArchitectureName;

  private int primaryMaxMnemonicLen;
  private int secondaryMaxMnemonicLen;

  private final GregorianCalendar date;

  private final double totalSimilarity;
  private final double totalConfidence;

  private final int[] similarityIntervalCounts;

  private int matchedFunctions;
  private int changedFunctions;
  private final int priFunctions;
  private final int secFunctions;

  private int matchedCalls;
  private int changedCalls;
  private final int priCalls;
  private final int secCalls;

  private final int priBasicBlocks;
  private final int secBasicBlocks;

  private final int priJumps;
  private final int secJumps;

  private final int priInstructions;
  private final int secInstructions;

  private int matchedBasicBlocks;
  private int matchedJumps;
  private int matchedInstructions;

  private final ListenerProvider<CountsChangedListener> listeners = new ListenerProvider<>();

  // TODO(cblichmann): Convert to a builder.
  public DiffMetadata(
      final String version,
      final String description,
      final GregorianCalendar date,
      final double similarity,
      final double confidence,
      final String priFilename,
      final String secFilename,
      final String priExeFilename,
      final String secExeFilename,
      final String priHash,
      final String secHash,
      final int[] similarityIntervalCounts,
      final int matchedFunctionsCount,
      final int primaryUnmatchedFunctionsCount,
      final int secondaryUnmatchedFunctionsCount,
      final int priCalls,
      final int secCalls,
      final int priBasicBlocks,
      final int secBasicBlocks,
      final int priJumps,
      final int secJumps,
      final int priInstructions,
      final int secInstructions) {
    this.version = version;
    this.description = description;
    this.primaryIdbName = priFilename;
    this.secondaryIdbName = secFilename;
    this.primaryImageName = priExeFilename;
    this.secondaryImageName = secExeFilename;
    this.primaryHash = priHash;
    this.secondaryHash = secHash;
    this.date = date;
    this.totalSimilarity = similarity;
    this.totalConfidence = confidence;
    this.similarityIntervalCounts = similarityIntervalCounts;
    this.matchedFunctions = matchedFunctionsCount;
    this.priFunctions = primaryUnmatchedFunctionsCount;
    this.secFunctions = secondaryUnmatchedFunctionsCount;
    this.priCalls = priCalls;
    this.secCalls = secCalls;
    this.priBasicBlocks = priBasicBlocks;
    this.secBasicBlocks = secBasicBlocks;
    this.priJumps = priJumps;
    this.secJumps = secJumps;
    this.priInstructions = priInstructions;
    this.secInstructions = secInstructions;
    this.changedFunctions = 0;
    this.matchedCalls = 0;
    this.changedCalls = 0;
  }

  public static GregorianCalendar dbDateStringToCalendar(final String date) {
    final GregorianCalendar result = new GregorianCalendar();
    try {
      result.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
    } catch (final ParseException e) {
      logger.at(Level.SEVERE).withCause(e).log();
    }
    return result;
  }

  public void addListener(final CountsChangedListener countsChangeListener) {
    listeners.addListener(countsChangeListener);
  }

  public String getArchitectureName(final ESide side) {
    return side == ESide.PRIMARY ? primaryArchitectureName : secondaryArchitectureName;
  }

  public GregorianCalendar getDate() {
    return date;
  }

  public String getDateString() {
    return DateHelpers.formatDate(date.getTime(), Calendar.LONG, Locale.getDefault());
  }

  public String getDiffDescription() {
    return description;
  }

  public String getIdbName(final ESide side) {
    return side == ESide.PRIMARY ? primaryIdbName : secondaryIdbName;
  }

  public String getImageHash(final ESide side) {
    return side == ESide.PRIMARY ? primaryHash : secondaryHash;
  }

  public String getImageName(final ESide side) {
    return side == ESide.PRIMARY ? primaryImageName : secondaryImageName;
  }

  public String getDisplayName(final ESide side) {
    String result = getImageName(side);
    if ("".equals(result)) {
      result = getIdbName(side);
    }
    if ("".equals(result)) {
      result = getImageHash(side);
    }
    if ("".equals(result)) {
      result = side.toString();
    }
    return result;
  }

  public int getMaxMnemonicLen() {
    int maxMnemSize = Math.max(primaryMaxMnemonicLen, secondaryMaxMnemonicLen);
    if (maxMnemSize == 0) {
      maxMnemSize = ViewCodeNodeBuilder.MAX_MNEMONIC_SIZE;
    }
    return maxMnemSize;
  }

  public int getMaxMnemonicLen(final ESide side) {
    return side == ESide.PRIMARY ? primaryMaxMnemonicLen : secondaryMaxMnemonicLen;
  }

  public int getSimilarityIntervalCount(final int index) {
    checkArgument(index <= 10, "Confidence interval index cannot be greater than 10.");
    return similarityIntervalCounts[index];
  }

  public int[] getSimilarityIntervalCounts() {
    return similarityIntervalCounts;
  }

  public int getSizeOfBasicBlocks(final ESide side) {
    return side == ESide.PRIMARY ? priBasicBlocks : secBasicBlocks;
  }

  public int getSizeOfCalls(final ESide side) {
    return side == ESide.PRIMARY ? priCalls : secCalls;
  }

  public int getSizeOfChangedCalls() {
    return changedCalls;
  }

  public int getSizeOfChangedFunctions() {
    return changedFunctions;
  }

  public int getSizeOfFunctions(final ESide side) {
    return side == ESide.PRIMARY ? priFunctions : secFunctions;
  }

  public int getSizeOfInstructions(final ESide side) {
    return side == ESide.PRIMARY ? priInstructions : secInstructions;
  }

  public int getSizeOfJumps(final ESide side) {
    return side == ESide.PRIMARY ? priJumps : secJumps;
  }

  public int getSizeOfMatchedBasicBlocks() {
    return matchedBasicBlocks;
  }

  public int getSizeOfMatchedCalls() {
    return matchedCalls;
  }

  public int getSizeOfMatchedFunctions() {
    return matchedFunctions;
  }

  public int getSizeOfMatchedInstructions() {
    return matchedInstructions;
  }

  public int getSizeOfMatchedJumps() {
    return matchedJumps;
  }

  public int getSizeOfUnmatchedBasicblocks(final ESide side) {
    return side == ESide.PRIMARY
        ? priBasicBlocks - matchedBasicBlocks
        : secBasicBlocks - matchedBasicBlocks;
  }

  public int getSizeOfUnmatchedCalls(final ESide side) {
    return side == ESide.PRIMARY ? priCalls - matchedCalls : secCalls - matchedCalls;
  }

  public int getSizeOfUnmatchedFunctions(final ESide side) {
    return side == ESide.PRIMARY
        ? priFunctions - matchedFunctions
        : secFunctions - matchedFunctions;
  }

  public int getSizeOfUnmatchedInstructions(final ESide side) {
    return side == ESide.PRIMARY
        ? priInstructions - matchedInstructions
        : secInstructions - matchedInstructions;
  }

  public int getSizeOfUnmatchedJumps(final ESide side) {
    return side == ESide.PRIMARY ? priJumps - matchedJumps : secJumps - matchedJumps;
  }

  public double getTotalConfidence() {
    return totalConfidence;
  }

  public double getTotalSimilarity() {
    return totalSimilarity;
  }

  public String getVersion() {
    return version;
  }

  public void removeListener(final CountsChangedListener countsChangeListener) {
    listeners.removeListener(countsChangeListener);
  }

  public void setArchitectureName(final String value, final ESide side) {
    if (side == ESide.PRIMARY) {
      primaryArchitectureName = value;
    } else {
      secondaryArchitectureName = value;
    }
  }

  public void setMaxMnemonicLen(final int value, final ESide side) {
    if (side == ESide.PRIMARY) {
      primaryMaxMnemonicLen = value;
    } else {
      secondaryMaxMnemonicLen = value;
    }
  }

  public void setSizeOfChangedCalls(final int count) {
    changedCalls = count;
    for (final CountsChangedListener listener : listeners) {
      listener.callsCountChanged();
    }
  }

  public void setSizeOfChangedFunctions(final int count) {
    changedFunctions = count;
    for (final CountsChangedListener listener : listeners) {
      listener.functionsCountChanged();
    }
  }

  public void setSizeOfMatchedBasicBlocks(final int count) {
    matchedBasicBlocks = count;
    for (final CountsChangedListener listener : listeners) {
      listener.basicBlocksCountChanged();
    }
  }

  public void setSizeOfMatchedCalls(final int count) {
    matchedCalls = count;
    for (final CountsChangedListener listener : listeners) {
      listener.callsCountChanged();
    }
  }

  public void setSizeOfMatchedFunctions(final int count) {
    matchedFunctions = count;
    for (final CountsChangedListener listener : listeners) {
      listener.functionsCountChanged();
    }
  }

  public void setSizeOfMatchedInstructions(final int count) {
    matchedInstructions = count;
    for (final CountsChangedListener listener : listeners) {
      listener.instructionsCountsChanged();
    }
  }

  public void setSizeOfMatchedJumps(final int count) {
    matchedJumps = count;
    for (final CountsChangedListener listener : listeners) {
      listener.jumpsCountChanged();
    }
  }
}
