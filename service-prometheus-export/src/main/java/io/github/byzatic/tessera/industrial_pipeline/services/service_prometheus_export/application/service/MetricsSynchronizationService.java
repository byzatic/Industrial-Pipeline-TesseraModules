package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.service;

import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.usecase.SynchronizeMetricsUseCase;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.service.MetricPublisher;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.repository.MetricSource;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.domain.MetricSeriesIdentity;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MetricsSynchronizationService implements SynchronizeMetricsUseCase {
    private final MetricSource metricSource;
    private final MetricPublisher metricPublisher;
    private final Duration retention;
    private final Clock clock;
    private Map<MetricSeriesIdentity, MetricSample> publishedMetrics = Map.of();

    public MetricsSynchronizationService(
            MetricSource metricSource,
            MetricPublisher metricPublisher,
            Duration retention,
            Clock clock
    ) {
        this.metricSource = Objects.requireNonNull(metricSource, "metricSource must not be null");
        this.metricPublisher = Objects.requireNonNull(metricPublisher, "metricPublisher must not be null");
        this.retention = Objects.requireNonNull(retention, "retention must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public synchronized void synchronize() throws RoutineExecutionException {
        Instant expirationBoundary = clock.instant().minus(retention);
        Map<MetricSeriesIdentity, MetricSample> incoming = new HashMap<>();
        for (MetricSample metric : metricSource.loadMetrics()) {
            if (!metric.createdAt().isBefore(expirationBoundary)) {
                incoming.put(MetricSeriesIdentity.from(metric), metric);
            }
        }

        publishedMetrics.forEach((identity, metric) -> {
            if (!incoming.containsKey(identity)) {
                metricPublisher.remove(metric);
            }
        });
        incoming.values().forEach(metricPublisher::upsert);
        publishedMetrics = Map.copyOf(incoming);
    }
}
