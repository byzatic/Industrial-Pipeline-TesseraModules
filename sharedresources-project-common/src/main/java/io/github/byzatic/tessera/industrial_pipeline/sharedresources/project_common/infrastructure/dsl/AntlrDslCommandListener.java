package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.infrastructure.dsl;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.exception.RoutineExecutionException;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.dsl.DslCommandHandler;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslBaseListener;
import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl.MyDslParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AntlrDslCommandListener extends MyDslBaseListener {
    private final DslCommandHandler commandHandler;

    public AntlrDslCommandListener(DslCommandHandler commandHandler) {
        this.commandHandler = Objects.requireNonNull(commandHandler, "commandHandler must not be null");
    }

    @Override
    public void enterGetCommand(MyDslParser.GetCommandContext context) {
        String childName = context.source.child == null
                ? null
                : context.source.child.getText();
        String modifier = context.modifier == null
                ? "local"
                : context.modifier.mod.getText();
        String dataId = context.dataId.getText();
        String alias = context.alias == null
                ? dataId
                : context.alias.alias.getText();
        invoke(() -> commandHandler.getData(
                childName,
                context.storage.getText(),
                "global".equals(modifier),
                dataId,
                alias
        ));
    }

    @Override
    public void enterProcessCommand(MyDslParser.ProcessCommandContext context) {
        List<String> arguments = new ArrayList<>();
        if (context.arguments != null && context.arguments.argList() != null) {
            for (TerminalNode argument : context.arguments.argList().STRING()) {
                arguments.add(unquote(argument.getText()));
            }
        }
        invoke(() -> commandHandler.processData(
                context.function.getText(),
                arguments,
                context.resultId.getText()
        ));
    }

    @Override
    public void enterPutCommand(MyDslParser.PutCommandContext context) {
        String modifier = context.modifier == null
                ? "local"
                : context.modifier.mod.getText();
        invoke(() -> commandHandler.putData(
                context.localDataId.getText(),
                context.storage.getText(),
                "global".equals(modifier),
                context.dataId.getText()
        ));
    }

    private static String unquote(String value) {
        return value.length() >= 2 ? value.substring(1, value.length() - 1) : value;
    }

    private static void invoke(Command command) {
        try {
            command.run();
        } catch (RoutineExecutionException e) {
            throw new DslCommandRuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface Command {
        void run() throws RoutineExecutionException;
    }
}
