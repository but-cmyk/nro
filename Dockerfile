# Stage 1: Build NRO Game Server (Java 21 LTS)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY lib/ ./lib/
COPY src/ ./src/

RUN mkdir -p out \
    && find src -name "*.java" > sources.txt \
    && javac -encoding UTF-8 --release 21 -cp "lib/*" -d out @sources.txt \
    && jar --create --file NROK.jar --main-class server.ServerManager -C out .

# Stage 2: Minimal Runtime Image (Non-Root, Java 21 LTS)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S nro && adduser -S nro -G nro

COPY --from=builder /build/lib/ ./lib/
COPY --from=builder /build/NROK.jar ./NROK.jar

RUN mkdir -p /app/data /app/logs && chown -R nro:nro /app
USER nro

EXPOSE 14445 14446

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD nc -z 127.0.0.1 14445 || exit 1

CMD ["java", \
     "-server", \
     "-XX:+UseG1GC", \
     "-XX:MaxGCPauseMillis=20", \
     "-XX:+UseStringDeduplication", \
     "-XX:+ExitOnOutOfMemoryError", \
     "-Xms512M", \
     "-Xmx1536M", \
     "-XX:MaxDirectMemorySize=256M", \
     "-XX:MaxMetaspaceSize=192M", \
     "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED", \
     "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
     "--add-opens", "java.base/java.nio=ALL-UNNAMED", \
     "-Dio.netty.tryReflectionSetAccessible=true", \
     "-Dfile.encoding=UTF-8", \
     "-cp", "NROK.jar:lib/*", \
     "server.ServerManager"]
