package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.application.provider;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain.GeneratedStatus;

public interface MetricGeneratorProvider {
    GeneratedStatus generate() throws RoutineExecutionException;
}
