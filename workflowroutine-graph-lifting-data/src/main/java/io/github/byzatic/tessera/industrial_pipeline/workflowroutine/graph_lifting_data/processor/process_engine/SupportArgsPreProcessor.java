package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.graph_lifting_data.processor.process_engine;

import org.apache.commons.math3.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupportArgsPreProcessor {
    public static Argument searchArg(List<String> args, String argName) {
        Argument argument = null;

        Map<String, String> stringStringMap = extract(args);
        if (stringStringMap.containsKey(argName)) {
            argument = Argument.newBuilder()
                    .setKey(String.copyValueOf(argName.toCharArray()))
                    .setValue(stringStringMap.get(argName))
                    .build();

        }

        return argument;
    }

    public static Pair<String, String> parseKeyValue(String input) {
        int equalsIndex = input.indexOf('=');
        if (equalsIndex == -1) {
            throw new IllegalArgumentException("Input must contain '=' character");
        }

        String key = input.substring(0, equalsIndex).trim();
        String value = input.substring(equalsIndex + 1).trim();

        return new Pair<>(key, value);
    }

    public static Map<String, String> extract(List<String> input) {
        Map<String, String> result = new HashMap<>();

        for (String arg : input) {
            String[] parts = arg.split("=", 2);
            if (parts.length == 2) {
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }
}
