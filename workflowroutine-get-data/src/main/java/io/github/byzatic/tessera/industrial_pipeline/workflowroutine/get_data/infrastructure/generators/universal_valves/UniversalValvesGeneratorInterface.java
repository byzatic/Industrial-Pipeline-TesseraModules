package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.universal_valves;

import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.GeneratorInterface;

public interface UniversalValvesGeneratorInterface extends GeneratorInterface {
    ResolveResult resolve(ValveState state);

    enum ValveState {
        OPEN,        // OK
        CLOSED,      // OK
        PROCESSED,      // WARN
        NOT_STATED   // CRITICAL
    }

    ValveState generate();
}
