package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model;

import java.util.Objects;

public record FunctionArgument(String key, String value) {

    public FunctionArgument {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
    }
}
