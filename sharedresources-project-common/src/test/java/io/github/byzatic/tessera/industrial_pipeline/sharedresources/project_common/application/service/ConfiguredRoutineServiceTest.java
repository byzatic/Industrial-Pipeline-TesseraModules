package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.StorageLocation;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.repository.MetricStorage;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertThrows;

public class ConfiguredRoutineServiceTest {

    @Test
    public void clearsWorkspaceAfterSuccessfulRun() throws Exception {
        RoutineWorkspace workspace = workspaceWithMetric();
        ConfiguredRoutineService service = new ConfiguredRoutineService(
                () -> List.of("script"),
                script -> {
                },
                workspace
        );

        service.run();

        assertWorkspaceIsEmpty(workspace);
    }

    @Test
    public void clearsWorkspaceAfterFailedRun() {
        RoutineWorkspace workspace = workspaceWithMetric();
        ConfiguredRoutineService service = new ConfiguredRoutineService(
                () -> List.of("script"),
                script -> {
                    throw new RoutineExecutionException("Execution failed");
                },
                workspace
        );

        assertThrows(RoutineExecutionException.class, service::run);

        assertWorkspaceIsEmpty(workspace);
    }

    private static RoutineWorkspace workspaceWithMetric() {
        RoutineWorkspace workspace = new RoutineWorkspace(new UnsupportedMetricStorage(), () -> "graph.path");
        workspace.save("metric", new MetricSample("metric", "1", Instant.EPOCH, List.of()));
        return workspace;
    }

    private static void assertWorkspaceIsEmpty(RoutineWorkspace workspace) {
        assertThrows(IllegalArgumentException.class, () -> workspace.metric("metric"));
    }

    private static final class UnsupportedMetricStorage implements MetricStorage {
        @Override
        public MetricSample load(StorageLocation location) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void save(StorageLocation location, MetricSample metric) {
            throw new UnsupportedOperationException();
        }
    }
}
