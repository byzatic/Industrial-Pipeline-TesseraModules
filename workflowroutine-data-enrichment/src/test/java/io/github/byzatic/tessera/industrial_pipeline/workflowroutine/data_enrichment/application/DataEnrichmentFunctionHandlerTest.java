package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment.application;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.FunctionArguments;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.StorageLocation;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.repository.MetricStorage;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.RoutineWorkspace;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricLabel;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DataEnrichmentFunctionHandlerTest {

    @Test
    public void modifyMetricRemovesAndUpsertsLabelsWithoutMutatingSource() throws Exception {
        RoutineWorkspace workspace = new RoutineWorkspace(
                new UnsupportedMetricStorage(),
                () -> "root.node"
        );
        MetricSample source = new MetricSample(
                "old_name",
                "1",
                Instant.EPOCH,
                List.of(
                        new MetricLabel("remove", "old", "="),
                        new MetricLabel("keep", "old", "=")
                )
        );
        workspace.save("source", source);

        MetricSample result = new DataEnrichmentFunctionHandler().execute(
                "ModifyMetric",
                FunctionArguments.parse(List.of(
                        "DataId=source",
                        "RemoveLabelByName=remove",
                        "PromLabel_keep=new",
                        "PromLabel_added=value"
                )),
                "result",
                workspace
        );

        assertEquals(List.of(
                new MetricLabel("keep", "new", "="),
                new MetricLabel("added", "value", "=")
        ), result.labels());
        assertEquals(2, source.labels().size());
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
