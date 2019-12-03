// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

// A class to convert and store a BinExport::Callgraph protocol buffer into a
// Boost compressed sparse row graph.

#ifndef READER_CALL_GRAPH_H_
#define READER_CALL_GRAPH_H_

#include <memory>
#include <string>

#include <boost/graph/compressed_sparse_row_graph.hpp>  // NOLINT

#include "base/integral_types.h"
#include "base/macros.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/types.h"

namespace security::binexport {

class CallGraph {
 public:
  CallGraph() = default;

  CallGraph(const CallGraph&) = delete;
  CallGraph& operator=(const CallGraph&) = delete;

  struct VertexProperty {
    Address address;             // Function address.
    std::string name;            // Function name.
    std::string demangled_name;  // Demangled function name.
    uint32_t flags;                // Function flags.
    std::string library_name;    // Library name.
    std::string module_name;     // Module name.

    VertexProperty(Address address, const std::string& name,
                   const std::string& demangled_name, uint32_t flags,
                   const std::string& library_name,
                   const std::string& module_name)
        : address(address),
          name(name),
          demangled_name(demangled_name),
          flags(flags),
          library_name(library_name),
          module_name(module_name) {}
    VertexProperty() : VertexProperty(0, "", "", 0, "", "") {}
  };

  enum {                            // vertex flags
    kVertexLibrary = 1 << 0,        // Library function.
    kVertexThunk = 1 << 1,          // Thunk function, e.g trampoline,
    kVertexImported = 1 << 2,       // Imported functions, e.g without code.
    kVertexInvalid = 1 << 3,        // Invalid function.
    kVertexName = 1 << 4,           // Has a non auto generated name.
    kVertexDemangledName = 1 << 5,  // Has a C++ demangled name.
  };

  using Graph = boost::compressed_sparse_row_graph<
      boost::bidirectionalS,  // Iterate in and out edges.
      VertexProperty,         // The information per vertex.
      boost::no_property,     // The information per edge.
      boost::no_property,     // Use no graph properties.
      uint32_t,                 // Index type for vertices.
      uint32_t>;                // Index type for edges.

  using Vertex = boost::graph_traits<Graph>::vertex_descriptor;
  using VertexIterator = boost::graph_traits<Graph>::vertex_iterator;
  using Edge = boost::graph_traits<Graph>::edge_descriptor;
  using EdgeIterator = boost::graph_traits<Graph>::edge_iterator;
  using OutEdgeIterator = boost::graph_traits<Graph>::out_edge_iterator;
  using InEdgeIterator = boost::graph_traits<Graph>::in_edge_iterator;
  using AdjacencyIterator = boost::graph_traits<Graph>::adjacency_iterator;

  // Factory method to read and initialize a call graph from a BinExport2
  // protocol buffer.
  static std::unique_ptr<CallGraph> FromBinExport2Proto(
      const BinExport2& proto);

  const Graph& graph() const { return graph_; }

  // Get the address of a vertex.
  Address GetAddress(Vertex vertex) const;

  // Returns true if the input corresponds to a valid non-library,
  // non-thunk, non-imported function. The overload taking an Address
  // CHECK-fails if the address does not correspond to a vertex in the
  // callgraph.
  bool IsValidEntryPoint(Address address) const;
  bool IsValidEntryPoint(Vertex vertex) const;

  // Get a vertex by its address.
  Vertex GetVertex(Address address) const;

 private:
  Graph graph_;
};

}  // namespace security::binexport

#endif  // READER_CALL_GRAPH_H_
