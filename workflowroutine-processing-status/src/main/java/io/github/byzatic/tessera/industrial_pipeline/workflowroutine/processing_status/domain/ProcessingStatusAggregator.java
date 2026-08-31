package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.processing_status.domain;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricLabel;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.service.MetricLabels;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class ProcessingStatusAggregator {
    private static final String REASON_KEY = "reason";
    private static final String EMPTY_REASON = "Null";
    private static final String REASON_SEPARATOR = " *|* ";

    public MetricSample aggregate(
            String metricName,
            List<MetricSample> inputs,
            List<MetricLabel> labels,
            String graphPath
    ) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("At least one DataId is required");
        }

        int maximumStatus = 0;
        List<String> reasons = new ArrayList<>();
        for (MetricSample input : inputs) {
            int status = Integer.parseInt(input.value());
            maximumStatus = Math.max(maximumStatus, status);
            if (status <= 0) {
                continue;
            }
            String reason = MetricLabels.find(input.labels(), REASON_KEY)
                    .map(MetricLabel::value)
                    .orElse(graphPath + " => [Can't find reason]");
            if (!reason.trim().endsWith("=> Null")) {
                reasons.add(reason);
            }
        }

        MetricLabel reasonLabel = new MetricLabel(
                REASON_KEY,
                reasons.isEmpty() ? EMPTY_REASON : String.join(REASON_SEPARATOR, reasons),
                "="
        );
        return new MetricSample(
                metricName,
                Integer.toString(maximumStatus),
                Instant.now(),
                MetricLabels.upsert(labels, List.of(reasonLabel))
        );
    }
}
