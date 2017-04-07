package com.google.security.zynamics.bindiff.io;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.security.zynamics.BinExport.BinExport2;
import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.EInstructionHighlighting;
import com.google.security.zynamics.bindiff.enums.EJumpType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.io.matches.FunctionDiffSocketXmlData;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCall;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstruction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstructionComment;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawJump;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

/** Reads data from BinExport2 files and renders disassemblies. */
public class BinExport2Reader {
  private final ESide side;

  private final BinExport2 binexport;
  private int maxMnemonicLen;
  private final Map<IAddress, RawInstructionComment> addressComments;

  public BinExport2Reader(final File file, final ESide side) throws IOException {
    this.side = side;

    try (final InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
      binexport = BinExport2.parseFrom(stream);

      for (final BinExport2.Mnemonic mnemonic : binexport.getMnemonicList()) {
        maxMnemonicLen = Math.max(maxMnemonicLen, mnemonic.getName().length());
      }

      // Cache instruction comments
      addressComments = new TreeMap<>();
      for (final BinExport2.Reference addressComment : binexport.getAddressCommentList()) {
        final IAddress address =
            new CAddress(getInstructionAddress(addressComment.getInstructionIndex()));
        final String text = binexport.getStringTable(addressComment.getStringTableIndex());
        addressComments.put(
            address, new RawInstructionComment(text, ECommentPlacement.BEHIND_LINE));
      }
    }
  }

  private static EFunctionType vertexToFunctionType(final BinExport2.CallGraph.Vertex.Type type) {
    switch (type) {
      case NORMAL:
        return EFunctionType.NORMAL;
      case LIBRARY:
        return EFunctionType.LIBRARY;
      case IMPORTED:
        return EFunctionType.IMPORTED;
      case THUNK:
        return EFunctionType.THUNK;
      case INVALID:
        return EFunctionType.UNKNOWN;
      default:
        throw new IllegalArgumentException("Invalid vertex type");
    }
  }

  private static String getFunctionName(final BinExport2.CallGraph.Vertex vertex) {
    String name = vertex.getDemangledName();
    if (!name.isEmpty()) {
      return name;
    }
    name = vertex.getMangledName();
    if (!name.isEmpty()) {
      return name;
    }
    return String.format("sub_%08X", vertex.getAddress());
  }

  private static EJumpType toJumpType(final BinExport2.FlowGraph.Edge.Type type) {
    switch (type) {
      case CONDITION_TRUE:
        return EJumpType.JUMP_TRUE;
      case CONDITION_FALSE:
        return EJumpType.JUMP_FALSE;
      case UNCONDITIONAL:
        return EJumpType.UNCONDITIONAL;
      case SWITCH:
        return EJumpType.SWITCH;
      default:
        throw new IllegalArgumentException("Invalid flow graph edge type");
    }
  }

  public RawCallGraph readCallgraph() {
    final BinExport2.CallGraph callGraph = binexport.getCallGraph();

    final List<RawFunction> nodes = new ArrayList<>();
    for (final BinExport2.CallGraph.Vertex vertex : callGraph.getVertexList()) {
      final String name = getFunctionName(vertex);

      nodes.add(
          new RawFunction(
              new CAddress(vertex.getAddress()),
              name,
              vertexToFunctionType(vertex.getType()),
              side));
    }

    final List<RawCall> edges = new ArrayList<>();
    final Set<IAddress> edgeSet = new TreeSet<>();

    for (final BinExport2.CallGraph.Edge edge : callGraph.getEdgeList()) {
      final RawFunction source = nodes.get(edge.getSourceVertexIndex());
      if (edgeSet.add(source.getAddress())) {
        edges.add(
            new RawCall(source, nodes.get(edge.getTargetVertexIndex()), source.getAddress(), side));
      }
    }

    return new RawCallGraph(nodes, edges, side);
  }

  private long getInstructionAddress(int index) {
    BinExport2.Instruction instruction = binexport.getInstruction(index);
    if (instruction.hasAddress()) {
      return instruction.getAddress();
    }
    int delta = 0;
    for (--index; index >= 0; --index) {
      instruction = binexport.getInstruction(index);
      delta += instruction.getRawBytes().size();
      if (instruction.hasAddress()) {
        return binexport.getInstruction(index).getAddress() + delta;
      }
    }
    throw new IllegalStateException("Invalid instruction index");
  }

  private static char highlightChar(final EInstructionHighlighting value) {
    return (char) EInstructionHighlighting.getOrdinal(value);
  }

