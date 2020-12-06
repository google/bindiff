// Copyright 2011-2020 Google LLC
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

// This file is auto-generated. To regenerate, run the "regenerate-api-stubs.sh"
// script.

#include <cstddef>
#include <cstdint>

#define BINARYNINJACORE_LIBRARY
// clang-format off
#include "binaryninjaapi.h"  // NOLINT
// clang-format on

extern "C" {
BINARYNINJACOREAPI char* BNAllocString(const char* contents) { return {}; }
BINARYNINJACOREAPI void BNFreeString(char* str) {}
BINARYNINJACOREAPI char** BNAllocStringList(const char** contents,
                                            size_t size) {
  return {};
}
BINARYNINJACOREAPI void BNFreeStringList(char** strs, size_t count) {}
BINARYNINJACOREAPI void BNShutdown(void) {}
BINARYNINJACOREAPI bool BNIsShutdownRequested(void) { return {}; }
BINARYNINJACOREAPI char* BNGetVersionString(void) { return {}; }
BINARYNINJACOREAPI uint32_t BNGetBuildId(void) { return {}; }
BINARYNINJACOREAPI char* BNGetSerialNumber(void) { return {}; }
BINARYNINJACOREAPI uint64_t BNGetLicenseExpirationTime(void) { return {}; }
BINARYNINJACOREAPI bool BNIsLicenseValidated(void) { return {}; }
BINARYNINJACOREAPI char* BNGetLicensedUserEmail(void) { return {}; }
BINARYNINJACOREAPI char* BNGetProduct(void) { return {}; }
BINARYNINJACOREAPI char* BNGetProductType(void) { return {}; }
BINARYNINJACOREAPI int BNGetLicenseCount(void) { return {}; }
BINARYNINJACOREAPI bool BNIsUIEnabled(void) { return {}; }
BINARYNINJACOREAPI void BNSetLicense(const char* licenseData) {}
BINARYNINJACOREAPI void BNRegisterObjectDestructionCallbacks(
    BNObjectDestructionCallbacks* callbacks) {}
BINARYNINJACOREAPI void BNUnregisterObjectDestructionCallbacks(
    BNObjectDestructionCallbacks* callbacks) {}
BINARYNINJACOREAPI char* BNGetUniqueIdentifierString(void) { return {}; }
BINARYNINJACOREAPI bool BNInitPlugins(bool allowUserPlugins) { return {}; }
BINARYNINJACOREAPI bool BNInitCorePlugins(void) { return {}; }
BINARYNINJACOREAPI void BNDisablePlugins(void) {}
BINARYNINJACOREAPI bool BNIsPluginsEnabled(void) { return {}; }
BINARYNINJACOREAPI void BNInitUserPlugins(void) {}
BINARYNINJACOREAPI void BNInitRepoPlugins(void) {}
BINARYNINJACOREAPI char* BNGetInstallDirectory(void) { return {}; }
BINARYNINJACOREAPI char* BNGetBundledPluginDirectory(void) { return {}; }
BINARYNINJACOREAPI void BNSetBundledPluginDirectory(const char* path) {}
BINARYNINJACOREAPI char* BNGetUserDirectory(void) { return {}; }
BINARYNINJACOREAPI char* BNGetUserPluginDirectory(void) { return {}; }
BINARYNINJACOREAPI char* BNGetRepositoriesDirectory(void) { return {}; }
BINARYNINJACOREAPI char* BNGetSettingsFileName(void) { return {}; }
BINARYNINJACOREAPI void BNSaveLastRun(void) {}
BINARYNINJACOREAPI char* BNGetPathRelativeToBundledPluginDirectory(
    const char* path) {
  return {};
}
BINARYNINJACOREAPI char* BNGetPathRelativeToUserPluginDirectory(
    const char* path) {
  return {};
}
BINARYNINJACOREAPI char* BNGetPathRelativeToUserDirectory(const char* path) {
  return {};
}
BINARYNINJACOREAPI bool BNExecuteWorkerProcess(
    const char* path, const char* args[], BNDataBuffer* input, char** output,
    char** error, bool stdoutIsText, bool stderrIsText) {
  return {};
}
BINARYNINJACOREAPI void BNSetCurrentPluginLoadOrder(BNPluginLoadOrder order) {}
BINARYNINJACOREAPI void BNAddRequiredPluginDependency(const char* name) {}
BINARYNINJACOREAPI void BNAddOptionalPluginDependency(const char* name) {}
BINARYNINJACOREAPI void BNLog(BNLogLevel level, const char* fmt, ...) {}
BINARYNINJACOREAPI void BNLogDebug(const char* fmt, ...) {}
BINARYNINJACOREAPI void BNLogInfo(const char* fmt, ...) {}
BINARYNINJACOREAPI void BNLogWarn(const char* fmt, ...) {}
BINARYNINJACOREAPI void BNLogError(const char* fmt, ...) {}
BINARYNINJACOREAPI void BNLogAlert(const char* fmt, ...) {}
BINARYNINJACOREAPI void BNLogString(BNLogLevel level, const char* str) {}
BINARYNINJACOREAPI void BNRegisterLogListener(BNLogListener* listener) {}
BINARYNINJACOREAPI void BNUnregisterLogListener(BNLogListener* listener) {}
BINARYNINJACOREAPI void BNUpdateLogListeners(void) {}
BINARYNINJACOREAPI void BNLogToStdout(BNLogLevel minimumLevel) {}
BINARYNINJACOREAPI void BNLogToStderr(BNLogLevel minimumLevel) {}
BINARYNINJACOREAPI bool BNLogToFile(BNLogLevel minimumLevel, const char* path,
                                    bool append) {
  return {};
}
BINARYNINJACOREAPI void BNCloseLogs(void) {}
BINARYNINJACOREAPI BNTemporaryFile* BNCreateTemporaryFile(void) { return {}; }
BINARYNINJACOREAPI BNTemporaryFile* BNCreateTemporaryFileWithContents(
    BNDataBuffer* data) {
  return {};
}
BINARYNINJACOREAPI BNTemporaryFile* BNNewTemporaryFileReference(
    BNTemporaryFile* file) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTemporaryFile(BNTemporaryFile* file) {}
BINARYNINJACOREAPI char* BNGetTemporaryFilePath(BNTemporaryFile* file) {
  return {};
}
BINARYNINJACOREAPI BNDataBuffer* BNGetTemporaryFileContents(
    BNTemporaryFile* file) {
  return {};
}
BINARYNINJACOREAPI BNDataBuffer* BNCreateDataBuffer(const void* data,
                                                    size_t len) {
  return {};
}
BINARYNINJACOREAPI BNDataBuffer* BNDuplicateDataBuffer(BNDataBuffer* buf) {
  return {};
}
BINARYNINJACOREAPI void BNFreeDataBuffer(BNDataBuffer* buf) {}
BINARYNINJACOREAPI void* BNGetDataBufferContents(BNDataBuffer* buf) {
  return {};
}
BINARYNINJACOREAPI void* BNGetDataBufferContentsAt(BNDataBuffer* buf,
                                                   size_t offset) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetDataBufferLength(BNDataBuffer* buf) {
  return {};
}
BINARYNINJACOREAPI BNDataBuffer* BNGetDataBufferSlice(BNDataBuffer* buf,
                                                      size_t start,
                                                      size_t len) {
  return {};
}
BINARYNINJACOREAPI void BNSetDataBufferLength(BNDataBuffer* buf, size_t len) {}
BINARYNINJACOREAPI void BNClearDataBuffer(BNDataBuffer* buf) {}
BINARYNINJACOREAPI void BNSetDataBufferContents(BNDataBuffer* buf, void* data,
                                                size_t len) {}
BINARYNINJACOREAPI void BNAssignDataBuffer(BNDataBuffer* dest,
                                           BNDataBuffer* src) {}
BINARYNINJACOREAPI void BNAppendDataBuffer(BNDataBuffer* dest,
                                           BNDataBuffer* src) {}
BINARYNINJACOREAPI void BNAppendDataBufferContents(BNDataBuffer* dest,
                                                   const void* src,
                                                   size_t len) {}
BINARYNINJACOREAPI uint8_t BNGetDataBufferByte(BNDataBuffer* buf,
                                               size_t offset) {
  return {};
}
BINARYNINJACOREAPI void BNSetDataBufferByte(BNDataBuffer* buf, size_t offset,
                                            uint8_t val) {}
BINARYNINJACOREAPI char* BNDataBufferToEscapedString(BNDataBuffer* buf) {
  return {};
}
BINARYNINJACOREAPI BNDataBuffer* BNDecodeEscapedString(const char* str) {
  return {};
}
BINARYNINJACOREAPI char* BNDataBufferToBase64(BNDataBuffer* buf) { return {}; }
BINARYNINJACOREAPI BNDataBuffer* BNDecodeBase64(const char* str) { return {}; }
BINARYNINJACOREAPI BNDataBuffer* BNZlibCompress(BNDataBuffer* buf) {
  return {};
}
BINARYNINJACOREAPI BNDataBuffer* BNZlibDecompress(BNDataBuffer* buf) {
  return {};
}
BINARYNINJACOREAPI BNSaveSettings* BNCreateSaveSettings(void) { return {}; }
BINARYNINJACOREAPI BNSaveSettings* BNNewSaveSettingsReference(
    BNSaveSettings* settings) {
  return {};
}
BINARYNINJACOREAPI void BNFreeSaveSettings(BNSaveSettings* settings) {}
BINARYNINJACOREAPI bool BNIsSaveSettingsOptionSet(BNSaveSettings* settings,
                                                  BNSaveOption option) {
  return {};
}
BINARYNINJACOREAPI void BNSetSaveSettingsOption(BNSaveSettings* settings,
                                                BNSaveOption option,
                                                bool state) {}
BINARYNINJACOREAPI BNFileMetadata* BNCreateFileMetadata(void) { return {}; }
BINARYNINJACOREAPI BNFileMetadata* BNNewFileReference(BNFileMetadata* file) {
  return {};
}
BINARYNINJACOREAPI void BNFreeFileMetadata(BNFileMetadata* file) {}
BINARYNINJACOREAPI void BNCloseFile(BNFileMetadata* file) {}
BINARYNINJACOREAPI void BNSetFileMetadataNavigationHandler(
    BNFileMetadata* file, BNNavigationHandler* handler) {}
BINARYNINJACOREAPI bool BNIsFileModified(BNFileMetadata* file) { return {}; }
BINARYNINJACOREAPI bool BNIsAnalysisChanged(BNFileMetadata* file) { return {}; }
BINARYNINJACOREAPI void BNMarkFileModified(BNFileMetadata* file) {}
BINARYNINJACOREAPI void BNMarkFileSaved(BNFileMetadata* file) {}
BINARYNINJACOREAPI bool BNIsBackedByDatabase(BNFileMetadata* file) {
  return {};
}
BINARYNINJACOREAPI bool BNCreateDatabase(BNBinaryView* data, const char* path,
                                         BNSaveSettings* settings) {
  return {};
}
BINARYNINJACOREAPI bool BNCreateDatabaseWithProgress(
    BNBinaryView* data, const char* path, void* ctxt,
    void (*progress)(void* ctxt, size_t progress, size_t total),
    BNSaveSettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNOpenExistingDatabase(BNFileMetadata* file,
                                                        const char* path) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNOpenExistingDatabaseWithProgress(
    BNFileMetadata* file, const char* path, void* ctxt,
    void (*progress)(void* ctxt, size_t progress, size_t total)) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNOpenDatabaseForConfiguration(
    BNFileMetadata* file, const char* path) {
  return {};
}
BINARYNINJACOREAPI bool BNSaveAutoSnapshot(BNBinaryView* data,
                                           BNSaveSettings* settings) {
  return {};
}
BINARYNINJACOREAPI bool BNSaveAutoSnapshotWithProgress(
    BNBinaryView* data, void* ctxt,
    void (*progress)(void* ctxt, size_t progress, size_t total),
    BNSaveSettings* settings) {
  return {};
}
BINARYNINJACOREAPI bool BNRebase(BNBinaryView* data, uint64_t address) {
  return {};
}
BINARYNINJACOREAPI bool BNRebaseWithProgress(
    BNBinaryView* data, uint64_t address, void* ctxt,
    void (*progress)(void* ctxt, size_t progress, size_t total)) {
  return {};
}
BINARYNINJACOREAPI BNMergeResult
BNMergeUserAnalysis(BNFileMetadata* file, const char* name, void* ctxt,
                    void (*progress)(void* ctxt, size_t progress, size_t total),
                    char** excludedHashes, size_t excludedHashesCount) {
  return {};
}
BINARYNINJACOREAPI char* BNGetOriginalFilename(BNFileMetadata* file) {
  return {};
}
BINARYNINJACOREAPI void BNSetOriginalFilename(BNFileMetadata* file,
                                              const char* name) {}
BINARYNINJACOREAPI char* BNGetFilename(BNFileMetadata* file) { return {}; }
BINARYNINJACOREAPI void BNSetFilename(BNFileMetadata* file, const char* name) {}
BINARYNINJACOREAPI void BNBeginUndoActions(BNFileMetadata* file) {}
BINARYNINJACOREAPI void BNCommitUndoActions(BNFileMetadata* file) {}
BINARYNINJACOREAPI bool BNUndo(BNFileMetadata* file) { return {}; }
BINARYNINJACOREAPI bool BNRedo(BNFileMetadata* file) { return {}; }
BINARYNINJACOREAPI BNUndoEntry* BNGetUndoEntries(BNFileMetadata* file,
                                                 size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeUndoEntries(BNUndoEntry* entries, size_t count) {}
BINARYNINJACOREAPI BNUser* BNNewUserReference(BNUser* user) { return {}; }
BINARYNINJACOREAPI void BNFreeUser(BNUser* user) {}
BINARYNINJACOREAPI BNUser** BNGetUsers(BNFileMetadata* file, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeUserList(BNUser** users, size_t count) {}
BINARYNINJACOREAPI char* BNGetUserName(BNUser* user) { return {}; }
BINARYNINJACOREAPI char* BNGetUserEmail(BNUser* user) { return {}; }
BINARYNINJACOREAPI char* BNGetUserId(BNUser* user) { return {}; }
BINARYNINJACOREAPI char* BNGetCurrentView(BNFileMetadata* file) { return {}; }
BINARYNINJACOREAPI uint64_t BNGetCurrentOffset(BNFileMetadata* file) {
  return {};
}
BINARYNINJACOREAPI bool BNNavigate(BNFileMetadata* file, const char* view,
                                   uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNGetFileViewOfType(BNFileMetadata* file,
                                                     const char* name) {
  return {};
}
BINARYNINJACOREAPI char** BNGetExistingViews(BNFileMetadata* file,
                                             size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNIsSnapshotDataAppliedWithoutError(
    BNFileMetadata* view) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNNewViewReference(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI void BNFreeBinaryView(BNBinaryView* view) {}
BINARYNINJACOREAPI BNFileMetadata* BNGetFileForView(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI char* BNGetViewType(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI BNBinaryView* BNGetParentView(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI size_t BNReadViewData(BNBinaryView* view, void* dest,
                                         uint64_t offset, size_t len) {
  return {};
}
BINARYNINJACOREAPI BNDataBuffer* BNReadViewBuffer(BNBinaryView* view,
                                                  uint64_t offset, size_t len) {
  return {};
}
BINARYNINJACOREAPI size_t BNWriteViewData(BNBinaryView* view, uint64_t offset,
                                          const void* data, size_t len) {
  return {};
}
BINARYNINJACOREAPI size_t BNWriteViewBuffer(BNBinaryView* view, uint64_t offset,
                                            BNDataBuffer* data) {
  return {};
}
BINARYNINJACOREAPI size_t BNInsertViewData(BNBinaryView* view, uint64_t offset,
                                           const void* data, size_t len) {
  return {};
}
BINARYNINJACOREAPI size_t BNInsertViewBuffer(BNBinaryView* view,
                                             uint64_t offset,
                                             BNDataBuffer* data) {
  return {};
}
BINARYNINJACOREAPI size_t BNRemoveViewData(BNBinaryView* view, uint64_t offset,
                                           uint64_t len) {
  return {};
}
BINARYNINJACOREAPI void BNNotifyDataWritten(BNBinaryView* view, uint64_t offset,
                                            size_t len) {}
BINARYNINJACOREAPI void BNNotifyDataInserted(BNBinaryView* view,
                                             uint64_t offset, size_t len) {}
BINARYNINJACOREAPI void BNNotifyDataRemoved(BNBinaryView* view, uint64_t offset,
                                            uint64_t len) {}
BINARYNINJACOREAPI size_t BNGetEntropy(BNBinaryView* view, uint64_t offset,
                                       size_t len, size_t blockSize,
                                       float* result) {
  return {};
}
BINARYNINJACOREAPI BNModificationStatus BNGetModification(BNBinaryView* view,
                                                          uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetModificationArray(BNBinaryView* view,
                                                 uint64_t offset,
                                                 BNModificationStatus* result,
                                                 size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNIsValidOffset(BNBinaryView* view, uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI bool BNIsOffsetReadable(BNBinaryView* view,
                                           uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI bool BNIsOffsetWritable(BNBinaryView* view,
                                           uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI bool BNIsOffsetExecutable(BNBinaryView* view,
                                             uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI bool BNIsOffsetBackedByFile(BNBinaryView* view,
                                               uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI bool BNIsOffsetCodeSemantics(BNBinaryView* view,
                                                uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI bool BNIsOffsetExternSemantics(BNBinaryView* view,
                                                  uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI bool BNIsOffsetWritableSemantics(BNBinaryView* view,
                                                    uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetNextValidOffset(BNBinaryView* view,
                                                 uint64_t offset) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetStartOffset(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI uint64_t BNGetEndOffset(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI uint64_t BNGetViewLength(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI uint64_t BNGetEntryPoint(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI BNArchitecture* BNGetDefaultArchitecture(
    BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI void BNSetDefaultArchitecture(BNBinaryView* view,
                                                 BNArchitecture* arch) {}
BINARYNINJACOREAPI BNPlatform* BNGetDefaultPlatform(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI void BNSetDefaultPlatform(BNBinaryView* view,
                                             BNPlatform* platform) {}
BINARYNINJACOREAPI BNEndianness BNGetDefaultEndianness(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI bool BNIsRelocatable(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI size_t BNGetViewAddressSize(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI bool BNIsViewModified(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI bool BNIsExecutableView(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI bool BNSaveToFile(BNBinaryView* view, BNFileAccessor* file) {
  return {};
}
BINARYNINJACOREAPI bool BNSaveToFilename(BNBinaryView* view,
                                         const char* filename) {
  return {};
}
BINARYNINJACOREAPI void BNDefineRelocation(BNBinaryView* view,
                                           BNArchitecture* arch,
                                           BNRelocationInfo* info,
                                           uint64_t target, uint64_t reloc) {}
BINARYNINJACOREAPI void BNDefineSymbolRelocation(BNBinaryView* view,
                                                 BNArchitecture* arch,
                                                 BNRelocationInfo* info,
                                                 BNSymbol* target,
                                                 uint64_t reloc) {}
BINARYNINJACOREAPI BNRange* BNGetRelocationRanges(BNBinaryView* segment,
                                                  size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNRange* BNGetRelocationRangesAtAddress(
    BNBinaryView* segment, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterDataNotification(
    BNBinaryView* view, BNBinaryDataNotification* notify) {}
BINARYNINJACOREAPI void BNUnregisterDataNotification(
    BNBinaryView* view, BNBinaryDataNotification* notify) {}
BINARYNINJACOREAPI bool BNCanAssemble(BNBinaryView* view,
                                      BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI bool BNIsNeverBranchPatchAvailable(BNBinaryView* view,
                                                      BNArchitecture* arch,
                                                      uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNIsAlwaysBranchPatchAvailable(BNBinaryView* view,
                                                       BNArchitecture* arch,
                                                       uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNIsInvertBranchPatchAvailable(BNBinaryView* view,
                                                       BNArchitecture* arch,
                                                       uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNIsSkipAndReturnZeroPatchAvailable(
    BNBinaryView* view, BNArchitecture* arch, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNIsSkipAndReturnValuePatchAvailable(
    BNBinaryView* view, BNArchitecture* arch, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNConvertToNop(BNBinaryView* view, BNArchitecture* arch,
                                       uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNAlwaysBranch(BNBinaryView* view, BNArchitecture* arch,
                                       uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNInvertBranch(BNBinaryView* view, BNArchitecture* arch,
                                       uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNSkipAndReturnValue(BNBinaryView* view,
                                             BNArchitecture* arch,
                                             uint64_t addr, uint64_t value) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetInstructionLength(BNBinaryView* view,
                                                 BNArchitecture* arch,
                                                 uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNFindNextData(BNBinaryView* view, uint64_t start,
                                       BNDataBuffer* data, uint64_t* result,
                                       BNFindFlag flags) {
  return {};
}
BINARYNINJACOREAPI bool BNFindNextText(BNBinaryView* view, uint64_t start,
                                       const char* data, uint64_t* result,
                                       BNDisassemblySettings* settings,
                                       BNFindFlag flags) {
  return {};
}
BINARYNINJACOREAPI bool BNFindNextConstant(BNBinaryView* view, uint64_t start,
                                           uint64_t constant, uint64_t* result,
                                           BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI bool BNFindNextDataWithProgress(
    BNBinaryView* view, uint64_t start, uint64_t end, BNDataBuffer* data,
    uint64_t* result, BNFindFlag flags, void* ctxt,
    bool (*progress)(void* ctxt, size_t current, size_t total)) {
  return {};
}
BINARYNINJACOREAPI bool BNFindNextTextWithProgress(
    BNBinaryView* view, uint64_t start, uint64_t end, const char* data,
    uint64_t* result, BNDisassemblySettings* settings, BNFindFlag flags,
    void* ctxt, bool (*progress)(void* ctxt, size_t current, size_t total)) {
  return {};
}
BINARYNINJACOREAPI bool BNFindNextConstantWithProgress(
    BNBinaryView* view, uint64_t start, uint64_t end, uint64_t constant,
    uint64_t* result, BNDisassemblySettings* settings, void* ctxt,
    bool (*progress)(void* ctxt, size_t current, size_t total)) {
  return {};
}
BINARYNINJACOREAPI void BNAddAutoSegment(BNBinaryView* view, uint64_t start,
                                         uint64_t length, uint64_t dataOffset,
                                         uint64_t dataLength, uint32_t flags) {}
BINARYNINJACOREAPI void BNRemoveAutoSegment(BNBinaryView* view, uint64_t start,
                                            uint64_t length) {}
BINARYNINJACOREAPI void BNAddUserSegment(BNBinaryView* view, uint64_t start,
                                         uint64_t length, uint64_t dataOffset,
                                         uint64_t dataLength, uint32_t flags) {}
BINARYNINJACOREAPI void BNRemoveUserSegment(BNBinaryView* view, uint64_t start,
                                            uint64_t length) {}
BINARYNINJACOREAPI BNSegment** BNGetSegments(BNBinaryView* view,
                                             size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeSegmentList(BNSegment** segments, size_t count) {}
BINARYNINJACOREAPI BNSegment* BNGetSegmentAt(BNBinaryView* view,
                                             uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNGetAddressForDataOffset(BNBinaryView* view,
                                                  uint64_t offset,
                                                  uint64_t* addr) {
  return {};
}
BINARYNINJACOREAPI void BNAddAutoSection(
    BNBinaryView* view, const char* name, uint64_t start, uint64_t length,
    BNSectionSemantics semantics, const char* type, uint64_t align,
    uint64_t entrySize, const char* linkedSection, const char* infoSection,
    uint64_t infoData) {}
BINARYNINJACOREAPI void BNRemoveAutoSection(BNBinaryView* view,
                                            const char* name) {}
BINARYNINJACOREAPI void BNAddUserSection(
    BNBinaryView* view, const char* name, uint64_t start, uint64_t length,
    BNSectionSemantics semantics, const char* type, uint64_t align,
    uint64_t entrySize, const char* linkedSection, const char* infoSection,
    uint64_t infoData) {}
BINARYNINJACOREAPI void BNRemoveUserSection(BNBinaryView* view,
                                            const char* name) {}
BINARYNINJACOREAPI BNSection** BNGetSections(BNBinaryView* view,
                                             size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNSection** BNGetSectionsAt(BNBinaryView* view,
                                               uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeSectionList(BNSection** sections, size_t count) {}
BINARYNINJACOREAPI BNSection* BNGetSectionByName(BNBinaryView* view,
                                                 const char* name) {
  return {};
}
BINARYNINJACOREAPI char** BNGetUniqueSectionNames(BNBinaryView* view,
                                                  const char** names,
                                                  size_t count) {
  return {};
}
BINARYNINJACOREAPI BNNameSpace* BNGetNameSpaces(BNBinaryView* view,
                                                size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeNameSpaceList(BNNameSpace* nameSpace,
                                            size_t count) {}
BINARYNINJACOREAPI BNNameSpace BNGetExternalNameSpace() { return {}; }
BINARYNINJACOREAPI BNNameSpace BNGetInternalNameSpace() { return {}; }
BINARYNINJACOREAPI void BNFreeNameSpace(BNNameSpace* name) {}
BINARYNINJACOREAPI BNAddressRange* BNGetAllocatedRanges(BNBinaryView* view,
                                                        size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeAddressRanges(BNAddressRange* ranges) {}
BINARYNINJACOREAPI BNRegisterValueWithConfidence
BNGetGlobalPointerValue(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNCreateBinaryDataView(BNFileMetadata* file) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNCreateBinaryDataViewFromBuffer(
    BNFileMetadata* file, BNDataBuffer* buf) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNCreateBinaryDataViewFromData(
    BNFileMetadata* file, const void* data, size_t len) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNCreateBinaryDataViewFromFilename(
    BNFileMetadata* file, const char* filename) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNCreateBinaryDataViewFromFile(
    BNFileMetadata* file, BNFileAccessor* accessor) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNCreateCustomBinaryView(
    const char* name, BNFileMetadata* file, BNBinaryView* parent,
    BNCustomBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI BNBinaryViewType* BNGetBinaryViewTypeByName(
    const char* name) {
  return {};
}
BINARYNINJACOREAPI BNBinaryViewType** BNGetBinaryViewTypes(size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNBinaryViewType** BNGetBinaryViewTypesForData(
    BNBinaryView* data, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeBinaryViewTypeList(BNBinaryViewType** types) {}
BINARYNINJACOREAPI char* BNGetBinaryViewTypeName(BNBinaryViewType* type) {
  return {};
}
BINARYNINJACOREAPI char* BNGetBinaryViewTypeLongName(BNBinaryViewType* type) {
  return {};
}
BINARYNINJACOREAPI bool BNIsBinaryViewTypeDeprecated(BNBinaryViewType* type) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNCreateBinaryViewOfType(
    BNBinaryViewType* type, BNBinaryView* data) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNParseBinaryViewOfType(BNBinaryViewType* type,
                                                         BNBinaryView* data) {
  return {};
}
BINARYNINJACOREAPI bool BNIsBinaryViewTypeValidForData(BNBinaryViewType* type,
                                                       BNBinaryView* data) {
  return {};
}
BINARYNINJACOREAPI BNSettings* BNGetBinaryViewDefaultLoadSettingsForData(
    BNBinaryViewType* type, BNBinaryView* data) {
  return {};
}
BINARYNINJACOREAPI BNSettings* BNGetBinaryViewLoadSettingsForData(
    BNBinaryViewType* type, BNBinaryView* data) {
  return {};
}
BINARYNINJACOREAPI BNBinaryViewType* BNRegisterBinaryViewType(
    const char* name, const char* longName, BNCustomBinaryViewType* type) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterArchitectureForViewType(
    BNBinaryViewType* type, uint32_t id, BNEndianness endian,
    BNArchitecture* arch) {}
BINARYNINJACOREAPI BNArchitecture* BNGetArchitectureForViewType(
    BNBinaryViewType* type, uint32_t id, BNEndianness endian) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterPlatformForViewType(BNBinaryViewType* type,
                                                      uint32_t id,
                                                      BNArchitecture* arch,
                                                      BNPlatform* platform) {}
BINARYNINJACOREAPI void BNRegisterDefaultPlatformForViewType(
    BNBinaryViewType* type, BNArchitecture* arch, BNPlatform* platform) {}
BINARYNINJACOREAPI BNPlatform* BNGetPlatformForViewType(BNBinaryViewType* type,
                                                        uint32_t id,
                                                        BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterBinaryViewEvent(
    BNBinaryViewEventType type, void (*callback)(void* ctx, BNBinaryView* view),
    void* ctx) {}
BINARYNINJACOREAPI BNBinaryReader* BNCreateBinaryReader(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI void BNFreeBinaryReader(BNBinaryReader* stream) {}
BINARYNINJACOREAPI BNEndianness
BNGetBinaryReaderEndianness(BNBinaryReader* stream) {
  return {};
}
BINARYNINJACOREAPI void BNSetBinaryReaderEndianness(BNBinaryReader* stream,
                                                    BNEndianness endian) {}
BINARYNINJACOREAPI bool BNReadData(BNBinaryReader* stream, void* dest,
                                   size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNRead8(BNBinaryReader* stream, uint8_t* result) {
  return {};
}
BINARYNINJACOREAPI bool BNRead16(BNBinaryReader* stream, uint16_t* result) {
  return {};
}
BINARYNINJACOREAPI bool BNRead32(BNBinaryReader* stream, uint32_t* result) {
  return {};
}
BINARYNINJACOREAPI bool BNRead64(BNBinaryReader* stream, uint64_t* result) {
  return {};
}
BINARYNINJACOREAPI bool BNReadLE16(BNBinaryReader* stream, uint16_t* result) {
  return {};
}
BINARYNINJACOREAPI bool BNReadLE32(BNBinaryReader* stream, uint32_t* result) {
  return {};
}
BINARYNINJACOREAPI bool BNReadLE64(BNBinaryReader* stream, uint64_t* result) {
  return {};
}
BINARYNINJACOREAPI bool BNReadBE16(BNBinaryReader* stream, uint16_t* result) {
  return {};
}
BINARYNINJACOREAPI bool BNReadBE32(BNBinaryReader* stream, uint32_t* result) {
  return {};
}
BINARYNINJACOREAPI bool BNReadBE64(BNBinaryReader* stream, uint64_t* result) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetReaderPosition(BNBinaryReader* stream) {
  return {};
}
BINARYNINJACOREAPI void BNSeekBinaryReader(BNBinaryReader* stream,
                                           uint64_t offset) {}
BINARYNINJACOREAPI void BNSeekBinaryReaderRelative(BNBinaryReader* stream,
                                                   int64_t offset) {}
BINARYNINJACOREAPI bool BNIsEndOfFile(BNBinaryReader* stream) { return {}; }
BINARYNINJACOREAPI BNBinaryWriter* BNCreateBinaryWriter(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI void BNFreeBinaryWriter(BNBinaryWriter* stream) {}
BINARYNINJACOREAPI BNEndianness
BNGetBinaryWriterEndianness(BNBinaryWriter* stream) {
  return {};
}
BINARYNINJACOREAPI void BNSetBinaryWriterEndianness(BNBinaryWriter* stream,
                                                    BNEndianness endian) {}
BINARYNINJACOREAPI bool BNWriteData(BNBinaryWriter* stream, const void* src,
                                    size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNWrite8(BNBinaryWriter* stream, uint8_t val) {
  return {};
}
BINARYNINJACOREAPI bool BNWrite16(BNBinaryWriter* stream, uint16_t val) {
  return {};
}
BINARYNINJACOREAPI bool BNWrite32(BNBinaryWriter* stream, uint32_t val) {
  return {};
}
BINARYNINJACOREAPI bool BNWrite64(BNBinaryWriter* stream, uint64_t val) {
  return {};
}
BINARYNINJACOREAPI bool BNWriteLE16(BNBinaryWriter* stream, uint16_t val) {
  return {};
}
BINARYNINJACOREAPI bool BNWriteLE32(BNBinaryWriter* stream, uint32_t val) {
  return {};
}
BINARYNINJACOREAPI bool BNWriteLE64(BNBinaryWriter* stream, uint64_t val) {
  return {};
}
BINARYNINJACOREAPI bool BNWriteBE16(BNBinaryWriter* stream, uint16_t val) {
  return {};
}
BINARYNINJACOREAPI bool BNWriteBE32(BNBinaryWriter* stream, uint32_t val) {
  return {};
}
BINARYNINJACOREAPI bool BNWriteBE64(BNBinaryWriter* stream, uint64_t val) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetWriterPosition(BNBinaryWriter* stream) {
  return {};
}
BINARYNINJACOREAPI void BNSeekBinaryWriter(BNBinaryWriter* stream,
                                           uint64_t offset) {}
BINARYNINJACOREAPI void BNSeekBinaryWriterRelative(BNBinaryWriter* stream,
                                                   int64_t offset) {}
BINARYNINJACOREAPI BNTransform* BNGetTransformByName(const char* name) {
  return {};
}
BINARYNINJACOREAPI BNTransform** BNGetTransformTypeList(size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTransformTypeList(BNTransform** xforms) {}
BINARYNINJACOREAPI BNTransform* BNRegisterTransformType(
    BNTransformType type, const char* name, const char* longName,
    const char* group, BNCustomTransform* xform) {
  return {};
}
BINARYNINJACOREAPI BNTransformType BNGetTransformType(BNTransform* xform) {
  return {};
}
BINARYNINJACOREAPI char* BNGetTransformName(BNTransform* xform) { return {}; }
BINARYNINJACOREAPI char* BNGetTransformLongName(BNTransform* xform) {
  return {};
}
BINARYNINJACOREAPI char* BNGetTransformGroup(BNTransform* xform) { return {}; }
BINARYNINJACOREAPI BNTransformParameterInfo* BNGetTransformParameterList(
    BNTransform* xform, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTransformParameterList(
    BNTransformParameterInfo* params, size_t count) {}
BINARYNINJACOREAPI bool BNDecode(BNTransform* xform, BNDataBuffer* input,
                                 BNDataBuffer* output,
                                 BNTransformParameter* params,
                                 size_t paramCount) {
  return {};
}
BINARYNINJACOREAPI bool BNEncode(BNTransform* xform, BNDataBuffer* input,
                                 BNDataBuffer* output,
                                 BNTransformParameter* params,
                                 size_t paramCount) {
  return {};
}
BINARYNINJACOREAPI BNArchitecture* BNGetArchitectureByName(const char* name) {
  return {};
}
BINARYNINJACOREAPI BNArchitecture** BNGetArchitectureList(size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeArchitectureList(BNArchitecture** archs) {}
BINARYNINJACOREAPI BNArchitecture* BNRegisterArchitecture(
    const char* name, BNCustomArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI BNArchitecture* BNRegisterArchitectureExtension(
    const char* name, BNArchitecture* base, BNCustomArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI void BNAddArchitectureRedirection(BNArchitecture* arch,
                                                     BNArchitecture* from,
                                                     BNArchitecture* to) {}
BINARYNINJACOREAPI BNArchitecture* BNRegisterArchitectureHook(
    BNArchitecture* base, BNCustomArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI void BNFinalizeArchitectureHook(BNArchitecture* base) {}
BINARYNINJACOREAPI char* BNGetArchitectureName(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI BNEndianness
BNGetArchitectureEndianness(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetArchitectureAddressSize(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetArchitectureDefaultIntegerSize(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetArchitectureInstructionAlignment(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetArchitectureMaxInstructionLength(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetArchitectureOpcodeDisplayLength(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI BNArchitecture* BNGetAssociatedArchitectureByAddress(
    BNArchitecture* arch, uint64_t* addr) {
  return {};
}
BINARYNINJACOREAPI bool BNGetInstructionInfo(BNArchitecture* arch,
                                             const uint8_t* data, uint64_t addr,
                                             size_t maxLen,
                                             BNInstructionInfo* result) {
  return {};
}
BINARYNINJACOREAPI bool BNGetInstructionText(BNArchitecture* arch,
                                             const uint8_t* data, uint64_t addr,
                                             size_t* len,
                                             BNInstructionTextToken** result,
                                             size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNGetInstructionLowLevelIL(BNArchitecture* arch,
                                                   const uint8_t* data,
                                                   uint64_t addr, size_t* len,
                                                   BNLowLevelILFunction* il) {
  return {};
}
BINARYNINJACOREAPI void BNFreeInstructionText(BNInstructionTextToken* tokens,
                                              size_t count) {}
BINARYNINJACOREAPI void BNFreeInstructionTextLines(BNInstructionTextLine* lines,
                                                   size_t count) {}
BINARYNINJACOREAPI char* BNGetArchitectureRegisterName(BNArchitecture* arch,
                                                       uint32_t reg) {
  return {};
}
BINARYNINJACOREAPI char* BNGetArchitectureFlagName(BNArchitecture* arch,
                                                   uint32_t flag) {
  return {};
}
BINARYNINJACOREAPI char* BNGetArchitectureFlagWriteTypeName(
    BNArchitecture* arch, uint32_t flags) {
  return {};
}
BINARYNINJACOREAPI char* BNGetArchitectureSemanticFlagClassName(
    BNArchitecture* arch, uint32_t semClass) {
  return {};
}
BINARYNINJACOREAPI char* BNGetArchitectureSemanticFlagGroupName(
    BNArchitecture* arch, uint32_t semGroup) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetFullWidthArchitectureRegisters(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetAllArchitectureRegisters(BNArchitecture* arch,
                                                           size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetAllArchitectureFlags(BNArchitecture* arch,
                                                       size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetAllArchitectureFlagWriteTypes(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetAllArchitectureSemanticFlagClasses(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetAllArchitectureSemanticFlagGroups(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNFlagRole BNGetArchitectureFlagRole(BNArchitecture* arch,
                                                        uint32_t flag,
                                                        uint32_t semClass) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetArchitectureFlagsRequiredForFlagCondition(
    BNArchitecture* arch, BNLowLevelILFlagCondition cond, uint32_t semClass,
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetArchitectureFlagsRequiredForSemanticFlagGroup(
    BNArchitecture* arch, uint32_t semGroup, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNFlagConditionForSemanticClass*
BNGetArchitectureFlagConditionsForSemanticFlagGroup(BNArchitecture* arch,
                                                    uint32_t semGroup,
                                                    size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeFlagConditionsForSemanticFlagGroup(
    BNFlagConditionForSemanticClass* conditions) {}
BINARYNINJACOREAPI uint32_t* BNGetArchitectureFlagsWrittenByFlagWriteType(
    BNArchitecture* arch, uint32_t writeType, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t BNGetArchitectureSemanticClassForFlagWriteType(
    BNArchitecture* arch, uint32_t writeType) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetArchitectureFlagWriteLowLevelIL(
    BNArchitecture* arch, BNLowLevelILOperation op, size_t size,
    uint32_t flagWriteType, uint32_t flag, BNRegisterOrConstant* operands,
    size_t operandCount, BNLowLevelILFunction* il) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetDefaultArchitectureFlagWriteLowLevelIL(
    BNArchitecture* arch, BNLowLevelILOperation op, size_t size,
    BNFlagRole role, BNRegisterOrConstant* operands, size_t operandCount,
    BNLowLevelILFunction* il) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetArchitectureFlagConditionLowLevelIL(
    BNArchitecture* arch, BNLowLevelILFlagCondition cond, uint32_t semClass,
    BNLowLevelILFunction* il) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetDefaultArchitectureFlagConditionLowLevelIL(
    BNArchitecture* arch, BNLowLevelILFlagCondition cond, uint32_t semClass,
    BNLowLevelILFunction* il) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetArchitectureSemanticFlagGroupLowLevelIL(
    BNArchitecture* arch, uint32_t semGroup, BNLowLevelILFunction* il) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetModifiedArchitectureRegistersOnWrite(
    BNArchitecture* arch, uint32_t reg, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRegisterList(uint32_t* regs) {}
BINARYNINJACOREAPI BNRegisterInfo
BNGetArchitectureRegisterInfo(BNArchitecture* arch, uint32_t reg) {
  return {};
}
BINARYNINJACOREAPI uint32_t
BNGetArchitectureStackPointerRegister(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI uint32_t
BNGetArchitectureLinkRegister(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetArchitectureGlobalRegisters(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNIsArchitectureGlobalRegister(BNArchitecture* arch,
                                                       uint32_t reg) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetArchitectureSystemRegisters(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNIsArchitectureSystemRegister(BNArchitecture* arch,
                                                       uint32_t reg) {
  return {};
}
BINARYNINJACOREAPI uint32_t
BNGetArchitectureRegisterByName(BNArchitecture* arch, const char* name) {
  return {};
}
BINARYNINJACOREAPI char* BNGetArchitectureRegisterStackName(
    BNArchitecture* arch, uint32_t regStack) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetAllArchitectureRegisterStacks(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNRegisterStackInfo
BNGetArchitectureRegisterStackInfo(BNArchitecture* arch, uint32_t regStack) {
  return {};
}
BINARYNINJACOREAPI uint32_t
BNGetArchitectureRegisterStackForRegister(BNArchitecture* arch, uint32_t reg) {
  return {};
}
BINARYNINJACOREAPI char* BNGetArchitectureIntrinsicName(BNArchitecture* arch,
                                                        uint32_t intrinsic) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetAllArchitectureIntrinsics(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNNameAndType* BNGetArchitectureIntrinsicInputs(
    BNArchitecture* arch, uint32_t intrinsic, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeNameAndTypeList(BNNameAndType* nt, size_t count) {
}
BINARYNINJACOREAPI BNTypeWithConfidence* BNGetArchitectureIntrinsicOutputs(
    BNArchitecture* arch, uint32_t intrinsic, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeOutputTypeList(BNTypeWithConfidence* types,
                                             size_t count) {}
BINARYNINJACOREAPI bool BNCanArchitectureAssemble(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI bool BNAssemble(BNArchitecture* arch, const char* code,
                                   uint64_t addr, BNDataBuffer* result,
                                   char** errors) {
  return {};
}
BINARYNINJACOREAPI bool BNIsArchitectureNeverBranchPatchAvailable(
    BNArchitecture* arch, const uint8_t* data, uint64_t addr, size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNIsArchitectureAlwaysBranchPatchAvailable(
    BNArchitecture* arch, const uint8_t* data, uint64_t addr, size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNIsArchitectureInvertBranchPatchAvailable(
    BNArchitecture* arch, const uint8_t* data, uint64_t addr, size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNIsArchitectureSkipAndReturnZeroPatchAvailable(
    BNArchitecture* arch, const uint8_t* data, uint64_t addr, size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNIsArchitectureSkipAndReturnValuePatchAvailable(
    BNArchitecture* arch, const uint8_t* data, uint64_t addr, size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNArchitectureConvertToNop(BNArchitecture* arch,
                                                   uint8_t* data, uint64_t addr,
                                                   size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNArchitectureAlwaysBranch(BNArchitecture* arch,
                                                   uint8_t* data, uint64_t addr,
                                                   size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNArchitectureInvertBranch(BNArchitecture* arch,
                                                   uint8_t* data, uint64_t addr,
                                                   size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNArchitectureSkipAndReturnValue(BNArchitecture* arch,
                                                         uint8_t* data,
                                                         uint64_t addr,
                                                         size_t len,
                                                         uint64_t value) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterArchitectureFunctionRecognizer(
    BNArchitecture* arch, BNFunctionRecognizer* rec) {}
BINARYNINJACOREAPI bool BNIsBinaryViewTypeArchitectureConstantDefined(
    BNArchitecture* arch, const char* type, const char* name) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetBinaryViewTypeArchitectureConstant(
    BNArchitecture* arch, const char* type, const char* name,
    uint64_t defaultValue) {
  return {};
}
BINARYNINJACOREAPI void BNSetBinaryViewTypeArchitectureConstant(
    BNArchitecture* arch, const char* type, const char* name, uint64_t value) {}
BINARYNINJACOREAPI void BNArchitectureRegisterRelocationHandler(
    BNArchitecture* arch, const char* viewName, BNRelocationHandler* handler) {}
BINARYNINJACOREAPI BNRelocationHandler* BNCreateRelocationHandler(
    BNCustomRelocationHandler* handler) {
  return {};
}
BINARYNINJACOREAPI BNRelocationHandler* BNArchitectureGetRelocationHandler(
    BNArchitecture* arch, const char* viewName) {
  return {};
}
BINARYNINJACOREAPI BNRelocationHandler* BNNewRelocationHandlerReference(
    BNRelocationHandler* handler) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRelocationHandler(BNRelocationHandler* handler) {}
BINARYNINJACOREAPI bool BNRelocationHandlerGetRelocationInfo(
    BNRelocationHandler* handler, BNBinaryView* data, BNArchitecture* arch,
    BNRelocationInfo* info, size_t infoCount) {
  return {};
}
BINARYNINJACOREAPI bool BNRelocationHandlerApplyRelocation(
    BNRelocationHandler* handler, BNBinaryView* view, BNArchitecture* arch,
    BNRelocation* reloc, uint8_t* dest, size_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNRelocationHandlerDefaultApplyRelocation(
    BNRelocationHandler* handler, BNBinaryView* view, BNArchitecture* arch,
    BNRelocation* reloc, uint8_t* dest, size_t len) {
  return {};
}
BINARYNINJACOREAPI size_t BNRelocationHandlerGetOperandForExternalRelocation(
    BNRelocationHandler* handler, const uint8_t* data, uint64_t addr,
    size_t length, const BNLowLevelILFunction* il, BNRelocation* relocation) {
  return {};
}
BINARYNINJACOREAPI void BNAddAnalysisOption(BNBinaryView* view,
                                            const char* name) {}
BINARYNINJACOREAPI void BNAddFunctionForAnalysis(BNBinaryView* view,
                                                 BNPlatform* platform,
                                                 uint64_t addr) {}
BINARYNINJACOREAPI void BNAddEntryPointForAnalysis(BNBinaryView* view,
                                                   BNPlatform* platform,
                                                   uint64_t addr) {}
BINARYNINJACOREAPI void BNRemoveAnalysisFunction(BNBinaryView* view,
                                                 BNFunction* func) {}
BINARYNINJACOREAPI void BNCreateUserFunction(BNBinaryView* view,
                                             BNPlatform* platform,
                                             uint64_t addr) {}
BINARYNINJACOREAPI void BNRemoveUserFunction(BNBinaryView* view,
                                             BNFunction* func) {}
BINARYNINJACOREAPI void BNUpdateAnalysisAndWait(BNBinaryView* view) {}
BINARYNINJACOREAPI void BNUpdateAnalysis(BNBinaryView* view) {}
BINARYNINJACOREAPI void BNAbortAnalysis(BNBinaryView* view) {}
BINARYNINJACOREAPI bool BNIsFunctionUpdateNeeded(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNRequestAdvancedFunctionAnalysisData(
    BNFunction* func) {}
BINARYNINJACOREAPI void BNReleaseAdvancedFunctionAnalysisData(
    BNFunction* func) {}
BINARYNINJACOREAPI void BNReleaseAdvancedFunctionAnalysisDataMultiple(
    BNFunction* func, size_t count) {}
BINARYNINJACOREAPI BNFunction* BNNewFunctionReference(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNFreeFunction(BNFunction* func) {}
BINARYNINJACOREAPI BNFunction** BNGetAnalysisFunctionList(BNBinaryView* view,
                                                          size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeFunctionList(BNFunction** funcs, size_t count) {}
BINARYNINJACOREAPI bool BNHasFunctions(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI bool BNHasSymbols(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI bool BNHasDataVariables(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI BNFunction* BNGetAnalysisFunction(BNBinaryView* view,
                                                     BNPlatform* platform,
                                                     uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI BNFunction* BNGetRecentAnalysisFunctionForAddress(
    BNBinaryView* view, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI BNFunction** BNGetAnalysisFunctionsForAddress(
    BNBinaryView* view, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNFunction* BNGetAnalysisEntryPoint(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI char* BNGetGlobalCommentForAddress(BNBinaryView* view,
                                                      uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNGetGlobalCommentedAddresses(BNBinaryView* view,
                                                           size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNSetGlobalCommentForAddress(BNBinaryView* view,
                                                     uint64_t addr,
                                                     const char* comment) {}
BINARYNINJACOREAPI BNBinaryView* BNGetFunctionData(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNArchitecture* BNGetFunctionArchitecture(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNPlatform* BNGetFunctionPlatform(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetFunctionStart(BNFunction* func) { return {}; }
BINARYNINJACOREAPI BNSymbol* BNGetFunctionSymbol(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI bool BNWasFunctionAutomaticallyDiscovered(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNBoolWithConfidence BNCanFunctionReturn(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNSetFunctionAutoType(BNFunction* func, BNType* type) {}
BINARYNINJACOREAPI void BNSetFunctionUserType(BNFunction* func, BNType* type) {}
BINARYNINJACOREAPI char* BNGetFunctionComment(BNFunction* func) { return {}; }
BINARYNINJACOREAPI char* BNGetCommentForAddress(BNFunction* func,
                                                uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNGetCommentedAddresses(BNFunction* func,
                                                     size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeAddressList(uint64_t* addrs) {}
BINARYNINJACOREAPI void BNSetFunctionComment(BNFunction* func,
                                             const char* comment) {}
BINARYNINJACOREAPI void BNSetCommentForAddress(BNFunction* func, uint64_t addr,
                                               const char* comment) {}
BINARYNINJACOREAPI void BNAddUserCodeReference(BNFunction* func,
                                               BNArchitecture* fromArch,
                                               uint64_t fromAddr,
                                               uint64_t toAddr) {}
BINARYNINJACOREAPI void BNRemoveUserCodeReference(BNFunction* func,
                                                  BNArchitecture* fromArch,
                                                  uint64_t fromAddr,
                                                  uint64_t toAddr) {}
BINARYNINJACOREAPI BNBasicBlock* BNNewBasicBlockReference(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI void BNFreeBasicBlock(BNBasicBlock* block) {}
BINARYNINJACOREAPI BNBasicBlock** BNGetFunctionBasicBlockList(BNFunction* func,
                                                              size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeBasicBlockList(BNBasicBlock** blocks,
                                             size_t count) {}
BINARYNINJACOREAPI BNBasicBlock* BNGetFunctionBasicBlockAtAddress(
    BNFunction* func, BNArchitecture* arch, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock* BNGetRecentBasicBlockForAddress(
    BNBinaryView* view, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock** BNGetBasicBlocksForAddress(BNBinaryView* view,
                                                             uint64_t addr,
                                                             size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock** BNGetBasicBlocksStartingAtAddress(
    BNBinaryView* view, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNLowLevelILFunction* BNGetFunctionLowLevelIL(
    BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNLowLevelILFunction* BNGetFunctionLowLevelILIfAvailable(
    BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetLowLevelILForInstruction(BNFunction* func,
                                                        BNArchitecture* arch,
                                                        uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetLowLevelILExitsForInstruction(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeILInstructionList(size_t* list) {}
BINARYNINJACOREAPI BNMediumLevelILFunction* BNGetFunctionMediumLevelIL(
    BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction*
BNGetFunctionMediumLevelILIfAvailable(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNHighLevelILFunction* BNGetFunctionHighLevelIL(
    BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNHighLevelILFunction* BNGetFunctionHighLevelILIfAvailable(
    BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetRegisterValueAtInstruction(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, uint32_t reg) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetRegisterValueAfterInstruction(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, uint32_t reg) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue
BNGetStackContentsAtInstruction(BNFunction* func, BNArchitecture* arch,
                                uint64_t addr, int64_t offset, size_t size) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue
BNGetStackContentsAfterInstruction(BNFunction* func, BNArchitecture* arch,
                                   uint64_t addr, int64_t offset, size_t size) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetParameterValueAtInstruction(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, BNType* functionType,
    size_t i) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetParameterValueAtLowLevelILInstruction(
    BNFunction* func, size_t instr, BNType* functionType, size_t i) {
  return {};
}
BINARYNINJACOREAPI void BNFreePossibleValueSet(BNPossibleValueSet* value) {}
BINARYNINJACOREAPI uint32_t* BNGetRegistersReadByInstruction(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetRegistersWrittenByInstruction(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNStackVariableReference*
BNGetStackVariablesReferencedByInstruction(BNFunction* func,
                                           BNArchitecture* arch, uint64_t addr,
                                           size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeStackVariableReferenceList(
    BNStackVariableReference* refs, size_t count) {}
BINARYNINJACOREAPI BNConstantReference* BNGetConstantsReferencedByInstruction(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeConstantReferenceList(BNConstantReference* refs) {
}
BINARYNINJACOREAPI BNLowLevelILFunction* BNGetFunctionLiftedIL(
    BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNLowLevelILFunction* BNGetFunctionLiftedILIfAvailable(
    BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetLiftedILForInstruction(BNFunction* func,
                                                      BNArchitecture* arch,
                                                      uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetLiftedILFlagUsesForDefinition(BNFunction* func,
                                                              size_t i,
                                                              uint32_t flag,
                                                              size_t* count) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetLiftedILFlagDefinitionsForUse(BNFunction* func,
                                                              size_t i,
                                                              uint32_t flag,
                                                              size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetFlagsReadByLiftedILInstruction(
    BNFunction* func, size_t i, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetFlagsWrittenByLiftedILInstruction(
    BNFunction* func, size_t i, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNType* BNGetFunctionType(BNFunction* func) { return {}; }
BINARYNINJACOREAPI BNTypeWithConfidence
BNGetFunctionReturnType(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNRegisterSetWithConfidence
BNGetFunctionReturnRegisters(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNCallingConventionWithConfidence
BNGetFunctionCallingConvention(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNParameterVariablesWithConfidence
BNGetFunctionParameterVariables(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNFreeParameterVariables(
    BNParameterVariablesWithConfidence* vars) {}
BINARYNINJACOREAPI BNBoolWithConfidence
BNFunctionHasVariableArguments(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNOffsetWithConfidence
BNGetFunctionStackAdjustment(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNRegisterStackAdjustment*
BNGetFunctionRegisterStackAdjustments(BNFunction* func, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRegisterStackAdjustments(
    BNRegisterStackAdjustment* adjustments) {}
BINARYNINJACOREAPI BNRegisterSetWithConfidence
BNGetFunctionClobberedRegisters(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRegisterSet(BNRegisterSetWithConfidence* regs) {}
BINARYNINJACOREAPI void BNSetAutoFunctionReturnType(
    BNFunction* func, BNTypeWithConfidence* type) {}
BINARYNINJACOREAPI void BNSetAutoFunctionReturnRegisters(
    BNFunction* func, BNRegisterSetWithConfidence* regs) {}
BINARYNINJACOREAPI void BNSetAutoFunctionCallingConvention(
    BNFunction* func, BNCallingConventionWithConfidence* convention) {}
BINARYNINJACOREAPI void BNSetAutoFunctionParameterVariables(
    BNFunction* func, BNParameterVariablesWithConfidence* vars) {}
BINARYNINJACOREAPI void BNSetAutoFunctionHasVariableArguments(
    BNFunction* func, BNBoolWithConfidence* varArgs) {}
BINARYNINJACOREAPI void BNSetAutoFunctionCanReturn(
    BNFunction* func, BNBoolWithConfidence* returns) {}
BINARYNINJACOREAPI void BNSetAutoFunctionStackAdjustment(
    BNFunction* func, BNOffsetWithConfidence* stackAdjust) {}
BINARYNINJACOREAPI void BNSetAutoFunctionRegisterStackAdjustments(
    BNFunction* func, BNRegisterStackAdjustment* adjustments, size_t count) {}
BINARYNINJACOREAPI void BNSetAutoFunctionClobberedRegisters(
    BNFunction* func, BNRegisterSetWithConfidence* regs) {}
BINARYNINJACOREAPI void BNSetUserFunctionReturnType(
    BNFunction* func, BNTypeWithConfidence* type) {}
BINARYNINJACOREAPI void BNSetUserFunctionReturnRegisters(
    BNFunction* func, BNRegisterSetWithConfidence* regs) {}
BINARYNINJACOREAPI void BNSetUserFunctionCallingConvention(
    BNFunction* func, BNCallingConventionWithConfidence* convention) {}
BINARYNINJACOREAPI void BNSetUserFunctionParameterVariables(
    BNFunction* func, BNParameterVariablesWithConfidence* vars) {}
BINARYNINJACOREAPI void BNSetUserFunctionHasVariableArguments(
    BNFunction* func, BNBoolWithConfidence* varArgs) {}
BINARYNINJACOREAPI void BNSetUserFunctionCanReturn(
    BNFunction* func, BNBoolWithConfidence* returns) {}
BINARYNINJACOREAPI void BNSetUserFunctionStackAdjustment(
    BNFunction* func, BNOffsetWithConfidence* stackAdjust) {}
BINARYNINJACOREAPI void BNSetUserFunctionRegisterStackAdjustments(
    BNFunction* func, BNRegisterStackAdjustment* adjustments, size_t count) {}
BINARYNINJACOREAPI void BNSetUserFunctionClobberedRegisters(
    BNFunction* func, BNRegisterSetWithConfidence* regs) {}
BINARYNINJACOREAPI void BNApplyImportedTypes(BNFunction* func, BNSymbol* sym,
                                             BNType* type) {}
BINARYNINJACOREAPI void BNApplyAutoDiscoveredFunctionType(BNFunction* func,
                                                          BNType* type) {}
BINARYNINJACOREAPI bool BNFunctionHasExplicitlyDefinedType(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextLine* BNGetFunctionTypeTokens(
    BNFunction* func, BNDisassemblySettings* settings, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValueWithConfidence
BNGetFunctionGlobalPointerValue(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValueWithConfidence
BNGetFunctionRegisterValueAtExit(BNFunction* func, uint32_t reg) {
  return {};
}
BINARYNINJACOREAPI BNFunction* BNGetBasicBlockFunction(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI BNArchitecture* BNGetBasicBlockArchitecture(
    BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock* BNGetBasicBlockSource(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetBasicBlockStart(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetBasicBlockEnd(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetBasicBlockLength(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlockEdge* BNGetBasicBlockOutgoingEdges(
    BNBasicBlock* block, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlockEdge* BNGetBasicBlockIncomingEdges(
    BNBasicBlock* block, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeBasicBlockEdgeList(BNBasicBlockEdge* edges,
                                                 size_t count) {}
BINARYNINJACOREAPI bool BNBasicBlockHasUndeterminedOutgoingEdges(
    BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI bool BNBasicBlockCanExit(BNBasicBlock* block) { return {}; }
BINARYNINJACOREAPI bool BNBasicBlockHasInvalidInstructions(
    BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetBasicBlockIndex(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock** BNGetBasicBlockDominators(BNBasicBlock* block,
                                                            size_t* count,
                                                            bool post) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock** BNGetBasicBlockStrictDominators(
    BNBasicBlock* block, size_t* count, bool post) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock* BNGetBasicBlockImmediateDominator(
    BNBasicBlock* block, bool post) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock** BNGetBasicBlockDominatorTreeChildren(
    BNBasicBlock* block, size_t* count, bool post) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock** BNGetBasicBlockDominanceFrontier(
    BNBasicBlock* block, size_t* count, bool post) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock** BNGetBasicBlockIteratedDominanceFrontier(
    BNBasicBlock** blocks, size_t incomingCount, size_t* outputCount) {
  return {};
}
BINARYNINJACOREAPI bool BNIsILBasicBlock(BNBasicBlock* block) { return {}; }
BINARYNINJACOREAPI bool BNIsLowLevelILBasicBlock(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI bool BNIsMediumLevelILBasicBlock(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI BNLowLevelILFunction* BNGetBasicBlockLowLevelILFunction(
    BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction*
BNGetBasicBlockMediumLevelILFunction(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextLine* BNGetBasicBlockDisassemblyText(
    BNBasicBlock* block, BNDisassemblySettings* settings, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeDisassemblyTextLines(BNDisassemblyTextLine* lines,
                                                   size_t count) {}
BINARYNINJACOREAPI char* BNGetDisplayStringForInteger(BNBinaryView* binaryView,
                                                      BNIntegerDisplayType type,
                                                      uint64_t value,
                                                      size_t inputWidth,
                                                      bool isSigned) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextRenderer* BNCreateDisassemblyTextRenderer(
    BNFunction* func, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextRenderer*
BNCreateLowLevelILDisassemblyTextRenderer(BNLowLevelILFunction* func,
                                          BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextRenderer*
BNCreateMediumLevelILDisassemblyTextRenderer(BNMediumLevelILFunction* func,
                                             BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextRenderer*
BNCreateHighLevelILDisassemblyTextRenderer(BNHighLevelILFunction* func,
                                           BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextRenderer*
BNNewDisassemblyTextRendererReference(BNDisassemblyTextRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI void BNFreeDisassemblyTextRenderer(
    BNDisassemblyTextRenderer* renderer) {}
BINARYNINJACOREAPI BNFunction* BNGetDisassemblyTextRendererFunction(
    BNDisassemblyTextRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI BNLowLevelILFunction*
BNGetDisassemblyTextRendererLowLevelILFunction(
    BNDisassemblyTextRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction*
BNGetDisassemblyTextRendererMediumLevelILFunction(
    BNDisassemblyTextRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI BNHighLevelILFunction*
BNGetDisassemblyTextRendererHighLevelILFunction(
    BNDisassemblyTextRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock* BNGetDisassemblyTextRendererBasicBlock(
    BNDisassemblyTextRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI BNArchitecture* BNGetDisassemblyTextRendererArchitecture(
    BNDisassemblyTextRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblySettings* BNGetDisassemblyTextRendererSettings(
    BNDisassemblyTextRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI void BNSetDisassemblyTextRendererBasicBlock(
    BNDisassemblyTextRenderer* renderer, BNBasicBlock* block) {}
BINARYNINJACOREAPI void BNSetDisassemblyTextRendererArchitecture(
    BNDisassemblyTextRenderer* renderer, BNArchitecture* arch) {}
BINARYNINJACOREAPI void BNSetDisassemblyTextRendererSettings(
    BNDisassemblyTextRenderer* renderer, BNDisassemblySettings* settings) {}
BINARYNINJACOREAPI bool BNIsILDisassemblyTextRenderer(
    BNDisassemblyTextRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI bool BNDisassemblyTextRendererHasDataFlow(
    BNDisassemblyTextRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI BNInstructionTextToken*
BNGetDisassemblyTextRendererInstructionAnnotations(
    BNDisassemblyTextRenderer* renderer, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNGetDisassemblyTextRendererInstructionText(
    BNDisassemblyTextRenderer* renderer, uint64_t addr, size_t* len,
    BNDisassemblyTextLine** result, size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNGetDisassemblyTextRendererLines(
    BNDisassemblyTextRenderer* renderer, uint64_t addr, size_t* len,
    BNDisassemblyTextLine** result, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextLine*
BNPostProcessDisassemblyTextRendererLines(BNDisassemblyTextRenderer* renderer,
                                          uint64_t addr, size_t len,
                                          BNDisassemblyTextLine* inLines,
                                          size_t inCount, size_t* outCount,
                                          const char* indentSpaces) {
  return {};
}
BINARYNINJACOREAPI void BNResetDisassemblyTextRendererDeduplicatedComments(
    BNDisassemblyTextRenderer* renderer) {}
BINARYNINJACOREAPI bool BNGetDisassemblyTextRendererSymbolTokens(
    BNDisassemblyTextRenderer* renderer, uint64_t addr, size_t size,
    size_t operand, BNInstructionTextToken** result, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNInstructionTextToken*
BNGetDisassemblyTextRendererStackVariableReferenceTokens(
    BNDisassemblyTextRenderer* renderer, BNStackVariableReference* ref,
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNIsIntegerToken(BNInstructionTextTokenType type) {
  return {};
}
BINARYNINJACOREAPI BNInstructionTextToken*
BNGetDisassemblyTextRendererIntegerTokens(BNDisassemblyTextRenderer* renderer,
                                          BNInstructionTextToken* token,
                                          BNArchitecture* arch, uint64_t addr,
                                          size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextLine* BNDisassemblyTextRendererWrapComment(
    BNDisassemblyTextRenderer* renderer, const BNDisassemblyTextLine* inLine,
    size_t* outLineCount, const char* comment, bool hasAutoAnnotations,
    const char* leadingSpaces, const char* indentSpaces) {
  return {};
}
BINARYNINJACOREAPI void BNMarkFunctionAsRecentlyUsed(BNFunction* func) {}
BINARYNINJACOREAPI void BNMarkBasicBlockAsRecentlyUsed(BNBasicBlock* block) {}
BINARYNINJACOREAPI BNReferenceSource* BNGetCodeReferences(BNBinaryView* view,
                                                          uint64_t addr,
                                                          size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNReferenceSource* BNGetCodeReferencesInRange(
    BNBinaryView* view, uint64_t addr, uint64_t len, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeCodeReferences(BNReferenceSource* refs,
                                             size_t count) {}
BINARYNINJACOREAPI uint64_t* BNGetCodeReferencesFrom(BNBinaryView* view,
                                                     BNReferenceSource* src,
                                                     size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNGetCodeReferencesFromInRange(
    BNBinaryView* view, BNReferenceSource* src, uint64_t len, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNGetDataReferences(BNBinaryView* view,
                                                 uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNGetDataReferencesInRange(BNBinaryView* view,
                                                        uint64_t addr,
                                                        uint64_t len,
                                                        size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNGetDataReferencesFrom(BNBinaryView* view,
                                                     uint64_t addr,
                                                     size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNGetDataReferencesFromInRange(BNBinaryView* view,
                                                            uint64_t addr,
                                                            uint64_t len,
                                                            size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNAddUserDataReference(BNBinaryView* view,
                                               uint64_t fromAddr,
                                               uint64_t toAddr) {}
BINARYNINJACOREAPI void BNRemoveUserDataReference(BNBinaryView* view,
                                                  uint64_t fromAddr,
                                                  uint64_t toAddr) {}
BINARYNINJACOREAPI void BNFreeDataReferences(uint64_t* refs) {}
BINARYNINJACOREAPI void BNRegisterGlobalFunctionRecognizer(
    BNFunctionRecognizer* rec) {}
BINARYNINJACOREAPI bool BNGetStringAtAddress(BNBinaryView* view, uint64_t addr,
                                             BNStringReference* strRef) {
  return {};
}
BINARYNINJACOREAPI BNStringReference* BNGetStrings(BNBinaryView* view,
                                                   size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNStringReference* BNGetStringsInRange(BNBinaryView* view,
                                                          uint64_t start,
                                                          uint64_t len,
                                                          size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeStringReferenceList(BNStringReference* strings) {}
BINARYNINJACOREAPI BNVariableNameAndType* BNGetStackLayout(BNFunction* func,
                                                           size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeVariableNameAndTypeList(
    BNVariableNameAndType* vars, size_t count) {}
BINARYNINJACOREAPI void BNCreateAutoStackVariable(BNFunction* func,
                                                  int64_t offset,
                                                  BNTypeWithConfidence* type,
                                                  const char* name) {}
BINARYNINJACOREAPI void BNCreateUserStackVariable(BNFunction* func,
                                                  int64_t offset,
                                                  BNTypeWithConfidence* type,
                                                  const char* name) {}
BINARYNINJACOREAPI void BNDeleteAutoStackVariable(BNFunction* func,
                                                  int64_t offset) {}
BINARYNINJACOREAPI void BNDeleteUserStackVariable(BNFunction* func,
                                                  int64_t offset) {}
BINARYNINJACOREAPI bool BNGetStackVariableAtFrameOffset(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, int64_t offset,
    BNVariableNameAndType* var) {
  return {};
}
BINARYNINJACOREAPI void BNFreeVariableNameAndType(BNVariableNameAndType* var) {}
BINARYNINJACOREAPI BNVariableNameAndType* BNGetFunctionVariables(
    BNFunction* func, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNCreateAutoVariable(BNFunction* func,
                                             const BNVariable* var,
                                             BNTypeWithConfidence* type,
                                             const char* name,
                                             bool ignoreDisjointUses) {}
BINARYNINJACOREAPI void BNCreateUserVariable(BNFunction* func,
                                             const BNVariable* var,
                                             BNTypeWithConfidence* type,
                                             const char* name,
                                             bool ignoreDisjointUses) {}
BINARYNINJACOREAPI void BNDeleteAutoVariable(BNFunction* func,
                                             const BNVariable* var) {}
BINARYNINJACOREAPI void BNDeleteUserVariable(BNFunction* func,
                                             const BNVariable* var) {}
BINARYNINJACOREAPI BNTypeWithConfidence
BNGetVariableType(BNFunction* func, const BNVariable* var) {
  return {};
}
BINARYNINJACOREAPI char* BNGetVariableName(BNFunction* func,
                                           const BNVariable* var) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNToVariableIdentifier(const BNVariable* var) {
  return {};
}
BINARYNINJACOREAPI BNVariable BNFromVariableIdentifier(uint64_t id) {
  return {};
}
BINARYNINJACOREAPI BNReferenceSource* BNGetFunctionCallSites(BNFunction* func,
                                                             size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNGetCallees(BNBinaryView* view,
                                          BNReferenceSource* callSite,
                                          size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNReferenceSource* BNGetCallers(BNBinaryView* view,
                                                   uint64_t callee,
                                                   size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNSetAutoIndirectBranches(
    BNFunction* func, BNArchitecture* sourceArch, uint64_t source,
    BNArchitectureAndAddress* branches, size_t count) {}
BINARYNINJACOREAPI void BNSetUserIndirectBranches(
    BNFunction* func, BNArchitecture* sourceArch, uint64_t source,
    BNArchitectureAndAddress* branches, size_t count) {}
BINARYNINJACOREAPI BNIndirectBranchInfo* BNGetIndirectBranches(BNFunction* func,
                                                               size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNIndirectBranchInfo* BNGetIndirectBranchesAt(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeIndirectBranchList(
    BNIndirectBranchInfo* branches) {}
BINARYNINJACOREAPI void BNSetAutoCallTypeAdjustment(
    BNFunction* func, BNArchitecture* arch, uint64_t addr,
    BNTypeWithConfidence* type) {}
BINARYNINJACOREAPI void BNSetUserCallTypeAdjustment(
    BNFunction* func, BNArchitecture* arch, uint64_t addr,
    BNTypeWithConfidence* type) {}
BINARYNINJACOREAPI void BNSetAutoCallStackAdjustment(BNFunction* func,
                                                     BNArchitecture* arch,
                                                     uint64_t addr,
                                                     int64_t adjust,
                                                     uint8_t confidence) {}
BINARYNINJACOREAPI void BNSetUserCallStackAdjustment(BNFunction* func,
                                                     BNArchitecture* arch,
                                                     uint64_t addr,
                                                     int64_t adjust,
                                                     uint8_t confidence) {}
BINARYNINJACOREAPI void BNSetAutoCallRegisterStackAdjustment(
    BNFunction* func, BNArchitecture* arch, uint64_t addr,
    BNRegisterStackAdjustment* adjust, size_t count) {}
BINARYNINJACOREAPI void BNSetUserCallRegisterStackAdjustment(
    BNFunction* func, BNArchitecture* arch, uint64_t addr,
    BNRegisterStackAdjustment* adjust, size_t count) {}
BINARYNINJACOREAPI void BNSetAutoCallRegisterStackAdjustmentForRegisterStack(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, uint32_t regStack,
    int32_t adjust, uint8_t confidence) {}
BINARYNINJACOREAPI void BNSetUserCallRegisterStackAdjustmentForRegisterStack(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, uint32_t regStack,
    int32_t adjust, uint8_t confidence) {}
BINARYNINJACOREAPI BNTypeWithConfidence
BNGetCallTypeAdjustment(BNFunction* func, BNArchitecture* arch, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI BNOffsetWithConfidence BNGetCallStackAdjustment(
    BNFunction* func, BNArchitecture* arch, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI BNRegisterStackAdjustment* BNGetCallRegisterStackAdjustment(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNRegisterStackAdjustment
BNGetCallRegisterStackAdjustmentForRegisterStack(BNFunction* func,
                                                 BNArchitecture* arch,
                                                 uint64_t addr,
                                                 uint32_t regStack) {
  return {};
}
BINARYNINJACOREAPI bool BNIsCallInstruction(BNFunction* func,
                                            BNArchitecture* arch,
                                            uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI BNInstructionTextLine* BNGetFunctionBlockAnnotations(
    BNFunction* func, BNArchitecture* arch, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNIntegerDisplayType BNGetIntegerConstantDisplayType(
    BNFunction* func, BNArchitecture* arch, uint64_t instrAddr, uint64_t value,
    size_t operand) {
  return {};
}
BINARYNINJACOREAPI void BNSetIntegerConstantDisplayType(
    BNFunction* func, BNArchitecture* arch, uint64_t instrAddr, uint64_t value,
    size_t operand, BNIntegerDisplayType type) {}
BINARYNINJACOREAPI bool BNIsFunctionTooLarge(BNFunction* func) { return {}; }
BINARYNINJACOREAPI bool BNIsFunctionAnalysisSkipped(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNAnalysisSkipReason
BNGetAnalysisSkipReason(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNFunctionAnalysisSkipOverride
BNGetFunctionAnalysisSkipOverride(BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNSetFunctionAnalysisSkipOverride(
    BNFunction* func, BNFunctionAnalysisSkipOverride skip) {}
BINARYNINJACOREAPI char* BNGetGotoLabelName(BNFunction* func,
                                            uint64_t labelId) {
  return {};
}
BINARYNINJACOREAPI void BNSetUserGotoLabelName(BNFunction* func,
                                               uint64_t labelId,
                                               const char* name) {}
BINARYNINJACOREAPI BNAnalysisParameters
BNGetParametersForAnalysis(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI void BNSetParametersForAnalysis(
    BNBinaryView* view, BNAnalysisParameters params) {}
BINARYNINJACOREAPI uint64_t
BNGetMaxFunctionSizeForAnalysis(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI void BNSetMaxFunctionSizeForAnalysis(BNBinaryView* view,
                                                        uint64_t size) {}
BINARYNINJACOREAPI bool BNGetNewAutoFunctionAnalysisSuppressed(
    BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI void BNSetNewAutoFunctionAnalysisSuppressed(
    BNBinaryView* view, bool suppress) {}
BINARYNINJACOREAPI BNAnalysisCompletionEvent* BNAddAnalysisCompletionEvent(
    BNBinaryView* view, void* ctxt, void (*callback)(void* ctxt)) {
  return {};
}
BINARYNINJACOREAPI BNAnalysisCompletionEvent*
BNNewAnalysisCompletionEventReference(BNAnalysisCompletionEvent* event) {
  return {};
}
BINARYNINJACOREAPI void BNFreeAnalysisCompletionEvent(
    BNAnalysisCompletionEvent* event) {}
BINARYNINJACOREAPI void BNCancelAnalysisCompletionEvent(
    BNAnalysisCompletionEvent* event) {}
BINARYNINJACOREAPI BNAnalysisInfo* BNGetAnalysisInfo(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI void BNFreeAnalysisInfo(BNAnalysisInfo* info) {}
BINARYNINJACOREAPI BNAnalysisProgress
BNGetAnalysisProgress(BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI BNBackgroundTask* BNGetBackgroundAnalysisTask(
    BNBinaryView* view) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNGetNextFunctionStartAfterAddress(BNBinaryView* view, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNGetNextBasicBlockStartAfterAddress(BNBinaryView* view, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetNextDataAfterAddress(BNBinaryView* view,
                                                      uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNGetNextDataVariableStartAfterAddress(BNBinaryView* view, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNGetPreviousFunctionStartBeforeAddress(BNBinaryView* view, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNGetPreviousBasicBlockStartBeforeAddress(BNBinaryView* view, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNGetPreviousBasicBlockEndBeforeAddress(BNBinaryView* view, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetPreviousDataBeforeAddress(BNBinaryView* view,
                                                           uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNGetPreviousDataVariableStartBeforeAddress(BNBinaryView* view, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNCreateLinearViewDisassembly(
    BNBinaryView* view, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNCreateLinearViewLiftedIL(
    BNBinaryView* view, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNCreateLinearViewLowLevelIL(
    BNBinaryView* view, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNCreateLinearViewLowLevelILSSAForm(
    BNBinaryView* view, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNCreateLinearViewMediumLevelIL(
    BNBinaryView* view, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNCreateLinearViewMediumLevelILSSAForm(
    BNBinaryView* view, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNCreateLinearViewMappedMediumLevelIL(
    BNBinaryView* view, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject*
BNCreateLinearViewMappedMediumLevelILSSAForm(BNBinaryView* view,
                                             BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNCreateLinearViewHighLevelIL(
    BNBinaryView* view, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNCreateLinearViewHighLevelILSSAForm(
    BNBinaryView* view, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNNewLinearViewObjectReference(
    BNLinearViewObject* obj) {
  return {};
}
BINARYNINJACOREAPI void BNFreeLinearViewObject(BNLinearViewObject* obj) {}
BINARYNINJACOREAPI BNLinearViewObject* BNGetFirstLinearViewObjectChild(
    BNLinearViewObject* obj) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNGetLastLinearViewObjectChild(
    BNLinearViewObject* obj) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNGetPreviousLinearViewObjectChild(
    BNLinearViewObject* parent, BNLinearViewObject* child) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNGetNextLinearViewObjectChild(
    BNLinearViewObject* parent, BNLinearViewObject* child) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNGetLinearViewObjectChildForAddress(
    BNLinearViewObject* parent, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNGetLinearViewObjectChildForIdentifier(
    BNLinearViewObject* parent, BNLinearViewObjectIdentifier* id) {
  return {};
}
BINARYNINJACOREAPI BNLinearDisassemblyLine* BNGetLinearViewObjectLines(
    BNLinearViewObject* obj, BNLinearViewObject* prev, BNLinearViewObject* next,
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeLinearDisassemblyLines(
    BNLinearDisassemblyLine* lines, size_t count) {}
BINARYNINJACOREAPI uint64_t
BNGetLinearViewObjectStart(BNLinearViewObject* obj) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetLinearViewObjectEnd(BNLinearViewObject* obj) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObjectIdentifier
BNGetLinearViewObjectIdentifier(BNLinearViewObject* obj) {
  return {};
}
BINARYNINJACOREAPI void BNFreeLinearViewObjectIdentifier(
    BNLinearViewObjectIdentifier* id) {}
BINARYNINJACOREAPI int BNCompareLinearViewObjectChildren(
    BNLinearViewObject* obj, BNLinearViewObject* a, BNLinearViewObject* b) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNGetLinearViewObjectOrderingIndexTotal(BNLinearViewObject* obj) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetLinearViewObjectOrderingIndexForChild(
    BNLinearViewObject* parent, BNLinearViewObject* child) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject*
BNGetLinearViewObjectChildForOrderingIndex(BNLinearViewObject* parent,
                                           uint64_t idx) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewCursor* BNCreateLinearViewCursor(
    BNLinearViewObject* root) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewCursor* BNDuplicateLinearViewCursor(
    BNLinearViewCursor* cursor) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewCursor* BNNewLinearViewCursorReference(
    BNLinearViewCursor* cursor) {
  return {};
}
BINARYNINJACOREAPI void BNFreeLinearViewCursor(BNLinearViewCursor* cursor) {}
BINARYNINJACOREAPI bool BNIsLinearViewCursorBeforeBegin(
    BNLinearViewCursor* cursor) {
  return {};
}
BINARYNINJACOREAPI bool BNIsLinearViewCursorAfterEnd(
    BNLinearViewCursor* cursor) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObject* BNGetLinearViewCursorCurrentObject(
    BNLinearViewCursor* cursor) {
  return {};
}
BINARYNINJACOREAPI BNLinearViewObjectIdentifier* BNGetLinearViewCursorPath(
    BNLinearViewCursor* cursor, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeLinearViewCursorPath(
    BNLinearViewObjectIdentifier* objs, size_t count) {}
BINARYNINJACOREAPI BNLinearViewObject** BNGetLinearViewCursorPathObjects(
    BNLinearViewCursor* cursor, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeLinearViewCursorPathObjects(
    BNLinearViewObject** objs, size_t count) {}
BINARYNINJACOREAPI BNAddressRange
BNGetLinearViewCursorOrderingIndex(BNLinearViewCursor* cursor) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNGetLinearViewCursorOrderingIndexTotal(BNLinearViewCursor* cursor) {
  return {};
}
BINARYNINJACOREAPI void BNSeekLinearViewCursorToBegin(
    BNLinearViewCursor* cursor) {}
BINARYNINJACOREAPI void BNSeekLinearViewCursorToEnd(
    BNLinearViewCursor* cursor) {}
BINARYNINJACOREAPI void BNSeekLinearViewCursorToAddress(
    BNLinearViewCursor* cursor, uint64_t addr) {}
BINARYNINJACOREAPI bool BNSeekLinearViewCursorToPath(
    BNLinearViewCursor* cursor, BNLinearViewObjectIdentifier* ids,
    size_t count) {
  return {};
}
BINARYNINJACOREAPI bool BNSeekLinearViewCursorToPathAndAddress(
    BNLinearViewCursor* cursor, BNLinearViewObjectIdentifier* ids, size_t count,
    uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNSeekLinearViewCursorToCursorPath(
    BNLinearViewCursor* cursor, BNLinearViewCursor* path) {
  return {};
}
BINARYNINJACOREAPI bool BNSeekLinearViewCursorToCursorPathAndAddress(
    BNLinearViewCursor* cursor, BNLinearViewCursor* path, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI void BNSeekLinearViewCursorToOrderingIndex(
    BNLinearViewCursor* cursor, uint64_t idx) {}
BINARYNINJACOREAPI bool BNLinearViewCursorNext(BNLinearViewCursor* cursor) {
  return {};
}
BINARYNINJACOREAPI bool BNLinearViewCursorPrevious(BNLinearViewCursor* cursor) {
  return {};
}
BINARYNINJACOREAPI BNLinearDisassemblyLine* BNGetLinearViewCursorLines(
    BNLinearViewCursor* cursor, size_t* count) {
  return {};
}
BINARYNINJACOREAPI int BNCompareLinearViewCursors(BNLinearViewCursor* a,
                                                  BNLinearViewCursor* b) {
  return {};
}
BINARYNINJACOREAPI void BNDefineDataVariable(BNBinaryView* view, uint64_t addr,
                                             BNTypeWithConfidence* type) {}
BINARYNINJACOREAPI void BNDefineUserDataVariable(BNBinaryView* view,
                                                 uint64_t addr,
                                                 BNTypeWithConfidence* type) {}
BINARYNINJACOREAPI void BNUndefineDataVariable(BNBinaryView* view,
                                               uint64_t addr) {}
BINARYNINJACOREAPI void BNUndefineUserDataVariable(BNBinaryView* view,
                                                   uint64_t addr) {}
BINARYNINJACOREAPI BNDataVariable* BNGetDataVariables(BNBinaryView* view,
                                                      size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeDataVariables(BNDataVariable* vars,
                                            size_t count) {}
BINARYNINJACOREAPI bool BNGetDataVariableAtAddress(BNBinaryView* view,
                                                   uint64_t addr,
                                                   BNDataVariable* var) {
  return {};
}
BINARYNINJACOREAPI bool BNParseTypeString(
    BNBinaryView* view, const char* text, BNQualifiedNameAndType* result,
    char** errors, BNQualifiedNameList* typesAllowRedefinition) {
  return {};
}
BINARYNINJACOREAPI bool BNParseTypesString(
    BNBinaryView* view, const char* text, BNTypeParserResult* result,
    char** errors, BNQualifiedNameList* typesAllowRedefinition) {
  return {};
}
BINARYNINJACOREAPI void BNFreeQualifiedNameAndType(
    BNQualifiedNameAndType* obj) {}
BINARYNINJACOREAPI void BNFreeQualifiedNameAndTypeArray(
    BNQualifiedNameAndType* obj, size_t count) {}
BINARYNINJACOREAPI BNQualifiedNameAndType* BNGetAnalysisTypeList(
    BNBinaryView* view, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTypeList(BNQualifiedNameAndType* types,
                                       size_t count) {}
BINARYNINJACOREAPI BNQualifiedName* BNGetAnalysisTypeNames(
    BNBinaryView* view, size_t* count, const char* matching) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTypeNameList(BNQualifiedName* names,
                                           size_t count) {}
BINARYNINJACOREAPI BNType* BNGetAnalysisTypeByName(BNBinaryView* view,
                                                   BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNType* BNGetAnalysisTypeById(BNBinaryView* view,
                                                 const char* id) {
  return {};
}
BINARYNINJACOREAPI char* BNGetAnalysisTypeId(BNBinaryView* view,
                                             BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedName BNGetAnalysisTypeNameById(BNBinaryView* view,
                                                             const char* id) {
  return {};
}
BINARYNINJACOREAPI bool BNIsAnalysisTypeAutoDefined(BNBinaryView* view,
                                                    BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedName
BNDefineAnalysisType(BNBinaryView* view, const char* id,
                     BNQualifiedName* defaultName, BNType* type) {
  return {};
}
BINARYNINJACOREAPI void BNDefineUserAnalysisType(BNBinaryView* view,
                                                 BNQualifiedName* name,
                                                 BNType* type) {}
BINARYNINJACOREAPI void BNUndefineAnalysisType(BNBinaryView* view,
                                               const char* id) {}
BINARYNINJACOREAPI void BNUndefineUserAnalysisType(BNBinaryView* view,
                                                   BNQualifiedName* name) {}
BINARYNINJACOREAPI void BNRenameAnalysisType(BNBinaryView* view,
                                             BNQualifiedName* oldName,
                                             BNQualifiedName* newName) {}
BINARYNINJACOREAPI char* BNGenerateAutoTypeId(const char* source,
                                              BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI char* BNGenerateAutoPlatformTypeId(BNPlatform* platform,
                                                      BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI char* BNGenerateAutoDemangledTypeId(BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI char* BNGetAutoPlatformTypeIdSource(BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI char* BNGetAutoDemangledTypeIdSource(void) { return {}; }
BINARYNINJACOREAPI char* BNGenerateAutoDebugTypeId(BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI char* BNGetAutoDebugTypeIdSource(void) { return {}; }
BINARYNINJACOREAPI void BNRegisterPlatformTypes(BNBinaryView* view,
                                                BNPlatform* platform) {}
BINARYNINJACOREAPI void BNReanalyzeAllFunctions(BNBinaryView* view) {}
BINARYNINJACOREAPI void BNReanalyzeFunction(BNFunction* func) {}
BINARYNINJACOREAPI BNHighlightColor BNGetInstructionHighlight(
    BNFunction* func, BNArchitecture* arch, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI void BNSetAutoInstructionHighlight(BNFunction* func,
                                                      BNArchitecture* arch,
                                                      uint64_t addr,
                                                      BNHighlightColor color) {}
BINARYNINJACOREAPI void BNSetUserInstructionHighlight(BNFunction* func,
                                                      BNArchitecture* arch,
                                                      uint64_t addr,
                                                      BNHighlightColor color) {}
BINARYNINJACOREAPI BNHighlightColor
BNGetBasicBlockHighlight(BNBasicBlock* block) {
  return {};
}
BINARYNINJACOREAPI void BNSetAutoBasicBlockHighlight(BNBasicBlock* block,
                                                     BNHighlightColor color) {}
BINARYNINJACOREAPI void BNSetUserBasicBlockHighlight(BNBasicBlock* block,
                                                     BNHighlightColor color) {}
BINARYNINJACOREAPI BNTagType* BNCreateTagType(BNBinaryView* view) { return {}; }
BINARYNINJACOREAPI BNTagType* BNNewTagTypeReference(BNTagType* tagType) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTagType(BNTagType* tagType) {}
BINARYNINJACOREAPI void BNFreeTagTypeList(BNTagType** tagTypes, size_t count) {}
BINARYNINJACOREAPI BNBinaryView* BNTagTypeGetView(BNTagType* tagType) {
  return {};
}
BINARYNINJACOREAPI char* BNTagTypeGetName(BNTagType* tagType) { return {}; }
BINARYNINJACOREAPI void BNTagTypeSetName(BNTagType* tagType, const char* name) {
}
BINARYNINJACOREAPI char* BNTagTypeGetIcon(BNTagType* tagType) { return {}; }
BINARYNINJACOREAPI void BNTagTypeSetIcon(BNTagType* tagType, const char* icon) {
}
BINARYNINJACOREAPI bool BNTagTypeGetVisible(BNTagType* tagType) { return {}; }
BINARYNINJACOREAPI void BNTagTypeSetVisible(BNTagType* tagType, bool visible) {}
BINARYNINJACOREAPI BNTagTypeType BNTagTypeGetType(BNTagType* tagType) {
  return {};
}
BINARYNINJACOREAPI void BNTagTypeSetType(BNTagType* tagType,
                                         BNTagTypeType type) {}
BINARYNINJACOREAPI BNTag* BNCreateTag(BNTagType* type, const char* data) {
  return {};
}
BINARYNINJACOREAPI BNTag* BNNewTagReference(BNTag* tag) { return {}; }
BINARYNINJACOREAPI void BNFreeTag(BNTag* tag) {}
BINARYNINJACOREAPI void BNFreeTagList(BNTag** tags, size_t count) {}
BINARYNINJACOREAPI BNTagType* BNTagGetType(BNTag* tag) { return {}; }
BINARYNINJACOREAPI char* BNTagGetData(BNTag* tag) { return {}; }
BINARYNINJACOREAPI void BNTagSetData(BNTag* tag, const char* data) {}
BINARYNINJACOREAPI void BNAddTagType(BNBinaryView* view, BNTagType* tagType) {}
BINARYNINJACOREAPI void BNRemoveTagType(BNBinaryView* view,
                                        BNTagType* tagType) {}
BINARYNINJACOREAPI BNTagType* BNGetTagType(BNBinaryView* view,
                                           const char* name) {
  return {};
}
BINARYNINJACOREAPI BNTagType* BNGetTagTypeWithType(BNBinaryView* view,
                                                   const char* name,
                                                   BNTagTypeType type) {
  return {};
}
BINARYNINJACOREAPI BNTagType** BNGetTagTypes(BNBinaryView* view,
                                             size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNAddTag(BNBinaryView* view, BNTag* tag, bool user) {}
BINARYNINJACOREAPI BNTag* BNGetTag(BNBinaryView* view, uint64_t tagId) {
  return {};
}
BINARYNINJACOREAPI void BNRemoveTag(BNBinaryView* view, BNTag* tag, bool user) {
}
BINARYNINJACOREAPI BNTagReference* BNGetAllTagReferences(BNBinaryView* view,
                                                         size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTagReference* BNGetAllAddressTagReferences(
    BNBinaryView* view, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTagReference* BNGetAllFunctionTagReferences(
    BNBinaryView* view, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTagReference* BNGetAllTagReferencesOfType(
    BNBinaryView* view, BNTagType* tagType, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTagReference* BNGetTagReferencesOfType(BNBinaryView* view,
                                                            BNTagType* tagType,
                                                            size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTagReference* BNGetDataTagReferences(BNBinaryView* view,
                                                          size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTagReferences(BNTagReference* refs,
                                            size_t count) {}
BINARYNINJACOREAPI BNTag** BNGetDataTags(BNBinaryView* view, uint64_t addr,
                                         size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTag** BNGetDataTagsOfType(BNBinaryView* view,
                                               uint64_t addr,
                                               BNTagType* tagType,
                                               size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTag** BNGetDataTagsInRange(BNBinaryView* view,
                                                uint64_t start, uint64_t end,
                                                size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNAddAutoDataTag(BNBinaryView* view, uint64_t addr,
                                         BNTag* tag) {}
BINARYNINJACOREAPI void BNRemoveAutoDataTag(BNBinaryView* view, uint64_t addr,
                                            BNTag* tag) {}
BINARYNINJACOREAPI void BNAddUserDataTag(BNBinaryView* view, uint64_t addr,
                                         BNTag* tag) {}
BINARYNINJACOREAPI void BNRemoveUserDataTag(BNBinaryView* view, uint64_t addr,
                                            BNTag* tag) {}
BINARYNINJACOREAPI void BNRemoveTagReference(BNBinaryView* view,
                                             BNTagReference ref) {}
BINARYNINJACOREAPI size_t BNGetTagReferencesOfTypeCount(BNBinaryView* view,
                                                        BNTagType* tagType) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetAllTagReferencesOfTypeCount(BNBinaryView* view,
                                                           BNTagType* tagType) {
  return {};
}
BINARYNINJACOREAPI BNTagReference* BNGetFunctionAllTagReferences(
    BNFunction* func, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTagReference* BNGetFunctionTagReferencesOfType(
    BNFunction* func, BNTagType* tagType, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTagReference* BNGetAddressTagReferences(BNFunction* func,
                                                             size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTag** BNGetAddressTags(BNFunction* func,
                                            BNArchitecture* arch, uint64_t addr,
                                            size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTag** BNGetAddressTagsOfType(BNFunction* func,
                                                  BNArchitecture* arch,
                                                  uint64_t addr,
                                                  BNTagType* tagType,
                                                  size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNAddAutoAddressTag(BNFunction* func,
                                            BNArchitecture* arch, uint64_t addr,
                                            BNTag* tag) {}
BINARYNINJACOREAPI void BNRemoveAutoAddressTag(BNFunction* func,
                                               BNArchitecture* arch,
                                               uint64_t addr, BNTag* tag) {}
BINARYNINJACOREAPI void BNAddUserAddressTag(BNFunction* func,
                                            BNArchitecture* arch, uint64_t addr,
                                            BNTag* tag) {}
BINARYNINJACOREAPI void BNRemoveUserAddressTag(BNFunction* func,
                                               BNArchitecture* arch,
                                               uint64_t addr, BNTag* tag) {}
BINARYNINJACOREAPI BNTagReference* BNGetFunctionTagReferences(BNFunction* func,
                                                              size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTag** BNGetFunctionTags(BNFunction* func, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTag** BNGetFunctionTagsOfType(BNFunction* func,
                                                   BNTagType* tagType,
                                                   size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNAddAutoFunctionTag(BNFunction* func, BNTag* tag) {}
BINARYNINJACOREAPI void BNRemoveAutoFunctionTag(BNFunction* func, BNTag* tag) {}
BINARYNINJACOREAPI void BNAddUserFunctionTag(BNFunction* func, BNTag* tag) {}
BINARYNINJACOREAPI void BNRemoveUserFunctionTag(BNFunction* func, BNTag* tag) {}
BINARYNINJACOREAPI BNPerformanceInfo* BNGetFunctionAnalysisPerformanceInfo(
    BNFunction* func, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeAnalysisPerformanceInfo(BNPerformanceInfo* info,
                                                      size_t count) {}
BINARYNINJACOREAPI BNFlowGraph* BNGetUnresolvedStackAdjustmentGraph(
    BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNSetUserVariableValue(
    BNFunction* func, const BNVariable* var,
    const BNArchitectureAndAddress* defSite, const BNPossibleValueSet* value) {}
BINARYNINJACOREAPI void BNClearUserVariableValue(
    BNFunction* func, const BNVariable* var,
    const BNArchitectureAndAddress* defSite) {}
BINARYNINJACOREAPI BNUserVariableValue* BNGetAllUserVariableValues(
    BNFunction* func, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeUserVariableValues(BNUserVariableValue* result) {}
BINARYNINJACOREAPI bool BNParsePossibleValueSet(BNBinaryView* view,
                                                const char* valueText,
                                                BNRegisterValueType state,
                                                BNPossibleValueSet* result,
                                                uint64_t here, char** errors) {
  return {};
}
BINARYNINJACOREAPI void BNRequestFunctionDebugReport(BNFunction* func,
                                                     const char* name) {}
BINARYNINJACOREAPI BNDisassemblySettings* BNCreateDisassemblySettings(void) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblySettings* BNNewDisassemblySettingsReference(
    BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI void BNFreeDisassemblySettings(
    BNDisassemblySettings* settings) {}
BINARYNINJACOREAPI bool BNIsDisassemblySettingsOptionSet(
    BNDisassemblySettings* settings, BNDisassemblyOption option) {
  return {};
}
BINARYNINJACOREAPI void BNSetDisassemblySettingsOption(
    BNDisassemblySettings* settings, BNDisassemblyOption option, bool state) {}
BINARYNINJACOREAPI size_t
BNGetDisassemblyWidth(BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI void BNSetDisassemblyWidth(BNDisassemblySettings* settings,
                                              size_t width) {}
BINARYNINJACOREAPI size_t
BNGetDisassemblyMaximumSymbolWidth(BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI void BNSetDisassemblyMaximumSymbolWidth(
    BNDisassemblySettings* settings, size_t width) {}
BINARYNINJACOREAPI size_t
BNGetDisassemblyGutterWidth(BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI void BNSetDisassemblyGutterWidth(
    BNDisassemblySettings* settings, size_t width) {}
BINARYNINJACOREAPI BNFlowGraph* BNCreateFlowGraph() { return {}; }
BINARYNINJACOREAPI BNFlowGraph* BNCreateFunctionGraph(
    BNFunction* func, BNFunctionGraphType type,
    BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraph* BNCreateLowLevelILFunctionGraph(
    BNLowLevelILFunction* func, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraph* BNCreateMediumLevelILFunctionGraph(
    BNMediumLevelILFunction* func, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraph* BNCreateHighLevelILFunctionGraph(
    BNHighLevelILFunction* func, BNDisassemblySettings* settings) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraph* BNCreateCustomFlowGraph(
    BNCustomFlowGraph* callbacks) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraph* BNNewFlowGraphReference(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI void BNFreeFlowGraph(BNFlowGraph* graph) {}
BINARYNINJACOREAPI BNFunction* BNGetFunctionForFlowGraph(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI void BNSetFunctionForFlowGraph(BNFlowGraph* graph,
                                                  BNFunction* func) {}
BINARYNINJACOREAPI BNBinaryView* BNGetViewForFlowGraph(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI void BNSetViewForFlowGraph(BNFlowGraph* graph,
                                              BNBinaryView* view) {}
BINARYNINJACOREAPI int BNGetHorizontalFlowGraphNodeMargin(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI int BNGetVerticalFlowGraphNodeMargin(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI void BNSetFlowGraphNodeMargins(BNFlowGraph* graph, int horiz,
                                                  int vert) {}
BINARYNINJACOREAPI BNFlowGraphLayoutRequest* BNStartFlowGraphLayout(
    BNFlowGraph* graph, void* ctxt, void (*func)(void* ctxt)) {
  return {};
}
BINARYNINJACOREAPI bool BNIsFlowGraphLayoutComplete(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraphLayoutRequest*
BNNewFlowGraphLayoutRequestReference(BNFlowGraphLayoutRequest* layout) {
  return {};
}
BINARYNINJACOREAPI void BNFreeFlowGraphLayoutRequest(
    BNFlowGraphLayoutRequest* layout) {}
BINARYNINJACOREAPI bool BNIsFlowGraphLayoutRequestComplete(
    BNFlowGraphLayoutRequest* layout) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraph* BNGetGraphForFlowGraphLayoutRequest(
    BNFlowGraphLayoutRequest* layout) {
  return {};
}
BINARYNINJACOREAPI void BNAbortFlowGraphLayoutRequest(
    BNFlowGraphLayoutRequest* graph) {}
BINARYNINJACOREAPI bool BNIsILFlowGraph(BNFlowGraph* graph) { return {}; }
BINARYNINJACOREAPI bool BNIsLowLevelILFlowGraph(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI bool BNIsMediumLevelILFlowGraph(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI bool BNIsHighLevelILFlowGraph(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI BNLowLevelILFunction* BNGetFlowGraphLowLevelILFunction(
    BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction* BNGetFlowGraphMediumLevelILFunction(
    BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI BNHighLevelILFunction* BNGetFlowGraphHighLevelILFunction(
    BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI void BNSetFlowGraphLowLevelILFunction(
    BNFlowGraph* graph, BNLowLevelILFunction* func) {}
BINARYNINJACOREAPI void BNSetFlowGraphMediumLevelILFunction(
    BNFlowGraph* graph, BNMediumLevelILFunction* func) {}
BINARYNINJACOREAPI void BNSetFlowGraphHighLevelILFunction(
    BNFlowGraph* graph, BNHighLevelILFunction* func) {}
BINARYNINJACOREAPI BNFlowGraphNode** BNGetFlowGraphNodes(BNFlowGraph* graph,
                                                         size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraphNode* BNGetFlowGraphNode(BNFlowGraph* graph,
                                                       size_t i) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraphNode** BNGetFlowGraphNodesInRegion(
    BNFlowGraph* graph, int left, int top, int right, int bottom,
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeFlowGraphNodeList(BNFlowGraphNode** nodes,
                                                size_t count) {}
BINARYNINJACOREAPI bool BNFlowGraphHasNodes(BNFlowGraph* graph) { return {}; }
BINARYNINJACOREAPI size_t BNAddFlowGraphNode(BNFlowGraph* graph,
                                             BNFlowGraphNode* node) {
  return {};
}
BINARYNINJACOREAPI int BNGetFlowGraphWidth(BNFlowGraph* graph) { return {}; }
BINARYNINJACOREAPI int BNGetFlowGraphHeight(BNFlowGraph* graph) { return {}; }
BINARYNINJACOREAPI BNFlowGraphNode* BNCreateFlowGraphNode(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraphNode* BNNewFlowGraphNodeReference(
    BNFlowGraphNode* node) {
  return {};
}
BINARYNINJACOREAPI void BNFreeFlowGraphNode(BNFlowGraphNode* node) {}
BINARYNINJACOREAPI BNFlowGraph* BNGetFlowGraphNodeOwner(BNFlowGraphNode* node) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock* BNGetFlowGraphBasicBlock(
    BNFlowGraphNode* node) {
  return {};
}
BINARYNINJACOREAPI void BNSetFlowGraphBasicBlock(BNFlowGraphNode* node,
                                                 BNBasicBlock* block) {}
BINARYNINJACOREAPI int BNGetFlowGraphNodeX(BNFlowGraphNode* node) { return {}; }
BINARYNINJACOREAPI int BNGetFlowGraphNodeY(BNFlowGraphNode* node) { return {}; }
BINARYNINJACOREAPI int BNGetFlowGraphNodeWidth(BNFlowGraphNode* node) {
  return {};
}
BINARYNINJACOREAPI int BNGetFlowGraphNodeHeight(BNFlowGraphNode* node) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextLine* BNGetFlowGraphNodeLines(
    BNFlowGraphNode* node, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNSetFlowGraphNodeLines(BNFlowGraphNode* node,
                                                BNDisassemblyTextLine* lines,
                                                size_t count) {}
BINARYNINJACOREAPI BNFlowGraphEdge* BNGetFlowGraphNodeOutgoingEdges(
    BNFlowGraphNode* node, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraphEdge* BNGetFlowGraphNodeIncomingEdges(
    BNFlowGraphNode* node, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeFlowGraphNodeEdgeList(BNFlowGraphEdge* edges,
                                                    size_t count) {}
BINARYNINJACOREAPI void BNAddFlowGraphNodeOutgoingEdge(BNFlowGraphNode* node,
                                                       BNBranchType type,
                                                       BNFlowGraphNode* target,
                                                       BNEdgeStyle edgeStyle) {}
BINARYNINJACOREAPI BNHighlightColor
BNGetFlowGraphNodeHighlight(BNFlowGraphNode* node) {
  return {};
}
BINARYNINJACOREAPI void BNSetFlowGraphNodeHighlight(BNFlowGraphNode* node,
                                                    BNHighlightColor color) {}
BINARYNINJACOREAPI void BNFinishPrepareForLayout(BNFlowGraph* graph) {}
BINARYNINJACOREAPI BNFlowGraph* BNUpdateFlowGraph(BNFlowGraph* graph) {
  return {};
}
BINARYNINJACOREAPI void BNSetFlowGraphOption(BNFlowGraph* graph,
                                             BNFlowGraphOption option,
                                             bool value) {}
BINARYNINJACOREAPI bool BNIsFlowGraphOptionSet(BNFlowGraph* graph,
                                               BNFlowGraphOption option) {
  return {};
}
BINARYNINJACOREAPI bool BNIsNodeValidForFlowGraph(BNFlowGraph* graph,
                                                  BNFlowGraphNode* node) {
  return {};
}
BINARYNINJACOREAPI BNSymbol* BNCreateSymbol(
    BNSymbolType type, const char* shortName, const char* fullName,
    const char* rawName, uint64_t addr, BNSymbolBinding binding,
    const BNNameSpace* nameSpace, uint64_t ordinal) {
  return {};
}
BINARYNINJACOREAPI BNSymbol* BNNewSymbolReference(BNSymbol* sym) { return {}; }
BINARYNINJACOREAPI void BNFreeSymbol(BNSymbol* sym) {}
BINARYNINJACOREAPI BNSymbolType BNGetSymbolType(BNSymbol* sym) { return {}; }
BINARYNINJACOREAPI BNSymbolBinding BNGetSymbolBinding(BNSymbol* sym) {
  return {};
}
BINARYNINJACOREAPI BNNameSpace BNGetSymbolNameSpace(BNSymbol* sym) {
  return {};
}
BINARYNINJACOREAPI char* BNGetSymbolShortName(BNSymbol* sym) { return {}; }
BINARYNINJACOREAPI char* BNGetSymbolFullName(BNSymbol* sym) { return {}; }
BINARYNINJACOREAPI char* BNGetSymbolRawName(BNSymbol* sym) { return {}; }
BINARYNINJACOREAPI uint64_t BNGetSymbolAddress(BNSymbol* sym) { return {}; }
BINARYNINJACOREAPI uint64_t BNGetSymbolOrdinal(BNSymbol* sym) { return {}; }
BINARYNINJACOREAPI bool BNIsSymbolAutoDefined(BNSymbol* sym) { return {}; }
BINARYNINJACOREAPI char** BNGetSymbolAliases(BNSymbol* sym, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNSymbol* BNGetSymbolByAddress(
    BNBinaryView* view, uint64_t addr, const BNNameSpace* nameSpace) {
  return {};
}
BINARYNINJACOREAPI BNSymbol* BNGetSymbolByRawName(
    BNBinaryView* view, const char* name, const BNNameSpace* nameSpace) {
  return {};
}
BINARYNINJACOREAPI BNSymbol** BNGetSymbolsByName(BNBinaryView* view,
                                                 const char* name,
                                                 size_t* count,
                                                 const BNNameSpace* nameSpace) {
  return {};
}
BINARYNINJACOREAPI BNSymbol** BNGetSymbols(BNBinaryView* view, size_t* count,
                                           const BNNameSpace* nameSpace) {
  return {};
}
BINARYNINJACOREAPI BNSymbol** BNGetSymbolsInRange(
    BNBinaryView* view, uint64_t start, uint64_t len, size_t* count,
    const BNNameSpace* nameSpace) {
  return {};
}
BINARYNINJACOREAPI BNSymbol** BNGetSymbolsOfType(BNBinaryView* view,
                                                 BNSymbolType type,
                                                 size_t* count,
                                                 const BNNameSpace* nameSpace) {
  return {};
}
BINARYNINJACOREAPI BNSymbol** BNGetSymbolsOfTypeInRange(
    BNBinaryView* view, BNSymbolType type, uint64_t start, uint64_t len,
    size_t* count, const BNNameSpace* nameSpace) {
  return {};
}
BINARYNINJACOREAPI void BNFreeSymbolList(BNSymbol** syms, size_t count) {}
BINARYNINJACOREAPI BNSymbol** BNGetVisibleSymbols(
    BNBinaryView* view, size_t* count, const BNNameSpace* nameSpace) {
  return {};
}
BINARYNINJACOREAPI void BNDefineAutoSymbol(BNBinaryView* view, BNSymbol* sym) {}
BINARYNINJACOREAPI void BNUndefineAutoSymbol(BNBinaryView* view,
                                             BNSymbol* sym) {}
BINARYNINJACOREAPI void BNDefineUserSymbol(BNBinaryView* view, BNSymbol* sym) {}
BINARYNINJACOREAPI void BNUndefineUserSymbol(BNBinaryView* view,
                                             BNSymbol* sym) {}
BINARYNINJACOREAPI void BNDefineImportedFunction(BNBinaryView* view,
                                                 BNSymbol* importAddressSym,
                                                 BNFunction* func,
                                                 BNType* type) {}
BINARYNINJACOREAPI void BNDefineAutoSymbolAndVariableOrFunction(
    BNBinaryView* view, BNPlatform* platform, BNSymbol* sym, BNType* type) {}
BINARYNINJACOREAPI BNSymbol* BNImportedFunctionFromImportAddressSymbol(
    BNSymbol* sym, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI BNLowLevelILFunction* BNCreateLowLevelILFunction(
    BNArchitecture* arch, BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNLowLevelILFunction* BNNewLowLevelILFunctionReference(
    BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNFreeLowLevelILFunction(BNLowLevelILFunction* func) {}
BINARYNINJACOREAPI BNFunction* BNGetLowLevelILOwnerFunction(
    BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNLowLevelILGetCurrentAddress(BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNLowLevelILSetCurrentAddress(
    BNLowLevelILFunction* func, BNArchitecture* arch, uint64_t addr) {}
BINARYNINJACOREAPI void BNLowLevelILSetCurrentSourceBlock(
    BNLowLevelILFunction* func, BNBasicBlock* source) {}
BINARYNINJACOREAPI size_t BNLowLevelILGetInstructionStart(
    BNLowLevelILFunction* func, BNArchitecture* arch, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI void BNLowLevelILClearIndirectBranches(
    BNLowLevelILFunction* func) {}
BINARYNINJACOREAPI void BNLowLevelILSetIndirectBranches(
    BNLowLevelILFunction* func, BNArchitectureAndAddress* branches,
    size_t count) {}
BINARYNINJACOREAPI size_t BNLowLevelILAddExpr(BNLowLevelILFunction* func,
                                              BNLowLevelILOperation operation,
                                              size_t size, uint32_t flags,
                                              uint64_t a, uint64_t b,
                                              uint64_t c, uint64_t d) {
  return {};
}
BINARYNINJACOREAPI size_t BNLowLevelILAddExprWithLocation(
    BNLowLevelILFunction* func, uint64_t addr, uint32_t sourceOperand,
    BNLowLevelILOperation operation, size_t size, uint32_t flags, uint64_t a,
    uint64_t b, uint64_t c, uint64_t d) {
  return {};
}
BINARYNINJACOREAPI void BNLowLevelILSetExprSourceOperand(
    BNLowLevelILFunction* func, size_t expr, uint32_t operand) {}
BINARYNINJACOREAPI size_t BNLowLevelILAddInstruction(BNLowLevelILFunction* func,
                                                     size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t BNLowLevelILGoto(BNLowLevelILFunction* func,
                                           BNLowLevelILLabel* label) {
  return {};
}
BINARYNINJACOREAPI size_t BNLowLevelILGotoWithLocation(
    BNLowLevelILFunction* func, BNLowLevelILLabel* label, uint64_t addr,
    uint32_t sourceOperand) {
  return {};
}
BINARYNINJACOREAPI size_t BNLowLevelILIf(BNLowLevelILFunction* func,
                                         uint64_t op, BNLowLevelILLabel* t,
                                         BNLowLevelILLabel* f) {
  return {};
}
BINARYNINJACOREAPI size_t BNLowLevelILIfWithLocation(
    BNLowLevelILFunction* func, uint64_t op, BNLowLevelILLabel* t,
    BNLowLevelILLabel* f, uint64_t addr, uint32_t sourceOperand) {
  return {};
}
BINARYNINJACOREAPI void BNLowLevelILInitLabel(BNLowLevelILLabel* label) {}
BINARYNINJACOREAPI void BNLowLevelILMarkLabel(BNLowLevelILFunction* func,
                                              BNLowLevelILLabel* label) {}
BINARYNINJACOREAPI void BNFinalizeLowLevelILFunction(
    BNLowLevelILFunction* func) {}
BINARYNINJACOREAPI void BNPrepareToCopyLowLevelILFunction(
    BNLowLevelILFunction* func, BNLowLevelILFunction* src) {}
BINARYNINJACOREAPI void BNPrepareToCopyLowLevelILBasicBlock(
    BNLowLevelILFunction* func, BNBasicBlock* block) {}
BINARYNINJACOREAPI BNLowLevelILLabel* BNGetLabelForLowLevelILSourceInstruction(
    BNLowLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t BNLowLevelILAddLabelMap(BNLowLevelILFunction* func,
                                                  uint64_t* values,
                                                  BNLowLevelILLabel** labels,
                                                  size_t count) {
  return {};
}
BINARYNINJACOREAPI size_t BNLowLevelILAddOperandList(BNLowLevelILFunction* func,
                                                     uint64_t* operands,
                                                     size_t count) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNLowLevelILGetOperandList(
    BNLowLevelILFunction* func, size_t expr, size_t operand, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNLowLevelILFreeOperandList(uint64_t* operands) {}
BINARYNINJACOREAPI BNLowLevelILInstruction
BNGetLowLevelILByIndex(BNLowLevelILFunction* func, size_t i) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetLowLevelILIndexForInstruction(BNLowLevelILFunction* func, size_t i) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetLowLevelILInstructionForExpr(BNLowLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetLowLevelILInstructionCount(BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetLowLevelILExprCount(BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNUpdateLowLevelILOperand(BNLowLevelILFunction* func,
                                                  size_t instr,
                                                  size_t operandIndex,
                                                  uint64_t value) {}
BINARYNINJACOREAPI void BNReplaceLowLevelILExpr(BNLowLevelILFunction* func,
                                                size_t expr, size_t newExpr) {}
BINARYNINJACOREAPI void BNAddLowLevelILLabelForAddress(
    BNLowLevelILFunction* func, BNArchitecture* arch, uint64_t addr) {}
BINARYNINJACOREAPI BNLowLevelILLabel* BNGetLowLevelILLabelForAddress(
    BNLowLevelILFunction* func, BNArchitecture* arch, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI bool BNGetLowLevelILExprText(BNLowLevelILFunction* func,
                                                BNArchitecture* arch, size_t i,
                                                BNInstructionTextToken** tokens,
                                                size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNGetLowLevelILInstructionText(
    BNLowLevelILFunction* il, BNFunction* func, BNArchitecture* arch, size_t i,
    BNInstructionTextToken** tokens, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t
BNGetLowLevelILTemporaryRegisterCount(BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI uint32_t
BNGetLowLevelILTemporaryFlagCount(BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock** BNGetLowLevelILBasicBlockList(
    BNLowLevelILFunction* func, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock* BNGetLowLevelILBasicBlockForInstruction(
    BNLowLevelILFunction* func, size_t i) {
  return {};
}
BINARYNINJACOREAPI BNLowLevelILFunction* BNGetLowLevelILSSAForm(
    BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNLowLevelILFunction* BNGetLowLevelILNonSSAForm(
    BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetLowLevelILSSAInstructionIndex(BNLowLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetLowLevelILNonSSAInstructionIndex(
    BNLowLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetLowLevelILSSAExprIndex(BNLowLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetLowLevelILNonSSAExprIndex(BNLowLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetLowLevelILSSARegisterDefinition(
    BNLowLevelILFunction* func, uint32_t reg, size_t version) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetLowLevelILSSAFlagDefinition(
    BNLowLevelILFunction* func, uint32_t reg, size_t version) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetLowLevelILSSAMemoryDefinition(BNLowLevelILFunction* func, size_t version) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetLowLevelILSSARegisterUses(
    BNLowLevelILFunction* func, uint32_t reg, size_t version, size_t* count) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetLowLevelILSSAFlagUses(
    BNLowLevelILFunction* func, uint32_t reg, size_t version, size_t* count) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetLowLevelILSSAMemoryUses(
    BNLowLevelILFunction* func, size_t version, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetLowLevelILSSARegisterValue(
    BNLowLevelILFunction* func, uint32_t reg, size_t version) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetLowLevelILSSAFlagValue(
    BNLowLevelILFunction* func, uint32_t flag, size_t version) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue
BNGetLowLevelILExprValue(BNLowLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet BNGetLowLevelILPossibleExprValues(
    BNLowLevelILFunction* func, size_t expr, BNDataFlowQueryOption* options,
    size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetLowLevelILRegisterValueAtInstruction(
    BNLowLevelILFunction* func, uint32_t reg, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetLowLevelILRegisterValueAfterInstruction(
    BNLowLevelILFunction* func, uint32_t reg, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetLowLevelILPossibleRegisterValuesAtInstruction(
    BNLowLevelILFunction* func, uint32_t reg, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetLowLevelILPossibleRegisterValuesAfterInstruction(
    BNLowLevelILFunction* func, uint32_t reg, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetLowLevelILFlagValueAtInstruction(
    BNLowLevelILFunction* func, uint32_t flag, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetLowLevelILFlagValueAfterInstruction(
    BNLowLevelILFunction* func, uint32_t flag, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetLowLevelILPossibleFlagValuesAtInstruction(BNLowLevelILFunction* func,
                                               uint32_t flag, size_t instr,
                                               BNDataFlowQueryOption* options,
                                               size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetLowLevelILPossibleFlagValuesAfterInstruction(
    BNLowLevelILFunction* func, uint32_t flag, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetLowLevelILStackContentsAtInstruction(
    BNLowLevelILFunction* func, int64_t offset, size_t len, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetLowLevelILStackContentsAfterInstruction(
    BNLowLevelILFunction* func, int64_t offset, size_t len, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetLowLevelILPossibleStackContentsAtInstruction(
    BNLowLevelILFunction* func, int64_t offset, size_t len, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetLowLevelILPossibleStackContentsAfterInstruction(
    BNLowLevelILFunction* func, int64_t offset, size_t len, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction* BNGetMediumLevelILForLowLevelIL(
    BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction* BNGetMappedMediumLevelIL(
    BNLowLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetMediumLevelILInstructionIndex(BNLowLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetMediumLevelILExprIndex(BNLowLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetMediumLevelILExprIndexes(
    BNLowLevelILFunction* func, size_t expr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetMappedMediumLevelILInstructionIndex(
    BNLowLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetMappedMediumLevelILExprIndex(BNLowLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction* BNCreateMediumLevelILFunction(
    BNArchitecture* arch, BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction* BNNewMediumLevelILFunctionReference(
    BNMediumLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNFreeMediumLevelILFunction(
    BNMediumLevelILFunction* func) {}
BINARYNINJACOREAPI BNFunction* BNGetMediumLevelILOwnerFunction(
    BNMediumLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNMediumLevelILGetCurrentAddress(BNMediumLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNMediumLevelILSetCurrentAddress(
    BNMediumLevelILFunction* func, BNArchitecture* arch, uint64_t addr) {}
BINARYNINJACOREAPI size_t BNMediumLevelILGetInstructionStart(
    BNMediumLevelILFunction* func, BNArchitecture* arch, uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI size_t BNMediumLevelILAddExpr(
    BNMediumLevelILFunction* func, BNMediumLevelILOperation operation,
    size_t size, uint64_t a, uint64_t b, uint64_t c, uint64_t d, uint64_t e) {
  return {};
}
BINARYNINJACOREAPI size_t BNMediumLevelILAddExprWithLocation(
    BNMediumLevelILFunction* func, BNMediumLevelILOperation operation,
    uint64_t addr, uint32_t sourceOperand, size_t size, uint64_t a, uint64_t b,
    uint64_t c, uint64_t d, uint64_t e) {
  return {};
}
BINARYNINJACOREAPI size_t
BNMediumLevelILAddInstruction(BNMediumLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t BNMediumLevelILGoto(BNMediumLevelILFunction* func,
                                              BNMediumLevelILLabel* label) {
  return {};
}
BINARYNINJACOREAPI size_t BNMediumLevelILGotoWithLocation(
    BNMediumLevelILFunction* func, BNMediumLevelILLabel* label, uint64_t addr,
    uint32_t sourceOperand) {
  return {};
}
BINARYNINJACOREAPI size_t BNMediumLevelILIf(BNMediumLevelILFunction* func,
                                            uint64_t op,
                                            BNMediumLevelILLabel* t,
                                            BNMediumLevelILLabel* f) {
  return {};
}
BINARYNINJACOREAPI size_t BNMediumLevelILIfWithLocation(
    BNMediumLevelILFunction* func, uint64_t op, BNMediumLevelILLabel* t,
    BNMediumLevelILLabel* f, uint64_t addr, uint32_t sourceOperand) {
  return {};
}
BINARYNINJACOREAPI void BNMediumLevelILInitLabel(BNMediumLevelILLabel* label) {}
BINARYNINJACOREAPI void BNMediumLevelILMarkLabel(BNMediumLevelILFunction* func,
                                                 BNMediumLevelILLabel* label) {}
BINARYNINJACOREAPI void BNFinalizeMediumLevelILFunction(
    BNMediumLevelILFunction* func) {}
BINARYNINJACOREAPI void BNGenerateMediumLevelILSSAForm(
    BNMediumLevelILFunction* func, bool analyzeConditionals, bool handleAliases,
    BNVariable* knownNotAliases, size_t knownNotAliasCount,
    BNVariable* knownAliases, size_t knownAliasCount) {}
BINARYNINJACOREAPI void BNPrepareToCopyMediumLevelILFunction(
    BNMediumLevelILFunction* func, BNMediumLevelILFunction* src) {}
BINARYNINJACOREAPI void BNPrepareToCopyMediumLevelILBasicBlock(
    BNMediumLevelILFunction* func, BNBasicBlock* block) {}
BINARYNINJACOREAPI BNMediumLevelILLabel*
BNGetLabelForMediumLevelILSourceInstruction(BNMediumLevelILFunction* func,
                                            size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNMediumLevelILAddLabelMap(BNMediumLevelILFunction* func, uint64_t* values,
                           BNMediumLevelILLabel** labels, size_t count) {
  return {};
}
BINARYNINJACOREAPI size_t BNMediumLevelILAddOperandList(
    BNMediumLevelILFunction* func, uint64_t* operands, size_t count) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNMediumLevelILGetOperandList(
    BNMediumLevelILFunction* func, size_t expr, size_t operand, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNMediumLevelILFreeOperandList(uint64_t* operands) {}
BINARYNINJACOREAPI BNMediumLevelILInstruction
BNGetMediumLevelILByIndex(BNMediumLevelILFunction* func, size_t i) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetMediumLevelILIndexForInstruction(BNMediumLevelILFunction* func, size_t i) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetMediumLevelILInstructionForExpr(
    BNMediumLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetMediumLevelILInstructionCount(BNMediumLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetMediumLevelILExprCount(BNMediumLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNUpdateMediumLevelILOperand(
    BNMediumLevelILFunction* func, size_t instr, size_t operandIndex,
    uint64_t value) {}
BINARYNINJACOREAPI void BNMarkMediumLevelILInstructionForRemoval(
    BNMediumLevelILFunction* func, size_t instr) {}
BINARYNINJACOREAPI void BNReplaceMediumLevelILInstruction(
    BNMediumLevelILFunction* func, size_t instr, size_t expr) {}
BINARYNINJACOREAPI void BNReplaceMediumLevelILExpr(
    BNMediumLevelILFunction* func, size_t expr, size_t newExpr) {}
BINARYNINJACOREAPI bool BNGetMediumLevelILExprText(
    BNMediumLevelILFunction* func, BNArchitecture* arch, size_t i,
    BNInstructionTextToken** tokens, size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNGetMediumLevelILInstructionText(
    BNMediumLevelILFunction* il, BNFunction* func, BNArchitecture* arch,
    size_t i, BNInstructionTextToken** tokens, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock** BNGetMediumLevelILBasicBlockList(
    BNMediumLevelILFunction* func, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock* BNGetMediumLevelILBasicBlockForInstruction(
    BNMediumLevelILFunction* func, size_t i) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction* BNGetMediumLevelILSSAForm(
    BNMediumLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction* BNGetMediumLevelILNonSSAForm(
    BNMediumLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetMediumLevelILSSAInstructionIndex(
    BNMediumLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetMediumLevelILNonSSAInstructionIndex(
    BNMediumLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetMediumLevelILSSAExprIndex(BNMediumLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetMediumLevelILNonSSAExprIndex(BNMediumLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetMediumLevelILSSAVarDefinition(
    BNMediumLevelILFunction* func, const BNVariable* var, size_t version) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetMediumLevelILSSAMemoryDefinition(
    BNMediumLevelILFunction* func, size_t version) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetMediumLevelILSSAVarUses(
    BNMediumLevelILFunction* func, const BNVariable* var, size_t version,
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetMediumLevelILSSAMemoryUses(
    BNMediumLevelILFunction* func, size_t version, size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNIsMediumLevelILSSAVarLive(
    BNMediumLevelILFunction* func, const BNVariable* var, size_t version) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetMediumLevelILVariableDefinitions(
    BNMediumLevelILFunction* func, const BNVariable* var, size_t* count) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetMediumLevelILVariableUses(
    BNMediumLevelILFunction* func, const BNVariable* var, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetMediumLevelILSSAVarValue(
    BNMediumLevelILFunction* func, const BNVariable* var, size_t version) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue
BNGetMediumLevelILExprValue(BNMediumLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet BNGetMediumLevelILPossibleSSAVarValues(
    BNMediumLevelILFunction* func, const BNVariable* var, size_t version,
    size_t instr, BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet BNGetMediumLevelILPossibleExprValues(
    BNMediumLevelILFunction* func, size_t expr, BNDataFlowQueryOption* options,
    size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetMediumLevelILSSAVarVersionAtILInstruction(
    BNMediumLevelILFunction* func, const BNVariable* var, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetMediumLevelILSSAMemoryVersionAtILInstruction(
    BNMediumLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNVariable
BNGetMediumLevelILVariableForRegisterAtInstruction(
    BNMediumLevelILFunction* func, uint32_t reg, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNVariable BNGetMediumLevelILVariableForFlagAtInstruction(
    BNMediumLevelILFunction* func, uint32_t flag, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNVariable
BNGetMediumLevelILVariableForStackLocationAtInstruction(
    BNMediumLevelILFunction* func, int64_t offset, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetMediumLevelILRegisterValueAtInstruction(
    BNMediumLevelILFunction* func, uint32_t reg, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue
BNGetMediumLevelILRegisterValueAfterInstruction(BNMediumLevelILFunction* func,
                                                uint32_t reg, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetMediumLevelILPossibleRegisterValuesAtInstruction(
    BNMediumLevelILFunction* func, uint32_t reg, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetMediumLevelILPossibleRegisterValuesAfterInstruction(
    BNMediumLevelILFunction* func, uint32_t reg, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetMediumLevelILFlagValueAtInstruction(
    BNMediumLevelILFunction* func, uint32_t flag, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetMediumLevelILFlagValueAfterInstruction(
    BNMediumLevelILFunction* func, uint32_t flag, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetMediumLevelILPossibleFlagValuesAtInstruction(
    BNMediumLevelILFunction* func, uint32_t flag, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetMediumLevelILPossibleFlagValuesAfterInstruction(
    BNMediumLevelILFunction* func, uint32_t flag, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetMediumLevelILStackContentsAtInstruction(
    BNMediumLevelILFunction* func, int64_t offset, size_t len, size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue
BNGetMediumLevelILStackContentsAfterInstruction(BNMediumLevelILFunction* func,
                                                int64_t offset, size_t len,
                                                size_t instr) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetMediumLevelILPossibleStackContentsAtInstruction(
    BNMediumLevelILFunction* func, int64_t offset, size_t len, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNPossibleValueSet
BNGetMediumLevelILPossibleStackContentsAfterInstruction(
    BNMediumLevelILFunction* func, int64_t offset, size_t len, size_t instr,
    BNDataFlowQueryOption* options, size_t optionCount) {
  return {};
}
BINARYNINJACOREAPI BNILBranchDependence BNGetMediumLevelILBranchDependence(
    BNMediumLevelILFunction* func, size_t curInstr, size_t branchInstr) {
  return {};
}
BINARYNINJACOREAPI BNILBranchInstructionAndDependence*
BNGetAllMediumLevelILBranchDependence(BNMediumLevelILFunction* func,
                                      size_t instr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeILBranchDependenceList(
    BNILBranchInstructionAndDependence* branches) {}
BINARYNINJACOREAPI BNLowLevelILFunction* BNGetLowLevelILForMediumLevelIL(
    BNMediumLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetLowLevelILInstructionIndex(BNMediumLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetLowLevelILExprIndex(BNMediumLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetLowLevelILExprIndexes(
    BNMediumLevelILFunction* func, size_t expr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNHighLevelILFunction* BNGetHighLevelILForMediumLevelIL(
    BNMediumLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILInstructionIndex(BNMediumLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILExprIndex(BNMediumLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI BNTypeWithConfidence
BNGetMediumLevelILExprType(BNMediumLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI BNHighLevelILFunction* BNCreateHighLevelILFunction(
    BNArchitecture* arch, BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNHighLevelILFunction* BNNewHighLevelILFunctionReference(
    BNHighLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNFreeHighLevelILFunction(BNHighLevelILFunction* func) {
}
BINARYNINJACOREAPI BNFunction* BNGetHighLevelILOwnerFunction(
    BNHighLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI uint64_t
BNHighLevelILGetCurrentAddress(BNHighLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNHighLevelILSetCurrentAddress(
    BNHighLevelILFunction* func, BNArchitecture* arch, uint64_t addr) {}
BINARYNINJACOREAPI size_t BNHighLevelILAddExpr(BNHighLevelILFunction* func,
                                               BNHighLevelILOperation operation,
                                               size_t size, uint64_t a,
                                               uint64_t b, uint64_t c,
                                               uint64_t d, uint64_t e) {
  return {};
}
BINARYNINJACOREAPI size_t BNHighLevelILAddExprWithLocation(
    BNHighLevelILFunction* func, BNHighLevelILOperation operation,
    uint64_t addr, uint32_t sourceOperand, size_t size, uint64_t a, uint64_t b,
    uint64_t c, uint64_t d, uint64_t e) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILRootExpr(BNHighLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI void BNSetHighLevelILRootExpr(BNHighLevelILFunction* func,
                                                 size_t expr) {}
BINARYNINJACOREAPI void BNFinalizeHighLevelILFunction(
    BNHighLevelILFunction* func) {}
BINARYNINJACOREAPI size_t BNHighLevelILAddOperandList(
    BNHighLevelILFunction* func, uint64_t* operands, size_t count) {
  return {};
}
BINARYNINJACOREAPI uint64_t* BNHighLevelILGetOperandList(
    BNHighLevelILFunction* func, size_t expr, size_t operand, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNHighLevelILFreeOperandList(uint64_t* operands) {}
BINARYNINJACOREAPI BNHighLevelILInstruction
BNGetHighLevelILByIndex(BNHighLevelILFunction* func, size_t i, bool asFullAst) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILIndexForInstruction(BNHighLevelILFunction* func, size_t i) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILInstructionForExpr(BNHighLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILInstructionCount(BNHighLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILExprCount(BNHighLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNMediumLevelILFunction*
BNGetMediumLevelILForHighLevelILFunction(BNHighLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetMediumLevelILExprIndexFromHighLevelIL(
    BNHighLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetMediumLevelILExprIndexesFromHighLevelIL(
    BNHighLevelILFunction* func, size_t expr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNUpdateHighLevelILOperand(BNHighLevelILFunction* func,
                                                   size_t instr,
                                                   size_t operandIndex,
                                                   uint64_t value) {}
BINARYNINJACOREAPI void BNReplaceHighLevelILExpr(BNHighLevelILFunction* func,
                                                 size_t expr, size_t newExpr) {}
BINARYNINJACOREAPI BNDisassemblyTextLine* BNGetHighLevelILExprText(
    BNHighLevelILFunction* func, size_t expr, bool asFullAst, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTypeWithConfidence
BNGetHighLevelILExprType(BNHighLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock** BNGetHighLevelILBasicBlockList(
    BNHighLevelILFunction* func, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNBasicBlock* BNGetHighLevelILBasicBlockForInstruction(
    BNHighLevelILFunction* func, size_t i) {
  return {};
}
BINARYNINJACOREAPI BNHighLevelILFunction* BNGetHighLevelILSSAForm(
    BNHighLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNHighLevelILFunction* BNGetHighLevelILNonSSAForm(
    BNHighLevelILFunction* func) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILSSAInstructionIndex(BNHighLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetHighLevelILNonSSAInstructionIndex(
    BNHighLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILSSAExprIndex(BNHighLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILNonSSAExprIndex(BNHighLevelILFunction* func, size_t expr) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetHighLevelILSSAVarDefinition(
    BNHighLevelILFunction* func, const BNVariable* var, size_t version) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetHighLevelILSSAMemoryDefinition(
    BNHighLevelILFunction* func, size_t version) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetHighLevelILSSAVarUses(
    BNHighLevelILFunction* func, const BNVariable* var, size_t version,
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetHighLevelILSSAMemoryUses(
    BNHighLevelILFunction* func, size_t version, size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNIsHighLevelILSSAVarLive(BNHighLevelILFunction* func,
                                                  const BNVariable* var,
                                                  size_t version) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetHighLevelILVariableDefinitions(
    BNHighLevelILFunction* func, const BNVariable* var, size_t* count) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetHighLevelILVariableUses(
    BNHighLevelILFunction* func, const BNVariable* var, size_t* count) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetHighLevelILSSAVarVersionAtILInstruction(
    BNHighLevelILFunction* func, const BNVariable* var, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetHighLevelILSSAMemoryVersionAtILInstruction(
    BNHighLevelILFunction* func, size_t instr) {
  return {};
}
BINARYNINJACOREAPI size_t
BNGetHighLevelILExprIndexForLabel(BNHighLevelILFunction* func, uint64_t label) {
  return {};
}
BINARYNINJACOREAPI size_t* BNGetHighLevelILUsesForLabel(
    BNHighLevelILFunction* func, uint64_t label, size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNHighLevelILExprLessThan(
    BNHighLevelILFunction* leftFunc, size_t leftExpr,
    BNHighLevelILFunction* rightFunc, size_t rightExpr) {
  return {};
}
BINARYNINJACOREAPI bool BNHighLevelILExprEqual(BNHighLevelILFunction* leftFunc,
                                               size_t leftExpr,
                                               BNHighLevelILFunction* rightFunc,
                                               size_t rightExpr) {
  return {};
}
BINARYNINJACOREAPI BNTypeLibrary* BNNewTypeLibrary(BNArchitecture* arch,
                                                   const char* name) {
  return {};
}
BINARYNINJACOREAPI BNTypeLibrary* BNNewTypeLibraryReference(
    BNTypeLibrary* lib) {
  return {};
}
BINARYNINJACOREAPI BNTypeLibrary* BNDuplicateTypeLibrary(BNTypeLibrary* lib) {
  return {};
}
BINARYNINJACOREAPI BNTypeLibrary* BNLoadTypeLibraryFromFile(const char* path) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTypeLibrary(BNTypeLibrary* lib) {}
BINARYNINJACOREAPI BNTypeLibrary* BNLookupTypeLibraryByName(
    BNArchitecture* arch, const char* name) {
  return {};
}
BINARYNINJACOREAPI BNTypeLibrary* BNLookupTypeLibraryByGuid(
    BNArchitecture* arch, const char* guid) {
  return {};
}
BINARYNINJACOREAPI BNTypeLibrary** BNGetArchitectureTypeLibraries(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTypeLibraryList(BNTypeLibrary** lib,
                                              size_t count) {}
BINARYNINJACOREAPI void BNFinalizeTypeLibrary(BNTypeLibrary* lib) {}
BINARYNINJACOREAPI BNArchitecture* BNGetTypeLibraryArchitecture(
    BNTypeLibrary* lib) {
  return {};
}
BINARYNINJACOREAPI void BNSetTypeLibraryName(BNTypeLibrary* lib,
                                             const char* name) {}
BINARYNINJACOREAPI char* BNGetTypeLibraryName(BNTypeLibrary* lib) { return {}; }
BINARYNINJACOREAPI void BNAddTypeLibraryAlternateName(BNTypeLibrary* lib,
                                                      const char* name) {}
BINARYNINJACOREAPI char** BNGetTypeLibraryAlternateNames(BNTypeLibrary* lib,
                                                         size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNSetTypeLibraryDependencyName(BNTypeLibrary* lib,
                                                       const char* name) {}
BINARYNINJACOREAPI char* BNGetTypeLibraryDependencyName(BNTypeLibrary* lib) {
  return {};
}
BINARYNINJACOREAPI void BNSetTypeLibraryGuid(BNTypeLibrary* lib,
                                             const char* name) {}
BINARYNINJACOREAPI char* BNGetTypeLibraryGuid(BNTypeLibrary* lib) { return {}; }
BINARYNINJACOREAPI void BNClearTypeLibraryPlatforms(BNTypeLibrary* lib) {}
BINARYNINJACOREAPI void BNAddTypeLibraryPlatform(BNTypeLibrary* lib,
                                                 BNPlatform* platform) {}
BINARYNINJACOREAPI char** BNGetTypeLibraryPlatforms(BNTypeLibrary* lib,
                                                    size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNTypeLibraryStoreMetadata(BNTypeLibrary* lib,
                                                   const char* key,
                                                   BNMetadata* value) {}
BINARYNINJACOREAPI BNMetadata* BNTypeLibraryQueryMetadata(BNTypeLibrary* lib,
                                                          const char* key) {
  return {};
}
BINARYNINJACOREAPI void BNTypeLibraryRemoveMetadata(BNTypeLibrary* lib,
                                                    const char* key) {}
BINARYNINJACOREAPI void BNAddTypeLibraryNamedObject(BNTypeLibrary* lib,
                                                    BNQualifiedName* name,
                                                    BNType* type) {}
BINARYNINJACOREAPI void BNAddTypeLibraryNamedType(BNTypeLibrary* lib,
                                                  BNQualifiedName* name,
                                                  BNType* type) {}
BINARYNINJACOREAPI BNType* BNGetTypeLibraryNamedObject(BNTypeLibrary* lib,
                                                       BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNType* BNGetTypeLibraryNamedType(BNTypeLibrary* lib,
                                                     BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedNameAndType* BNGetTypeLibraryNamedObjects(
    BNTypeLibrary* lib, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedNameAndType* BNGetTypeLibraryNamedTypes(
    BNTypeLibrary* lib, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNWriteTypeLibraryToFile(BNTypeLibrary* lib,
                                                 const char* path) {}
BINARYNINJACOREAPI void BNAddBinaryViewTypeLibrary(BNBinaryView* view,
                                                   BNTypeLibrary* lib) {}
BINARYNINJACOREAPI BNTypeLibrary* BNGetBinaryViewTypeLibrary(BNBinaryView* view,
                                                             const char* name) {
  return {};
}
BINARYNINJACOREAPI BNTypeLibrary** BNGetBinaryViewTypeLibraries(
    BNBinaryView* view, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNType* BNBinaryViewImportTypeLibraryType(
    BNBinaryView* view, BNTypeLibrary* lib, BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNType* BNBinaryViewImportTypeLibraryObject(
    BNBinaryView* view, BNTypeLibrary* lib, BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI void BNBinaryViewExportTypeToTypeLibrary(
    BNBinaryView* view, BNTypeLibrary* lib, BNQualifiedName* name,
    BNType* type) {}
BINARYNINJACOREAPI void BNBinaryViewExportObjectToTypeLibrary(
    BNBinaryView* view, BNTypeLibrary* lib, BNQualifiedName* name,
    BNType* type) {}
BINARYNINJACOREAPI bool BNTypesEqual(BNType* a, BNType* b) { return {}; }
BINARYNINJACOREAPI bool BNTypesNotEqual(BNType* a, BNType* b) { return {}; }
BINARYNINJACOREAPI BNType* BNCreateVoidType(void) { return {}; }
BINARYNINJACOREAPI BNType* BNCreateBoolType(void) { return {}; }
BINARYNINJACOREAPI BNType* BNCreateIntegerType(size_t width,
                                               BNBoolWithConfidence* sign,
                                               const char* altName) {
  return {};
}
BINARYNINJACOREAPI BNType* BNCreateFloatType(size_t width,
                                             const char* altName) {
  return {};
}
BINARYNINJACOREAPI BNType* BNCreateStructureType(BNStructure* s) { return {}; }
BINARYNINJACOREAPI BNType* BNCreateEnumerationType(BNArchitecture* arch,
                                                   BNEnumeration* e,
                                                   size_t width,
                                                   bool isSigned) {
  return {};
}
BINARYNINJACOREAPI BNType* BNCreatePointerType(BNArchitecture* arch,
                                               BNTypeWithConfidence* type,
                                               BNBoolWithConfidence* cnst,
                                               BNBoolWithConfidence* vltl,
                                               BNReferenceType refType) {
  return {};
}
BINARYNINJACOREAPI BNType* BNCreatePointerTypeOfWidth(
    size_t width, BNTypeWithConfidence* type, BNBoolWithConfidence* cnst,
    BNBoolWithConfidence* vltl, BNReferenceType refType) {
  return {};
}
BINARYNINJACOREAPI BNType* BNCreateArrayType(BNTypeWithConfidence* type,
                                             uint64_t elem) {
  return {};
}
BINARYNINJACOREAPI BNType* BNCreateFunctionType(
    BNTypeWithConfidence* returnValue,
    BNCallingConventionWithConfidence* callingConvention,
    BNFunctionParameter* params, size_t paramCount,
    BNBoolWithConfidence* varArg, BNOffsetWithConfidence* stackAdjust) {
  return {};
}
BINARYNINJACOREAPI BNType* BNNewTypeReference(BNType* type) { return {}; }
BINARYNINJACOREAPI BNType* BNDuplicateType(BNType* type) { return {}; }
BINARYNINJACOREAPI char* BNGetTypeAndName(BNType* type, BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI void BNFreeType(BNType* type) {}
BINARYNINJACOREAPI BNTypeBuilder* BNCreateTypeBuilderFromType(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNCreateVoidTypeBuilder(void) { return {}; }
BINARYNINJACOREAPI BNTypeBuilder* BNCreateBoolTypeBuilder(void) { return {}; }
BINARYNINJACOREAPI BNTypeBuilder* BNCreateIntegerTypeBuilder(
    size_t width, BNBoolWithConfidence* sign, const char* altName) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNCreateFloatTypeBuilder(
    size_t width, const char* altName) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNCreateStructureTypeBuilder(BNStructure* s) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNCreateEnumerationTypeBuilder(
    BNArchitecture* arch, BNEnumeration* e, size_t width, bool isSigned) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNCreatePointerTypeBuilder(
    BNArchitecture* arch, BNTypeWithConfidence* type,
    BNBoolWithConfidence* cnst, BNBoolWithConfidence* vltl,
    BNReferenceType refType) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNCreatePointerTypeBuilderOfWidth(
    size_t width, BNTypeWithConfidence* type, BNBoolWithConfidence* cnst,
    BNBoolWithConfidence* vltl, BNReferenceType refType) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNCreateArrayTypeBuilder(
    BNTypeWithConfidence* type, uint64_t elem) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNCreateFunctionTypeBuilder(
    BNTypeWithConfidence* returnValue,
    BNCallingConventionWithConfidence* callingConvention,
    BNFunctionParameter* params, size_t paramCount,
    BNBoolWithConfidence* varArg, BNOffsetWithConfidence* stackAdjust) {
  return {};
}
BINARYNINJACOREAPI BNType* BNFinalizeTypeBuilder(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNDuplicateTypeBuilder(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI char* BNGetTypeBuilderTypeAndName(BNTypeBuilder* type,
                                                     BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTypeBuilder(BNTypeBuilder* type) {}
BINARYNINJACOREAPI BNQualifiedName BNTypeGetTypeName(BNType* nt) { return {}; }
BINARYNINJACOREAPI BNTypeClass BNGetTypeClass(BNType* type) { return {}; }
BINARYNINJACOREAPI uint64_t BNGetTypeWidth(BNType* type) { return {}; }
BINARYNINJACOREAPI size_t BNGetTypeAlignment(BNType* type) { return {}; }
BINARYNINJACOREAPI BNIntegerDisplayType
BNGetIntegerTypeDisplayType(BNType* type) {
  return {};
}
BINARYNINJACOREAPI void BNSetIntegerTypeDisplayType(
    BNTypeBuilder* type, BNIntegerDisplayType displayType) {}
BINARYNINJACOREAPI BNBoolWithConfidence BNIsTypeSigned(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNBoolWithConfidence BNIsTypeConst(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNBoolWithConfidence BNIsTypeVolatile(BNType* type) {
  return {};
}
BINARYNINJACOREAPI bool BNIsTypeFloatingPoint(BNType* type) { return {}; }
BINARYNINJACOREAPI BNTypeWithConfidence BNGetChildType(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNCallingConventionWithConfidence
BNGetTypeCallingConvention(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNFunctionParameter* BNGetTypeParameters(BNType* type,
                                                            size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTypeParameterList(BNFunctionParameter* types,
                                                size_t count) {}
BINARYNINJACOREAPI BNBoolWithConfidence
BNTypeHasVariableArguments(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNBoolWithConfidence BNFunctionTypeCanReturn(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNStructure* BNGetTypeStructure(BNType* type) { return {}; }
BINARYNINJACOREAPI BNEnumeration* BNGetTypeEnumeration(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNNamedTypeReference* BNGetTypeNamedTypeReference(
    BNType* type) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetTypeElementCount(BNType* type) { return {}; }
BINARYNINJACOREAPI uint64_t BNGetTypeOffset(BNType* type) { return {}; }
BINARYNINJACOREAPI BNMemberScopeWithConfidence
BNTypeGetMemberScope(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNMemberAccessWithConfidence
BNTypeGetMemberAccess(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNOffsetWithConfidence
BNGetTypeStackAdjustment(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedName BNTypeGetStructureName(BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNNamedTypeReference* BNGetRegisteredTypeName(BNType* type) {
  return {};
}
BINARYNINJACOREAPI char* BNGetTypeString(BNType* type, BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI char* BNGetTypeStringBeforeName(BNType* type,
                                                   BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI char* BNGetTypeStringAfterName(BNType* type,
                                                  BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI BNInstructionTextToken* BNGetTypeTokens(
    BNType* type, BNPlatform* platform, uint8_t baseConfidence, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNInstructionTextToken* BNGetTypeTokensBeforeName(
    BNType* type, BNPlatform* platform, uint8_t baseConfidence, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNInstructionTextToken* BNGetTypeTokensAfterName(
    BNType* type, BNPlatform* platform, uint8_t baseConfidence, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNType* BNTypeWithReplacedStructure(BNType* type,
                                                       BNStructure* from,
                                                       BNStructure* to) {
  return {};
}
BINARYNINJACOREAPI BNType* BNTypeWithReplacedEnumeration(BNType* type,
                                                         BNEnumeration* from,
                                                         BNEnumeration* to) {
  return {};
}
BINARYNINJACOREAPI BNType* BNTypeWithReplacedNamedTypeReference(
    BNType* type, BNNamedTypeReference* from, BNNamedTypeReference* to) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedName BNTypeBuilderGetTypeName(BNTypeBuilder* nt) {
  return {};
}
BINARYNINJACOREAPI void BNTypeBuilderSetTypeName(BNTypeBuilder* type,
                                                 BNQualifiedName* name) {}
BINARYNINJACOREAPI BNTypeClass BNGetTypeBuilderClass(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetTypeBuilderWidth(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI size_t BNGetTypeBuilderAlignment(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNBoolWithConfidence
BNIsTypeBuilderSigned(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNBoolWithConfidence
BNIsTypeBuilderConst(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNBoolWithConfidence
BNIsTypeBuilderVolatile(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI bool BNIsTypeBuilderFloatingPoint(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNTypeWithConfidence
BNGetTypeBuilderChildType(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNCallingConventionWithConfidence
BNGetTypeBuilderCallingConvention(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNFunctionParameter* BNGetTypeBuilderParameters(
    BNTypeBuilder* type, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNBoolWithConfidence
BNTypeBuilderHasVariableArguments(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNBoolWithConfidence
BNFunctionTypeBuilderCanReturn(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNStructure* BNGetTypeBuilderStructure(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNEnumeration* BNGetTypeBuilderEnumeration(
    BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNNamedTypeReference* BNGetTypeBuilderNamedTypeReference(
    BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetTypeBuilderElementCount(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetTypeBuilderOffset(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI void BNSetFunctionTypeBuilderCanReturn(
    BNTypeBuilder* type, BNBoolWithConfidence* canReturn) {}
BINARYNINJACOREAPI BNMemberScopeWithConfidence
BNTypeBuilderGetMemberScope(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI void BNTypeBuilderSetMemberScope(
    BNTypeBuilder* type, BNMemberScopeWithConfidence* scope) {}
BINARYNINJACOREAPI BNMemberAccessWithConfidence
BNTypeBuilderGetMemberAccess(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI void BNTypeBuilderSetMemberAccess(
    BNTypeBuilder* type, BNMemberAccessWithConfidence* access) {}
BINARYNINJACOREAPI void BNTypeBuilderSetConst(BNTypeBuilder* type,
                                              BNBoolWithConfidence* cnst) {}
BINARYNINJACOREAPI void BNTypeBuilderSetVolatile(BNTypeBuilder* type,
                                                 BNBoolWithConfidence* vltl) {}
BINARYNINJACOREAPI BNOffsetWithConfidence
BNGetTypeBuilderStackAdjustment(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedName
BNTypeBuilderGetStructureName(BNTypeBuilder* type) {
  return {};
}
BINARYNINJACOREAPI char* BNGetTypeBuilderString(BNTypeBuilder* type,
                                                BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI char* BNGetTypeBuilderStringBeforeName(
    BNTypeBuilder* type, BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI char* BNGetTypeBuilderStringAfterName(BNTypeBuilder* type,
                                                         BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI BNInstructionTextToken* BNGetTypeBuilderTokens(
    BNTypeBuilder* type, BNPlatform* platform, uint8_t baseConfidence,
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNInstructionTextToken* BNGetTypeBuilderTokensBeforeName(
    BNTypeBuilder* type, BNPlatform* platform, uint8_t baseConfidence,
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNInstructionTextToken* BNGetTypeBuilderTokensAfterName(
    BNTypeBuilder* type, BNPlatform* platform, uint8_t baseConfidence,
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNType* BNCreateNamedTypeReference(BNNamedTypeReference* nt,
                                                      size_t width,
                                                      size_t align) {
  return {};
}
BINARYNINJACOREAPI BNType* BNCreateNamedTypeReferenceFromTypeAndId(
    const char* id, BNQualifiedName* name, BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNType* BNCreateNamedTypeReferenceFromType(
    BNBinaryView* view, BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNCreateNamedTypeReferenceBuilder(
    BNNamedTypeReference* nt, size_t width, size_t align) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder*
BNCreateNamedTypeReferenceBuilderFromTypeAndId(const char* id,
                                               BNQualifiedName* name,
                                               BNType* type) {
  return {};
}
BINARYNINJACOREAPI BNTypeBuilder* BNCreateNamedTypeReferenceBuilderFromType(
    BNBinaryView* view, BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNNamedTypeReference* BNCreateNamedType(
    BNNamedTypeReferenceClass cls, const char* id, BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNNamedTypeReferenceClass
BNGetTypeReferenceClass(BNNamedTypeReference* nt) {
  return {};
}
BINARYNINJACOREAPI char* BNGetTypeReferenceId(BNNamedTypeReference* nt) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedName
BNGetTypeReferenceName(BNNamedTypeReference* nt) {
  return {};
}
BINARYNINJACOREAPI void BNFreeQualifiedName(BNQualifiedName* name) {}
BINARYNINJACOREAPI void BNFreeNamedTypeReference(BNNamedTypeReference* nt) {}
BINARYNINJACOREAPI BNNamedTypeReference* BNNewNamedTypeReference(
    BNNamedTypeReference* nt) {
  return {};
}
BINARYNINJACOREAPI BNStructureBuilder* BNCreateStructureBuilder(void) {
  return {};
}
BINARYNINJACOREAPI BNStructureBuilder* BNCreateStructureBuilderWithOptions(
    BNStructureType type, bool packed) {
  return {};
}
BINARYNINJACOREAPI BNStructureBuilder* BNCreateStructureBuilderFromStructure(
    BNStructure* s) {
  return {};
}
BINARYNINJACOREAPI BNStructureBuilder* BNDuplicateStructureBuilder(
    BNStructureBuilder* s) {
  return {};
}
BINARYNINJACOREAPI BNStructure* BNFinalizeStructureBuilder(
    BNStructureBuilder* s) {
  return {};
}
BINARYNINJACOREAPI BNStructure* BNNewStructureReference(BNStructure* s) {
  return {};
}
BINARYNINJACOREAPI void BNFreeStructure(BNStructure* s) {}
BINARYNINJACOREAPI void BNFreeStructureBuilder(BNStructureBuilder* s) {}
BINARYNINJACOREAPI BNStructureMember* BNGetStructureMemberByName(
    BNStructure* s, const char* name) {
  return {};
}
BINARYNINJACOREAPI BNStructureMember* BNGetStructureMemberAtOffset(
    BNStructure* s, int64_t offset, size_t* idx) {
  return {};
}
BINARYNINJACOREAPI void BNFreeStructureMember(BNStructureMember* s) {}
BINARYNINJACOREAPI BNStructureMember* BNGetStructureMembers(BNStructure* s,
                                                            size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeStructureMemberList(BNStructureMember* members,
                                                  size_t count) {}
BINARYNINJACOREAPI uint64_t BNGetStructureWidth(BNStructure* s) { return {}; }
BINARYNINJACOREAPI size_t BNGetStructureAlignment(BNStructure* s) { return {}; }
BINARYNINJACOREAPI bool BNIsStructurePacked(BNStructure* s) { return {}; }
BINARYNINJACOREAPI bool BNIsStructureUnion(BNStructure* s) { return {}; }
BINARYNINJACOREAPI BNStructureType BNGetStructureType(BNStructure* s) {
  return {};
}
BINARYNINJACOREAPI BNStructure* BNStructureWithReplacedStructure(
    BNStructure* s, BNStructure* from, BNStructure* to) {
  return {};
}
BINARYNINJACOREAPI BNStructure* BNStructureWithReplacedEnumeration(
    BNStructure* s, BNEnumeration* from, BNEnumeration* to) {
  return {};
}
BINARYNINJACOREAPI BNStructure* BNStructureWithReplacedNamedTypeReference(
    BNStructure* s, BNNamedTypeReference* from, BNNamedTypeReference* to) {
  return {};
}
BINARYNINJACOREAPI BNStructureMember* BNGetStructureBuilderMemberByName(
    BNStructureBuilder* s, const char* name) {
  return {};
}
BINARYNINJACOREAPI BNStructureMember* BNGetStructureBuilderMemberAtOffset(
    BNStructureBuilder* s, int64_t offset, size_t* idx) {
  return {};
}
BINARYNINJACOREAPI BNStructureMember* BNGetStructureBuilderMembers(
    BNStructureBuilder* s, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNGetStructureBuilderWidth(BNStructureBuilder* s) {
  return {};
}
BINARYNINJACOREAPI void BNSetStructureBuilderWidth(BNStructureBuilder* s,
                                                   uint64_t width) {}
BINARYNINJACOREAPI size_t
BNGetStructureBuilderAlignment(BNStructureBuilder* s) {
  return {};
}
BINARYNINJACOREAPI void BNSetStructureBuilderAlignment(BNStructureBuilder* s,
                                                       size_t align) {}
BINARYNINJACOREAPI bool BNIsStructureBuilderPacked(BNStructureBuilder* s) {
  return {};
}
BINARYNINJACOREAPI void BNSetStructureBuilderPacked(BNStructureBuilder* s,
                                                    bool packed) {}
BINARYNINJACOREAPI bool BNIsStructureBuilderUnion(BNStructureBuilder* s) {
  return {};
}
BINARYNINJACOREAPI void BNSetStructureBuilderType(BNStructureBuilder* s,
                                                  BNStructureType type) {}
BINARYNINJACOREAPI BNStructureType
BNGetStructureBuilderType(BNStructureBuilder* s) {
  return {};
}
BINARYNINJACOREAPI void BNAddStructureBuilderMember(BNStructureBuilder* s,
                                                    BNTypeWithConfidence* type,
                                                    const char* name) {}
BINARYNINJACOREAPI void BNAddStructureBuilderMemberAtOffset(
    BNStructureBuilder* s, BNTypeWithConfidence* type, const char* name,
    uint64_t offset) {}
BINARYNINJACOREAPI void BNRemoveStructureBuilderMember(BNStructureBuilder* s,
                                                       size_t idx) {}
BINARYNINJACOREAPI void BNReplaceStructureBuilderMember(
    BNStructureBuilder* s, size_t idx, BNTypeWithConfidence* type,
    const char* name) {}
BINARYNINJACOREAPI BNEnumerationBuilder* BNCreateEnumerationBuilder(void) {
  return {};
}
BINARYNINJACOREAPI BNEnumerationBuilder*
BNCreateEnumerationBuilderFromEnumeration(BNEnumeration* e) {
  return {};
}
BINARYNINJACOREAPI BNEnumerationBuilder* BNDuplicateEnumerationBuilder(
    BNEnumerationBuilder* e) {
  return {};
}
BINARYNINJACOREAPI BNEnumeration* BNFinalizeEnumerationBuilder(
    BNEnumerationBuilder* e) {
  return {};
}
BINARYNINJACOREAPI BNEnumeration* BNNewEnumerationReference(BNEnumeration* e) {
  return {};
}
BINARYNINJACOREAPI void BNFreeEnumeration(BNEnumeration* e) {}
BINARYNINJACOREAPI void BNFreeEnumerationBuilder(BNEnumerationBuilder* e) {}
BINARYNINJACOREAPI BNEnumerationMember* BNGetEnumerationMembers(
    BNEnumeration* e, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeEnumerationMemberList(
    BNEnumerationMember* members, size_t count) {}
BINARYNINJACOREAPI BNEnumerationMember* BNGetEnumerationBuilderMembers(
    BNEnumerationBuilder* e, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNAddEnumerationBuilderMember(BNEnumerationBuilder* e,
                                                      const char* name) {}
BINARYNINJACOREAPI void BNAddEnumerationBuilderMemberWithValue(
    BNEnumerationBuilder* e, const char* name, uint64_t value) {}
BINARYNINJACOREAPI void BNRemoveEnumerationBuilderMember(
    BNEnumerationBuilder* e, size_t idx) {}
BINARYNINJACOREAPI void BNReplaceEnumerationBuilderMember(
    BNEnumerationBuilder* e, size_t idx, const char* name, uint64_t value) {}
BINARYNINJACOREAPI bool BNPreprocessSource(const char* source,
                                           const char* fileName, char** output,
                                           char** errors,
                                           const char** includeDirs,
                                           size_t includeDirCount) {
  return {};
}
BINARYNINJACOREAPI bool BNParseTypesFromSource(
    BNPlatform* platform, const char* source, const char* fileName,
    BNTypeParserResult* result, char** errors, const char** includeDirs,
    size_t includeDirCount, const char* autoTypeSource) {
  return {};
}
BINARYNINJACOREAPI bool BNParseTypesFromSourceFile(
    BNPlatform* platform, const char* fileName, BNTypeParserResult* result,
    char** errors, const char** includeDirs, size_t includeDirCount,
    const char* autoTypeSource) {
  return {};
}
BINARYNINJACOREAPI void BNFreeTypeParserResult(BNTypeParserResult* result) {}
BINARYNINJACOREAPI BNUpdateChannel* BNGetUpdateChannels(size_t* count,
                                                        char** errors) {
  return {};
}
BINARYNINJACOREAPI void BNFreeUpdateChannelList(BNUpdateChannel* list,
                                                size_t count) {}
BINARYNINJACOREAPI BNUpdateVersion* BNGetUpdateChannelVersions(
    const char* channel, size_t* count, char** errors) {
  return {};
}
BINARYNINJACOREAPI void BNFreeUpdateChannelVersionList(BNUpdateVersion* list,
                                                       size_t count) {}
BINARYNINJACOREAPI bool BNAreUpdatesAvailable(const char* channel,
                                              uint64_t* expireTime,
                                              uint64_t* serverTime,
                                              char** errors) {
  return {};
}
BINARYNINJACOREAPI BNUpdateResult BNUpdateToVersion(
    const char* channel, const char* version, char** errors,
    bool (*progress)(void* ctxt, uint64_t progress, uint64_t total),
    void* context) {
  return {};
}
BINARYNINJACOREAPI BNUpdateResult BNUpdateToLatestVersion(
    const char* channel, char** errors,
    bool (*progress)(void* ctxt, uint64_t progress, uint64_t total),
    void* context) {
  return {};
}
BINARYNINJACOREAPI bool BNAreAutoUpdatesEnabled(void) { return {}; }
BINARYNINJACOREAPI void BNSetAutoUpdatesEnabled(bool enabled) {}
BINARYNINJACOREAPI uint64_t BNGetTimeSinceLastUpdateCheck(void) { return {}; }
BINARYNINJACOREAPI void BNUpdatesChecked(void) {}
BINARYNINJACOREAPI char* BNGetActiveUpdateChannel(void) { return {}; }
BINARYNINJACOREAPI void BNSetActiveUpdateChannel(const char* channel) {}
BINARYNINJACOREAPI bool BNIsUpdateInstallationPending(void) { return {}; }
BINARYNINJACOREAPI void BNInstallPendingUpdate(char** errors) {}
BINARYNINJACOREAPI void BNRegisterPluginCommand(
    const char* name, const char* description,
    void (*action)(void* ctxt, BNBinaryView* view),
    bool (*isValid)(void* ctxt, BNBinaryView* view), void* context) {}
BINARYNINJACOREAPI void BNRegisterPluginCommandForAddress(
    const char* name, const char* description,
    void (*action)(void* ctxt, BNBinaryView* view, uint64_t addr),
    bool (*isValid)(void* ctxt, BNBinaryView* view, uint64_t addr),
    void* context) {}
BINARYNINJACOREAPI void BNRegisterPluginCommandForRange(
    const char* name, const char* description,
    void (*action)(void* ctxt, BNBinaryView* view, uint64_t addr, uint64_t len),
    bool (*isValid)(void* ctxt, BNBinaryView* view, uint64_t addr,
                    uint64_t len),
    void* context) {}
BINARYNINJACOREAPI void BNRegisterPluginCommandForFunction(
    const char* name, const char* description,
    void (*action)(void* ctxt, BNBinaryView* view, BNFunction* func),
    bool (*isValid)(void* ctxt, BNBinaryView* view, BNFunction* func),
    void* context) {}
BINARYNINJACOREAPI void BNRegisterPluginCommandForLowLevelILFunction(
    const char* name, const char* description,
    void (*action)(void* ctxt, BNBinaryView* view, BNLowLevelILFunction* func),
    bool (*isValid)(void* ctxt, BNBinaryView* view, BNLowLevelILFunction* func),
    void* context) {}
BINARYNINJACOREAPI void BNRegisterPluginCommandForLowLevelILInstruction(
    const char* name, const char* description,
    void (*action)(void* ctxt, BNBinaryView* view, BNLowLevelILFunction* func,
                   size_t instr),
    bool (*isValid)(void* ctxt, BNBinaryView* view, BNLowLevelILFunction* func,
                    size_t instr),
    void* context) {}
BINARYNINJACOREAPI void BNRegisterPluginCommandForMediumLevelILFunction(
    const char* name, const char* description,
    void (*action)(void* ctxt, BNBinaryView* view,
                   BNMediumLevelILFunction* func),
    bool (*isValid)(void* ctxt, BNBinaryView* view,
                    BNMediumLevelILFunction* func),
    void* context) {}
BINARYNINJACOREAPI void BNRegisterPluginCommandForMediumLevelILInstruction(
    const char* name, const char* description,
    void (*action)(void* ctxt, BNBinaryView* view,
                   BNMediumLevelILFunction* func, size_t instr),
    bool (*isValid)(void* ctxt, BNBinaryView* view,
                    BNMediumLevelILFunction* func, size_t instr),
    void* context) {}
BINARYNINJACOREAPI BNPluginCommand* BNGetAllPluginCommands(size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNPluginCommand* BNGetValidPluginCommands(BNBinaryView* view,
                                                             size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNPluginCommand* BNGetValidPluginCommandsForAddress(
    BNBinaryView* view, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNPluginCommand* BNGetValidPluginCommandsForRange(
    BNBinaryView* view, uint64_t addr, uint64_t len, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNPluginCommand* BNGetValidPluginCommandsForFunction(
    BNBinaryView* view, BNFunction* func, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNPluginCommand*
BNGetValidPluginCommandsForLowLevelILFunction(BNBinaryView* view,
                                              BNLowLevelILFunction* func,
                                              size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNPluginCommand*
BNGetValidPluginCommandsForLowLevelILInstruction(BNBinaryView* view,
                                                 BNLowLevelILFunction* func,
                                                 size_t instr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNPluginCommand*
BNGetValidPluginCommandsForMediumLevelILFunction(BNBinaryView* view,
                                                 BNMediumLevelILFunction* func,
                                                 size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNPluginCommand*
BNGetValidPluginCommandsForMediumLevelILInstruction(
    BNBinaryView* view, BNMediumLevelILFunction* func, size_t instr,
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreePluginCommandList(BNPluginCommand* commands) {}
BINARYNINJACOREAPI BNCallingConvention* BNCreateCallingConvention(
    BNArchitecture* arch, const char* name, BNCustomCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterCallingConvention(BNArchitecture* arch,
                                                    BNCallingConvention* cc) {}
BINARYNINJACOREAPI BNCallingConvention* BNNewCallingConventionReference(
    BNCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI void BNFreeCallingConvention(BNCallingConvention* cc) {}
BINARYNINJACOREAPI BNCallingConvention** BNGetArchitectureCallingConventions(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeCallingConventionList(BNCallingConvention** list,
                                                    size_t count) {}
BINARYNINJACOREAPI BNCallingConvention*
BNGetArchitectureCallingConventionByName(BNArchitecture* arch,
                                         const char* name) {
  return {};
}
BINARYNINJACOREAPI BNArchitecture* BNGetCallingConventionArchitecture(
    BNCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI char* BNGetCallingConventionName(BNCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetCallerSavedRegisters(BNCallingConvention* cc,
                                                       size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetCalleeSavedRegisters(BNCallingConvention* cc,
                                                       size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetIntegerArgumentRegisters(
    BNCallingConvention* cc, size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetFloatArgumentRegisters(
    BNCallingConvention* cc, size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNAreArgumentRegistersSharedIndex(
    BNCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI bool BNIsStackReservedForArgumentRegisters(
    BNCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI bool BNIsStackAdjustedOnReturn(BNCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI uint32_t
BNGetIntegerReturnValueRegister(BNCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI uint32_t
BNGetHighIntegerReturnValueRegister(BNCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI uint32_t
BNGetFloatReturnValueRegister(BNCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI uint32_t
BNGetGlobalPointerRegister(BNCallingConvention* cc) {
  return {};
}
BINARYNINJACOREAPI uint32_t* BNGetImplicitlyDefinedRegisters(
    BNCallingConvention* cc, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetIncomingRegisterValue(
    BNCallingConvention* cc, uint32_t reg, BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNRegisterValue BNGetIncomingFlagValue(
    BNCallingConvention* cc, uint32_t reg, BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNVariable BNGetIncomingVariableForParameterVariable(
    BNCallingConvention* cc, const BNVariable* var, BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNVariable BNGetParameterVariableForIncomingVariable(
    BNCallingConvention* cc, const BNVariable* var, BNFunction* func) {
  return {};
}
BINARYNINJACOREAPI BNVariable BNGetDefaultIncomingVariableForParameterVariable(
    BNCallingConvention* cc, const BNVariable* var) {
  return {};
}
BINARYNINJACOREAPI BNVariable BNGetDefaultParameterVariableForIncomingVariable(
    BNCallingConvention* cc, const BNVariable* var) {
  return {};
}
BINARYNINJACOREAPI BNCallingConvention*
BNGetArchitectureDefaultCallingConvention(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI BNCallingConvention* BNGetArchitectureCdeclCallingConvention(
    BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI BNCallingConvention*
BNGetArchitectureStdcallCallingConvention(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI BNCallingConvention*
BNGetArchitectureFastcallCallingConvention(BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI void BNSetArchitectureDefaultCallingConvention(
    BNArchitecture* arch, BNCallingConvention* cc) {}
BINARYNINJACOREAPI void BNSetArchitectureCdeclCallingConvention(
    BNArchitecture* arch, BNCallingConvention* cc) {}
BINARYNINJACOREAPI void BNSetArchitectureStdcallCallingConvention(
    BNArchitecture* arch, BNCallingConvention* cc) {}
BINARYNINJACOREAPI void BNSetArchitectureFastcallCallingConvention(
    BNArchitecture* arch, BNCallingConvention* cc) {}
BINARYNINJACOREAPI BNPlatform* BNCreatePlatform(BNArchitecture* arch,
                                                const char* name) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterPlatform(const char* os,
                                           BNPlatform* platform) {}
BINARYNINJACOREAPI BNPlatform* BNNewPlatformReference(BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI void BNFreePlatform(BNPlatform* platform) {}
BINARYNINJACOREAPI char* BNGetPlatformName(BNPlatform* platform) { return {}; }
BINARYNINJACOREAPI BNArchitecture* BNGetPlatformArchitecture(
    BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI BNPlatform* BNGetPlatformByName(const char* name) {
  return {};
}
BINARYNINJACOREAPI BNPlatform** BNGetPlatformList(size_t* count) { return {}; }
BINARYNINJACOREAPI BNPlatform** BNGetPlatformListByArchitecture(
    BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNPlatform** BNGetPlatformListByOS(const char* os,
                                                      size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNPlatform** BNGetPlatformListByOSAndArchitecture(
    const char* os, BNArchitecture* arch, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreePlatformList(BNPlatform** platform,
                                           size_t count) {}
BINARYNINJACOREAPI char** BNGetPlatformOSList(size_t* count) { return {}; }
BINARYNINJACOREAPI void BNFreePlatformOSList(char** list, size_t count) {}
BINARYNINJACOREAPI BNCallingConvention* BNGetPlatformDefaultCallingConvention(
    BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI BNCallingConvention* BNGetPlatformCdeclCallingConvention(
    BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI BNCallingConvention* BNGetPlatformStdcallCallingConvention(
    BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI BNCallingConvention* BNGetPlatformFastcallCallingConvention(
    BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI BNCallingConvention** BNGetPlatformCallingConventions(
    BNPlatform* platform, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNCallingConvention* BNGetPlatformSystemCallConvention(
    BNPlatform* platform) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterPlatformCallingConvention(
    BNPlatform* platform, BNCallingConvention* cc) {}
BINARYNINJACOREAPI void BNRegisterPlatformDefaultCallingConvention(
    BNPlatform* platform, BNCallingConvention* cc) {}
BINARYNINJACOREAPI void BNRegisterPlatformCdeclCallingConvention(
    BNPlatform* platform, BNCallingConvention* cc) {}
BINARYNINJACOREAPI void BNRegisterPlatformStdcallCallingConvention(
    BNPlatform* platform, BNCallingConvention* cc) {}
BINARYNINJACOREAPI void BNRegisterPlatformFastcallCallingConvention(
    BNPlatform* platform, BNCallingConvention* cc) {}
BINARYNINJACOREAPI void BNSetPlatformSystemCallConvention(
    BNPlatform* platform, BNCallingConvention* cc) {}
BINARYNINJACOREAPI BNPlatform* BNGetArchitectureStandalonePlatform(
    BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI BNPlatform* BNGetRelatedPlatform(BNPlatform* platform,
                                                    BNArchitecture* arch) {
  return {};
}
BINARYNINJACOREAPI void BNAddRelatedPlatform(BNPlatform* platform,
                                             BNArchitecture* arch,
                                             BNPlatform* related) {}
BINARYNINJACOREAPI BNPlatform* BNGetAssociatedPlatformByAddress(
    BNPlatform* platform, uint64_t* addr) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedNameAndType* BNGetPlatformTypes(
    BNPlatform* platform, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedNameAndType* BNGetPlatformVariables(
    BNPlatform* platform, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNQualifiedNameAndType* BNGetPlatformFunctions(
    BNPlatform* platform, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNSystemCallInfo* BNGetPlatformSystemCalls(
    BNPlatform* platform, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeSystemCallList(BNSystemCallInfo* syscalls,
                                             size_t count) {}
BINARYNINJACOREAPI BNType* BNGetPlatformTypeByName(BNPlatform* platform,
                                                   BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNType* BNGetPlatformVariableByName(BNPlatform* platform,
                                                       BNQualifiedName* name) {
  return {};
}
BINARYNINJACOREAPI BNType* BNGetPlatformFunctionByName(BNPlatform* platform,
                                                       BNQualifiedName* name,
                                                       bool exactMatch) {
  return {};
}
BINARYNINJACOREAPI char* BNGetPlatformSystemCallName(BNPlatform* platform,
                                                     uint32_t number) {
  return {};
}
BINARYNINJACOREAPI BNType* BNGetPlatformSystemCallType(BNPlatform* platform,
                                                       uint32_t number) {
  return {};
}
BINARYNINJACOREAPI BNTypeLibrary** BNGetPlatformTypeLibraries(
    BNPlatform* platform, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNTypeLibrary** BNGetPlatformTypeLibrariesByName(
    BNPlatform* platform, char* depName, size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNDemangleMS(BNArchitecture* arch,
                                     const char* mangledName, BNType** outType,
                                     char*** outVarName,
                                     size_t* outVarNameElements,
                                     const bool simplify) {
  return {};
}
BINARYNINJACOREAPI bool BNDemangleMSWithOptions(
    BNArchitecture* arch, const char* mangledName, BNType** outType,
    char*** outVarName, size_t* outVarNameElements,
    const BNBinaryView* const view) {
  return {};
}
BINARYNINJACOREAPI BNDownloadProvider* BNRegisterDownloadProvider(
    const char* name, BNDownloadProviderCallbacks* callbacks) {
  return {};
}
BINARYNINJACOREAPI BNDownloadProvider** BNGetDownloadProviderList(
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeDownloadProviderList(
    BNDownloadProvider** providers) {}
BINARYNINJACOREAPI BNDownloadProvider* BNGetDownloadProviderByName(
    const char* name) {
  return {};
}
BINARYNINJACOREAPI char* BNGetDownloadProviderName(
    BNDownloadProvider* provider) {
  return {};
}
BINARYNINJACOREAPI BNDownloadInstance* BNCreateDownloadProviderInstance(
    BNDownloadProvider* provider) {
  return {};
}
BINARYNINJACOREAPI BNDownloadInstance* BNInitDownloadInstance(
    BNDownloadProvider* provider, BNDownloadInstanceCallbacks* callbacks) {
  return {};
}
BINARYNINJACOREAPI BNDownloadInstance* BNNewDownloadInstanceReference(
    BNDownloadInstance* instance) {
  return {};
}
BINARYNINJACOREAPI void BNFreeDownloadInstance(BNDownloadInstance* instance) {}
BINARYNINJACOREAPI void BNFreeDownloadInstanceResponse(
    BNDownloadInstanceResponse* response) {}
BINARYNINJACOREAPI int BNPerformDownloadRequest(
    BNDownloadInstance* instance, const char* url,
    BNDownloadInstanceOutputCallbacks* callbacks) {
  return {};
}
BINARYNINJACOREAPI int BNPerformCustomRequest(
    BNDownloadInstance* instance, const char* method, const char* url,
    uint64_t headerCount, const char* const* headerKeys,
    const char* const* headerValues, BNDownloadInstanceResponse** response,
    BNDownloadInstanceInputOutputCallbacks* callbacks) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNReadDataForDownloadInstance(
    BNDownloadInstance* instance, uint8_t* data, uint64_t len) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNWriteDataForDownloadInstance(
    BNDownloadInstance* instance, uint8_t* data, uint64_t len) {
  return {};
}
BINARYNINJACOREAPI bool BNNotifyProgressForDownloadInstance(
    BNDownloadInstance* instance, uint64_t progress, uint64_t total) {
  return {};
}
BINARYNINJACOREAPI char* BNGetErrorForDownloadInstance(
    BNDownloadInstance* instance) {
  return {};
}
BINARYNINJACOREAPI void BNSetErrorForDownloadInstance(
    BNDownloadInstance* instance, const char* error) {}
BINARYNINJACOREAPI BNScriptingProvider* BNRegisterScriptingProvider(
    const char* name, BNScriptingProviderCallbacks* callbacks) {
  return {};
}
BINARYNINJACOREAPI BNScriptingProvider** BNGetScriptingProviderList(
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeScriptingProviderList(
    BNScriptingProvider** providers) {}
BINARYNINJACOREAPI BNScriptingProvider* BNGetScriptingProviderByName(
    const char* name) {
  return {};
}
BINARYNINJACOREAPI char* BNGetScriptingProviderName(
    BNScriptingProvider* provider) {
  return {};
}
BINARYNINJACOREAPI BNScriptingInstance* BNCreateScriptingProviderInstance(
    BNScriptingProvider* provider) {
  return {};
}
BINARYNINJACOREAPI BNScriptingInstance* BNInitScriptingInstance(
    BNScriptingProvider* provider, BNScriptingInstanceCallbacks* callbacks) {
  return {};
}
BINARYNINJACOREAPI BNScriptingInstance* BNNewScriptingInstanceReference(
    BNScriptingInstance* instance) {
  return {};
}
BINARYNINJACOREAPI void BNFreeScriptingInstance(BNScriptingInstance* instance) {
}
BINARYNINJACOREAPI void BNNotifyOutputForScriptingInstance(
    BNScriptingInstance* instance, const char* text) {}
BINARYNINJACOREAPI void BNNotifyErrorForScriptingInstance(
    BNScriptingInstance* instance, const char* text) {}
BINARYNINJACOREAPI void BNNotifyInputReadyStateForScriptingInstance(
    BNScriptingInstance* instance, BNScriptingProviderInputReadyState state) {}
BINARYNINJACOREAPI void BNRegisterScriptingInstanceOutputListener(
    BNScriptingInstance* instance, BNScriptingOutputListener* callbacks) {}
BINARYNINJACOREAPI void BNUnregisterScriptingInstanceOutputListener(
    BNScriptingInstance* instance, BNScriptingOutputListener* callbacks) {}
BINARYNINJACOREAPI const char* BNGetScriptingInstanceDelimiters(
    BNScriptingInstance* instance) {
  return {};
}
BINARYNINJACOREAPI void BNSetScriptingInstanceDelimiters(
    BNScriptingInstance* instance, const char* delimiters) {}
BINARYNINJACOREAPI BNScriptingProviderInputReadyState
BNGetScriptingInstanceInputReadyState(BNScriptingInstance* instance) {
  return {};
}
BINARYNINJACOREAPI BNScriptingProviderExecuteResult
BNExecuteScriptInput(BNScriptingInstance* instance, const char* input) {
  return {};
}
BINARYNINJACOREAPI void BNCancelScriptInput(BNScriptingInstance* instance) {}
BINARYNINJACOREAPI void BNSetScriptingInstanceCurrentBinaryView(
    BNScriptingInstance* instance, BNBinaryView* view) {}
BINARYNINJACOREAPI void BNSetScriptingInstanceCurrentFunction(
    BNScriptingInstance* instance, BNFunction* func) {}
BINARYNINJACOREAPI void BNSetScriptingInstanceCurrentBasicBlock(
    BNScriptingInstance* instance, BNBasicBlock* block) {}
BINARYNINJACOREAPI void BNSetScriptingInstanceCurrentAddress(
    BNScriptingInstance* instance, uint64_t addr) {}
BINARYNINJACOREAPI void BNSetScriptingInstanceCurrentSelection(
    BNScriptingInstance* instance, uint64_t begin, uint64_t end) {}
BINARYNINJACOREAPI char* BNScriptingInstanceCompleteInput(
    BNScriptingInstance* instance, const char* text, uint64_t state) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterMainThread(BNMainThreadCallbacks* callbacks) {
}
BINARYNINJACOREAPI BNMainThreadAction* BNNewMainThreadActionReference(
    BNMainThreadAction* action) {
  return {};
}
BINARYNINJACOREAPI void BNFreeMainThreadAction(BNMainThreadAction* action) {}
BINARYNINJACOREAPI void BNExecuteMainThreadAction(BNMainThreadAction* action) {}
BINARYNINJACOREAPI bool BNIsMainThreadActionDone(BNMainThreadAction* action) {
  return {};
}
BINARYNINJACOREAPI void BNWaitForMainThreadAction(BNMainThreadAction* action) {}
BINARYNINJACOREAPI BNMainThreadAction* BNExecuteOnMainThread(
    void* ctxt, void (*func)(void* ctxt)) {
  return {};
}
BINARYNINJACOREAPI void BNExecuteOnMainThreadAndWait(void* ctxt,
                                                     void (*func)(void* ctxt)) {
}
BINARYNINJACOREAPI bool BNIsMainThread(void) { return {}; }
BINARYNINJACOREAPI void BNWorkerEnqueue(void* ctxt,
                                        void (*action)(void* ctxt)) {}
BINARYNINJACOREAPI void BNWorkerPriorityEnqueue(void* ctxt,
                                                void (*action)(void* ctxt)) {}
BINARYNINJACOREAPI void BNWorkerInteractiveEnqueue(void* ctxt,
                                                   void (*action)(void* ctxt)) {
}
BINARYNINJACOREAPI size_t BNGetWorkerThreadCount(void) { return {}; }
BINARYNINJACOREAPI void BNSetWorkerThreadCount(size_t count) {}
BINARYNINJACOREAPI BNBackgroundTask* BNBeginBackgroundTask(
    const char* initialText, bool canCancel) {
  return {};
}
BINARYNINJACOREAPI void BNFinishBackgroundTask(BNBackgroundTask* task) {}
BINARYNINJACOREAPI void BNSetBackgroundTaskProgressText(BNBackgroundTask* task,
                                                        const char* text) {}
BINARYNINJACOREAPI bool BNIsBackgroundTaskCancelled(BNBackgroundTask* task) {
  return {};
}
BINARYNINJACOREAPI BNBackgroundTask** BNGetRunningBackgroundTasks(
    size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNBackgroundTask* BNNewBackgroundTaskReference(
    BNBackgroundTask* task) {
  return {};
}
BINARYNINJACOREAPI void BNFreeBackgroundTask(BNBackgroundTask* task) {}
BINARYNINJACOREAPI void BNFreeBackgroundTaskList(BNBackgroundTask** tasks,
                                                 size_t count) {}
BINARYNINJACOREAPI char* BNGetBackgroundTaskProgressText(
    BNBackgroundTask* task) {
  return {};
}
BINARYNINJACOREAPI bool BNCanCancelBackgroundTask(BNBackgroundTask* task) {
  return {};
}
BINARYNINJACOREAPI void BNCancelBackgroundTask(BNBackgroundTask* task) {}
BINARYNINJACOREAPI bool BNIsBackgroundTaskFinished(BNBackgroundTask* task) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterInteractionHandler(
    BNInteractionHandlerCallbacks* callbacks) {}
BINARYNINJACOREAPI char* BNMarkdownToHTML(const char* contents) { return {}; }
BINARYNINJACOREAPI void BNShowPlainTextReport(BNBinaryView* view,
                                              const char* title,
                                              const char* contents) {}
BINARYNINJACOREAPI void BNShowMarkdownReport(BNBinaryView* view,
                                             const char* title,
                                             const char* contents,
                                             const char* plaintext) {}
BINARYNINJACOREAPI void BNShowHTMLReport(BNBinaryView* view, const char* title,
                                         const char* contents,
                                         const char* plaintext) {}
BINARYNINJACOREAPI void BNShowGraphReport(BNBinaryView* view, const char* title,
                                          BNFlowGraph* graph) {}
BINARYNINJACOREAPI void BNShowReportCollection(const char* title,
                                               BNReportCollection* reports) {}
BINARYNINJACOREAPI bool BNGetTextLineInput(char** result, const char* prompt,
                                           const char* title) {
  return {};
}
BINARYNINJACOREAPI bool BNGetIntegerInput(int64_t* result, const char* prompt,
                                          const char* title) {
  return {};
}
BINARYNINJACOREAPI bool BNGetAddressInput(uint64_t* result, const char* prompt,
                                          const char* title, BNBinaryView* view,
                                          uint64_t currentAddr) {
  return {};
}
BINARYNINJACOREAPI bool BNGetChoiceInput(size_t* result, const char* prompt,
                                         const char* title,
                                         const char** choices, size_t count) {
  return {};
}
BINARYNINJACOREAPI bool BNGetOpenFileNameInput(char** result,
                                               const char* prompt,
                                               const char* ext) {
  return {};
}
BINARYNINJACOREAPI bool BNGetSaveFileNameInput(char** result,
                                               const char* prompt,
                                               const char* ext,
                                               const char* defaultName) {
  return {};
}
BINARYNINJACOREAPI bool BNGetDirectoryNameInput(char** result,
                                                const char* prompt,
                                                const char* defaultName) {
  return {};
}
BINARYNINJACOREAPI bool BNGetFormInput(BNFormInputField* fields, size_t count,
                                       const char* title) {
  return {};
}
BINARYNINJACOREAPI void BNFreeFormInputResults(BNFormInputField* fields,
                                               size_t count) {}
BINARYNINJACOREAPI BNMessageBoxButtonResult
BNShowMessageBox(const char* title, const char* text,
                 BNMessageBoxButtonSet buttons, BNMessageBoxIcon icon) {
  return {};
}
BINARYNINJACOREAPI BNReportCollection* BNCreateReportCollection(void) {
  return {};
}
BINARYNINJACOREAPI BNReportCollection* BNNewReportCollectionReference(
    BNReportCollection* reports) {
  return {};
}
BINARYNINJACOREAPI void BNFreeReportCollection(BNReportCollection* reports) {}
BINARYNINJACOREAPI size_t
BNGetReportCollectionCount(BNReportCollection* reports) {
  return {};
}
BINARYNINJACOREAPI BNReportType BNGetReportType(BNReportCollection* reports,
                                                size_t i) {
  return {};
}
BINARYNINJACOREAPI BNBinaryView* BNGetReportView(BNReportCollection* reports,
                                                 size_t i) {
  return {};
}
BINARYNINJACOREAPI char* BNGetReportTitle(BNReportCollection* reports,
                                          size_t i) {
  return {};
}
BINARYNINJACOREAPI char* BNGetReportContents(BNReportCollection* reports,
                                             size_t i) {
  return {};
}
BINARYNINJACOREAPI char* BNGetReportPlainText(BNReportCollection* reports,
                                              size_t i) {
  return {};
}
BINARYNINJACOREAPI BNFlowGraph* BNGetReportFlowGraph(
    BNReportCollection* reports, size_t i) {
  return {};
}
BINARYNINJACOREAPI void BNAddPlainTextReportToCollection(
    BNReportCollection* reports, BNBinaryView* view, const char* title,
    const char* contents) {}
BINARYNINJACOREAPI void BNAddMarkdownReportToCollection(
    BNReportCollection* reports, BNBinaryView* view, const char* title,
    const char* contents, const char* plaintext) {}
BINARYNINJACOREAPI void BNAddHTMLReportToCollection(BNReportCollection* reports,
                                                    BNBinaryView* view,
                                                    const char* title,
                                                    const char* contents,
                                                    const char* plaintext) {}
BINARYNINJACOREAPI void BNAddGraphReportToCollection(
    BNReportCollection* reports, BNBinaryView* view, const char* title,
    BNFlowGraph* graph) {}
BINARYNINJACOREAPI bool BNIsGNU3MangledString(const char* mangledName) {
  return {};
}
BINARYNINJACOREAPI bool BNDemangleGNU3(BNArchitecture* arch,
                                       const char* mangledName,
                                       BNType** outType, char*** outVarName,
                                       size_t* outVarNameElements,
                                       const bool simplify) {
  return {};
}
BINARYNINJACOREAPI bool BNDemangleGNU3WithOptions(
    BNArchitecture* arch, const char* mangledName, BNType** outType,
    char*** outVarName, size_t* outVarNameElements,
    const BNBinaryView* const view) {
  return {};
}
BINARYNINJACOREAPI void BNFreeDemangledName(char*** name, size_t nameElements) {
}
BINARYNINJACOREAPI char** BNPluginGetApis(BNRepoPlugin* p, size_t* count) {
  return {};
}
BINARYNINJACOREAPI const char* BNPluginGetAuthor(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI const char* BNPluginGetDescription(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI const char* BNPluginGetLicense(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI const char* BNPluginGetLicenseText(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI const char* BNPluginGetLongdescription(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNPluginGetMinimumVersion(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI const char* BNPluginGetName(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI const char* BNPluginGetProjectUrl(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI const char* BNPluginGetPackageUrl(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI const char* BNPluginGetAuthorUrl(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI const char* BNPluginGetVersion(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI const char* BNPluginGetCommit(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI void BNFreePluginTypes(BNPluginType* r) {}
BINARYNINJACOREAPI BNRepoPlugin* BNNewPluginReference(BNRepoPlugin* r) {
  return {};
}
BINARYNINJACOREAPI void BNFreePlugin(BNRepoPlugin* plugin) {}
BINARYNINJACOREAPI const char* BNPluginGetPath(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginIsInstalled(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginIsEnabled(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI BNPluginStatus BNPluginGetPluginStatus(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI BNPluginType* BNPluginGetPluginTypes(BNRepoPlugin* p,
                                                        size_t* count) {
  return {};
}
BINARYNINJACOREAPI bool BNPluginEnable(BNRepoPlugin* p, bool force) {
  return {};
}
BINARYNINJACOREAPI bool BNPluginDisable(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginInstall(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginUninstall(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginUpdate(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI char* BNPluginGetInstallInstructions(BNRepoPlugin* p,
                                                        const char* platform) {
  return {};
}
BINARYNINJACOREAPI char** BNPluginGetPlatforms(BNRepoPlugin* p, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreePluginPlatforms(char** platforms, size_t count) {}
BINARYNINJACOREAPI const char* BNPluginGetRepository(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI bool BNPluginIsBeingDeleted(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginIsBeingUpdated(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginIsRunning(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginIsUpdatePending(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginIsDisablePending(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginIsDeletePending(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI bool BNPluginIsUpdateAvailable(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI char* BNPluginGetProjectData(BNRepoPlugin* p) { return {}; }
BINARYNINJACOREAPI uint64_t BNPluginGetLastUpdate(BNRepoPlugin* p) {
  return {};
}
BINARYNINJACOREAPI BNRepository* BNNewRepositoryReference(BNRepository* r) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRepository(BNRepository* r) {}
BINARYNINJACOREAPI char* BNRepositoryGetUrl(BNRepository* r) { return {}; }
BINARYNINJACOREAPI char* BNRepositoryGetRepoPath(BNRepository* r) { return {}; }
BINARYNINJACOREAPI BNRepoPlugin** BNRepositoryGetPlugins(BNRepository* r,
                                                         size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRepositoryPluginList(BNRepoPlugin** r) {}
BINARYNINJACOREAPI void BNRepositoryFreePluginDirectoryList(char** list,
                                                            size_t count) {}
BINARYNINJACOREAPI BNRepoPlugin* BNRepositoryGetPluginByPath(
    BNRepository* r, const char* pluginPath) {
  return {};
}
BINARYNINJACOREAPI const char* BNRepositoryGetPluginsPath(BNRepository* r) {
  return {};
}
BINARYNINJACOREAPI BNRepositoryManager* BNCreateRepositoryManager(
    const char* enabledPluginsPath) {
  return {};
}
BINARYNINJACOREAPI BNRepositoryManager* BNNewRepositoryManagerReference(
    BNRepositoryManager* r) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRepositoryManager(BNRepositoryManager* r) {}
BINARYNINJACOREAPI bool BNRepositoryManagerCheckForUpdates(
    BNRepositoryManager* r) {
  return {};
}
BINARYNINJACOREAPI BNRepository** BNRepositoryManagerGetRepositories(
    BNRepositoryManager* r, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRepositoryManagerRepositoriesList(
    BNRepository** r) {}
BINARYNINJACOREAPI bool BNRepositoryManagerAddRepository(BNRepositoryManager* r,
                                                         const char* url,
                                                         const char* repoPath) {
  return {};
}
BINARYNINJACOREAPI BNRepository* BNRepositoryGetRepositoryByPath(
    BNRepositoryManager* r, const char* repoPath) {
  return {};
}
BINARYNINJACOREAPI BNRepositoryManager* BNGetRepositoryManager() { return {}; }
BINARYNINJACOREAPI BNRepository* BNRepositoryManagerGetDefaultRepository(
    BNRepositoryManager* r) {
  return {};
}
BINARYNINJACOREAPI void BNRegisterForPluginLoading(
    const char* pluginApiName,
    bool (*cb)(const char* repoPath, const char* pluginPath, bool force,
               void* ctx),
    void* ctx) {}
BINARYNINJACOREAPI bool BNLoadPluginForApi(const char* pluginApiName,
                                           const char* repoPath,
                                           const char* pluginPath, bool force) {
  return {};
}
BINARYNINJACOREAPI char** BNGetRegisteredPluginLoaders(size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRegisteredPluginLoadersList(char** pluginLoaders,
                                                          size_t count) {}
BINARYNINJACOREAPI void BNLlvmServicesInit(void) {}
BINARYNINJACOREAPI int BNLlvmServicesAssemble(const char* src, int dialect,
                                              const char* triplet,
                                              int codeModel, int relocMode,
                                              char** outBytes, int* outBytesLen,
                                              char** err, int* errLen) {
  return {};
}
BINARYNINJACOREAPI void BNLlvmServicesAssembleFree(char* outBytes, char* err) {}
BINARYNINJACOREAPI int BNDeleteFile(const char* path) { return {}; }
BINARYNINJACOREAPI int BNDeleteDirectory(const char* path, int contentsOnly) {
  return {};
}
BINARYNINJACOREAPI bool BNCreateDirectory(const char* path,
                                          bool createSubdirectories) {
  return {};
}
BINARYNINJACOREAPI bool BNPathExists(const char* path) { return {}; }
BINARYNINJACOREAPI bool BNIsPathDirectory(const char* path) { return {}; }
BINARYNINJACOREAPI bool BNIsPathRegularFile(const char* path) { return {}; }
BINARYNINJACOREAPI bool BNFileSize(const char* path, uint64_t* size) {
  return {};
}
BINARYNINJACOREAPI bool BNRenameFile(const char* source, const char* dest) {
  return {};
}
BINARYNINJACOREAPI BNSettings* BNCreateSettings(const char* schemaId) {
  return {};
}
BINARYNINJACOREAPI BNSettings* BNNewSettingsReference(BNSettings* settings) {
  return {};
}
BINARYNINJACOREAPI void BNFreeSettings(BNSettings* settings) {}
BINARYNINJACOREAPI void BNSettingsSetResourceId(BNSettings* settings,
                                                const char* resourceId) {}
BINARYNINJACOREAPI bool BNSettingsRegisterGroup(BNSettings* settings,
                                                const char* group,
                                                const char* title) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsRegisterSetting(BNSettings* settings,
                                                  const char* key,
                                                  const char* properties) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsContains(BNSettings* settings,
                                           const char* key) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsIsEmpty(BNSettings* settings) { return {}; }
BINARYNINJACOREAPI const char** BNSettingsKeysList(BNSettings* settings,
                                                   size_t* inoutSize) {
  return {};
}
BINARYNINJACOREAPI const char** BNSettingsQueryPropertyStringList(
    BNSettings* settings, const char* key, const char* property,
    size_t* inoutSize) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsUpdateProperty(BNSettings* settings,
                                                 const char* key,
                                                 const char* property) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsUpdateBoolProperty(BNSettings* settings,
                                                     const char* key,
                                                     const char* property,
                                                     bool value) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsUpdateDoubleProperty(BNSettings* settings,
                                                       const char* key,
                                                       const char* property,
                                                       double value) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsUpdateInt64Property(BNSettings* settings,
                                                      const char* key,
                                                      const char* property,
                                                      int64_t value) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsUpdateUInt64Property(BNSettings* settings,
                                                       const char* key,
                                                       const char* property,
                                                       uint64_t value) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsUpdateStringProperty(BNSettings* settings,
                                                       const char* key,
                                                       const char* property,
                                                       const char* value) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsUpdateStringListProperty(BNSettings* settings,
                                                           const char* key,
                                                           const char* property,
                                                           const char** value,
                                                           size_t size) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsDeserializeSchema(BNSettings* settings,
                                                    const char* schema,
                                                    BNSettingsScope scope,
                                                    bool merge) {
  return {};
}
BINARYNINJACOREAPI char* BNSettingsSerializeSchema(BNSettings* settings) {
  return {};
}
BINARYNINJACOREAPI bool BNDeserializeSettings(BNSettings* settings,
                                              const char* contents,
                                              BNBinaryView* view,
                                              BNSettingsScope scope) {
  return {};
}
BINARYNINJACOREAPI char* BNSerializeSettings(BNSettings* settings,
                                             BNBinaryView* view,
                                             BNSettingsScope scope) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsReset(BNSettings* settings, const char* key,
                                        BNBinaryView* view,
                                        BNSettingsScope scope) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsResetAll(BNSettings* settings,
                                           BNBinaryView* view,
                                           BNSettingsScope scope,
                                           bool schemaOnly) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsGetBool(BNSettings* settings, const char* key,
                                          BNBinaryView* view,
                                          BNSettingsScope* scope) {
  return {};
}
BINARYNINJACOREAPI double BNSettingsGetDouble(BNSettings* settings,
                                              const char* key,
                                              BNBinaryView* view,
                                              BNSettingsScope* scope) {
  return {};
}
BINARYNINJACOREAPI int64_t BNSettingsGetInt64(BNSettings* settings,
                                              const char* key,
                                              BNBinaryView* view,
                                              BNSettingsScope* scope) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNSettingsGetUInt64(BNSettings* settings,
                                                const char* key,
                                                BNBinaryView* view,
                                                BNSettingsScope* scope) {
  return {};
}
BINARYNINJACOREAPI char* BNSettingsGetString(BNSettings* settings,
                                             const char* key,
                                             BNBinaryView* view,
                                             BNSettingsScope* scope) {
  return {};
}
BINARYNINJACOREAPI const char** BNSettingsGetStringList(BNSettings* settings,
                                                        const char* key,
                                                        BNBinaryView* view,
                                                        BNSettingsScope* scope,
                                                        size_t* inoutSize) {
  return {};
}
BINARYNINJACOREAPI char* BNSettingsGetJson(BNSettings* settings,
                                           const char* key, BNBinaryView* view,
                                           BNSettingsScope* scope) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsSetBool(BNSettings* settings,
                                          BNBinaryView* view,
                                          BNSettingsScope scope,
                                          const char* key, bool value) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsSetDouble(BNSettings* settings,
                                            BNBinaryView* view,
                                            BNSettingsScope scope,
                                            const char* key, double value) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsSetInt64(BNSettings* settings,
                                           BNBinaryView* view,
                                           BNSettingsScope scope,
                                           const char* key, int64_t value) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsSetUInt64(BNSettings* settings,
                                            BNBinaryView* view,
                                            BNSettingsScope scope,
                                            const char* key, uint64_t value) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsSetString(BNSettings* settings,
                                            BNBinaryView* view,
                                            BNSettingsScope scope,
                                            const char* key,
                                            const char* value) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsSetStringList(
    BNSettings* settings, BNBinaryView* view, BNSettingsScope scope,
    const char* key, const char** value, size_t size) {
  return {};
}
BINARYNINJACOREAPI bool BNSettingsSetJson(BNSettings* settings,
                                          BNBinaryView* view,
                                          BNSettingsScope scope,
                                          const char* key, const char* value) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNNewMetadataReference(BNMetadata* data) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNCreateMetadataBooleanData(bool data) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNCreateMetadataStringData(const char* data) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNCreateMetadataUnsignedIntegerData(
    uint64_t data) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNCreateMetadataSignedIntegerData(int64_t data) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNCreateMetadataDoubleData(double data) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNCreateMetadataOfType(BNMetadataType type) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNCreateMetadataRawData(const uint8_t* data,
                                                       size_t size) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNCreateMetadataArray(BNMetadata** data,
                                                     size_t size) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNCreateMetadataValueStore(const char** keys,
                                                          BNMetadata** values,
                                                          size_t size) {
  return {};
}
BINARYNINJACOREAPI bool BNMetadataIsEqual(BNMetadata* lhs, BNMetadata* rhs) {
  return {};
}
BINARYNINJACOREAPI bool BNMetadataSetValueForKey(BNMetadata* data,
                                                 const char* key,
                                                 BNMetadata* md) {
  return {};
}
BINARYNINJACOREAPI BNMetadata* BNMetadataGetForKey(BNMetadata* data,
                                                   const char* key) {
  return {};
}
BINARYNINJACOREAPI bool BNMetadataArrayAppend(BNMetadata* data,
                                              BNMetadata* md) {
  return {};
}
BINARYNINJACOREAPI void BNMetadataRemoveKey(BNMetadata* data, const char* key) {
}
BINARYNINJACOREAPI size_t BNMetadataSize(BNMetadata* data) { return {}; }
BINARYNINJACOREAPI BNMetadata* BNMetadataGetForIndex(BNMetadata* data,
                                                     size_t index) {
  return {};
}
BINARYNINJACOREAPI void BNMetadataRemoveIndex(BNMetadata* data, size_t index) {}
BINARYNINJACOREAPI void BNFreeMetadataArray(BNMetadata** data) {}
BINARYNINJACOREAPI void BNFreeMetadataValueStore(BNMetadataValueStore* data) {}
BINARYNINJACOREAPI void BNFreeMetadata(BNMetadata* data) {}
BINARYNINJACOREAPI void BNFreeMetadataRaw(uint8_t* data) {}
BINARYNINJACOREAPI bool BNMetadataGetBoolean(BNMetadata* data) { return {}; }
BINARYNINJACOREAPI char* BNMetadataGetString(BNMetadata* data) { return {}; }
BINARYNINJACOREAPI uint64_t BNMetadataGetUnsignedInteger(BNMetadata* data) {
  return {};
}
BINARYNINJACOREAPI int64_t BNMetadataGetSignedInteger(BNMetadata* data) {
  return {};
}
BINARYNINJACOREAPI double BNMetadataGetDouble(BNMetadata* data) { return {}; }
BINARYNINJACOREAPI uint8_t* BNMetadataGetRaw(BNMetadata* data, size_t* size) {
  return {};
}
BINARYNINJACOREAPI BNMetadata** BNMetadataGetArray(BNMetadata* data,
                                                   size_t* size) {
  return {};
}
BINARYNINJACOREAPI BNMetadataValueStore* BNMetadataGetValueStore(
    BNMetadata* data) {
  return {};
}
BINARYNINJACOREAPI BNMetadataType BNMetadataGetType(BNMetadata* data) {
  return {};
}
BINARYNINJACOREAPI bool BNMetadataIsBoolean(BNMetadata* data) { return {}; }
BINARYNINJACOREAPI bool BNMetadataIsString(BNMetadata* data) { return {}; }
BINARYNINJACOREAPI bool BNMetadataIsUnsignedInteger(BNMetadata* data) {
  return {};
}
BINARYNINJACOREAPI bool BNMetadataIsSignedInteger(BNMetadata* data) {
  return {};
}
BINARYNINJACOREAPI bool BNMetadataIsDouble(BNMetadata* data) { return {}; }
BINARYNINJACOREAPI bool BNMetadataIsRaw(BNMetadata* data) { return {}; }
BINARYNINJACOREAPI bool BNMetadataIsArray(BNMetadata* data) { return {}; }
BINARYNINJACOREAPI bool BNMetadataIsKeyValueStore(BNMetadata* data) {
  return {};
}
BINARYNINJACOREAPI void BNBinaryViewStoreMetadata(BNBinaryView* view,
                                                  const char* key,
                                                  BNMetadata* value) {}
BINARYNINJACOREAPI BNMetadata* BNBinaryViewQueryMetadata(BNBinaryView* view,
                                                         const char* key) {
  return {};
}
BINARYNINJACOREAPI void BNBinaryViewRemoveMetadata(BNBinaryView* view,
                                                   const char* key) {}
BINARYNINJACOREAPI char** BNBinaryViewGetLoadSettingsTypeNames(
    BNBinaryView* view, size_t* count) {
  return {};
}
BINARYNINJACOREAPI BNSettings* BNBinaryViewGetLoadSettings(
    BNBinaryView* view, const char* typeName) {
  return {};
}
BINARYNINJACOREAPI void BNBinaryViewSetLoadSettings(BNBinaryView* view,
                                                    const char* typeName,
                                                    BNSettings* settings) {}
BINARYNINJACOREAPI BNRelocation* BNNewRelocationReference(BNRelocation* reloc) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRelocation(BNRelocation* reloc) {}
BINARYNINJACOREAPI BNRelocationInfo BNRelocationGetInfo(BNRelocation* reloc) {
  return {};
}
BINARYNINJACOREAPI BNArchitecture* BNRelocationGetArchitecture(
    BNRelocation* reloc) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNRelocationGetTarget(BNRelocation* reloc) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNRelocationGetReloc(BNRelocation* reloc) {
  return {};
}
BINARYNINJACOREAPI BNSymbol* BNRelocationGetSymbol(BNRelocation* reloc) {
  return {};
}
BINARYNINJACOREAPI BNSegment* BNCreateSegment(uint64_t start, uint64_t length,
                                              uint64_t dataOffset,
                                              uint64_t dataLength,
                                              uint32_t flags,
                                              bool autoDefined) {
  return {};
}
BINARYNINJACOREAPI BNSegment* BNNewSegmentReference(BNSegment* seg) {
  return {};
}
BINARYNINJACOREAPI void BNFreeSegment(BNSegment* seg) {}
BINARYNINJACOREAPI BNRange* BNSegmentGetRelocationRanges(BNSegment* segment,
                                                         size_t* count) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNSegmentGetRelocationsCount(BNSegment* segment) {
  return {};
}
BINARYNINJACOREAPI BNRange* BNSegmentGetRelocationRangesAtAddress(
    BNSegment* segment, uint64_t addr, size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeRelocationRanges(BNRange* ranges) {}
BINARYNINJACOREAPI uint64_t BNSegmentGetStart(BNSegment* segment) { return {}; }
BINARYNINJACOREAPI uint64_t BNSegmentGetLength(BNSegment* segment) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNSegmentGetEnd(BNSegment* segment) { return {}; }
BINARYNINJACOREAPI uint64_t BNSegmentGetDataEnd(BNSegment* segment) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNSegmentGetDataOffset(BNSegment* segment) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNSegmentGetDataLength(BNSegment* segment) {
  return {};
}
BINARYNINJACOREAPI uint32_t BNSegmentGetFlags(BNSegment* segment) { return {}; }
BINARYNINJACOREAPI bool BNSegmentIsAutoDefined(BNSegment* segment) {
  return {};
}
BINARYNINJACOREAPI void BNSegmentSetLength(BNSegment* segment,
                                           uint64_t length) {}
BINARYNINJACOREAPI void BNSegmentSetDataOffset(BNSegment* segment,
                                               uint64_t dataOffset) {}
BINARYNINJACOREAPI void BNSegmentSetDataLength(BNSegment* segment,
                                               uint64_t dataLength) {}
BINARYNINJACOREAPI void BNSegmentSetFlags(BNSegment* segment, uint32_t flags) {}
BINARYNINJACOREAPI BNSection* BNNewSectionReference(BNSection* section) {
  return {};
}
BINARYNINJACOREAPI void BNFreeSection(BNSection* section) {}
BINARYNINJACOREAPI char* BNSectionGetName(BNSection* section) { return {}; }
BINARYNINJACOREAPI char* BNSectionGetType(BNSection* section) { return {}; }
BINARYNINJACOREAPI uint64_t BNSectionGetStart(BNSection* section) { return {}; }
BINARYNINJACOREAPI uint64_t BNSectionGetLength(BNSection* section) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNSectionGetEnd(BNSection* section) { return {}; }
BINARYNINJACOREAPI char* BNSectionGetLinkedSection(BNSection* section) {
  return {};
}
BINARYNINJACOREAPI char* BNSectionGetInfoSection(BNSection* section) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNSectionGetInfoData(BNSection* section) {
  return {};
}
BINARYNINJACOREAPI uint64_t BNSectionGetAlign(BNSection* section) { return {}; }
BINARYNINJACOREAPI uint64_t BNSectionGetEntrySize(BNSection* section) {
  return {};
}
BINARYNINJACOREAPI BNSectionSemantics
BNSectionGetSemantics(BNSection* section) {
  return {};
}
BINARYNINJACOREAPI bool BNSectionIsAutoDefined(BNSection* section) {
  return {};
}
BINARYNINJACOREAPI BNDataRenderer* BNCreateDataRenderer(
    BNCustomDataRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI BNDataRenderer* BNNewDataRendererReference(
    BNDataRenderer* renderer) {
  return {};
}
BINARYNINJACOREAPI bool BNIsValidForData(void* ctxt, BNBinaryView* view,
                                         uint64_t addr, BNType* type,
                                         BNTypeContext* typeCtx,
                                         size_t ctxCount) {
  return {};
}
BINARYNINJACOREAPI BNDisassemblyTextLine* BNGetLinesForData(
    void* ctxt, BNBinaryView* view, uint64_t addr, BNType* type,
    const BNInstructionTextToken* prefix, size_t prefixCount, size_t width,
    size_t* count, BNTypeContext* typeCtx, size_t ctxCount) {
  return {};
}
BINARYNINJACOREAPI void BNFreeDataRenderer(BNDataRenderer* renderer) {}
BINARYNINJACOREAPI BNDataRendererContainer* BNGetDataRendererContainer() {
  return {};
}
BINARYNINJACOREAPI void BNRegisterGenericDataRenderer(
    BNDataRendererContainer* container, BNDataRenderer* renderer) {}
BINARYNINJACOREAPI void BNRegisterTypeSpecificDataRenderer(
    BNDataRendererContainer* container, BNDataRenderer* renderer) {}
BINARYNINJACOREAPI bool BNParseExpression(BNBinaryView* view,
                                          const char* expression,
                                          uint64_t* offset, uint64_t here,
                                          char** errorString) {
  return {};
}
BINARYNINJACOREAPI void BNFreeParseError(char* errorString) {}
BINARYNINJACOREAPI void* BNRegisterObjectRefDebugTrace(const char* typeName) {
  return {};
}
BINARYNINJACOREAPI void BNUnregisterObjectRefDebugTrace(const char* typeName,
                                                        void* trace) {}
BINARYNINJACOREAPI BNMemoryUsageInfo* BNGetMemoryUsageInfo(size_t* count) {
  return {};
}
BINARYNINJACOREAPI void BNFreeMemoryUsageInfo(BNMemoryUsageInfo* info,
                                              size_t count) {}
BINARYNINJACOREAPI uint32_t BNGetAddressRenderedWidth(uint64_t addr) {
  return {};
}
BINARYNINJACOREAPI void BNRustFreeString(const char* const) {}
BINARYNINJACOREAPI void BNRustFreeStringArray(const char** const, uint64_t) {}
BINARYNINJACOREAPI char** BNRustSimplifyStrToFQN(const char* const, bool) {
  return {};
}
BINARYNINJACOREAPI char* BNRustSimplifyStrToStr(const char* const) {
  return {};
}
}  // extern "C"
