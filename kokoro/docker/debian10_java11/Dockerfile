# Dockerfile for a Debian 10 container with Java 11 installed.
# Rebuild and upload with
#   ./rebuild.sh
#   podman push gcr.io/zynamics-build/debian10-java11:latest

# If you modify this container, push it to gcr.io and update our Kokoro builds
# to reference it.

FROM openjdk:17-jdk-bullseye@sha256:0d66ca8d9f5bb7ec86daf28f84bebc732f71a94ecb26be7209770b475d189f23

ENV DEBIAN_FRONTEND noninteractive
SHELL ["/bin/bash", "-c"]
WORKDIR /build
ENTRYPOINT ["/bin/bash"]

RUN \
    [[ "$(wget -qO - \
            https://services.gradle.org/distributions/gradle-7.4.2-bin.zip | \
            tee gradle.zip | sha256sum | cut '-d ' -f1)" == \
        "29e49b10984e585d8118b7d0bc452f944e386458df27371b49b4ac1dec4b7fda" ]] && \
    unzip -q gradle.zip && \
    cp -R gradle-*/{bin,lib} /usr/local && \
    ln -s /usr/local/bin/gradle /usr/bin && \
    rm -rf gradle*
ADD third_party /build/src/third_party

ENV GHIDRA_INSTALL_DIR=/build/src/third_party/ghidra \
    YFILES_DIR=/build/src/third_party/java/yfiles/v2_17
RUN cd src/third_party/zynamics/binexport/java && \
    gradle && \
    gradle clean && gradle --stop
RUN cd src/third_party/zynamics/bindiff/java && \
    gradle clean && \
    gradle :zylib:build :ui:obfuscatedJar && \
    gradle clean && gradle --stop && \
    rm -rf /build/*
