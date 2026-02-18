package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.processing_status.processor.process_engine;

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
    private final GraphPathInterface graphPath;

    public ProcessEngine(MCg3WorkflowRoutineApiInterface workflowRoutineApi) throws MCg3ApiOperationIncompleteException {
        try {
            this.workflowRoutineStorageApi = workflowRoutineApi.getStorageApi();
            this.workflowRoutineExecutionContext = workflowRoutineApi.getExecutionContext();
            this.graphPath = workflowRoutineApi.getExecutionContext().getPipelineExecutionInfo().getCurrentNodeExecutionGraphPath();
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    @Override
    public void processData(String functionName, List<String> args, String resultId) throws MCg3ApiOperationIncompleteException {
        try {
            if (functionName.equals("ProcessStatus")) {
                logger.debug("Process ProcessStatus");

                preprocessorProcessStatus(resultId, args);

                logger.debug("Process ProcessStatus complete");
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

    private void preprocessorProcessStatus(String resultId, List<String> args) throws MCg3ApiOperationIncompleteException {
        Argument argMetricName = getArgument("MetricName", args, Boolean.FALSE);
        List<Argument> argDataIdList = getArguments("DataId", args, Boolean.FALSE);
        logger.debug("args= {}", args);
        logger.debug("argDataIdList.size()= {}", argDataIdList.size());
        List<MetricLabel> metricLabelList = extractLabels(args);

        List<DataItem> dataItemList = new ArrayList<>();
        for (Argument argument : argDataIdList) {
            logger.debug("Get data for argument {}", argument);
            DataItem dataItem = (DataItem) localStore.get(argument.getValue());
            logger.debug("DataItem is {}", dataItem);
            dataItemList.add(dataItem);
        }

        int maxMetricValue = 0;
        List<MetricLabel> reasonMetricLabels = new ArrayList<>();
        for (DataItem dataItem : dataItemList) {

            int metricValue = Integer.parseInt(dataItem.getMetricValue());

            if (metricValue > 0) {
                reasonMetricLabels.add(findReasonLabel(dataItem.getMetricLabels()));
            }

            if (metricValue >= maxMetricValue) {
                maxMetricValue = metricValue;
            }
        }

        MetricLabel newReasonMetricLabel = getReasonMetricLabel(reasonMetricLabels);

        metricLabelList.add(newReasonMetricLabel);

        localStore.put(resultId, DataItem.newBuilder()
                .setMetricCreationTime(Instant.now())
                .setMetricLabels(metricLabelList)
                .setMetricName(argMetricName.getValue())
                .setMetricValue(String.valueOf(maxMetricValue))
                .build()
        );
    }

    private MetricLabel getReasonMetricLabel(List<MetricLabel> reasonMetricLabels) {
        logger.debug("Process reason for list reason of size {}", reasonMetricLabels.size());
        Reason reason = new Reason();
        for (MetricLabel reasonMetricLabel : reasonMetricLabels) {
            logger.debug("Process reason for reason MetricLabel - {}", reasonMetricLabel);
            if (reasonMetricLabel == null) {
                logger.debug("Reason MetricLabel is null; Set - Can't find reason");
                reason.add(graphPath.getGraphPath() + " => " + "[Can't find reason]");
            } else {
                logger.debug("Reason MetricLabel is not null");
                if (endsWithArrowNull(reasonMetricLabel.getValue())) {
                    logger.debug("reason MetricLabel is ends with Null - setEmpty");
                    reason.setEmpty();
                } else {
                    logger.debug("Reason MetricLabel is not ends with Null - add");
                    reason.add(reasonMetricLabel.getValue());
                }
            }
        }

        MetricLabel newReasonMetricLabel = reason.getMetricLabel();
        return newReasonMetricLabel;
    }

    private boolean endsWithArrowNull(String input) {
        return input != null && input.trim().endsWith("=> Null");
    }

    private List<Argument> getArguments(String argName, List<String> args, Boolean ignoreNull) {
        List<Argument> result = SupportArgsPreProcessor.searchArgs(args, argName);
        if (!ignoreNull)
            ObjectsUtils.requireNonNull(result, new IllegalArgumentException("Arguments " + argName + " not found!"));
        return result;
    }

    private Argument getArgument(String argName, List<String> args, Boolean ignoreNull) {
        Argument result = SupportArgsPreProcessor.searchArg(args, argName);
        if (!ignoreNull)
            ObjectsUtils.requireNonNull(result, new IllegalArgumentException("Argument " + argName + " not found!"));
        return result;
    }

    private List<MetricLabel> extractLabels(List<String> args) {
        List<MetricLabel> metricLabelList = new ArrayList<>();

        for (String arg : args) {
            if (arg == null || arg.isEmpty()) {
                continue;
            }

            String[] parts = arg.split("=", 2);
            if (parts.length != 2) {
                // можно выбросить исключение или залогировать предупреждение
                logger.warn("Invalid argument: " + arg);
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
