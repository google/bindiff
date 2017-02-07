#include "third_party/zynamics/bindiff/ida/visual_diff.h"

#include <algorithm>
#include <chrono>  // NOLINT
#include <iomanip>
#include <iterator>
#include <map>
#include <sstream>
#include <thread>  // NOLINT

#ifdef WIN32
#define _WIN32_WINNT 0x0501
#include <direct.h>
#else
#include <sys/sysinfo.h>
#endif

#include "base/logging.h"
#include "strings/strutil.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/matching.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/filesystem_util.h"
#undef min
#undef max

static const char kGuiJarName[] = "bindiff.jar";

#ifdef WIN32
// The JRE's registry key under HKEY_LOCAL_MACHINE
static const char kRegkeyHklmJreRoot[] =
    "SOFTWARE\\JavaSoft\\Java Runtime Environment";
// Minimum required version of the JRE
static const double kMinJavaVersion = 1.7;

bool RegQueryStringValue(HKEY key, const char* name, char* buffer,
                         int bufsize) {
  DWORD type;
  DWORD size;

  // Do we have a value of that name?
  if (RegQueryValueEx(key, name, 0, &type, 0, &size) != ERROR_SUCCESS) {
    return false;
  }

  // Is it the right type and not too large?
  if (type != REG_SZ || (size >= static_cast<unsigned int>(bufsize))) {
    return false;
  }

  // Finally read the string and return status
  return RegQueryValueEx(key, name, 0, 0,
                         reinterpret_cast<unsigned char*>(buffer),
                         &size) == ERROR_SUCCESS;
}

std::string GetJavaHomeDir() {
  HKEY key = 0;
  int wow64_flag = KEY_WOW64_64KEY;

  // Note: When running on 64-bit Windows, 32-bit processes (such as IDA) have
  //       an isolated view on the registry. This means that an installed
  //       64-bit JVM will not be found using default access flags. Thus, this
  //       function first tries to open the 64-bit view of the registry to
  //       determine where Java is installed. If it failes to detect a 64-bit
  //       JVM, the 32-bit view of the registry is searched.
  //       Saving the KEY_WOW64_64KEY access flag is necessary because
  //       applications are supposed to supply it in subsequent registry API
  //       calls.

  // Try 64-bit registry view first
  if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, kRegkeyHklmJreRoot, 0,
                   KEY_READ | wow64_flag, &key) != ERROR_SUCCESS) {
    wow64_flag = 0;
    // Try default registry view
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, kRegkeyHklmJreRoot, 0,
                     KEY_READ | wow64_flag, &key) != ERROR_SUCCESS) {
      // Still not found, return empty path
      return "";
    }
  }

  std::string result;
  char buffer[MAX_PATH];
  if (RegQueryStringValue(key, "CurrentVersion", buffer, MAX_PATH)) {
    const double cur_var(strtod(buffer, 0));
    HKEY subkey(0);
    if (cur_var >= kMinJavaVersion &&
        RegOpenKeyEx(key, buffer, 0, KEY_READ | wow64_flag, &subkey) ==
            ERROR_SUCCESS) {
      if (RegQueryStringValue(subkey, "JavaHome", buffer, MAX_PATH)) {
        result = std::string(buffer);
      }
      RegCloseKey(subkey);
    }
  }
  RegCloseKey(key);

  return result;
}
#endif

uint64_t GetPhysicalMemSize() {
#if defined(WIN32)
  MEMORYSTATUSEX mi;
  mi.dwLength = sizeof(MEMORYSTATUSEX);
  GlobalMemoryStatusEx(&mi);
  return mi.ullTotalPhys;
#elif defined(__APPLE__)
  uint64_t result;
  int param[2];
  param[0] = CTL_HW;
  param[1] = HW_MEMSIZE;
  size_t length = sizeof(uint64_t);
  sysctl(param, 2, &result, &length, nullptr, 0);
  return result;
#else
  struct sysinfo mi;
  sysinfo(&mi);
  return static_cast<uint64_t>(mi.totalram) * mi.mem_unit;
#endif
}

