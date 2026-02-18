package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pipeline_segment;

import java.util.concurrent.ThreadLocalRandom;

public class Gradients implements PipelineSegmentGeneratorInterface {
//    dP/dt — скорость изменения давления
//    Тип данных: float
//    Единицы: МПа/с
//    Диапазон генерации: −1.0 … +1.0
//    OK: −0.05 … +0.05
//    WARN: −0.05 … −0.2 или +0.05 … +0.2
//    CRITICAL: < −0.2 или > +0.2
//
//    dQ/dt — скорость изменения расхода
//    Тип данных: float
//    Единицы: м³/ч·с
//    Диапазон генерации: −500 … +500
//    OK: −20 … +20
//    WARN: −20 … −100 или +20 … +100
//    CRITICAL: < −100 или > +100
//
//    dT/dt — скорость изменения температуры
//    Тип данных: float
//    Единицы: °C/с
//    Диапазон генерации: −5 … +5
//    OK: −0.1 … +0.1
//    WARN: −0.1 … −0.5 или +0.1 … +0.5
//    CRITICAL: < −0.5 или > +0.5

    public enum Type {
        RATE_OF_PRESSURE_CHANGE,     // dP/dt, МПа/с
        RATE_OF_CHANGE_OF_FLOW,      // dQ/dt, м3/ч·с
        RATE_OF_TEMPERATURE_CHANGE;  // dT/dt, °C/с
    }

    private Type type = null;
    private SpecialValueRange range = null;

    public Gradients(Type type, SpecialValueRange range) {
        this.type = type;
        this.range = range;
    }

    public Gradients(Type type) {
        this(type, null);
    }

    @Override
    public Float generate() {
        return switch (type) {
            case RATE_OF_PRESSURE_CHANGE -> generatePressureGradient(range);
            case RATE_OF_CHANGE_OF_FLOW -> generateFlowGradient(range);
            case RATE_OF_TEMPERATURE_CHANGE -> generateTemperatureGradient(range);
        };
    }

    /* ================= dP/dt ================= */

    private float generatePressureGradient(SpecialValueRange range) {
        if (range == null) {
            return random(-1.0f, 1.0f);
        }

        return switch (range) {
            case OK -> random(-0.05f, 0.05f);
            case WARN -> randomFromTwoRanges(-0.2f, -0.05f, 0.05f, 0.2f);
            case CRITICAL -> randomFromTwoRanges(-1.0f, -0.2f, 0.2f, 1.0f);
        };
    }

    /* ================= dQ/dt ================= */

    private float generateFlowGradient(SpecialValueRange range) {
        if (range == null) {
            return random(-500f, 500f);
        }

        return switch (range) {
            case OK -> random(-20f, 20f);
            case WARN -> randomFromTwoRanges(-100f, -20f, 20f, 100f);
            case CRITICAL -> randomFromTwoRanges(-500f, -100f, 100f, 500f);
        };
    }

    /* ================= dT/dt ================= */

    private float generateTemperatureGradient(SpecialValueRange range) {
        if (range == null) {
            return random(-5.0f, 5.0f);
        }

        return switch (range) {
            case OK -> random(-0.1f, 0.1f);
            case WARN -> randomFromTwoRanges(-0.5f, -0.1f, 0.1f, 0.5f);
            case CRITICAL -> randomFromTwoRanges(-5.0f, -0.5f, 0.5f, 5.0f);
        };
    }

    @Override
    public ResolveResult resolve(Float value) {

        if (value == null) {
            return new ResolveResult(
                    SpecialValueRange.CRITICAL,
                    type + ": value is null → treated as CRITICAL (observability failure)"
            );
        }

        return switch (type) {
            case RATE_OF_PRESSURE_CHANGE -> resolvePressure(value);
            case RATE_OF_CHANGE_OF_FLOW -> resolveFlow(value);
            case RATE_OF_TEMPERATURE_CHANGE -> resolveTemperature(value);
        };
    }

    /* ================= dP/dt ================= */

    private ResolveResult resolvePressure(float v) {

        if (v >= -0.05f && v <= 0.05f) {
            return new ResolveResult(
                    SpecialValueRange.OK,
                    type + ": value " + v + " inside OK range [-0.05 … 0.05]"
            );
        }

        if ((v >= -0.2f && v < -0.05f) || (v > 0.05f && v <= 0.2f)) {
            return new ResolveResult(
                    SpecialValueRange.WARN,
                    type + ": value " + v +
                            " inside WARN range [-0.2 … -0.05] or [0.05 … 0.2]"
            );
        }

        return new ResolveResult(
                SpecialValueRange.CRITICAL,
                type + ": value " + v +
                        " outside safe bounds → CRITICAL (< -0.2 or > 0.2)"
        );
    }

    /* ================= dQ/dt ================= */

    private ResolveResult resolveFlow(float v) {

        if (v >= -20f && v <= 20f) {
            return new ResolveResult(
                    SpecialValueRange.OK,
                    type + ": value " + v + " inside OK range [-20 … 20]"
            );
        }

        if ((v >= -100f && v < -20f) || (v > 20f && v <= 100f)) {
            return new ResolveResult(
                    SpecialValueRange.WARN,
                    type + ": value " + v +
                            " inside WARN range [-100 … -20] or [20 … 100]"
            );
        }

        return new ResolveResult(
                SpecialValueRange.CRITICAL,
                type + ": value " + v +
                        " outside safe bounds → CRITICAL (< -100 or > 100)"
        );
    }

    /* ================= dT/dt ================= */

    private ResolveResult resolveTemperature(float v) {

        if (v >= -0.1f && v <= 0.1f) {
            return new ResolveResult(
                    SpecialValueRange.OK,
                    type + ": value " + v + " inside OK range [-0.1 … 0.1]"
            );
        }

        if ((v >= -0.5f && v < -0.1f) || (v > 0.1f && v <= 0.5f)) {
            return new ResolveResult(
                    SpecialValueRange.WARN,
                    type + ": value " + v +
                            " inside WARN range [-0.5 … -0.1] or [0.1 … 0.5]"
            );
        }

        return new ResolveResult(
                SpecialValueRange.CRITICAL,
                type + ": value " + v +
                        " outside safe bounds → CRITICAL (< -0.5 or > 0.5)"
        );
    }

    /* ================= helpers ================= */

    private float random(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, max);
    }

    private float randomFromTwoRanges(float min1, float max1, float min2, float max2) {
        return ThreadLocalRandom.current().nextBoolean()
                ? random(min1, max1)
                : random(min2, max2);
    }
}