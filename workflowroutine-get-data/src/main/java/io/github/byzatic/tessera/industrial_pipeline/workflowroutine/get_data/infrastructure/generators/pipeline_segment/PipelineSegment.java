package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pipeline_segment;

import java.util.concurrent.ThreadLocalRandom;

public class PipelineSegment implements PipelineSegmentGeneratorInterface {

    public enum Type {
        DP_SEG,      // ΔP_seg, MPa, 0..2.0
        DP_NORM,     // ΔP_norm, MPa/km, 0..1.0
        DQ_SEG,      // ΔQ_seg, %, 0..20
        DT_SEG,      // ΔT_seg, °C, 0..20
        R_SEG,       // R_seg, MPa·h/m3, 0..1.0
        ERR_P        // err_P, %, 0..30
    }

    private static final float EPS = 0.0001f;

    /**
     * Номиналы для относительных метрик (где используются 0.8..1.2 * nominal).
     * Для метрик без номинала (ΔQ_seg, ΔT_seg, err_P) можно передавать null.
     */
    public static final class Nominals {
        public final float dpSegNom;   // MPa
        public final float dpNormNom;  // MPa/km
        public final float rSegNom;    // MPa·h/m3

        public Nominals(float dpSegNom, float dpNormNom, float rSegNom) {
            this.dpSegNom = dpSegNom;
            this.dpNormNom = dpNormNom;
            this.rSegNom = rSegNom;
        }
    }

    private Nominals nominals = null;
    private Type type = null;
    private SpecialValueRange range = null;

    /**
     * @param nominals номиналы для DP_SEG, DP_NORM, R_SEG. Может быть null, но тогда
     *                 генерация OK/WARN/CRITICAL для этих метрик невозможна (только range=null).
     */
    public PipelineSegment(Nominals nominals, Type type, SpecialValueRange range) {
        this.nominals = nominals;
        this.type = type;
        this.range = range;
    }

    public PipelineSegment(Nominals nominals, Type type) {
        this(nominals, type, null);
    }

    @Override
    public Float generate() {
        return switch (type) {
            case DP_SEG -> generateDpSeg(range);
            case DP_NORM -> generateDpNorm(range);
            case DQ_SEG -> generateDqSeg(range);
            case DT_SEG -> generateDtSeg(range);
            case R_SEG -> generateRSeg(range);
            case ERR_P -> generateErrP(range);
        };
    }

    /* ================= ΔP_seg (MPa, 0..2.0) ================= */

