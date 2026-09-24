# Dockerfile NRO Game Server (Siêu nhẹ & Khởi động trong 1 giây)
FROM eclipse-temurin:22-jre-alpine

WORKDIR /app

# Chỉ copy thư viện và file JAR đã đóng gói sẵn
COPY lib/ ./lib/
COPY dist/NROK.jar ./NROK.jar

EXPOSE 14445

# Chạy Server với G1GC tối ưu bộ nhớ RAM thực tế (~60MB - 150MB)
CMD ["java", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=20", "-Xms128M", "-Xmx1024M", "-cp", "NROK.jar:lib/*", "server.ServerManager"]
