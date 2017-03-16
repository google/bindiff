#include "third_party/zynamics/bindiff/ida/results.h"

#include <pro.h>        // NOLINT
#include <enum.hpp>     // NOLINT
#include <frame.hpp>    // NOLINT
#include <funcs.hpp>    // NOLINT
#include <kernwin.hpp>  // NOLINT
#include <name.hpp>     // NOLINT
#include <struct.hpp>   // NOLINT
#include <xref.hpp>     // NOLINT

#include "base/logging.h"
#include "base/stringprintf.h"
#ifndef GOOGLE  // MOE:strip_line
#include "strings/strutil.h"
#endif  // MOE:strip_line
#include "third_party/zynamics/bindiff/call_graph_matching.h"
#include "third_party/zynamics/bindiff/flow_graph_matching.h"
#include "third_party/zynamics/bindiff/ida/names.h"
#include "third_party/zynamics/bindiff/ida/ui.h"
#include "third_party/zynamics/bindiff/matching.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/filesystem_util.h"
#include "third_party/zynamics/binexport/ida/ui.h"
#include "third_party/zynamics/binexport/timer.h"

// These are defined in main_plugin.cc.
extern uint32_t idaapi GetNumUnmatchedSecondary(void* object);
extern void idaapi GetUnmatchedSecondaryDescription(void* object, uint32_t index,
                                                    char* const* line);
extern uint32_t idaapi GetNumUnmatchedPrimary(void* object);
extern void idaapi GetUnmatchedPrimaryDescription(void* object, uint32_t index,
                                                  char* const* line);

namespace {

void ReadTemporaryFlowGraph(const FixedPointInfo& fixed_point_info,
                            const FlowGraphInfos& flow_graph_infos,
                            CallGraph* call_graph, FlowGraph* flow_graph,
                            Instruction::Cache* instruction_cache) {
  auto info = flow_graph_infos.find(fixed_point_info.primary);
  if (info == flow_graph_infos.end()) {
    throw std::runtime_error("error: flow graph not found for fixed point");
  }
  std::ifstream stream(call_graph->GetFilePath().c_str(),
                       std::ios_base::binary);
  BinExport2 proto;
  if (!proto.ParseFromIstream(&stream)) {
    throw std::runtime_error("failed parsing protocol buffer");
  }
  for (const auto& proto_flow_graph : proto.flow_graph()) {
    // Entry point address is always set.
    const auto address =
        proto
            .instruction(
                proto.basic_block(proto_flow_graph.entry_basic_block_index())
                    .instruction_index(0)
                    .begin_index())
            .address();
    if (address == info->second.address) {
      flow_graph->SetCallGraph(call_graph);
      flow_graph->Read(proto, proto_flow_graph, call_graph, instruction_cache);
      return;
    }
  }
  throw std::runtime_error("error: flow graph data not found");
}

// TODO(cblichmann): Move to names.h
void UpdateName(CallGraph* call_graph, Address address) {
  const std::string& name = GetName(address);
  const CallGraph::Vertex vertex = call_graph->GetVertex(address);
  if (!name.empty() && name != call_graph->GetName(vertex)) {
    call_graph->SetName(vertex, name);
    const std::string& demangled_name = GetDemangledName(address);
    if (demangled_name != name) {
      call_graph->SetDemangledName(vertex, demangled_name);
    } else {
      call_graph->SetDemangledName(vertex, "");
    }
  }
}

// Sort by: similarity desc, confidence desc, address asc.
bool SortBySimilarity(const FixedPointInfo* one, const FixedPointInfo* two) {
  CHECK(one && two);
  return one->similarity == two->similarity
             ? (one->confidence == two->confidence
                    ? one->primary < two->primary
                    : one->confidence > two->confidence)
             : one->similarity > two->similarity;
}

bool PortFunctionName(FixedPoint* fixed_point) {
  CallGraph* primary_call_graph = fixed_point->GetPrimary()->GetCallGraph();
  CallGraph* secondary_call_graph = fixed_point->GetSecondary()->GetCallGraph();
  const Address primary_address =
      fixed_point->GetPrimary()->GetEntryPointAddress();
  const Address secondary_address =
      fixed_point->GetSecondary()->GetEntryPointAddress();
  if (const func_t* function = get_func(static_cast<ea_t>(primary_address))) {
    if (function->startEA == primary_address) {
      if (!secondary_call_graph->HasRealName(
              secondary_call_graph->GetVertex(secondary_address))) {
        return false;
      }

      const std::string& name = fixed_point->GetSecondary()->GetName();
      enum { BUFFER_SIZE = MAXSTR };
      char buffer[BUFFER_SIZE];
      get_true_name(static_cast<ea_t>(primary_address),
                    static_cast<ea_t>(primary_address), buffer, BUFFER_SIZE);
      if (std::string(buffer) != name) {
        set_name(static_cast<ea_t>(primary_address), name.c_str(),
                 SN_NOWARN | SN_CHECK);
        const CallGraph::Vertex vertex =
            primary_call_graph->GetVertex(primary_address);
        primary_call_graph->SetName(vertex, name);
        primary_call_graph->SetDemangledName(
            vertex, GetDemangledName(static_cast<ea_t>(primary_address)));
        return true;
      }
    }
  }
  return false;
}

size_t SetComments(Address source, Address target, const Comments& comments,
                   FixedPoint* fixed_point = 0) {
  int comment_count = 0;
  const OperatorId begin(std::make_pair(source, 0));
  for (auto i = comments.lower_bound(begin);
       i != comments.end() && i->first.first == source; ++i, ++comment_count) {
    CHECK(source == i->first.first);
    const Comment& comment = i->second;
    const Address address = target;
    const int operand_id = i->first.second;

    // Do not port auto generated names (unfortunately this does not work for
    // comments)
    // The IDA API is totally broken here. See:
    // https://zynamics.fogbugz.com/default.asp?4451
    if ((comment.type_ == Comment::ENUM || comment.type_ == Comment::LOCATION ||
         comment.type_ == Comment::GLOBALREFERENCE ||
         comment.type_ == Comment::LOCALREFERENCE) &&
        !is_uname(comment.comment_.c_str())) {
      continue;
    }

    switch (comment.type_) {
      case Comment::REGULAR: {
        set_cmt(static_cast<ea_t>(address), comment.comment_.c_str(),
                comment.repeatable_);
      } break;
      case Comment::ENUM: {
        unsigned char serial;
        if (isEnum0(getFlags(static_cast<ea_t>(address))) && operand_id == 0) {
          if (int id = get_enum_id(static_cast<ea_t>(address), operand_id,
                                   &serial) != BADNODE) {
            set_enum_name(id, comment.comment_.c_str());
          }
        }
        if (isEnum1(getFlags(static_cast<ea_t>(address))) && operand_id == 1) {
          if (int id = get_enum_id(static_cast<ea_t>(address), operand_id,
                                   &serial) != BADNODE) {
            set_enum_name(id, comment.comment_.c_str());
          }
        }
      } break;
      case Comment::FUNCTION: {
        if (func_t* function = get_func(static_cast<ea_t>(address))) {
          if (function->startEA == address) {
            set_func_cmt(function, comment.comment_.c_str(),
                         comment.repeatable_);
          }
        }
      } break;
      case Comment::LOCATION: {
        if (fixed_point) {
          PortFunctionName(fixed_point);
        }
      } break;
      case Comment::ANTERIOR: {
        const std::string existing_comment = GetLineComments(address, -1);
        if (existing_comment.rfind(comment.comment_) == std::string::npos) {
          describe(static_cast<ea_t>(address), true, "%s",
                   comment.comment_.c_str());
        }
      } break;
      case Comment::POSTERIOR: {
        const std::string existing_comment = GetLineComments(address, +1);
        if (existing_comment.rfind(comment.comment_) == std::string::npos) {
          describe(static_cast<ea_t>(address), false, "%s",
                   comment.comment_.c_str());
        }
      } break;
      case Comment::GLOBALREFERENCE: {
        int count = 0;
        xrefblk_t xb;
        for (bool ok = xb.first_from(static_cast<ea_t>(address), XREF_DATA); ok;
             ok = xb.next_from(), ++count) {
          if (count == operand_id - UA_MAXOP - 1024) {
            char current_name[MAXSTR];
            get_name(BADADDR, xb.to, current_name, MAXSTR);
            if (strcmp(current_name, comment.comment_.c_str()) != 0) {
              set_name(xb.to, comment.comment_.c_str(), SN_NOWARN | SN_CHECK);
            }
            break;
          }
        }
      } break;
      case Comment::LOCALREFERENCE: {
        func_t* function = get_func(static_cast<ea_t>(address));
        if (!function) break;

        struc_t* frame = get_frame(function);
        if (!frame) break;

        for (int operand_num = 0; operand_num < UA_MAXOP; ++operand_num) {
          const ea_t offset = calc_stkvar_struc_offset(
              function, static_cast<ea_t>(address), operand_num);
          if (offset == BADADDR) {
            continue;
          }

          if (operand_num == operand_id - UA_MAXOP - 2048) {
            set_member_name(frame, offset, comment.comment_.c_str());
          }
        }
      } break;
      case Comment::STRUCTURE: {
        /*
        tid_t id = 0;
        adiff_t disp = 0;
        adiff_t delta = 0;
        if (get_struct_operand(address, operand_num, &id, &disp, &delta)) {
          // Bug: this must be recursive for nested structs
          if (const struc_t* structure = get_struc(id)) {
            set_struc_name(structure->id, comment.m_Comment.c_str());
          }
          // TODO: structure members
        } */
      } break;
      default:
        LOG(INFO) << "Unknown comment type " << comment.type_ << ": "
                  << StringPrintf(HEX_ADDRESS, source) << " -> "
                  << StringPrintf(HEX_ADDRESS, target) << " "
                  << i->second.comment_;
        break;
    }
  }
  return comment_count;
}

size_t SetComments(FixedPoint* fixed_point, const Comments& comments,
                   Address start_source, Address end_source,
                   Address start_target, Address end_target,
                   double min_confidence, double min_similarity) {
  // we call SetComments three times here which potentially sets a single
  // comment multiple times. We have to do this however, because we are
  // iterating over fixedpoints and might miss comments otherwise.
  // i.e. we may have a function fixed point but no corresponding instruction
  // fixed point for the function's entry point address
  size_t counts = 0;
  Address source = fixed_point->GetSecondary()->GetEntryPointAddress();
  Address target = fixed_point->GetPrimary()->GetEntryPointAddress();
  fixed_point->SetCommentsPorted(true);

  if (source >= start_source && source <= end_source &&
      target >= start_target && target <= end_target) {
    if (fixed_point->GetConfidence() >= min_confidence &&
        fixed_point->GetSimilarity() >= min_similarity) {
      counts += SetComments(source, target, comments, fixed_point);
      counts += PortFunctionName(fixed_point);
    } else {
      // Skip whole function if similarity or confidence criteria aren't
      // satisfied.
      return counts;
    }
  }

  auto source_vertex = fixed_point->GetSecondary()->GetVertex(source);
  auto target_vertex = fixed_point->GetPrimary()->GetVertex(target);
  const BasicBlockFixedPoints& basic_block_fixed_points =
      fixed_point->GetBasicBlockFixedPoints();
  for (auto j = basic_block_fixed_points.begin();
       j != basic_block_fixed_points.end(); ++j) {
    if (source_vertex != j->GetSecondaryVertex() ||
        target_vertex != j->GetPrimaryVertex()) {
      source_vertex = j->GetSecondaryVertex();
      target_vertex = j->GetPrimaryVertex();
      source = fixed_point->GetSecondary()->GetAddress(source_vertex);
      target = fixed_point->GetPrimary()->GetAddress(target_vertex);
      if (source >= start_source && source <= end_source &&
          target >= start_target && target <= end_target) {
        counts += SetComments(source, target, comments);
      }
    }

    const InstructionMatches& instruction_matches = j->GetInstructionMatches();
    for (auto k = instruction_matches.begin(); k != instruction_matches.end();
         ++k) {
      const Address target_address = k->first->GetAddress();
      const Address source_address = k->second->GetAddress();
      if (source != source_address || target != target_address) {
        source = source_address;
        target = target_address;
        source_vertex = fixed_point->GetSecondary()->GetVertex(source);
        target_vertex = fixed_point->GetPrimary()->GetVertex(target);
        if (source >= start_source && source <= end_source &&
            target >= start_target && target <= end_target) {
          counts += SetComments(source, target, comments);
        }
      }
    }
  }
  return counts;
}

}  // namespace

