package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.graph_lifting_data.processor.dsl;

import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.graph_lifting_data.processor.process_engine.ProcessEngineInterface;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslBaseListener;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslParser;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MyDslCustomListener extends MyDslBaseListener {
    private final static Logger logger = LoggerFactory.getLogger(MyDslCustomListener.class);
    private final ProcessEngineInterface processEngine;

    public MyDslCustomListener(ProcessEngineInterface processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    public void enterGetCommand(MyDslParser.GetCommandContext ctx) throws MCg3ApiOperationIncompleteException {
        String storage = ctx.storage.getText();
        String dataId = ctx.dataId.getText();
        String modifier = ctx.modifierClause() != null ? ctx.modifierClause().mod.getText() : "none";
        String alias = ctx.aliasClause() != null ? ctx.aliasClause().alias.getText() : dataId;

        boolean isGlobal = "global".equals(modifier);
        String childName = null;

        if (ctx.sourceType().getText().equals("SELF")) {
            logger.debug("[GET] FROM SELF STORAGE '{}' (modifier={}) BY DATA ID '{}' AS '{}'",
                    storage, modifier, dataId, alias);
        } else if (ctx.sourceType().child != null) {
            childName = ctx.sourceType().child.getText();
            logger.debug("[GET] FROM CHILD '{}' STORAGE '{}' (modifier={}) BY DATA ID '{}' AS '{}'",
                    childName, storage, modifier, dataId, alias);
        }

        processEngine.getData(childName, storage, isGlobal, dataId, alias);
    }

    @Override
    public void enterProcessCommand(MyDslParser.ProcessCommandContext ctx) throws MCg3ApiOperationIncompleteException {
        String function = ctx.function.getText();
        String resultId = ctx.resultId.getText();

        List<String> args = new ArrayList<>();
        if (ctx.argsClause() != null && ctx.argsClause().argList() != null) {
            for (TerminalNode argNode : ctx.argsClause().argList().STRING()) {
                args.add(unquote(argNode.getText()));
            }
        }

        logger.debug("[PROCESS] FUNCTION '{}' ARGS={} RETURN '{}'", function, args, resultId);

        processEngine.processData(function, args, resultId);
    }

    @Override
    public void enterPutCommand(MyDslParser.PutCommandContext ctx) throws MCg3ApiOperationIncompleteException {
        String localDataId = ctx.localDataId.getText();
        String storage = ctx.storage.getText();
        String dataId = ctx.dataId.getText();
        String modifier = ctx.modifierClause() != null ? ctx.modifierClause().mod.getText() : "none";

        boolean isGlobal = "global".equals(modifier);

        logger.debug("[PUT] DATA '{}' TO STORAGE '{}' (modifier={}) BY DATA ID '{}'",
                localDataId, storage, modifier, dataId);

        processEngine.putData(localDataId, storage, isGlobal, dataId);
    }

    private String unquote(String quotedString) {
        if (quotedString.length() >= 2 && quotedString.startsWith("\"") && quotedString.endsWith("\"")) {
            return quotedString.substring(1, quotedString.length() - 1);
        }
        return quotedString;
    }
}