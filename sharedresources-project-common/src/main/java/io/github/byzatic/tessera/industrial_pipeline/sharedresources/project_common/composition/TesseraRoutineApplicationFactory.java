package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.composition;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.dsl.DslCommandHandler;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.dsl.DslScriptExecutor;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.usecase.RunRoutineUseCase;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.provider.GraphPathProvider;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.repository.MetricStorage;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.provider.RoutineScriptSource;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.ConfiguredRoutineService;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.MetricFunctionHandler;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.RoutineCommandService;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.service.RoutineWorkspace;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.dsl.AntlrDslScriptExecutor;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.dsl.GraphPathTemplateProcessor;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.tessera.TesseraGraphPathProvider;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.tessera.TesseraMetricMapper;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.tessera.TesseraMetricStorage;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.tessera.TesseraRoutineScriptSource;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;

public final class TesseraRoutineApplicationFactory {
    private TesseraRoutineApplicationFactory() {
    }

    public static RunRoutineUseCase create(
            MCg3WorkflowRoutineApiInterface routineApi,
            MetricFunctionHandler functionHandler
    ) throws Exception {
        GraphPathProvider graphPathProvider = new TesseraGraphPathProvider(routineApi);
        MetricStorage metricStorage = new TesseraMetricStorage(
                routineApi.getStorageApi(),
                new TesseraMetricMapper()
        );
        RoutineWorkspace workspace = new RoutineWorkspace(metricStorage, graphPathProvider);
        DslCommandHandler commandHandler = new RoutineCommandService(workspace, functionHandler);
        DslScriptExecutor scriptExecutor = new AntlrDslScriptExecutor(
                commandHandler,
                new GraphPathTemplateProcessor(graphPathProvider)
        );
        RoutineScriptSource scriptSource = new TesseraRoutineScriptSource(routineApi);
        return new ConfiguredRoutineService(scriptSource, scriptExecutor, workspace);
    }
}