Results::Results()
    : temp_database_("temporary.database", true),
      incomplete_results_(false),  // Set when we have loaded from disk.
      similarity_(0.0),
      confidence_(0.0),
      dirty_(false),
      diff_database_id_(0) {}

Results::~Results() {
  // we need to close this explicitly here as otherwise the
  // DeleteTemporaryFiles() call below will fail due to locked db file
  temp_database_.Close();
  DeleteFlowGraphs(&flow_graphs1_);
  DeleteFlowGraphs(&flow_graphs2_);
  DatabaseTransmuter::DeleteTempFile();
  Results::DeleteTemporaryFiles();
}

void Results::SetDirty() { dirty_ = true; }

bool Results::IsDirty() const { return dirty_; }

void Results::DeleteTemporaryFiles() {
  // Extremely dangerous, make very sure GetDirectory _never_ returns something
  // like "C:".
  try {
    RemoveAll(GetTempDirectory("BinDiff", /* create = */ false));
  } catch (...) {  // We don't care if it failed-only litters the temp dir a bit
  }
}

uint32_t Results::GetColor(uint32_t index) const {
  if (!index || index > indexed_fixed_points_.size()) {
    return 0;
  }

  const FixedPointInfo& fixed_point = *indexed_fixed_points_[index - 1];

  if (fixed_point.IsManual()) {
    // Mark manual matches in blue.
    return (230 << 16) | (200 << 8) | 150;
  }
  // Choose hue for automatic matches according to similarity score.
  uint8_t r = 0;
  uint8_t g = 0;
  uint8_t b = 0;
  HsvToRgb(360 * 0.31 * fixed_point.similarity, 0.3, 0.9, r, g, b);
  return (b << 16) | (g << 8) | r;
}

size_t Results::GetNumFixedPoints() const {
  return indexed_fixed_points_.size();
}

size_t Results::GetNumUnmatchedPrimary() const {
  return indexed_flow_graphs1_.size();
}

size_t Results::GetNumUnmatchedSecondary() const {
  return indexed_flow_graphs2_.size();
}

Address Results::GetSecondaryAddress(size_t index) const {
  if (!index || index > indexed_flow_graphs2_.size()) {
    return 0;
  }
  return indexed_flow_graphs2_[index - 1]->address;
}

Address Results::GetPrimaryAddress(size_t index) const {
  if (!index || index > indexed_flow_graphs1_.size()) {
    return 0;
  }
  return indexed_flow_graphs1_[index - 1]->address;
}

Address Results::GetMatchPrimaryAddress(size_t index) const {
  if (!index || index > indexed_fixed_points_.size()) {
    return 0;
  }
  return indexed_fixed_points_[index - 1]->primary;
}

void Results::GetUnmatchedDescriptionPrimary(size_t index,
                                             char* const* line) const {
  GetUnmatchedDescription(indexed_flow_graphs1_, index, line);
}

void Results::GetUnmatchedDescriptionSecondary(size_t index,
                                               char* const* line) const {
  GetUnmatchedDescription(indexed_flow_graphs2_, index, line);
}

size_t Results::GetNumStatistics() const {
  return counts_.size() + histogram_.size() + 2;
}

