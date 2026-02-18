package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.process;

import io.github.byzatic.commons.ObjectsUtils;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.MetricLabel;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.storage.LocalKeyValueStorage;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.storage.LocalKeyValueStorageInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain.model.Argument;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain.service.ProcessEngineInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.GeneratorInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.GeneratorsFactory;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.GeneratorsFactoryInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pipeline_segment.PipelineSegmentGeneratorInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pump_unit.PumpUnitValuesGeneratorInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.universal_valves.UniversalValvesGeneratorInterface;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.service.SupportArgsPreProcessor;
import io.github.byzatic.tessera.storageapi.dto.DataValueInterface;
import io.github.byzatic.tessera.storageapi.dto.StorageItem;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.storageapi.storageapi.StorageApiInterface;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.configuration.ConfigurationParameter;
import io.github.byzatic.tessera.workflowroutine.execution_context.GraphPathInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessEngine implements ProcessEngineInterface {
    private final static Logger logger = LoggerFactory.getLogger(ProcessEngine.class);
    private final StorageApiInterface workflowRoutineStorageApi;
    private final LocalKeyValueStorageInterface<String, DataValueInterface> localStore = new LocalKeyValueStorage<>();
    private final GraphPathInterface graphPath;
    private final GeneratorsFactoryInterface generatorsFactory = new GeneratorsFactory();

    private Path configurationFilePath = null;

    public ProcessEngine(MCg3WorkflowRoutineApiInterface workflowRoutineApi) throws MCg3ApiOperationIncompleteException {
        try {
            for (ConfigurationParameter configurationParameter : workflowRoutineApi.getConfigurationParameters()) {
                logger.debug("Process configurationParameter - {}", configurationParameter);

                if (configurationParameter.getParameterKey().equals("configurationFilePath")) {
                    configurationFilePath = Path.of(configurationParameter.getParameterValue());
                    break;
                }
            }

            if (configurationFilePath == null) {
                throw new MCg3ApiOperationIncompleteException("Routine param configurationFilePath not set");
            }

            this.workflowRoutineStorageApi = workflowRoutineApi.getStorageApi();

            this.graphPath = workflowRoutineApi.getExecutionContext().getPipelineExecutionInfo().getCurrentNodeExecutionGraphPath();
            logger.debug("Requested CurrentNodeExecutionGraphPath graphPath= {}", graphPath);

        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    @Override
    public void processData(String functionName, List<String> args, String resultId) throws MCg3ApiOperationIncompleteException {
        try {
            switch (functionName) {
                case "GenerateData" -> preprocessorGenerateData(resultId, args);
                case "ProcessReason" -> preprocessorProcessReason(resultId, args);
                default -> throw new MCg3ApiOperationIncompleteException("No such function " + functionName);
            }
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    private void preprocessorGenerateData(String resultId, List<String> args) {
        try {
            Integer metricData = null;
            MetricLabel reason = null;

            GeneratorInterface generator = generatorsFactory.createFromConfigFile(configurationFilePath);

            if (generator instanceof UniversalValvesGeneratorInterface) {
                UniversalValvesGeneratorInterface.ValveState valveState = ((UniversalValvesGeneratorInterface) generator).generate();
                GeneratorInterface.ResolveResult resolveResult = ((UniversalValvesGeneratorInterface) generator).resolve(valveState);
                metricData = switch (resolveResult.range()) {
                    case CRITICAL -> 2;
                    case WARN -> 1;
                    case OK -> 0;
                };
                if (resolveResult.range() == GeneratorInterface.SpecialValueRange.CRITICAL || resolveResult.range() == GeneratorInterface.SpecialValueRange.WARN) {
                    reason = MetricLabel.newBuilder()
                            .setKey("reason")
                            .setSign("=")
                            .setValue(graphPath.getGraphPath() + " => " + resolveResult.explain())
                            .build();
                }
            } else if (generator instanceof PipelineSegmentGeneratorInterface) {
                Float value = ((PipelineSegmentGeneratorInterface) generator).generate();
                GeneratorInterface.ResolveResult resolveResult = ((PipelineSegmentGeneratorInterface) generator).resolve(value);
                metricData = switch (resolveResult.range()) {
                    case CRITICAL -> 2;
                    case WARN -> 1;
                    case OK -> 0;
                };
                if (resolveResult.range() == GeneratorInterface.SpecialValueRange.CRITICAL || resolveResult.range() == GeneratorInterface.SpecialValueRange.WARN) {
                    reason = MetricLabel.newBuilder()
                            .setKey("reason")
                            .setSign("=")
                            .setValue(graphPath.getGraphPath() + " => " + resolveResult.explain())
                            .build();
                }
            } else if (generator instanceof PumpUnitValuesGeneratorInterface) {
                Float value = ((PumpUnitValuesGeneratorInterface) generator).generate();
                GeneratorInterface.ResolveResult resolveResult = ((PumpUnitValuesGeneratorInterface) generator).resolve(value);
                metricData = switch (resolveResult.range()) {
                    case CRITICAL -> 2;
                    case WARN -> 1;
                    case OK -> 0;
                };
                if (resolveResult.range() == GeneratorInterface.SpecialValueRange.CRITICAL || resolveResult.range() == GeneratorInterface.SpecialValueRange.WARN) {
                    reason = MetricLabel.newBuilder()
                            .setKey("reason")
                            .setSign("=")
                            .setValue(graphPath.getGraphPath() + " => " + resolveResult.explain())
                            .build();
                }
            } else {
                throw new MCg3ApiOperationIncompleteException("Failed to calculate metric data >> generator has interfaces: " + Arrays.toString(generator.getClass().getInterfaces()));
            }

            List<MetricLabel> metricLabels = new ArrayList<>();
            if (reason != null) {
                metricLabels.add(reason);
            }

            localStore.put(resultId, DataItem.newBuilder()
                    .setMetricName(resultId.toLowerCase())
                    .setMetricLabels(metricLabels)
                    .setMetricValue(metricData.toString())
                    .setMetricCreationTime(Instant.now())
                    .build()
            );

        } catch (IOException | MCg3ApiOperationIncompleteException e) {
            throw new RuntimeException(e);
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

    private MetricLabel getReasonMetricLabel(DataItem dataItem, String reasonMsg) {
        MetricLabel reason = null;
        MetricLabel reasonMetricLabel = findByKey(dataItem, "reason");
        if (reasonMetricLabel == null) {
            reason = MetricLabel.newBuilder()
                    .setKey("reason")
                    .setSign("=")
                    .setValue(graphPath.getGraphPath() + " => " + reasonMsg)
                    .build();
        } else {
            reason = MetricLabel.newBuilder()
                    .setKey("reason")
                    .setSign("=")
                    .setValue(reasonMetricLabel.getKey())
                    .build();
        }
        return reason;
    }

    private MetricLabel findByKey(DataItem dataItem, String key) {
        List<MetricLabel> labels = dataItem.getMetricLabels();
        if (labels == null || key == null) return null;
        for (MetricLabel label : labels) {
            if (key.equals(label.getKey())) {
                return label;
            }
        }
        return null;
    }

    private void preprocessorProcessReason(String resultId, List<String> args) {
        Argument argGlobalReasonMessage = getArg(args, "GlobalReasonMessage", Boolean.TRUE);
        Argument argOkReasonMessage = getArg(args, "OkReasonMessage", Boolean.TRUE);
        Argument argWarningReasonMessage = getArg(args, "WarningReasonMessage", Boolean.TRUE);
        Argument argAlarmReasonMessage = getArg(args, "AlarmReasonMessage", Boolean.TRUE);
        Argument argPasteReasonWhenOk = getArg(args, "PasteReasonWhenOk", Boolean.FALSE);
        Argument argEmptyData = getArg(args, "EmptyData", Boolean.FALSE);
        Argument argIgnoreExistsReason = getArg(args, "IgnoreExistsReason", Boolean.TRUE);
        Argument argDataId = getArg(args, "DataId", Boolean.FALSE);

        DataItem dataItem = (DataItem) localStore.get(argDataId.getValue());
        Integer value = Integer.valueOf(dataItem.getMetricValue());

        DataItem newDataItem = null;

        if (argIgnoreExistsReason != null && Boolean.valueOf(argIgnoreExistsReason.getValue()) == Boolean.FALSE) {
            boolean hasReasonLabel = dataItem.getMetricLabels().stream()
                    .anyMatch(label -> "reason".equals(label.getKey()));
            if (hasReasonLabel == Boolean.FALSE)
                throw new RuntimeException("arg IgnoreExistsReason is not null and equals True, but reasons' label was not found");
            newDataItem = DataItem.newBuilder(dataItem).build();
        } else if (argGlobalReasonMessage == null) {
            logger.debug("GlobalReasonMessage is null");

            ObjectsUtils.requireNonNull(argWarningReasonMessage, new IllegalArgumentException("Argument " + "WarningReasonMessage" + " not found!"));
            ObjectsUtils.requireNonNull(argAlarmReasonMessage, new IllegalArgumentException("Argument " + "AlarmReasonMessage" + " not found!"));

            if (value == 0 && !Boolean.parseBoolean(argPasteReasonWhenOk.getValue())) {
                logger.debug("value is 0 and PasteReasonWhenOk is False");
                newDataItem = DataItem.newBuilder(dataItem).build();
            } else if (value == 0 && Boolean.parseBoolean(argPasteReasonWhenOk.getValue())) {
                logger.debug("value is 0 and PasteReasonWhenOk is True");
                ObjectsUtils.requireNonNull(argOkReasonMessage, new IllegalArgumentException("Argument " + "OkReasonMessage" + " not found!"));
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argOkReasonMessage.getValue()));
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
            } else if (value == 1) {
                logger.debug("value is 1");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argWarningReasonMessage.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
            } else if (value == 2) {
                logger.debug("value is 2");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argAlarmReasonMessage.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
            } else if (value == 3) {
                logger.debug("value is 3");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argEmptyData.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).setMetricValue("1").build();
            } else {
                throw new IllegalArgumentException("Wrong value " + value + " to generate reason message.");
            }

        } else {
            logger.debug("GlobalReasonMessage is not null");

            if (value == 0 && !Boolean.parseBoolean(argPasteReasonWhenOk.getValue())) {
                logger.debug("value is 0 and PasteReasonWhenOk is False");
                newDataItem = DataItem.newBuilder(dataItem).build();
            } else if (value == 0 && Boolean.parseBoolean(argPasteReasonWhenOk.getValue())) {
                logger.debug("value is 0 and PasteReasonWhenOk is True");
                if (argOkReasonMessage != null) {
                    logger.debug("OkReasonMessage is not null");
                    List<MetricLabel> labelsList = dataItem.getMetricLabels();
                    labelsList.add(getReasonMetricLabel(dataItem, argOkReasonMessage.getValue())
                    );
                    newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
                } else {
                    logger.debug("OkReasonMessage is null");
                    newDataItem = DataItem.newBuilder(dataItem).build();
                }
            } else if (value == 1) {
                logger.debug("value is 1");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argGlobalReasonMessage.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
            } else if (value == 2) {
                logger.debug("value is 2");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argGlobalReasonMessage.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
            } else if (value == 3) {
                logger.debug("value is 3");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argEmptyData.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).setMetricValue("1").build();
            } else {
                throw new IllegalArgumentException("Wrong value " + value + " to generate reason message.");
            }
        }

        if (localStore.containsKey(resultId)) {
            localStore.delete(resultId);
            localStore.put(resultId, newDataItem);
        } else {
            localStore.put(resultId, newDataItem);
        }
    }

    private void preprocessorRemoveServiceLabel(String resultId, List<String> args) {
        String argName = "FromDataId";
        Argument dataId = SupportArgsPreProcessor.searchArg(args, argName);
        ObjectsUtils.requireNonNull(dataId, new IllegalArgumentException("Argument " + argName + " not found!"));
        String dataIdName = dataId.getValue();


        DataItem dataItem = (DataItem) localStore.get(dataIdName);

        List<MetricLabel> labels = new ArrayList<>(dataItem.getMetricLabels());
        labels.removeIf(label -> {
            String val = label.getValue();
            return val != null && val.matches("__.*__");
        });

        labels.removeIf(label -> {
            String val = label.getKey();
            return val != null && val.matches("__.*__");
        });

        DataItem newDataItem = DataItem.newBuilder()
                .setMetricName(dataItem.getMetricName())
                .setMetricLabels(labels)
                .setMetricValue(dataItem.getMetricValue())
                .setMetricCreationTime(dataItem.getMetricCreationTime())
                .build();

        localStore.delete(dataIdName);
        localStore.put(dataIdName, newDataItem);
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
}
