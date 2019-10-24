#ifndef VERSION_H_
#define VERSION_H_

namespace security::bindiff {

// The product name, "BinDiff"
extern const char* kBinDiffName;

// The BinDiff version, a single stringified integer
extern const char* kBinDiffRelease;

// Detailed version and build information:
// "N (Google internal, YYYYMMDD, debug build)"
extern const char* kBinDiffDetailedVersion;

// Full copyright string with current year
extern const char* kBinDiffCopyright;

}  // namespace security::bindiff

#endif  // VERSION_H_
