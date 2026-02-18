package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.processing_status.processor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateMethodModelEx;
import io.github.byzatic.commons.ObjectsUtils;
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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

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
            String processedCommandLineInput = templateProcessor(commandLineInput);

            CharStream charStream = CharStreams.fromString(processedCommandLineInput);
            MyDslLexer lexer = new MyDslLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MyDslParser parser = new MyDslParser(tokens);

            ParseTree tree = parser.script();
            ParseTreeWalker.DEFAULT.walk(dslListener, tree);
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    public String templateProcessor(String dslString) throws Exception {
        // Настройка конфигурации FreeMarker
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Загружаем шаблон из строки
        Template template = new Template("dslTemplate", new StringReader(dslString), cfg);

        // Подготавливаем данные
        Map<String, Object> data = new HashMap<>();
        data.put("GP", new GetPathFunction(workflowRoutineApi));
        data.put("GRAPH_PATH", new GetPathFunction(workflowRoutineApi));

        // Рендерим шаблон
        StringWriter out = new StringWriter();
        template.process(data, out);

        return out.toString();
    }

    // Функция для вызова из шаблона
    static class GetPathFunction implements TemplateMethodModelEx {
        private final MCg3WorkflowRoutineApiInterface workflowRoutineApi;

        public GetPathFunction(MCg3WorkflowRoutineApiInterface workflowRoutineApi) {
            this.workflowRoutineApi = workflowRoutineApi;
        }

        @Override
        public String exec(@SuppressWarnings("rawtypes") java.util.List arguments) {
            try {
                String finalValue = null;
                if (arguments.isEmpty()) {
                    finalValue = String.valueOf(workflowRoutineApi.getExecutionContext().getPipelineExecutionInfo().getCurrentNodeExecutionGraphPath().getGraphPath().hashCode());
                } else {
                    Object arg = arguments.get(0);
                    String nodeName = arg != null ? arg.toString() : null;
                    ObjectsUtils.requireNonNull(nodeName, new IllegalArgumentException("Illegal Node name" + nodeName));
                    String path = workflowRoutineApi.getExecutionContext().getPipelineExecutionInfo().getCurrentNodeExecutionGraphPath().getGraphPath();
                    path = path + "." + nodeName;
                    finalValue = String.valueOf(path.hashCode());
                }
                return finalValue;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
