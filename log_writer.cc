#include "third_party/zynamics/bindiff/log_writer.h"

#include <iomanip>
#include <memory>

#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/iterator/transform_iterator.hpp"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"

namespace {

struct ProjectPrimary : public std::unary_function<FixedPoint, FlowGraph*> {
  FlowGraph* operator()(const FixedPoint& fixed_point) const {
    return fixed_point.GetPrimary();
  }
};

struct ProjectSecondary : public std::unary_function<FixedPoint, FlowGraph*> {
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

  std::ofstream result_file(filename_.c_str());
  result_file.precision(16);
  result_file << call_graph1.GetFilename() << "\n" << call_graph2.GetFilename()
              << "\n"
              << "call graph1 MD index " << std::dec << call_graph1.GetMdIndex()
              << "\ncall graph2 MD index " << std::dec
              << call_graph2.GetMdIndex() << std::endl
              << std::dec << "\n --------- statistics ---------\n";
  for (Counts::const_iterator i = counts.begin(); i != counts.end(); ++i) {
    const std::string padding(60 - i->first.size(), '.');
    result_file << i->first << padding << ":" << std::setw(7) << i->second
                << "\n";
  }
  result_file << std::endl;
  for (Histogram::const_iterator i = histogram.begin(); i != histogram.end();
       ++i) {
    const std::string padding(60 - i->first.size(), '.');
    result_file << i->first << padding << ":" << std::setw(7) << i->second
                << "\n";
  }
  result_file << "\nsimilarity: "
              << GetSimilarityScore(call_graph1, call_graph2, histogram, counts)
              << "\nconfidence: " << GetConfidence(histogram, &confidences)
              << "\n\nindividual confidence values used: " << std::endl;
  for (Confidences::const_iterator i = confidences.begin();
       i != confidences.end(); ++i) {
    const std::string padding(60 - i->first.size(), '.');
    result_file << i->first << padding << ":" << std::setw(7)
                << std::setprecision(2) << i->second << "\n";
  }
  result_file << std::endl
              << " --------- matched " << std::dec << fixed_points.size()
              << " of "
              << counts.find("functions primary (non-library)")->second << "/"
              << counts.find("functions secondary (non-library)")->second
              << " (" << counts.find("functions primary (library)")->second
              << "/" << counts.find("functions secondary (library)")->second
              << ") ------------ " << std::endl;
  for (FixedPoints::const_iterator i = fixed_points.begin();
       i != fixed_points.end(); ++i) {
    const Address primary_address = i->GetPrimary()->GetEntryPointAddress();
    const Address secondary_address = i->GetSecondary()->GetEntryPointAddress();
    const size_t basic_blocks1 =
        boost::num_vertices(i->GetPrimary()->GetGraph());
    const size_t basic_blocks2 =
        boost::num_vertices(i->GetSecondary()->GetGraph());
    result_file << std::hex << primary_address << "\t" << std::hex
                << secondary_address << "\t" << i->GetSimilarity() << "\t"
                << i->GetConfidence() << "\t" << i->GetPrimary()->GetMdIndex()
                << "\t" << i->GetSecondary()->GetMdIndex() << "\t"
                << i->GetPrimary()->IsLibrary() << "\t"
                << i->GetSecondary()->IsLibrary() << "\t"
                << i->GetMatchingStep() << "\t\"" << i->GetPrimary()->GetName()
                << "\"\t\"" << i->GetSecondary()->GetName() << "\"" << std::endl
                << std::dec << "\t" << i->GetBasicBlockFixedPoints().size()
                << "\t" << basic_blocks1 << "\t" << basic_blocks2 << std::endl;
    for (BasicBlockFixedPoints::const_iterator j =
             i->GetBasicBlockFixedPoints().begin();
         j != i->GetBasicBlockFixedPoints().end(); ++j) {
      const size_t instructions1 =
          i->GetPrimary()->GetInstructionCount(j->GetPrimaryVertex());
      const size_t instructions2 =
          i->GetSecondary()->GetInstructionCount(j->GetSecondaryVertex());
      const InstructionMatches& matches = j->GetInstructionMatches();
      result_file << "\t" << std::hex
                  << i->GetPrimary()->GetAddress(j->GetPrimaryVertex()) << "\t"
                  << std::hex
                  << i->GetSecondary()->GetAddress(j->GetSecondaryVertex())
                  << "\t" << j->GetMatchingStep() << "\n"
                  << "\t\t" << std::dec << matches.size() << "\t"
                  << instructions1 << "\t" << instructions2 << std::endl;
      for (InstructionMatches::const_iterator k = matches.begin();
           k != matches.end(); ++k) {
        result_file << "\t\t" << std::hex << k->first->GetAddress() << "\t"
                    << std::hex << k->second->GetAddress() << std::endl;
      }
    }
  }

  typedef boost::transform_iterator<ProjectPrimary, FixedPoints::const_iterator>
      IteratorPrimary;
  FlowGraphs unmatched;
  std::set_symmetric_difference(
      flow_graphs1.begin(), flow_graphs1.end(),
      IteratorPrimary(fixed_points.begin(), ProjectPrimary()),
      IteratorPrimary(fixed_points.end(), ProjectPrimary()),
      std::inserter(unmatched, unmatched.begin()), SortByAddress());
  result_file << " --------- unmatched primary (" << std::dec
              << unmatched.size() << ") ------------ " << std::endl;
  for (FlowGraphs::const_iterator i = unmatched.begin(); i != unmatched.end();
       ++i) {
    result_file << std::hex << (*i)->GetEntryPointAddress() << "\t"
                << (*i)->IsLibrary() << "\t" << (*i)->GetMdIndex() << "\t"
                << (*i)->GetName() << std::endl;
  }

  typedef boost::transform_iterator<
      ProjectSecondary, FixedPoints::const_iterator> IteratorSecondary;
  unmatched.clear();
  // Necessary because this set is sorted by primary flow_graph and we need it
  // by secondary now.
  FlowGraphs temp(IteratorSecondary(fixed_points.begin(), ProjectSecondary()),
                  IteratorSecondary(fixed_points.end(), ProjectSecondary()));
  std::set_symmetric_difference(
      flow_graphs2.begin(), flow_graphs2.end(), temp.begin(), temp.end(),
      std::inserter(unmatched, unmatched.begin()), SortByAddress());
  result_file << " --------- unmatched secondary (" << std::dec
              << unmatched.size() << ") ------------ " << std::endl;
  for (FlowGraphs::const_iterator i = unmatched.begin(); i != unmatched.end();
       ++i) {
    result_file << std::hex << (*i)->GetEntryPointAddress() << "\t"
                << (*i)->IsLibrary() << "\t" << (*i)->GetMdIndex() << "\t"
                << (*i)->GetName() << std::endl;
  }
}
