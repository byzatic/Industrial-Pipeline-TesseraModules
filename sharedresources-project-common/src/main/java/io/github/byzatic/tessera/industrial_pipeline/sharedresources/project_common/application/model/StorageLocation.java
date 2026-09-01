package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model;

import java.util.Objects;

public record StorageLocation(
        StorageScope scope,
        String downstreamName,
        String storageId,
        String dataId
) {

    public StorageLocation {
        Objects.requireNonNull(scope, "scope must not be null");
        Objects.requireNonNull(storageId, "storageId must not be null");
        Objects.requireNonNull(dataId, "dataId must not be null");
        if (scope == StorageScope.DOWNSTREAM) {
            Objects.requireNonNull(downstreamName, "downstreamName must not be null for DOWNSTREAM scope");
        }
    }

    public static StorageLocation local(String storageId, String dataId) {
        return new StorageLocation(StorageScope.LOCAL, null, storageId, dataId);
    }

    public static StorageLocation global(String storageId, String dataId) {
        return new StorageLocation(StorageScope.GLOBAL, null, storageId, dataId);
    }

    public static StorageLocation downstream(String downstreamName, String storageId, String dataId) {
        return new StorageLocation(StorageScope.DOWNSTREAM, downstreamName, storageId, dataId);
    }
}
