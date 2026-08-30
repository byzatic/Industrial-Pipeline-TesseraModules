package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.graph_lifting_data.processor;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslBaseListener;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslLexer;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslParser;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Processor implements ProcessorInterface {

    private static final String GP = "GP";
    private static final String GRAPH_PATH = "GRAPH_PATH";

    private final MyDslBaseListener dslListener;
    private final MCg3WorkflowRoutineApiInterface workflowRoutineApi;

    public Processor(
            MyDslBaseListener dslListener,
            MCg3WorkflowRoutineApiInterface workflowRoutineApi
    ) {
        this.dslListener = dslListener;
        this.workflowRoutineApi = workflowRoutineApi;
    }

    @Override
    public void process(String commandLineInput)
            throws MCg3ApiOperationIncompleteException {

        try {
            String processedCommandLineInput =
                    templateProcessor(commandLineInput);

            CharStream charStream =
                    CharStreams.fromString(processedCommandLineInput);

            MyDslLexer lexer =
                    new MyDslLexer(charStream);

            CommonTokenStream tokens =
                    new CommonTokenStream(lexer);

            MyDslParser parser =
                    new MyDslParser(tokens);

            ParseTree tree =
                    parser.script();

            ParseTreeWalker.DEFAULT.walk(dslListener, tree);

        } catch (MCg3ApiOperationIncompleteException e) {
            throw e;
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    public String templateProcessor(String input)
            throws MCg3ApiOperationIncompleteException {

        if (input == null) {
            throw new MCg3ApiOperationIncompleteException(
                    new IllegalArgumentException("DSL input must not be null")
            );
        }

        int expressionStart = input.indexOf("${");

        if (expressionStart < 0) {
            return input;
        }

        StringBuilder result =
                new StringBuilder(input.length() + 16);

        int position = 0;

        while (expressionStart >= 0) {

            result.append(input, position, expressionStart);

            int expressionEnd =
                    findExpressionEnd(input, expressionStart + 2);

            if (expressionEnd < 0) {
                throw malformedExpression(
                        expressionStart,
                        "Missing closing '}'"
                );
            }

            appendExpression(
                    result,
                    input,
                    expressionStart + 2,
                    expressionEnd
            );

            position = expressionEnd + 1;
            expressionStart = input.indexOf("${", position);
        }

        result.append(input, position, input.length());

        return result.toString();
    }

    private void appendExpression(
            StringBuilder result,
            String source,
            int start,
            int end
    ) throws MCg3ApiOperationIncompleteException {

        int position = skipWhitespace(source, start, end);

        int functionStart = position;

        while (position < end
                && isIdentifierCharacter(source.charAt(position))) {
            position++;
        }

        if (functionStart == position) {
            throw malformedExpression(
                    start - 2,
                    "Missing function name"
            );
        }

        String functionName =
                source.substring(functionStart, position);

        position = skipWhitespace(source, position, end);

        if (position >= end || source.charAt(position) != '(') {
            throw malformedExpression(
                    start - 2,
                    "Expected '('"
            );
        }

        position++;

        position = skipWhitespace(source, position, end);

        String argument = null;

        if (position < end && source.charAt(position) != ')') {

            ParseResult parseResult =
                    parseArgument(source, position, end);

            argument = parseResult.value();
            position = parseResult.nextPosition();

            position = skipWhitespace(source, position, end);
        }

        if (position >= end || source.charAt(position) != ')') {
            throw malformedExpression(
                    start - 2,
                    "Expected ')'"
            );
        }

        position++;

        position = skipWhitespace(source, position, end);

        if (position != end) {
            throw malformedExpression(
                    start - 2,
                    "Unexpected characters after function call"
            );
        }

        if (!GP.equals(functionName)
                && !GRAPH_PATH.equals(functionName)) {

            throw malformedExpression(
                    start - 2,
                    "Unknown function: " + functionName
            );
        }

        result.append(resolveGraphPath(argument));
    }

    private String resolveGraphPath(String nodeName)
            throws MCg3ApiOperationIncompleteException {

        String graphPath =
                workflowRoutineApi
                        .getExecutionContext()
                        .getPipelineExecutionInfo()
                        .getCurrentNodeExecutionGraphPath()
                        .getGraphPath();

        if (graphPath == null) {
            throw new MCg3ApiOperationIncompleteException(
                    new IllegalStateException(
                            "Current graph path must not be null"
                    )
            );
        }

        if (nodeName == null) {
            return Integer.toString(graphPath.hashCode());
        }

        if (nodeName.isEmpty()) {
            throw new MCg3ApiOperationIncompleteException(
                    new IllegalArgumentException(
                            "Node name must not be empty"
                    )
            );
        }

        return Integer.toString(
                hashPath(graphPath, nodeName)
        );
    }

    private static int hashPath(
            String graphPath,
            String nodeName
    ) {

        int hash = 0;

        for (int i = 0; i < graphPath.length(); i++) {
            hash = 31 * hash + graphPath.charAt(i);
        }

        hash = 31 * hash + '.';

        for (int i = 0; i < nodeName.length(); i++) {
            hash = 31 * hash + nodeName.charAt(i);
        }

        return hash;
    }

    private static ParseResult parseArgument(
            String source,
            int position,
            int end
    ) throws MCg3ApiOperationIncompleteException {

        char quote = source.charAt(position);

        if (quote != '"' && quote != '\'') {
            throw malformedExpression(
                    position,
                    "Argument must be a quoted string"
            );
        }

        position++;

        int valueStart = position;
        StringBuilder escapedValue = null;

        while (position < end) {

            char ch = source.charAt(position);

            if (ch == quote) {

                if (escapedValue == null) {
                    return new ParseResult(
                            source.substring(valueStart, position),
                            position + 1
                    );
                }

                escapedValue.append(
                        source,
                        valueStart,
                        position
                );

                return new ParseResult(
                        escapedValue.toString(),
                        position + 1
                );
            }

            if (ch == '\\') {

                if (escapedValue == null) {
                    escapedValue = new StringBuilder();
                }

                escapedValue.append(
                        source,
                        valueStart,
                        position
                );

                position++;

                if (position >= end) {
                    throw malformedExpression(
                            position,
                            "Invalid escape sequence"
                    );
                }

                char escaped = source.charAt(position);

                switch (escaped) {
                    case '\\' -> escapedValue.append('\\');
                    case '"' -> escapedValue.append('"');
                    case '\'' -> escapedValue.append('\'');
                    case 'n' -> escapedValue.append('\n');
                    case 'r' -> escapedValue.append('\r');
                    case 't' -> escapedValue.append('\t');

                    default -> throw malformedExpression(
                            position,
                            "Unsupported escape sequence: \\" + escaped
                    );
                }

                position++;
                valueStart = position;

                continue;
            }

            position++;
        }

        throw malformedExpression(
                position,
                "Unterminated string argument"
        );
    }

    private static int findExpressionEnd(
            String source,
            int position
    ) {

        char quote = 0;
        boolean escaped = false;

        for (int i = position; i < source.length(); i++) {

            char ch = source.charAt(i);

            if (quote != 0) {

                if (escaped) {
                    escaped = false;
                    continue;
                }

                if (ch == '\\') {
                    escaped = true;
                    continue;
                }

                if (ch == quote) {
                    quote = 0;
                }

                continue;
            }

            if (ch == '"' || ch == '\'') {
                quote = ch;
                continue;
            }

            if (ch == '}') {
                return i;
            }
        }

        return -1;
    }

    private static int skipWhitespace(
            String source,
            int position,
            int end
    ) {

        while (position < end
                && Character.isWhitespace(
                source.charAt(position)
        )) {
            position++;
        }

        return position;
    }

    private static boolean isIdentifierCharacter(char ch) {
        return Character.isLetterOrDigit(ch)
                || ch == '_';
    }

    private static MCg3ApiOperationIncompleteException malformedExpression(
            int position,
            String message
    ) {

        return new MCg3ApiOperationIncompleteException(
                new IllegalArgumentException(
                        message + " at position " + position
                )
        );
    }

    private record ParseResult(
            String value,
            int nextPosition
    ) {
    }
}