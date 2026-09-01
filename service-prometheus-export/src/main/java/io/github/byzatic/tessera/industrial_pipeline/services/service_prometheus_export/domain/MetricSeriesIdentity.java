package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.domain;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;

import java.util.Map;
import java.util.TreeMap;

public record MetricSeriesIdentity(String metricName, Map<String, String> labels) {
    private static final String REASON_LABEL = "reason";

    public MetricSeriesIdentity {
        labels = Map.copyOf(labels);
    }

    public static MetricSeriesIdentity from(MetricSample metric) {
        Map<String, String> labels = new TreeMap<>();
        metric.labels().stream()
                .filter(label -> !REASON_LABEL.equals(label.key()))
                .forEach(label -> labels.put(label.key(), label.value()));
        return new MetricSeriesIdentity(metric.name(), labels);
    }
}
