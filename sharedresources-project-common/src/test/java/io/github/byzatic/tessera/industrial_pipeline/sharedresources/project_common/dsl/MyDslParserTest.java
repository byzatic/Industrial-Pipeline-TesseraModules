package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    private static void assertParsesWithoutErrors(String source) {
        CollectingErrorListener errorListener = new CollectingErrorListener();
        MyDslLexer lexer = new MyDslLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        MyDslParser parser = new MyDslParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        parser.script();

        assertTrue(
                "Expected valid DSL, but got: " + errorListener.messages,
                errorListener.messages.isEmpty()
        );
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
