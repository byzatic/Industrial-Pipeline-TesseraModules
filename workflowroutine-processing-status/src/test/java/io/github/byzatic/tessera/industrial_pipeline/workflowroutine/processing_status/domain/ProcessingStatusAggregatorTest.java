package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.processing_status.domain;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricLabel;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProcessingStatusAggregatorTest {

    @Test
    public void usesMaximumSeverityAndCombinesReasons() {
        MetricSample warning = metric("1", "node.a => warning");
        MetricSample critical = metric("2", "node.b => critical");

        MetricSample result = new ProcessingStatusAggregator().aggregate(
                "pipeline_status",
                List.of(warning, critical),
                List.of(new MetricLabel("site", "north", "=")),
                "root"
        );

        assertEquals("2", result.value());
        assertEquals("node.a => warning *|* node.b => critical", result.labels().get(1).value());
    }

    private static MetricSample metric(String value, String reason) {
        return new MetricSample(
                "status",
                value,
                Instant.EPOCH,
                List.of(new MetricLabel("reason", reason, "="))
        );
    }
}
