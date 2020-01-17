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

package com.google.security.zynamics.bindiff.graph.labelcontent.lineeditor;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EPlaceholderType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.builders.ViewCodeNodeBuilder;
import com.google.security.zynamics.bindiff.graph.labelcontent.editableline.BasicBlockLineObject;
import com.google.security.zynamics.bindiff.graph.labelcontent.editableline.PlaceholderObject;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.project.helpers.GraphGetter;
import com.google.security.zynamics.bindiff.project.helpers.MatchesGetter;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstruction;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyLineEditor;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.strings.StringHelper;

import java.util.ArrayList;
import java.util.List;

public class BasicBlockContentEditor implements IZyLineEditor {
  private static final int NODELABEL_PADDING = 10;
  private final GraphsContainer graphContainer;
  private final FunctionMatchData functionMatch;
  private final ESide side;

  public BasicBlockContentEditor(
      final FunctionMatchData functionMatch,
      final GraphsContainer graphContainer,
      final ESide side) {
    this.functionMatch = functionMatch;
    this.graphContainer = Preconditions.checkNotNull(graphContainer);
    this.side = Preconditions.checkNotNull(side);
  }

  private int preCountLabelCommentLines(final RawBasicBlock basicblock, final ESide side) {
    int counter = 0;

    if (side == basicblock.getSide()) {
      if (!basicblock.getComment().isEmpty()) {
        counter = StringHelper.count(basicblock.getComment(), '\n') + 1;
      }
    } else {
      SingleDiffNode otherDiffNode = null;

      if (side == ESide.PRIMARY) {
        otherDiffNode =
            GraphGetter.getPrimaryDiffNode(graphContainer.getSecondaryGraph(), basicblock);
      } else {
        otherDiffNode =
            GraphGetter.getSecondaryDiffNode(graphContainer.getPrimaryGraph(), basicblock);
      }

      if (otherDiffNode != null) {
        final RawBasicBlock otherBasicblock = (RawBasicBlock) otherDiffNode.getRawNode();

        if (!otherBasicblock.getComment().isEmpty()) {
          counter = StringHelper.count(otherBasicblock.getComment(), '\n') + 1;
        }
      }
    }

    return counter;
  }

  private void recreateInstruction(
      final ZyLabelContent labelContent,
      final RawInstruction instruction,
      final int firstIndex,
      final int maxTopCommentLineCount,
      final int maxRightCommentLineCount) {
    final RawBasicBlock basicblock =
        ((BasicBlockLineObject) labelContent.getModel()).getRawBasicblock();
    final int maxOperandLength = basicblock.getMaxOperandLength();

    final int lastIndex = labelContent.getLastLineIndexOfModelAt(firstIndex);

    for (int index = firstIndex; index <= lastIndex; index++) {
      labelContent.removeLine(firstIndex);
    }

    boolean matchedInstruction = false;

    if (functionMatch != null && instruction != null) {
      matchedInstruction =
          MatchesGetter.isMatchedInstruction(functionMatch, basicblock, instruction);
    }

    final List<ZyLineContent> lines =
        ViewCodeNodeBuilder.buildInstruction(
            instruction,
            maxOperandLength,
            !matchedInstruction,
            side,
            maxTopCommentLineCount,
            maxRightCommentLineCount);

    int index = firstIndex;

    for (final ZyLineContent line : lines) {
      labelContent.insertLine(line, index++);
    }
  }

