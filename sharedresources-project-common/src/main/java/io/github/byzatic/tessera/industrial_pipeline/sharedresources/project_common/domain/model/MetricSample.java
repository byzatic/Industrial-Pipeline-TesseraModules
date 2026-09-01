package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record MetricSample(
        String name,
        String value,
        Instant createdAt,
        List<MetricLabel> labels
) {

    public MetricSample {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        labels = labels == null ? List.of() : List.copyOf(labels);
    }

    public MetricSample withName(String newName) {
        return new MetricSample(newName, value, createdAt, labels);
    }

    public MetricSample withValue(String newValue) {
        return new MetricSample(name, newValue, createdAt, labels);
    }

    public MetricSample withCreatedAt(Instant newCreatedAt) {
        return new MetricSample(name, value, newCreatedAt, labels);
    }

    public MetricSample withLabels(List<MetricLabel> newLabels) {
        return new MetricSample(name, value, createdAt, newLabels);
    }
}
