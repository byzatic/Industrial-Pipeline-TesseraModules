package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.infrastructure.tessera;

import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.repository.MetricSource;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.tessera.TesseraMetricMapper;
import io.github.byzatic.tessera.storageapi.dto.StorageItem;
import io.github.byzatic.tessera.storageapi.storageapi.StorageApiInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TesseraGlobalMetricSource implements MetricSource {
    private final StorageApiInterface storageApi;
    private final String storageName;
    private final TesseraMetricMapper mapper;

    public TesseraGlobalMetricSource(
            StorageApiInterface storageApi,
            String storageName,
            TesseraMetricMapper mapper
    ) {
        this.storageApi = Objects.requireNonNull(storageApi, "storageApi must not be null");
        this.storageName = Objects.requireNonNull(storageName, "storageName must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @Override
    public List<MetricSample> loadMetrics() throws RoutineExecutionException {
        try {
            List<StorageItem> items = storageApi.listStorageObjects(
                    StorageItem.newBuilder()
                            .setScope(StorageItem.ScopeType.GLOBAL)
                            .setStorageId(storageName)
                            .build()
            );
            List<MetricSample> metrics = new ArrayList<>(items.size());
            for (StorageItem item : items) {
                if (!(item.getDataValue() instanceof DataItem dataItem)) {
                    throw new RoutineExecutionException(
                            "Storage " + storageName + " contains a non-DataItem value"
                    );
                }
                metrics.add(mapper.toDomain(dataItem));
            }
            return List.copyOf(metrics);
        } catch (RoutineExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new RoutineExecutionException(
                    "Failed to load metrics from global storage " + storageName,
                    e
            );
        }
    }
}
