package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.usecase;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;

public interface RunRoutineUseCase {
    void run() throws RoutineExecutionException;
}
