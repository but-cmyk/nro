# Dockerfile NRO Game Server (Multi-stage build)
# Stage 1: Build JAR using JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY lib/ ./lib/
COPY src/ ./src/

RUN mkdir -p classes dist \
    && find src -name "*.java" > sources.txt \
    && CP=$(find lib -name "*.jar" | tr '\n' ':') \
    && javac -encoding UTF-8 -cp "classes:$CP" -d classes --release 21 @sources.txt \
    && jar --create --file dist/NROK.jar --main-class server.ServerManager -C classes .

# Stage 2: Minimal, secure JRE 21 runtime
FROM eclipse-temurin:21-jre-alpine AS runner

# Create non-root user for security
RUN addgroup -g 10001 -S nro && adduser -u 10001 -S nro -G nro

WORKDIR /app

# Copy dependencies and compiled jar from builder
COPY --from=builder /build/lib/ ./lib/
COPY --from=builder /build/dist/NROK.jar ./NROK.jar

# Data directory will be mounted via volume in docker-compose (- ./data:/app/data)
RUN mkdir -p /app/data && chown -R nro:nro /app

USER nro:nro

EXPOSE 14445 14446

ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -Dfile.encoding=UTF-8 -Dserver.container=true"

# Chạy Server với G1GC tối ưu bộ nhớ RAM thực tế (~60MB - 150MB)
CMD ["java", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=20", "-Xms128M", "-Xmx1024M", "-cp", "NROK.jar:lib/*", "server.ServerManager"]

