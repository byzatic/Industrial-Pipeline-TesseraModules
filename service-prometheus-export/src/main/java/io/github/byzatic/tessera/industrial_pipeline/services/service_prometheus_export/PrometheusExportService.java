package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export;

import com.github.zafarkhaja.semver.Version;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.usecase.SynchronizeMetricsUseCase;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.service.MetricPublisher;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.repository.MetricSource;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.service.MetricsSynchronizationService;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.infrastructure.prometheus.PrometheusMetricPublisher;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.infrastructure.runtime.ScheduledPrometheusServer;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.infrastructure.tessera.PrometheusServiceSettings;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.infrastructure.tessera.TesseraGlobalMetricSource;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.infrastructure.tessera.TesseraServiceSettingsLoader;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.tessera.TesseraMetricMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.github.byzatic.tessera.service.service.AbstractService;
import io.github.byzatic.tessera.service.service.health.HealthFlagProxy;
import io.github.byzatic.tessera.service.service.health.HealthFlagState;
import java.time.Clock;

public class PrometheusExportService extends AbstractService {
    private final static Logger logger = LoggerFactory.getLogger(PrometheusExportService.class);
    private final ScheduledPrometheusServer runtime;

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
            PrometheusServiceSettings settings = new TesseraServiceSettingsLoader().load(serviceApi);
            MetricSource metricSource = new TesseraGlobalMetricSource(
                    serviceApi.getStorageApi(),
                    settings.storageName(),
                    new TesseraMetricMapper()
            );
            MetricPublisher metricPublisher = new PrometheusMetricPublisher();
            SynchronizeMetricsUseCase synchronizeMetrics = new MetricsSynchronizationService(
                    metricSource,
                    metricPublisher,
                    settings.retention(),
                    Clock.systemUTC()
            );
            this.runtime = new ScheduledPrometheusServer(
                    synchronizeMetrics,
                    settings.serverAddress(),
                    settings.serverPort(),
                    settings.updateCron(),
                    serviceApi
            );
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }

    }

    @Override
    public void run() {
        try (AutoCloseable ignored = super.getServiceApi().getExecutionContext().getMdcContext().use()) {
            super.healthFlagProxy.setHealthFlagState(HealthFlagState.RUNNING);
            logger.debug("healthFlagProxy RUNNING -> {}", super.healthFlagProxy);

            runtime.run();

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
        runtime.terminate();
    }

}
