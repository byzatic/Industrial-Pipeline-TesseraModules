package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception;

public class RoutineExecutionException extends Exception {

    public RoutineExecutionException(String message) {
        super(message);
    }

    public RoutineExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoutineExecutionException(Throwable cause) {
        super(cause);
    }
}
