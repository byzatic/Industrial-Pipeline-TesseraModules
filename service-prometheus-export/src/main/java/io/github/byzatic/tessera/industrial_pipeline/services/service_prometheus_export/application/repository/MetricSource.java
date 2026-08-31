package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.repository;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;

import java.util.List;

public interface MetricSource {
    List<MetricSample> loadMetrics() throws RoutineExecutionException;
}
