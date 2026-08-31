package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model;

import java.util.Objects;

public record MetricLabel(String key, String value, String sign) {

    public MetricLabel {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        sign = sign == null ? "=" : sign;
    }
}
