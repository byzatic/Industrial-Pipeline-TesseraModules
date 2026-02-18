package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export;

import com.google.auto.service.AutoService;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.github.byzatic.tessera.service.service.ServiceFactoryInterface;
import io.github.byzatic.tessera.service.service.health.HealthFlagProxy;

@AutoService(ServiceFactoryInterface.class)
public class PrometheusExportServiceFactory implements ServiceFactoryInterface {
    @Override
    public PrometheusExportService create(MCg3ServiceApiInterface api, HealthFlagProxy healthFlagProxy) {
        try {
            return new PrometheusExportService(api, healthFlagProxy);
        } catch (MCg3ApiOperationIncompleteException e) {
            throw new RuntimeException(e);
        }
    }
}