  private void recreateInstructionSynchronized(
      final ZyLabelContent labelContent, final RawInstruction instruction, final int lineIndex) {
    final RawBasicBlock basicblock =
        ((BasicBlockLineObject) labelContent.getModel()).getRawBasicblock();

    final ESide side = basicblock.getSide();

    RawInstruction priInstruction = null;
    RawInstruction secInstruction = null;

    if (basicblock.getSide() == ESide.PRIMARY) {
      priInstruction = instruction;
      secInstruction = null;

      final RawBasicBlock secBasicblock =
          GraphGetter.getSecondaryRawBasicblock(graphContainer, basicblock);
      final BasicBlockMatchData basicblockMatch =
          MatchesGetter.getBasicBlockMatch(functionMatch, basicblock);

      if (basicblockMatch != null) {
        final IAddress secInstructionAddr =
            basicblockMatch.getSecondaryInstructionAddr(priInstruction.getAddress());
        secInstruction = secBasicblock.getInstruction(secInstructionAddr);
      }
    } else {
      priInstruction = null;
      secInstruction = instruction;

      final RawBasicBlock priBasicblock =
          GraphGetter.getPrimaryRawBasicblock(graphContainer, basicblock);
      final BasicBlockMatchData basicblockMatch =
          MatchesGetter.getBasicBlockMatch(functionMatch, basicblock);

      if (basicblockMatch != null) {
        final IAddress priInstructionAddr =
            basicblockMatch.getPrimaryInstructionAddr(secInstruction.getAddress());
        priInstruction = priBasicblock.getInstruction(priInstructionAddr);
      }
    }

    final int maxTopCommentLineCount =
        ViewCodeNodeBuilder.precalcMaxCommentLineCount(
            priInstruction, secInstruction, ECommentPlacement.ABOVE_LINE);
    final int maxRightCommentLineCount =
        ViewCodeNodeBuilder.precalcMaxCommentLineCount(
            priInstruction, secInstruction, ECommentPlacement.BEHIND_LINE);

    labelContent.setRightPadding(NODELABEL_PADDING);
    recreateInstruction(
        labelContent, instruction, lineIndex, maxTopCommentLineCount, maxRightCommentLineCount);

    SingleDiffNode otherDiffNode = null;
    if (side == ESide.PRIMARY) {
      otherDiffNode =
          GraphGetter.getSecondaryDiffNode(graphContainer.getPrimaryGraph(), basicblock);
    } else {
      otherDiffNode =
          GraphGetter.getPrimaryDiffNode(graphContainer.getSecondaryGraph(), basicblock);
    }

    if (otherDiffNode != null) {
      final ZyLabelContent otherLabelContent = otherDiffNode.getRealizer().getNodeContent();

      otherLabelContent.setRightPadding(NODELABEL_PADDING);
      recreateInstruction(
          otherLabelContent,
          side == ESide.PRIMARY ? secInstruction : priInstruction,
          lineIndex,
          maxTopCommentLineCount,
          maxRightCommentLineCount);

      final double width = labelContent.getBounds().getWidth();
      final double otherWidth = otherLabelContent.getBounds().getWidth();
      if (width > otherWidth) {
        otherLabelContent.setRightPadding((int) Math.round(width - otherWidth) + 10);
      } else {
        labelContent.setRightPadding((int) Math.round(otherWidth - width) + 10);
      }

      otherDiffNode.getRealizer().regenerate();
    }

    final SingleDiffNode diffNode = GraphGetter.getDiffNode(graphContainer, basicblock);
    diffNode.getRealizer().regenerate();

    graphContainer
        .getSuperGraph()
        .refreshSuperNodeSize(
            graphContainer.getPrimaryGraph(),
            graphContainer.getSecondaryGraph(),
            diffNode.getSuperDiffNode());

    graphContainer.updateViews();
  }

  private void recreateLabelComment(
      final ZyLabelContent labelContent,
      final RawBasicBlock basicblock,
      final int insertLineCount) {
    final List<ZyLineContent> lines = new ArrayList<>();

    final int lineCount = labelContent.getLineCount();

    lines.addAll(ViewCodeNodeBuilder.buildBasicblockComment(basicblock, insertLineCount));

    int index = lineCount;

    while (--index > 0) {
      final ZyLineContent lineContent = labelContent.getLineContent(index);
      final IZyEditableObject lineObject = lineContent.getLineObject();

      if (lineObject.isPlaceholder()) {
        if (((PlaceholderObject) lineObject).getPlaceholderType()
            == EPlaceholderType.BASICBLOCK_COMMENT) {
          labelContent.removeLine(index);

          continue;
        }

        break;
      }

      if (lineObject.getPersistentModel() instanceof RawBasicBlock) {
        labelContent.removeLine(index);

        continue;
      }

      break;
    }

    for (final ZyLineContent lineContent : lines) {
      labelContent.addLineContent(lineContent);
    }
  }

