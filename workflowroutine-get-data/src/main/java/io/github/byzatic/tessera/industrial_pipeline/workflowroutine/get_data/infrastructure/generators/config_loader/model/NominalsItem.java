package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.config_loader.model;

import com.google.gson.annotations.SerializedName;

public class NominalsItem {

    @SerializedName("name")
    private String name;

    @SerializedName("value")
    private String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return
                "NominalsItem{" +
                        "name = '" + name + '\'' +
                        ",value = '" + value + '\'' +
                        "}";
    }
}