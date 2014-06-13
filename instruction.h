#ifndef SRC_INSTRUCTION_H_
#define SRC_INSTRUCTION_H_

// TODO(cblichmann): Migrate to std::unordered_map, which is supported in the
//                   environments we care about.
#ifndef __APPLE__
#include <hash_map>
#include <hash_set>
#else
#include <ext/hash_map>
#include <ext/hash_set>
#endif
#include <list>
#include <set>
#include <vector>

#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/zylibcpp/utility/utility.h"

// Google3 puts these into the global namespace
#ifdef _WIN32
using std::hash_map;
using std::hash_set;
#else
#ifndef GOOGLE
using __gnu_cxx::hash_map;
using __gnu_cxx::hash_set;
#endif
#endif

class CacheEntry {
 public:
  CacheEntry(size_t prime, const std::string& operands);
  const std::string operands_;
  const size_t prime_;  // TODO(soerenme) make this fixed size!

 private:
  const CacheEntry& operator =(const CacheEntry&);
};

bool operator ==(const CacheEntry& one, const CacheEntry& two);

// MSVC provides a different interface to its hash_set/map implementations.
struct CacheEntryHash
#ifdef _WIN32
    : public std::hash_compare<CacheEntry, std::less<CacheEntry>>
#endif
      {
  std::size_t operator()(const CacheEntry& entry) const {
    // TODO(soerenme) Try adding prime_ and see whether having less collisions
    //     is worth the extra effort.
    return GetSdbmHash(entry.operands_);
  }

  bool operator()(const CacheEntry& left, const CacheEntry& right) const {
    return left.prime_ == right.prime_
        ? left.operands_ < right.operands_
        : left.prime_ < right.prime_;
  }
};

class Instruction {
 public:
  struct Cache {
    typedef hash_map<uint32_t, std::string> PrimeToMnemonic;
    typedef hash_set<CacheEntry, CacheEntryHash> CacheData;

    void Clear() {
      cache_.clear();
      prime_to_mnemonic_.clear();
    }

    CacheData cache_;
    PrimeToMnemonic prime_to_mnemonic_;
  };

  Instruction(Cache* cache, Address address, const std::string& mnemonic,
              uint32_t prime, const std::string& operands);
  uint32_t GetPrime() const;
  std::string GetOperandString() const;
  std::string GetMnemonic(const Cache* cache) const;
  Address GetAddress() const;

 private:
  const CacheEntry* cache_entry_;
  Address address_;
};

typedef std::vector<Instruction> Instructions;
typedef std::vector<std::pair<const Instruction*, const Instruction*> >
    InstructionMatches;

const Instruction* GetInstruction(const Instructions& instructions,
                                  const Address instruction_address);

void ComputeLcs(const Instructions::const_iterator& instructions1_begin,
                const Instructions::const_iterator& instructions1_end,
                const Instructions::const_iterator& instructions2_begin,
                const Instructions::const_iterator& instructions2_end,
                InstructionMatches& matches);

#endif  // SRC_INSTRUCTION_H_
