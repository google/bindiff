# Dockerfile for a Debian 12 container with Java 21 installed.
# TODO(cblichmann): Rename container to not include Java version
#
# Rebuild and upload with
#   ./rebuild.sh
#   podman push gcr.io/zynamics-build/debian10-java11:latest

# If you modify this container, push it to gcr.io and update our Kokoro builds
# to reference it.

FROM debian:trixie

ARG DEBIAN_FRONTEND=noninteractive
SHELL ["/bin/bash", "-c"]
WORKDIR /build
ENTRYPOINT ["/bin/bash"]

ADD azul.gpg /etc/apt/trusted.gpg.d/
RUN chmod 644 /etc/apt/trusted.gpg.d/azul.gpg && \
    apt-get update && \
    apt-get install -qy --no-install-recommends ca-certificates && \
    echo "deb https://repos.azul.com/zulu/deb stable main" \
        > /etc/apt/sources.list.d/azul.list && \
    apt-get update && \
    apt-get -qy install --no-install-recommends \
        unzip \
        wget \
        zulu21-jdk-headless \
    && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    [[ "$(wget -qO - \
            https://services.gradle.org/distributions/gradle-8.5-bin.zip | \
            tee gradle.zip | sha256sum | cut '-d ' -f1)" == \
        "9d926787066a081739e8200858338b4a69e837c3a821a33aca9db09dd4a41026" ]] && \
    unzip -q gradle.zip && \
    cp -R gradle-*/{bin,lib} /usr/local && \
    ln -s /usr/local/bin/gradle /usr/bin && \
    rm -rf gradle*

ADD third_party /build/src/third_party

ARG GHIDRA_INSTALL_DIR=/build/src/third_party/ghidra \
    YFILES_DIR=/build/src/third_party/java/yfiles/v2_17
RUN pushd src/third_party/zynamics/binexport/java && \
    (gradle || true) && \
    gradle clean && \
    popd && \
    pushd src/third_party/zynamics/bindiff/java && \
    (gradle :zylib:build :ui:obfuscatedJar || true) && \
    gradle clean && \
    gradle --stop && \
    popd && \
    rm -rf /build/*

