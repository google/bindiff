#include "third_party/zynamics/bindiff/ida/ui.h"

#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
#define NOMINMAX
#include <windows.h>
#endif

#include <cmath>
#include <limits>

#include "third_party/zynamics/bindiff/utility.h"

void HsvToRgb(double h, double s, double v,
              unsigned char& r,  // NOLINT(runtime/references)
              unsigned char& g,  // NOLINT(runtime/references)
              unsigned char& b   // NOLINT(runtime/references)
              ) {
  if (std::fabs(s) <=
      std::numeric_limits<double>::epsilon()) {  // achromatic (gray)
    r = g = b = static_cast<unsigned char>(v * 255.0);
    return;
  }

  h /= 60.0;
  const int i = static_cast<int>(floor(h));
  const double f = h - i;
  const double p = v * (1 - s);
  const double q = v * (1 - s * f);
  const double t = v * (1 - s * (1 - f));

  switch (i) {
    case 0:
      r = static_cast<unsigned char>(v * 255);
      g = static_cast<unsigned char>(t * 255);
      b = static_cast<unsigned char>(p * 255);
      break;
    case 1:
      r = static_cast<unsigned char>(q * 255);
      g = static_cast<unsigned char>(v * 255);
      b = static_cast<unsigned char>(p * 255);
      break;
    case 2:
      r = static_cast<unsigned char>(p * 255);
      g = static_cast<unsigned char>(v * 255);
      b = static_cast<unsigned char>(t * 255);
      break;
    case 3:
      r = static_cast<unsigned char>(p * 255);
      g = static_cast<unsigned char>(q * 255);
      b = static_cast<unsigned char>(v * 255);
      break;
    case 4:
      r = static_cast<unsigned char>(t * 255);
      g = static_cast<unsigned char>(p * 255);
      b = static_cast<unsigned char>(v * 255);
      break;
    default:  // case 5:
      r = static_cast<unsigned char>(v * 255);
      g = static_cast<unsigned char>(p * 255);
      b = static_cast<unsigned char>(q * 255);
      break;
  }
}

void CopyToClipboard(const std::string& data) {
#ifdef WIN32
  if (!OpenClipboard(0)) {
    throw std::runtime_error(GetLastOsError());
  }
  struct ClipboardCloser {
    ~ClipboardCloser() { CloseClipboard(); }
  } deleter;

  if (!EmptyClipboard()) {
    throw std::runtime_error(GetLastOsError());
  }

  // std::strings are not required to be zero terminated, thus we add an extra
  // zero.
  HGLOBAL buffer_handle =
      GlobalAlloc(GMEM_MOVEABLE | GMEM_ZEROINIT, data.size() + 1);
  if (!buffer_handle) {
    throw std::runtime_error(GetLastOsError());
  }

  bool fail = true;
  char* buffer = static_cast<char*>(GlobalLock(buffer_handle));
  if (buffer) {
    memcpy(buffer, data.c_str(), data.size());
    if (GlobalUnlock(buffer) &&
        SetClipboardData(CF_TEXT, buffer_handle /* Transfer ownership */)) {
      fail = false;
    }
  }
  if (fail) {
    // Only free on failure, as SetClipboardData() takes ownership.
    GlobalFree(buffer_handle);
    throw std::runtime_error(GetLastOsError());
  }
#else
  // TODO(cblichmann): Implement copy to clipboard for Linux/macOS.
#endif
}
