package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FunctionArguments {
    private static final String PROMETHEUS_LABEL_PREFIX = "PromLabel_";

    private final List<FunctionArgument> values;

    private FunctionArguments(List<FunctionArgument> values) {
        this.values = List.copyOf(values);
    }

    public static FunctionArguments parse(List<String> rawArguments) {
        List<FunctionArgument> parsed = new ArrayList<>();
        for (String rawArgument : rawArguments) {
            if (rawArgument == null || rawArgument.isBlank()) {
                continue;
            }
            int separator = rawArgument.indexOf('=');
            if (separator < 0) {
                throw new IllegalArgumentException("Argument must contain '=': " + rawArgument);
            }
            parsed.add(new FunctionArgument(
                    rawArgument.substring(0, separator).trim(),
                    rawArgument.substring(separator + 1).trim()
            ));
        }
        return new FunctionArguments(parsed);
    }

    public String required(String key) {
        return optional(key).orElseThrow(
                () -> new IllegalArgumentException("Required argument not found: " + key)
        );
    }

    public Optional<String> optional(String key) {
        return values.stream()
                .filter(argument -> argument.key().equals(key))
                .map(FunctionArgument::value)
                .findFirst();
    }

    public List<String> all(String key) {
        return values.stream()
                .filter(argument -> argument.key().equals(key))
                .map(FunctionArgument::value)
                .toList();
    }

    public List<MetricLabel> prometheusLabels() {
        return values.stream()
                .filter(argument -> argument.key().startsWith(PROMETHEUS_LABEL_PREFIX))
                .map(argument -> new MetricLabel(
                        argument.key().substring(PROMETHEUS_LABEL_PREFIX.length()),
                        argument.value(),
                        "="
                ))
                .filter(label -> !label.key().isEmpty())
                .toList();
    }
}