bool Results::IncrementalDiff() {
  WaitBox wait_box("Performing incremental diff...");

  if (IsInComplete()) {
    const std::string temp_dir(
        GetTempDirectory("BinDiff", /* create = */ true));
    {
      ::Read(call_graph1_.GetFilePath(), &call_graph1_, &flow_graphs1_,
             &flow_graph_infos1_, &instruction_cache_);
      ::Read(call_graph2_.GetFilePath(), &call_graph2_, &flow_graphs2_,
             &flow_graph_infos2_, &instruction_cache_);

      CopyFile(input_filename_, JoinPath(temp_dir, "incremental.BinDiff"));

      SqliteDatabase database(
          JoinPath(temp_dir, "incremental.BinDiff").c_str());
      DatabaseTransmuter writer(database, fixed_point_infos_);
      Write(&writer);

      DatabaseReader::ReadFullMatches(&database, &call_graph1_, &call_graph2_,
                                      &flow_graphs1_, &flow_graphs2_,
                                      &fixed_points_);
    }

    std::remove(JoinPath(temp_dir, "incremental.BinDiff").c_str());
    incomplete_results_ = false;
  }

  Timer<> timer;
  MatchingContext context(call_graph1_, call_graph2_, flow_graphs1_,
                          flow_graphs2_, fixed_points_);

  // try to find any confirmed fixedpoints, if we don't have any we can just ret
  bool has_confirmed_fixedpoints = false;
  for (auto i = fixed_points_.cbegin(), end = fixed_points_.cend(); i != end;
       ++i) {
    const FixedPoint& fixedpoint = *i;
    if (fixedpoint.GetMatchingStep() == "function: manual") {
      has_confirmed_fixedpoints = true;
      break;
    }
  }
  if (!has_confirmed_fixedpoints) {
    warning(
        "No manually confirmed fixedpoints found. Please add some matches "
        "or use the matched functions window context menu to confirm automatic "
        "matches before running an incremental diff");
    return false;
  }

  // Remove all non-manual matches from current result
  for (auto i = fixed_points_.begin(), end = fixed_points_.end(); i != end;) {
    FixedPoint& fixed_point = const_cast<FixedPoint&>(*i);
    FlowGraph* primary = fixed_point.GetPrimary();
    FlowGraph* secondary = fixed_point.GetSecondary();
    if (fixed_point.GetMatchingStep() == "function: manual") {
      ++i;
      continue;  // Keep confirmed fixed points.
    }
    fixed_points_.erase(i++);

    primary->ResetMatches();
    secondary->ResetMatches();
    temp_database_.DeleteFromTempDatabase(primary->GetEntryPointAddress(),
                                          secondary->GetEntryPointAddress());
  }

  // These will get refilled by ShowResults().
  indexed_flow_graphs1_.clear();
  indexed_flow_graphs2_.clear();
  indexed_fixed_points_.clear();
  histogram_.clear();
  counts_.clear();

  // Diff
  const MatchingSteps default_callgraph_steps(GetDefaultMatchingSteps());
  const MatchingStepsFlowGraph default_basicblock_steps(
      GetDefaultMatchingStepsBasicBlock());
  Diff(&context, default_callgraph_steps, default_basicblock_steps);

  // Refill fixed point info.
  fixed_point_infos_.clear();
  for (auto i = fixed_points_.cbegin(), end = fixed_points_.cend(); i != end;
       ++i) {
    FixedPointInfo info;
    const FixedPoint& fixed_point = *i;
    info.algorithm = FindString(fixed_point.GetMatchingStep());
    info.confidence = fixed_point.GetConfidence();
    info.evaluate = false;
    info.flags = fixed_point.GetFlags();
    info.primary = fixed_point.GetPrimary()->GetEntryPointAddress();
    info.secondary = fixed_point.GetSecondary()->GetEntryPointAddress();
    info.similarity = fixed_point.GetSimilarity();
    info.comments_ported = fixed_point.GetCommentsPorted();

    Counts counts;
    Histogram histogram;
    FlowGraphs dummy1;
    dummy1.insert(fixed_point.GetPrimary());
    FlowGraphs dummy2;
    dummy2.insert(fixed_point.GetSecondary());
    FixedPoints dummy3;
    dummy3.insert(fixed_point);
    GetCountsAndHistogram(dummy1, dummy2, dummy3, &histogram, &counts);
    info.basic_block_count = counts["basicBlock matches (library)"] +
                             counts["basicBlock matches (non-library)"];
    info.instruction_count = counts["instruction matches (library)"] +
                             counts["instruction matches (non-library)"];
    info.edge_count = counts["flowGraph edge matches (library)"] +
                      counts["flowGraph edge matches (non-library)"];
    fixed_point_infos_.insert(info);
  }

  LOG(INFO) << StringPrintf("%.2fs", timer.elapsed())
            << " seconds for incremental matching.";

  SetDirty();
  return true;
}

void Results::GetStatisticsDescription(size_t index, char* const* line) const {
  // The target buffers are promised to be MAXSTR == 1024 characters long.
  if (!index) {
    snprintf(line[0], MAXSTR, "name");
    snprintf(line[1], MAXSTR, "value");
    return;
  }

  if (index > GetNumStatistics()) {
    return;
  }
  --index;

  size_t nr = 0;
  std::string description;
  std::string value;
  if (index < counts_.size()) {
    auto i = counts_.cbegin();
    for (; i != counts_.cend() && nr < index; ++i, ++nr) {
    }
    description = i->first;
    value = std::to_string(i->second);
  } else if (index < histogram_.size() + counts_.size()) {
    index -= counts_.size();
    auto i = histogram_.cbegin();
    for (; i != histogram_.cend() && nr < index; ++i, ++nr) {
    }
    description = i->first;
    value = std::to_string(i->second);
  } else if (index == histogram_.size() + counts_.size() + 1) {
    description = "similarity";
    value = std::to_string(similarity_);
  } else {
    description = "confidence";
    value = std::to_string(confidence_);
  }

  snprintf(line[0], MAXSTR, "%s", description.c_str());
  snprintf(line[1], MAXSTR, "%s", value.c_str());
}

int Results::DeleteMatch(size_t index) {
  if (index == static_cast<size_t>(START_SEL) || !index) {
    return 1;
  }
  if (index == static_cast<size_t>(END_SEL)) {
    // Refresh GUI when operation is done.
    // refresh_chooser("Matched Functions");
    refresh_chooser("Primary Unmatched");
    refresh_chooser("Secondary Unmatched");
    refresh_chooser("Statistics");
    return 1;
  }
  --index;
  if (index >= indexed_fixed_points_.size()) {
    return 0;
  }

  // This is real nasty:
  // - recalculate statistics
  // - remove fixedpointinfo
  // - remove matching flowgraph pointer from both graphs if loaded
  // - remove fixedpoint if loaded
  // ( - recalculate similarity and confidence )
  // - update all views
  // - be prepared to save .bindiff result file (again, tricky if it wasn't
  //   loaded fully)

  const FixedPointInfo& fixed_point_info = *indexed_fixed_points_[index];

  temp_database_.DeleteFromTempDatabase(fixed_point_info.primary,
                                        fixed_point_info.secondary);

  if (call_graph2_.IsLibrary(
          call_graph2_.GetVertex(fixed_point_info.secondary)) ||
      flow_graph_infos2_.find(fixed_point_info.secondary) ==
          flow_graph_infos2_.end() ||
      call_graph1_.IsLibrary(
          call_graph1_.GetVertex(fixed_point_info.primary)) ||
      flow_graph_infos1_.find(fixed_point_info.primary) ==
          flow_graph_infos1_.end()) {
    counts_["function matches (library)"] -= 1;
    counts_["basicBlock matches (library)"] -=
        fixed_point_info.basic_block_count;
    counts_["instruction matches (library)"] -=
        fixed_point_info.instruction_count;
    counts_["flowGraph edge matches (library)"] -= fixed_point_info.edge_count;
  } else {
    counts_["function matches (non-library)"] -= 1;
    counts_["basicBlock matches (non-library)"] -=
        fixed_point_info.basic_block_count;
    counts_["instruction matches (non-library)"] -=
        fixed_point_info.instruction_count;
    counts_["flowGraph edge matches (non-library)"] -=
        fixed_point_info.edge_count;
  }
  histogram_[*fixed_point_info.algorithm]--;

  // Remove 0 entries from histogram.
  for (auto i = histogram_.begin(), end = histogram_.end(); i != end;) {
    if (!i->second) {
      histogram_.erase(i++);
    } else {
      ++i;
    }
  }

  // TODO(soerenme) tree search, this is O(n^2) when deleting all matches
  if (!IsInComplete()) {
    for (auto i = fixed_points_.cbegin(), end = fixed_points_.cend(); i != end;
         ++i) {
      const FixedPoint& fixed_point = *i;
      if (fixed_point.GetPrimary()->GetEntryPointAddress() ==
              fixed_point_info.primary &&
          fixed_point.GetSecondary()->GetEntryPointAddress() ==
              fixed_point_info.secondary) {
        FlowGraph* primary = fixed_point.GetPrimary();
        FlowGraph* secondary = fixed_point.GetSecondary();
        fixed_points_.erase(i);
        primary->ResetMatches();
        secondary->ResetMatches();
        break;
      }
    }
  }

  CHECK(flow_graph_infos1_.find(fixed_point_info.primary) !=
        flow_graph_infos1_.end());
  CHECK(flow_graph_infos2_.find(fixed_point_info.secondary) !=
        flow_graph_infos2_.end());
  FlowGraphInfo& primary(
      flow_graph_infos1_.find(fixed_point_info.primary)->second);
  FlowGraphInfo& secondary(
      flow_graph_infos2_.find(fixed_point_info.secondary)->second);
  indexed_flow_graphs1_.push_back(&primary);
  indexed_flow_graphs2_.push_back(&secondary);

  CHECK(fixed_point_infos_.find(fixed_point_info) != fixed_point_infos_.end());
  indexed_fixed_points_.erase(indexed_fixed_points_.begin() + index);

  fixed_point_infos_.erase(fixed_point_info);

  CHECK(indexed_fixed_points_.size() == fixed_point_infos_.size());
  CHECK(IsInComplete() || indexed_fixed_points_.size() == fixed_points_.size());

  SetDirty();

  return 1;
}

