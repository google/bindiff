// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "third_party/zynamics/bindiff/ida/results.h"

#include <algorithm>
#include <cstddef>
#include <cstdint>
#include <cstdio>
#include <exception>
#include <fstream>
#include <ios>
#include <limits>
#include <memory>
#include <stdexcept>
#include <string>
#include <utility>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <bytes.hpp>                                            // NOLINT
#include <funcs.hpp>                                            // NOLINT
#include <frame.hpp>                                            // NOLINT
#include <enum.hpp>                                             // NOLINT
#include <ida.hpp>                                              // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include <lines.hpp>                                            // NOLINT
#include <netnode.hpp>                                          // NOLINT
#include <name.hpp>                                             // NOLINT
#include <struct.hpp>                                           // NOLINT
#include <ua.hpp>                                               // NOLINT
#include <xref.hpp>                                             // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/absl/container/flat_hash_set.h"
#include "third_party/absl/log/check.h"
#include "third_party/absl/log/log.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/types/span.h"
#include "third_party/zynamics/bindiff/change_classifier.h"
#include "third_party/zynamics/bindiff/comment.h"
#include "third_party/zynamics/bindiff/database_writer.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/ida/names.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/match/call_graph.h"
#include "third_party/zynamics/bindiff/match/context.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"
#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/sqlite.h"
#include "third_party/zynamics/bindiff/statistics.h"
#include "third_party/zynamics/bindiff/writer.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/ida/names.h"
#include "third_party/zynamics/binexport/ida/ui.h"
#include "third_party/zynamics/binexport/ida/util.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"
#include "third_party/zynamics/binexport/util/status_macros.h"
#include "third_party/zynamics/binexport/util/timer.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

using binexport::FormatAddress;
using binexport::FormatFunctionName;
using binexport::GetDemangledName;
using binexport::GetName;
using binexport::HumanReadableDuration;
using binexport::ToStringView;

namespace {

absl::Status ReadTemporaryFlowGraph(Address address,
                                    const FlowGraphInfos& flow_graph_infos,
                                    CallGraph* call_graph,
                                    FlowGraph* flow_graph,
                                    Instruction::Cache* instruction_cache) {
  auto info = flow_graph_infos.find(address);
  if (info == flow_graph_infos.end()) {
    return absl::NotFoundError(absl::StrCat("Flow graph not found for address",
                                            FormatAddress(address)));
  }
  std::ifstream stream(call_graph->GetFilePath(), std::ios::binary);
  BinExport2 proto;
  if (!proto.ParseFromIstream(&stream)) {
    return absl::UnknownError(absl::StrCat(
        "Failed parsing protocol buffer for ", call_graph->GetFilePath()));
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
      return flow_graph->Read(proto, proto_flow_graph, call_graph,
                              instruction_cache);
    }
  }
  return absl::UnknownError(
      absl::StrCat("Flow graph data not found for address ",
                   FormatAddress(info->second.address)));
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
  CallGraph* secondary_call_graph = fixed_point->GetSecondary()->GetCallGraph();
  const Address secondary_address =
      fixed_point->GetSecondary()->GetEntryPointAddress();
  if (!secondary_call_graph->HasRealName(
          secondary_call_graph->GetVertex(secondary_address))) {
    return false;  // No name to port
  }

  CallGraph* primary_call_graph = fixed_point->GetPrimary()->GetCallGraph();
  const Address primary_address =
      fixed_point->GetPrimary()->GetEntryPointAddress();
  const func_t* function = get_func(static_cast<ea_t>(primary_address));
  if (!function || function->start_ea != primary_address) {
    return false;  // Function does not exist in primary (manually deleted?)
  }

  const qstring buffer =
      get_name(static_cast<ea_t>(primary_address), /*gtn_flags=*/0);
  const std::string& name = fixed_point->GetSecondary()->GetName();
  if (ToStringView(buffer) == name) {
    return false;  // Function already has the same name
  }

  set_name(static_cast<ea_t>(primary_address), name.c_str(),
           SN_NOWARN | SN_CHECK);
  const auto vertex = primary_call_graph->GetVertex(primary_address);
  primary_call_graph->SetName(vertex, name);
  primary_call_graph->SetDemangledName(
      vertex, GetDemangledName(static_cast<ea_t>(primary_address)));
  return true;
}

