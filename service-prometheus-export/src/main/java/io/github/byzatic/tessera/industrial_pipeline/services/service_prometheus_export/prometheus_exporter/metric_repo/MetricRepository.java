package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter.metric_repo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.DataItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricRepository implements MetricRepositoryInterface {
    private final static Logger logger = LoggerFactory.getLogger(MetricRepository.class);
    private Map<Integer, Scope> idsScopeMap = new HashMap<>();

    @Override
    public void put(@NotNull DataItem dataItem) {
        logger.debug("PUT dataItem= {}", dataItem);
        Scope scope= getScope(dataItem);
        if (scope == null) {
            logger.debug("Scope not found by id; create");
            scope= createScope(dataItem);
            logger.debug("Scope update item; update");
            scope.update(dataItem);
        } else {
            if (scope.contains(dataItem)) {
                logger.debug("Scope contains item; scope.update");
                scope.update(dataItem);
            } else {
                logger.debug("Scope not contains item; scope.add");
                scope.add(dataItem);
            }
        }
    }

    @Override
    public void remove(@NotNull DataItem dataItem) {
        Scope scope= getScope(dataItem);
        if (scope == null) throw new IllegalArgumentException("Can't remove DataItem; not in scope");
        scope.remove(dataItem);
    }

    @Override
    public List<DataItem> list() {
        List<DataItem> dataItems = new ArrayList<>();
        for (Map.Entry<Integer, Scope> scopeEntry : idsScopeMap.entrySet()) {
            dataItems.addAll(scopeEntry.getValue().list());
        }
        return dataItems;
    }

    private @Nullable Scope getScope(@NotNull DataItem dataItem) {
        logger.debug("Search scope for dataItem= {}", dataItem);
        Scope result = null;
        Integer scopeId = Scope.calculateId(dataItem);
        logger.debug("Search scope id is {}", scopeId);
        if (idsScopeMap.containsKey(scopeId)) {
            result = idsScopeMap.get(scopeId);
            logger.debug("Scope by id {} was found; scope= {}", scopeId, result);
        } else {
            logger.debug("Scope by id {} was not found; scope= {}", scopeId, result);
        }
        logger.debug("Returns serach result= {}", result);
        return result;
    }

    private @NotNull Scope createScope(@NotNull DataItem dataItem) {
        logger.debug("Create scope for dataItem= {}", dataItem);
        Integer scopeId = Scope.calculateId(dataItem);
        logger.debug("Create scope id is {}", scopeId);
        if (idsScopeMap.containsKey(scopeId)) throw new IllegalArgumentException("Can't create scope; already exists");
        Scope scope = new Scope(dataItem);
        idsScopeMap.put(scopeId, scope);
        logger.debug("Create scope by id {} complete; scope= {}", scopeId, scope);
        return scope;
    }

}