FlowGraph* FindGraph(FlowGraphs& graphs,  // NOLINT(runtime/references)
                     Address address) {
  // TODO(soerenme): Graphs are sorted, we don't need to search the whole thing.
  for (auto i = graphs.begin(), end = graphs.end(); i != end; ++i) {
    if ((*i)->GetEntryPointAddress() == address) {
      return *i;
    }
  }
  return 0;
}

int Results::AddMatch(Address primary, Address secondary) {
  FixedPointInfo fixed_point_info;
  fixed_point_info.algorithm = FindString("function: manual");
  fixed_point_info.confidence = 1.0;
  fixed_point_info.basic_block_count = 0;
  fixed_point_info.edge_count = 0;
  fixed_point_info.instruction_count = 0;
  fixed_point_info.primary = primary;
  fixed_point_info.secondary = secondary;
  fixed_point_info.similarity = 0.0;
  fixed_point_info.flags = 0;
  fixed_point_info.comments_ported = false;
  // Results have been loaded: we need to reload flow graphs and recreate
  // basic block fixed points.
  if (IsInComplete()) {
    FlowGraph primary_graph;
    FlowGraph secondary_graph;
    FixedPoint fixed_point;
    SetupTemporaryFlowGraphs(fixed_point_info, primary_graph, secondary_graph,
                             fixed_point, true);

    Counts counts;
    Histogram histogram;
    FlowGraphs dummy1;
    dummy1.insert(&primary_graph);
    FlowGraphs dummy2;
    dummy2.insert(&secondary_graph);
    FixedPoints dummy3;
    dummy3.insert(fixed_point);
    GetCountsAndHistogram(dummy1, dummy2, dummy3, &histogram, &counts);

    fixed_point.SetMatchingStep("function: manual");
    fixed_point.SetSimilarity(
        GetSimilarityScore(primary_graph, secondary_graph, histogram, counts));
    ClassifyChanges(&fixed_point);
    fixed_point_info.basic_block_count =
        counts["basicBlock matches (library)"] +
        counts["basicBlock matches (non-library)"];
    fixed_point_info.instruction_count =
        counts["instruction matches (library)"] +
        counts["instruction matches (non-library)"];
    fixed_point_info.edge_count =
        counts["flowGraph edge matches (library)"] +
        counts["flowGraph edge matches (non-library)"];
    fixed_point_info.similarity = fixed_point.GetSimilarity();
    fixed_point_info.flags = fixed_point.GetFlags();

    temp_database_.WriteToTempDatabase(fixed_point);

    DeleteTemporaryFlowGraphs();
  } else {
    FlowGraph* primary_graph = FindGraph(flow_graphs1_, primary);
    FlowGraph* secondary_graph = FindGraph(flow_graphs2_, secondary);
    if (!primary_graph || primary_graph->GetEntryPointAddress() != primary ||
        !secondary_graph ||
        secondary_graph->GetEntryPointAddress() != secondary) {
      LOG(INFO) << "invalid graphs in addmatch";
      return 0;
    }
    FixedPoint& fixed_point(const_cast<FixedPoint&>(
        *fixed_points_.insert(FixedPoint(primary_graph, secondary_graph,
                                         "function: manual"))
             .first));
    MatchingContext context(call_graph1_, call_graph2_, flow_graphs1_,
                            flow_graphs2_, fixed_points_);
    primary_graph->SetFixedPoint(&fixed_point);
    secondary_graph->SetFixedPoint(&fixed_point);
    FindFixedPointsBasicBlock(&fixed_point, &context,
                              GetDefaultMatchingStepsBasicBlock());

    Counts counts;
    Histogram histogram;
    FlowGraphs dummy1;
    dummy1.insert(primary_graph);
    FlowGraphs dummy2;
    dummy2.insert(secondary_graph);
    FixedPoints dummy3;
    dummy3.insert(fixed_point);
    GetCountsAndHistogram(dummy1, dummy2, dummy3, &histogram, &counts);

    fixed_point.SetSimilarity(GetSimilarityScore(
        *primary_graph, *secondary_graph, histogram, counts));
    fixed_point.SetConfidence(fixed_point_info.confidence);
    ClassifyChanges(&fixed_point);
    fixed_point_info.basic_block_count =
        counts["basicBlock matches (library)"] +
        counts["basicBlock matches (non-library)"];
    fixed_point_info.instruction_count =
        counts["instruction matches (library)"] +
        counts["instruction matches (non-library)"];
    fixed_point_info.edge_count =
        counts["flowGraph edge matches (library)"] +
        counts["flowGraph edge matches (non-library)"];
    fixed_point_info.similarity = fixed_point.GetSimilarity();
    fixed_point_info.flags = fixed_point.GetFlags();
  }

  fixed_point_infos_.insert(fixed_point_info);
  indexed_fixed_points_.push_back(
      const_cast<FixedPointInfo*>(&*fixed_point_infos_.find(fixed_point_info)));
  std::sort(indexed_fixed_points_.begin(), indexed_fixed_points_.end(),
            &SortBySimilarity);

  if (call_graph2_.IsLibrary(
          call_graph2_.GetVertex(fixed_point_info.secondary)) ||
      flow_graph_infos2_.find(fixed_point_info.secondary) ==
          flow_graph_infos2_.end() ||
      call_graph1_.IsLibrary(
          call_graph1_.GetVertex(fixed_point_info.primary)) ||
      flow_graph_infos1_.find(fixed_point_info.primary) ==
          flow_graph_infos1_.end()) {
    counts_["function matches (library)"] += 1;
    counts_["basicBlock matches (library)"] +=
        fixed_point_info.basic_block_count;
    counts_["instruction matches (library)"] +=
        fixed_point_info.instruction_count;
    counts_["flowGraph edge matches (library)"] += fixed_point_info.edge_count;
  } else {
    counts_["function matches (non-library)"] += 1;
    counts_["basicBlock matches (non-library)"] +=
        fixed_point_info.basic_block_count;
    counts_["instruction matches (non-library)"] +=
        fixed_point_info.instruction_count;
    counts_["flowGraph edge matches (non-library)"] +=
        fixed_point_info.edge_count;
  }
  histogram_[*fixed_point_info.algorithm]++;

  FlowGraphInfo& primary_info(
      flow_graph_infos1_.find(fixed_point_info.primary)->second);
  FlowGraphInfo& secondary_info(
      flow_graph_infos2_.find(fixed_point_info.secondary)->second);
  indexed_flow_graphs1_.erase(std::find(indexed_flow_graphs1_.begin(),
                                        indexed_flow_graphs1_.end(),
                                        &primary_info));
  indexed_flow_graphs2_.erase(std::find(indexed_flow_graphs2_.begin(),
                                        indexed_flow_graphs2_.end(),
                                        &secondary_info));

  refresh_chooser("Matched Functions");
  refresh_chooser("Primary Unmatched");
  refresh_chooser("Secondary Unmatched");
  refresh_chooser("Statistics");
  SetDirty();

  return 1;
}

