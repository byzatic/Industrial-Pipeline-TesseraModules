package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators;

import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.config_loader.ConfigLoader;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.config_loader.model.GeneratorConfig;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pipeline_segment.Gradients;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pipeline_segment.PipelineSegment;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pipeline_segment.SegmentDataQuality;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pump_unit.Hydraulics;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pump_unit.PumpUnit;
import io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.universal_valves.Valves;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Factory для генераторов технологического объекта.
 * - единая точка сборки
 * - конфигурируемые номиналы/уставки
 * - быстрый дефолт для эмулятора
 */
public final class GeneratorsFactory implements GeneratorsFactoryInterface {

    public static final class Config {
        public final PipelineSegment.Nominals pipelineSegmentNominals;
        public final Hydraulics.Nominals hydraulicsNominals;
        public final PumpUnit.Nominals pumpUnitNominals;

        private Config(Builder b) {
            this.pipelineSegmentNominals = b.pipelineSegmentNominals;
            this.hydraulicsNominals = b.hydraulicsNominals;
            this.pumpUnitNominals = b.pumpUnitNominals;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private PipelineSegment.Nominals pipelineSegmentNominals = new PipelineSegment.Nominals(
                    0.6f,   // ΔP_seg_nom, MPa
                    0.3f,   // ΔP_norm_nom, MPa/km
                    0.25f   // R_seg_nom, MPa·h/m3
            );

            private Hydraulics.Nominals hydraulicsNominals = new Hydraulics.Nominals(
                    4.0f,    // Pin_nom, MPa
                    1.5f,    // ΔP_nom, MPa
                    2500f,   // Q_nom, m3/h
                    5.5f,    // Pout_nom, MPa
                    25f      // T_nom, °C
            );

            private PumpUnit.Nominals pumpUnitNominals = new PumpUnit.Nominals(
                    180f,  // I_nom, A
                    400f,  // U_nom, V
                    50f,   // N_set, Hz
                    160f   // P_nom, kW
            );

            public Builder pipelineSegmentNominals(PipelineSegment.Nominals v) {
                this.pipelineSegmentNominals = v;
                return this;
            }

            public Builder hydraulicsNominals(Hydraulics.Nominals v) {
                this.hydraulicsNominals = v;
                return this;
            }

            public Builder pumpUnitNominals(PumpUnit.Nominals v) {
                this.pumpUnitNominals = v;
                return this;
            }

            public Config build() {
                return new Config(this);
            }
        }
    }

    private Config config;

    public GeneratorsFactory() {
        this.config = Config.builder().build();
    }

    @Override
    public GeneratorInterface createFromConfigFile(Path path) throws IOException {
        GeneratorConfig generatorConfig = ConfigLoader.readConfig(path);

        return switch (generatorConfig.getGeneratorName()) {
            // pipeline_segment
            case "RATE_OF_PRESSURE_CHANGE" -> getGradients(generatorConfig);
            case "RATE_OF_CHANGE_OF_FLOW" -> getGradients(generatorConfig);
            case "RATE_OF_TEMPERATURE_CHANGE" -> getGradients(generatorConfig);
            case "DP_SEG" -> getPipelineSegment(generatorConfig);
            case "DP_NORM" -> getPipelineSegment(generatorConfig);
            case "DQ_SEG" -> getPipelineSegment(generatorConfig);
            case "DT_SEG" -> getPipelineSegment(generatorConfig);
            case "R_SEG" -> getPipelineSegment(generatorConfig);
            case "ERR_P" -> getPipelineSegment(generatorConfig);
            case "MISSING_COUNT" -> getSegmentDataQuality(generatorConfig);
            case "TIME_DESYNC" -> getSegmentDataQuality(generatorConfig);
            case "CORR_MISMATCH" -> getSegmentDataQuality(generatorConfig);
            // pump_unit
            case "P_IN" -> getHydraulics(generatorConfig);
            case "DP" -> getHydraulics(generatorConfig);
            case "Q" -> getHydraulics(generatorConfig);
            case "P_OUT" -> getHydraulics(generatorConfig);
            case "T" -> getHydraulics(generatorConfig);
            case "V_RMS" -> getPumpUnit(generatorConfig);
            case "T_BEARING" -> getPumpUnit(generatorConfig);
            case "I" -> getPumpUnit(generatorConfig);
            case "U" -> getPumpUnit(generatorConfig);
            case "N" -> getPumpUnit(generatorConfig);
            case "P_EL" -> getPumpUnit(generatorConfig);
            case "VALVE" -> getValves(generatorConfig);
            default -> throw new IllegalStateException("Unexpected value: " + generatorConfig.getGeneratorName());
        };
    }

