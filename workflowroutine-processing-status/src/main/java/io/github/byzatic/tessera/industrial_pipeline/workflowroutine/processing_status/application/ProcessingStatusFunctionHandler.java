package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.processing_status.application;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.FunctionArguments;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.MetricFunctionHandler;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.RoutineWorkspace;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.processing_status.domain.ProcessingStatusAggregator;

import java.util.List;
import java.util.Objects;

public final class ProcessingStatusFunctionHandler implements MetricFunctionHandler {
    private final ProcessingStatusAggregator aggregator;

    public ProcessingStatusFunctionHandler(ProcessingStatusAggregator aggregator) {
        this.aggregator = Objects.requireNonNull(aggregator, "aggregator must not be null");
    }

    @Override
    public MetricSample execute(
            String functionName,
            FunctionArguments arguments,
            String resultId,
            RoutineWorkspace workspace
    ) throws RoutineExecutionException {
        if (!"ProcessStatus".equals(functionName)) {
            throw new RoutineExecutionException("Unknown processing-status function: " + functionName);
        }
        List<MetricSample> inputs = arguments.all("DataId").stream()
                .map(workspace::metric)
                .toList();
        return aggregator.aggregate(
                arguments.required("MetricName"),
                inputs,
                arguments.prometheusLabels(),
                workspace.graphPath()
        );
    }
}
