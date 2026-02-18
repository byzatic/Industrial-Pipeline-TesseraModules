package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pump_unit;

import java.util.concurrent.ThreadLocalRandom;

public class PumpUnit implements PumpUnitValuesGeneratorInterface {

    public enum Type {
        V_RMS,      // vibration, mm/s, 0..25
        T_BEARING,  // bearing temperature, °C, 0..120
        I,          // motor current, A, 0..400
        U,          // voltage, V, 0..480
        N,          // rotation frequency, Hz, 0..60
        P_EL        // electric power, kW, 0..500
    }

    // чтобы CRITICAL был строго ">"
    private static final float EPS = 0.0001f;

    /**
     * Номинальные и уставочные значения насосного агрегата
     */
    public static final class Nominals {
        public final float iNom;   // A
        public final float uNom;   // V
        public final float nSet;   // Hz (уставка ЧРП)
        public final float pNom;   // kW

        public Nominals(float iNom, float uNom, float nSet, float pNom) {
            this.iNom = iNom;
            this.uNom = uNom;
            this.nSet = nSet;
            this.pNom = pNom;
        }
    }

    private PumpUnit.Nominals nominals = null;
    private PumpUnit.Type type = null;
    private PumpUnit.SpecialValueRange range = null;


    public PumpUnit(PumpUnit.Nominals nominals, PumpUnit.Type type, PumpUnit.SpecialValueRange range) {
        this.nominals = nominals;
        this.type = type;
        this.range = range;
    }

    public PumpUnit(PumpUnit.Nominals nominals, PumpUnit.Type type) {
        this(nominals, type, null);
    }

    @Override
    public Float generate() {
        return switch (type) {
            case V_RMS -> generateVrms(range);
            case T_BEARING -> generateTBearing(range);
            case I -> generateCurrent(range);
            case U -> generateVoltage(range);
            case N -> generateSpeed(range);
            case P_EL -> generatePower(range);
        };
    }

    /* ================= V_rms (mm/s, 0..25) ================= */