size_t SetComments(Address source, Address target,
                   const CommentsByOperatorId& comments,
                   FixedPoint* fixed_point = nullptr) {
  int comment_count = 0;
  for (auto i = comments.lower_bound({source, 0});
       i != comments.end() && i->first.first == source; ++i, ++comment_count) {
    CHECK(source == i->first.first);
    const Comment& comment = i->second;
    const Address address = target;
    const int operand_id = i->first.second;

    // Do not port auto-generated names (unfortunately this does not work for
    // comments that were auto-generated).
    if ((comment.type == Comment::ENUM || comment.type == Comment::LOCATION ||
         comment.type == Comment::GLOBAL_REFERENCE ||
         comment.type == Comment::LOCAL_REFERENCE) &&
        !is_uname(comment.comment.c_str())) {
      continue;
    }

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
      case Comment::FUNCTION:
        if (func_t* function = get_func(static_cast<ea_t>(address));
            function && function->start_ea == address) {
          set_func_cmt(function, comment.comment.c_str(), comment.repeatable);
        }
        break;
      case Comment::LOCATION:
        if (fixed_point) {
          PortFunctionName(fixed_point);
        }
        break;
      case Comment::ANTERIOR:
        if (const std::string existing_comment =
                GetLineComments(address, LineComment::kAnterior);
            existing_comment.rfind(comment.comment) == std::string::npos) {
          add_extra_cmt(static_cast<ea_t>(address), /*isprev=*/true, "%s",
                        comment.comment.c_str());
        }
        break;
      case Comment::POSTERIOR:
        if (const std::string existing_comment =
                GetLineComments(address, LineComment::kPosterior);
            existing_comment.rfind(comment.comment) == std::string::npos) {
          add_extra_cmt(static_cast<ea_t>(address), /*isprev=*/false, "%s",
                        comment.comment.c_str());
        }
        break;
      case Comment::GLOBAL_REFERENCE: {
        int count = 0;
        xrefblk_t xb;
        for (bool ok = xb.first_from(static_cast<ea_t>(address), XREF_DATA); ok;
             ok = xb.next_from(), ++count) {
          if (count == operand_id - UA_MAXOP - 1024) {
            qstring current_name = get_name(xb.to, /*gtn_flags=*/0);
            if (ToStringView(current_name) == comment.comment) {
              set_name(xb.to, comment.comment.c_str(), SN_NOWARN | SN_CHECK);
            }
            break;
          }
        }
        break;
      }
      case Comment::LOCAL_REFERENCE: {
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

size_t SetComments(FixedPoint* fixed_point,
                   const CommentsByOperatorId& comments, Address start_source,
                   Address end_source, Address start_target, Address end_target,
                   double min_confidence, double min_similarity) {
  // Symbols and comments are always ported from secondary to primary:
  const FlowGraph* source_flow_graph = fixed_point->GetSecondary();
  const FlowGraph* target_flow_graph = fixed_point->GetPrimary();

  // SetComments is called three times below which potentially sets a single
  // comment multiple times. This is necessary however, because iterating over
  // fixed points might miss comments otherwise. For instance, there may be a
  // function fixed point but no corresponding instruction fixed point for the
  // function's entry point address.
  size_t counts = 0;
  Address source = source_flow_graph->GetEntryPointAddress();
  Address target = target_flow_graph->GetEntryPointAddress();
  fixed_point->SetCommentsPorted(true);

  auto address_pair_in_range = [start_source, end_source, start_target,
                                end_target](Address source, Address target) {
    return source >= start_source && source <= end_source &&
           target >= start_target && target <= end_target;
  };

  if (address_pair_in_range(source, target)) {
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
      if (address_pair_in_range(source, target)) {
        counts +=
            SetComments(source, target, comments, /*fixed_point=*/nullptr);
      }
    }

    for (const auto& instruction_match : basic_block.GetInstructionMatches()) {
      source = instruction_match.second->GetAddress();
      target = instruction_match.first->GetAddress();
      if (address_pair_in_range(source, target)) {
        counts +=
            SetComments(source, target, comments, /*fixed_point=*/nullptr);
      }
    }
  }
  return counts;
}

std::string VisualDiffMessage(bool call_graph_match,
                              const std::string& database,
                              const std::string& primary_path,
                              Address primary_address,
                              const std::string& secondary_path,
                              Address secondary_address) {
  return absl::StrCat("<BinDiffMatch type=\"",
                      call_graph_match ? "call_graph" : "flow_graph", "\">",
                      "<Database path =\"", database, "\"/><Primary path=\"",
                      primary_path, "\" address=\"", primary_address,
                      "\"/><Secondary path=\"", secondary_path, "\" address=\"",
                      secondary_address, "\"/></BinDiffMatch>");
}

// Maps algorithm names from the config file to their respective display names.
std::string GetMatchingStepDisplayName(absl::string_view name) {
  static auto* algorithms =
      []() -> absl::flat_hash_map<std::string, std::string>* {
    auto* algorithms = new absl::flat_hash_map<std::string, std::string>();

    (*algorithms)[MatchingStep::kFunctionManualName] =
        MatchingStep::kFunctionManualDisplayName;
    (*algorithms)[MatchingStep::kFunctionCallReferenceName] =
        MatchingStep::kFunctionCallReferenceDisplayName;
    for (const auto* step : GetDefaultMatchingSteps()) {
      (*algorithms)[step->name()] = step->display_name();
    }

    (*algorithms)[MatchingStepFlowGraph::kBasicBlockManualName] =
        MatchingStepFlowGraph::kBasicBlockManualDisplayName;
    (*algorithms)[MatchingStepFlowGraph::kBasicBlockPropagationName] =
        MatchingStepFlowGraph::kBasicBlockPropagationDisplayName;
    for (const auto* step : GetDefaultMatchingStepsBasicBlock()) {
      (*algorithms)[step->name()] = step->display_name();
    }
    return algorithms;
  }();

  auto found = algorithms->find(name);
  return found != algorithms->end() ? found->second : std::string(name);
}

}  // namespace

Results::~Results() {
  // Need to close this explicitly here as otherwise the DeleteTemporaryFiles()
  // call below will fail (on Windows) due to locked db file.
  temp_database_->Close();
  DeleteFlowGraphs(&flow_graphs1_);
  DeleteFlowGraphs(&flow_graphs2_);
  DatabaseTransmuter::DeleteTempFile();
  DeleteTemporaryFiles();
}

absl::StatusOr<std::unique_ptr<Results>> Results::Create() {
  auto results = absl::WrapUnique(new Results());
  NA_ASSIGN_OR_RETURN(results->temp_database_,
                      DatabaseWriter::Create("temporary.database", true));
  return results;
}

void Results::set_modified() { modified_ = true; }

bool Results::is_modified() const { return modified_; }

void Results::DeleteTemporaryFiles() {
  // Extremely dangerous, make very sure GetDirectory _never_ returns something
  // like "C:".
  auto temp_dir = GetTempDirectory("BinDiff");
  if (!temp_dir.ok()) {
    return;
  }
  // Don't care if this fails - only litters the temp dir a bit.
  RemoveAll(*temp_dir).IgnoreError();
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

  const std::string* name = flow_graph_info.demangled_name;
  if (name == nullptr) {
    name = flow_graph_info.name;
  }

  Results::UnmatchedDescription desc{};
  desc.address = flow_graph_info.address;
  desc.name = name != nullptr && !name->empty()
                  ? *name
                  // Fallback, name may still be nullptr, as the underlying
                  // function simply might not have a name (b/263365607).
                  : FormatFunctionName(flow_graph_info.address);
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

absl::Status Results::IncrementalDiff() {
  WaitBox wait_box("Performing incremental diff...");

  if (is_incomplete()) {
    NA_ASSIGN_OR_RETURN(std::string temp_dir,
                        GetOrCreateTempDirectory("BinDiff"));
    const std::string incremental = JoinPath(temp_dir, "incremental.BinDiff");
    NA_RETURN_IF_ERROR(::security::bindiff::Read(
        call_graph1_.GetFilePath(), &call_graph1_, &flow_graphs1_,
        &flow_graph_infos1_, &instruction_cache_));
    NA_RETURN_IF_ERROR(::security::bindiff::Read(
        call_graph2_.GetFilePath(), &call_graph2_, &flow_graphs2_,
        &flow_graph_infos2_, &instruction_cache_));

    NA_RETURN_IF_ERROR(CopyFile(input_filename_, incremental));

    NA_ASSIGN_OR_RETURN(auto database, SqliteDatabase::Connect(incremental));
    DatabaseTransmuter writer(database, fixed_point_infos_);
    NA_RETURN_IF_ERROR(Write(&writer));

    try {
      DatabaseReader::ReadFullMatches(&database, &call_graph1_, &call_graph2_,
                                      &flow_graphs1_, &flow_graphs2_,
                                      &fixed_points_);
    } catch (const std::runtime_error& message) {
      return absl::UnknownError(message.what());
    }

    std::remove(incremental.c_str());
    incomplete_ = false;
  }

  Timer<> timer;
  MatchingContext context(call_graph1_, call_graph2_, flow_graphs1_,
                          flow_graphs2_, fixed_points_);

  // Try to find any confirmed fixed points. If there aren't any just return.
  bool has_confirmed_fixedpoints = false;
  for (const FixedPoint& fixed_point : fixed_points_) {
    if (fixed_point.GetMatchingStep() ==
        absl::string_view(MatchingStep::kFunctionManualName)) {
      has_confirmed_fixedpoints = true;
      break;
    }
  }
  if (!has_confirmed_fixedpoints) {
    warning(
        "No manually confirmed fixed points found. Please add some matches "
        "or use the matched functions window context menu to confirm automatic "
        "matches before running an incremental diff");
    return absl::CancelledError("");
  }

  // Remove all non-manual matches from current result
  for (auto it = fixed_points_.begin(), end = fixed_points_.end(); it != end;) {
    FixedPoint& fixed_point = const_cast<FixedPoint&>(*it);
    FlowGraph* primary = fixed_point.GetPrimary();
    FlowGraph* secondary = fixed_point.GetSecondary();
    if (fixed_point.GetMatchingStep() ==
        absl::string_view{MatchingStep::kFunctionManualName}) {
      ++it;
      continue;  // Keep confirmed fixed points.
    }
    fixed_points_.erase(it++);

    primary->ResetMatches();
    secondary->ResetMatches();
    temp_database_->DeleteFromTempDatabase(primary->GetEntryPointAddress(),
                                           secondary->GetEntryPointAddress());
  }

  // These will get refilled by ShowResults().
  indexed_flow_graphs1_.clear();
  indexed_flow_graphs2_.clear();
  indexed_fixed_points_.clear();
  histogram_.clear();
  counts_.clear();

  // Diff
  const MatchingSteps call_graph_steps = GetDefaultMatchingSteps();
  const MatchingStepsFlowGraph basic_block_steps =
      GetDefaultMatchingStepsBasicBlock();
  Diff(&context, call_graph_steps, basic_block_steps);

  // Refill fixed point info.
  fixed_point_infos_.clear();
  for (const FixedPoint& fixed_point : fixed_points_) {
    FixedPointInfo info;
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
    info.basic_block_count = counts[Counts::kBasicBlockMatchesLibrary] +
                             counts[Counts::kBasicBlockMatchesNonLibrary];
    info.instruction_count = counts[Counts::kInstructionMatchesLibrary] +
                             counts[Counts::kInstructionMatchesNonLibrary];
    info.edge_count = counts[Counts::kFlowGraphEdgeMatchesLibrary] +
                      counts[Counts::kFlowGraphEdgeMatchesNonLibrary];
    fixed_point_infos_.insert(info);
  }

  LOG(INFO) << absl::StrCat(HumanReadableDuration(timer.elapsed()),
                            " for incremental matching.");

  set_modified();
  return absl::OkStatus();
}

size_t Results::GetNumStatistics() const {
  return counts_.ui_entry_size() + histogram_.size() +
         2 /* Similarity & Confidence */;
}

Results::StatisticDescription Results::GetStatisticDescription(
    size_t index) const {
  Results::StatisticDescription desc{};
  if (index > GetNumStatistics()) {
    return desc;
  }

  if (index < counts_.ui_entry_size()) {
    const auto entry = counts_.GetEntry(index);
    desc.name = std::string(entry.first);
    desc.is_count = true;
    desc.count = entry.second;
  } else if (index < histogram_.size() + counts_.ui_entry_size()) {
    index -= counts_.ui_entry_size();
    auto it = histogram_.cbegin();
    for (size_t nr = 0; it != histogram_.cend() && nr < index; ++it, ++nr) {
    }
    desc.name = GetMatchingStepDisplayName(it->first);
    desc.is_count = true;
    desc.count = it->second;
  } else if (index == histogram_.size() + counts_.ui_entry_size() + 1) {
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

absl::Status Results::DeleteMatches(absl::Span<const size_t> indices) {
  if (indices.empty()) {
    return absl::OkStatus();
  }
  auto num_indexed_fixed_points = indexed_fixed_points_.size();
  for (const auto& index : indices) {
    if (index >= num_indexed_fixed_points) {
      return absl::InvalidArgumentError(
          absl::StrCat("Index out of range: ", index));
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

    temp_database_->DeleteFromTempDatabase(primary_address, secondary_address);

    if (call_graph2_.IsLibrary(call_graph2_.GetVertex(secondary_address)) ||
        call_graph1_.IsLibrary(call_graph1_.GetVertex(primary_address))) {
      counts_[Counts::kFunctionMatchesLibrary] -= 1;
      counts_[Counts::kBasicBlockMatchesLibrary] -=
          fixed_point_info.basic_block_count;
      counts_[Counts::kInstructionMatchesLibrary] -=
          fixed_point_info.instruction_count;
      counts_[Counts::kFlowGraphEdgeMatchesLibrary] -=
          fixed_point_info.edge_count;
    } else {
      counts_[Counts::kFunctionMatchesNonLibrary] -= 1;
      counts_[Counts::kBasicBlockMatchesNonLibrary] -=
          fixed_point_info.basic_block_count;
      counts_[Counts::kInstructionMatchesNonLibrary] -=
          fixed_point_info.instruction_count;
      counts_[Counts::kFlowGraphEdgeMatchesNonLibrary] -=
          fixed_point_info.edge_count;
    }
    auto& algorithm_count = histogram_[*fixed_point_info.algorithm];
    if (algorithm_count > 0) {
      --algorithm_count;
    }

    // TODO(cblichmann): Tree search, this is O(n^2) when deleting all matches.
    if (!is_incomplete()) {
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
  DCHECK(is_incomplete() || num_indexed_fixed_points == fixed_points_.size());

  set_modified();
  should_reset_selection_ = true;
  return absl::OkStatus();
}

FlowGraph* FindGraph(FlowGraphs& graphs,  // NOLINT(runtime/references)
                     Address address) {
  // TODO(cblichmann): Graphs are sorted, we don't need to search everything.
  for (auto i = graphs.begin(), end = graphs.end(); i != end; ++i) {
    if ((*i)->GetEntryPointAddress() == address) {
      return *i;
    }
  }
  return 0;
}

absl::Status Results::AddMatch(Address primary, Address secondary) {
  try {
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
    if (is_incomplete()) {
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
      fixed_point.SetSimilarity(GetSimilarityScore(
          primary_graph, secondary_graph, histogram, counts));
      ClassifyChanges(&fixed_point);
      fixed_point_info.basic_block_count =
          counts[Counts::kBasicBlockMatchesLibrary] +
          counts[Counts::kBasicBlockMatchesNonLibrary];
      fixed_point_info.instruction_count =
          counts[Counts::kInstructionMatchesLibrary] +
          counts[Counts::kInstructionMatchesNonLibrary];
      fixed_point_info.edge_count =
          counts[Counts::kFlowGraphEdgeMatchesLibrary] +
          counts[Counts::kFlowGraphEdgeMatchesNonLibrary];
      fixed_point_info.similarity = fixed_point.GetSimilarity();
      fixed_point_info.flags = fixed_point.GetFlags();

      temp_database_->WriteToTempDatabase(fixed_point);

      DeleteTemporaryFlowGraphs();
    } else {
      FlowGraph* primary_graph = FindGraph(flow_graphs1_, primary);
      FlowGraph* secondary_graph = FindGraph(flow_graphs2_, secondary);
      if (!primary_graph || primary_graph->GetEntryPointAddress() != primary ||
          !secondary_graph ||
          secondary_graph->GetEntryPointAddress() != secondary) {
        return absl::InternalError("Invalid graphs in AddMatch()");
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
          counts[Counts::kBasicBlockMatchesLibrary] +
          counts[Counts::kBasicBlockMatchesNonLibrary];
      fixed_point_info.instruction_count =
          counts[Counts::kInstructionMatchesLibrary] +
          counts[Counts::kInstructionMatchesNonLibrary];
      fixed_point_info.edge_count =
          counts[Counts::kFlowGraphEdgeMatchesLibrary] +
          counts[Counts::kFlowGraphEdgeMatchesNonLibrary];
      fixed_point_info.similarity = fixed_point.GetSimilarity();
      fixed_point_info.flags = fixed_point.GetFlags();
    }

    fixed_point_infos_.insert(fixed_point_info);
    indexed_fixed_points_.push_back(const_cast<FixedPointInfo*>(
        &*fixed_point_infos_.find(fixed_point_info)));
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
      counts_[Counts::kFunctionMatchesLibrary] += 1;
      counts_[Counts::kBasicBlockMatchesLibrary] +=
          fixed_point_info.basic_block_count;
      counts_[Counts::kInstructionMatchesLibrary] +=
          fixed_point_info.instruction_count;
      counts_[Counts::kFlowGraphEdgeMatchesLibrary] +=
          fixed_point_info.edge_count;
    } else {
      counts_[Counts::kFunctionMatchesNonLibrary] += 1;
      counts_[Counts::kBasicBlockMatchesNonLibrary] +=
          fixed_point_info.basic_block_count;
      counts_[Counts::kInstructionMatchesNonLibrary] +=
          fixed_point_info.instruction_count;
      counts_[Counts::kFlowGraphEdgeMatchesNonLibrary] +=
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
    set_modified();
  } catch (const std::exception& message) {
    return absl::InternalError(
        absl::StrCat("Error adding manual match: ", message.what()));
  } catch (...) {
    return absl::UnknownError("Error adding manual match");
  }
  return absl::OkStatus();
}

size_t Results::GetNumMatches() const { return indexed_fixed_points_.size(); }

Results::MatchDescription Results::GetMatchDescription(size_t index) const {
  if (index >= indexed_fixed_points_.size()) {
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
  desc.algorithm_name = GetMatchingStepDisplayName(*fixed_point.algorithm);
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
  // (the user may have added fixed points manually)
  // only if we cannot find the fixed point there we load from the original db
  int id = 0;
  temp_database_->database()
      ->StatementOrThrow(
          "SELECT COALESCE(id, 0) FROM function WHERE function.address1 = "
          ":address1 AND function.address2 = :address2")
      .BindInt64(fixed_point->GetPrimary()->GetEntryPointAddress())
      .BindInt64(fixed_point->GetSecondary()->GetEntryPointAddress())
      .ExecuteOrThrow()
      .Into(&id);
  absl::StatusOr<SqliteDatabase> original_database;
  SqliteDatabase* database;
  if (id) {  // Found in temp db
    database = temp_database_->database();
  } else {  // Load original
    original_database = SqliteDatabase::Connect(input_filename_);
    database = &*original_database;
  }

  absl::flat_hash_map<int, std::string> algorithms;
  {
    SqliteStatement statement =
        database->StatementOrThrow("SELECT id, name FROM basicblockalgorithm");
    for (statement.ExecuteOrThrow(); statement.GotData();
         statement.ExecuteOrThrow()) {
      int id;
      std::string name;
      statement.Into(&id).Into(&name);
      algorithms[id] = name;
    }
  }

  SqliteStatement statement = database->StatementOrThrow(
      "SELECT basicblock.address1, basicblock.address2, "
      "basicblock.algorithm "
      "FROM function "
      "INNER JOIN basicblock ON functionid = function.id "
      "INNER JOIN instruction ON basicblockid = basicblock.id "
      "WHERE function.address1 = :address1 AND "
      "function.address2 = :address2 "
      "ORDER BY basicblock.id");
  statement.BindInt64(fixed_point->GetPrimary()->GetEntryPointAddress())
      .BindInt64(fixed_point->GetSecondary()->GetEntryPointAddress());

  std::pair<Address, Address> last_basic_block(
      std::numeric_limits<Address>::max(), std::numeric_limits<Address>::max());
  int last_algorithm = -1;
  for (statement.ExecuteOrThrow(); statement.GotData();
       statement.ExecuteOrThrow()) {
    std::pair<Address, Address> basic_block;
    int algorithm;
    statement.Into(&basic_block.first)
        .Into(&basic_block.second)
        .Into(&algorithm);
    if (last_algorithm < 0) {
      last_algorithm = algorithm;
      last_basic_block = basic_block;
    }
    if (basic_block == last_basic_block) {
      continue;
    }

    const auto primary_vertex =
        fixed_point->GetPrimary()->GetVertex(last_basic_block.first);
    const auto secondary_vertex =
        fixed_point->GetSecondary()->GetVertex(last_basic_block.second);
    fixed_point->Add(primary_vertex, secondary_vertex,
                     algorithms[last_algorithm]);

    last_basic_block = basic_block;
    last_algorithm = algorithm;
  }
  if (last_algorithm != -1) {
    const auto primary_vertex =
        fixed_point->GetPrimary()->GetVertex(last_basic_block.first);
    const auto secondary_vertex =
        fixed_point->GetSecondary()->GetVertex(last_basic_block.second);
    fixed_point->Add(primary_vertex, secondary_vertex,
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
  if (auto status =
          ReadTemporaryFlowGraph(fixed_point_info.primary, flow_graph_infos1_,
                                 &call_graph1_, &primary, &instruction_cache_);
      !status.ok()) {
    throw std::runtime_error(std::string(status.message()));
  }
  if (auto status = ReadTemporaryFlowGraph(fixed_point_info.secondary,
                                           flow_graph_infos2_, &call_graph2_,
                                           &secondary, &instruction_cache_);
      !status.ok()) {
    throw std::runtime_error(std::string(status.message()));
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
  if (index >= indexed_fixed_points_.size()) {
    return false;
  }

  const FixedPointInfo& fixed_point_info = *indexed_fixed_points_[index];
  ++diff_database_id_;
  std::string name =
      absl::StrCat("visual_diff", diff_database_id_, ".database");
  std::string database_file;
  // TODO(cblichmann): Bug: if matches have been manually modified in the
  //                   meantime we are hosed!
  if (is_incomplete()) {
    database_file = input_filename_;
  } else {
    // TODO(cblichmann): This is insanely inefficient: every single call graph
    //                   diff recreates the full result.
    // TODO(cblichmann): This code is duplicated in PrepareVisualDiff().
    auto database_writer = DatabaseWriter::Create(name, true);
    if (!database_writer.ok()) {
      throw std::runtime_error(std::string(database_writer.status().message()));
    }
    if (auto status = (*database_writer)
                          ->Write(call_graph1_, call_graph2_, flow_graphs1_,
                                  flow_graphs2_, fixed_points_);
        !status.ok()) {
      throw std::runtime_error(std::string(status.message()));
    }
    database_file = (*database_writer)->filename();
  }

  *message = VisualDiffMessage(
      /*call_graph_match=*/true, database_file, call_graph1_.GetFilePath(),
      fixed_point_info.primary, call_graph2_.GetFilePath(),
      fixed_point_info.secondary);
  return true;
}

bool Results::PrepareVisualDiff(size_t index, std::string* message) {
  if (index >= indexed_fixed_points_.size()) {
    return false;
  }

  const FixedPointInfo& fixed_point_info = *indexed_fixed_points_[index];

  FlowGraphInfo empty{0};
  auto primary_entry = flow_graph_infos1_.find(fixed_point_info.primary);
  const FlowGraphInfo& primary_info =
      primary_entry != flow_graph_infos1_.end() ? primary_entry->second : empty;
  auto secondary_entry = flow_graph_infos2_.find(fixed_point_info.secondary);
  const FlowGraphInfo& secondary_info =
      secondary_entry != flow_graph_infos2_.end() ? secondary_entry->second
                                                  : empty;
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
  if (is_incomplete()) {
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
  std::string name(absl::StrCat("visual_diff", diff_database_id_, ".database"));

  auto database_writer = DatabaseWriter::Create(name, true);
  if (!database_writer.ok()) {
    throw std::runtime_error(std::string(database_writer.status().message()));
  }
  if (auto status = (*database_writer)
                        ->Write(call_graph1_, call_graph2_, flow_graphs1_,
                                flow_graphs2_, fixed_points_);
      !status.ok()) {
    throw std::runtime_error(std::string(status.message()));
  }
  const std::string& database_file = (*database_writer)->filename();

  *message = VisualDiffMessage(
      /*call_graph_match=*/false, database_file, call_graph1_.GetFilePath(),
      fixed_point.GetPrimary()->GetEntryPointAddress(),
      call_graph2_.GetFilePath(),
      fixed_point.GetSecondary()->GetEntryPointAddress());
  if (is_incomplete()) {
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
  return nullptr;
}

void Results::Read(Reader* reader) {
  flow_graph_infos1_.clear();
  flow_graph_infos2_.clear();
  fixed_point_infos_.clear();
  indexed_flow_graphs1_.clear();
  indexed_flow_graphs2_.clear();
  indexed_fixed_points_.clear();

  incomplete_ = true;
  if (auto status = reader->Read(call_graph1_, call_graph2_, flow_graph_infos1_,
                                 flow_graph_infos2_, fixed_point_infos_);
      !status.ok()) {
    throw std::runtime_error(std::string(status.message()));
  }
  if (const auto* database_reader = dynamic_cast<DatabaseReader*>(reader)) {
    input_filename_ = database_reader->GetInputFilename();
    histogram_ = database_reader->GetBasicBlockFixedPointInfo();
  } else {
    CHECK(false && "unsupported reader");
  }

  InitializeIndexedVectors();
  Count();
  similarity_ = reader->similarity();
  confidence_ = reader->confidence();
  modified_ = false;

  // TODO(cblichmann): Iterate over all fixed points that have been added
  //                   manually by the Java UI and evaluate them (add basic
  //                   block/instruction matches).
}

absl::Status Results::Write(Writer* writer) {
  NA_RETURN_IF_ERROR(writer->Write(call_graph1_, call_graph2_, flow_graphs1_,
                                   flow_graphs2_, fixed_points_));
  modified_ = false;
  return absl::OkStatus();
}

void Results::CreateIndexedViews() {
  if (!indexed_flow_graphs1_.empty() || !indexed_flow_graphs2_.empty() ||
      !indexed_fixed_points_.empty()) {
    return;  // Only initialize indices the first time around.
  }
  for (const auto& fixed_point : fixed_points_) {
    FixedPointInfo fixed_point_info;
    fixed_point_info.algorithm = FindString(fixed_point.GetMatchingStep());
    fixed_point_info.confidence = fixed_point.GetConfidence();
    fixed_point_info.similarity = fixed_point.GetSimilarity();
    fixed_point_info.flags = fixed_point.GetFlags();
    fixed_point_info.primary = fixed_point.GetPrimary()->GetEntryPointAddress();
    fixed_point_info.secondary =
        fixed_point.GetSecondary()->GetEntryPointAddress();
    fixed_point_info.comments_ported = fixed_point.GetCommentsPorted();
    Counts counts;
    Histogram histogram;
    ::security::bindiff::Count(fixed_point, &counts, &histogram);
    fixed_point_info.basic_block_count =
        counts[Counts::kBasicBlockMatchesLibrary] +
        counts[Counts::kBasicBlockMatchesNonLibrary];
    fixed_point_info.instruction_count =
        counts[Counts::kInstructionMatchesLibrary] +
        counts[Counts::kInstructionMatchesNonLibrary];
    fixed_point_info.edge_count =
        counts[Counts::kFlowGraphEdgeMatchesLibrary] +
        counts[Counts::kFlowGraphEdgeMatchesNonLibrary];
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

void Results::MarkPortedCommentsInTempDatabase() {
  temp_database_->SetCommentsPorted(fixed_point_infos_);
}

// Transfer from temp db to real db.
void Results::MarkPortedCommentsInDatabase() {
  try {
    if (!input_filename_.empty()) {
      auto database = *SqliteDatabase::Connect(input_filename_);
      DatabaseTransmuter::MarkPortedComments(
          &database, temp_database_->filename().c_str(), fixed_point_infos_);
    }
  } catch (...) {
    // Swallow any errors here. The database may be read only or
    // the commentsported table doesn't exist. We don't care...
  }
}

absl::Status Results::PortComments(Address start_address_source,
                                   Address end_address_source,
                                   Address start_address_target,
                                   Address end_address_target,
                                   double min_confidence,
                                   double min_similarity) {
  // TODO(cblichmann): Merge with the vector version of PortComments().
  try {
    for (auto* fixed_point_info : indexed_fixed_points_) {
      if (get_func(static_cast<ea_t>(fixed_point_info->primary))) {
        if (is_incomplete()) {
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
    return absl::InternalError(
        absl::StrCat("Importing symbols/comments: ", message.what()));
  } catch (...) {
    return absl::UnknownError("Importing symbols/comments");
  }
  return absl::OkStatus();
}

absl::Status Results::PortComments(absl::Span<const size_t> indices,
                                   Results::PortCommentsKind how) {
  if (indices.empty()) {
    return absl::OkStatus();
  }
  try {
    for (const size_t index : indices) {
      if (index >= indexed_fixed_points_.size()) {
        return absl::InvalidArgumentError(
            absl::StrCat("Index out of range: ", index));
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
        if (is_incomplete()) {
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
    return absl::InternalError(
        absl::StrCat("Importing symbols/comments: ", message.what()));
  } catch (...) {
    return absl::UnknownError("Importing symbols/comments");
  }
  return absl::OkStatus();
}

absl::Status Results::ConfirmMatches(absl::Span<const size_t> indices) {
  if (indices.empty()) {
    return absl::OkStatus();
  }
  for (const auto& index : indices) {
    if (index >= indexed_fixed_points_.size()) {
      return absl::InvalidArgumentError(
          absl::StrCat("Index out of range: ", index));
    }

    FixedPointInfo* fixed_point_info(indexed_fixed_points_[index]);
    fixed_point_info->algorithm = FindString(MatchingStep::kFunctionManualName);
    fixed_point_info->confidence = 1.0;
    if (!is_incomplete()) {
      FixedPoint* fixed_point(FindFixedPoint(*fixed_point_info));
      fixed_point->SetMatchingStep(*fixed_point_info->algorithm);
      fixed_point->SetConfidence(fixed_point_info->confidence);
    }
  }
  set_modified();
  return absl::OkStatus();
}

bool Results::is_incomplete() const { return incomplete_; }

void Results::InitializeIndexedVectors() {
  absl::flat_hash_set<Address> matched_primaries;
  absl::flat_hash_set<Address> matched_secondaries;
  for (const auto& fixed_point : fixed_point_infos_) {
    matched_primaries.insert(fixed_point.primary);
    matched_secondaries.insert(fixed_point.secondary);
    // TODO(cblichmann): Get rid of the const_cast
    indexed_fixed_points_.push_back(const_cast<FixedPointInfo*>(&fixed_point));
  }
  std::sort(indexed_fixed_points_.begin(), indexed_fixed_points_.end(),
            &SortBySimilarity);

  for (auto& [address, info] : flow_graph_infos1_) {
    if (matched_primaries.find(address) == matched_primaries.end()) {
      indexed_flow_graphs1_.push_back(&info);
    }
  }
  for (auto& [address, info] : flow_graph_infos2_) {
    if (matched_secondaries.find(address) == matched_secondaries.end()) {
      indexed_flow_graphs2_.push_back(&info);
    }
  }

  FlowGraphInfo empty{0};
  for (auto [it, end] = boost::vertices(call_graph1_.GetGraph()); it != end;
       ++it) {
    const Address address = call_graph1_.GetAddress(*it);
    if (flow_graph_infos1_.find(address) == flow_graph_infos1_.end()) {
      empty.address = address;
      empty.name = &call_graph1_.GetName(*it);
      empty.demangled_name = &call_graph1_.GetDemangledName(*it);
      flow_graph_infos1_[address] = empty;
      if (matched_primaries.find(address) == matched_primaries.end()) {
        indexed_flow_graphs1_.push_back(&flow_graph_infos1_[address]);
      }
    }
  }
  for (auto [it, end] = boost::vertices(call_graph2_.GetGraph()); it != end;
       ++it) {
    const Address address = call_graph2_.GetAddress(*it);
    if (flow_graph_infos2_.find(address) == flow_graph_infos2_.end()) {
      empty.address = address;
      empty.name = &call_graph2_.GetName(*it);
      empty.demangled_name = &call_graph2_.GetDemangledName(*it);
      flow_graph_infos2_[address] = empty;
      if (matched_secondaries.find(address) == matched_secondaries.end()) {
        indexed_flow_graphs2_.push_back(&flow_graph_infos2_[address]);
      }
    }
  }
}

void Results::Count() {
  counts_.clear();
  // TODO(cblichmann): Run the loops for primary and secondary in parallel.
  for (const auto& [address, info] : flow_graph_infos1_) {
    const int is_lib =
        call_graph1_.IsLibrary(call_graph1_.GetVertex(info.address)) ||
        call_graph1_.IsStub(call_graph1_.GetVertex(info.address)) ||
        info.basic_block_count == 0;
    counts_[Counts::kFunctionsPrimaryLibrary] += is_lib;
    counts_[Counts::kFunctionsPrimaryNonLibrary] += (1 - is_lib);
    counts_[Counts::kBasicBlocksPrimaryLibrary] +=
        is_lib * info.basic_block_count;
    counts_[Counts::kBasicBlocksPrimaryNonLibrary] +=
        (1 - is_lib) * info.basic_block_count;
    counts_[Counts::kInstructionsPrimaryLibrary] +=
        is_lib * info.instruction_count;
    counts_[Counts::kInstructionsPrimaryNonLibrary] +=
        (1 - is_lib) * info.instruction_count;
    counts_[Counts::kFlowGraphEdgesPrimaryLibrary] += is_lib * info.edge_count;
    counts_[Counts::kFlowGraphEdgesPrimaryNonLibrary] +=
        (1 - is_lib) * info.edge_count;
  }

  for (auto [it, end] = boost::vertices(call_graph1_.GetGraph()); it != end;
       ++it) {
    const Address address = call_graph1_.GetAddress(*it);
    if (flow_graph_infos1_.find(address) == flow_graph_infos1_.end()) {
      counts_[Counts::kFunctionsPrimaryLibrary] += 1;
    }
  }

  for (const auto& [address, info] : flow_graph_infos2_) {
    const int is_lib =
        call_graph2_.IsLibrary(call_graph2_.GetVertex(info.address)) ||
        call_graph2_.IsStub(call_graph2_.GetVertex(info.address)) ||
        info.basic_block_count == 0;
    counts_[Counts::kFunctionsSecondaryLibrary] += is_lib;
    counts_[Counts::kFunctionsSecondaryNonLibrary] += (1 - is_lib);
    counts_[Counts::kBasicBlocksSecondaryLibrary] +=
        is_lib * info.basic_block_count;
    counts_[Counts::kBasicBlocksSecondaryNonLibrary] +=
        (1 - is_lib) * info.basic_block_count;
    counts_[Counts::kInstructionsSecondaryLibrary] +=
        is_lib * info.instruction_count;
    counts_[Counts::kInstructionsSecondaryNonLibrary] +=
        (1 - is_lib) * info.instruction_count;
    counts_[Counts::kFlowGraphEdgesSecondaryLibrary] +=
        is_lib * info.edge_count;
    counts_[Counts::kFlowGraphEdgesSecondaryNonLibrary] +=
        (1 - is_lib) * info.edge_count;
  }

  for (auto [it, end] = boost::vertices(call_graph2_.GetGraph()); it != end;
       ++it) {
    const Address address = call_graph2_.GetAddress(*it);
    if (flow_graph_infos2_.find(address) == flow_graph_infos2_.end()) {
      counts_[Counts::kFunctionsSecondaryLibrary] += 1;
    }
  }

  counts_[Counts::kFunctionMatchesLibrary] = 0;
  counts_[Counts::kBasicBlockMatchesLibrary] = 0;
  counts_[Counts::kInstructionMatchesLibrary] = 0;
  counts_[Counts::kFlowGraphEdgeMatchesLibrary] = 0;
  counts_[Counts::kFunctionMatchesNonLibrary] = 0;
  counts_[Counts::kBasicBlockMatchesNonLibrary] = 0;
  counts_[Counts::kInstructionMatchesNonLibrary] = 0;
  counts_[Counts::kFlowGraphEdgeMatchesNonLibrary] = 0;
  for (const auto& entry : fixed_point_infos_) {
    if (call_graph2_.IsLibrary(call_graph2_.GetVertex(entry.secondary)) ||
        flow_graph_infos2_.find(entry.secondary) == flow_graph_infos2_.end() ||
        call_graph1_.IsLibrary(call_graph1_.GetVertex(entry.primary)) ||
        flow_graph_infos1_.find(entry.primary) == flow_graph_infos1_.end()) {
      counts_[Counts::kFunctionMatchesLibrary] += 1;
      counts_[Counts::kBasicBlockMatchesLibrary] += entry.basic_block_count;
      counts_[Counts::kInstructionMatchesLibrary] += entry.instruction_count;
      counts_[Counts::kFlowGraphEdgeMatchesLibrary] += entry.edge_count;
    } else {
      counts_[Counts::kFunctionMatchesNonLibrary] += 1;
      counts_[Counts::kBasicBlockMatchesNonLibrary] += entry.basic_block_count;
      counts_[Counts::kInstructionMatchesNonLibrary] += entry.instruction_count;
      counts_[Counts::kFlowGraphEdgeMatchesNonLibrary] += entry.edge_count;
    }
    histogram_[*entry.algorithm]++;
  }
}

}  // namespace security::bindiff
