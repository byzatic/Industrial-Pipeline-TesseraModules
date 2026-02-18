package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators;

import java.io.IOException;
import java.nio.file.Path;

public interface GeneratorsFactoryInterface {
    GeneratorInterface createFromConfigFile(Path path) throws IOException;
}
