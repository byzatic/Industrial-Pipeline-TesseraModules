package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pipeline_segment;

import java.util.concurrent.ThreadLocalRandom;

public class SegmentDataQuality implements PipelineSegmentGeneratorInterface {
//    Missing_count — доля / количество пропусков данных
//    Тип данных: float
//    Единицы: %
//    Диапазон генерации: 0 … 100
//    OK: 0 … 1
//    WARN: 1 … 5
//    CRITICAL: >5
//
//    Time_desync — рассинхронизация временных меток
//    Тип данных: float
//    Единицы: мс
//    Диапазон генерации: 0 … 5000
//    OK: 0 … 100
//    WARN: 100 … 500
//    CRITICAL: >500
//
//    Corr_mismatch — расхождение коррелирующих сигналов
//    Тип данных: float
//    Единицы: %
//    Диапазон генерации: 0 … 100
//    OK: 0 … 3
//    WARN: 3 … 10
//    CRITICAL: >10

    public enum Type {
        MISSING_COUNT,   // %, 0..100
        TIME_DESYNC,     // ms, 0..5000
        CORR_MISMATCH    // %, 0..100
    }

    // Хитринка! Чтобы CRITICAL был строго ">" порога, а не ">= порога"
    private static final float EPS = 0.0001f;

    private SegmentDataQuality.Type type = null;
    private SpecialValueRange range = null;

    public SegmentDataQuality(SegmentDataQuality.Type type, SpecialValueRange range) {
        this.type = type;
        this.range = range;
    }

    public SegmentDataQuality(SegmentDataQuality.Type type) {
        this(type, null);
    }

    @Override
    public Float generate() {
        return switch (type) {
            case MISSING_COUNT -> generateMissingCount(range);
            case TIME_DESYNC -> generateTimeDesync(range);
            case CORR_MISMATCH -> generateCorrMismatch(range);
        };
    }

    /* ================= Missing_count (%) ================= */

    private float generateMissingCount(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 100f);
        }

        return switch (range) {
            case OK -> random(0f, 1f);
            case WARN -> random(1f, 5f);
            case CRITICAL -> random(5f + EPS, 100f);
        };
    }

    /* ================= Time_desync (ms) ================= */

    private float generateTimeDesync(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 5000f);
        }

        return switch (range) {
            case OK -> random(0f, 100f);
            case WARN -> random(100f, 500f);
            case CRITICAL -> random(500f + EPS, 5000f);
        };
    }

    /* ================= Corr_mismatch (%) ================= */

    private float generateCorrMismatch(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 100f);
        }

        return switch (range) {
            case OK -> random(0f, 3f);
            case WARN -> random(3f, 10f);
            case CRITICAL -> random(10f + EPS, 100f);
        };
    }

    @Override
    public ResolveResult resolve(Float value) {

        if (value == null) {
            return new ResolveResult(
                    SpecialValueRange.CRITICAL,
                    type + ": value is null → treated as CRITICAL (data unavailable)"
            );
        }

        float v = value;

        SpecialValueRange range = switch (type) {
            case MISSING_COUNT -> resolveMissingCount(v);
            case TIME_DESYNC -> resolveTimeDesync(v);
            case CORR_MISMATCH -> resolveCorrMismatch(v);
        };

        return new ResolveResult(range, explain(v, range));
    }

    private String explain(float value, SpecialValueRange range) {

        return switch (type) {

            case MISSING_COUNT -> explainThreshold(value, range, 1f, 5f, "%");

            case TIME_DESYNC -> explainThreshold(value, range, 100f, 500f, "ms");

            case CORR_MISMATCH -> explainThreshold(value, range, 3f, 10f, "%");
        };
    }

    private String explainThreshold(float v,
                                    SpecialValueRange range,
                                    float okUpper,
                                    float warnUpper,
                                    String unit) {

        return switch (range) {

            case OK -> String.format(
                    "%s: %.4f %s within OK range [0..%.2f]",
                    type, v, unit, okUpper
            );

            case WARN -> String.format(
                    "%s: %.4f %s within WARN range (%.2f..%.2f]",
                    type, v, unit, okUpper, warnUpper
            );

            case CRITICAL -> String.format(
                    "%s: %.4f %s > %.2f → CRITICAL",
                    type, v, unit, warnUpper
            );
        };
    }


    private SpecialValueRange resolveMissingCount(float v) {
        // OK: 0..1
        if (v >= 0f && v <= 1f) {
            return SpecialValueRange.OK;
        }
        // WARN: (1..5]
        if (v > 1f && v <= 5f) {
            return SpecialValueRange.WARN;
        }
        // CRITICAL: >5 (и любые странные значения)
        return SpecialValueRange.CRITICAL;
    }

    private SpecialValueRange resolveTimeDesync(float v) {
        // OK: 0..100
        if (v >= 0f && v <= 100f) {
            return SpecialValueRange.OK;
        }
        // WARN: (100..500]
        if (v > 100f && v <= 500f) {
            return SpecialValueRange.WARN;
        }
        // CRITICAL: >500
        return SpecialValueRange.CRITICAL;
    }

    private SpecialValueRange resolveCorrMismatch(float v) {
        // OK: 0..3
        if (v >= 0f && v <= 3f) {
            return SpecialValueRange.OK;
        }
        // WARN: (3..10]
        if (v > 3f && v <= 10f) {
            return SpecialValueRange.WARN;
        }
        // CRITICAL: >10
        return SpecialValueRange.CRITICAL;
    }

    /* ================= helpers ================= */

    private float random(float minInclusive, float maxExclusive) {
        return (float) ThreadLocalRandom.current().nextDouble(minInclusive, maxExclusive);
    }
}