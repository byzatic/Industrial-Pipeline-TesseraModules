package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pipeline_segment;

import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.GeneratorInterface;

public interface PipelineSegmentGeneratorInterface extends GeneratorInterface {
    Float generate();

    ResolveResult resolve(Float value);
}
