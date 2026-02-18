package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment.processor;

import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;

public interface ProcessorInterface {
    void process(String commandLineInput) throws MCg3ApiOperationIncompleteException;
}
