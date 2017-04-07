package com.google.security.zynamics.bindiff.database;

import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {
  protected static final int SQLITE_MAX_QUERY_SIZE = 1000000;

  private final List<StringBuffer> queries = new ArrayList<>();

  private final String baseQuery;

  private StringBuffer currentBuffer;

  public QueryBuilder(final String baseQuery) {
    Preconditions.checkArgument(baseQuery != null && !baseQuery.isEmpty());
    Preconditions.checkArgument(
        !baseQuery.toLowerCase().startsWith("insert"),
        "Multi row inserts are not supported by sqlite");
    Preconditions.checkArgument(
        !baseQuery.toLowerCase().startsWith("update"),
        "Multi row updates with more than one value set are not supported by SQLite");

    this.currentBuffer = new StringBuffer();
    this.baseQuery = baseQuery + (baseQuery.endsWith(" ") ? "" : " ");
  }

  private void addCurrentQuery() {
    if (currentBuffer.charAt(currentBuffer.length() - 1) != ')') {
      currentBuffer.append(")");
    }
    currentBuffer.append(";");
    queries.add(currentBuffer);

    currentBuffer = new StringBuffer();
  }

  public void appendInSet(final String inSetValues) {
    Preconditions.checkArgument(
        inSetValues != null && !inSetValues.isEmpty(), "Row insert string cannot be null or empty");
    Preconditions.checkArgument(
        !inSetValues.startsWith("(") && !inSetValues.endsWith(")"),
        "In set values can not start with a \"(\" and end with a \")\")");

    if (currentBuffer.length() + inSetValues.length() >= SQLITE_MAX_QUERY_SIZE - 3) {
      currentBuffer.append(")");
      addCurrentQuery();
    }

    if (currentBuffer.length() == 0) {
      currentBuffer.append(baseQuery);
      currentBuffer.append("(");
    } else {
      currentBuffer.append(",");
    }

    currentBuffer.append(inSetValues);
  }

  public void execute(final Connection connection) throws SQLException {
    Preconditions.checkNotNull(connection);

    if (currentBuffer.length() != 0) {
      addCurrentQuery();
    }

    for (final StringBuffer query : queries) {
      try (final Statement statement = connection.createStatement()) {
        statement.executeUpdate(query.toString());
      }
    }
  }
}
