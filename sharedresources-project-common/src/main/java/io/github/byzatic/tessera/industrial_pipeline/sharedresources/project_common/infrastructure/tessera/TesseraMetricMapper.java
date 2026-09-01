package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.tessera;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.MetricLabel;

import java.util.List;

public final class TesseraMetricMapper {

    public MetricSample toDomain(DataItem source) {
        List<io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricLabel>
                labels = source.getMetricLabels() == null
                ? List.of()
                : source.getMetricLabels().stream()
                .map(label -> new io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricLabel(
                        label.getKey(),
                        label.getValue(),
                        label.getSign()
                ))
                .toList();
        return new MetricSample(
                source.getMetricName(),
                source.getMetricValue(),
                source.getMetricCreationTime(),
                labels
        );
    }

    public DataItem toPersistence(MetricSample source) {
        List<MetricLabel> labels = source.labels().stream()
                .map(label -> MetricLabel.newBuilder()
                        .setKey(label.key())
                        .setValue(label.value())
                        .setSign(label.sign())
                        .build())
                .toList();
        return DataItem.newBuilder()
                .setMetricName(source.name())
                .setMetricValue(source.value())
                .setMetricCreationTime(source.createdAt())
                .setMetricLabels(labels)
                .build();
    }
}
