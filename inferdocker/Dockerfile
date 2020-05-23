FROM debian:stretch-slim
RUN mkdir -p /usr/share/man/man1 && \
    apt-get update && \
    apt-get install --yes --no-install-recommends \
    autoconf \
    automake \
    cmake \
    curl \
    git \
    openjdk-8-jdk-headless \
    maven \
    python2.7 \
    unzip \
    xz-utils 
RUN VERSION=0.17.0; \
    curl -sSL "https://github.com/facebook/infer/releases/download/v$VERSION/infer-linux64-v$VERSION.tar.xz" \
    | tar -C /opt -xJ && \
    ln -s "/opt/infer-linux64-v$VERSION/bin/infer" /usr/local/bin/infer