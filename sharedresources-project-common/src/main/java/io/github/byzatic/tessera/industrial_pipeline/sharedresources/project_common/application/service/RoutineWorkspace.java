package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.StorageLocation;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.provider.GraphPathProvider;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.repository.MetricStorage;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RoutineWorkspace {
    private final MetricStorage metricStorage;
    private final GraphPathProvider graphPathProvider;
    private final Map<String, MetricSample> localMetrics = new HashMap<>();

    public RoutineWorkspace(MetricStorage metricStorage, GraphPathProvider graphPathProvider) {
        this.metricStorage = Objects.requireNonNull(metricStorage, "metricStorage must not be null");
        this.graphPathProvider = Objects.requireNonNull(graphPathProvider, "graphPathProvider must not be null");
    }

    public MetricSample metric(String id) {
        MetricSample metric = localMetrics.get(id);
        if (metric == null) {
            throw new IllegalArgumentException("Local metric not found: " + id);
        }
        return metric;
    }

    public void save(String id, MetricSample metric) {
        localMetrics.put(
                Objects.requireNonNull(id, "id must not be null"),
                Objects.requireNonNull(metric, "metric must not be null")
        );
    }

    public void clear() {
        localMetrics.clear();
    }

    public MetricSample load(StorageLocation location) throws RoutineExecutionException {
        return metricStorage.load(location);
    }

    public void store(StorageLocation location, MetricSample metric) throws RoutineExecutionException {
        metricStorage.save(location, metric);
    }

    public String graphPath() throws RoutineExecutionException {
        return graphPathProvider.currentGraphPath();
    }
}
