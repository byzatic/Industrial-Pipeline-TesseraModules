package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pump_unit;

import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.GeneratorInterface;

public interface PumpUnitValuesGeneratorInterface extends GeneratorInterface {
    Float generate();

    ResolveResult resolve(Float value);
}
