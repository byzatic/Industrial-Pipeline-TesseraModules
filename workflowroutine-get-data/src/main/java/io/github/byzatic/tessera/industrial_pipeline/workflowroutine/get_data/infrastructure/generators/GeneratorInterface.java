package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators;

public interface GeneratorInterface {
    enum SpecialValueRange {
        OK,
        WARN,
        CRITICAL;
    }

    record ResolveResult(
            SpecialValueRange range,
            String explain
    ) {
    }
}
