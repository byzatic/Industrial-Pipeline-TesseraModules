package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export;

import com.github.zafarkhaja.semver.Version;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.domain_logic.DomainLogic;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.domain_logic.DomainLogicInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.github.byzatic.tessera.service.service.AbstractService;
import io.github.byzatic.tessera.service.service.health.HealthFlagProxy;
import io.github.byzatic.tessera.service.service.health.HealthFlagState;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.MetricsUpdateManager;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.MetricsUpdateManagerInterface;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.PrometheusExporter;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.PrometheusExporterInterface;

import java.net.URL;

public class PrometheusExportService extends AbstractService {
    private final static Logger logger = LoggerFactory.getLogger(PrometheusExportService.class);
    private final DomainLogicInterface domainLogic;

    public PrometheusExportService(MCg3ServiceApiInterface serviceApi, HealthFlagProxy healthFlagProxy) throws MCg3ApiOperationIncompleteException {
        super(
                PrometheusExportService.class,
                serviceApi,
                healthFlagProxy,                           // service state proxy
                Version.of(1, 0, 0),    // service Version
                Version.of(1, 0, 0),    // service Requires MCg3 Version
                "My example private String service",       // service Description
                "My Name",                                 // service Provider
                "Apache License 2.0",                      // service License
                1L                                         // termination Interval Minutes (1 min)
        );
        healthFlagProxy.setHealthFlagState(HealthFlagState.RUNNING);
        try {
            SupportParamParser paramParser = new SupportParamParser(serviceApi);

            String storageName = paramParser.getParamByKey("storage").getParameterValue();
            Long expiredMinutesAgo = Long.valueOf(paramParser.getParamByKey("expiredMinutesAgo").getParameterValue());
            MetricsUpdateManagerInterface metricsUpdateManager = new MetricsUpdateManager(
                    serviceApi.getStorageApi(),
                    storageName,
                    expiredMinutesAgo

            );

            URL url = new URL(paramParser.getParamByKey("apiURL").getParameterValue());

            String httpServerAddress = url.getHost();
            Integer httpServerPort = url.getPort();
            String cronMetricUpdateString = paramParser.getParamByKey("cronMetricUpdateString").getParameterValue();
            PrometheusExporterInterface prometheusExporter = new PrometheusExporter(
                    metricsUpdateManager,
                    httpServerAddress,
                    httpServerPort,
                    cronMetricUpdateString,
                    serviceApi
            );

            this.domainLogic = new DomainLogic(prometheusExporter);
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }

    }

    // TODO: default public PrometheusExportService() with develop realisation service API (MCg3ServiceApiInterface) and Health Flag Proxy (healthFlagProxy)


    @Override
    public void run() {
        try (AutoCloseable ignored = super.getServiceApi().getExecutionContext().getMdcContext().use()) {
            super.healthFlagProxy.setHealthFlagState(HealthFlagState.RUNNING);
            logger.debug("healthFlagProxy RUNNING -> {}", super.healthFlagProxy);

            this.domainLogic.process();

            super.healthFlagProxy.setHealthFlagState(HealthFlagState.STOPPED);
            logger.debug("healthFlagProxy STOPPED -> {}", super.healthFlagProxy);

        } catch (Exception t) {
            logger.error("Service critical error", t);
            super.healthFlagProxy.setHealthFlagState(HealthFlagState.FATAL);
            logger.debug("healthFlagProxy FATAL -> {}", super.healthFlagProxy);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void terminate() {
        try {
            this.domainLogic.terminate();
        } catch (MCg3ApiOperationIncompleteException e) {
            super.healthFlagProxy.setHealthFlagState(HealthFlagState.FATAL);
            throw new RuntimeException(e);
        }
    }

}
