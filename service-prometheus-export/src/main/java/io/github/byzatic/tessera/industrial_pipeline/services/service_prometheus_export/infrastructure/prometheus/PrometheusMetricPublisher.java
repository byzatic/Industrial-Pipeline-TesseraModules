package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.infrastructure.prometheus;

import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.service.MetricPublisher;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricLabel;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import io.prometheus.metrics.core.metrics.Gauge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PrometheusMetricPublisher implements MetricPublisher {
    private static final String REASON_LABEL = "reason";

    private final Map<ScopeIdentity, MetricScope> scopes = new LinkedHashMap<>();

    @Override
    public synchronized void upsert(MetricSample metric) {
        List<MetricLabel> labels = exportableLabels(metric);
        ScopeIdentity identity = ScopeIdentity.from(metric.name(), labels);
        scopes.computeIfAbsent(identity, MetricScope::new).upsert(metric, labels);
    }

    @Override
    public synchronized void remove(MetricSample metric) {
        List<MetricLabel> labels = exportableLabels(metric);
        MetricScope scope = scopes.get(ScopeIdentity.from(metric.name(), labels));
        if (scope != null) {
            scope.remove(labels);
        }
    }

    private static List<MetricLabel> exportableLabels(MetricSample metric) {
        return metric.labels().stream()
                .filter(label -> !REASON_LABEL.equals(label.key()))
                .sorted(java.util.Comparator.comparing(MetricLabel::key))
                .toList();
    }

    private record ScopeIdentity(String name, List<String> labelNames) {
        private static ScopeIdentity from(String name, List<MetricLabel> labels) {
            return new ScopeIdentity(name, labels.stream().map(MetricLabel::key).toList());
        }
    }

    private static final class MetricScope {
        private final Gauge gauge;

        private MetricScope(ScopeIdentity identity) {
            this.gauge = Gauge.builder()
                    .name(identity.name())
                    .labelNames(identity.labelNames().toArray(String[]::new))
                    .register();
        }

        private void upsert(MetricSample metric, List<MetricLabel> labels) {
            gauge.labelValues(labelValues(labels)).set(Double.parseDouble(metric.value()));
        }

        private void remove(List<MetricLabel> labels) {
            gauge.remove(labelValues(labels));
        }

        private static String[] labelValues(List<MetricLabel> labels) {
            List<String> values = new ArrayList<>(labels.size());
            labels.forEach(label -> values.add(label.value()));
            return values.toArray(String[]::new);
        }
    }
}
