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

#include "third_party/zynamics/bindiff/log_writer.h"

#include <fstream>
#include <iomanip>
#include <memory>

// TODO(cblichmann): Replace this
#include <boost/iterator/transform_iterator.hpp>  // NOLINT(readability/boost)

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security::bindiff {

using binexport::FormatAddress;

namespace {

struct ProjectPrimary {
  FlowGraph* operator()(const FixedPoint& fixed_point) const {
    return fixed_point.GetPrimary();
  }
};

struct ProjectSecondary {
  FlowGraph* operator()(const FixedPoint& fixed_point) const {
    return fixed_point.GetSecondary();
  }
};

}  // namespace

ResultsLogWriter::ResultsLogWriter(const std::string& filename)
    : filename_(filename) {}

void ResultsLogWriter::Write(const CallGraph& call_graph1,
                             const CallGraph& call_graph2,
                             const FlowGraphs& flow_graphs1,
                             const FlowGraphs& flow_graphs2,
                             const FixedPoints& fixed_points) {
  Confidences confidences;
  Histogram histogram;
  Counts counts;
  GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points, &histogram,
                        &counts);

  std::ofstream result(filename_.c_str());
  result.precision(16);
  result << std::dec << call_graph1.GetFilename() << "\n"
         << call_graph2.GetFilename() << "\n"
         << "call graph1 MD index " << call_graph1.GetMdIndex() << "\n"
         << "call graph2 MD index " << call_graph2.GetMdIndex() << "\n"
         << "\n"
         << " --------- statistics ---------\n";
  for (int i = 0; i < counts.ui_entry_size(); ++i) {
    const auto& [name, value] = counts.GetEntry(i);
    const std::string padding(60 - name.size(), '.');
    result << name << padding << ":" << std::setw(7) << value << "\n";
  }
  result << "\n";
  for (Histogram::const_iterator i = histogram.begin(); i != histogram.end();
       ++i) {
    const std::string padding(60 - i->first.size(), '.');
    result << i->first << padding << ":" << std::setw(7) << i->second << "\n";
  }
  result << "\n"
         << "similarity: "
         << GetSimilarityScore(call_graph1, call_graph2, histogram, counts)
         << "\n"
         << "confidence: " << GetConfidence(histogram, &confidences) << "\n\n"
         << "individual confidence values used: \n";
  for (Confidences::const_iterator i = confidences.begin();
       i != confidences.end(); ++i) {
    const std::string padding(60 - i->first.size(), '.');
    result << i->first << padding << ":" << std::setw(7) << std::setprecision(2)
           << i->second << "\n";
  }
  result << "\n"
         << " --------- matched " << std::dec << fixed_points.size() << " of "
         << counts[Counts::kFunctionsPrimaryNonLibrary] << "/"
         << counts[Counts::kFunctionsSecondaryNonLibrary] << " ("
         << counts[Counts::kFunctionsPrimaryLibrary] << "/"
         << counts[Counts::kFunctionsSecondaryLibrary] << ") ------------ "
         << "\n";
  for (FixedPoints::const_iterator i = fixed_points.begin();
       i != fixed_points.end(); ++i) {
    const Address primary_address = i->GetPrimary()->GetEntryPointAddress();
    const Address secondary_address = i->GetSecondary()->GetEntryPointAddress();
    const size_t basic_blocks1 =
        boost::num_vertices(i->GetPrimary()->GetGraph());
    const size_t basic_blocks2 =
        boost::num_vertices(i->GetSecondary()->GetGraph());
    result << FormatAddress(primary_address) << "\t"
           << FormatAddress(secondary_address) << "\t" << i->GetSimilarity()
           << "\t" << i->GetConfidence() << "\t"
           << i->GetPrimary()->GetMdIndex() << "\t"
           << i->GetSecondary()->GetMdIndex() << "\t"
           << i->GetPrimary()->IsLibrary() << "\t"
           << i->GetSecondary()->IsLibrary() << "\t" << i->GetMatchingStep()
           << "\t\"" << i->GetPrimary()->GetName() << "\"\t\""
           << i->GetSecondary()->GetName() << "\""
           << "\n"
           << std::dec << "\t" << i->GetBasicBlockFixedPoints().size() << "\t"
           << basic_blocks1 << "\t" << basic_blocks2 << "\n";
    for (BasicBlockFixedPoints::const_iterator j =
             i->GetBasicBlockFixedPoints().begin();
         j != i->GetBasicBlockFixedPoints().end(); ++j) {
      const size_t instructions1 =
          i->GetPrimary()->GetInstructionCount(j->GetPrimaryVertex());
      const size_t instructions2 =
          i->GetSecondary()->GetInstructionCount(j->GetSecondaryVertex());
      const InstructionMatches& matches = j->GetInstructionMatches();
      result << "\t"
             << FormatAddress(
                    i->GetPrimary()->GetAddress(j->GetPrimaryVertex()))
             << "\t"
             << FormatAddress(
                    i->GetSecondary()->GetAddress(j->GetSecondaryVertex()))
             << "\t" << j->GetMatchingStep() << "\n"
             << "\t\t" << std::dec << matches.size() << "\t" << instructions1
             << "\t" << instructions2 << "\n";
      for (InstructionMatches::const_iterator k = matches.begin();
           k != matches.end(); ++k) {
        result << "\t\t" << FormatAddress(k->first->GetAddress()) << "\t"
               << FormatAddress(k->second->GetAddress()) << "\n";
      }
    }
  }

  using IteratorPrimary =
      boost::transform_iterator<ProjectPrimary, FixedPoints::const_iterator>;
  FlowGraphs unmatched;
  std::set_symmetric_difference(
      flow_graphs1.begin(), flow_graphs1.end(),
      IteratorPrimary(fixed_points.begin(), ProjectPrimary()),
      IteratorPrimary(fixed_points.end(), ProjectPrimary()),
      std::inserter(unmatched, unmatched.begin()), SortByAddress());
  result << " --------- unmatched primary (" << std::dec << unmatched.size()
         << ") ------------ "
         << "\n";
  for (FlowGraphs::const_iterator i = unmatched.begin(); i != unmatched.end();
       ++i) {
    result << FormatAddress((*i)->GetEntryPointAddress()) << "\t"
           << (*i)->IsLibrary() << "\t" << (*i)->GetMdIndex() << "\t"
           << (*i)->GetName() << "\n";
  }

  using IteratorSecondary =
      boost::transform_iterator<ProjectSecondary, FixedPoints::const_iterator>;
  unmatched.clear();
  // Necessary because this set is sorted by primary flow_graph and we need it
  // by secondary now.
  FlowGraphs temp(IteratorSecondary(fixed_points.begin(), ProjectSecondary()),
                  IteratorSecondary(fixed_points.end(), ProjectSecondary()));
  std::set_symmetric_difference(
      flow_graphs2.begin(), flow_graphs2.end(), temp.begin(), temp.end(),
      std::inserter(unmatched, unmatched.begin()), SortByAddress());
  result << " --------- unmatched secondary (" << std::dec << unmatched.size()
         << ") ------------ "
         << "\n";
  for (FlowGraphs::const_iterator i = unmatched.begin(); i != unmatched.end();
       ++i) {
    result << FormatAddress((*i)->GetEntryPointAddress()) << "\t"
           << (*i)->IsLibrary() << "\t" << (*i)->GetMdIndex() << "\t"
           << (*i)->GetName() << "\n";
  }
}

}  // namespace security::bindiff
