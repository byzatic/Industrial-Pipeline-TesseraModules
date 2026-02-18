package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data;
// composition package

import com.google.auto.service.AutoService;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineFactoryInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy;

@AutoService(WorkflowRoutineFactoryInterface.class)
public class GetDataWorkflowRoutineFactory implements WorkflowRoutineFactoryInterface {
    @Override
    public GetDataWorkflowRoutine create(MCg3WorkflowRoutineApiInterface api, HealthFlagProxy healthFlagProxy) {
        try {
            return new GetDataWorkflowRoutine(api, healthFlagProxy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
