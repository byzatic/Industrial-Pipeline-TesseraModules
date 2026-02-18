package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data;
// composition package

import com.github.zafarkhaja.semver.Version;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslBaseListener;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain.service.ProcessEngineInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain.service.ProcessorInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.listener.MyDslCustomListener;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.process.ProcessEngine;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.service.Processor;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.service.SupportParamParser;
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

public class GetDataWorkflowRoutine extends AbstractWorkflowRoutine {
    private final static Logger logger = LoggerFactory.getLogger(GetDataWorkflowRoutine.class);
    private HealthFlagState state = null;
    private ProcessorInterface processor = null;
    private SupportParamParser paramParser;

    public GetDataWorkflowRoutine(MCg3WorkflowRoutineApiInterface workflowRoutineApi, HealthFlagProxy healthFlagProxy) throws MCg3ApiOperationIncompleteException {
        super(
                GetDataWorkflowRoutine.class,
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

    // TODO: default public PrometheusExportService() with develop realisation workflowRoutine API (MCg3WorkflowRoutineApiInterface) and Health Flag Proxy (healthFlagProxy)

    private ProcessEngineInterface getProcessEngine(MCg3WorkflowRoutineApiInterface workflowRoutineApi) throws MCg3ApiOperationIncompleteException {
        try {

            return new ProcessEngine(workflowRoutineApi);
        } catch (MCg3ApiOperationIncompleteException e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
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
