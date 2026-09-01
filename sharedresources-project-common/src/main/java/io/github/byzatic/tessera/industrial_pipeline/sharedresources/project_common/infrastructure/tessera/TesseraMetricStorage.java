package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.tessera;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.StorageLocation;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model.StorageScope;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.repository.MetricStorage;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.domain.model.MetricSample;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;
import io.github.byzatic.tessera.storageapi.dto.StorageItem;
import io.github.byzatic.tessera.storageapi.storageapi.StorageApiInterface;

import java.util.Objects;

public final class TesseraMetricStorage implements MetricStorage {
    private final StorageApiInterface storageApi;
    private final TesseraMetricMapper mapper;

    public TesseraMetricStorage(StorageApiInterface storageApi, TesseraMetricMapper mapper) {
        this.storageApi = Objects.requireNonNull(storageApi, "storageApi must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @Override
    public MetricSample load(StorageLocation location) throws RoutineExecutionException {
        try {
            StorageItem response = storageApi.getStorageObject(toStorageItem(location, null));
            if (!(response.getDataValue() instanceof DataItem dataItem)) {
                throw new RoutineExecutionException(
                        "Storage value is not a DataItem: " + location
                );
            }
            return mapper.toDomain(dataItem);
        } catch (RoutineExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new RoutineExecutionException("Failed to load metric from " + location, e);
        }
    }

    @Override
    public void save(StorageLocation location, MetricSample metric) throws RoutineExecutionException {
        try {
            storageApi.putStorageObject(toStorageItem(location, mapper.toPersistence(metric)));
        } catch (Exception e) {
            throw new RoutineExecutionException("Failed to save metric to " + location, e);
        }
    }

    private static StorageItem toStorageItem(StorageLocation location, DataItem value) {
        return StorageItem.newBuilder()
                .setScope(toTesseraScope(location.scope()))
                .setDownstreamName(location.downstreamName())
                .setStorageId(location.storageId())
                .setDataId(location.dataId())
                .setDataValue(value)
                .build();
    }

    private static StorageItem.ScopeType toTesseraScope(StorageScope scope) {
        return switch (scope) {
            case LOCAL -> StorageItem.ScopeType.LOCAL;
            case GLOBAL -> StorageItem.ScopeType.GLOBAL;
            case DOWNSTREAM -> StorageItem.ScopeType.DOWNSTREAM;
        };
    }
}
