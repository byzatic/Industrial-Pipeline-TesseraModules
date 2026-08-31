package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.dsl.DslScriptExecutor;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.usecase.RunRoutineUseCase;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.provider.RoutineScriptSource;

import java.util.Objects;

public final class ConfiguredRoutineService implements RunRoutineUseCase {
    private final RoutineScriptSource scriptSource;
    private final DslScriptExecutor scriptExecutor;
    private final RoutineWorkspace workspace;

    public ConfiguredRoutineService(
            RoutineScriptSource scriptSource,
            DslScriptExecutor scriptExecutor,
            RoutineWorkspace workspace
    ) {
        this.scriptSource = Objects.requireNonNull(scriptSource, "scriptSource must not be null");
        this.scriptExecutor = Objects.requireNonNull(scriptExecutor, "scriptExecutor must not be null");
        this.workspace = Objects.requireNonNull(workspace, "workspace must not be null");
    }

    @Override
    public void run() throws RoutineExecutionException {
        try {
            for (String script : scriptSource.loadScripts()) {
                scriptExecutor.execute(script);
            }
        } finally {
            workspace.clear();
        }
    }
}
