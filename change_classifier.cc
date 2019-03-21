#include "third_party/zynamics/bindiff/change_classifier.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/match_context.h"

namespace security {
namespace bindiff {
namespace {

bool InstructionsChanged(const FlowGraph& primary, const FlowGraph& secondary,
                         const BasicBlockFixedPoint& fixed_point) {
  const int matched_instruction_count =
      fixed_point.GetInstructionMatches().size();
  const auto primary_instructions =
      primary.GetInstructions(fixed_point.GetPrimaryVertex());
  const auto secondary_instructions =
      secondary.GetInstructions(fixed_point.GetSecondaryVertex());
  return secondary_instructions.second - secondary_instructions.first !=
             matched_instruction_count ||
         primary_instructions.second - primary_instructions.first !=
             matched_instruction_count;
}

bool IsBranchInversion(const FlowGraph& primary, const FlowGraph& secondary,
                       const BasicBlockFixedPoint& fixed_point) {
  const auto& instruction_matches = fixed_point.GetInstructionMatches();
  const int matched_instruction_count = instruction_matches.size();
  auto primary_instructions =
      primary.GetInstructions(fixed_point.GetPrimaryVertex());
  auto secondary_instructions =
      secondary.GetInstructions(fixed_point.GetSecondaryVertex());
  const int primary_changed =
      std::abs(secondary_instructions.second - secondary_instructions.first -
               matched_instruction_count);
  const int secondary_changed =
      std::abs(primary_instructions.second - primary_instructions.first -
               matched_instruction_count);
  if (primary_changed > 1 || secondary_changed > 1) {
    return false;
  }

  if (!instruction_matches.empty()) {
    for (auto i = instruction_matches.cbegin(),
              end = --instruction_matches.cend();
         i != end &&
         primary_instructions.first != primary_instructions.second &&
         secondary_instructions.first != secondary_instructions.second;
         ++i, ++primary_instructions.first, ++secondary_instructions.first) {
      if (&*primary_instructions.first != i->first ||
          &*secondary_instructions.first != i->second) {
        return false;
      }
    }
    // Return false if the single matched instruction is the branch instruction.
    if (instruction_matches.size() == 1) {
      if (instruction_matches.begin()->first ==
          &*--primary_instructions.second) {
        return false;
      }
    }
  }

  int primary_out_edges = 0;
  int secondary_out_edges = 0;
  FlowGraph::OutEdgeIterator i, end;
  for (boost::tie(i, end) =
           out_edges(fixed_point.GetPrimaryVertex(), primary.GetGraph());
       i != end; ++i, ++primary_out_edges) {
    // intentionally empty
  }
  for (boost::tie(i, end) =
           out_edges(fixed_point.GetSecondaryVertex(), secondary.GetGraph());
       i != end; ++i, ++secondary_out_edges) {
    // intentionally empty
  }
  if (primary_out_edges != secondary_out_edges || primary_out_edges < 2 ||
      secondary_out_edges < 2) {
    return false;
  }

  return true;
}

}  // namespace

void ClassifyChanges(FixedPoint* fixed_point) {
  const FlowGraph& primary = *fixed_point->GetPrimary();
  const FlowGraph& secondary = *fixed_point->GetSecondary();

  // Check for structural changes.
  if (primary.GetBasicBlockCount() != secondary.GetBasicBlockCount() ||
      boost::num_edges(primary.GetGraph()) !=
          boost::num_edges(secondary.GetGraph())) {
    fixed_point->SetFlag(CHANGE_STRUCTURAL);
  }
  // If we haven't already diagnosed a structural change due to differing
  // basic block or edge count check that all edges have been matched.
  if (!fixed_point->HasFlag(CHANGE_STRUCTURAL)) {
    // We only need to iterate primary edges since edge count is equal at this
    // point.
    FlowGraph::EdgeIterator edge, end;
    for (boost::tie(edge, end) = boost::edges(primary.GetGraph()); edge != end;
         ++edge) {
      if (primary.GetFixedPoint(boost::source(*edge, primary.GetGraph())) ==
              nullptr ||
          primary.GetFixedPoint(boost::target(*edge, primary.GetGraph())) ==
              nullptr) {
        fixed_point->SetFlag(CHANGE_STRUCTURAL);
        break;
      }
    }
  }

  // Check for instruction changes, branch inversions and call target changes.
  const auto& basic_block_fixed_points =
      fixed_point->GetBasicBlockFixedPoints();
  for (auto i = basic_block_fixed_points.cbegin(),
            end = basic_block_fixed_points.cend();
       i != end; ++i) {
    const BasicBlockFixedPoint& basic_block_fixed_point = *i;
    if (InstructionsChanged(primary, secondary, basic_block_fixed_point)) {
      fixed_point->SetFlag(CHANGE_INSTRUCTIONS);

      // Branch inversions
      if (!(fixed_point->GetFlags() & CHANGE_BRANCHINVERSION)) {
        if (IsBranchInversion(primary, secondary, basic_block_fixed_point)) {
          fixed_point->SetFlag(CHANGE_BRANCHINVERSION);
        }
      }
    }

    if (!(fixed_point->GetFlags() & CHANGE_CALLS)) {  // Call target changes
      auto call_targets_primary = primary.GetCallTargets(i->GetPrimaryVertex());
      auto call_targets_secondary =
          secondary.GetCallTargets(i->GetSecondaryVertex());
      if (call_targets_primary.second - call_targets_primary.first ==
          call_targets_secondary.second - call_targets_secondary.first) {
        for (; call_targets_primary.first != call_targets_primary.second &&
               call_targets_secondary.first != call_targets_secondary.second;
             ++call_targets_primary.first, ++call_targets_secondary.first) {
          const CallGraph::Vertex vertex1 =
              primary.GetCallGraph()->GetVertex(*call_targets_primary.first);
          const CallGraph::Vertex vertex2 = secondary.GetCallGraph()->GetVertex(
              *call_targets_secondary.first);
          if (vertex1 == CallGraph::kInvalidVertex ||
              vertex2 == CallGraph::kInvalidVertex) {
            continue;
          }
          const FlowGraph* target_primary =
              primary.GetCallGraph()->GetFlowGraph(vertex1);
          const FlowGraph* target_secondary =
              secondary.GetCallGraph()->GetFlowGraph(vertex2);
          if (!target_primary || !target_secondary) {
            fixed_point->SetFlag(CHANGE_CALLS);
            break;
          }
          const FixedPoint* target_fixed_point =
              target_primary->GetFixedPoint();
          if (!target_fixed_point ||
              target_fixed_point->GetSecondary() != target_secondary) {
            fixed_point->SetFlag(CHANGE_CALLS);
            break;
          }
        }
      }
    }
  }

  // Check for changes in entry point.
  // TODO(cblichmann): Set this only if it is the only change (i.e. no other
  //                   instructions have been modified).
  if (primary.GetBasicBlockCount()) {
    const auto* basic_block_fixed_point = primary.GetFixedPoint(
        primary.GetVertex(primary.GetEntryPointAddress()));
    if (!basic_block_fixed_point ||
        secondary.GetVertex(secondary.GetEntryPointAddress()) !=
            basic_block_fixed_point->GetSecondaryVertex() ||
        InstructionsChanged(primary, secondary, *basic_block_fixed_point)) {
      fixed_point->SetFlag(CHANGE_ENTRYPOINT);
    }
  }

  // Check for loop changes.
  if (primary.GetLoopCount() != secondary.GetLoopCount()) {
    fixed_point->SetFlag(CHANGE_LOOPS);
  }

  // TODO(cblichmann): Check for changes in operands.
}

void ClassifyChanges(MatchingContext* context) {
  for (auto& fixed_point : context->fixed_points_) {
    ClassifyChanges(const_cast<FixedPoint*>(&fixed_point));
  }
}

std::string GetChangeDescription(ChangeType change) {
  std::string result("GIOJELC");
  for (int i = 0; i < CHANGE_COUNT; ++i) {
    if (!(change & (1 << i))) {
      result[i] = '-';
    }
  }
  return result;
}

}  // namespace bindiff
}  // namespace security
