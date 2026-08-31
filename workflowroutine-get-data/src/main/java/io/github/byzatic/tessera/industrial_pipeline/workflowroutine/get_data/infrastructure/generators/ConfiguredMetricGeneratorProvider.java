package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.application.provider.MetricGeneratorProvider;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain.GeneratedStatus;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pipeline_segment.PipelineSegmentGeneratorInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pump_unit.PumpUnitValuesGeneratorInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.universal_valves.UniversalValvesGeneratorInterface;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.configuration.ConfigurationParameter;

import java.nio.file.Path;
import java.util.Objects;

public final class ConfiguredMetricGeneratorProvider implements MetricGeneratorProvider {
    private static final String CONFIGURATION_FILE_PATH = "configurationFilePath";

    private final Path configurationFile;
    private final GeneratorsFactoryInterface generatorsFactory;

    public ConfiguredMetricGeneratorProvider(
            Path configurationFile,
            GeneratorsFactoryInterface generatorsFactory
    ) {
        this.configurationFile = Objects.requireNonNull(configurationFile, "configurationFile must not be null");
        this.generatorsFactory = Objects.requireNonNull(generatorsFactory, "generatorsFactory must not be null");
    }

    public static ConfiguredMetricGeneratorProvider from(
            MCg3WorkflowRoutineApiInterface routineApi
    ) throws RoutineExecutionException {
        for (ConfigurationParameter parameter : routineApi.getConfigurationParameters()) {
            if (CONFIGURATION_FILE_PATH.equals(parameter.getParameterKey())) {
                return new ConfiguredMetricGeneratorProvider(
                        Path.of(parameter.getParameterValue()),
                        new GeneratorsFactory()
                );
            }
        }
        throw new RoutineExecutionException("Routine parameter configurationFilePath is not set");
    }

    @Override
    public GeneratedStatus generate() throws RoutineExecutionException {
        try {
            GeneratorInterface generator = generatorsFactory.createFromConfigFile(configurationFile);
            GeneratorInterface.ResolveResult result;
            if (generator instanceof UniversalValvesGeneratorInterface valves) {
                result = valves.resolve(valves.generate());
            } else if (generator instanceof PipelineSegmentGeneratorInterface segment) {
                result = segment.resolve(segment.generate());
            } else if (generator instanceof PumpUnitValuesGeneratorInterface pumpUnit) {
                result = pumpUnit.resolve(pumpUnit.generate());
            } else {
                throw new RoutineExecutionException(
                        "Unsupported generator type: " + generator.getClass().getName()
                );
            }
            int severity = switch (result.range()) {
                case OK -> 0;
                case WARN -> 1;
                case CRITICAL -> 2;
            };
            return new GeneratedStatus(severity, result.explain());
        } catch (RoutineExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new RoutineExecutionException(
                    "Failed to generate metric from " + configurationFile,
                    e
            );
        }
    }
}
