package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter;

import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.metric_repo.MetricRepository;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.metric_repo.MetricRepositoryInterface;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.metric_repo.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.commons.base_exceptions.OperationIncompleteException;
import io.github.byzatic.tessera.storageapi.dto.StorageItem;
import io.github.byzatic.tessera.storageapi.storageapi.StorageApiInterface;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MetricsUpdateManager implements MetricsUpdateManagerInterface {
    private final static Logger logger = LoggerFactory.getLogger(MetricsUpdateManager.class);
    private final Long expiredMinutesAgo;
    private final String storageName;
    private final StorageApiInterface storageApi;
    private final MetricRepositoryInterface metricRepository = new MetricRepository();

    public MetricsUpdateManager(StorageApiInterface storageApi, String storageName, Long expiredMinutesAgo) {
        this.storageApi = storageApi;
        this.storageName = storageName;
        this.expiredMinutesAgo = expiredMinutesAgo;
    }

    @Override
    public void updateMetrics() throws OperationIncompleteException {
        try {
            logger.debug("Run update metrics");

            removeExpired();

            List<StorageItem> storageItemList = storageApi.listStorageObjects(
                    StorageItem.newBuilder()
                            .setScope(StorageItem.ScopeType.GLOBAL)
                            .setStorageId(storageName)
                            .build());
            logger.debug("Metric Item List size is {}", storageItemList.size());

            for (StorageItem storageItem : storageItemList) {
                logger.debug("Processing storageItem - {}", storageItem);
                // TODO: Not used until https://github.com/byzatic/JavaByzaticCommons/issues/3
                //DataItem newDataItem = CustomConverter.parse(storageItem.getDataValue(), DataItem.class);
                DataItem newDataItem = (DataItem) storageItem.getDataValue();
                metricRepository.put(newDataItem);
            }
            logger.debug("Update metrics complete");
        } catch (Exception e) {
            throw new OperationIncompleteException(e);
        }
    }

    private void removeExpired() {
        logger.debug("Expired metric cleaning");
        Instant expirationInstant = Instant.now().minus(Duration.ofMinutes(expiredMinutesAgo));

        List<DataItem> dataItemsToRemove = new ArrayList<>();

        for (DataItem dataItem : metricRepository.list()) {
            String dataId = String.valueOf(Scope.calculateId(dataItem));
            logger.debug("Check for id={} dataItem={}", dataId, dataItem);
            boolean isOlder = dataItem.getMetricCreationTime().isBefore(expirationInstant);
            logger.debug("dataItem id={} is older than {} -> {}", dataId, expirationInstant, isOlder);
            if (isOlder) {
                logger.debug("Metric id={} needs to remove, remove", dataId);
                dataItemsToRemove.add(dataItem);
            }
        }
        for (DataItem dataItem : dataItemsToRemove) {
            metricRepository.remove(dataItem);
        }
        logger.debug("Expired metric cleaning complete");
    }
}
