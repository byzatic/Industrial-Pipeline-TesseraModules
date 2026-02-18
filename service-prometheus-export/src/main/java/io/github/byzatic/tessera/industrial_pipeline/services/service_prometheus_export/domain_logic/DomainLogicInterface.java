package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.domain_logic;

import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;

public interface DomainLogicInterface {
    void process() throws MCg3ApiOperationIncompleteException;

    void terminate() throws MCg3ApiOperationIncompleteException;
}