int Results::AddMatchPrimary(size_t index) {
  static const int widths[] = {10, 30, 5, 6, 5};
  static const char* popups[] = {0, 0, 0, 0};
  const int index2 = choose2(
      CH_MODAL, -1, -1, -1, -1, reinterpret_cast<void*>(this),
      sizeof(widths) / sizeof(widths[0]), widths, &::GetNumUnmatchedSecondary,
      &::GetUnmatchedSecondaryDescription, "Secondary Unmatched", -1 /* Icon */,
      1 /* Default */, 0 /* Delete callback */, 0 /* New callback */,
      0 /* Update callback */, 0 /* Edit callback */, 0 /* Enter callback */,
      0 /* Destroy callback */,
      popups /* Popups (insert, delete, edit, refresh) */, 0);
  if (index2 > 0) {
    const Address primary = GetPrimaryAddress(index);
    const Address secondary = GetSecondaryAddress(index2);
    return AddMatch(primary, secondary);
  }
  return 0;
}

int Results::AddMatchSecondary(size_t index) {
  static const int widths[] = {10, 30, 5, 6, 5};
  static const char* popups[] = {0, 0, 0, 0};
  const int index2 = choose2(
      CH_MODAL, -1, -1, -1, -1, reinterpret_cast<void*>(this),
      sizeof(widths) / sizeof(widths[0]), widths, &::GetNumUnmatchedPrimary,
      &::GetUnmatchedPrimaryDescription, "Primary Unmatched", -1,  // icon
      1,                                                           // default
      0,       // delete callback
      0,       // new callback
      0,       // update callback
      0,       // edit callback
      0,       // enter callback
      0,       // destroy callback
      popups,  // popups (insert, delete, edit, refresh)
      0);
  if (index2 > 0) {
    const Address secondary = GetSecondaryAddress(index);
    const Address primary = GetPrimaryAddress(index2);
    return AddMatch(primary, secondary);
  }
  return 0;
}

void Results::GetMatchDescription(size_t index, char* const* line) const {
  // the target buffers are promised to be MAXSTR == 1024 characters long...
  if (!index) {
    snprintf(line[0], MAXSTR, "similarity");
    snprintf(line[1], MAXSTR, "confidence");
    snprintf(line[2], MAXSTR, "change");
    snprintf(line[3], MAXSTR, "EA primary");
    snprintf(line[4], MAXSTR, "name primary");
    snprintf(line[5], MAXSTR, "EA secondary");
    snprintf(line[6], MAXSTR, "name secondary");
    snprintf(line[7], MAXSTR, "comments ported");
    snprintf(line[8], MAXSTR, "algorithm");
    snprintf(line[9], MAXSTR, "matched basicblocks");
    snprintf(line[10], MAXSTR, "basicblocks primary");
    snprintf(line[11], MAXSTR, "basicblocks secondary");
    snprintf(line[12], MAXSTR, "matched instructions");
    snprintf(line[13], MAXSTR, "instructions primary");
    snprintf(line[14], MAXSTR, "instructions secondary");
    snprintf(line[15], MAXSTR, "matched edges");
    snprintf(line[16], MAXSTR, "edges primary");
    snprintf(line[17], MAXSTR, "edges secondary");
    return;
  }

  if (index > indexed_fixed_points_.size()) {
    return;
  }

  const FixedPointInfo& fixed_point(*indexed_fixed_points_[index - 1]);
  UpdateName(const_cast<CallGraph*>(&call_graph1_), fixed_point.primary);

  FlowGraphInfo empty;
  memset(&empty, 0, sizeof(empty));
  const FlowGraphInfo& primary(
      flow_graph_infos1_.find(fixed_point.primary) != flow_graph_infos1_.end()
          ? flow_graph_infos1_.find(fixed_point.primary)->second
          : empty);
  const FlowGraphInfo& secondary(
      flow_graph_infos2_.find(fixed_point.secondary) != flow_graph_infos2_.end()
          ? flow_graph_infos2_.find(fixed_point.secondary)->second
          : empty);
  snprintf(line[0], MAXSTR, "%.2f", fixed_point.similarity);
  snprintf(line[1], MAXSTR, "%.2f", fixed_point.confidence);
  snprintf(line[2], MAXSTR, "%s",
           GetChangeDescription(ChangeType(fixed_point.flags)).c_str());
  snprintf(line[3], MAXSTR, HEX_ADDRESS,
           static_cast<ea_t>(fixed_point.primary));
  snprintf(line[4], MAXSTR, "%s",
           call_graph1_.GetGoodName(call_graph1_.GetVertex(fixed_point.primary))
               .c_str());
  snprintf(line[5], MAXSTR, HEX_ADDRESS,
           static_cast<ea_t>(fixed_point.secondary));
  snprintf(
      line[6], MAXSTR, "%s",
      call_graph2_.GetGoodName(call_graph2_.GetVertex(fixed_point.secondary))
          .c_str());
  snprintf(line[7], MAXSTR, "%s", fixed_point.comments_ported ? "X" : " ");
  snprintf(
      line[8], MAXSTR, "%s",
      fixed_point.algorithm->substr(fixed_point.algorithm->size() > 10 ? 10 : 0)
          .c_str());
  snprintf(line[9], MAXSTR, "%5d", fixed_point.basic_block_count);
  snprintf(line[10], MAXSTR, "%5d", primary.basic_block_count);
  snprintf(line[11], MAXSTR, "%5d", secondary.basic_block_count);
  snprintf(line[12], MAXSTR, "%6d", fixed_point.instruction_count);
  snprintf(line[13], MAXSTR, "%6d", primary.instruction_count);
  snprintf(line[14], MAXSTR, "%6d", secondary.instruction_count);
  snprintf(line[15], MAXSTR, "%5d", fixed_point.edge_count);
  snprintf(line[16], MAXSTR, "%5d", primary.edge_count);
  snprintf(line[17], MAXSTR, "%5d", secondary.edge_count);
}

void Results::ReadBasicblockMatches(FixedPoint* fixed_point) {
  // we need to check the temporary database first to get up to date data
  // (the user may have added fixedpoints manually)
  // only if we cannot find the fixedpoint there we load from the original db
  int id = 0;
  temp_database_.GetDatabase()
      ->Statement(
          "select coalesce(id, 0) from function where function.address1 = "
          ":address1 and function.address2 = :address2")
      ->BindInt64(fixed_point->GetPrimary()->GetEntryPointAddress())
      .BindInt64(fixed_point->GetSecondary()->GetEntryPointAddress())
      .Execute()
      .Into(&id);
  std::shared_ptr<SqliteDatabase> database;
  if (id) {  // found in temp db
    database.reset(temp_database_.GetDatabase(), [](SqliteDatabase*) {});
  } else {  // load original
    database.reset(new SqliteDatabase(input_filename_.c_str()));
  }

  std::map<int, std::string> algorithms;
  {
    SqliteStatement statement(database.get(),
                              "select id, name from basicblockalgorithm");
    for (statement.Execute(); statement.GotData(); statement.Execute()) {
      int id;
      std::string name;
      statement.Into(&id).Into(&name);
      algorithms[id] = name;
    }
  }

  SqliteStatement statement(
      database.get(),
      "select basicblock.address1, basicblock.address2, basicblock.algorithm "
      "from function "
      "inner join basicblock on functionid = function.id "
      "inner join instruction on basicblockid = basicblock.id "
      "where function.address1 = :address1 and function.address2 = :address2 "
      "order by basicblock.id");
  statement.BindInt64(fixed_point->GetPrimary()->GetEntryPointAddress());
  statement.BindInt64(fixed_point->GetSecondary()->GetEntryPointAddress());

  std::pair<Address, Address> last_basicblock(
      std::make_pair(std::numeric_limits<Address>::max(),
                     std::numeric_limits<Address>::max()));
  int last_algorithm = -1;
  for (statement.Execute(); statement.GotData(); statement.Execute()) {
    std::pair<Address, Address> basicblock;
    int algorithm;
    statement.Into(&basicblock.first).Into(&basicblock.second).Into(&algorithm);
    if (last_algorithm < 0) {
      last_algorithm = algorithm;
      last_basicblock = basicblock;
    }
    if (basicblock != last_basicblock) {
      fixed_point->Add(last_basicblock.first, last_basicblock.second,
                       algorithms[last_algorithm]);
      last_basicblock = basicblock;
      last_algorithm = algorithm;
    }
  }
  if (last_algorithm != -1) {
    fixed_point->Add(last_basicblock.first, last_basicblock.second,
                     algorithms[last_algorithm]);
  }
}

