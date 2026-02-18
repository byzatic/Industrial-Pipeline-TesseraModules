package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.metric_repo;

import io.prometheus.metrics.core.metrics.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.MetricLabel;

import java.util.*;

public class Scope {
    private final static Logger logger = LoggerFactory.getLogger(Scope.class);
    private final String scopeName;
    private final Integer scopeId;
    private final Gauge gauge;
    private final Map<Integer, DataItem> dataItemMap;

    public Scope(DataItem dataItem) {
        this.scopeName = dataItem.getMetricName();
        this.scopeId = calculateId(dataItem);

        String[] labelNames = extractLabelNameStringArray(dataItem.getMetricLabels());
        this.gauge= Gauge.builder()
                .name(dataItem.getMetricName())
                .labelNames(labelNames)
                .register();

        this.gauge
                .labelValues(extractLabelValuesStringArray(dataItem.getMetricLabels()))
                .set(Double.parseDouble(dataItem.getMetricValue()));

        this.dataItemMap= new HashMap<>();
        this.dataItemMap.put(getUnreasonedHash(dataItem), dataItem);
    }

    public Integer getId() {
        return this.scopeId;
    }

    public void add(DataItem dataItem) {
        logger.debug("add to scope");
        if (contains(dataItem)) throw new IllegalArgumentException("Can't add DataItem; already contains in scope "+scopeName+"; DataItem is "+dataItem);
        this.gauge
                .labelValues(extractLabelValuesStringArray(dataItem.getMetricLabels()))
                .set(Double.parseDouble(dataItem.getMetricValue()));
        this.dataItemMap.put(getUnreasonedHash(dataItem), dataItem);
    }

    public void remove(DataItem dataItem) {
        logger.debug("remove from scope");
        if (!contains(dataItem)) throw new IllegalArgumentException("Can't remove DataItem; not contains in scope "+scopeName+"; DataItem is "+dataItem);
        this.gauge.remove(extractLabelValuesStringArray(dataItem.getMetricLabels()));
        this.dataItemMap.remove(getUnreasonedHash(dataItem));
    }

    public void update(DataItem dataItem) {
        logger.debug("update scope");
        if (!contains(dataItem)) throw new IllegalArgumentException("Can't update DataItem; not contains in scope "+scopeName+"; DataItem is "+dataItem);
        DataItem oldDataItem = dataItemMap.get(getUnreasonedHash(dataItem));
        if (isEqualExcludeValue(dataItem, oldDataItem)) {
            logger.debug("update scope clear");
            this.gauge
                    .labelValues(extractLabelValuesStringArray(dataItem.getMetricLabels()))
                    .set(Double.parseDouble(dataItem.getMetricValue()));
            this.dataItemMap.put(getUnreasonedHash(dataItem), dataItem);
        } else {
            logger.debug("update scope remove & add");
            remove(oldDataItem);
            add(dataItem);
        }
    }

    private boolean isEqualExcludeValue(DataItem dataItemOne, DataItem dataItemTwo) {
        if (!dataItemOne.getMetricName().equals(dataItemTwo.getMetricName())) {
            return Boolean.FALSE;
        }
        if (!Objects.equals(dataItemOne.getMetricLabels(), dataItemTwo.getMetricLabels())) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public Boolean contains(DataItem dataItem) {
        return dataItemMap.containsKey(getUnreasonedHash(dataItem));
    }

    public List<DataItem> list() {
        List<DataItem> dataItems = new ArrayList<>();
        for (Map.Entry<Integer, DataItem> dataItemEntryEntry : dataItemMap.entrySet()) {
            dataItems.add(dataItemEntryEntry.getValue());
        }
        return dataItems;
    }

    public Boolean matchesScope(DataItem dataItem) {
        return Objects.equals(scopeId, calculateId(dataItem));
    }

    public static Integer calculateId(DataItem dataItem) {
        String name = dataItem.getMetricName();
        List<String> labelNameList = new ArrayList<>();
        for (MetricLabel label : dataItem.getMetricLabels()) {
            labelNameList.add(label.getKey());
        }
        return Objects.hash(name, labelNameList);
    }

    private String[] extractLabelNameStringArray(List<MetricLabel> labelList) {
        List<String> metricLabelNamesList = new ArrayList<>();
        for (MetricLabel metricLabel : labelList) {
            metricLabelNamesList.add(metricLabel.getKey());
        }
        return metricLabelNamesList.toArray(new String[0]);
    }

    private String[] extractLabelValuesStringArray(List<MetricLabel> labelList) {
        List<String> metricLabelValuesList = new ArrayList<>();
        for (MetricLabel metricLabel : labelList) {
            metricLabelValuesList.add(metricLabel.getValue());
        }
        return metricLabelValuesList.toArray(new String[0]);
    }

    private Integer getUnreasonedHash(DataItem dataItem) {
        String metricName = dataItem.getMetricName();
        List<MetricLabel> unreasonedMetricLabels = removeReason(dataItem.getMetricLabels());
        return Objects.hash(metricName, unreasonedMetricLabels);
    }

    private List<MetricLabel> removeReason(List<MetricLabel> metricLabels) {
        List<MetricLabel> newMetricLabels = new ArrayList<>();
        for (MetricLabel metricLabel : metricLabels) {
            if (!metricLabel.getKey().equals("reason")) {
                newMetricLabels.add(metricLabel);
            }
        }
        return newMetricLabels;
    }
}
