package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.application;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.FunctionArguments;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.StorageLocation;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.repository.MetricStorage;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.RoutineWorkspace;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain.GeneratedStatus;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetDataFunctionHandlerTest {

    @Test
    public void generatedWarningContainsGraphPathReason() throws Exception {
        RoutineWorkspace workspace = new RoutineWorkspace(
                new UnsupportedMetricStorage(),
                () -> "root.pump"
        );
        GetDataFunctionHandler handler = new GetDataFunctionHandler(
                () -> new GeneratedStatus(1, "pressure warning")
        );

        MetricSample result = handler.execute(
                "GenerateData",
                FunctionArguments.parse(List.of()),
                "P_IN",
                workspace
        );

        assertEquals("p_in", result.name());
        assertEquals("1", result.value());
        assertEquals("root.pump => pressure warning", result.labels().get(0).value());
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