void Results::SetupTemporaryFlowGraphs(const FixedPointInfo& fixed_point_info,
                                       FlowGraph& primary, FlowGraph& secondary,
                                       FixedPoint& fixed_point,
                                       bool create_instruction_matches) {
  instruction_cache_.Clear();
  try {
    ReadTemporaryFlowGraph(fixed_point_info, flow_graph_infos1_, &call_graph1_,
                           &primary, &instruction_cache_);
  } catch (...) {
    throw std::runtime_error(
        StrCat("error reading: ", call_graph1_.GetFilePath()));
  }
  try {
    ReadTemporaryFlowGraph(fixed_point_info, flow_graph_infos2_, &call_graph2_,
                           &secondary, &instruction_cache_);
  } catch (...) {
    throw std::runtime_error(
        StrCat("error reading: ", call_graph2_.GetFilePath()));
  }
  fixed_point.Create(&primary, &secondary);
  MatchingContext context(call_graph1_, call_graph2_, flow_graphs1_,
                          flow_graphs2_, fixed_points_);
  flow_graphs1_.clear();
  flow_graphs1_.insert(&primary);
  flow_graphs2_.clear();
  flow_graphs2_.insert(&secondary);
  fixed_points_.clear();
  fixed_point.SetConfidence(fixed_point_info.confidence);
  fixed_point.SetSimilarity(fixed_point_info.similarity);
  fixed_point.SetFlags(fixed_point_info.flags);
  fixed_point.SetMatchingStep(*fixed_point_info.algorithm);
  std::pair<FixedPoints::iterator, bool> fixed_point_it =
      fixed_points_.insert(fixed_point);
  primary.SetFixedPoint(const_cast<FixedPoint*>(&*fixed_point_it.first));
  secondary.SetFixedPoint(const_cast<FixedPoint*>(&*fixed_point_it.first));
  call_graph1_.AttachFlowGraph(&primary);
  call_graph2_.AttachFlowGraph(&secondary);
  if (create_instruction_matches) {
    FindFixedPointsBasicBlock(&fixed_point, &context,
                              GetDefaultMatchingStepsBasicBlock());
  } else {
    ReadBasicblockMatches(&fixed_point);
  }
}

void Results::DeleteTemporaryFlowGraphs() {
  flow_graphs1_.clear();
  flow_graphs2_.clear();
  fixed_points_.clear();
}

bool Results::PrepareVisualCallGraphDiff(size_t index, std::string* message) {
  if (!index || index > indexed_fixed_points_.size()) {
    return false;
  }

  const FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);
  diff_database_id_++;
  std::string name(StrCat("visual_diff", diff_database_id_, ".database"));
  std::string database_file;
  // TODO(soerenme): Bug: if matches have been manually modified in the meantime
  //                 we are hosed!
  if (IsInComplete()) {
    database_file = input_filename_;
  } else {
    // TODO(soerenme): This is insanely inefficient: every single call graph
    //                 diff recreates the full result.
    DatabaseWriter writer(name, true);
    writer.Write(call_graph1_, call_graph2_, flow_graphs1_, flow_graphs2_,
                 fixed_points_);
    database_file = writer.GetFilename();
  }

  *message = StringPrintf(
      "<BinDiffMatch>\n"
      "\t<Type value=\"callgraph\" />\n"
      "\t<Database path=\"%s\" />\n"
      "\t<Match primary=\"%zu\" secondary=\"%zu\" />\n"
      "\t<Primary path=\"%s\" />\n"
      "\t<Secondary path=\"%s\" />\n"
      "</BinDiffMatch>\n",
      database_file.c_str(), fixed_point_info.primary,
      fixed_point_info.secondary, call_graph1_.GetFilename().c_str(),
      call_graph2_.GetFilename().c_str());
  return true;
}

bool Results::PrepareVisualDiff(size_t index, std::string* message) {
  if (!index || index > indexed_fixed_points_.size()) {
    return false;
  }

  const FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);

  FlowGraphInfo empty;
  memset(&empty, 0, sizeof(empty));
  const FlowGraphInfo& primary_info(
      flow_graph_infos1_.find(fixed_point_info.primary) !=
              flow_graph_infos1_.end()
          ? flow_graph_infos1_.find(fixed_point_info.primary)->second
          : empty);
  const FlowGraphInfo& secondary_info(
      flow_graph_infos2_.find(fixed_point_info.secondary) !=
              flow_graph_infos2_.end()
          ? flow_graph_infos2_.find(fixed_point_info.secondary)->second
          : empty);
  if (primary_info.instruction_count == 0 &&
      secondary_info.instruction_count == 0) {
    warning("Both functions are empty, nothing to display!");
    return false;
  }

  FixedPoint fixed_point;
  FlowGraphs flow_graphs1;
  FlowGraphs flow_graphs2;
  FixedPoints fixed_points;
  FlowGraph primary;
  FlowGraph secondary;
  if (IsInComplete()) {
    LOG(INFO) << "Loading incomplete flow graphs";
    // Results have been loaded: we need to reload flow graphs and recreate
    // basic block fixed_points.
    SetupTemporaryFlowGraphs(fixed_point_info, primary, secondary, fixed_point,
                             false);
  } else {
    fixed_point = *FindFixedPoint(fixed_point_info);
  }
  flow_graphs1.insert(fixed_point.GetPrimary());
  flow_graphs2.insert(fixed_point.GetSecondary());
  fixed_points.insert(fixed_point);

  diff_database_id_++;
  std::string name(StrCat("visual_diff", diff_database_id_, ".database"));
  DatabaseWriter writer(name, true);
  writer.Write(call_graph1_, call_graph2_, flow_graphs1, flow_graphs2,
               fixed_points);
  const std::string database_file = writer.GetFilename();

  *message = StringPrintf(
      "<BinDiffMatch>\n"
      "\t<Type value=\"flowgraph\" />\n"
      "\t<Database path=\"%s\" />\n"
      "\t<Primary path=\"%s\" address=\"%zu\" />\n"
      "\t<Secondary path=\"%s\" address=\"%zu\" />\n"
      "</BinDiffMatch>\n",
      database_file.c_str(), call_graph1_.GetFilename().c_str(),
      fixed_point.GetPrimary()->GetEntryPointAddress(),
      call_graph2_.GetFilename().c_str(),
      fixed_point.GetSecondary()->GetEntryPointAddress());

  if (IsInComplete()) {
    DeleteTemporaryFlowGraphs();
  }
  return true;
}

FixedPoint* Results::FindFixedPoint(const FixedPointInfo& fixed_point_info) {
  // TODO(soerenme): Use tree search.
  for (const auto& fixed_point : fixed_points_) {
    CHECK(fixed_point.GetPrimary() && fixed_point.GetSecondary());
    if (fixed_point.GetPrimary()->GetEntryPointAddress() ==
            fixed_point_info.primary &&
        fixed_point.GetSecondary()->GetEntryPointAddress() ==
            fixed_point_info.secondary) {
      return const_cast<FixedPoint*>(&fixed_point);
    }
  }
  return 0;
}

void Results::Read(Reader* reader) {
  flow_graph_infos1_.clear();
  flow_graph_infos2_.clear();
  fixed_point_infos_.clear();
  indexed_flow_graphs1_.clear();
  indexed_flow_graphs2_.clear();
  indexed_fixed_points_.clear();

  incomplete_results_ = true;
  reader->Read(call_graph1_, call_graph2_, flow_graph_infos1_,
               flow_graph_infos2_, fixed_point_infos_);
  if (const DatabaseReader* databaseReader =
          dynamic_cast<DatabaseReader*>(reader)) {
    input_filename_ = databaseReader->GetInputFilename();
    histogram_ = databaseReader->GetBasicBlockFixedPointInfo();
  } else {
    CHECK(false && "unsupported reader");
  }

  InitializeIndexedVectors();
  Count();
  similarity_ = reader->GetSimilarity();
  confidence_ = reader->GetConfidence();
  dirty_ = false;

  // TODO(soerenme): Iterate over all fixedpoints that have been added manually
  //                 by the Java UI and evaluate them (add basic
  //                 block/instruction matches).
}

