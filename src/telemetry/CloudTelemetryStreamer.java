package telemetry;

import utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

/**
 * Cloud Native Telemetry Streamer.
 * Directly compresses RAM batches into GZIP payloads and streams them
 * to external Cloud Storage (AWS S3, Cloudflare R2, MinIO, or Remote VPS API)
 * via HTTP PUT/POST, ensuring ZERO disk space is consumed on the local laptop.
 */
public class CloudTelemetryStreamer {

    private static CloudTelemetryStreamer instance;

    private final HttpClient httpClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Metrics for observability
    private final AtomicLong totalCloudBatchesSent = new AtomicLong(0);
    private final AtomicLong totalCloudRecordsSent = new AtomicLong(0);
    private final AtomicLong totalCompressedBytesSent = new AtomicLong(0);
    private final AtomicLong totalCloudUploadSuccess = new AtomicLong(0);
    private final AtomicLong totalCloudUploadFailed = new AtomicLong(0);
    private final AtomicBoolean warnedNoEndpoint = new AtomicBoolean(false);

    public static CloudTelemetryStreamer gI() {
        if (instance == null) {
            synchronized (CloudTelemetryStreamer.class) {
                if (instance == null) {
                    instance = new CloudTelemetryStreamer();
                }
            }
        }
        return instance;
    }

    private CloudTelemetryStreamer() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Compresses the records in RAM using GZIP and streams directly to Cloud Storage.
     * Guaranteed ZERO local disk footprint.
     *
     * @param records List of trajectory records to stream
     * @return true if successfully dispatched or gracefully discarded, false on critical failure
     */
    public boolean streamBatch(List<TrajectoryRecord> records) {
        if (records == null || records.isEmpty()) {
            return true;
        }

        String endpoint = TelemetryConfig.CLOUD_ENDPOINT;
        if (endpoint == null || endpoint.trim().isEmpty()) {
            if (warnedNoEndpoint.compareAndSet(false, true)) {
                Logger.warning("[CloudTelemetry] CLOUD_ENDPOINT chưa được cấu hình. " +
                        "Dữ liệu " + records.size() + " bản ghi được giải phóng an toàn khỏi RAM " +
                        "để bảo vệ ổ cứng laptop (Zero-Disk mode active).\n");
            }
            // Discard safely from RAM without touching local storage
            return true;
        }

        try {
            // 1. Compress raw JSONL in RAM using GZIPOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream(records.size() * 250);
            int rawBytesLength = 0;

            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                for (TrajectoryRecord rec : records) {
                    byte[] lineBytes = (rec.toJson() + "\n").getBytes(StandardCharsets.UTF_8);
                    rawBytesLength += lineBytes.length;
                    gzos.write(lineBytes);
                }
                gzos.finish();
            }

            byte[] gzipPayload = baos.toByteArray();
            totalCompressedBytesSent.addAndGet(gzipPayload.length);
            totalCloudBatchesSent.incrementAndGet();
            totalCloudRecordsSent.addAndGet(records.size());

            // 2. Build HTTP Request
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint.trim()))
                    .timeout(Duration.ofSeconds(TelemetryConfig.CLOUD_TIMEOUT_SEC))
                    .header("Content-Type", "application/x-ndjson")
                    .header("Content-Encoding", "gzip")
                    .header("X-Record-Count", String.valueOf(records.size()))
                    .header("X-Raw-Bytes", String.valueOf(rawBytesLength))
                    .header("X-Compressed-Bytes", String.valueOf(gzipPayload.length))
                    .header("X-Partition-Date", LocalDate.now().format(DATE_FORMATTER))
                    .header("X-Source-System", "NRO-AI-Telemetry")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(gzipPayload));

            if (TelemetryConfig.CLOUD_AUTH_TOKEN != null && !TelemetryConfig.CLOUD_AUTH_TOKEN.trim().isEmpty()) {
                reqBuilder.header("Authorization", "Bearer " + TelemetryConfig.CLOUD_AUTH_TOKEN.trim());
            }

            HttpRequest request = reqBuilder.build();

            // 3. Asynchronously upload to Cloud Storage
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .whenComplete((resp, throwable) -> {
                        if (throwable != null) {
                            totalCloudUploadFailed.incrementAndGet();
                            Logger.logException(CloudTelemetryStreamer.class, (Exception) throwable,
                                    "[CloudTelemetry] Lỗi kết nối đẩy dữ liệu lên Cloud: " + throwable.getMessage());
                        } else if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                            totalCloudUploadSuccess.incrementAndGet();
                        } else {
                            totalCloudUploadFailed.incrementAndGet();
                            Logger.warning("[CloudTelemetry] Cloud server trả về mã lỗi HTTP " + resp.statusCode() +
                                    ": " + resp.body() + "\n");
                        }
                    });

            return true;
        } catch (Exception e) {
            totalCloudUploadFailed.incrementAndGet();
            Logger.logException(CloudTelemetryStreamer.class, e, "[CloudTelemetry] Lỗi nén GZIP trong RAM");
            return false;
        }
    }

    public String getCloudMetricsJson() {
        return String.format(
                "{\"destination\":\"CLOUD_STREAM\",\"endpoint\":\"%s\",\"batches_sent\":%d," +
                "\"records_sent\":%d,\"compressed_bytes\":%d,\"upload_success\":%d,\"upload_failed\":%d}",
                TelemetryConfig.CLOUD_ENDPOINT,
                totalCloudBatchesSent.get(),
                totalCloudRecordsSent.get(),
                totalCompressedBytesSent.get(),
                totalCloudUploadSuccess.get(),
                totalCloudUploadFailed.get()
        );
    }

    public void resetMetrics() {
        totalCloudBatchesSent.set(0);
        totalCloudRecordsSent.set(0);
        totalCompressedBytesSent.set(0);
        totalCloudUploadSuccess.set(0);
        totalCloudUploadFailed.set(0);
        warnedNoEndpoint.set(false);
    }
}

