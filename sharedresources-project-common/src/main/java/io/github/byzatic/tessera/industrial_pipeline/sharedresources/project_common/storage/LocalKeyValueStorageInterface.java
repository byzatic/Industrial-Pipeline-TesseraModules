package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.storage;

import io.github.byzatic.tessera.storageapi.dto.DataValueInterface;

public interface LocalKeyValueStorageInterface<K extends String, V extends DataValueInterface> {
    // Create or Update
    void put(K key, V value);

    // Read
    V get(K key);

    // Delete
    boolean delete(K key);

    // Check existence
    boolean containsKey(K key);

    // Print all entries (for testing)
    void printAll();

    // Clear all entries
    void clear();
}
