package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.tessera;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.provider.RoutineScriptSource;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.configuration.ConfigurationParameter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TesseraRoutineScriptSource implements RoutineScriptSource {
    public static final String INLINE_DSL_KEY = "MCg3-WorkflowRoutine-DSL";
    public static final String DSL_FILE_KEY = "MCg3-WorkflowRoutine-DSL-File";

    private final MCg3WorkflowRoutineApiInterface routineApi;

    public TesseraRoutineScriptSource(MCg3WorkflowRoutineApiInterface routineApi) {
        this.routineApi = Objects.requireNonNull(routineApi, "routineApi must not be null");
    }

    @Override
    public List<String> loadScripts() throws RoutineExecutionException {
        List<String> scripts = new ArrayList<>();
        try {
            for (ConfigurationParameter parameter : routineApi.getConfigurationParameters()) {
                if (INLINE_DSL_KEY.equals(parameter.getParameterKey())) {
                    scripts.add(parameter.getParameterValue());
                } else if (DSL_FILE_KEY.equals(parameter.getParameterKey())) {
                    scripts.add(Files.readString(
                            Path.of(parameter.getParameterValue()),
                            StandardCharsets.UTF_8
                    ));
                }
            }
            return List.copyOf(scripts);
        } catch (Exception e) {
            throw new RoutineExecutionException("Failed to load configured DSL scripts", e);
        }
    }
}
