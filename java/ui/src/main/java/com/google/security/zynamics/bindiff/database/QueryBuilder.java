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

package com.google.security.zynamics.bindiff.database;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
    checkArgument(baseQuery != null && !baseQuery.isEmpty());
    checkArgument(
        !baseQuery.toLowerCase().startsWith("insert"),
        "Multi row inserts are not supported by sqlite");
    checkArgument(
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
    checkArgument(
        inSetValues != null && !inSetValues.isEmpty(), "Row insert string cannot be null or empty");
    checkArgument(
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
    checkNotNull(connection);

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
