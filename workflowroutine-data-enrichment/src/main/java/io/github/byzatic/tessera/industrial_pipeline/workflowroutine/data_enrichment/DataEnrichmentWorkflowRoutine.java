package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment;

import com.github.zafarkhaja.semver.Version;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslBaseListener;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment.processor.Processor;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment.processor.ProcessorInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment.processor.dsl.MyDslCustomListener;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment.processor.process_engine.ProcessEngine;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment.processor.process_engine.ProcessEngineInterface;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.configuration.ConfigurationParameter;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.AbstractWorkflowRoutine;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DataEnrichmentWorkflowRoutine extends AbstractWorkflowRoutine {
    private final static Logger logger = LoggerFactory.getLogger(DataEnrichmentWorkflowRoutine.class);
    private HealthFlagState state = null;
    private final ProcessorInterface processor;
    private SupportParamParser paramParser;

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

        paramParser = new SupportParamParser(workflowRoutineApi);

        ProcessEngineInterface processEngine = new ProcessEngine(workflowRoutineApi);
        MyDslBaseListener dslListener = new MyDslCustomListener(processEngine);
        this.processor = new Processor(dslListener, workflowRoutineApi);

    }


    @Override
    public void run() {
        try (AutoCloseable ignored = super.getWorkflowRoutineApi().getExecutionContext().getMdcContext().use()) {
            super.healthFlagProxy.setHealthFlagState(HealthFlagState.RUNNING);

            List<ConfigurationParameter> configurationParameterListDSL = paramParser.getParamsByKey("MCg3-WorkflowRoutine-DSL");
            List<ConfigurationParameter> configurationParameterListDSLFile = paramParser.getParamsByKey("MCg3-WorkflowRoutine-DSL-File");


            for (ConfigurationParameter configurationParameter : configurationParameterListDSL) {
                String dslString = configurationParameter.getParameterValue();
                processor.process(dslString);
            }

            for (ConfigurationParameter configurationParameter : configurationParameterListDSLFile) {
                String dslString = Files.readString(Paths.get(configurationParameter.getParameterValue()), StandardCharsets.UTF_8);
                processor.process(dslString);
            }

            super.healthFlagProxy.setHealthFlagState(HealthFlagState.COMPLETE);

        } catch (Throwable t) {
            logger.error("Service critical error", t);
            super.healthFlagProxy.setHealthFlagState(HealthFlagState.FATAL);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void terminate() {
        state = HealthFlagState.COMPLETE;
    }
}
