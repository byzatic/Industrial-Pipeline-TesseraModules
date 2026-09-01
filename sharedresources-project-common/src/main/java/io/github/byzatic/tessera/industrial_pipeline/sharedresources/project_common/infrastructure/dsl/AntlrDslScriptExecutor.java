package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.dsl;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.dsl.DslCommandHandler;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.dsl.DslScriptExecutor;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslLexer;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslParser;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AntlrDslScriptExecutor implements DslScriptExecutor {
    private final DslCommandHandler commandHandler;
    private final GraphPathTemplateProcessor templateProcessor;

    public AntlrDslScriptExecutor(
            DslCommandHandler commandHandler,
            GraphPathTemplateProcessor templateProcessor
    ) {
        this.commandHandler = Objects.requireNonNull(commandHandler, "commandHandler must not be null");
        this.templateProcessor = Objects.requireNonNull(templateProcessor, "templateProcessor must not be null");
    }

    @Override
    public void execute(String script) throws RoutineExecutionException {
        String processedScript = templateProcessor.process(script);
        SyntaxErrorCollector errors = new SyntaxErrorCollector();
        MyDslLexer lexer = new MyDslLexer(CharStreams.fromString(processedScript));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errors);
        MyDslParser parser = new MyDslParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errors);
        ParseTree tree = parser.script();
        if (!errors.messages.isEmpty()) {
            throw new RoutineExecutionException("Invalid DSL: " + String.join("; ", errors.messages));
        }
        try {
            ParseTreeWalker.DEFAULT.walk(new AntlrDslCommandListener(commandHandler), tree);
        } catch (DslCommandRuntimeException e) {
            throw e.getCause();
        }
    }

    private static final class SyntaxErrorCollector extends BaseErrorListener {
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
