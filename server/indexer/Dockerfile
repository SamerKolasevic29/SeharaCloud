# === STAGE 1: Build C++ Indexer ===
FROM debian:bookworm-slim AS build

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    pkg-config \
    libpqxx-dev \
    libpq-dev \
    libtag1-dev \
    libpoppler-cpp-dev \
    libmagick++-dev \
    libssl-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /src
COPY . .

RUN mkdir build && cd build && \
    cmake -DCMAKE_BUILD_TYPE=Release .. && \
    make -j2

# === STAGE 2: Runtime Container ===
FROM debian:bookworm-slim AS final

RUN apt-get update && apt-get install -y --no-install-recommends \
    libpqxx-dev \
    libpoppler-cpp-dev \
    libmagick++-dev \
    libtag1-dev \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /src/build/indexer_daemon .

ENTRYPOINT ["./indexer_daemon"]
