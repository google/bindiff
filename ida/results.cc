#include "third_party/zynamics/bindiff/ida/results.h"

#include <fstream>

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <frame.hpp>                                            // NOLINT
#include <enum.hpp>                                             // NOLINT
#include <name.hpp>                                             // NOLINT
#include <struct.hpp>                                           // NOLINT
#include <ua.hpp>                                               // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "base/logging.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/time/time.h"
#include "third_party/zynamics/bindiff/call_graph_match.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"
#include "third_party/zynamics/bindiff/ida/matched_functions_chooser.h"
#include "third_party/zynamics/bindiff/ida/names.h"
#include "third_party/zynamics/bindiff/ida/ui.h"
#include "third_party/zynamics/bindiff/match_context.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/ida/ui.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"
#include "third_party/zynamics/binexport/util/timer.h"

namespace security {

using binexport::FormatAddress;
using binexport::HumanReadableDuration;

namespace bindiff {
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
  const string& name = GetName(address);
  const CallGraph::Vertex vertex = call_graph->GetVertex(address);
  if (!name.empty() && name != call_graph->GetName(vertex)) {
    call_graph->SetName(vertex, name);
    const string& demangled_name = GetDemangledName(address);
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
    if (function->start_ea == primary_address) {
      if (!secondary_call_graph->HasRealName(
              secondary_call_graph->GetVertex(secondary_address))) {
        return false;
      }

      const string& name = fixed_point->GetSecondary()->GetName();
      qstring buffer =
          get_name(static_cast<ea_t>(primary_address), /*flags=*/0);
      if (absl::string_view(buffer.c_str(), buffer.length()) != name) {
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
                   FixedPoint* fixed_point = nullptr) {
  int comment_count = 0;
  const OperatorId begin(std::make_pair(source, 0));
  for (auto i = comments.lower_bound(begin);
       i != comments.end() && i->first.first == source; ++i, ++comment_count) {
    CHECK(source == i->first.first);
    const Comment& comment = i->second;
    const Address address = target;
    const int operand_id = i->first.second;

    // Do not port auto-generated names (unfortunately this does not work for
    // comments that were auto-generated).
    if ((comment.type == Comment::ENUM || comment.type == Comment::LOCATION ||
         comment.type == Comment::GLOBALREFERENCE ||
         comment.type == Comment::LOCALREFERENCE) &&
        !is_uname(comment.comment.c_str())) {
      continue;
    }

    // TODO(b/63693724): comment.type will only ever be REGULAR, due to changed
    //                   behavior in BinExport2.
    switch (comment.type) {
      case Comment::REGULAR:
        set_cmt(static_cast<ea_t>(address), comment.comment.c_str(),
                comment.repeatable);
        break;
      case Comment::ENUM: {
        uint8_t serial;
        if (is_enum0(get_full_flags(static_cast<ea_t>(address))) &&
            operand_id == 0) {
          const auto id =
              get_enum_id(&serial, static_cast<ea_t>(address), operand_id);
          if (id != BADNODE) {
            set_enum_name(id, comment.comment.c_str());
          }
        }
        if (is_enum1(get_full_flags(static_cast<ea_t>(address))) &&
            operand_id == 1) {
          const auto id =
              get_enum_id(&serial, static_cast<ea_t>(address), operand_id);
          if (id != BADNODE) {
            set_enum_name(id, comment.comment.c_str());
          }
        }
        break;
      }
      case Comment::FUNCTION: {
        func_t* function = get_func(static_cast<ea_t>(address));
        if (function && function->start_ea == address) {
          set_func_cmt(function, comment.comment.c_str(), comment.repeatable);
        }
        break;
      }
      case Comment::LOCATION:
        if (fixed_point) {
          PortFunctionName(fixed_point);
        }
        break;
      case Comment::ANTERIOR: {
        const string existing_comment = GetLineComments(address, -1);
        if (existing_comment.rfind(comment.comment) == string::npos) {
          add_extra_line(static_cast<ea_t>(address), true, "%s",
                         comment.comment.c_str());
        }
        break;
      }
      case Comment::POSTERIOR: {
        const string existing_comment = GetLineComments(address, +1);
        if (existing_comment.rfind(comment.comment) == string::npos) {
          add_extra_line(static_cast<ea_t>(address), false, "%s",
                         comment.comment.c_str());
        }
        break;
      }
      case Comment::GLOBALREFERENCE: {
        int count = 0;
        xrefblk_t xb;
        for (bool ok = xb.first_from(static_cast<ea_t>(address), XREF_DATA); ok;
             ok = xb.next_from(), ++count) {
          if (count == operand_id - UA_MAXOP - 1024) {
            qstring current_name = get_name(xb.to, /*flags=*/0);
            if (absl::string_view(current_name.c_str(),
                                  current_name.length()) == comment.comment) {
              set_name(xb.to, comment.comment.c_str(), SN_NOWARN | SN_CHECK);
            }
            break;
          }
        }
        break;
      }
      case Comment::LOCALREFERENCE: {
        func_t* function = get_func(static_cast<ea_t>(address));
        if (!function) {
          break;
        }
        struc_t* frame = get_frame(function);
        if (!frame) {
          break;
        }
        insn_t instruction;
        if (decode_insn(&instruction, address) <= 0) {
          break;
        }
        for (int operand_num = 0; operand_num < UA_MAXOP; ++operand_num) {
          const ea_t offset =
              calc_stkvar_struc_offset(function, instruction, operand_num);
          if (offset == BADADDR) {
            continue;
          }

          if (operand_num == operand_id - UA_MAXOP - 2048) {
            set_member_name(frame, offset, comment.comment.c_str());
          }
        }
        break;
      }
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
        break;
      }
      default:
        LOG(INFO) << absl::StrCat(
            "Unknown comment type ", comment.type, ": ", FormatAddress(source),
            " -> ", FormatAddress(target), " ", i->second.comment);
        break;
    }
  }
  return comment_count;
}

size_t SetComments(FixedPoint* fixed_point, const Comments& comments,
                   Address start_source, Address end_source,
                   Address start_target, Address end_target,
                   double min_confidence, double min_similarity) {
  // Comments are always ported from secondary to primary:
  const FlowGraph* source_flow_graph = fixed_point->GetSecondary();
  const FlowGraph* target_flow_graph = fixed_point->GetPrimary();

  // SetComments is called three times below which potentially sets a single
  // comment multiple times. This is necessary however, because iterating over
  // fixedpoints might miss comments otherwise. For instance, there may be a
  // function fixed point but no corresponding instruction fixed point for the
  // function's entry point address.
  size_t counts = 0;
  Address source = source_flow_graph->GetEntryPointAddress();
  Address target = target_flow_graph->GetEntryPointAddress();
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

  FlowGraph::Vertex source_vertex = -1;  // Invalid vertex
  FlowGraph::Vertex target_vertex = -1;  // Invalid vertex
  for (const auto& basic_block : fixed_point->GetBasicBlockFixedPoints()) {
    if (source_vertex != basic_block.GetSecondaryVertex() ||
        target_vertex != basic_block.GetPrimaryVertex()) {
      source_vertex = basic_block.GetSecondaryVertex();
      target_vertex = basic_block.GetPrimaryVertex();
      source = source_flow_graph->GetAddress(source_vertex);
      target = target_flow_graph->GetAddress(target_vertex);
      if (source >= start_source && source <= end_source &&
          target >= start_target && target <= end_target) {
        counts +=
            SetComments(source, target, comments, /*fixed_point=*/nullptr);
      }
    }

    for (const auto& instruction_match : basic_block.GetInstructionMatches()) {
      source = instruction_match.second->GetAddress();
      target = instruction_match.first->GetAddress();
      if (source >= start_source && source <= end_source &&
          target >= start_target && target <= end_target) {
        counts +=
            SetComments(source, target, comments, /*fixed_point=*/nullptr);
      }
    }
  }
  return counts;
}

string VisualDiffMessage(bool call_graph_match,
                              const string& database,
                              const string& primary_path,
                              Address primary_address,
                              const string& secondary_path,
                              Address secondary_address) {
  return absl::StrCat("<BinDiffMatch type=\"",
                      call_graph_match ? "call_graph" : "flow_graph", "\">",
                      "<Database path =\"", database, "\"/><Primary path=\"",
                      primary_path, "\" address=\"", primary_address,
                      "\"/><Secondary path=\"", secondary_path, "\" address=\"",
                      secondary_address, "\"/></BinDiffMatch>");
}

}  // namespace

Results::Results()
    : temp_database_{"temporary.database", true},
      incomplete_results_{false},  // Set when we have loaded from disk.
      similarity_{0.0},
      confidence_{0.0},
      dirty_{false},
      should_reset_selection_{false},
      diff_database_id_(0) {}

Results::~Results() {
  // Need to close this explicitly here as otherwise the DeleteTemporaryFiles()
  // call below will fail (on Windows) due to locked db file.
  temp_database_.Close();
  DeleteFlowGraphs(&flow_graphs1_);
  DeleteFlowGraphs(&flow_graphs2_);
  DatabaseTransmuter::DeleteTempFile();
  DeleteTemporaryFiles();
}

void Results::SetDirty() { dirty_ = true; }

bool Results::IsDirty() const { return dirty_; }

void Results::DeleteTemporaryFiles() {
  // Extremely dangerous, make very sure GetDirectory _never_ returns something
  // like "C:".
  try {
    auto temp_dir_or = GetTempDirectory("BinDiff");
    if (!temp_dir_or.ok()) {
      return;
    }
    RemoveAll(temp_dir_or.ValueOrDie());
  } catch (...) {  // We don't care if it failed-only litters the temp dir a bit
  }
}

size_t Results::GetNumUnmatchedPrimary() const {
  return indexed_flow_graphs1_.size();
}

Results::UnmatchedDescription Results::GetUnmatchedDescriptionPrimary(
    size_t index) const {
  return GetUnmatchedDescription(indexed_flow_graphs1_, index);
}

size_t Results::GetNumUnmatchedSecondary() const {
  return indexed_flow_graphs2_.size();
}

Results::UnmatchedDescription Results::GetUnmatchedDescriptionSecondary(
    size_t index) const {
  return GetUnmatchedDescription(indexed_flow_graphs2_, index);
}

Results::UnmatchedDescription Results::GetUnmatchedDescription(
    const IndexedFlowGraphs& flow_graphs, size_t index) const {
  if (index >= flow_graphs.size()) {
    return {};
  }

  const FlowGraphInfo& flow_graph_info = *flow_graphs[index];
  // The primary IDB is loaded in IDA and the function name might have been
  // changed manually, thus we need to propagate that information.
  if (&flow_graphs == &indexed_flow_graphs1_) {
    UpdateName(const_cast<CallGraph*>(&call_graph1_), flow_graph_info.address);
  }

  CHECK(flow_graph_info.demangled_name);

  Results::UnmatchedDescription desc{};
  desc.address = flow_graph_info.address;
  desc.name = flow_graph_info.demangled_name->empty()
                  ? *flow_graph_info.name
                  : *flow_graph_info.demangled_name;
  desc.basic_block_count = flow_graph_info.basic_block_count;
  desc.instruction_count = flow_graph_info.instruction_count;
  desc.edge_count = flow_graph_info.edge_count;
  return desc;
}

Address Results::GetPrimaryAddress(size_t index) const {
  if (index >= indexed_flow_graphs1_.size()) {
    return 0;
  }
  return indexed_flow_graphs1_[index]->address;
}

Address Results::GetSecondaryAddress(size_t index) const {
  if (index >= indexed_flow_graphs2_.size()) {
    return 0;
  }
  return indexed_flow_graphs2_[index]->address;
}

Address Results::GetMatchPrimaryAddress(size_t index) const {
  if (index >= indexed_fixed_points_.size()) {
    return 0;
  }
  return indexed_fixed_points_[index]->primary;
}

Address Results::GetMatchSecondaryAddress(size_t index) const {
  if (index >= indexed_fixed_points_.size()) {
    return 0;
  }
  return indexed_fixed_points_[index]->secondary;
}

bool Results::IncrementalDiff() {
  WaitBox wait_box("Performing incremental diff...");

  if (IsIncomplete()) {
    auto temp_dir_or = GetOrCreateTempDirectory("BinDiff");
    if (!temp_dir_or.ok()) {
      return false;
    }
    const string temp_dir = std::move(temp_dir_or).ValueOrDie();
    {
      ::security::bindiff::Read(call_graph1_.GetFilePath(), &call_graph1_,
                                &flow_graphs1_, &flow_graph_infos1_,
                                &instruction_cache_);
      ::security::bindiff::Read(call_graph2_.GetFilePath(), &call_graph2_,
                                &flow_graphs2_, &flow_graph_infos2_,
                                &instruction_cache_);

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
    if (fixedpoint.GetMatchingStep() ==
        absl::string_view{MatchingStep::kFunctionManualName}) {
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
    if (fixed_point.GetMatchingStep() ==
        absl::string_view{MatchingStep::kFunctionManualName}) {
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

  LOG(INFO) << absl::StrCat(HumanReadableDuration(timer.elapsed()),
                            " for incremental matching.");

  SetDirty();
  return true;
}

size_t Results::GetNumStatistics() const {
  return counts_.size() + histogram_.size() + 2 /* Similarity & Confidence */;
}

Results::StatisticDescription Results::GetStatisticDescription(
    size_t index) const {
  Results::StatisticDescription desc{};
  if (index > GetNumStatistics()) {
    return desc;
  }

  size_t nr = 0;
  if (index < counts_.size()) {
    auto it = counts_.cbegin();
    for (; it != counts_.cend() && nr < index; ++it, ++nr) {
    }
    desc.name = it->first;
    desc.is_count = true;
    desc.count = it->second;
  } else if (index < histogram_.size() + counts_.size()) {
    index -= counts_.size();
    auto it = histogram_.cbegin();
    for (; it != histogram_.cend() && nr < index; ++it, ++nr) {
    }
    desc.name = it->first;
    desc.is_count = true;
    desc.count = it->second;
  } else if (index == histogram_.size() + counts_.size() + 1) {
    desc.name = "Similarity";
    desc.is_count = false;
    desc.value = similarity_;
  } else {
    desc.name = "Confidence";
    desc.is_count = false;
    desc.value = confidence_;
  }
  return desc;
}

util::Status Results::DeleteMatches(absl::Span<const size_t> indices) {
  if (indices.empty()) {
    return util::OkStatus();
  }
  auto num_indexed_fixed_points = indexed_fixed_points_.size();
  for (const auto& index : indices) {
    if (index >= num_indexed_fixed_points) {
      return util::Status{absl::StatusCode::kInvalidArgument,
                          absl::StrCat("Index out of range: ", index)};
    }

    // This is quite involved, especially if results were loaded and not
    // calculated:
    // - Recalculate statistics
    // - Remove fixed point information
    // - Remove matching flow graph pointer from both graphs if loaded
    // - Remove fixed point if loaded
    //   [ - Recalculate similarity and confidence ]
    // - Update all views
    // - Be prepared to save .BinDiff result file (again, tricky if it wasn't
    //   loaded fully)

    const FixedPointInfo& fixed_point_info = *indexed_fixed_points_[index];
    const auto primary_address = fixed_point_info.primary;
    const auto secondary_address = fixed_point_info.secondary;

    auto flow_graph_info_entry1 = flow_graph_infos1_.find(primary_address);
    DCHECK(flow_graph_info_entry1 != flow_graph_infos1_.end());
    auto flow_graph_info_entry2 = flow_graph_infos2_.find(secondary_address);
    DCHECK(flow_graph_info_entry2 != flow_graph_infos2_.end());

    temp_database_.DeleteFromTempDatabase(primary_address, secondary_address);

    if (call_graph2_.IsLibrary(call_graph2_.GetVertex(secondary_address)) ||
        call_graph1_.IsLibrary(call_graph1_.GetVertex(primary_address))) {
      counts_["function matches (library)"] -= 1;
      counts_["basicBlock matches (library)"] -=
          fixed_point_info.basic_block_count;
      counts_["instruction matches (library)"] -=
          fixed_point_info.instruction_count;
      counts_["flowGraph edge matches (library)"] -=
          fixed_point_info.edge_count;
    } else {
      counts_["function matches (non-library)"] -= 1;
      counts_["basicBlock matches (non-library)"] -=
          fixed_point_info.basic_block_count;
      counts_["instruction matches (non-library)"] -=
          fixed_point_info.instruction_count;
      counts_["flowGraph edge matches (non-library)"] -=
          fixed_point_info.edge_count;
    }
    auto& algorithm_count = histogram_[*fixed_point_info.algorithm];
    if (algorithm_count > 0) {
      --algorithm_count;
    }

    // TODO(cblichmann): Tree search, this is O(n^2) when deleting all matches.
    if (!IsIncomplete()) {
      for (auto it = fixed_points_.begin(), end = fixed_points_.end();
           it != end; ++it) {
        auto* primary_flow_graph = it->GetPrimary();
        auto* secondary_flow_graph = it->GetSecondary();
        if (primary_flow_graph->GetEntryPointAddress() == primary_address &&
            secondary_flow_graph->GetEntryPointAddress() == secondary_address) {
          fixed_points_.erase(it);
          primary_flow_graph->ResetMatches();
          secondary_flow_graph->ResetMatches();
          break;
        }
      }
    }

    indexed_flow_graphs1_.push_back(&flow_graph_info_entry1->second);
    indexed_flow_graphs2_.push_back(&flow_graph_info_entry2->second);

    indexed_fixed_points_[index] = nullptr;  // Mark for deletion

    DCHECK(fixed_point_infos_.find(fixed_point_info) !=
           fixed_point_infos_.end());
    fixed_point_infos_.erase(fixed_point_info);
  }

  // Clear out zero entries from histogram.
  for (auto it = histogram_.begin(), end = histogram_.end(); it != end;) {
    if (it->second == 0) {
      histogram_.erase(it++);
    } else {
      ++it;
    }
  }

  // Erase indexed fixed points that were marked for deletion.
  indexed_fixed_points_.erase(std::remove(indexed_fixed_points_.begin(),
                                          indexed_fixed_points_.end(), nullptr),
                              indexed_fixed_points_.end());
  num_indexed_fixed_points = indexed_fixed_points_.size();
  DCHECK(num_indexed_fixed_points == fixed_point_infos_.size());
  DCHECK(IsIncomplete() || num_indexed_fixed_points == fixed_points_.size());

  SetDirty();
  should_reset_selection_ = true;
  return util::OkStatus();
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
  fixed_point_info.algorithm = FindString(MatchingStep::kFunctionManualName);
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
  if (IsIncomplete()) {
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

    fixed_point.SetMatchingStep(MatchingStep::kFunctionManualName);
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
        *fixed_points_
             .insert(FixedPoint(primary_graph, secondary_graph,
                                MatchingStep::kFunctionManualName))
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

  MatchedFunctionsChooser::Refresh();
  refresh_chooser("Primary Unmatched");
  refresh_chooser("Secondary Unmatched");
  refresh_chooser("Statistics");
  SetDirty();

  return 1;
}

int Results::AddMatchPrimary(size_t index) {
  // TODO(cblichmann): Port this over to the new IDA 7 API
  return 0;
#if 0
  static const int widths[] = {10, 30, 5, 6, 5};
  static const char* popups[] = {0, 0, 0, 0};
  const int index2 = choose2(
      CH_MODAL, /*x0=*/-1, /*y0=*/-1, /*x1=*/-1, /*y1=*/-1,
      /*obj=*/reinterpret_cast<void*>(this), sizeof(widths) / sizeof(widths[0]),
      widths,
      /*sizer=*/&::GetNumUnmatchedSecondary,
      /*getl=*/&::GetUnmatchedSecondaryDescription, "Secondary Unmatched",
      /*icon=*/-1, /*deflt=*/1, /*del=*/nullptr,
      /*ins=*/nullptr, /*update=*/nullptr, /*edit=*/nullptr,
      /*enter=*/nullptr, /*destroy=*/nullptr,
      /*popup_names=*/popups, /*get_icon=*/nullptr);
  if (index2 > 0) {
    const Address primary = GetPrimaryAddress(index);
    const Address secondary = GetSecondaryAddress(index2);
    return AddMatch(primary, secondary);
  }
#endif
}

int Results::AddMatchSecondary(size_t index) {
  // TODO(cblichmann): Port this over to the new IDA 7 API
  return 0;
#if 0
  static const int widths[] = {10, 30, 5, 6, 5};
  static const char* popups[] = {0, 0, 0, 0};
  const int index2 =
      choose2(CH_MODAL, /*x0=*/-1, /*y0=*/-1, /*x1=*/-1, /*y1=*/-1,
              /*obj=*/reinterpret_cast<void*>(this),
              sizeof(widths) / sizeof(widths[0]), widths,
              /*sizer=*/&::GetNumUnmatchedPrimary,
              /*getl=*/&::GetUnmatchedPrimaryDescription, "Primary Unmatched",
              /*icon=*/-1, /*deflt=*/1, /*del=*/nullptr,
              /*ins=*/nullptr, /*update=*/nullptr, /*edit=*/nullptr,
              /*enter=*/nullptr, /*destroy=*/nullptr,
              /*popup_names=*/popups, /*get_icon=*/nullptr);
  if (index2 > 0) {
    const Address secondary = GetSecondaryAddress(index);
    const Address primary = GetPrimaryAddress(index2);
    return AddMatch(primary, secondary);
  }
#endif
}

size_t Results::GetNumMatches() const {
  return indexed_fixed_points_.size();
}

Results::MatchDescription Results::GetMatchDescription(int index) const {
  if (index < 0 || index >= indexed_fixed_points_.size()) {
    return {};
  }

  const FixedPointInfo& fixed_point = *indexed_fixed_points_[index];
  UpdateName(const_cast<CallGraph*>(&call_graph1_), fixed_point.primary);

  FlowGraphInfo empty{0};
  const FlowGraphInfo& primary(
      flow_graph_infos1_.find(fixed_point.primary) != flow_graph_infos1_.end()
          ? flow_graph_infos1_.find(fixed_point.primary)->second
          : empty);
  const FlowGraphInfo& secondary(
      flow_graph_infos2_.find(fixed_point.secondary) != flow_graph_infos2_.end()
          ? flow_graph_infos2_.find(fixed_point.secondary)->second
          : empty);

  MatchDescription desc{};
  desc.similarity = fixed_point.similarity;
  desc.confidence = fixed_point.confidence;
  desc.change_type = static_cast<ChangeType>(fixed_point.flags);
  desc.address_primary = fixed_point.primary;
  desc.name_primary =
      call_graph1_.GetGoodName(call_graph1_.GetVertex(fixed_point.primary));
  desc.address_secondary = fixed_point.secondary;
  desc.name_secondary =
      call_graph2_.GetGoodName(call_graph2_.GetVertex(fixed_point.secondary));
  desc.comments_ported = fixed_point.comments_ported;
  desc.algorithm_name = *fixed_point.algorithm;
  desc.basic_block_count = fixed_point.basic_block_count;
  desc.basic_block_count_primary = primary.basic_block_count;
  desc.basic_block_count_secondary = secondary.basic_block_count;
  desc.instruction_count = fixed_point.instruction_count;
  desc.instruction_count_primary = primary.instruction_count;
  desc.instruction_count_secondary = secondary.instruction_count;
  desc.edge_count = fixed_point.edge_count;
  desc.edge_count_primary = primary.edge_count;
  desc.edge_count_secondary = secondary.edge_count;
  desc.manual = fixed_point.IsManual();
  return desc;
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

  std::map<int, string> algorithms;
  {
    SqliteStatement statement(database.get(),
                              "select id, name from basicblockalgorithm");
    for (statement.Execute(); statement.GotData(); statement.Execute()) {
      int id;
      string name;
      statement.Into(&id).Into(&name);
      algorithms[id] = name;
    }
  }

  SqliteStatement statement(
      database.get(),
      "SELECT basicblock.address1, basicblock.address2, basicblock.algorithm "
      "FROM function "
      "INNER JOIN basicblock ON functionid = function.id "
      "INNER JOIN instruction ON basicblockid = basicblock.id "
      "WHERE function.address1 = :address1 AND function.address2 = :address2 "
      "ORDER BY basicblock.id");
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
  // TODO(cblichmann): Cache the temporary flow graphs. Comment porting should
  //                   not need to re-parse the full BinExport2 for each match.
  //                   In the BinExport1 format, it was necessary and efficient
  //                   to it this way.
  instruction_cache_.clear();
  try {
    ReadTemporaryFlowGraph(fixed_point_info, flow_graph_infos1_, &call_graph1_,
                           &primary, &instruction_cache_);
  } catch (...) {
    throw std::runtime_error(
        absl::StrCat("error reading: ", call_graph1_.GetFilePath()));
  }
  try {
    ReadTemporaryFlowGraph(fixed_point_info, flow_graph_infos2_, &call_graph2_,
                           &secondary, &instruction_cache_);
  } catch (...) {
    throw std::runtime_error(
        absl::StrCat("error reading: ", call_graph2_.GetFilePath()));
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

bool Results::PrepareVisualCallGraphDiff(size_t index, string* message) {
  if (index >= indexed_fixed_points_.size()) {
    return false;
  }

  const FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index]);
  ++diff_database_id_;
  string name = absl::StrCat("visual_diff", diff_database_id_, ".database");
  string database_file;
  // TODO(cblichmann): Bug: if matches have been manually modified in the
  //                   meantime we are hosed!
  if (IsIncomplete()) {
    database_file = input_filename_;
  } else {
    // TODO(cblichmann): This is insanely inefficient: every single call graph
    //                   diff recreates the full result.
    DatabaseWriter writer(name, true);
    writer.Write(call_graph1_, call_graph2_, flow_graphs1_, flow_graphs2_,
                 fixed_points_);
    database_file = writer.GetFilename();
  }

  *message = VisualDiffMessage(
      /*call_graph_match=*/true, database_file, call_graph1_.GetFilePath(),
      fixed_point_info.primary, call_graph2_.GetFilePath(),
      fixed_point_info.secondary);
  return true;
}

bool Results::PrepareVisualDiff(size_t index, string* message) {
  if (index >= indexed_fixed_points_.size()) {
    return false;
  }

  const FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index]);

  FlowGraphInfo empty{0};
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
  if (IsIncomplete()) {
    LOG(INFO) << "Loading incomplete flow graphs";
    // Results have been loaded: we need to reload flow graphs and recreate
    // basic block fixed_points.
    SetupTemporaryFlowGraphs(fixed_point_info, primary, secondary, fixed_point,
                             /*create_instruction_matches=*/false);
  } else {
    fixed_point = *FindFixedPoint(fixed_point_info);
  }
  flow_graphs1.insert(fixed_point.GetPrimary());
  flow_graphs2.insert(fixed_point.GetSecondary());
  fixed_points.insert(fixed_point);

  ++diff_database_id_;
  string name(absl::StrCat("visual_diff", diff_database_id_, ".database"));
  DatabaseWriter writer(name, true);
  writer.Write(call_graph1_, call_graph2_, flow_graphs1, flow_graphs2,
               fixed_points);
  const string& database_file = writer.GetFilename();

  *message = VisualDiffMessage(
      /*call_graph_match=*/false, database_file, call_graph1_.GetFilePath(),
      fixed_point.GetPrimary()->GetEntryPointAddress(),
      call_graph2_.GetFilePath(),
      fixed_point.GetSecondary()->GetEntryPointAddress());
  if (IsIncomplete()) {
    DeleteTemporaryFlowGraphs();
  }
  return true;
}

FixedPoint* Results::FindFixedPoint(const FixedPointInfo& fixed_point_info) {
  // TODO(cblichmann): Use binary search.
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

  // TODO(cblichmann): Iterate over all fixedpoints that have been added
  //                   manually by the Java UI and evaluate them (add basic
  //                   block/instruction matches).
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
      ::security::bindiff::Count(fixed_point, &counts, &histogram);
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

util::Status Results::PortComments(Address start_address_source,
                                   Address end_address_source,
                                   Address start_address_target,
                                   Address end_address_target,
                                   double min_confidence,
                                   double min_similarity) {
  // TODO(cblichmann): Merge with the vector version of PortComments().
  try {
    for (auto* fixed_point_info : indexed_fixed_points_) {
      if (get_func(static_cast<ea_t>(fixed_point_info->primary))) {
        if (IsIncomplete()) {
          FlowGraph primary;
          FlowGraph secondary;
          FixedPoint fixed_point;
          SetupTemporaryFlowGraphs(*fixed_point_info, primary, secondary,
                                   fixed_point,
                                   /*create_instruction_matches=*/false);

          SetComments(&fixed_point, call_graph2_.GetComments(),
                      start_address_target, end_address_target,
                      start_address_source, end_address_source, min_confidence,
                      min_similarity);

          DeleteTemporaryFlowGraphs();
        } else {
          SetComments(FindFixedPoint(*fixed_point_info),
                      call_graph2_.GetComments(), start_address_target,
                      end_address_target, start_address_source,
                      end_address_source, min_confidence, min_similarity);
        }
      }
      fixed_point_info->comments_ported = true;
    }
    MarkPortedCommentsInTempDatabase();
  } catch (const std::exception& message) {
    return util::Status{
        absl::StatusCode::kInternal,
        absl::StrCat("Importing symbols/comments: ", message.what())};
  } catch (...) {
    return util::Status{absl::StatusCode::kUnknown,
                        "Importing symbols/comments"};
  }
  return util::OkStatus();
}

util::Status Results::PortComments(absl::Span<const size_t> indices,
                                   Results::PortCommentsKind how) {
  if (indices.empty()) {
    return util::OkStatus();
  }
  try {
    for (const size_t index : indices) {
      if (index >= indexed_fixed_points_.size()) {
        return util::Status{absl::StatusCode::kInvalidArgument,
                            absl::StrCat("Index out of range: ", index)};
      }
      FixedPointInfo& fixed_point_info = *indexed_fixed_points_[index];
      const ea_t start_address_target = 0;
      const ea_t end_address_target = std::numeric_limits<ea_t>::max() - 1;
      const ea_t start_address_source = fixed_point_info.primary;
      func_t* function = get_func(static_cast<ea_t>(start_address_source));
      if (function) {
        const ea_t end_address_source = function->end_ea;
        if (how == kAsExternalLib) {
          function->flags |= FUNC_LIB;
        }
        if (IsIncomplete()) {
          FlowGraph primary;
          FlowGraph secondary;
          FixedPoint fixed_point;
          // TODO(cblichmann): See comment in SetupTemporaryFlowGraphs(), cache
          //                   the BinExport2.
          SetupTemporaryFlowGraphs(fixed_point_info, primary, secondary,
                                   fixed_point,
                                   /*create_instruction_matches=*/false);

          SetComments(&fixed_point, call_graph2_.GetComments(),
                      start_address_target, end_address_target,
                      start_address_source, end_address_source,
                      /*min_confidence=*/0.0, /*min_similarity=*/0.0);

          DeleteTemporaryFlowGraphs();
        } else {
          SetComments(FindFixedPoint(fixed_point_info),
                      call_graph2_.GetComments(), start_address_target,
                      end_address_target, start_address_source,
                      end_address_source, /*min_confidence=*/0.0,
                      /*min_similarity=*/0.0);
        }
      }
      fixed_point_info.comments_ported = true;
    }
    MarkPortedCommentsInTempDatabase();
  } catch (const std::exception& message) {
    return util::Status{
        absl::StatusCode::kInternal,
        absl::StrCat("Importing symbols/comments: ", message.what())};
  } catch (...) {
    return util::Status{absl::StatusCode::kUnknown,
                        "Importing symbols/comments"};
  }
  return util::OkStatus();
}

util::Status Results::ConfirmMatches(absl::Span<const size_t> indices) {
  if (indices.empty()) {
    return util::OkStatus();
  }
  for (const auto& index : indices) {
    if (index >= indexed_fixed_points_.size()) {
      return util::Status{absl::StatusCode::kInvalidArgument,
                          absl::StrCat("Index out of range: ", index)};
    }

    FixedPointInfo* fixed_point_info(indexed_fixed_points_[index]);
    fixed_point_info->algorithm = FindString(MatchingStep::kFunctionManualName);
    fixed_point_info->confidence = 1.0;
    if (!IsIncomplete()) {
      FixedPoint* fixed_point(FindFixedPoint(*fixed_point_info));
      fixed_point->SetMatchingStep(*fixed_point_info->algorithm);
      fixed_point->SetConfidence(fixed_point_info->confidence);
    }
  }
  SetDirty();
  return util::OkStatus();
}

int Results::CopyPrimaryAddress(int index) const {
  if (index < 0 || index >= static_cast<int>(indexed_fixed_points_.size())) {
    return 0;
  }

  const FixedPointInfo& fixed_point_info = *indexed_fixed_points_[index];
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(fixed_point_info.primary));
  CopyToClipboard(buffer);

  return 1;
}

int Results::CopySecondaryAddress(int index) const {
  if (index < 0 || index >= static_cast<int>(indexed_fixed_points_.size())) {
    return 0;
  }

  const FixedPointInfo& fixed_point_info = *indexed_fixed_points_[index];
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(fixed_point_info.secondary));
  CopyToClipboard(buffer);

  return 1;
}

int Results::CopyPrimaryAddressUnmatched(int index) const {
  if (index < 0 || index >= static_cast<int>(indexed_flow_graphs1_.size())) {
    return 0;
  }

  const FlowGraphInfo& flowGraphInfo = *indexed_flow_graphs1_[index];
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(flowGraphInfo.address));
  CopyToClipboard(buffer);

  return 1;
}

int Results::CopySecondaryAddressUnmatched(int index) const {
  if (index < 0 || index >= static_cast<int>(indexed_flow_graphs2_.size())) {
    return 0;
  }

  const FlowGraphInfo& flowGraphInfo = *indexed_flow_graphs2_[index];
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(flowGraphInfo.address));
  CopyToClipboard(buffer);

  return 1;
}

bool Results::IsIncomplete() const { return incomplete_results_; }

void Results::InitializeIndexedVectors() {
  std::set<Address> matched_primaries;
  std::set<Address> matched_secondaries;
  for (const auto& fixed_point : fixed_point_infos_) {
    matched_primaries.insert(fixed_point.primary);
    matched_secondaries.insert(fixed_point.secondary);
    // TODO(cblichmann): Get rid of the const_cast
    indexed_fixed_points_.push_back(const_cast<FixedPointInfo*>(&fixed_point));
  }
  std::sort(indexed_fixed_points_.begin(), indexed_fixed_points_.end(),
            &SortBySimilarity);

  for (auto& info : flow_graph_infos1_) {
    if (matched_primaries.find(info.first) == matched_primaries.end()) {
      indexed_flow_graphs1_.push_back(&info.second);
    }
  }
  for (auto& info : flow_graph_infos2_) {
    if (matched_secondaries.find(info.first) == matched_secondaries.end()) {
      indexed_flow_graphs2_.push_back(&info.second);
    }
  }

  FlowGraphInfo empty{0};
  CallGraph::VertexIterator i;
  CallGraph::VertexIterator end;
  for (boost::tie(i, end) = boost::vertices(call_graph1_.GetGraph()); i != end;
       ++i) {
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
  for (boost::tie(i, end) = boost::vertices(call_graph2_.GetGraph()); i != end;
       ++i) {
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

}  // namespace bindiff
}  // namespace security