void Results::Write(Writer* writer) {
  writer->Write(call_graph1_, call_graph2_, flow_graphs1_, flow_graphs2_,
                fixed_points_);
  dirty_ = false;
}

void Results::CreateIndexedViews() {
  if (indexed_flow_graphs1_.empty() && indexed_flow_graphs2_.empty() &&
      indexed_fixed_points_.empty()) {
    // Only initialize indices the first time around.
    for (const auto& fixed_point : fixed_points_) {
      FixedPointInfo fixed_point_info;
      fixed_point_info.algorithm = FindString(fixed_point.GetMatchingStep());
      fixed_point_info.confidence = fixed_point.GetConfidence();
      fixed_point_info.similarity = fixed_point.GetSimilarity();
      fixed_point_info.flags = fixed_point.GetFlags();
      fixed_point_info.primary =
          fixed_point.GetPrimary()->GetEntryPointAddress();
      fixed_point_info.secondary =
          fixed_point.GetSecondary()->GetEntryPointAddress();
      fixed_point_info.comments_ported = fixed_point.GetCommentsPorted();
      Counts counts;
      Histogram histogram;
      ::Count(fixed_point, &counts, &histogram);
      fixed_point_info.basic_block_count =
          counts["basicBlock matches (library)"] +
          counts["basicBlock matches (non-library)"];
      fixed_point_info.instruction_count =
          counts["instruction matches (library)"] +
          counts["instruction matches (non-library)"];
      fixed_point_info.edge_count =
          counts["flowGraph edge matches (library)"] +
          counts["flowGraph edge matches (non-library)"];
      fixed_point_infos_.insert(fixed_point_info);
    }
    InitializeIndexedVectors();
    GetCountsAndHistogram(flow_graphs1_, flow_graphs2_, fixed_points_,
                          &histogram_, &counts_);
    Confidences confidences;
    confidence_ = GetConfidence(histogram_, &confidences);
    similarity_ =
        GetSimilarityScore(call_graph1_, call_graph2_, histogram_, counts_);
  }
}

void Results::MarkPortedCommentsInTempDatabase() {
  temp_database_.SetCommentsPorted(fixed_point_infos_);
}

// Transfer from temp db to real db.
void Results::MarkPortedCommentsInDatabase() {
  try {
    if (!input_filename_.empty()) {
      SqliteDatabase database(input_filename_.c_str());
      DatabaseTransmuter::MarkPortedComments(
          &database, temp_database_.GetFilename().c_str(), fixed_point_infos_);
    }
  } catch (...) {
    // we swallow any errors here. The database may be read only or
    // the commentsported table doesn't exist. We don't care...
  }
}

int Results::PortComments(Address start_address_source,
                          Address end_address_source,
                          Address start_address_target,
                          Address end_address_target, double min_confidence,
                          double min_similarity) {
  for (size_t index = 1; index <= indexed_fixed_points_.size(); ++index) {
    FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);
    if (get_func(static_cast<ea_t>(fixed_point_info.primary))) {
      if (IsInComplete()) {
        FlowGraph primary, secondary;
        FixedPoint fixed_point;
        SetupTemporaryFlowGraphs(fixed_point_info, primary, secondary,
                                 fixed_point, false);

        SetComments(&fixed_point, call_graph2_.GetComments(),
                    start_address_target, end_address_target,
                    start_address_source, end_address_source, min_confidence,
                    min_similarity);

        DeleteTemporaryFlowGraphs();
      } else {
        SetComments(FindFixedPoint(fixed_point_info),
                    call_graph2_.GetComments(), start_address_target,
                    end_address_target, start_address_source,
                    end_address_source, min_confidence, min_similarity);
      }
    }
    fixed_point_info.comments_ported = true;
  }
  MarkPortedCommentsInTempDatabase();
  return 1;  // IDA API 1 == ok
}

int Results::PortComments(int index, bool as_external) {
  if (index == START_SEL) {
    // multiselection support. we must return OK, but cannot do anything yet
    return 1;
  }
  if (index == END_SEL) {
    MarkPortedCommentsInTempDatabase();
    return 1;
  }
  if (!index || index > static_cast<int>(indexed_fixed_points_.size())) {
    return 0;
  }

  FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);
  const Address start_address_target = 0;
  const Address end_address_target = std::numeric_limits<ea_t>::max() - 1;
  const Address start_address_source = fixed_point_info.primary;
  if (func_t* function = get_func(static_cast<ea_t>(start_address_source))) {
    const ea_t end_address_source = function->endEA;
    if (as_external) {
      function->flags |= FUNC_LIB;
    }
    if (IsInComplete()) {
      FlowGraph primary, secondary;
      FixedPoint fixed_point;
      SetupTemporaryFlowGraphs(fixed_point_info, primary, secondary,
                               fixed_point, false);

      SetComments(&fixed_point, call_graph2_.GetComments(),
                  start_address_target, end_address_target,
                  start_address_source, end_address_source, 0.0, 0.0);

      DeleteTemporaryFlowGraphs();
    } else {
      SetComments(FindFixedPoint(fixed_point_info), call_graph2_.GetComments(),
                  start_address_target, end_address_target,
                  start_address_source, end_address_source, 0.0, 0.0);
    }
  }
  fixed_point_info.comments_ported = true;
  return 1;
}

int Results::ConfirmMatch(int index) {
  if (index == START_SEL || index == END_SEL) {
    // multiselection support. we must return OK, but cannot do anything yet
    return 1;
  }

  if (!index || index > static_cast<int>(indexed_fixed_points_.size())) {
    return 0;
  }

  FixedPointInfo* fixed_point_info(indexed_fixed_points_[index - 1]);
  fixed_point_info->algorithm = FindString("function: manual");
  fixed_point_info->confidence = 1.0;
  if (!IsInComplete()) {
    FixedPoint* fixed_point(FindFixedPoint(*fixed_point_info));
    fixed_point->SetMatchingStep(*fixed_point_info->algorithm);
    fixed_point->SetConfidence(fixed_point_info->confidence);
  }
  SetDirty();

  return 1;
}


int Results::CopyPrimaryAddress(int index) const {
  if (index < 1 || index > static_cast<int>(indexed_fixed_points_.size())) {
    return 0;
  }

  const FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(fixed_point_info.primary));
  CopyToClipboard(buffer);

  return 1;
}

int Results::CopySecondaryAddress(int index) const {
  if (index < 1 || index > static_cast<int>(indexed_fixed_points_.size())) {
    return 0;
  }

  const FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(fixed_point_info.secondary));
  CopyToClipboard(buffer);

  return 1;
}

int Results::CopyPrimaryAddressUnmatched(int index) const {
  if (index < 1 || index > static_cast<int>(indexed_flow_graphs1_.size())) {
    return 0;
  }

  const FlowGraphInfo& flowGraphInfo(*indexed_flow_graphs1_[index - 1]);
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(flowGraphInfo.address));
  CopyToClipboard(buffer);

  return 1;
}

int Results::CopySecondaryAddressUnmatched(int index) const {
  if (index < 1 || index > static_cast<int>(indexed_flow_graphs2_.size())) {
    return 0;
  }

  const FlowGraphInfo& flowGraphInfo(*indexed_flow_graphs2_[index - 1]);
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(flowGraphInfo.address));
  CopyToClipboard(buffer);

  return 1;
}

bool Results::IsInComplete() const { return incomplete_results_; }

