package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.metric_repo;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;

import java.util.List;

public interface MetricRepositoryInterface {
    void put(DataItem dataItem);

    void remove(DataItem dataItem);

    List<DataItem> list();
}