    private GeneratorInterface getGradients(GeneratorConfig generatorConfig) {

        Gradients.Type type = null;

        if (generatorConfig.getGeneratorName() != null && !generatorConfig.getGeneratorName().isEmpty()) {
            type = switch (generatorConfig.getGeneratorName()) {
                case "RATE_OF_PRESSURE_CHANGE" -> Gradients.Type.RATE_OF_PRESSURE_CHANGE;
                case "RATE_OF_CHANGE_OF_FLOW" -> Gradients.Type.RATE_OF_CHANGE_OF_FLOW;
                case "RATE_OF_TEMPERATURE_CHANGE" -> Gradients.Type.RATE_OF_TEMPERATURE_CHANGE;
                default ->
                        throw new IllegalStateException("Unexpected value of GeneratorName: " + generatorConfig.getGeneratorName());
            };
        } else {
            throw new IllegalStateException("Unexpected value of GeneratorName: " + generatorConfig);
        }

        Gradients.SpecialValueRange range = null;

        if (generatorConfig.getSpecialValueRange() != null && !generatorConfig.getSpecialValueRange().isEmpty()) {
            range = switch (generatorConfig.getSpecialValueRange()) {
                case "OK" -> Gradients.SpecialValueRange.OK;
                case "WARNING" -> Gradients.SpecialValueRange.WARN;
                case "CRITICAL" -> Gradients.SpecialValueRange.CRITICAL;
                default ->
                        throw new IllegalStateException("Unexpected value of SpecialValueRange: " + generatorConfig.getSpecialValueRange());
            };
        }

        GeneratorInterface generator = null;

        if (range == null) {
            generator = gradients(type);
        } else {
            generator = gradients(type, range);
        }

        return generator;
    }

    private GeneratorInterface getPipelineSegment(GeneratorConfig generatorConfig) {
        PipelineSegment.Type type = null;

        if (generatorConfig.getGeneratorName() != null && !generatorConfig.getGeneratorName().isEmpty()) {
            type = switch (generatorConfig.getGeneratorName()) {
                case "DP_NORM" -> PipelineSegment.Type.DP_NORM;
                case "DP_SEG" -> PipelineSegment.Type.DP_SEG;
                case "DQ_SEG" -> PipelineSegment.Type.DQ_SEG;
                case "DT_SEG" -> PipelineSegment.Type.DT_SEG;
                case "R_SEG" -> PipelineSegment.Type.R_SEG;
                case "ERR_P" -> PipelineSegment.Type.ERR_P;
                default ->
                        throw new IllegalStateException("Unexpected value of GeneratorName: " + generatorConfig.getGeneratorName());
            };
        } else {
            throw new IllegalStateException("Unexpected value of GeneratorName: " + generatorConfig);
        }

        PipelineSegment.SpecialValueRange range = null;

        if (generatorConfig.getSpecialValueRange() != null && !generatorConfig.getSpecialValueRange().isEmpty()) {
            range = switch (generatorConfig.getSpecialValueRange()) {
                case "OK" -> PipelineSegment.SpecialValueRange.OK;
                case "WARNING" -> PipelineSegment.SpecialValueRange.WARN;
                case "CRITICAL" -> PipelineSegment.SpecialValueRange.CRITICAL;
                default ->
                        throw new IllegalStateException("Unexpected value of SpecialValueRange: " + generatorConfig.getSpecialValueRange());
            };
        }

        GeneratorInterface generator = null;

        // TODO: тут должена быть загрузка номиналов из фала, сейчас из дефолтов (config)!
        if (range == null) {
            generator = pipelineSegment(config, type);
        } else {
            generator = pipelineSegment(config, type, range);
        }

        return generator;
    }

