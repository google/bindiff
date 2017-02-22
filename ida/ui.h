#ifndef IDA_UI_H_
#define IDA_UI_H_

#include <string>

#include "base/integral_types.h"

// TODO(cblichmann): Should return uint32_t.
void HsvToRgb(double h, double s, double v,
              unsigned char& r,  // NOLINT(runtime/references)
              unsigned char& g,  // NOLINT(runtime/references)
              unsigned char& b   // NOLINT(runtime/references)
              );

void CopyToClipboard(const std::string& data);

#endif  // IDA_UI_H_
