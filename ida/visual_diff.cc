#include "third_party/zynamics/bindiff/ida/visual_diff.h"

#ifdef _WIN32
#define _WIN32_WINNT 0x0501
#include <windows.h>  // NOLINT
#include <winsock2.h>
#include <ws2tcpip.h>
#else
#include <netdb.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>
#endif

#include <algorithm>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <iomanip>
#include <iterator>
#include <map>
#include <sstream>
#include <thread>  // NOLINT

#include "base/logging.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/time/clock.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/match_context.h"
#include "third_party/zynamics/bindiff/start_ui.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/util/filesystem.h"

namespace security {
namespace bindiff {

bool DoSendGuiMessageTCP(absl::string_view server, uint16_t port,
                         absl::string_view arguments) {
#ifdef _WIN32
  static int winsock_status = []() -> int {
    WSADATA wsa_data;
    return WSAStartup(MAKEWORD(2, 2), &wsa_data);
  }();
  if (winsock_status != 0) {
    return false;
  }

  // Use the original BSD names for these.
  int (__stdcall *close)(SOCKET) = closesocket;
  auto write = [](SOCKET socket, const char* buf, int len) -> int {
    return send(socket, buf, len, /* flags = */ 0);
  };
#endif

  uint32_t packet_size(arguments.size());
  string packet(reinterpret_cast<const uint8_t*>(&packet_size),
                reinterpret_cast<const uint8_t*>(&packet_size) + 4);
  absl::StrAppend(&packet, arguments);

  struct addrinfo hints = {0};
  hints.ai_family = AF_UNSPEC;  // IPv4 or IPv6
  hints.ai_socktype = SOCK_STREAM;
  hints.ai_flags = AI_NUMERICSERV;
  hints.ai_protocol = IPPROTO_TCP;
  struct addrinfo* address_info = nullptr;
  auto err = getaddrinfo(string(server).c_str(), std::to_string(port).c_str(),
                         &hints, &address_info);
  if (err != 0) {
    // TODO(cblichmann): This function should return a not_absl::Status and use
    //                   gai_strerror(err).
    return false;
  }
  std::unique_ptr<struct addrinfo, decltype(&freeaddrinfo)>
      address_info_deleter(address_info, freeaddrinfo);

  int socket_fd = 0;
  bool connected = false;
  for (auto* r = address_info; r != nullptr; r = r->ai_next) {
    socket_fd = socket(r->ai_family, r->ai_socktype, r->ai_protocol);
    if (socket_fd == -1) {
      continue;
    }
    connected = connect(socket_fd, r->ai_addr, r->ai_addrlen) != -1;
    if (connected) {
      break;
    }
    close(socket_fd);
  }
  if (!connected) {
    return false;
  }

  bool success =
      write(socket_fd, packet.data(), packet.size()) == packet.size();
  close(socket_fd);
  return success;
}

bool SendGuiMessage(int retries, absl::string_view gui_dir,
                    absl::string_view server, uint16_t port,
                    absl::string_view arguments,
                    std::function<void()> callback) {
  if (DoSendGuiMessageTCP(server, port, arguments)) {
    return true;
  }
  const auto* config = GetConfig();
  not_absl::Status status = StartUiWithOptions(
      /*extra_args=*/{},
      StartUiOptions{}
          .set_java_binary(config->ReadString("/BinDiff/Gui/@java_binary", ""))
          .set_java_vm_options(
              config->ReadString("/BinDiff/Gui/@java_vm_options", ""))
          .set_max_heap_size_mb(
              config->ReadInt("/BinDiff/Gui/@maxHeapSize", -1))
          .set_gui_dir(config->ReadString("/BinDiff/Gui/@directory", "")));
  if (!status.ok()) {
    throw std::runtime_error{absl::StrCat(
        "Cannot launch BinDiff user interface. Process creation failed: ",
        status.message())};
  }

  for (int retry = 0; retry < retries * 10; ++retry) {
    if (DoSendGuiMessageTCP(server, port, arguments)) {
      return true;
    }

    absl::SleepFor(absl::Milliseconds(100));
    if (callback) {
      callback();
    }
  }
  return false;
}

}  // namespace bindiff
}  // namespace security
