package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.tessera;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.provider.GraphPathProvider;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;

import java.util.Objects;

public final class TesseraGraphPathProvider implements GraphPathProvider {
    private final MCg3WorkflowRoutineApiInterface routineApi;

    public TesseraGraphPathProvider(MCg3WorkflowRoutineApiInterface routineApi) {
        this.routineApi = Objects.requireNonNull(routineApi, "routineApi must not be null");
    }

    @Override
    public String currentGraphPath() throws RoutineExecutionException {
        try {
            String graphPath = routineApi.getExecutionContext()
                    .getPipelineExecutionInfo()
                    .getCurrentNodeExecutionGraphPath()
                    .getGraphPath();
            if (graphPath == null) {
                throw new RoutineExecutionException("Current graph path must not be null");
            }
            return graphPath;
        } catch (RoutineExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new RoutineExecutionException("Failed to resolve current graph path", e);
        }
    }
}
