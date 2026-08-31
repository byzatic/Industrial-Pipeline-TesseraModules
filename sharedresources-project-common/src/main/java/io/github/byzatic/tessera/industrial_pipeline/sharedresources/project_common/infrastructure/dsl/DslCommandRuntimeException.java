package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.dsl;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;

final class DslCommandRuntimeException extends RuntimeException {
    DslCommandRuntimeException(RoutineExecutionException cause) {
        super(cause);
    }

    @Override
    public synchronized RoutineExecutionException getCause() {
        return (RoutineExecutionException) super.getCause();
    }
}
