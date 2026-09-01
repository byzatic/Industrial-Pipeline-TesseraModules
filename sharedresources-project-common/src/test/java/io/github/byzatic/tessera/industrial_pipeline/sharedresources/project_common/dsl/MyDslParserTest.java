package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.dsl.DslCommandHandler;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.dsl.AntlrDslCommandListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MyDslParserTest {

    @Test
    public void processCommandAcceptsArgumentsWithoutTrailingComma() {
        assertParsesWithoutErrors(
                "PROCESS FUNCTION ModifyMetric(\"DataId=source\", \"PromLabel_site=test\") RETURN result;"
        );
    }

    @Test
    public void processCommandAcceptsArgumentsWithTrailingComma() {
        assertParsesWithoutErrors(
                "PROCESS FUNCTION ModifyMetric(\"DataId=source\", \"PromLabel_site=test\",) RETURN result;"
        );
    }

    @Test
    public void putCommandWithoutModifierDefaultsToLocal() {
        RecordingCommandHandler commandHandler = new RecordingCommandHandler();
        MyDslParser parser = parserFor(
                "PUT DATA enriched TO STORAGE metrics BY DATA ID pressure;"
        );

        ParseTreeWalker.DEFAULT.walk(
                new AntlrDslCommandListener(commandHandler),
                parser.script()
        );

        assertEquals("enriched", commandHandler.localDataId);
        assertEquals("metrics", commandHandler.storageId);
        assertFalse(commandHandler.global);
        assertEquals("pressure", commandHandler.dataId);
    }

    private static void assertParsesWithoutErrors(String source) {
        CollectingErrorListener errorListener = new CollectingErrorListener();
        MyDslParser parser = parserFor(source, errorListener);
        parser.script();

        assertTrue(
                "Expected valid DSL, but got: " + errorListener.messages,
                errorListener.messages.isEmpty()
        );
    }

    private static MyDslParser parserFor(String source) {
        return parserFor(source, new CollectingErrorListener());
    }

    private static MyDslParser parserFor(String source, CollectingErrorListener errorListener) {
        MyDslLexer lexer = new MyDslLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        MyDslParser parser = new MyDslParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        return parser;
    }

    private static final class RecordingCommandHandler implements DslCommandHandler {
        private String localDataId;
        private String storageId;
        private boolean global;
        private String dataId;

        @Override
        public void getData(String childName, String storageId, boolean global, String dataId, String alias) {
            throw new AssertionError("Unexpected GET command");
        }

        @Override
        public void processData(String functionName, List<String> arguments, String resultId) {
            throw new AssertionError("Unexpected PROCESS command");
        }

        @Override
        public void putData(String localDataId, String storageId, boolean global, String dataId)
                throws RoutineExecutionException {
            this.localDataId = localDataId;
            this.storageId = storageId;
            this.global = global;
            this.dataId = dataId;
        }
    }

    private static final class CollectingErrorListener extends BaseErrorListener {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String message,
                RecognitionException exception
        ) {
            messages.add(line + ":" + charPositionInLine + " " + message);
        }
    }
}
