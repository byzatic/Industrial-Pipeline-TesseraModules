package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.config_loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.config_loader.model.GeneratorConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader {

    private static final Gson GSON = new GsonBuilder()
            // при желании можно включить:
            // .serializeNulls()
            // .setLenient()
            .create();

    private ConfigLoader() {
    }

    public static GeneratorConfig readConfig(Path jsonPath) throws IOException {
        if (jsonPath == null) {
            throw new IllegalArgumentException("jsonPath must not be null");
        }

        if (!Files.exists(jsonPath)) {
            throw new IllegalStateException("Config file does not exist: " + jsonPath.toAbsolutePath());
        }

        if (!Files.isRegularFile(jsonPath)) {
            throw new IllegalStateException("Config path is not a regular file: " + jsonPath.toAbsolutePath());
        }

        if (!Files.isReadable(jsonPath)) {
            throw new IllegalStateException("Config file is not readable: " + jsonPath.toAbsolutePath());
        }

        // Опционально: защита от пустых файлов
        if (Files.size(jsonPath) == 0) {
            throw new IllegalStateException("Config file is empty: " + jsonPath.toAbsolutePath());
        }

        final GeneratorConfig cfg;
        try (BufferedReader reader = Files.newBufferedReader(jsonPath, StandardCharsets.UTF_8)) {
            cfg = GSON.fromJson(reader, GeneratorConfig.class);
        } catch (JsonParseException e) {
            throw new IllegalStateException("Invalid JSON in config file: " + jsonPath.toAbsolutePath(), e);
        }

        if (cfg == null) {
            throw new IllegalStateException("Config parsed as null (empty/invalid root JSON?): " + jsonPath.toAbsolutePath());
        }

        // Базовая валидация структуры (под твои поля)
        if (cfg.getGeneratorName() == null || cfg.getGeneratorName().isBlank()) {
            throw new IllegalStateException("Missing/blank field 'generator_name' in: " + jsonPath.toAbsolutePath());
        }

        if (cfg.getNominals() == null) {
            throw new IllegalStateException("Missing field 'nominals' in: " + jsonPath.toAbsolutePath());
        }

        // Если надо — можно валидировать NominalsItem внутри списка (name/value)
        // cfg.getNominals().forEach(n -> { ... });

        return cfg;
    }
}