    private float generateDpSeg(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 2.0f);
        }
        requireNominals("DP_SEG");

        float nom = nominals.dpSegNom;

        return switch (range) {
            case OK -> random(clampLow(0.9f * nom, 0f), clampHigh(1.1f * nom, 2.0f));
            case WARN -> randomFromTwoRanges(
                    clampLow(0.8f * nom, 0f), clampHigh(0.9f * nom, 2.0f),
                    clampLow(1.1f * nom, 0f), clampHigh(1.2f * nom, 2.0f)
            );
            case CRITICAL -> randomFromTwoRanges(
                    0f, clampHigh(0.8f * nom, 2.0f),
                    clampLow(1.2f * nom + EPS, 0f), 2.0f
            );
        };
    }

    /* ================= ΔP_norm (MPa/km, 0..1.0) ================= */

    private float generateDpNorm(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 1.0f);
        }
        requireNominals("DP_NORM");

        float nom = nominals.dpNormNom;

        return switch (range) {
            case OK -> random(clampLow(0.9f * nom, 0f), clampHigh(1.1f * nom, 1.0f));
            case WARN -> randomFromTwoRanges(
                    clampLow(0.8f * nom, 0f), clampHigh(0.9f * nom, 1.0f),
                    clampLow(1.1f * nom, 0f), clampHigh(1.2f * nom, 1.0f)
            );
            case CRITICAL -> randomFromTwoRanges(
                    0f, clampHigh(0.8f * nom, 1.0f),
                    clampLow(1.2f * nom + EPS, 0f), 1.0f
            );
        };
    }

    /* ================= ΔQ_seg (% , 0..20) ================= */

    private float generateDqSeg(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 20f);
        }

        return switch (range) {
            case OK -> random(0f, 2f);
            case WARN -> random(2f, 5f);
            case CRITICAL -> random(5f + EPS, 20f);
        };
    }

    /* ================= ΔT_seg (°C, 0..20) ================= */

    private float generateDtSeg(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 20f);
        }

        return switch (range) {
            case OK -> random(0f, 2f);
            case WARN -> random(2f, 5f);
            case CRITICAL -> random(5f + EPS, 20f);
        };
    }

    /* ================= R_seg (MPa·h/m3, 0..1.0) ================= */

    private float generateRSeg(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 1.0f);
        }
        requireNominals("R_SEG");

        float nom = nominals.rSegNom;

        return switch (range) {
            case OK -> random(clampLow(0.9f * nom, 0f), clampHigh(1.1f * nom, 1.0f));
            case WARN -> randomFromTwoRanges(
                    clampLow(0.8f * nom, 0f), clampHigh(0.9f * nom, 1.0f),
                    clampLow(1.1f * nom, 0f), clampHigh(1.2f * nom, 1.0f)
            );
            case CRITICAL -> randomFromTwoRanges(
                    0f, clampHigh(0.8f * nom, 1.0f),
                    clampLow(1.2f * nom + EPS, 0f), 1.0f
            );
        };
    }

    /* ================= err_P (% , 0..30) ================= */

    private float generateErrP(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 30f);
        }

        return switch (range) {
            case OK -> random(0f, 5f);
            case WARN -> random(5f, 15f);
            case CRITICAL -> random(15f + EPS, 30f);
        };
    }

    @Override
    public ResolveResult resolve(Float value) {

        if (value == null) {
            return new ResolveResult(
                    SpecialValueRange.CRITICAL,
                    type + ": value is null → treated as CRITICAL (observability loss)"
            );
        }

        SpecialValueRange range = switch (type) {
            case DP_SEG -> resolveRelative(value, nominalDpSeg());
            case DP_NORM -> resolveRelative(value, nominalDpNorm());
            case R_SEG -> resolveRelative(value, nominalRseg());

            case DQ_SEG -> resolveAbs_0_2_5(value);
            case DT_SEG -> resolveAbs_0_2_5(value);
            case ERR_P -> resolveAbs_0_5_15(value);
        };

        return new ResolveResult(range, explain(value, range));
    }

    private String explain(float value, SpecialValueRange range) {

        return switch (type) {

            case DP_SEG -> explainRelative(value, range, nominalDpSeg(), "MPa");
            case DP_NORM -> explainRelative(value, range, nominalDpNorm(), "MPa/km");
            case R_SEG -> explainRelative(value, range, nominalRseg(), "MPa·h/m3");

            case DQ_SEG -> explainAbs_0_2_5(value, range, "%");
            case DT_SEG -> explainAbs_0_2_5(value, range, "°C");
            case ERR_P -> explainAbs_0_5_15(value, range, "%");
        };
    }

    /**
     * OK: 0.9..1.1*nom, WARN: 0.8..0.9*nom или 1.1..1.2*nom, CRITICAL: иначе
     */
    private SpecialValueRange resolveRelative(float v, float nom) {
        if (v >= 0.9f * nom && v <= 1.1f * nom) {
            return SpecialValueRange.OK;
        }
        if ((v >= 0.8f * nom && v < 0.9f * nom) || (v > 1.1f * nom && v <= 1.2f * nom)) {
            return SpecialValueRange.WARN;
        }
        return SpecialValueRange.CRITICAL;
    }

    private String explainRelative(float v,
                                   SpecialValueRange range,
                                   float nom,
                                   String unit) {

        return switch (range) {

            case OK -> String.format(
                    "%s: %.4f %s within OK range [0.9..1.1]×nom (nom=%.4f)",
                    type, v, unit, nom
            );

            case WARN -> String.format(
                    "%s: %.4f %s within WARN range [0.8..0.9]×nom or [1.1..1.2]×nom (nom=%.4f)",
                    type, v, unit, nom
            );

            case CRITICAL -> String.format(
                    "%s: %.4f %s outside 0.8..1.2×nom (nom=%.4f) → CRITICAL",
                    type, v, unit, nom
            );
        };
    }

    /**
     * OK: 0..2, WARN: (2..5], CRITICAL: >5
     */
    private SpecialValueRange resolveAbs_0_2_5(float v) {
        if (v >= 0f && v <= 2f) {
            return SpecialValueRange.OK;
        }
        if (v > 2f && v <= 5f) {
            return SpecialValueRange.WARN;
        }
        return SpecialValueRange.CRITICAL;
    }

    private String explainAbs_0_2_5(float v,
                                    SpecialValueRange range,
                                    String unit) {

        return switch (range) {

            case OK -> String.format(
                    "%s: %.4f %s in OK range [0..2]",
                    type, v, unit
            );

            case WARN -> String.format(
                    "%s: %.4f %s in WARN range (2..5]",
                    type, v, unit
            );

            case CRITICAL -> String.format(
                    "%s: %.4f %s > 5 → CRITICAL",
                    type, v, unit
            );
        };
    }

    /**
     * OK: 0..5, WARN: (5..15], CRITICAL: >15
     */
    private SpecialValueRange resolveAbs_0_5_15(float v) {
        if (v >= 0f && v <= 5f) {
            return SpecialValueRange.OK;
        }
        if (v > 5f && v <= 15f) {
            return SpecialValueRange.WARN;
        }
        return SpecialValueRange.CRITICAL;
    }

    private String explainAbs_0_5_15(float v,
                                     SpecialValueRange range,
                                     String unit) {

        return switch (range) {

            case OK -> String.format(
                    "%s: %.4f %s in OK range [0..5]",
                    type, v, unit
            );

            case WARN -> String.format(
                    "%s: %.4f %s in WARN range (5..15]",
                    type, v, unit
            );

            case CRITICAL -> String.format(
                    "%s: %.4f %s > 15 → CRITICAL",
                    type, v, unit
            );
        };
    }

    /* ================= helpers ================= */

    private void requireNominals(String metric) {
        if (nominals == null) {
            throw new IllegalStateException("Nominals are required for " + metric
                    + " when SpecialValueRange is not null");
        }
    }

    private float random(float minInclusive, float maxExclusive) {
        if (maxExclusive <= minInclusive) {
            // на случай, если номинал близок к границе диапазона
            return minInclusive;
        }
        return (float) ThreadLocalRandom.current().nextDouble(minInclusive, maxExclusive);
    }

    private float randomFromTwoRanges(float min1, float max1, float min2, float max2) {
        // если один из диапазонов вырожден, берём другой
        boolean firstValid = max1 > min1;
        boolean secondValid = max2 > min2;

        if (firstValid && secondValid) {
            return ThreadLocalRandom.current().nextBoolean() ? random(min1, max1) : random(min2, max2);
        }
        if (firstValid) return random(min1, max1);
        if (secondValid) return random(min2, max2);

        // оба вырождены
        return min1;
    }

    private float clampLow(float v, float low) {
        return Math.max(v, low);
    }

    private float clampHigh(float v, float high) {
        return Math.min(v, high);
    }


    private float nominalDpSeg() {
        if (nominals == null) {
            throw new IllegalStateException("Nominals are required for DP_SEG resolve()");
        }
        return nominals.dpSegNom;
    }

    private float nominalDpNorm() {
        if (nominals == null) {
            throw new IllegalStateException("Nominals are required for DP_NORM resolve()");
        }
        return nominals.dpNormNom;
    }

    private float nominalRseg() {
        if (nominals == null) {
            throw new IllegalStateException("Nominals are required for R_SEG resolve()");
        }
        return nominals.rSegNom;
    }

}