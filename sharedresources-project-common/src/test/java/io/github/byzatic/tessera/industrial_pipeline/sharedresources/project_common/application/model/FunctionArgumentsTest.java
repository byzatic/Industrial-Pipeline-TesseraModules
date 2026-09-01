package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.application.model;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FunctionArgumentsTest {

    @Test
    public void preservesRepeatedArgumentsInDeclarationOrder() {
        FunctionArguments arguments = FunctionArguments.parse(List.of(
                "DataId=first",
                "DataId=second",
                "DataId=third"
        ));

        assertEquals(List.of("first", "second", "third"), arguments.all("DataId"));
    }
}
