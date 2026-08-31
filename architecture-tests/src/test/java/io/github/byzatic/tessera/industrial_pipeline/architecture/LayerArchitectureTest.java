package io.github.byzatic.tessera.industrial_pipeline.architecture;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

public class LayerArchitectureTest {
    private static final List<String> OUTER_LAYER_MARKERS = List.of(
            ".infrastructure.",
            ".composition."
    );
    private static final List<String> FRAMEWORK_MARKERS = List.of(
            "io.github.byzatic.tessera.storageapi.",
            "io.github.byzatic.tessera.workflowroutine.",
            "io.github.byzatic.tessera.service.",
            "org.antlr.",
            "io.prometheus.",
            "com.google.gson."
    );

    @Test
    public void innerLayersDoNotDependOnOuterLayersOrFrameworks() throws IOException {
        Path repositoryRoot = Path.of("..").toAbsolutePath().normalize();
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(repositoryRoot)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("/src/main/java/"))
                    .forEach(path -> inspect(path, violations));
        }
        assertTrue("Architecture violations:\n" + String.join("\n", violations), violations.isEmpty());
    }

    private static void inspect(Path sourceFile, List<String> violations) {
        try {
            List<String> lines = Files.readAllLines(sourceFile);
            String packageName = lines.stream()
                    .filter(line -> line.startsWith("package "))
                    .map(line -> line.substring("package ".length(), line.length() - 1))
                    .findFirst()
                    .orElse("");
            boolean domain = packageName.contains(".domain.");
            boolean application = packageName.contains(".application.");
            if (!domain && !application) {
                return;
            }
            for (String line : lines) {
                if (!line.startsWith("import ")) {
                    continue;
                }
                if (OUTER_LAYER_MARKERS.stream().anyMatch(line::contains)
                        || FRAMEWORK_MARKERS.stream().anyMatch(line::contains)
                        || domain && line.contains(".application.")) {
                    violations.add(repositoryRelative(sourceFile) + " -> " + line.trim());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to inspect " + sourceFile, e);
        }
    }

    private static String repositoryRelative(Path sourceFile) {
        return Path.of("..").toAbsolutePath().normalize().relativize(sourceFile).toString();
    }
}