    private GeneratorInterface getSegmentDataQuality(GeneratorConfig generatorConfig) {
        SegmentDataQuality.Type type = null;

        if (generatorConfig.getGeneratorName() != null && !generatorConfig.getGeneratorName().isEmpty()) {
            type = switch (generatorConfig.getGeneratorName()) {
                case "CORR_MISMATCH" -> SegmentDataQuality.Type.CORR_MISMATCH;
                case "MISSING_COUNT" -> SegmentDataQuality.Type.MISSING_COUNT;
                case "TIME_DESYNC" -> SegmentDataQuality.Type.TIME_DESYNC;
                default ->
                        throw new IllegalStateException("Unexpected value of GeneratorName: " + generatorConfig.getGeneratorName());
            };
        } else {
            throw new IllegalStateException("Unexpected value of GeneratorName: " + generatorConfig);
        }

        SegmentDataQuality.SpecialValueRange range = null;

        if (generatorConfig.getSpecialValueRange() != null && !generatorConfig.getSpecialValueRange().isEmpty()) {
            range = switch (generatorConfig.getSpecialValueRange()) {
                case "OK" -> SegmentDataQuality.SpecialValueRange.OK;
                case "WARNING" -> SegmentDataQuality.SpecialValueRange.WARN;
                case "CRITICAL" -> SegmentDataQuality.SpecialValueRange.CRITICAL;
                default ->
                        throw new IllegalStateException("Unexpected value of SpecialValueRange: " + generatorConfig.getSpecialValueRange());
            };
        }

        GeneratorInterface generator = null;

        // TODO: тут должена быть загрузка номиналов из фала, сейчас из дефолтов (config)!
        if (range == null) {
            generator = segmentDataQuality(type);
        } else {
            generator = segmentDataQuality(type, range);
        }

        return generator;
    }

    private GeneratorInterface getHydraulics(GeneratorConfig generatorConfig) {
        Hydraulics.Type type = null;

        if (generatorConfig.getGeneratorName() != null && !generatorConfig.getGeneratorName().isEmpty()) {
            type = switch (generatorConfig.getGeneratorName()) {
                case "DP" -> Hydraulics.Type.DP;
                case "T" -> Hydraulics.Type.T;
                case "Q" -> Hydraulics.Type.Q;
                case "P_IN" -> Hydraulics.Type.P_IN;
                case "P_OUT" -> Hydraulics.Type.P_OUT;
                default ->
                        throw new IllegalStateException("Unexpected value of GeneratorName: " + generatorConfig.getGeneratorName());
            };
        } else {
            throw new IllegalStateException("Unexpected value of GeneratorName: " + generatorConfig);
        }

        Hydraulics.SpecialValueRange range = null;

        if (generatorConfig.getSpecialValueRange() != null && !generatorConfig.getSpecialValueRange().isEmpty()) {
            range = switch (generatorConfig.getSpecialValueRange()) {
                case "OK" -> Hydraulics.SpecialValueRange.OK;
                case "WARNING" -> Hydraulics.SpecialValueRange.WARN;
                case "CRITICAL" -> Hydraulics.SpecialValueRange.CRITICAL;
                default ->
                        throw new IllegalStateException("Unexpected value of SpecialValueRange: " + generatorConfig.getSpecialValueRange());
            };
        }

        GeneratorInterface generator = null;

        // TODO: тут должена быть загрузка номиналов из фала, сейчас из дефолтов (config)!
        if (range == null) {
            generator = hydraulics(config, type);
        } else {
            generator = hydraulics(config, type, range);
        }

        return generator;
    }

