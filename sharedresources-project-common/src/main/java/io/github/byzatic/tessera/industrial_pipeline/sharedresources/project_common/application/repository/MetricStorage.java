package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.repository;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.StorageLocation;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;

public interface MetricStorage {
    MetricSample load(StorageLocation location) throws RoutineExecutionException;

    void save(StorageLocation location, MetricSample metric) throws RoutineExecutionException;
}
