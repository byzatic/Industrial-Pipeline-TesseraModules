package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.graph_lifting_data;

import io.github.byzatic.commons.base_exceptions.OperationIncompleteException;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.configuration.ConfigurationParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupportParamParser {

    private final Map<String, List<ConfigurationParameter>> configurationParametersMap = new HashMap<>();

    public SupportParamParser(MCg3WorkflowRoutineApiInterface wrApi) {
        for (ConfigurationParameter configurationParameter : wrApi.getConfigurationParameters()) {
            String paramKey = configurationParameter.getParameterKey();
            if (this.configurationParametersMap.containsKey(paramKey)) {
                this.configurationParametersMap.get(paramKey).add(configurationParameter);
            } else {
                List<ConfigurationParameter> paramList = new ArrayList<>();
                paramList.add(configurationParameter);
                this.configurationParametersMap.put(paramKey, paramList);
            }
        }
    }

    public List<ConfigurationParameter> getParamsByKey(String key) throws OperationIncompleteException {
        List<ConfigurationParameter> configurationParameterList;
        if (configurationParametersMap.containsKey(key)) {
            configurationParameterList = configurationParametersMap.get(key);
        } else {
            configurationParameterList = new ArrayList<>();
        }
        return configurationParameterList;
    }
}
