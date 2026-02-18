package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.config_loader.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeneratorConfig {

    @SerializedName("generator_name")
    private String generatorName;

    @SerializedName("nominals")
    private List<NominalsItem> nominalsItems;

    @SerializedName("special_value_range")
    private String specialValueRange;

    public String getGeneratorName() {
        return generatorName;
    }

    public List<NominalsItem> getNominals() {
        return nominalsItems;
    }

    public String getSpecialValueRange() {
        return specialValueRange;
    }

    @Override
    public String toString() {
        return
                "Config{" +
                        "generator_name = '" + generatorName + '\'' +
                        ",nominals = '" + nominalsItems + '\'' +
                        ",special_value_range = '" + specialValueRange + '\'' +
                        "}";
    }
}