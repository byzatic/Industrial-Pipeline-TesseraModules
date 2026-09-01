package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.dsl;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;

import java.util.List;

public interface DslCommandHandler {
    void getData(String childName, String storageId, boolean global, String dataId, String alias)
            throws RoutineExecutionException;

    void processData(String functionName, List<String> arguments, String resultId)
            throws RoutineExecutionException;

    void putData(String localDataId, String storageId, boolean global, String dataId)
            throws RoutineExecutionException;
}
