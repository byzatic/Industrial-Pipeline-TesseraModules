package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.service;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricLabel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MetricLabels {
    private MetricLabels() {
    }

    public static List<MetricLabel> upsert(List<MetricLabel> existing, List<MetricLabel> replacements) {
        Map<String, MetricLabel> labelsByKey = new LinkedHashMap<>();
        existing.forEach(label -> labelsByKey.put(label.key(), label));
        replacements.forEach(label -> labelsByKey.put(label.key(), label));
        return List.copyOf(labelsByKey.values());
    }

    public static List<MetricLabel> remove(List<MetricLabel> labels, Set<String> keys) {
        List<MetricLabel> result = new ArrayList<>(labels);
        result.removeIf(label -> keys.contains(label.key()));
        return List.copyOf(result);
    }

    public static Optional<MetricLabel> find(List<MetricLabel> labels, String key) {
        return labels.stream().filter(label -> key.equals(label.key())).findFirst();
    }
}
