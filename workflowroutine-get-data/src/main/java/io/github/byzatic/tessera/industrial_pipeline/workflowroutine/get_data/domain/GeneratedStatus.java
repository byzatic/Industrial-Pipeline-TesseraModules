package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain;

public record GeneratedStatus(int severity, String explanation) {
    public GeneratedStatus {
        if (severity < 0 || severity > 2) {
            throw new IllegalArgumentException("severity must be between 0 and 2");
        }
    }
}
