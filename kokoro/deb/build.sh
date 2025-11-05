#!/bin/bash

# Fail on any error. Display commands being run.
set -ex

if [ "$1" == "release" ]; then
  echo "Release build"
else
  echo "Continuous integration build"
fi

bindiff_release=8
binexport_release=12

build_dir=${PWD}/build
google3_dir=${PWD}/../../..
third_party_dir=${PWD}/../..

bindiff_pkg_root=packaging/deb/bindiff
bindiff_root=opt/bindiff
app_dir=${bindiff_pkg_root}/files/${bindiff_root}

mkdir -p "${build_dir}"

# Verify/extract dependency
pushd "${KOKORO_GFILE_DIR}"
echo '236b5ea97aff3cb312e743848d7efa77faf305170e41371a732ca93c1b797665  zulu16.28.11-ca-jdk16.0.0-linux_x64.tar.gz' | \
  sha256sum -c
popd
tar -C "${build_dir}" -xzf "${KOKORO_GFILE_DIR}/zulu16.28.11-ca-jdk16.0.0-linux_x64.tar.gz"
export JAVA_HOME=${build_dir}/zulu16.28.11-ca-jdk16.0.0-linux_x64

# Build bundle JRE (see ../msi/build.bat)
"${JAVA_HOME}/bin/jlink" \
  --module-path "${JAVA_HOME}/jmods" \
  --no-header-files \
  --compress=2 \
  --strip-debug \
  --add-modules java.base,java.desktop,java.prefs,java.scripting,java.sql,jdk.unsupported,jdk.xml.dom \
  --output "${bindiff_pkg_root}/files/${bindiff_root}/jre"

# Update install file for bundled JRE
(cd "${bindiff_pkg_root}/files" && find "${bindiff_root}/jre" -type f) | \
  awk -F/ '{
    # Compute dirname($0)
    d = ""; for (i = 1; i < NF; i++) d = d "/" $i;
    print "files/" $0 " " substr(d, 2)
  }' >> "${bindiff_pkg_root}/debian/bindiff.install"

# Copy latest release artifacts.
mkdir -p \
  "${app_dir}/bin" \
  "${app_dir}/extra/config" \
  "${app_dir}/extra/ghidra" \
  "${app_dir}/libexec" \
  "${app_dir}/plugins/idapro"
cp \
  "${KOKORO_GFILE_DIR}/bindiff.jar" \
  "${KOKORO_GFILE_DIR}/bindiff" \
  "${KOKORO_GFILE_DIR}/binexport2dump" \
  "${app_dir}/bin/"
cp \
  "${KOKORO_GFILE_DIR}/bindiff_config_setup" \
  "${app_dir}/libexec/"
cp \
  bindiff_config.proto \
  "${app_dir}/extra/config/bindiff_config.proto"
(cd "${app_dir}/extra/ghidra/" && \
  unzip -q "${KOKORO_GFILE_DIR}/ghidra_BinExport.zip")
cp \
  "${KOKORO_GFILE_DIR}/bindiff${bindiff_release}_ida.so" \
  "${KOKORO_GFILE_DIR}/bindiff${bindiff_release}_ida64.so" \
  "${KOKORO_GFILE_DIR}/binexport${binexport_release}_ida.so" \
  "${KOKORO_GFILE_DIR}/binexport${binexport_release}_ida64.so" \
  "${app_dir}/plugins/idapro/"
cp \
  bindiff.json \
  "${bindiff_pkg_root}/files/etc/${bindiff_root}/bindiff.json"

# Update install file for BinExport Ghidra extension
(cd "${bindiff_pkg_root}/files" && find "${bindiff_root}/extra/ghidra" -type f) | \
  awk -F/ '{
    d = ""; for (i = 1; i < NF; i++) d = d "/" $i;
    print "files/" $0 " " substr(d, 2)
  }' >> "${bindiff_pkg_root}/debian/bindiff.install"

# Build unsigned, binary-only Debian package
pushd ${bindiff_pkg_root}
chmod +w debian/control
debuild -i -us -uc -b
mv ../*.deb "${build_dir}/"
popd

[ "$1" != "release" ] && exit 0

# Release build, code sign the artifacts
# TODO(cblichmann): Re-enable code signing using a separate job.
# echo "Code signing artifacts..."
#
# chmod 777 "${build_dir}"
# chmod 666 "${build_dir}"/*.deb
# /escalated_sign/escalated_sign.py -j /escalated_sign_jobs -t linux_gpg_sign \
#   "${build_dir}/"bindiff_*.deb

