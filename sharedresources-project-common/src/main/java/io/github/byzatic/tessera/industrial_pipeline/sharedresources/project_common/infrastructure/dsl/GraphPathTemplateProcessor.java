package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.dsl;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.provider.GraphPathProvider;

import java.util.Objects;

public final class GraphPathTemplateProcessor {
    private static final String SHORT_FUNCTION = "GP";
    private static final String LONG_FUNCTION = "GRAPH_PATH";

    private final GraphPathProvider graphPathProvider;

    public GraphPathTemplateProcessor(GraphPathProvider graphPathProvider) {
        this.graphPathProvider = Objects.requireNonNull(graphPathProvider, "graphPathProvider must not be null");
    }

    public String process(String source) throws RoutineExecutionException {
        Objects.requireNonNull(source, "source must not be null");
        StringBuilder result = new StringBuilder(source.length());
        int position = 0;
        int marker = source.indexOf("${");
        while (marker >= 0) {
            result.append(source, position, marker);
            int end = findExpressionEnd(source, marker + 2);
            if (end < 0) {
                throw malformed(marker, "Missing closing '}'");
            }
            result.append(resolve(source.substring(marker + 2, end), marker));
            position = end + 1;
            marker = source.indexOf("${", position);
        }
        return result.append(source, position, source.length()).toString();
    }

    private String resolve(String expression, int marker) throws RoutineExecutionException {
        int openParenthesis = expression.indexOf('(');
        int closeParenthesis = expression.lastIndexOf(')');
        if (openParenthesis <= 0 || closeParenthesis < openParenthesis) {
            throw malformed(marker, "Expected a function call");
        }
        if (!expression.substring(closeParenthesis + 1).isBlank()) {
            throw malformed(marker, "Unexpected characters after function call");
        }

        String function = expression.substring(0, openParenthesis).trim();
        if (!SHORT_FUNCTION.equals(function) && !LONG_FUNCTION.equals(function)) {
            throw malformed(marker, "Unknown function: " + function);
        }

        String rawArgument = expression.substring(openParenthesis + 1, closeParenthesis).trim();
        String graphPath = graphPathProvider.currentGraphPath();
        if (rawArgument.isEmpty()) {
            return Integer.toString(graphPath.hashCode());
        }
        if (rawArgument.length() < 2) {
            throw malformed(marker, "Argument must be a quoted string");
        }
        char quote = rawArgument.charAt(0);
        if ((quote != '\'' && quote != '"') || rawArgument.charAt(rawArgument.length() - 1) != quote) {
            throw malformed(marker, "Argument must be a quoted string");
        }
        String nodeName = unescape(rawArgument.substring(1, rawArgument.length() - 1), marker);
        if (nodeName.isEmpty()) {
            throw malformed(marker, "Node name must not be empty");
        }
        return Integer.toString((graphPath + "." + nodeName).hashCode());
    }

    private static int findExpressionEnd(String source, int position) {
        char quote = 0;
        boolean escaped = false;
        for (int i = position; i < source.length(); i++) {
            char current = source.charAt(i);
            if (quote != 0) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == quote) {
                    quote = 0;
                }
            } else if (current == '\'' || current == '"') {
                quote = current;
            } else if (current == '}') {
                return i;
            }
        }
        return -1;
    }

    private static String unescape(String source, int marker) throws RoutineExecutionException {
        StringBuilder result = new StringBuilder(source.length());
        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            if (current != '\\') {
                result.append(current);
                continue;
            }
            if (++i >= source.length()) {
                throw malformed(marker, "Invalid escape sequence");
            }
            char escaped = source.charAt(i);
            result.append(switch (escaped) {
                case '\\' -> '\\';
                case '"' -> '"';
                case '\'' -> '\'';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 't' -> '\t';
                default -> throw malformed(marker, "Unsupported escape sequence: \\" + escaped);
            });
        }
        return result.toString();
    }

    private static RoutineExecutionException malformed(int position, String message) {
        return new RoutineExecutionException(message + " at position " + position);
    }
}