  private static void renderExpression(
      final BinExport2 proto,
      final BinExport2.Operand operand,
      int index,
      final StringBuffer output) {
    final String architectureName = proto.getMetaInformation().getArchitectureName();
    boolean longMode = architectureName.endsWith("64");
    final BinExport2.Expression expression = proto.getExpression(operand.getExpressionIndex(index));
    final String symbol = expression.getSymbol();
    switch (expression.getType()) {
      case OPERATOR:
        {
          List<Integer> children = new ArrayList<>(4 /* Default maximum on x86 */);
          for (int i = index + 1;
              i < operand.getExpressionIndexCount()
                  && proto.getExpression(operand.getExpressionIndex(i)).getParentIndex()
                      == operand.getExpressionIndex(index);
              i++) {
            children.add(operand.getExpressionIndex(i));
          }
          int numChildren = children.size();
          if ("{".equals(symbol)) { // ARM Register lists
            output.append("{");
            for (int i = 0; i < numChildren; i++) {
              renderExpression(proto, operand, index + 1 + i, output);
              if (i != numChildren - 1) {
                output.append(highlightChar(EInstructionHighlighting.TYPE_NEWOPERAND_COMMA));
                output.append(",");
              }
            }
            output.append("}");
          } else if (numChildren == 1) {
            // Only a single child, treat expression as prefix operator (like 'ss:').
            output.append(highlightChar(EInstructionHighlighting.TYPE_OPERATOR));
            output.append(symbol);
            renderExpression(proto, operand, index + 1, output);
          } else if (numChildren > 1) {
            // Multiple children, treat expression as infix operator ('+' or '*').
            // TODO(cblichmann): Deal with negative numbers.
            renderExpression(proto, operand, index + 1, output);
            for (int i = 1; i < numChildren; i++) {
              output.append(highlightChar(EInstructionHighlighting.TYPE_OPERATOR));
              output.append(symbol);
              renderExpression(proto, operand, index + 1 + i, output);
            }
          }
          break;
        }
      case SYMBOL:
        output.append(highlightChar(EInstructionHighlighting.TYPE_SYMBOL));
        output.append(symbol);
        break;
      case REGISTER:
        output.append(highlightChar(EInstructionHighlighting.TYPE_REGISTER));
        output.append(symbol);
        break;
      case SIZE_PREFIX:
        {
          if ((longMode && symbol.equals("b8")) || (!longMode && !symbol.equals("b4"))) {
            output.append(highlightChar(EInstructionHighlighting.TYPE_SIZEPREFIX));
            output.append(symbol);
            output.append(" ");
          }
          renderExpression(proto, operand, index + 1, output);
          break;
        }
      case DEREFERENCE:
        output.append(highlightChar(EInstructionHighlighting.TYPE_DEREFERENCE));
        output.append("[");
        if (index + 1 < operand.getExpressionIndexCount()) {
          renderExpression(proto, operand, index + 1, output);
        }
        output.append(highlightChar(EInstructionHighlighting.TYPE_DEREFERENCE));
        output.append("]");
        break;
      case IMMEDIATE_INT:
      case IMMEDIATE_FLOAT:
        output.append(highlightChar(EInstructionHighlighting.TYPE_IMMEDIATE));
        output.append(String.format("0x%X", expression.getImmediate()));
        break;
      default:
        throw new IllegalStateException("Invalid expression type");
    }
  }

  private static String renderInstructionOperands(
      final BinExport2 proto, final BinExport2.Instruction instruction) {
    final StringBuffer disassembly = new StringBuffer();
    for (int i = 0; i < instruction.getOperandIndexCount(); i++) {
      final BinExport2.Operand operand = proto.getOperand(instruction.getOperandIndex(i));
      for (int j = 0; j < operand.getExpressionIndexCount(); j++) {
        final BinExport2.Expression expression = proto.getExpression(operand.getExpressionIndex(j));
        if (!expression.hasParentIndex()) {
          renderExpression(proto, operand, j, disassembly);
        }
      }
      if (i != instruction.getOperandIndexCount() - 1) {
        disassembly.append(", ");
      }
    }
    return disassembly.toString();
  }

