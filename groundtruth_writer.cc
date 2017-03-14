#include "third_party/zynamics/bindiff/groundtruth_writer.h"

#include <iomanip>

#include "third_party/zynamics/bindiff/differ.h"

GroundtruthWriter::GroundtruthWriter(const std::string& filename)
    : filename_(filename),
      fixed_point_infos_(nullptr),
      primary_(nullptr),
      secondary_(nullptr) {}

GroundtruthWriter::GroundtruthWriter(const std::string& filename,
                                     const FixedPointInfos& fixed_point_infos,
                                     const FlowGraphInfos& primary,
                                     const FlowGraphInfos& secondary)
    : filename_(filename),
      fixed_point_infos_(&fixed_point_infos),
      primary_(&primary),
      secondary_(&secondary) {}

void GroundtruthWriter::Write(const CallGraph& /* call_graph1 */,
                              const CallGraph& /* call_graph2 */,
                              const FlowGraphs& /* flow_graphs1 */,
                              const FlowGraphs& /* flow_graphs2 */,
                              const FixedPoints& fixed_points) {
  std::ofstream out_file(filename_.c_str());
  if (fixed_point_infos_) {
    // Note: we are not using the arguments here but instead rely on the stored
    // fixed point infos. This allows us to save from loaded results. But it is
    // not a very nice solution - we should instead load results fully and make
    // the two cases equivalent. This has not been done originally to allow
    // loading large results generated with the 64bit stand alone differ.
    for (FixedPointInfos::const_iterator i = fixed_point_infos_->begin();
         i != fixed_point_infos_->end(); ++i) {
      const FixedPointInfo& info = *i;
      out_file
          << std::hex << std::setfill('0') << std::setw(8)
          << info.primary << " "
          << std::hex << std::setfill('0') << std::setw(8)
          << info.secondary << " "
          << *primary_->find(info.primary)->second.name << " "
          << *secondary_->find(info.secondary)->second.name << "\n";
    }
  } else {
    for (FixedPoints::const_iterator i = fixed_points.begin();
         i != fixed_points.end(); ++i) {
      const FlowGraph* primary = i->GetPrimary();
      const FlowGraph* secondary = i->GetSecondary();
      out_file
          << std::hex << std::setfill('0') << std::setw(8)
          << primary->GetEntryPointAddress() << " "
          << std::hex << std::setfill('0') << std::setw(8)
          << secondary->GetEntryPointAddress() << " "
          << primary->GetName() << " "
          << secondary->GetName() << "\n";
    }
  }
}
