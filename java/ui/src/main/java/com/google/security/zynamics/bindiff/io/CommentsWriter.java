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

package com.google.security.zynamics.bindiff.io;

import com.google.security.zynamics.bindiff.database.CommentsDatabase;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstruction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstructionComment;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import java.sql.SQLException;

public final class CommentsWriter {
  private CommentsWriter() {}

  private static void writeBasicblockComment(
      final CommentsDatabase database,
      final String md5,
      final IAddress functionAddr,
      final IAddress basicblockAddr,
      final String comment)
      throws SQLException {
    if (comment != null) {
      database.writeBasicblockComment(md5, functionAddr, basicblockAddr, comment);
    }
  }

  private static Pair<Integer, Integer> writeComments(
      final CommentsDatabase database, final String md5, final RawFlowGraph flowgraph) {
    int caughtExceptions = 0;
    int changedComments = 0;

    if (flowgraph != null) {
      for (final RawBasicBlock rawBasicblock : flowgraph) {
        final IAddress functionAddr = rawBasicblock.getFunctionAddr();

        if (rawBasicblock.isChangedComment()) {
          final IAddress basicblockAddr = rawBasicblock.getAddress();
          final String comment = rawBasicblock.getComment();

          try {
            ++changedComments;

            writeBasicblockComment(database, md5, functionAddr, basicblockAddr, comment);
          } catch (final SQLException e) {
            ++caughtExceptions;
          }
        }

        for (final RawInstruction rawInstruction : rawBasicblock) {
          if (rawInstruction.hasComments()) {
            for (final RawInstructionComment comment : rawInstruction.getComments()) {
              if (comment.isModified()) {
                final IAddress instructionAddr = rawInstruction.getAddress();

                try {
                  ++changedComments;

                  writeInstructionComment(database, md5, functionAddr, instructionAddr, comment);
                } catch (final SQLException e) {
                  ++caughtExceptions;
                }
              }
            }
          }
        }
      }
    }

    return new Pair<>(caughtExceptions, changedComments);
  }

  private static void writeInstructionComment(
      final CommentsDatabase database,
      final String md5,
      final IAddress functionAddr,
      final IAddress instructionAddr,
      final RawInstructionComment instructionComment)
      throws SQLException {
    if (instructionComment.isModified()) {
      final String comment = instructionComment.getText();
      if (instructionComment != null) {
        final ECommentPlacement commentPlacement = instructionComment.getPlacement();

        database.writeInstructionComment(
            md5, functionAddr, instructionAddr, commentPlacement, comment);
      }
    }
  }

  public static void writeComments(
      final Workspace workspace,
      final String primaryMd5,
      final String secondaryMd5,
      final ViewData viewData)
      throws SQLException {
    try (final CommentsDatabase database = new CommentsDatabase(workspace, false)) {
      if (viewData.isFlowgraphView()) {
        final RawFlowGraph priFlowgraph;
        priFlowgraph = ((FlowGraphViewData) viewData).getRawGraph(ESide.PRIMARY);

        final Pair<Integer, Integer> priErrorCounter =
            writeComments(database, primaryMd5, priFlowgraph);

        final RawFlowGraph secFlowgraph;
        secFlowgraph = ((FlowGraphViewData) viewData).getRawGraph(ESide.SECONDARY);

        final Pair<Integer, Integer> secErrorCounter =
            writeComments(database, secondaryMd5, secFlowgraph);

        final int caughtExceptions = priErrorCounter.first() + secErrorCounter.first();
        final int changedComments = priErrorCounter.second() + secErrorCounter.second();

        if (caughtExceptions > 0) {
          throw new SQLException(
              String.format(
                  "View '%s' failed to write %d of %d changed comments.",
                  viewData.getViewName(), caughtExceptions, changedComments));
        }
      }
    }
  }
}
