# CMake build file for BinDiff. This file relies on the open source version of
# BinExport a lot.
cmake_minimum_required(VERSION 2.8 FATAL_ERROR)
project(bindiff C CXX)

# BinDiff version number
set(PROJECT_VERSION_MAJOR 4)
set(PROJECT_VERSION_MINOR 3)
set(PROJECT_VERSION_PATCH 0)

set(BinExport_SOURCE_DIR ${PROJECT_SOURCE_DIR}/../binexport)
list(APPEND CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR}
                              ${BinExport_SOURCE_DIR})
set(CMAKE_FIND_ROOT_PATH ${PROJECT_SOURCE_DIR}/third_party
                         ${BinExport_SOURCE_DIR}/third_party)

# Use a copy of a subset of Boost that ships with BinExport
set(Boost_NO_SYSTEM_PATHS, ON)
set(BOOST_ROOT ${BinExport_SOURCE_DIR}/third_party/boost_parts)
find_package(Boost 1.55 REQUIRED)

find_package(IdaSdk REQUIRED)

# BinExport ships with Protocol Buffers as a sub-module and we expect it to
# be built before
set(PROTOBUF_ROOT_FOLDER ${BinExport_SOURCE_DIR}/third_party/protobuf)
set(PROTOBUF_SRC_ROOT_FOLDER ${PROTOBUF_ROOT_FOLDER}/src)
set(PROTOBUF_INCLUDE_DIR ${PROTOBUF_SRC_ROOT_FOLDER})
if(UNIX)
  set(PROTOBUF_PROTOC_EXECUTABLE ${PROTOBUF_SRC_ROOT_FOLDER}/protoc)
  set(PROTOBUF_LIBRARY ${PROTOBUF_SRC_ROOT_FOLDER}/.libs/libprotobuf.a)
else()
  set(PROTOBUF_PROTOC_EXECUTABLE
      ${PROTOBUF_ROOT_FOLDER}/build_msvc/Release/protoc.exe)
  set(PROTOBUF_LIBRARY
      ${PROTOBUF_ROOT_FOLDER}/build_msvc/Release/libprotobuf.lib)
endif()
set(PROTOBUF_LIBRARY_DEBUG ${PROTOBUF_LIBRARY})
find_package(Protobuf 3.0 REQUIRED)

include_directories(${PROJECT_SOURCE_DIR}
                    ${BinExport_SOURCE_DIR} # Use BinExport infrastructure
                    ${Boost_INCLUDE_DIR}
                    ${IdaSdk_INCLUDE_DIRS}
                    ${PROTOBUF_INCLUDE_DIRS}
                    ${PROTOBUF_SRC_ROOT_FOLDER})

configure_file(version.h.in version.h ESCAPE_QUOTES @ONLY)

if(NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
  add_definitions(-DNDEBUG)
endif()

if(UNIX)
  add_compile_options(--std=c++11 -Wno-deprecated)
  if(NOT COMPILE_64BIT)
    add_compile_options(-m32)
    set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -m32")
  endif()
elseif(WIN32)
  # TODO(cblichmann): Configure compiler warnings for Windows
  if(MSVC)
    # Use the static runtime
    foreach(flag_var CMAKE_CXX_FLAGS
                     CMAKE_CXX_FLAGS_DEBUG
                     CMAKE_CXX_FLAGS_RELEASE
                     CMAKE_CXX_FLAGS_MINSIZEREL
                     CMAKE_CXX_FLAGS_RELWITHDEBINFO)
      if(${flag_var} MATCHES "/MD")
        string(REGEX REPLACE "/MD" "/MT" ${flag_var} "${${flag_var}}")
      endif()
      if(${flag_var} MATCHES "/MDd")
        string(REGEX REPLACE "/MDd" "/MTd" ${flag_var} "${${flag_var}}")
      endif()
    endforeach(flag_var)
  endif()
endif()

# Sources shared by the differ and the IDA plugin.
set(Shared_SRCS call_graph.cc
# TODO(cblichmann): Work in progress
#                call_graph_matching.cc
#                change_classifier.cc
#                comments.cc
#                database_writer.cc
#                differ.cc
#                fixed_points.cc
#                flow_graph.cc
#                flow_graph_matching.cc
#                fortknox_writer.cc
#                instruction.cc
#                log.cc
#                log_writer.cc
#                matching.cc
#                prime_signature.cc
#                reader.cc
#                sqlite.cc
#                utility.cc
#                writer.cc
                ${BinExport_SOURCE_DIR}/binexport.pb.h
                ${BinExport_SOURCE_DIR}/binexport2.pb.h
)

if(NOT COMPILE_64BIT)
  add_ida_plugin(zynamics_bindiff_4_3 EA64
                 ${Shared_SRCS})

  set(BinDiff_LIBRARIES ${IdaSdk_LIBRARIES}
                        ${PROTOBUF_LIBRARIES})
  if(APPLE)
    target_link_libraries(zynamics_bindiff_4_3.pmc ${BinDiff_LIBRARIES})
    target_link_libraries(zynamics_bindiff_4_3.pmc64 ${BinDiff_LIBRARIES})
  elseif(UNIX)
    target_link_libraries(zynamics_bindiff_4_3.plx ${BinDiff_LIBRARIES})
    target_link_libraries(zynamics_bindiff_4_3.plx64 ${BinDiff_LIBRARIES})
  elseif(WIN32)
    list(APPEND BinDiff_LIBRARIES shlwapi.lib)
    target_link_libraries(zynamics_bindiff_4_3.plw ${BinDiff_LIBRARIES})
    target_link_libraries(zynamics_bindiff_4_3.p64 ${BinDiff_LIBRARIES})
  endif()
endif()
