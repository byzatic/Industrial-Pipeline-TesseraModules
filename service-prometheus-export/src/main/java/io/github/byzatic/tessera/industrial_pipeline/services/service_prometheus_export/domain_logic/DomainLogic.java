package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.domain_logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.PrometheusExporterInterface;

public class DomainLogic implements DomainLogicInterface {
    private final static Logger logger = LoggerFactory.getLogger(DomainLogic.class);
    private final PrometheusExporterInterface prometheusExporter;

    public DomainLogic(PrometheusExporterInterface prometheusExporter) {
        this.prometheusExporter = prometheusExporter;
    }

    @Override
    public void process() throws MCg3ApiOperationIncompleteException {
        try {
            this.prometheusExporter.run();
        } catch (Exception e) {
            logger.error("Domain logic error", e);
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    @Override
    public void terminate() throws MCg3ApiOperationIncompleteException {
        try {
            this.prometheusExporter.terminate();
        } catch (Exception e) {
            logger.error("Domain logic error", e);
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }
}