  public RawFlowGraph readFlowgraph(final Diff diff, final IAddress functionAddress) {
    final RawFunction function = diff.getCallgraph(side).getFunction(functionAddress);

    BinExport2.FlowGraph flowGraph = null;
    // TODO(cblichmann): Binary search!
    for (final BinExport2.FlowGraph curFlowGraph : binexport.getFlowGraphList()) {
      final BinExport2.BasicBlock entryBasicBlock =
          binexport.getBasicBlock(curFlowGraph.getEntryBasicBlockIndex());
      final BinExport2.Instruction firstInstruction =
          binexport.getInstruction(entryBasicBlock.getInstructionIndex(0).getBeginIndex());
      if (functionAddress.toLong() == firstInstruction.getAddress()) {
        flowGraph = curFlowGraph;
        break;
      }
    }

    final Map<Integer, RawBasicBlock> basicBlocks = new TreeMap<>();
    final List<RawJump> jumps = new ArrayList<>();

    if (flowGraph != null) {
      for (final Integer basicBlockIndex : flowGraph.getBasicBlockIndexList()) {
        final BinExport2.BasicBlock basicBlock = binexport.getBasicBlock(basicBlockIndex);
        final CAddress basicBlockAddress =
            new CAddress(getInstructionAddress(basicBlock.getInstructionIndex(0).getBeginIndex()));

        final EMatchState matchState;
        final FunctionMatchData match = function.getFunctionMatch();
        if (match != null && match.getBasicblockMatch(basicBlockAddress, side) != null) {
          matchState = EMatchState.MATCHED;
        } else {
          matchState =
              side == ESide.PRIMARY
                  ? EMatchState.PRIMARY_UNMATCHED
                  : EMatchState.SECONDRAY_UNMATCHED;
        }

        final SortedMap<IAddress, RawInstruction> instructions = new TreeMap<>();
        for (final BinExport2.BasicBlock.IndexRange range : basicBlock.getInstructionIndexList()) {
          int endIndex = !range.hasEndIndex() ? range.getBeginIndex() + 1 : range.getEndIndex();
          for (int i = range.getBeginIndex(); i < endIndex; i++) {
            final BinExport2.Instruction instruction = binexport.getInstruction(i);
            final IAddress instructionAddress = new CAddress(getInstructionAddress(i));

            final long[] callTargets = new long[instruction.getCallTargetCount()];
            for (int j = 0; j < callTargets.length; j++) {
              callTargets[j] = instruction.getCallTarget(j);
            }

            final List<RawInstructionComment> instructionComments = new ArrayList<>();
            final RawInstructionComment comment = addressComments.get(instructionAddress);
            if (comment != null) {
              instructionComments.add(comment);
            }

            final RawInstruction rawInstruction =
                new RawInstruction(
                    instructionAddress,
                    binexport.getMnemonic(instruction.getMnemonicIndex()).getName(),
                    maxMnemonicLen,
                    renderInstructionOperands(binexport, instruction).getBytes(UTF_8),
                    callTargets,
                    instructionComments);
            instructions.put(instructionAddress, rawInstruction);
          }
        }

        final RawBasicBlock rawBasicBlock =
            new RawBasicBlock(
                functionAddress,
                function.getName(),
                basicBlockAddress,
                instructions,
                side,
                matchState);
        basicBlocks.put(basicBlockIndex, rawBasicBlock);
      }

      for (final BinExport2.FlowGraph.Edge edge : flowGraph.getEdgeList()) {
        final RawBasicBlock source = basicBlocks.get(edge.getSourceBasicBlockIndex());
        final RawBasicBlock target = basicBlocks.get(edge.getTargetBasicBlockIndex());
        jumps.add(new RawJump(source, target, toJumpType(edge.getType())));
      }
    }

    final List<RawBasicBlock> bbs = new ArrayList<>();
    bbs.addAll(basicBlocks.values());
    return new RawFlowGraph(
        functionAddress, function.getName(), function.getFunctionType(), bbs, jumps, side);
  }

  public void readFlowGraphsStatistics(final Diff diff) {
    for (final BinExport2.FlowGraph flowGraph : binexport.getFlowGraphList()) {
      final BinExport2.BasicBlock entryBasicBlock =
          binexport.getBasicBlock(flowGraph.getEntryBasicBlockIndex());
      final BinExport2.Instruction firstInstruction =
          binexport.getInstruction(entryBasicBlock.getInstructionIndex(0).getBeginIndex());
      long functionAddress = firstInstruction.getAddress();
      final RawFunction function =
          diff.getCallgraph(side).getFunction(new CAddress(functionAddress));
      function.setSizeOfBasicblocks(flowGraph.getBasicBlockIndexCount());
      int numInstructions = 0;
      for (final int index : flowGraph.getBasicBlockIndexList()) {
        numInstructions += binexport.getBasicBlock(index).getInstructionIndexCount();
      }
      function.setSizeOfInstructions(numInstructions);
      function.setSizeOfJumps(flowGraph.getEdgeCount());
    }
  }

  public RawCallGraph readSingleFunctionDiffCallGraph(final FunctionDiffSocketXmlData data) {
    final long functionAddress = data.getFunctionAddress(side);
    final List<RawFunction> nodes = new ArrayList<>();
    final List<RawCall> edges = new ArrayList<>();
    final BinExport2.CallGraph callGraph = binexport.getCallGraph();
    for (final BinExport2.CallGraph.Vertex vertex : callGraph.getVertexList()) {
      if (functionAddress == vertex.getAddress()) {
        final String name = getFunctionName(vertex);
        nodes.add(
            new RawFunction(
                new CAddress(vertex.getAddress()),
                name,
                vertexToFunctionType(vertex.getType()),
                side));
        return new RawCallGraph(nodes, edges, side);
      }
    }
    throw new RuntimeException("Function flow graph not found.");
  }

  public String getArchitectureName() {
    return binexport.getMetaInformation().getArchitectureName();
  }

  public int getMaxMnemonicLen() {
    return maxMnemonicLen;
  }
}
