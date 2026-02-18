package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain.service;

import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;

import java.util.List;

public interface ProcessEngineInterface {

    void processData(String functionName, List<String> args, String resultId) throws MCg3ApiOperationIncompleteException;

    void getData(String childName, String storageId, Boolean isGlobal, String dataId, String alias) throws MCg3ApiOperationIncompleteException;

    void putData(String localDataId, String storageId, Boolean isGlobal, String dataId) throws MCg3ApiOperationIncompleteException;
}
