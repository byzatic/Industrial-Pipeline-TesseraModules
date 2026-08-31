package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.FunctionArguments;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.StorageLocation;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.dsl.DslCommandHandler;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;

import java.util.List;
import java.util.Objects;

public final class RoutineCommandService implements DslCommandHandler {
    private final RoutineWorkspace workspace;
    private final MetricFunctionHandler functionHandler;

    public RoutineCommandService(RoutineWorkspace workspace, MetricFunctionHandler functionHandler) {
        this.workspace = Objects.requireNonNull(workspace, "workspace must not be null");
        this.functionHandler = Objects.requireNonNull(functionHandler, "functionHandler must not be null");
    }

    @Override
    public void getData(String childName, String storageId, boolean global, String dataId, String alias)
            throws RoutineExecutionException {
        StorageLocation location = childName != null
                ? StorageLocation.downstream(childName, storageId, dataId)
                : global
                ? StorageLocation.global(storageId, dataId)
                : StorageLocation.local(storageId, dataId);
        workspace.save(alias, workspace.load(location));
    }

    @Override
    public void processData(String functionName, List<String> arguments, String resultId)
            throws RoutineExecutionException {
        try {
            MetricSample result = functionHandler.execute(
                    functionName,
                    FunctionArguments.parse(arguments),
                    resultId,
                    workspace
            );
            workspace.save(resultId, result);
        } catch (IllegalArgumentException e) {
            throw new RoutineExecutionException(
                    "Invalid arguments for function " + functionName + ": " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public void putData(String localDataId, String storageId, boolean global, String dataId)
            throws RoutineExecutionException {
        StorageLocation location = global
                ? StorageLocation.global(storageId, dataId)
                : StorageLocation.local(storageId, dataId);
        workspace.store(location, workspace.metric(localDataId));
    }
}
