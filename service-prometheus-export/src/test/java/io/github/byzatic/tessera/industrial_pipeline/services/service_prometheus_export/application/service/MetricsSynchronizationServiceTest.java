package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.service;

import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.service.MetricPublisher;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MetricsSynchronizationServiceTest {

    @Test
    public void doesNotPublishExpiredStorageValues() throws Exception {
        Instant now = Instant.parse("2026-08-30T20:00:00Z");
        MetricSample expired = new MetricSample("expired", "1", now.minusSeconds(601), List.of());
        MetricSample current = new MetricSample("current", "2", now.minusSeconds(60), List.of());
        RecordingPublisher publisher = new RecordingPublisher();
        MetricsSynchronizationService service = new MetricsSynchronizationService(
                () -> List.of(expired, current),
                publisher,
                Duration.ofMinutes(10),
                Clock.fixed(now, ZoneOffset.UTC)
        );

        service.synchronize();

        assertEquals(List.of(current), publisher.upserted);
    }

    private static final class RecordingPublisher implements MetricPublisher {
        private final List<MetricSample> upserted = new ArrayList<>();

        @Override
        public void upsert(MetricSample metric) {
            upserted.add(metric);
        }

        @Override
        public void remove(MetricSample metric) {
        }
    }
}
