package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.graph_lifting_data.application;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.FunctionArguments;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.StorageLocation;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.MetricFunctionHandler;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.RoutineWorkspace;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;

public final class GraphLiftingFunctionHandler implements MetricFunctionHandler {

    @Override
    public MetricSample execute(
            String functionName,
            FunctionArguments arguments,
            String resultId,
            RoutineWorkspace workspace
    ) throws RoutineExecutionException {
        if (!"LiftData".equals(functionName)) {
            throw new RoutineExecutionException("Unknown graph-lifting function: " + functionName);
        }
        String downstreamName = arguments.optional("NodeId")
                .orElseThrow(() -> new IllegalArgumentException(
                        arguments.optional("NodeName").isPresent()
                                ? "NodeName lookup is not supported; use NodeId"
                                : "NodeId is required"
                ));
        return workspace.load(StorageLocation.downstream(
                downstreamName,
                arguments.required("NodeStorage"),
                arguments.required("DataId")
        ));
    }
}
