package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.MetricLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SupportMetric {
    private final static Logger logger = LoggerFactory.getLogger(SupportMetric.class);

    public static List<MetricLabel> removeReason(List<MetricLabel> metricLabels) {
        List<MetricLabel> newMetricLabels = new ArrayList<>();
        for (MetricLabel metricLabel : metricLabels) {
            if (!metricLabel.getKey().equals("reason")) {
                newMetricLabels.add(metricLabel);
            }
        }
        return newMetricLabels;
    }

    public static String[] extractLabelNameStringArray(List<MetricLabel> labelList) {
        List<String> metricLabelNamesList = new ArrayList<>();
        for (MetricLabel metricLabel : labelList) {
            metricLabelNamesList.add(metricLabel.getKey());
        }
        return metricLabelNamesList.toArray(new String[0]);
    }

    public static String[] extractLabelValuesStringArray(List<MetricLabel> labelList) {
        List<String> metricLabelValuesList = new ArrayList<>();
        for (MetricLabel metricLabel : labelList) {
            metricLabelValuesList.add(metricLabel.getValue());
        }
        return metricLabelValuesList.toArray(new String[0]);
    }

    public static Boolean isEqualByReason(DataItem firstDataItem, DataItem secondDataItem) {
        MetricLabel firstDataItemReason = extractReason(firstDataItem.getMetricLabels());
        MetricLabel secondDataItemReason = extractReason(secondDataItem.getMetricLabels());

        if (firstDataItemReason == null && secondDataItemReason == null) {
            return Boolean.TRUE;
        }

        if (firstDataItemReason == null || secondDataItemReason == null) {
            return Boolean.FALSE;
        }

        return firstDataItemReason.equals(secondDataItemReason);
    }


    public static @Nullable MetricLabel extractReason(List<MetricLabel> metricLabelList) {
        MetricLabel reason = null;
        for (MetricLabel metricLabel : metricLabelList) {
            if (metricLabel.getKey().equals("reason")) {
                reason = metricLabel;
                break;
            }
        }
        return reason;
    }

    public static Integer getUnreasonedHash(DataItem dataItem) {
        String metricName = dataItem.getMetricName();
        List<MetricLabel> unreasonedMetricLabels = removeReason(dataItem.getMetricLabels());
        return Objects.hash(metricName, unreasonedMetricLabels);
    }


}
