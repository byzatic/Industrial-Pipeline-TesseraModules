package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.graph_lifting_data.processor.process_engine;

import io.github.byzatic.commons.ObjectsUtils;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.storage.LocalKeyValueStorage;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.storage.LocalKeyValueStorageInterface;
import io.github.byzatic.tessera.storageapi.dto.DataValueInterface;
import io.github.byzatic.tessera.storageapi.dto.StorageItem;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.storageapi.storageapi.StorageApiInterface;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProcessEngine implements ProcessEngineInterface {
    private final static Logger logger = LoggerFactory.getLogger(ProcessEngine.class);
    private final StorageApiInterface workflowRoutineStorageApi;
    private final LocalKeyValueStorageInterface<String, DataValueInterface> localStore = new LocalKeyValueStorage<>();

    public ProcessEngine(MCg3WorkflowRoutineApiInterface workflowRoutineApi) throws MCg3ApiOperationIncompleteException {
        try {
            this.workflowRoutineStorageApi = workflowRoutineApi.getStorageApi();
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    @Override
    public void processData(String functionName, List<String> args, String resultId) throws MCg3ApiOperationIncompleteException {
        try {
            if (functionName.equals("LiftData")) {
                logger.debug("Process LiftData");

                preprocessorLiftData(resultId, args);

                logger.debug("Process LiftData complete");
            } else {
                throw new MCg3ApiOperationIncompleteException("No such function " + functionName);
            }

        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    @Override
    public void getData(String childName, String storageId, Boolean isGlobal, String dataId, String alias) throws MCg3ApiOperationIncompleteException {
        try {
            StorageItem.ScopeType scopeType = StorageItem.ScopeType.LOCAL;
            if (childName != null) {
                scopeType = StorageItem.ScopeType.DOWNSTREAM;
            } else {
                if (isGlobal) scopeType = StorageItem.ScopeType.GLOBAL;

            }
            StorageItem requestedStorageItem = workflowRoutineStorageApi.getStorageObject(
                    StorageItem.newBuilder()
                            .setScope(scopeType)
                            .setDownstreamName(childName)
                            .setStorageId(storageId)
                            .setDataId(dataId)
                            .setDataValue(null)
                            .build()
            );
            localStore.put(alias, requestedStorageItem.getDataValue());
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    @Override
    public void putData(String localDataId, String storageId, Boolean isGlobal, String dataId) throws MCg3ApiOperationIncompleteException {
        try {
            logger.debug("putData localDataId -> {} storageId -> {} isGlobal -> {} dataId -> {}", localDataId, storageId, isGlobal, dataId);
            StorageItem.ScopeType scopeType = StorageItem.ScopeType.LOCAL;
            if (isGlobal) scopeType = StorageItem.ScopeType.GLOBAL;
            StorageItem storageItem = StorageItem.newBuilder()
                    .setScope(scopeType)
                    .setDownstreamName(null)
                    .setStorageId(storageId)
                    .setDataId(dataId)
                    .setDataValue(localStore.get(localDataId))
                    .build();
            workflowRoutineStorageApi.putStorageObject(
                    storageItem
            );
            logger.debug("putData localDataId -> {} storageId -> {} isGlobal -> {} dataId -> {} is complete", localDataId, storageId, isGlobal, dataId);
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    private Argument getArgument(String argName, List<String> args, Boolean ignoreNull) {
        Argument result = SupportArgsPreProcessor.searchArg(args, argName);
        if (!ignoreNull)
            ObjectsUtils.requireNonNull(result, new IllegalArgumentException("Argument " + argName + " not found!"));
        return result;
    }

    private void preprocessorLiftData(String resultId, List<String> args) throws MCg3ApiOperationIncompleteException {

        Argument argNodeName = getArgument("NodeName", args, Boolean.TRUE);
        Argument argNodeId = getArgument("NodeId", args, Boolean.TRUE);
        Argument argNodeStorage = getArgument("NodeStorage", args, Boolean.FALSE);
        Argument argDataId = getArgument("DataId", args, Boolean.FALSE);

        String downstreamName;

        if (argNodeId != null) {
            downstreamName = argNodeId.getValue();
        } else if (argNodeName != null) {
            // TODO: Searching Node by Argument NodeName
            // downstreamName = ;
            throw new NotImplementedException("Searching Node by Argument NodeName is not realised; Please use Argument NodeId");
        } else {
            throw new MCg3ApiOperationIncompleteException("Argument NodeName or NodeId must be set");
        }

        StorageItem storageItemRequest = StorageItem.newBuilder()
                .setStorageId(argNodeStorage.getValue())
                .setDataId(argDataId.getValue())
                .setDataValue(null)
                .setScope(StorageItem.ScopeType.DOWNSTREAM)
                .setDownstreamName(downstreamName)
                .build();

        StorageItem storageItem = workflowRoutineStorageApi.getStorageObject(storageItemRequest);

        localStore.put(resultId, (DataItem) storageItem.getDataValue());
    }
}