bool DoSendGuiMessageTCP(const std::string& server, const unsigned short port,
                         const std::string& arguments) {
#if 0
  // TODO(soerenme): Allow "server" to contain a hostname as well.
  boost::asio::ip::tcp::endpoint endpoint(
      boost::asio::ip::address_v4::from_string(server), port);

  boost::asio::io_service io_service;
  boost::asio::ip::tcp::socket io_socket(io_service);
  boost::system::error_code error = boost::asio::error::host_not_found;
  io_socket.connect(endpoint, error);

  uint32_t packet_size(arguments.size());
  std::string packet(reinterpret_cast<const uint8_t*>(&packet_size),
                     reinterpret_cast<const uint8_t*>(&packet_size) + 4);
  packet.append(arguments);
  boost::asio::write(io_socket,
                     boost::asio::buffer(packet),
                     boost::asio::transfer_all(), error);
  return !error;
#else
  // TODO(cblichmann): Implement the above using simple direct socket
  //                   programming.
  return false;
#endif
}

void DoStartGui(const std::string& gui_dir) {
  // This is not strictly correct: we allow specifying a server by IP address in
  // our config file. If we cannot reach it we launch BinDiff GUI locally...
  // This will be the most common setup by far, so I guess it's ok.

  extern XmlConfig g_config;
  std::vector<std::string> argv;
  std::string java_binary(g_config.ReadString("/BinDiffDeluxe/Gui/@java_binary", ""));
  if (!java_binary.empty()) {
    argv.push_back(java_binary);
  } else {
#ifdef WIN32
    std::string java_exe(GetJavaHomeDir());
    if (!java_exe.empty()) {
      StrAppend(&java_exe, kPathSeparator, "bin");
    }
    StrAppend(&java_exe, kPathSeparator, "javaw.exe");
    argv.push_back(java_exe);
#else
    // TODO(cblichmann): Check if we need to find the real binary path by
    //                   using readlink(). On some machines that seems to be
    //                   necessary to be able to start the Java binary.
    argv.push_back("java");
#endif
  }
  argv.push_back("-Xms128m");
#ifdef __APPLE__
  argv.push_back("-Xdock:name=BinDiff");
#endif

  // Read default max heap size from configuration.
  int config_max_heap_mb(g_config.ReadInt("/BinDiffDeluxe/Gui/@maxHeapSize", -1));

  // Set max heap size to 75% of available physical memory if unset. Note, when
  // using 32-bit Java on a 64-bit machine with more than 4GiB of RAM, the
  // calculated value will be too large. In this case, we'll simply try again
  // without setting any max heap size.
  uint64_t max_heap_mb(
      config_max_heap_mb > 0
          ? config_max_heap_mb
          : std::max(static_cast<uint64_t>(512),
                     (GetPhysicalMemSize() / 1024 / 1024) * 3 / 4));
  int max_heap_index = argv.size();
  argv.push_back("-Xmx" + std::to_string(max_heap_mb) + "m");

  argv.push_back("-jar");
  std::string jar_file(JoinPath(gui_dir, "bin", kGuiJarName));
  if (!FileExists(jar_file)) {
    throw std::runtime_error(
        StrCat("Cannot launch BinDiff user interface (JAR file '", jar_file,
               "' missing).")
            .c_str());
  }
  argv.push_back(jar_file);

  std::string status_message;
  if (!SpawnProcess(argv, false /* Wait */, &status_message)) {
    // Try again without the max heap size argument.
    argv.erase(argv.begin() + max_heap_index - 1);

    if (!SpawnProcess(argv, false /* Wait */, &status_message)) {
      throw std::runtime_error(
          ("Cannot launch BinDiff user interface (process creation failed). " +
           status_message).c_str());
    }
  }
}

bool SendGuiMessage(const int retries, const std::string& gui_dir,
                    const std::string& server, const unsigned short port,
                    const std::string& arguments,
                    void progress_callback(void)) {
  if (DoSendGuiMessageTCP(server, port, arguments)) {
    return true;
  }
  DoStartGui(gui_dir);

  for (int retry = 0; retry < retries * 10; ++retry) {
    if (DoSendGuiMessageTCP(server, port, arguments)) {
      return true;
    }

    std::this_thread::sleep_for(std::chrono::milliseconds(100));
    if (progress_callback) {
      progress_callback();
    }
  }
  return false;
}
