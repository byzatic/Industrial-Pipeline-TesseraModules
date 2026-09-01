package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.usecase;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;

public interface SynchronizeMetricsUseCase {
    void synchronize() throws RoutineExecutionException;
}
