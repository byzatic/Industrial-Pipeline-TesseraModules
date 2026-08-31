package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.dsl;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;

public interface DslScriptExecutor {
    void execute(String script) throws RoutineExecutionException;
}
