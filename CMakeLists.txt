# CMake build file for BinDiff. This file relies on the open source version of
# BinExport a lot.
cmake_minimum_required(VERSION 2.8 FATAL_ERROR)
project(bindiff C CXX)

set(BinExport_DIR ${PROJECT_SOURCE_DIR}/../binexport)
set(Google3_DIR ${PROJECT_SOURCE_DIR}/../../..)
set(ThirdParty_DIR ${PROJECT_SOURCE_DIR}/../../../third_party)

list(APPEND CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR} ${BinExport_DIR})
include(ExternalProject)
include(CompileOptions)
include(ExternalGFlags)
include(ExternalGTest)
include(ExternalOpenSSL)
include(ExternalProtobuf)
include(ExternalSQLite)
include(ExternalTinyXPath)

# BinDiff version number
set(PROJECT_VERSION_MAJOR 4)
set(PROJECT_VERSION_MINOR 3)
set(PROJECT_VERSION_PATCH 0)

# Use a copy of a subset of Boost that ships with BinExport
set(Boost_NO_SYSTEM_PATHS ON)
set(BOOST_ROOT ${BinExport_DIR}/third_party/boost_parts)
find_package(Boost 1.55 REQUIRED)

find_package(IdaSdk REQUIRED)

# Make Google-style includes work
file(MAKE_DIRECTORY ${PROJECT_BINARY_DIR}/third_party/zynamics/binexport)
if(UNIX)
  execute_process(COMMAND
    ${CMAKE_COMMAND} -E create_symlink ../../..
    ${PROJECT_BINARY_DIR}/third_party/zynamics/bindiff)
elseif(WIN32)
  file(TO_NATIVE_PATH
    ${PROJECT_SOURCE_DIR}/third_party/zynamics/bindiff _bd_from)
  file(TO_NATIVE_PATH ${PROJECT_BINARY_DIR} _bd_to)
  execute_process(COMMAND $ENV{ComSpec} /c mklink /J ${_bd_from} ${_bd_to}
    ERROR_QUIET)
endif()

include_directories(${PROJECT_SOURCE_DIR}
                    ${PROJECT_BINARY_DIR}  # To find generated protobufs
                    ${BinExport_DIR}       # Use BinExport infrastructure
                    ${Boost_INCLUDE_DIR}
                    ${GFLAGS_INCLUDE_DIRS}
                    ${IdaSdk_INCLUDE_DIRS}
                    ${OPENSSL_INCLUDE_DIR}
                    ${PROTOBUF_INCLUDE_DIRS}
                    ${PROTOBUF_SRC_ROOT_FOLDER}
                    ${Google3_DIR})

configure_file(version.h.in version.h ESCAPE_QUOTES @ONLY)

set(BinExport_Proto_SRCS ${BinExport_DIR}/binexport2.proto)
PROTOBUF_GENERATE_CPP(PROTO_SRCS PROTO_HDRS ${BinExport_Proto_SRCS})
add_library(binexport_shared STATIC ${PROTO_SRCS}
                                    ${BinExport_DIR}/filesystem_util.cc
                                    ${BinExport_DIR}/filesystem_util.h
                                    ${BinExport_DIR}/hash.cc
                                    ${BinExport_DIR}/hash.h
                                    ${BinExport_DIR}/hex_codec.cc
                                    ${BinExport_DIR}/hex_codec.h)
set(binexport_shared_LIBRARIES ${OPENSSL_LIBRARIES}
                               ${PROTOBUF_LIBRARIES})
if(WIN32)
  list(APPEND binexport_shared_LIBRARIES shlwapi.lib)
endif()
target_link_libraries(binexport_shared ${binexport_shared_LIBRARIES})
add_custom_command(TARGET binexport_shared POST_BUILD
  COMMAND ${CMAKE_COMMAND} -E copy ${PROJECT_BINARY_DIR}/binexport2.pb.h
  ${PROJECT_BINARY_DIR}/third_party/zynamics/binexport/binexport2.pb.h)

# Sources shared by the differ and the IDA plugin.
add_library(bindiff STATIC call_graph.cc
                           call_graph.h
                           call_graph_matching.cc
                           call_graph_matching.h
                           change_classifier.cc
                           change_classifier.h
                           comments.cc
                           comments.h
                           database_writer.cc
                           database_writer.h
                           differ.cc
                           differ.h
                           fixed_points.cc
                           fixed_points.h
                           flow_graph.cc
                           flow_graph.h
                           flow_graph_matching.cc
                           flow_graph_matching.h
                           fortknox_writer.cc
                           fortknox_writer.h
                           instruction.cc
                           instruction.h
                           log_writer.cc
                           log_writer.cc
                           matching.cc
                           matching.h
                           prime_signature.cc
                           prime_signature.h
                           reader.cc
                           reader.h
                           sqlite.cc
                           sqlite.h
                           utility.cc
                           utility.h
                           writer.cc
                           writer.h
                           xmlconfig.cc
                           xmlconfig.h)
target_link_libraries(bindiff binexport_shared
                              sqlite
                              tinyxpath)

if(NOT COMPILE_64BIT)
  add_library(binexport_plugin_shared STATIC ${BinExport_DIR}/ida/digest.cc
                                             ${BinExport_DIR}/ida/digest.h
                                             ${BinExport_DIR}/ida/log.cc
                                             ${BinExport_DIR}/ida/log.h
                                             ${BinExport_DIR}/ida/ui.cc
                                             ${BinExport_DIR}/ida/ui.h)
  target_compile_definitions(binexport_plugin_shared PUBLIC
                             ${IdaSdk_PLATFORM}
                             __IDP__
                             USE_DANGEROUS_FUNCTIONS
                             USE_STANDARD_FILE_FUNCTIONS)
  target_link_libraries(binexport_plugin_shared ${OPENSSL_LIBRARIES}
                                                ${PROTOBUF_LIBRARIES})

  set(_bd_plugin_name
    zynamics_bindiff_${PROJECT_VERSION_MAJOR}_${PROJECT_VERSION_MINOR})
  add_ida_plugin(${_bd_plugin_name} EA64 ida/main_plugin.cc
                                         ida/names.cc
                                         ida/names.h
                                         ida/results.cc
                                         ida/results.h
                                         ida/ui.cc
                                         ida/ui.h
                                         ida/visual_diff.cc
                                         ida/visual_diff.h)
  set(Plugin_LIBRARIES binexport_plugin_shared
                       bindiff
                       ${IdaSdk_LIBRARIES})
  if(UNIX)
    list(APPEND Plugin_LIBRARIES pthread)
  elseif(WIN32)
    list(APPEND Plugin_LIBRARIES ws2_32.lib)
  endif()
  target_link_libraries(${_bd_plugin_name}${_plx} ${Plugin_LIBRARIES})
  target_link_libraries(${_bd_plugin_name}${_plx64} ${Plugin_LIBRARIES})
endif()

add_executable(differ main_portable.cc)
set(BinDiff_LIBRARIES bindiff
                      ${GFLAGS_LIBRARIES})
if(UNIX)
  list(APPEND BinDiff_LIBRARIES pthread
                                dl)
endif()
target_link_libraries(differ ${BinDiff_LIBRARIES})