  private void recreateLabelCommentSynchronized(
      final ZyLabelContent labelContent, final RawBasicBlock basicblock) {
    final ESide side = basicblock.getSide();

    final int commentLineCount = preCountLabelCommentLines(basicblock, side);
    final int otherCommentLineCount =
        preCountLabelCommentLines(
            basicblock, side == ESide.PRIMARY ? ESide.SECONDARY : ESide.PRIMARY);
    final int insertLineCount = Math.max(commentLineCount, otherCommentLineCount);

    labelContent.setRightPadding(NODELABEL_PADDING);
    recreateLabelComment(labelContent, basicblock, insertLineCount);

    SingleDiffNode otherDiffNode = null;
    RawBasicBlock otherBasicblock = null;

    if (side == ESide.PRIMARY) {
      otherDiffNode =
          GraphGetter.getSecondaryDiffNode(graphContainer.getPrimaryGraph(), basicblock);
      otherBasicblock = GraphGetter.getSecondaryRawBasicblock(graphContainer, basicblock);
    } else {
      otherDiffNode =
          GraphGetter.getPrimaryDiffNode(graphContainer.getSecondaryGraph(), basicblock);
      otherBasicblock = GraphGetter.getPrimaryRawBasicblock(graphContainer, basicblock);
    }

    if (otherBasicblock != null) {
      final ZyLabelContent otherLabelContent = otherDiffNode.getRealizer().getNodeContent();

      otherLabelContent.setRightPadding(NODELABEL_PADDING);
      recreateLabelComment(otherLabelContent, otherBasicblock, insertLineCount);

      final double width = labelContent.getBounds().getWidth();
      final double otherWidth = otherLabelContent.getBounds().getWidth();

      if (width > otherWidth) {
        otherLabelContent.setRightPadding((int) Math.round(width - otherWidth) + NODELABEL_PADDING);
      } else {
        labelContent.setRightPadding((int) Math.round(otherWidth - width) + NODELABEL_PADDING);
      }

      otherDiffNode.getRealizer().regenerate();
    }

    final SingleDiffNode diffNode = GraphGetter.getDiffNode(graphContainer, basicblock);
    diffNode.getRealizer().regenerate();

    graphContainer
        .getSuperGraph()
        .refreshSuperNodeSize(
            graphContainer.getPrimaryGraph(),
            graphContainer.getSecondaryGraph(),
            diffNode.getSuperDiffNode());

    graphContainer.updateViews();
  }

  private boolean validateLineCountEquality(
      final ZyLabelContent labelContent, final RawBasicBlock basicblock) {
    final ESide side = basicblock.getSide();
    final SingleDiffNode otherDiffNode;
    final RawBasicBlock otherBasicblock;

    if (side == ESide.PRIMARY) {
      otherDiffNode =
          GraphGetter.getSecondaryDiffNode(graphContainer.getPrimaryGraph(), basicblock);
      otherBasicblock = GraphGetter.getSecondaryRawBasicblock(graphContainer, basicblock);
    } else {
      otherDiffNode =
          GraphGetter.getPrimaryDiffNode(graphContainer.getSecondaryGraph(), basicblock);
      otherBasicblock = GraphGetter.getPrimaryRawBasicblock(graphContainer, basicblock);
    }

    if (otherBasicblock != null) {
      final ZyLabelContent otherLabelContent = otherDiffNode.getRealizer().getNodeContent();
      return labelContent.getLineCount() == otherLabelContent.getLineCount();
    }
    return true;
  }

  @Override
  public void recreateLabelLines(final ZyLabelContent labelContent, final Object persistentModel) {
    int lineIndex = 0;

    for (final ZyLineContent lineContent : labelContent.getContent()) {
      final IZyEditableObject lineObject = lineContent.getLineObject();
      if (lineObject != null && lineObject.getPersistentModel() == persistentModel) {
        if (persistentModel instanceof RawBasicBlock) {
          recreateLabelCommentSynchronized(labelContent, (RawBasicBlock) persistentModel);
          return;
        }
        if (persistentModel instanceof RawInstruction) {
          lineIndex = labelContent.getFirstLineIndexOfModelAt(lineIndex);

          recreateInstructionSynchronized(
              labelContent, (RawInstruction) persistentModel, lineIndex);
          if (!validateLineCountEquality(
              labelContent, ((BasicBlockLineObject) labelContent.getModel()).getRawBasicblock())) {
            throw new RuntimeException(
                "Pimary and secondary basicblock label content line count are not equal.");
          }
        }
      }

      lineIndex++;
    }

    if (persistentModel instanceof RawBasicBlock) {
      // a new label comment has been created
      recreateLabelCommentSynchronized(labelContent, (RawBasicBlock) persistentModel);
      if (!validateLineCountEquality(
          labelContent, ((BasicBlockLineObject) labelContent.getModel()).getRawBasicblock())) {
        throw new RuntimeException(
            "Pimary and secondary basicblock label content line count are not equal.");
      }
    }
  }

  @Override
  public void refreshSize(final ZyLabelContent labelContent, final Object persistentModel) {
    RawBasicBlock basicblock = null;

    if (persistentModel instanceof RawBasicBlock) {
      basicblock = (RawBasicBlock) persistentModel;
    } else if (persistentModel instanceof RawInstruction) {
      basicblock = ((BasicBlockLineObject) labelContent.getModel()).getRawBasicblock();
    } else {
      return;
    }

    final SingleDiffNode diffNode = GraphGetter.getDiffNode(graphContainer, basicblock);

    graphContainer
        .getSuperGraph()
        .refreshSuperNodeSize(
            graphContainer.getPrimaryGraph(),
            graphContainer.getPrimaryGraph(),
            diffNode.getSuperDiffNode());
  }
}
