// Copyright 2011-2022 Google LLC
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

#include "third_party/zynamics/binexport/reader/call_graph.h"

#include <algorithm>
#include <utility>
#include <vector>

#include "third_party/absl/log/check.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/reader/graph_utility.h"

namespace security::binexport {

CallGraph::Vertex CallGraph::GetVertex(Address address) const {
  return security::binexport::GetVertex(*this, address);
}

void AssignVertexProperties(
    const std::vector<CallGraph::VertexProperty>& vertex_properties,
    CallGraph::Graph* graph) {
  CallGraph::VertexIterator vertices_it;
  CallGraph::VertexIterator vertices_end;
  CallGraph::Graph& graph_ref = *graph;
  int j = 0;
  for (boost::tie(vertices_it, vertices_end) = boost::vertices(*graph);
       vertices_it != vertices_end; ++vertices_it, ++j) {
    graph_ref[*vertices_it] = vertex_properties[j];
  }
}

void VertexPropertyFromVertexProto(const BinExport2::CallGraph::Vertex& vertex,
                                   const BinExport2& proto,
                                   CallGraph::VertexProperty* vertex_property) {
  vertex_property->address = vertex.address();
  vertex_property->flags = 0;
  if (vertex.has_demangled_name()) {
    vertex_property->demangled_name = vertex.demangled_name();
    vertex_property->flags |= CallGraph::kVertexName;
    vertex_property->flags |= CallGraph::kVertexDemangledName;
  }
  if (vertex.has_mangled_name()) {
    vertex_property->name = vertex.mangled_name();
    vertex_property->flags |= CallGraph::kVertexName;
  }
  if (!(vertex_property->flags & CallGraph::kVertexName)) {
    vertex_property->name = absl::StrCat("sub_", absl::Hex(vertex.address()));
  }
  if (vertex.has_module_index()) {
    vertex_property->module_name = proto.module(vertex.module_index()).name();
  }
  switch (vertex.type()) {
    case BinExport2::CallGraph::Vertex::NORMAL:
      break;
    case BinExport2::CallGraph::Vertex::LIBRARY:
      vertex_property->flags |= CallGraph::kVertexLibrary;
      if (vertex.has_library_index()) {
        vertex_property->library_name =
            proto.library(vertex.library_index()).name();
      }
      break;
    case BinExport2::CallGraph::Vertex::THUNK:
      vertex_property->flags |= CallGraph::kVertexThunk;
      break;
    case BinExport2::CallGraph::Vertex::IMPORTED:
      vertex_property->flags |= CallGraph::kVertexImported;
      break;
    case BinExport2::CallGraph::Vertex::INVALID:
      vertex_property->flags |= CallGraph::kVertexInvalid;
      break;
  }
}

bool CallGraph::IsValidEntryPoint(Address address) const {
  return IsValidEntryPoint(GetVertex(address));
}

bool CallGraph::IsValidEntryPoint(Vertex vertex) const {
  constexpr uint32_t kValidFunctionMask =
      kVertexLibrary | kVertexThunk | kVertexImported | kVertexInvalid;
  if (vertex == VertexTypeTraits<Vertex>::kInvalidVertex) {
    return false;
  }
  const uint32_t function_flags = graph()[vertex].flags;
  return (function_flags & kValidFunctionMask) == 0;
}

std::unique_ptr<CallGraph> CallGraph::FromBinExport2Proto(
    const BinExport2& proto) {
  const BinExport2::CallGraph& call_graph_proto(proto.call_graph());
  std::vector<VertexProperty> vertex_properties;
  vertex_properties.reserve(call_graph_proto.vertex_size());
  std::vector<Address> vertex_addresses;
  vertex_addresses.reserve(call_graph_proto.vertex_size());

  for (const auto& vertex : call_graph_proto.vertex()) {
    VertexProperty vertex_property;
    VertexPropertyFromVertexProto(vertex, proto, &vertex_property);
    vertex_properties.push_back(vertex_property);
    vertex_addresses.push_back(vertex_property.address);
  }
  QCHECK(std::is_sorted(vertex_addresses.begin(), vertex_addresses.end()))
      << "CallGraph nodes not sorted by address";

  // Find corresponding edge index for source and target address.
  std::vector<std::pair<Graph::edges_size_type, Graph::edges_size_type>> edges;
  edges.reserve(call_graph_proto.edge_size());
  for (const auto& edge : call_graph_proto.edge()) {
    const Address source_address =
        call_graph_proto.vertex(edge.source_vertex_index()).address();
    const Address target_address =
        call_graph_proto.vertex(edge.target_vertex_index()).address();
    const auto source = std::lower_bound(
        vertex_addresses.begin(), vertex_addresses.end(), source_address);
    const auto target = std::lower_bound(
        vertex_addresses.begin(), vertex_addresses.end(), target_address);
    if (source != vertex_addresses.end() && target != vertex_addresses.end()) {
      edges.emplace_back(source - vertex_addresses.begin(),
                         target - vertex_addresses.begin());
    }
  }

  auto call_graph = absl::make_unique<CallGraph>();
  call_graph->graph_ =
      Graph(boost::edges_are_unsorted_multi_pass, edges.begin(), edges.end(),
            call_graph_proto.vertex_size());

  AssignVertexProperties(vertex_properties, &call_graph->graph_);
  return call_graph;
}

Address CallGraph::GetAddress(Vertex vertex) const {
  return graph_[vertex].address;
}

}  // namespace security::binexport