    private float generateVrms(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 25f);
        }

        return switch (range) {
            case OK -> random(0f, 4.5f);
            case WARN -> random(4.5f, 7.1f);
            case CRITICAL -> random(7.1f + EPS, 25f);
        };
    }

    /* ================= T_bearing (°C, 0..120) ================= */

    private float generateTBearing(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 120f);
        }

        return switch (range) {
            case OK -> random(0f, 70f);
            case WARN -> random(70f, 85f);
            case CRITICAL -> random(85f + EPS, 120f);
        };
    }

    /* ================= I (A, 0..400) ================= */

    private float generateCurrent(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 400f);
        }

        float nom = nominals.iNom;

        return switch (range) {
            case OK -> random(0.9f * nom, 1.05f * nom);
            case WARN -> random(1.05f * nom, 1.2f * nom);
            case CRITICAL -> random(1.2f * nom + EPS, 400f);
        };
    }

    /* ================= U (V, 0..480) ================= */

    private float generateVoltage(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 480f);
        }

        float nom = nominals.uNom;

        return switch (range) {
            case OK -> random(0.95f * nom, 1.05f * nom);
            case WARN -> randomFromTwoRanges(
                    0.9f * nom, 0.95f * nom,
                    1.05f * nom, 1.1f * nom
            );
            case CRITICAL -> randomFromTwoRanges(
                    0f, 0.9f * nom,
                    1.1f * nom + EPS, 480f
            );
        };
    }

    /* ================= N (Hz, 0..60) ================= */

    private float generateSpeed(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 60f);
        }

        float set = nominals.nSet;

        return switch (range) {
            case OK -> random(set - 2f, set + 2f);
            case WARN -> randomFromTwoRanges(
                    set - 5f, set - 2f,
                    set + 2f, set + 5f
            );
            case CRITICAL -> randomFromTwoRanges(
                    0f, set - 5f,
                    set + 5f + EPS, 60f
            );
        };
    }

    /* ================= P_el (kW, 0..500) ================= */

    private float generatePower(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 500f);
        }

        float nom = nominals.pNom;

        return switch (range) {
            case OK -> random(0.9f * nom, 1.05f * nom);
            case WARN -> random(1.05f * nom, 1.2f * nom);
            case CRITICAL -> random(1.2f * nom + EPS, 500f);
        };
    }

    @Override
    public ResolveResult resolve(Float value) {

        if (value == null) {
            return new ResolveResult(
                    SpecialValueRange.CRITICAL,
                    type + ": value is null → CRITICAL (no telemetry)"
            );
        }

        float v = value;

        SpecialValueRange range = switch (type) {
            case V_RMS -> resolveVrms(v);
            case T_BEARING -> resolveTBearing(v);

            case I -> resolveRelativeAboveOnly(v, nominals.iNom, 0.9f, 1.05f, 1.2f);
            case P_EL -> resolveRelativeAboveOnly(v, nominals.pNom, 0.9f, 1.05f, 1.2f);

            case U -> resolveVoltage(v, nominals.uNom);
            case N -> resolveSpeed(v, nominals.nSet);
        };

        return new ResolveResult(range, explain(v, range));
    }

    private String explain(float v, SpecialValueRange range) {

        return switch (type) {

            case V_RMS -> explainSimpleThreshold(v, range, "mm/s", 4.5f, 7.1f);

            case T_BEARING -> explainSimpleThreshold(v, range, "°C", 70f, 85f);

            case I -> explainRelativeAboveOnly(v, range, "A",
                    nominals.iNom, 0.9f, 1.05f, 1.2f);

            case P_EL -> explainRelativeAboveOnly(v, range, "kW",
                    nominals.pNom, 0.9f, 1.05f, 1.2f);

            case U -> explainVoltage(v, range, nominals.uNom);

            case N -> explainSpeed(v, range, nominals.nSet);
        };
    }

    private String explainSimpleThreshold(
            float v,
            SpecialValueRange range,
            String unit,
            float okUpper,
            float warnUpper
    ) {
        return switch (range) {
            case OK -> String.format(
                    "%s: %.3f %s ≤ %.2f → OK",
                    type, v, unit, okUpper
            );
            case WARN -> String.format(
                    "%s: %.3f %s in (%.2f .. %.2f] → WARN",
                    type, v, unit, okUpper, warnUpper
            );
            case CRITICAL -> String.format(
                    "%s: %.3f %s > %.2f → CRITICAL",
                    type, v, unit, warnUpper
            );
        };
    }

    private String explainRelativeAboveOnly(
            float v,
            SpecialValueRange range,
            String unit,
            float nom,
            float okLow,
            float okHigh,
            float warnHigh
    ) {
        float okMin = okLow * nom;
        float okMax = okHigh * nom;
        float warnMax = warnHigh * nom;

        return switch (range) {
            case OK -> String.format(
                    "%s: %.3f %s in [%.2f .. %.2f] → OK",
                    type, v, unit, okMin, okMax
            );
            case WARN -> String.format(
                    "%s: %.3f %s in (%.2f .. %.2f] → WARN",
                    type, v, unit, okMax, warnMax
            );
            case CRITICAL -> String.format(
                    "%s: %.3f %s outside [%.2f .. %.2f] → CRITICAL",
                    type, v, unit, okMin, warnMax
            );
        };
    }


    private String explainVoltage(
            float v,
            SpecialValueRange range,
            float nom
    ) {
        float okMin = 0.95f * nom;
        float okMax = 1.05f * nom;
        float warnLowMin = 0.9f * nom;
        float warnHighMax = 1.1f * nom;

        return switch (range) {
            case OK -> String.format(
                    "%s: %.3f V in [%.2f .. %.2f] → OK",
                    type, v, okMin, okMax
            );
            case WARN -> String.format(
                    "%s: %.3f V in WARN band (%.2f .. %.2f) → WARN",
                    type, v, warnLowMin, warnHighMax
            );
            case CRITICAL -> String.format(
                    "%s: %.3f V outside (%.2f .. %.2f) → CRITICAL",
                    type, v, warnLowMin, warnHighMax
            );
        };
    }

    private String explainSpeed(
            float v,
            SpecialValueRange range,
            float set
    ) {
        float diff = Math.abs(v - set);

        return switch (range) {
            case OK -> String.format(
                    "%s: %.3f Hz (Δ=%.2f ≤ 2) → OK",
                    type, v, diff
            );
            case WARN -> String.format(
                    "%s: %.3f Hz (Δ=%.2f ≤ 5) → WARN",
                    type, v, diff
            );
            case CRITICAL -> String.format(
                    "%s: %.3f Hz (Δ=%.2f > 5) → CRITICAL",
                    type, v, diff
            );
        };
    }

    /* ================= V_rms (mm/s) ================= */

    private SpecialValueRange resolveVrms(float v) {
        // OK: <= 4.5
        if (v <= 4.5f) return SpecialValueRange.OK;
        // WARN: (4.5..7.1]
        if (v <= 7.1f) return SpecialValueRange.WARN;
        // CRITICAL: > 7.1
        return SpecialValueRange.CRITICAL;
    }

    /* ================= T_bearing (°C) ================= */

    private SpecialValueRange resolveTBearing(float v) {
        // OK: <= 70
        if (v <= 70f) return SpecialValueRange.OK;
        // WARN: (70..85]
        if (v <= 85f) return SpecialValueRange.WARN;
        // CRITICAL: > 85
        return SpecialValueRange.CRITICAL;
    }

    /**
     * Для I и P_el:
     * OK: 0.9..1.05*nom
     * WARN: (1.05..1.2]*nom
     * CRITICAL: >1.2*nom
     * (нижняя сторона в модели не выделена, поэтому всё ниже OK-диапазона считаем CRITICAL)
     */
    private SpecialValueRange resolveRelativeAboveOnly(float v, float nom, float okLow, float okHigh, float warnHigh) {
        if (v >= okLow * nom && v <= okHigh * nom) {
            return SpecialValueRange.OK;
        }
        if (v > okHigh * nom && v <= warnHigh * nom) {
            return SpecialValueRange.WARN;
        }
        return SpecialValueRange.CRITICAL;
    }

    /**
     * U:
     * OK: 0.95..1.05*nom
     * WARN: 0.9..0.95*nom или 1.05..1.1*nom
     * CRITICAL: <0.9*nom или >1.1*nom
     */
    private SpecialValueRange resolveVoltage(float v, float nom) {
        if (v >= 0.95f * nom && v <= 1.05f * nom) {
            return SpecialValueRange.OK;
        }
        if ((v >= 0.9f * nom && v < 0.95f * nom) ||
                (v > 1.05f * nom && v <= 1.1f * nom)) {
            return SpecialValueRange.WARN;
        }
        return SpecialValueRange.CRITICAL;
    }

    /**
     * N:
     * OK: |N - N_set| <= 2
     * WARN: 2..5
     * CRITICAL: >5
     */
    private SpecialValueRange resolveSpeed(float n, float set) {
        float diff = Math.abs(n - set);
        if (diff <= 2f) return SpecialValueRange.OK;
        if (diff <= 5f) return SpecialValueRange.WARN;
        return SpecialValueRange.CRITICAL;
    }

    /* ================= helpers ================= */

    private float random(float minInclusive, float maxExclusive) {
        if (maxExclusive <= minInclusive) {
            return minInclusive;
        }
        return (float) ThreadLocalRandom.current().nextDouble(minInclusive, maxExclusive);
    }

    private float randomFromTwoRanges(float min1, float max1, float min2, float max2) {
        boolean firstValid = max1 > min1;
        boolean secondValid = max2 > min2;

        if (firstValid && secondValid) {
            return ThreadLocalRandom.current().nextBoolean()
                    ? random(min1, max1)
                    : random(min2, max2);
        }
        if (firstValid) return random(min1, max1);
        if (secondValid) return random(min2, max2);

        return min1;
    }
}