    private GeneratorInterface getPumpUnit(GeneratorConfig generatorConfig) {
        PumpUnit.Type type = null;

        if (generatorConfig.getGeneratorName() != null && !generatorConfig.getGeneratorName().isEmpty()) {
            type = switch (generatorConfig.getGeneratorName()) {
                case "I" -> PumpUnit.Type.I;
                case "N" -> PumpUnit.Type.N;
                case "U" -> PumpUnit.Type.U;
                case "P_EL" -> PumpUnit.Type.P_EL;
                case "V_RMS" -> PumpUnit.Type.V_RMS;
                case "T_BEARING" -> PumpUnit.Type.T_BEARING;
                default ->
                        throw new IllegalStateException("Unexpected value of GeneratorName: " + generatorConfig.getGeneratorName());
            };
        } else {
            throw new IllegalStateException("Unexpected value of GeneratorName: " + generatorConfig);
        }

        PumpUnit.SpecialValueRange range = null;

        if (generatorConfig.getSpecialValueRange() != null && !generatorConfig.getSpecialValueRange().isEmpty()) {
            range = switch (generatorConfig.getSpecialValueRange()) {
                case "OK" -> PumpUnit.SpecialValueRange.OK;
                case "WARNING" -> PumpUnit.SpecialValueRange.WARN;
                case "CRITICAL" -> PumpUnit.SpecialValueRange.CRITICAL;
                default ->
                        throw new IllegalStateException("Unexpected value of SpecialValueRange: " + generatorConfig.getSpecialValueRange());
            };
        }

        GeneratorInterface generator = null;

        // TODO: тут должена быть загрузка номиналов из фала, сейчас из дефолтов (config)!
        if (range == null) {
            generator = pumpUnit(config, type);
        } else {
            generator = pumpUnit(config, type, range);
        }

        return generator;
    }

    private GeneratorInterface getValves(GeneratorConfig generatorConfig) {
        Valves.SpecialValueRange range = null;

        if (generatorConfig.getSpecialValueRange() != null && !generatorConfig.getSpecialValueRange().isEmpty()) {
            range = switch (generatorConfig.getSpecialValueRange()) {
                case "OK" -> Valves.SpecialValueRange.OK;
                case "WARNING" -> Valves.SpecialValueRange.WARN;
                case "CRITICAL" -> Valves.SpecialValueRange.CRITICAL;
                default ->
                        throw new IllegalStateException("Unexpected value of SpecialValueRange: " + generatorConfig.getSpecialValueRange());
            };
        }

        GeneratorInterface generator = null;

        // TODO: тут должена быть загрузка номиналов из фала, сейчас из дефолтов (config)!
        if (range == null) {
            generator = valves();
        } else {
            generator = valves(range);
        }

        return generator;
    }


    // ===== LOADING =====

    // ===== pipeline_segment =====

    public Gradients gradients(Gradients.Type type) {
        return new Gradients(type);
    }

    public Gradients gradients(Gradients.Type type, Gradients.SpecialValueRange range) {
        return new Gradients(type, range);
    }

    public PipelineSegment pipelineSegment(Config config, PipelineSegment.Type type) {
        return new PipelineSegment(config.pipelineSegmentNominals, type);
    }

    public PipelineSegment pipelineSegment(Config config, PipelineSegment.Type type, PipelineSegment.SpecialValueRange range) {
        return new PipelineSegment(config.pipelineSegmentNominals, type, range);
    }

    public SegmentDataQuality segmentDataQuality(SegmentDataQuality.Type type) {
        return new SegmentDataQuality(type);
    }

    public SegmentDataQuality segmentDataQuality(SegmentDataQuality.Type type, SegmentDataQuality.SpecialValueRange range) {
        return new SegmentDataQuality(type, range);
    }

    // ===== pump_unit =====

    public Hydraulics hydraulics(Config config, Hydraulics.Type type) {
        return new Hydraulics(config.hydraulicsNominals, type);
    }

    public Hydraulics hydraulics(Config config, Hydraulics.Type type, Hydraulics.SpecialValueRange range) {
        return new Hydraulics(config.hydraulicsNominals, type, range);
    }

    public PumpUnit pumpUnit(Config config, PumpUnit.Type type) {
        return new PumpUnit(config.pumpUnitNominals, type);
    }

    public PumpUnit pumpUnit(Config config, PumpUnit.Type type, PumpUnit.SpecialValueRange range) {
        return new PumpUnit(config.pumpUnitNominals, type, range);
    }

    public Valves valves() {
        return new Valves();
    }

    public Valves valves(Valves.SpecialValueRange range) {
        return new Valves(range);
    }

    public Config config() {
        return config;
    }

}