# Dockerfile NRO Game Server
# Build the jar on the host first (build.bat / mvn package), then: docker compose up -d --build
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Chỉ copy thư viện và file JAR đã đóng gói sẵn
COPY lib/ ./lib/
COPY dist/NROK.jar ./NROK.jar

EXPOSE 14445
# EXPOSE 14446  # TLS endpoint (optional)

ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -Dfile.encoding=UTF-8"

# Chạy Server với G1GC tối ưu bộ nhớ RAM thực tế (~60MB - 150MB)
CMD ["java", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=20", "-Xms128M", "-Xmx1024M", "-cp", "NROK.jar:lib/*", "server.ServerManager"]
