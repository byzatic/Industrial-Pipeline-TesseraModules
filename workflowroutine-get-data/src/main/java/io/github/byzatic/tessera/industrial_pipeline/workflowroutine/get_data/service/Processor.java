package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.service;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslBaseListener;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslLexer;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslParser;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.domain.service.ProcessorInterface;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Processor implements ProcessorInterface {
    private final MyDslBaseListener dslListener;
    private final MCg3WorkflowRoutineApiInterface workflowRoutineApi;

    public Processor(MyDslBaseListener dslListener, MCg3WorkflowRoutineApiInterface workflowRoutineApi) {
        this.dslListener = dslListener;
        this.workflowRoutineApi = workflowRoutineApi;
    }

    @Override
    public void process(String commandLineInput) throws MCg3ApiOperationIncompleteException {
        try {
            CharStream charStream = CharStreams.fromString(commandLineInput);
            MyDslLexer lexer = new MyDslLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MyDslParser parser = new MyDslParser(tokens);
            ParseTree tree = parser.script();
            ParseTreeWalker.DEFAULT.walk(dslListener, tree);
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }
}
