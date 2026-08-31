package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.provider;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;

public interface GraphPathProvider {
    String currentGraphPath() throws RoutineExecutionException;
}