void Results::GetUnmatchedDescription(const IndexedFlowGraphs& flow_graphs,
                                      size_t index, char* const* line) const {
  // The target buffers are promised to be MAXSTR == 1024 characters long...
  if (!index) {
    snprintf(line[0], MAXSTR, "EA");
    snprintf(line[1], MAXSTR, "Name");
    snprintf(line[2], MAXSTR, "Basicblocks");
    snprintf(line[3], MAXSTR, "Instructions");
    snprintf(line[4], MAXSTR, "Edges");
    return;
  }

  const FlowGraphInfo& flow_graph_info = *flow_graphs[index - 1];
  // The primary IDB is loaded in IDA and the function name might have been
  // changed manually, thus we need to propagate that information.
  if (&flow_graphs == &indexed_flow_graphs1_) {
    UpdateName(const_cast<CallGraph*>(&call_graph1_), flow_graph_info.address);
  }

  CHECK(flow_graph_info.demangled_name);
  snprintf(line[0], MAXSTR, HEX_ADDRESS,
           static_cast<ea_t>(flow_graph_info.address));
  snprintf(line[1], MAXSTR, "%s",
           flow_graph_info.demangled_name->empty()
               ? flow_graph_info.name->c_str()
               : flow_graph_info.demangled_name->c_str());
  snprintf(line[2], MAXSTR, "%5d", flow_graph_info.basic_block_count);
  snprintf(line[3], MAXSTR, "%6d", flow_graph_info.instruction_count);
  snprintf(line[4], MAXSTR, "%5d", flow_graph_info.edge_count);
}

void Results::InitializeIndexedVectors() {
  std::set<Address> matched_primaries, matched_secondaries;
  for (auto i = fixed_point_infos_.begin(), end = fixed_point_infos_.end();
       i != end; ++i) {
    matched_primaries.insert(i->primary);
    matched_secondaries.insert(i->secondary);
    indexed_fixed_points_.push_back(const_cast<FixedPointInfo*>(&*i));
  }
  std::sort(indexed_fixed_points_.begin(), indexed_fixed_points_.end(),
            &SortBySimilarity);

  for (auto i = flow_graph_infos1_.begin(), end = flow_graph_infos1_.end();
       i != end; ++i) {
    if (matched_primaries.find(i->first) == matched_primaries.end()) {
      indexed_flow_graphs1_.push_back(&i->second);
    }
  }
  for (auto i = flow_graph_infos2_.begin(), end = flow_graph_infos2_.end();
       i != end; ++i) {
    if (matched_secondaries.find(i->first) == matched_secondaries.end()) {
      indexed_flow_graphs2_.push_back(&i->second);
    }
  }

  FlowGraphInfo empty;
  memset(&empty, 0, sizeof(empty));
  {
    CallGraph::VertexIterator i, end;
    for (boost::tie(i, end) = boost::vertices(call_graph1_.GetGraph());
         i != end; ++i) {
      const Address address = call_graph1_.GetAddress(*i);
      if (flow_graph_infos1_.find(address) == flow_graph_infos1_.end()) {
        empty.address = address;
        empty.name = &call_graph1_.GetName(*i);
        empty.demangled_name = &call_graph1_.GetDemangledName(*i);
        flow_graph_infos1_[address] = empty;
        if (matched_primaries.find(address) == matched_primaries.end()) {
          indexed_flow_graphs1_.push_back(&flow_graph_infos1_[address]);
        }
      }
    }
  }
  {
    CallGraph::VertexIterator i, end;
    for (boost::tie(i, end) = boost::vertices(call_graph2_.GetGraph());
         i != end; ++i) {
      const Address address = call_graph2_.GetAddress(*i);
      if (flow_graph_infos2_.find(address) == flow_graph_infos2_.end()) {
        empty.address = address;
        empty.name = &call_graph2_.GetName(*i);
        empty.demangled_name = &call_graph2_.GetDemangledName(*i);
        flow_graph_infos2_[address] = empty;
        if (matched_secondaries.find(address) == matched_secondaries.end()) {
          indexed_flow_graphs2_.push_back(&flow_graph_infos2_[address]);
        }
      }
    }
  }
}

void Results::Count() {
  counts_.clear();
  for (auto i = flow_graph_infos1_.cbegin(), end = flow_graph_infos1_.cend();
       i != end; ++i) {
    const FlowGraphInfo& info = i->second;
    const int is_lib =
        call_graph1_.IsLibrary(call_graph1_.GetVertex(info.address)) ||
        call_graph1_.IsStub(call_graph1_.GetVertex(info.address)) ||
        info.basic_block_count == 0;
    counts_["functions primary (library)"] += is_lib;
    counts_["functions primary (non-library)"] += (1 - is_lib);
    counts_["basicBlocks primary (library)"] += is_lib * info.basic_block_count;
    counts_["basicBlocks primary (non-library)"] +=
        (1 - is_lib) * info.basic_block_count;
    counts_["instructions primary (library)"] +=
        is_lib * info.instruction_count;
    counts_["instructions primary (non-library)"] +=
        (1 - is_lib) * info.instruction_count;
    counts_["flowGraph edges primary (library)"] += is_lib * info.edge_count;
    counts_["flowGraph edges primary (non-library)"] +=
        (1 - is_lib) * info.edge_count;
  }
  {
    CallGraph::VertexIterator i, end;
    for (boost::tie(i, end) = boost::vertices(call_graph1_.GetGraph());
         i != end; ++i) {
      const Address address = call_graph1_.GetAddress(*i);
      if (flow_graph_infos1_.find(address) == flow_graph_infos1_.end()) {
        counts_["functions primary (library)"] += 1;
      }
    }
  }
  for (auto i = flow_graph_infos2_.cbegin(), end = flow_graph_infos2_.cend();
       i != end; ++i) {
    const FlowGraphInfo& info = i->second;
    const int is_lib =
        call_graph2_.IsLibrary(call_graph2_.GetVertex(info.address)) ||
        call_graph2_.IsStub(call_graph2_.GetVertex(info.address)) ||
        info.basic_block_count == 0;
    counts_["functions secondary (library)"] += is_lib;
    counts_["functions secondary (non-library)"] += (1 - is_lib);
    counts_["basicBlocks secondary (library)"] +=
        is_lib * info.basic_block_count;
    counts_["basicBlocks secondary (non-library)"] +=
        (1 - is_lib) * info.basic_block_count;
    counts_["instructions secondary (library)"] +=
        is_lib * info.instruction_count;
    counts_["instructions secondary (non-library)"] +=
        (1 - is_lib) * info.instruction_count;
    counts_["flowGraph edges secondary (library)"] += is_lib * info.edge_count;
    counts_["flowGraph edges secondary (non-library)"] +=
        (1 - is_lib) * info.edge_count;
  }
  {
    CallGraph::VertexIterator i, end;
    for (boost::tie(i, end) = boost::vertices(call_graph2_.GetGraph());
         i != end; ++i) {
      const Address address = call_graph2_.GetAddress(*i);
      if (flow_graph_infos2_.find(address) == flow_graph_infos2_.end()) {
        counts_["functions secondary (library)"] += 1;
      }
    }
  }
  counts_["function matches (library)"] = 0;
  counts_["basicBlock matches (library)"] = 0;
  counts_["instruction matches (library)"] = 0;
  counts_["flowGraph edge matches (library)"] = 0;
  counts_["function matches (non-library)"] = 0;
  counts_["basicBlock matches (non-library)"] = 0;
  counts_["instruction matches (non-library)"] = 0;
  counts_["flowGraph edge matches (non-library)"] = 0;
  for (auto i = fixed_point_infos_.cbegin(), end = fixed_point_infos_.cend();
       i != end; ++i) {
    if (call_graph2_.IsLibrary(call_graph2_.GetVertex(i->secondary)) ||
        flow_graph_infos2_.find(i->secondary) == flow_graph_infos2_.end() ||
        call_graph1_.IsLibrary(call_graph1_.GetVertex(i->primary)) ||
        flow_graph_infos1_.find(i->primary) == flow_graph_infos1_.end()) {
      counts_["function matches (library)"] += 1;
      counts_["basicBlock matches (library)"] += i->basic_block_count;
      counts_["instruction matches (library)"] += i->instruction_count;
      counts_["flowGraph edge matches (library)"] += i->edge_count;
    } else {
      counts_["function matches (non-library)"] += 1;
      counts_["basicBlock matches (non-library)"] += i->basic_block_count;
      counts_["instruction matches (non-library)"] += i->instruction_count;
      counts_["flowGraph edge matches (non-library)"] += i->edge_count;
    }
    histogram_[*i->algorithm]++;
  }
}
