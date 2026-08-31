package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.application;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.FunctionArguments;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.MetricFunctionHandler;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.RoutineWorkspace;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricLabel;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.service.MetricLabels;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.application.provider.MetricGeneratorProvider;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain.GeneratedStatus;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GetDataFunctionHandler implements MetricFunctionHandler {
    private static final String REASON_KEY = "reason";

    private final MetricGeneratorProvider generatorProvider;

    public GetDataFunctionHandler(MetricGeneratorProvider generatorProvider) {
        this.generatorProvider = Objects.requireNonNull(generatorProvider, "generatorProvider must not be null");
    }

    @Override
    public MetricSample execute(
            String functionName,
            FunctionArguments arguments,
            String resultId,
            RoutineWorkspace workspace
    ) throws RoutineExecutionException {
        return switch (functionName) {
            case "GenerateData" -> generate(resultId, workspace);
            case "ProcessReason" -> processReason(arguments, workspace);
            default -> throw new RoutineExecutionException("Unknown get-data function: " + functionName);
        };
    }

    private MetricSample generate(
            String resultId,
            RoutineWorkspace workspace
    ) throws RoutineExecutionException {
        GeneratedStatus generated = generatorProvider.generate();
        List<MetricLabel> labels = generated.severity() == 0
                ? List.of()
                : List.of(reason(workspace.graphPath(), generated.explanation()));
        return new MetricSample(
                resultId.toLowerCase(),
                Integer.toString(generated.severity()),
                Instant.now(),
                labels
        );
    }

    private static MetricSample processReason(
            FunctionArguments arguments,
            RoutineWorkspace workspace
    ) throws RoutineExecutionException {
        MetricSample source = workspace.metric(arguments.required("DataId"));
        boolean ignoreExistingReason = arguments.optional("IgnoreExistsReason")
                .map(Boolean::parseBoolean)
                .orElse(false);
        Optional<MetricLabel> existingReason = MetricLabels.find(source.labels(), REASON_KEY);
        if (existingReason.isPresent() && !ignoreExistingReason) {
            return source;
        }

        int status = Integer.parseInt(source.value());
        Optional<String> message = selectReasonMessage(status, arguments);
        if (message.isEmpty()) {
            return source;
        }
        MetricLabel reason = reason(workspace.graphPath(), message.get());
        String value = status == 3 ? "1" : source.value();
        return new MetricSample(
                source.name(),
                value,
                source.createdAt(),
                MetricLabels.upsert(source.labels(), List.of(reason))
        );
    }

    private static Optional<String> selectReasonMessage(
            int status,
            FunctionArguments arguments
    ) {
        if (status == 0) {
            boolean pasteWhenOk = Boolean.parseBoolean(arguments.required("PasteReasonWhenOk"));
            return pasteWhenOk ? arguments.optional("OkReasonMessage") : Optional.empty();
        }
        if (status == 3) {
            return Optional.of(arguments.required("EmptyData"));
        }
        Optional<String> global = arguments.optional("GlobalReasonMessage");
        if (global.isPresent()) {
            return global;
        }
        return switch (status) {
            case 1 -> Optional.of(arguments.required("WarningReasonMessage"));
            case 2 -> Optional.of(arguments.required("AlarmReasonMessage"));
            default -> throw new IllegalArgumentException("Unsupported metric status: " + status);
        };
    }

    private static MetricLabel reason(String graphPath, String message) {
        return new MetricLabel(REASON_KEY, graphPath + " => " + message, "=");
    }
}
