package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment.application;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.FunctionArguments;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.MetricFunctionHandler;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.RoutineWorkspace;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricLabel;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.service.MetricLabels;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DataEnrichmentFunctionHandler implements MetricFunctionHandler {

    @Override
    public MetricSample execute(
            String functionName,
            FunctionArguments arguments,
            String resultId,
            RoutineWorkspace workspace
    ) throws RoutineExecutionException {
        return switch (functionName) {
            case "AddGraphPath" -> addGraphPath(arguments, workspace);
            case "AddLabel" -> addLabels(arguments, workspace);
            case "ModifyMetric" -> modifyMetric(arguments, workspace);
            default -> throw new RoutineExecutionException("Unknown data-enrichment function: " + functionName);
        };
    }

    private static MetricSample addGraphPath(
            FunctionArguments arguments,
            RoutineWorkspace workspace
    ) throws RoutineExecutionException {
        MetricSample source = workspace.metric(arguments.required("DataId"));
        MetricLabel graphPath = new MetricLabel("graph_path", workspace.graphPath(), "=");
        return source.withLabels(MetricLabels.upsert(source.labels(), List.of(graphPath)));
    }

    private static MetricSample addLabels(
            FunctionArguments arguments,
            RoutineWorkspace workspace
    ) {
        MetricSample source = workspace.metric(arguments.required("DataId"));
        return source.withLabels(MetricLabels.upsert(source.labels(), arguments.prometheusLabels()));
    }

    private static MetricSample modifyMetric(
            FunctionArguments arguments,
            RoutineWorkspace workspace
    ) {
        MetricSample source = workspace.metric(arguments.required("DataId"));
        List<MetricLabel> labels = source.labels();
        if (arguments.optional("RemoveAllLabels").isPresent()) {
            labels = List.of();
        } else {
            Set<String> labelsToRemove = new HashSet<>(arguments.all("RemoveLabelByName"));
            labels = MetricLabels.remove(labels, labelsToRemove);
        }
        labels = MetricLabels.upsert(labels, arguments.prometheusLabels());

        String name = arguments.optional("NewMetricName").orElse(source.name());
        String value = arguments.optional("NewMetricValue").orElse(source.value());
        Instant createdAt = arguments.optional("SetCreationTimeNow").isPresent()
                ? Instant.now()
                : source.createdAt();
        return new MetricSample(name, value, createdAt, labels);
    }
}
