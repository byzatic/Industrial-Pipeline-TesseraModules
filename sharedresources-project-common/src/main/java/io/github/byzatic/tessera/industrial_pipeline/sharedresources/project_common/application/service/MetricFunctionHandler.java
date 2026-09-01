package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.FunctionArguments;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;

public interface MetricFunctionHandler {
    MetricSample execute(
            String functionName,
            FunctionArguments arguments,
            String resultId,
            RoutineWorkspace workspace
    ) throws RoutineExecutionException;
}
