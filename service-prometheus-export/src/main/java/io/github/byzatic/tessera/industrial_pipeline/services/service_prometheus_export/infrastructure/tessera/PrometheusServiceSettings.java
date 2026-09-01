package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.infrastructure.tessera;

import java.time.Duration;

public record PrometheusServiceSettings(
        String storageName,
        Duration retention,
        String serverAddress,
        int serverPort,
        String updateCron
) {
}
