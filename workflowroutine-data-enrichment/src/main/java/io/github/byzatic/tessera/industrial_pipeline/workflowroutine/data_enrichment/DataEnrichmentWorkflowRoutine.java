package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment;

import com.github.zafarkhaja.semver.Version;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.usecase.RunRoutineUseCase;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.composition.TesseraRoutineApplicationFactory;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment.application.DataEnrichmentFunctionHandler;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.AbstractWorkflowRoutine;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataEnrichmentWorkflowRoutine extends AbstractWorkflowRoutine {
    private final static Logger logger = LoggerFactory.getLogger(DataEnrichmentWorkflowRoutine.class);
    private final RunRoutineUseCase runRoutine;

    public DataEnrichmentWorkflowRoutine(MCg3WorkflowRoutineApiInterface workflowRoutineApi, HealthFlagProxy healthFlagProxy) throws MCg3ApiOperationIncompleteException {
        super(
                DataEnrichmentWorkflowRoutine.class,
                workflowRoutineApi,
                healthFlagProxy,                                   // workflowRoutine state proxy
                Version.of(0, 0, 0),            // workflowRoutine Version
                Version.of(1, 0, 0),            // workflowRoutine Requires MCg3 Version
                "My example private String WorkflowRoutine",       // workflowRoutine Description
                "My Name",                                         // workflowRoutine Provider
                "Apache License 2.0",                              // workflowRoutine License
                3L                                                 // termination Interval Minutes (3 min)
        );
        healthFlagProxy.setHealthFlagState(HealthFlagState.RUNNING);

        try {
            this.runRoutine = TesseraRoutineApplicationFactory.create(
                    workflowRoutineApi,
                    new DataEnrichmentFunctionHandler()
            );
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }


    @Override
    public void run() {
        try (AutoCloseable ignored = super.getWorkflowRoutineApi().getExecutionContext().getMdcContext().use()) {
            super.healthFlagProxy.setHealthFlagState(HealthFlagState.RUNNING);

            runRoutine.run();

            super.healthFlagProxy.setHealthFlagState(HealthFlagState.COMPLETE);

        } catch (Throwable t) {
            logger.error("Service critical error", t);
            super.healthFlagProxy.setHealthFlagState(HealthFlagState.FATAL);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void terminate() {
        super.healthFlagProxy.setHealthFlagState(HealthFlagState.COMPLETE);
    }
}
