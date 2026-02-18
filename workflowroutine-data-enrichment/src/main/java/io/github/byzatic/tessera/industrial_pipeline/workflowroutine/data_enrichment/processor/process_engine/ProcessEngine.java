package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.data_enrichment.processor.process_engine;

import io.github.byzatic.commons.ObjectsUtils;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.MetricLabel;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.storage.LocalKeyValueStorage;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.storage.LocalKeyValueStorageInterface;
import io.github.byzatic.tessera.storageapi.dto.DataValueInterface;
import io.github.byzatic.tessera.storageapi.dto.StorageItem;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.storageapi.storageapi.StorageApiInterface;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.execution_context.ExecutionContextInterface;
import io.github.byzatic.tessera.workflowroutine.execution_context.GraphPathInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProcessEngine implements ProcessEngineInterface {
    private final static Logger logger = LoggerFactory.getLogger(ProcessEngine.class);
    private final StorageApiInterface workflowRoutineStorageApi;
    private final LocalKeyValueStorageInterface<String, DataValueInterface> localStore = new LocalKeyValueStorage<>();
    private final ExecutionContextInterface workflowRoutineExecutionContext;

    public ProcessEngine(MCg3WorkflowRoutineApiInterface workflowRoutineApi) throws MCg3ApiOperationIncompleteException {
        try {
            this.workflowRoutineStorageApi = workflowRoutineApi.getStorageApi();
            this.workflowRoutineExecutionContext = workflowRoutineApi.getExecutionContext();
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    @Override
    public void processData(String functionName, List<String> args, String resultId) throws MCg3ApiOperationIncompleteException {
        try {
            switch (functionName) {
                case "AddGraphPath" -> preprocessorAddGraphPath(resultId, args);
                case "AddLabel" -> preprocessorAddLabel(resultId, args);
                case "ModifyMetric" -> preprocessorModifyMetric(resultId, args);
                default -> throw new MCg3ApiOperationIncompleteException("No such function " + functionName);
            }
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    private void preprocessorModifyMetric(String resultId, List<String> args) {
        Argument argDataId = getArg(args, "DataId", Boolean.FALSE);
        Argument argNewMetricName = getArg(args, "NewMetricName", Boolean.TRUE);
        List<Argument> argsRemoveLabelByName = getArgs(args, "RemoveLabelByName", Boolean.TRUE);
        Argument argRemoveAllLabels = getArg(args, "RemoveAllLabels", Boolean.TRUE);
        Argument argNewMetricValue = getArg(args, "NewMetricValue", Boolean.TRUE);
        Argument argSetCreationTimeNow = getArg(args, "SetCreationTimeNow", Boolean.TRUE);

        List<MetricLabel> newMetricLabels = extractLabelsFromArgs(args);
        DataItem storageDataItem = (DataItem) localStore.get(argDataId.getValue());
        List<MetricLabel> metricLabelList = storageDataItem.getMetricLabels();
        List<MetricLabel> updatedMetricLabelList = storageDataItem.getMetricLabels();


        DataItem.Builder newDataItemBuilder = DataItem.newBuilder(storageDataItem);

        if (argRemoveAllLabels != null) {
            newDataItemBuilder.setMetricLabels(new ArrayList<>());
        } else if (!argsRemoveLabelByName.isEmpty()) {
            List<MetricLabel> finalMetricLabelList = new ArrayList<>(metricLabelList);
            for (MetricLabel metricLabel : metricLabelList) {
                boolean remove = Boolean.FALSE;
                for (Argument argumentRemoveLabelByName : argsRemoveLabelByName) {
                    if (metricLabel.getKey().equals(argumentRemoveLabelByName.getKey())) {
                        remove = Boolean.TRUE;
                        break;
                    }
                }
                if (remove) {
                    finalMetricLabelList.remove(metricLabel);
                }
                newDataItemBuilder.setMetricLabels(finalMetricLabelList);
                updatedMetricLabelList = finalMetricLabelList;
            }
        }

        if (argNewMetricName != null) {
            newDataItemBuilder.setMetricName(argNewMetricName.getValue());
        }

        if (argNewMetricValue != null) {
            newDataItemBuilder.setMetricValue(argNewMetricValue.getValue());
        }

        if (argSetCreationTimeNow != null) {
            newDataItemBuilder.setMetricCreationTime(Instant.now());
        }

        if (!metricLabelList.isEmpty()) {
            List<MetricLabel> finalMetricLabelList = mergeReplaceMetricLabelLists(newMetricLabels, updatedMetricLabelList);
            newDataItemBuilder.setMetricLabels(finalMetricLabelList);
        }

        saveData(resultId, newDataItemBuilder.build());
    }

    private void preprocessorAddLabel(String resultId, List<String> args) {
        Argument argDataId = getArg(args, "DataId", Boolean.FALSE);
        List<MetricLabel> metricNewLabelList = extractLabelsFromArgs(args);
        DataItem storageDataItem = (DataItem) localStore.get(argDataId.getValue());
        List<MetricLabel> metricLabelList = storageDataItem.getMetricLabels();

        List<MetricLabel> finalMetricLabelList = mergeReplaceMetricLabelLists(metricNewLabelList, metricLabelList);

        DataItem dataItem = DataItem.newBuilder(storageDataItem)
                .setMetricLabels(finalMetricLabelList)
                .build();

        saveData(resultId, dataItem);
    }

    private void preprocessorAddGraphPath(String resultId, List<String> args) {
        Argument argDataId = getArg(args, "DataId", Boolean.FALSE);
        DataItem storageDataItem = (DataItem) localStore.get(argDataId.getValue());

        List<MetricLabel> metricLabelList = storageDataItem.getMetricLabels();
        try {
            GraphPathInterface graphPath = workflowRoutineExecutionContext.getPipelineExecutionInfo().getCurrentNodeExecutionGraphPath();
            metricLabelList.add(
                    MetricLabel.newBuilder()
                            .setKey("graph_path")
                            .setSign("=")
                            .setValue(graphPath.getGraphPath())
                            .build()
            );
        } catch (MCg3ApiOperationIncompleteException e) {
            throw new RuntimeException(e);
        }

        DataItem dataItem = DataItem.newBuilder(storageDataItem)
                .setMetricLabels(metricLabelList)
                .build();

        saveData(resultId, dataItem);
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


    private Argument getArg(List<String> args, String argName, Boolean isNullable) {
        Argument arg = SupportArgsPreProcessor.searchArg(args, argName);
        if (!isNullable)
            ObjectsUtils.requireNonNull(arg, new IllegalArgumentException("Argument " + argName + " not found!"));
        return arg;
    }

    private List<Argument> getArgs(List<String> args, String argName, Boolean isNullable) {
        List<Argument> result = SupportArgsPreProcessor.searchArgs(args, argName);
        if (!isNullable)
            ObjectsUtils.requireNonNull(result, new IllegalArgumentException("Arguments " + argName + " not found!"));
        return result;
    }

    private void saveData(String id, DataValueInterface data) {
        logger.debug("Save data to local module storage -> id: {} data: {}", id, data);
        if (localStore.containsKey(id)) {
            logger.debug("Local storage contains data with id {}", id);
            localStore.delete(id);
            logger.debug("Data with id {} removed from local storage", id);
        }
        localStore.put(id, data);
        logger.debug("Data with id {} saved to local storage", id);
    }

    private List<MetricLabel> mergeReplaceMetricLabelLists(List<MetricLabel> newLabels, List<MetricLabel> oldLabels) {
        // IF Label keys not equals add old key to array of new key
        List<MetricLabel> finalMetricLabelList = new ArrayList<>(newLabels);
        for (MetricLabel metricNewLabel : newLabels) {
            for (MetricLabel metricLabel : oldLabels) {
                if (!metricLabel.getKey().equals(metricNewLabel.getKey()))
                    finalMetricLabelList.add(MetricLabel.newBuilder(metricLabel).build());
            }
        }
        return finalMetricLabelList;
    }

    private List<MetricLabel> extractLabelsFromArgs(List<String> args) {
        List<MetricLabel> metricLabelList = new ArrayList<>();

        for (String arg : args) {
            if (arg == null || arg.isEmpty()) {
                continue;
            }

            String[] parts = arg.split("=", 2);
            if (parts.length != 2) {
                // можно выбросить исключение или залогировать предупреждение
                logger.debug("Invalid argument: " + arg);
                continue;
            }

            String key = parts[0].trim();
            String value = parts[1].trim();

            if (key.startsWith("PromLabel_")) {
                String labelKey = key.substring("PromLabel_".length());
                if (!labelKey.isEmpty()) {
                    metricLabelList.add(MetricLabel.newBuilder()
                            .setKey(labelKey)
                            .setSign("=")
                            .setValue(value)
                            .build()
                    );
                } else {
                    logger.debug("Empty label key in argument: " + arg);
                }
            }
        }
        return metricLabelList;
    }

    private MetricLabel findReasonLabel(List<MetricLabel> labels) {
        MetricLabel result = null;
        Optional<MetricLabel> optionalMetricLabel = labels.stream()
                .filter(label -> "reason".equals(label.getKey()))
                .findFirst();
        if (optionalMetricLabel.isPresent()) {
            result = optionalMetricLabel.get();
        }
        return result;
    }
}
