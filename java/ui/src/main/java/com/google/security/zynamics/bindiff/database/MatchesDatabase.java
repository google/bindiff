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

package com.google.security.zynamics.bindiff.database;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.io.BinExport2Reader;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffDirectories;
import com.google.security.zynamics.bindiff.project.matches.AddressPair;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.project.matches.FunctionDiffMetaData;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.matches.IAddressPair;
import com.google.security.zynamics.bindiff.project.matches.InstructionMatchData;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCall;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.types.Matches;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MatchesDatabase extends SqliteDatabase {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public static final int UNSAVED_BASIC_BLOCK_MATCH_ALGORITH_ID = -1;

  private static final int DEFAULT_FILE_TABLE_COLUMN_COUNT = 13;

  public MatchesDatabase(final File matchesDatabase) throws SQLException {
    super(matchesDatabase);
  }

  private void addBasicBlockMatches(final FunctionMatchData functionMatch) throws SQLException {
    final int functionMatchId = getFunctionMatchId(functionMatch);
    int maxBasicBlockId = getNextBasicBlockMatchId();

    try (final PreparedStatement basicBlockStatement =
            connection.prepareStatement("INSERT INTO basicblock VALUES (?, ?, ?, ?, ?, ?)");
        final PreparedStatement instructionStatement =
            connection.prepareStatement("INSERT INTO instruction VALUES (?, ?, ?)")) {
      final int manualMatchAlgoId = getAlgorithmIdForManuallyMatchedBasicBlocks();
      for (final BasicBlockMatchData basicBlockMatch : functionMatch.getBasicBlockMatches()) {
        final int matchAlgoId = basicBlockMatch.getAlgorithmId();

        basicBlockStatement.setInt(1, maxBasicBlockId);
        basicBlockStatement.setInt(2, functionMatchId);
        basicBlockStatement.setLong(3, basicBlockMatch.getAddress(ESide.PRIMARY));
        basicBlockStatement.setLong(4, basicBlockMatch.getAddress(ESide.SECONDARY));
        basicBlockStatement.setInt(
            5,
            matchAlgoId == UNSAVED_BASIC_BLOCK_MATCH_ALGORITH_ID ? manualMatchAlgoId : matchAlgoId);
        basicBlockStatement.setInt(6, matchAlgoId == UNSAVED_BASIC_BLOCK_MATCH_ALGORITH_ID ? 1 : 0);

        basicBlockStatement.addBatch();

        for (final InstructionMatchData instructionMatch :
            basicBlockMatch.getInstructionMatches()) {
          instructionStatement.setInt(1, maxBasicBlockId);
          instructionStatement.setLong(2, instructionMatch.getAddress(ESide.PRIMARY));
          instructionStatement.setLong(3, instructionMatch.getAddress(ESide.SECONDARY));

          instructionStatement.addBatch();
        }

        maxBasicBlockId++;
      }

      basicBlockStatement.executeBatch();
      instructionStatement.executeBatch();
    }
  }

  private void alterFileTable() throws SQLException {
    try (final PreparedStatement files = connection.prepareStatement("SELECT * FROM file");
        final ResultSet result = files.executeQuery()) {
      if (result.getMetaData().getColumnCount() == DEFAULT_FILE_TABLE_COLUMN_COUNT) {
        try (final PreparedStatement addFunctionName =
                connection.prepareStatement("ALTER TABLE file ADD COLUMN functionname VARCHAR");
            final PreparedStatement addFunctionType =
                connection.prepareStatement("ALTER TABLE file ADD COLUMN functiontype INT")) {
          addFunctionName.executeUpdate();
          addFunctionType.executeUpdate();
        }
      }
    }
  }

  private int countFunctionSimilarity(final double intervalStart, final double intervalEnd)
      throws SQLException {
    try (final PreparedStatement statement =
        connection.prepareStatement(
            "SELECT COUNT(*) AS intervalcount FROM function "
                + "WHERE similarity >= ? AND similarity < ?")) {
      statement.setDouble(1, intervalStart);
      statement.setDouble(2, intervalEnd);

      final ResultSet result = statement.executeQuery();
      if (result.next()) {
        return result.getInt("intervalcount");
      }
    }
    return 0;
  }

  private int[] countFunctionSimilarityIntervals() throws SQLException {
    final int[] similarityIntervalCounts = new int[11];

    for (int i = 0; i <= 10; i++) {
      similarityIntervalCounts[i] = countFunctionSimilarity(i * 0.1, (i + 1) * 0.1);
    }

    return similarityIntervalCounts;
  }

  private int countMatchedFunctions() throws SQLException {
    try (final Statement statement = connection.createStatement()) {
      final ResultSet result =
          statement.executeQuery("SELECT COUNT(*) AS matchedfunctioncount FROM function");
      if (result.next()) {
        return result.getInt("matchedfunctioncount");
      }
    }
    return 0;
  }

  private void deleteBasicBlockMatches(final long priFunctionAddr, final long secFunctionAddr)
      throws SQLException {
    try (final PreparedStatement selectsBasicblockIds =
        connection.prepareStatement(
            "SELECT basicblock.id FROM function "
                + "INNER JOIN basicblock ON basicblock.functionid = function.id "
                + "WHERE function.address1 = ? AND function.address2 = ?")) {
      selectsBasicblockIds.setLong(1, priFunctionAddr);
      selectsBasicblockIds.setLong(2, secFunctionAddr);

      try (final ResultSet result = selectsBasicblockIds.executeQuery()) {
        final QueryBuilder queryBuilder =
            new QueryBuilder("DELETE FROM instruction WHERE basicblockid IN ");
        while (result.next()) {
          queryBuilder.appendInSet(result.getString(1));
        }
        queryBuilder.execute(connection);
      }
    }

    try (final PreparedStatement selectsFunctionId =
        connection.prepareStatement(
            "SELECT id FROM function WHERE address1 = ? AND address2 = ?")) {
      selectsFunctionId.setLong(1, priFunctionAddr);
      selectsFunctionId.setLong(2, secFunctionAddr);

      try (final ResultSet result = selectsFunctionId.executeQuery()) {
        final QueryBuilder queryBuilder =
            new QueryBuilder("DELETE FROM basicblock WHERE functionid IN ");
        if (result.next()) {
          queryBuilder.appendInSet(result.getString(1));
        }
        queryBuilder.execute(connection);
      }
    }
  }

  private int getAlgorithmIdForManuallyMatchedBasicBlocks() throws SQLException {
    try (final PreparedStatement statement =
            connection.prepareStatement("SELECT MAX(id) AS maxid FROM basicblockalgorithm");
        final ResultSet result = statement.executeQuery()) {
      result.next();
      return result.getInt("maxid");
    }
  }

  private int getFunctionMatchId(final FunctionMatchData functionMatch) throws SQLException {
    try (final PreparedStatement statement =
        connection.prepareStatement(
            "SELECT id FROM function WHERE address1 = ? AND address2 = ?")) {

      statement.setLong(1, functionMatch.getAddress(ESide.PRIMARY));
      statement.setLong(2, functionMatch.getAddress(ESide.SECONDARY));

      final ResultSet result = statement.executeQuery();
      return result.next() ? result.getInt(1) : -1;
    }
  }

  private int getNextBasicBlockMatchId() throws SQLException {
    try (final PreparedStatement statement =
            connection.prepareStatement(
                "SELECT COALESCE(MAX(id) + 1, 1) AS maxid FROM basicblock");
        final ResultSet result = statement.executeQuery()) {
      return result.next() ? result.getInt(1) : -1;
    }
  }

  private void addBinExportMetaData(final File matchesDatabaseFile, final DiffMetaData metaData)
      throws IOException {
    if (matchesDatabaseFile == null) {
      return;
    }

    final File priBinExport =
        DiffDirectories.getBinExportFile(matchesDatabaseFile, metaData, ESide.PRIMARY);
    if (priBinExport.canRead()) {
      final BinExport2Reader reader = new BinExport2Reader(priBinExport, ESide.PRIMARY);
      metaData.setArchitectureName(reader.getArchitectureName(), ESide.PRIMARY);
      metaData.setMaxMnemonicLen(reader.getMaxMnemonicLen(), ESide.PRIMARY);
    }

    final File secBinExport =
        DiffDirectories.getBinExportFile(matchesDatabaseFile, metaData, ESide.SECONDARY);
    if (secBinExport.canRead()) {
      final BinExport2Reader reader = new BinExport2Reader(priBinExport, ESide.SECONDARY);
      metaData.setArchitectureName(reader.getArchitectureName(), ESide.SECONDARY);
      metaData.setMaxMnemonicLen(reader.getMaxMnemonicLen(), ESide.SECONDARY);
    }
  }

  private void setFunctionMatchCounts(
      final long priFunctionAddr, final long secFunctionAddr, final FunctionMatchData functionMatch)
      throws SQLException {
    try (final PreparedStatement statement =
        connection.prepareStatement(
            "UPDATE function SET basicblocks = ?, edges = ?, instructions = ?"
                + "WHERE address1 = ? and address2 = ?")) {

      statement.setInt(1, functionMatch.getSizeOfMatchedBasicblocks());
      statement.setInt(2, functionMatch.getSizeOfMatchedJumps());
      statement.setInt(3, functionMatch.getSizeOfMatchedInstructions());
      statement.setLong(4, priFunctionAddr);
      statement.setLong(5, secFunctionAddr);

      statement.executeUpdate();
    }
  }

  public void changeExportFilename(final String fileName, final ESide side) throws SQLException {
    try (final PreparedStatement statement =
        connection.prepareStatement("UPDATE FILE SET filename = ? WHERE id = ?")) {
      statement.setString(1, fileName);
      statement.setInt(2, side == ESide.PRIMARY ? 1 : 2);

      statement.executeUpdate();
    }
  }

  public void changeFileTable(final Diff diff) throws SQLException {
    if (!diff.isLoaded()) {
      throw new IllegalStateException("Function diff has to be loaded before saving.");
    }
    if (!diff.isFunctionDiff()) {
      throw new IllegalArgumentException("Must be a function diff.");
    }

    alterFileTable();

    final RawFunction priFunction = diff.getCallGraph(ESide.PRIMARY).getNodes().get(0);
    final RawFunction secFunction = diff.getCallGraph(ESide.SECONDARY).getNodes().get(0);

    try (final PreparedStatement statement =
        connection.prepareStatement(
            "UPDATE file SET functionname = ?, functiontype = ? WHERE id = ?")) {

      statement.setString(1, priFunction.getName());
      statement.setInt(2, EFunctionType.getOrdinal(priFunction.getFunctionType()));
      statement.setInt(3, 1);
      statement.addBatch();

      statement.setString(1, secFunction.getName());
      statement.setInt(2, EFunctionType.getOrdinal(secFunction.getFunctionType()));
      statement.setInt(3, 2);
      statement.addBatch();

      statement.executeBatch();
    }
  }

  public void deleteBasicblockMatch(
      final FunctionMatchData functionMatch, final BasicBlockMatchData basicBlockMatch)
      throws SQLException {
    int functionMatchId = -1;
    int basicBlockMatchId = -1;

    try (final PreparedStatement statement =
        connection.prepareStatement(
            "SELECT function.id, basicblock.id FROM function "
                + "INNER JOIN basicblock ON basicblock.address1 = ? and basicblock.address2 = ? "
                + "INNER JOIN instruction ON basicblock.id = instruction.basicblockid "
                + "WHERE function.address1 = ? and function.address2 = ? "
                + "GROUP BY basicblock.functionid;")) {
      statement.setLong(1, basicBlockMatch.getAddress(ESide.PRIMARY));
      statement.setLong(2, basicBlockMatch.getAddress(ESide.SECONDARY));
      statement.setLong(3, functionMatch.getAddress(ESide.PRIMARY));
      statement.setLong(4, functionMatch.getAddress(ESide.SECONDARY));

      final ResultSet result = statement.executeQuery();

      if (result.next()) {
        functionMatchId = result.getInt(1);
        basicBlockMatchId = result.getInt(2);
      }
    } catch (final SQLException e) {
      throw new SQLException("Couldn't delete non existing basicblock match from database.", e);
    }

    try (final PreparedStatement statement =
        connection.prepareStatement("DELETE FROM basicblock WHERE id = ? AND functionid = ? ")) {
      statement.setInt(1, basicBlockMatchId);
      statement.setInt(2, functionMatchId);
      statement.executeUpdate();
    } catch (final SQLException e) {
      throw new SQLException("Failed to delete basic block match from database: " + e.getMessage());
    }

    try (final PreparedStatement statement =
        connection.prepareStatement("DELETE FROM instruction WHERE basicblockid = ?")) {
      statement.setInt(1, basicBlockMatchId);
      statement.executeUpdate();
    } catch (final SQLException e) {
      throw new SQLException(
          "Failed to delete instruction matches of removed basic block match "
              + "from database: "
              + e.getMessage());
    }
  }

  public String[] getIDBNames() throws SQLException {
    final String[] idbNames = new String[2];

    try (final Statement statement = connection.createStatement()) {
      final ResultSet result = statement.executeQuery("SELECT filename FROM file ORDER BY id");

      int index = 0;
      while (result.next()) {
        idbNames[index++] = result.getString("filename");
      }

      return idbNames;
    } catch (final SQLException e) {
      throw new SQLException("Couldn't read IDB names: " + e.getMessage());
    }
  }

  public void loadBasicBlockMatches(final FunctionMatchData functionMatch) throws IOException {
    if (functionMatch == null) {
      return;
    }

    try (final PreparedStatement statement =
        connection.prepareStatement(
            "SELECT "
                + "basicblock.address1 AS priBasicBlockAddr, "
                + "basicblock.address2 AS secBasicBlockAddr, basicblock.algorithm, "
                + "instruction.address1 AS priInstructionAddr, "
                + "instruction.address2 AS secInstructionAddr FROM function "
                + "INNER JOIN basicblock ON basicblock.functionid = function.id "
                + "INNER JOIN basicblockalgorithm ON basicblockalgorithm.id = basicblock.algorithm "
                // Left join because matched basic blocks must NOT have matched instructions.
                + "LEFT JOIN instruction ON basicblock.id = instruction.basicblockid "
                + "WHERE function.address1 = ? AND function.address2 = ? "
                + "ORDER BY priBasicBlockAddr, priInstructionAddr;")) {

      statement.setLong(1, functionMatch.getAddress(ESide.PRIMARY));
      statement.setLong(2, functionMatch.getAddress(ESide.SECONDARY));

      final List<BasicBlockMatchData> basicBlockMatches = new ArrayList<>();
      List<InstructionMatchData> instructionMatches = new ArrayList<>();

      final ResultSet result = statement.executeQuery();

      long lastPriBasicBlockAddr = 0;
      long priBasicBlockAddr = 0;
      long secBasicBlockAddr = 0;
      int algorithm = 0;
      long priInstructionAddr = 0;
      long secInstructionAddr = 0;

      while (result.next()) {
        priBasicBlockAddr = result.getLong(1);

        if (lastPriBasicBlockAddr != priBasicBlockAddr && !result.isFirst()) {
          final Matches<InstructionMatchData> instructionMachesBimap =
              new Matches<>(instructionMatches);
          final BasicBlockMatchData basicBlockMatch =
              new BasicBlockMatchData(
                  lastPriBasicBlockAddr, secBasicBlockAddr, algorithm, instructionMachesBimap);
          basicBlockMatches.add(basicBlockMatch);

          instructionMatches = new ArrayList<>();

          lastPriBasicBlockAddr = priBasicBlockAddr;
          secBasicBlockAddr = result.getLong(2);
          algorithm = result.getInt(3);
        }

        final boolean wasNull = result.getObject(4) == null || result.getObject(5) == null;

        if (!wasNull) {
          priInstructionAddr = result.getLong(4);
          secInstructionAddr = result.getLong(5);
        }

        if (result.isFirst()) {
          secBasicBlockAddr = result.getLong(2);
          algorithm = result.getInt(3);

          lastPriBasicBlockAddr = priBasicBlockAddr;
          instructionMatches = new ArrayList<>();
        }

        if (!wasNull) {
          final InstructionMatchData instructionMatchData =
              new InstructionMatchData(priInstructionAddr, secInstructionAddr);
          instructionMatches.add(instructionMatchData);
        }
      }

      final Matches<InstructionMatchData> instructionMatchesBimap =
          new Matches<>(instructionMatches);
      final BasicBlockMatchData basicBlockMatch =
          new BasicBlockMatchData(
              priBasicBlockAddr, secBasicBlockAddr, algorithm, instructionMatchesBimap);
      basicBlockMatches.add(basicBlockMatch);

      functionMatch.loadBasicBlockMatches(basicBlockMatches);
    } catch (final SQLException e) {
      throw new IOException(
          "Couldn't read basic block and instruction matches.\n" + e.getMessage());
    }
  }

  public String loadDiffDescription() throws SQLException {
    try (final PreparedStatement statement =
            connection.prepareStatement("SELECT description FROM metadata");
        final ResultSet result = statement.executeQuery()) {
      return result.next() ? result.getString("description") : "";
    }
  }

  public DiffMetaData loadDiffMetaData(final File matchesDatabaseFile) throws SQLException {
    try {
      final String version;
      final String description;
      final String dateCreated;
      final double similarity;
      final double confidence;
      try (final Statement statement = connection.createStatement();
          final ResultSet result =
              statement.executeQuery(
                  "SELECT version, description, created, similarity, confidence FROM metadata")) {
        result.next();
        version = result.getString("version");
        description = result.getString("description");
        dateCreated = result.getString("created");
        similarity = result.getDouble("similarity");
        confidence = result.getDouble("confidence");
      }

      try (final Statement statement = connection.createStatement();
          final ResultSet result =
              statement.executeQuery(
                  "SELECT filename, exefilename, hash, functions, libfunctions, calls,"
                      + " basicblocks, libbasicblocks, edges, libedges, instructions,"
                      + " libinstructions FROM file")) {
        // Primary
        result.next();
        final String priFilename = result.getString("filename");
        final String priExeFilename = result.getString("exefilename");
        final String priHash = result.getString("hash");

        final int priFunctions = result.getInt("functions") + result.getInt("libfunctions");
        final int priCalls = result.getInt("calls");
        final int priBasicBlocks = result.getInt("basicblocks") + result.getInt("libbasicblocks");
        final int priJumps = result.getInt("edges") + result.getInt("libedges");
        final int priInstructions =
            result.getInt("instructions") + result.getInt("libinstructions");

        // Secondary
        result.next();
        final String secFilename = result.getString("filename");
        final String secExeFilename = result.getString("exefilename");
        final String secHash = result.getString("hash");

        final int secFunctions = result.getInt("functions") + result.getInt("libfunctions");
        final int secCalls = result.getInt("calls");
        final int secBasicBlocks = result.getInt("basicblocks") + result.getInt("libbasicblocks");
        final int secJumps = result.getInt("edges") + result.getInt("libedges");
        final int secInstructions =
            result.getInt("instructions") + result.getInt("libinstructions");

        final int[] functionSimilarityIntervalCounts = countFunctionSimilarityIntervals();
        final int matchedFunctions = countMatchedFunctions();

        // Return new meta-data object
        final DiffMetaData metaData =
            new DiffMetaData(
                version,
                description,
                DiffMetaData.dbDateStringToCalendar(dateCreated),
                similarity,
                confidence,
                priFilename,
                secFilename,
                priExeFilename,
                secExeFilename,
                priHash,
                secHash,
                functionSimilarityIntervalCounts,
                matchedFunctions,
                priFunctions,
                secFunctions,
                priCalls,
                secCalls,
                priBasicBlocks,
                secBasicBlocks,
                priJumps,
                secJumps,
                priInstructions,
                secInstructions);
        addBinExportMetaData(matchesDatabaseFile, metaData);
        return metaData;
      }
    } catch (final IOException | SQLException e) {
      throw new SQLException(
          "Couldn't load diff. Reading meta data from matches database failed: " + e.getMessage(),
          e);
    }
  }

  public IAddressPair loadFunctionDiffAddressPair() throws SQLException {
    try (final PreparedStatement statement =
        connection.prepareStatement("SELECT function.address1, function.address2 FROM function")) {
      final ResultSet result = statement.executeQuery();

      if (result.next()) {
        final long priAddr = result.getLong(1);
        final long secAddr = result.getLong(2);

        if (!result.next()) {
          return new AddressPair(priAddr, secAddr);
        }
      }
      throw new SQLException("Illegal BinDiff database state: Function matches must be unique.");
    }
  }

  public FunctionDiffMetaData loadFunctionDiffMetaData(final boolean isDirectlyFromIda)
      throws SQLException {
    final DiffMetaData metaData = loadDiffMetaData(null);

    if (!isDirectlyFromIda) {
      final IAddressPair addressPair = loadFunctionDiffAddressPair();
      final IAddress priFunctionAddr = addressPair.getIAddress(ESide.PRIMARY);
      final IAddress secFunctionAddr = addressPair.getIAddress(ESide.SECONDARY);

      try (final PreparedStatement statement =
          connection.prepareStatement("SELECT functionname, functiontype FROM file ORDER BY id")) {
        String priFunctionName = null;
        String secFunctionName = null;
        EFunctionType priFunctionType = null;
        EFunctionType secFunctionType = null;

        final ResultSet result = statement.executeQuery();

        if (result.next()) {
          priFunctionName = result.getString("functionname");
          priFunctionType = EFunctionType.getType(result.getInt("functiontype"));
        }

        if (result.next()) {
          secFunctionName = result.getString("functionname");
          secFunctionType = EFunctionType.getType(result.getInt("functiontype"));
        }

        if (priFunctionName == null || secFunctionName == null) {
          throw new SQLException(
              "Failed to load function diff meta data: "
                  + "Primary and secondary function must not be null.");
        }
        if (priFunctionType == null || secFunctionType == null) {
          throw new SQLException(
              "Failed to load function diff meta data: "
                  + "Primary and secondary function type must not be null.");
        }

        final FunctionDiffMetaData functionDiffMetaData =
            new FunctionDiffMetaData(
                metaData,
                priFunctionAddr,
                secFunctionAddr,
                priFunctionName,
                secFunctionName,
                priFunctionType,
                secFunctionType);

        return functionDiffMetaData;
      }
    }
    return new FunctionDiffMetaData(metaData, null, null, null, null, null, null);
  }

  public MatchData loadFunctionMatches(final Diff diff) throws SQLException {
    final DiffMetaData metaData = diff.getMetaData();

    int matchedBasicblocks = 0;
    int matchedJumps = 0;
    int matchedInstructions = 0;

    try (final Statement statement = connection.createStatement()) {
      final ResultSet result =
          statement.executeQuery(
              "SELECT function.id, "
                  + "function.address1, function.address2, function.similarity, "
                  + "function.confidence, function.flags, function.algorithm, "
                  + "function.basicblocks, function.edges, function.instructions "
                  + "FROM function");

      final List<FunctionMatchData> functionMatches = new ArrayList<>();

      while (result.next()) {
        matchedBasicblocks += result.getInt("basicblocks");
        matchedJumps += result.getInt("edges");
        matchedInstructions += result.getInt("instructions");

        final FunctionMatchData functionMatch =
            new FunctionMatchData(
                result.getInt("id"),
                result.getLong("address1"),
                result.getLong("address2"),
                result.getDouble("similarity"),
                result.getDouble("confidence"),
                result.getInt("flags"),
                result.getInt("algorithm"),
                result.getInt("basicblocks"),
                result.getInt("edges"),
                result.getInt("instructions"));

        functionMatches.add(functionMatch);
      }

      metaData.setSizeOfMatchedBasicBlocks(matchedBasicblocks);
      metaData.setSizeOfMatchedJumps(matchedJumps);
      metaData.setSizeOfMatchedInstructions(matchedInstructions);

      return new MatchData(functionMatches, metaData);
    } catch (final SQLException e) {
      throw new SQLException(
          "Couldn't load diff. Reading function matches from database failed: " + e.getMessage());
    }
  }

  public Map<AddressPair, AddressPair> loadMatchedCallAddresses(final Diff diff)
      throws SQLException {
    final Map<AddressPair, AddressPair> addrsMap = new HashMap<>();
    final List<IAddress> callAddrs = new ArrayList<>();
    for (final RawCall call : diff.getCallGraph(ESide.PRIMARY).getEdges()) {
      callAddrs.add(call.getSourceInstructionAddr());
    }

    final String queryString =
        "SELECT function.address1, "
            + "instruction.address1, function.address2, instruction.address2 "
            + "FROM function INNER JOIN basicblock ON function.id = basicblock.functionid "
            + "INNER JOIN instruction ON basicblock.id = instruction.basicblockid "
            + "WHERE instruction.address1 IN ";

    StringBuilder queryBuffer = new StringBuilder(queryString);
    boolean first = true;
    try {
      try (final Statement statement = connection.createStatement()) {
        // TODO: Use QueryBuilder! (Which is also a query splitter and should be used in
        // addBasicBlockMatches(FunctionMatchData functionMatch);!)
        for (final IAddress callPair : callAddrs) {
          if (first) {
            queryBuffer.append("(");
            first = false;
          } else {
            queryBuffer.append(",");
          }
          queryBuffer.append("" + callPair.toLong());

          if (queryBuffer.length() >= QueryBuilder.SQLITE_MAX_QUERY_SIZE - 10) {
            queryBuffer.append(")");
            try (final ResultSet result = statement.executeQuery(queryBuffer.toString())) {
              while (result.next()) {
                final long priFunctionAddr = result.getLong(1);
                final long priInstructionAddr = result.getLong(2);
                final long secFunctionAddr = result.getLong(3);
                final long secInstructionAddr = result.getLong(4);

                addrsMap.put(
                    new AddressPair(priFunctionAddr, priInstructionAddr),
                    new AddressPair(secFunctionAddr, secInstructionAddr));
              }
            }
            queryBuffer = new StringBuilder(queryString);
            first = true;
          }
        }
      }

      if (!first && queryBuffer.length() != queryString.length()) {
        queryBuffer.append(")");

        try (final Statement statement = connection.createStatement();
            final ResultSet result = statement.executeQuery(queryBuffer.toString())) {

          while (result.next()) {
            final long priFunctionAddr = result.getLong(1);
            final long priInstructionAddr = result.getLong(2);
            final long secFunctionAddr = result.getLong(3);
            final long secInstructionAddr = result.getLong(4);

            addrsMap.put(
                new AddressPair(priFunctionAddr, priInstructionAddr),
                new AddressPair(secFunctionAddr, secInstructionAddr));
          }
        }
      }
    } catch (final SQLException e) {
      throw new SQLException("Couldn't read calls matches from database: " + e.getMessage());
    }
    return Collections.unmodifiableMap(addrsMap);
  }

  public void saveDiffDescription(final String description) throws SQLException {
    try (final PreparedStatement statement =
        connection.prepareStatement("UPDATE metadata SET description = ?")) {
      statement.setString(1, description);
      statement.executeUpdate();
    }
  }

  public void setFunctionDiffCounts(final RawFunction priFunction, final RawFunction secFunction)
      throws SQLException {
    try (final PreparedStatement statement =
            connection.prepareStatement(
                "SELECT basicblocks, libbasicblocks, edges, libedges, instructions,"
                    + " libinstructions FROM file ORDER BY id ");
        final ResultSet result = statement.executeQuery()) {

      if (result.next()) {
        priFunction.setSizeOfBasicBlocks(
            result.getInt("basicblocks") + result.getInt("libbasicblocks"));
        priFunction.setSizeOfJumps(result.getInt("edges") + result.getInt("libedges"));
        priFunction.setSizeOfInstructions(
            result.getInt("instructions") + result.getInt("libinstructions"));
      }
      if (result.next()) {
        secFunction.setSizeOfBasicBlocks(
            result.getInt("basicblocks") + result.getInt("libbasicblocks"));
        secFunction.setSizeOfJumps(result.getInt("edges") + result.getInt("libedges"));
        secFunction.setSizeOfInstructions(
            result.getInt("instructions") + result.getInt("libinstructions"));

        return;
      }
      throw new SQLException(
          "Invalid matches database state. File table must consist of exactly two records.");
    }
  }

  public void updateFunctionMatch(
      final long priFunctionAddr, final long secFunctionAddr, final FunctionMatchData functionMatch)
      throws SQLException {
    final boolean savedAutoCommit = connection.getAutoCommit();
    connection.setAutoCommit(false);

    try {
      // deletes and writes all matches of the affected function...
      deleteBasicBlockMatches(priFunctionAddr, secFunctionAddr);
      addBasicBlockMatches(functionMatch);
      setFunctionMatchCounts(priFunctionAddr, secFunctionAddr, functionMatch);

      connection.commit();
    } catch (final SQLException e) {
      logger.at(Level.SEVERE).withCause(e).log(
          "Couldn't update function match. Executing rollback.");
      connection.rollback();
    }
    connection.setAutoCommit(savedAutoCommit);
  }
}
