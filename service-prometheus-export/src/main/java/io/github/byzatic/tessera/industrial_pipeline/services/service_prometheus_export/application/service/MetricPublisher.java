package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.service;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;

public interface MetricPublisher {
    void upsert(MetricSample metric);

    void remove(MetricSample metric);
}
