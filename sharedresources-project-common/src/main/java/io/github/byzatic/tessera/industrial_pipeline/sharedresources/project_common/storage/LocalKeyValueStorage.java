package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.storage;

import io.github.byzatic.tessera.storageapi.dto.DataValueInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LocalKeyValueStorage<K extends String, V extends DataValueInterface> implements LocalKeyValueStorageInterface<K, V> {
    private final static Logger logger= LoggerFactory.getLogger(LocalKeyValueStorage.class);
    private final Map<K, V> storage = new HashMap<>();

    // Create or Update
    @Override
    public void put(K key, V value) {
        logger.debug("Put value {} by key {}", value, key);
        if (storage.containsKey(key)) {
            storage.remove(key);
            storage.put(key, value);
        } else {
            storage.put(key, value);
        }
        logger.debug("Put value {} by key {} complete", value, key);
    }

    // Read
    @Override
    public V get(K key) {
        logger.debug("Get value by key {}", key);
        V result = storage.get(key);
        if (result == null) {
            logger.error("Storage not contains object by key " + key);
            logger.error("Storage dump" + storage);
            this.printAll();
            throw new IllegalArgumentException("Storage not contains object by key " + key);
        }
        V value = storage.get(key);
        logger.debug("Get value by key {} complete; value is {}", key, value);
        return value;
    }

    // Delete
    @Override
    public boolean delete(K key) {
        logger.debug("Delete value by key {}", key);
        boolean result = storage.remove(key) != null;
        logger.debug("Delete value by key {} complete", key);
        return result;
    }

    // Check existence
    @Override
    public boolean containsKey(K key) {
        logger.debug("Check contains key {}", key);
        boolean result = storage.containsKey(key);
        logger.debug("Check contains key {} complete; result is {}", key, result);
        return result;
    }

    // Print all entries (for testing)
    @Override
    public void printAll() {
        storage.forEach((k, v) -> logger.debug(k + " -> " + v));
    }

    // Clear all entries
    @Override
    public void clear() {
        logger.debug("Clear storage");
        storage.clear();
        logger.debug("Clear storage complete");
    }
}
