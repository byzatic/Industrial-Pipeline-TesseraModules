package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.infrastructure.tessera;

import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.github.byzatic.tessera.service.configuration.ServiceConfigurationParameter;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class TesseraServiceSettingsLoader {

    public PrometheusServiceSettings load(MCg3ServiceApiInterface serviceApi) throws Exception {
        Map<String, String> parameters = new HashMap<>();
        for (ServiceConfigurationParameter parameter : serviceApi.getServiceConfigurationParameters()) {
            parameters.put(parameter.getParameterKey(), parameter.getParameterValue());
        }
        URI url = URI.create(required(parameters, "apiURL"));
        int port = url.getPort();
        if (port < 0) {
            throw new IllegalArgumentException("apiURL must contain a port: " + url);
        }
        return new PrometheusServiceSettings(
                required(parameters, "storage"),
                Duration.ofMinutes(Long.parseLong(required(parameters, "expiredMinutesAgo"))),
                url.getHost(),
                port,
                required(parameters, "cronMetricUpdateString")
        );
    }

    private static String required(Map<String, String> parameters, String key) {
        String value = parameters.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required service parameter not found: " + key);
        }
        return value;
    }
}
