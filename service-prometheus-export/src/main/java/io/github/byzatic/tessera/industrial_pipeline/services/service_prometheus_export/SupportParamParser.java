package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export;

import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.github.byzatic.tessera.service.configuration.ServiceConfigurationParameter;

import java.util.HashMap;
import java.util.Map;

public class SupportParamParser {

    private final Map<String, ServiceConfigurationParameter> serviceConfigurationParametersMap = new HashMap<>();

    public SupportParamParser(MCg3ServiceApiInterface serviceApi) {
        for (ServiceConfigurationParameter serviceConfigurationParameter : serviceApi.getServiceConfigurationParameters()) {
            this.serviceConfigurationParametersMap.put(serviceConfigurationParameter.getParameterKey(), serviceConfigurationParameter);
        }
    }

    public ServiceConfigurationParameter getParamByKey(String key) {
        if (!serviceConfigurationParametersMap.containsKey(key)) throw new IllegalArgumentException("Parameter by key "+ key + " not found.");
        return serviceConfigurationParametersMap.get(key);
    }
}
