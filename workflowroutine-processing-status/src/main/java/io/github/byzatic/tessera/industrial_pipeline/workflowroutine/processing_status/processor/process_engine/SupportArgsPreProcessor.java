package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.processing_status.processor.process_engine;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SupportArgsPreProcessor {
    private final static Logger logger = LoggerFactory.getLogger(SupportArgsPreProcessor.class);

    public static Argument searchArg(List<String> args, String argName) {
        logger.debug("searchArg args= {}", args);
        logger.debug("searchArg argName= {}", argName);
        Argument argument = null;

        Map<String, List<String>> extractedArgsMap = extract(args);

        logger.debug("searchArg extractedArgsMap= {}", extractedArgsMap);
        if (extractedArgsMap.containsKey(argName)) {
            argument = Argument.newBuilder()
                    .setKey(String.copyValueOf(argName.toCharArray()))
                    .setValue(extractedArgsMap.get(argName).get(0))
                    .build();

        }

        return argument;
    }

    public static List<Argument> searchArgs(List<String> args, String argName) {
        List<Argument> argumentList = new ArrayList<>();

        Map<String, List<String>> stringStringMap = extract(args);
        for (Map.Entry<String, List<String>> argumentEntry : stringStringMap.entrySet()) {
            String argNName = argumentEntry.getKey();
            for (String argVValue : argumentEntry.getValue()) {
                if (argumentEntry.getKey().equals(argName)) {
                    argumentList.add(Argument.newBuilder()
                            .setKey(String.copyValueOf(argNName.toCharArray()))
                            .setValue(String.copyValueOf(argVValue.toCharArray()))
                            .build());
                }
            }
        }

        if (argumentList.isEmpty()) argumentList = null;
        return argumentList;
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

    public static Map<String, List<String>> extract(List<String> input) {
        Map<String, List<String>> result = new HashMap<>();

        for (String arg : input) {
            String[] parts = arg.split("=", 2);
            if (parts.length == 2) {
                String argKey = parts[0];
                String argValue = parts[1];
                if (result.containsKey(argKey)) {
                    result.get(argKey).add(argValue);
                } else {
                    result.put(argKey, new ArrayList<>(Collections.singleton(argValue)));
                }
            }
        }
        return result;
    }
}
