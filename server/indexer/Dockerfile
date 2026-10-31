# === STAGE 1: Build  ===
FROM alpine:3.19 AS build

RUN apk add --no-cache \
    build-base \
    cmake \
    pkgconf \
    libpq-dev \
    libpqxx-dev \
    taglib-dev \
    poppler-dev \
    imagemagick-dev \
    openssl-dev

WORKDIR /src

COPY . .

RUN mkdir build && cd build && \
    cmake -DCMAKE_BUILD_TYPE=Release .. && \
    make -j$(nproc)

# === STAGE 2: Minimal runtime compiler ===
FROM alpine:3.19 AS final

RUN apk add --no-cache \
    libstdc++ \
    libpq \
    libpqxx \
    taglib \
    poppler-cpp \
    imagemagick \
    openssl \
    ffmpeg

WORKDIR /app

COPY --from=build /src/build/indexer_daemon .

ENTRYPOINT ["./indexer_daemon"]