ARG SOURCE_COMMIT
FROM --platform=$TARGETPLATFORM yolean/kafka-keyvalue:${SOURCE_COMMIT}-${TARGETARCH}${TARGETVARIANT}
