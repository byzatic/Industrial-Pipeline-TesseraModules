package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.pump_unit;

import java.util.concurrent.ThreadLocalRandom;

public class Hydraulics implements PumpUnitValuesGeneratorInterface {

    public enum Type {
        P_IN,    // Pin, MPa, 0..10
        DP,      // ΔP, MPa, 0..5
        Q,       // Q, m3/h, 0..5000
        P_OUT,   // Pout, MPa, 0..12
        T        // T, °C, -20..120
    }

    // Чтобы CRITICAL был строго ">" порога
    private static final float EPS = 0.0001f;

    /**
     * Номинальные значения для гидравлики насосной станции
     */
    public static final class Nominals {
        public final float pinNom;   // MPa
        public final float dpNom;    // MPa
        public final float qNom;     // m3/h
        public final float poutNom;  // MPa
        public final float tNom;     // °C

        public Nominals(float pinNom, float dpNom, float qNom, float poutNom, float tNom) {
            this.pinNom = pinNom;
            this.dpNom = dpNom;
            this.qNom = qNom;
            this.poutNom = poutNom;
            this.tNom = tNom;
        }
    }

    private Hydraulics.Nominals nominals = null;
    private Hydraulics.Type type = null;
    private Hydraulics.SpecialValueRange range = null;


    public Hydraulics(Hydraulics.Nominals nominals, Hydraulics.Type type, Hydraulics.SpecialValueRange range) {
        this.nominals = nominals;
        this.type = type;
        this.range = range;
    }

    public Hydraulics(Hydraulics.Nominals nominals, Hydraulics.Type type) {
        this(nominals, type, null);
    }


    @Override
    public Float generate() {
        return switch (type) {
            case P_IN -> generatePin(range);
            case DP -> generateDp(range);
            case Q -> generateQ(range);
            case P_OUT -> generatePout(range);
            case T -> generateTemperature(range);
        };
    }

    /* ================= Pin (MPa, 0..10) ================= */

    private float generatePin(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 10f);
        }

        float nom = nominals.pinNom;

        return switch (range) {
            case OK -> random(0.9f * nom, 1.1f * nom);
            case WARN -> randomFromTwoRanges(
                    0.8f * nom, 0.9f * nom,
                    1.1f * nom, 1.2f * nom
            );
            case CRITICAL -> randomFromTwoRanges(
                    0f, 0.8f * nom,
                    1.2f * nom + EPS, 10f
            );
        };
    }

    /* ================= ΔP (MPa, 0..5) ================= */

    private float generateDp(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 5f);
        }

        float nom = nominals.dpNom;

        return switch (range) {
            case OK -> random(0.9f * nom, 1.1f * nom);
            case WARN -> randomFromTwoRanges(
                    0.8f * nom, 0.9f * nom,
                    1.1f * nom, 1.2f * nom
            );
            case CRITICAL -> randomFromTwoRanges(
                    0f, 0.8f * nom,
                    1.2f * nom + EPS, 5f
            );
        };
    }

    /* ================= Q (m3/h, 0..5000) ================= */

    private float generateQ(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 5000f);
        }

        float nom = nominals.qNom;

        return switch (range) {
            case OK -> random(0.95f * nom, 1.05f * nom);
            case WARN -> randomFromTwoRanges(
                    0.85f * nom, 0.95f * nom,
                    1.05f * nom, 1.15f * nom
            );
            case CRITICAL -> randomFromTwoRanges(
                    0f, 0.85f * nom,
                    1.15f * nom + EPS, 5000f
            );
        };
    }

    /* ================= Pout (MPa, 0..12) ================= */

    private float generatePout(SpecialValueRange range) {
        if (range == null) {
            return random(0f, 12f);
        }

        float nom = nominals.poutNom;

        return switch (range) {
            case OK -> random(0.9f * nom, 1.1f * nom);
            case WARN -> randomFromTwoRanges(
                    0.85f * nom, 0.9f * nom,
                    1.1f * nom, 1.15f * nom
            );
            case CRITICAL -> randomFromTwoRanges(
                    0f, 0.85f * nom,
                    1.15f * nom + EPS, 12f
            );
        };
    }

    /* ================= Temperature (°C, -20..120) ================= */

    private float generateTemperature(SpecialValueRange range) {
        if (range == null) {
            return random(-20f, 120f);
        }

        float nom = nominals.tNom;

        return switch (range) {
            case OK -> random(nom - 5f, nom + 5f);
            case WARN -> randomFromTwoRanges(
                    nom - 10f, nom - 5f,
                    nom + 5f, nom + 10f
            );
            case CRITICAL -> randomFromTwoRanges(
                    -20f, nom - 10f,
                    nom + 10f + EPS, 120f
            );
        };
    }

    @Override
    public ResolveResult resolve(Float value) {

        if (value == null) {
            return new ResolveResult(
                    SpecialValueRange.CRITICAL,
                    type + ": value is null → treated as CRITICAL (no telemetry)"
            );
        }

        float v = value;

        SpecialValueRange range = switch (type) {
            case P_IN -> resolveRelative(v, nominals.pinNom, 0.8f, 0.9f, 1.1f, 1.2f);
            case DP -> resolveRelative(v, nominals.dpNom, 0.8f, 0.9f, 1.1f, 1.2f);
            case Q -> resolveRelative(v, nominals.qNom, 0.85f, 0.95f, 1.05f, 1.15f);
            case P_OUT -> resolveRelative(v, nominals.poutNom, 0.85f, 0.9f, 1.1f, 1.15f);
            case T -> resolveTemperature(v, nominals.tNom);
        };

        return new ResolveResult(range, explain(v, range));
    }

    private String explain(float v, SpecialValueRange range) {

        return switch (type) {

            case P_IN -> explainRelative(v, nominals.pinNom, "MPa", 0.8f, 0.9f, 1.1f, 1.2f);

            case DP -> explainRelative(v, nominals.dpNom, "MPa", 0.8f, 0.9f, 1.1f, 1.2f);

            case Q -> explainRelative(v, nominals.qNom, "m3/h", 0.85f, 0.95f, 1.05f, 1.15f);

            case P_OUT -> explainRelative(v, nominals.poutNom, "MPa", 0.85f, 0.9f, 1.1f, 1.15f);

            case T -> explainTemperature(v, nominals.tNom);
        };
    }

    /**
     * Универсальный относительный resolve
     */
    private SpecialValueRange resolveRelative(
            float v,
            float nom,
            float lowCrit,
            float lowWarn,
            float highWarn,
            float highCrit
    ) {
        if (v >= lowWarn * nom && v <= highWarn * nom) {
            return SpecialValueRange.OK;
        }
        if ((v >= lowCrit * nom && v < lowWarn * nom) ||
                (v > highWarn * nom && v <= highCrit * nom)) {
            return SpecialValueRange.WARN;
        }
        return SpecialValueRange.CRITICAL;
    }

    private String explainRelative(
            float v,
            float nom,
            String unit,
            float lowCrit,
            float lowWarn,
            float highWarn,
            float highCrit
    ) {

        float okLow = lowWarn * nom;
        float okHigh = highWarn * nom;
        float warnLow1 = lowCrit * nom;
        float warnHigh2 = highCrit * nom;

        return switch (resolveRelative(v, nom, lowCrit, lowWarn, highWarn, highCrit)) {

            case OK -> String.format(
                    "%s: %.4f %s within OK range [%.4f .. %.4f]",
                    type, v, unit, okLow, okHigh
            );

            case WARN -> String.format(
                    "%s: %.4f %s within WARN band (%.4f .. %.4f)",
                    type, v, unit, warnLow1, warnHigh2
            );

            case CRITICAL -> String.format(
                    "%s: %.4f %s outside allowed band (%.4f .. %.4f) → CRITICAL",
                    type, v, unit, warnLow1, warnHigh2
            );
        };
    }

    /**
     * Температура отдельно (симметрично вокруг номинала)
     */
    private SpecialValueRange resolveTemperature(float v, float nom) {
        if (v >= nom - 5f && v <= nom + 5f) {
            return SpecialValueRange.OK;
        }
        if ((v >= nom - 10f && v < nom - 5f) ||
                (v > nom + 5f && v <= nom + 10f)) {
            return SpecialValueRange.WARN;
        }
        return SpecialValueRange.CRITICAL;
    }

    private String explainTemperature(float v, float nom) {

        float okLow = nom - 5f;
        float okHigh = nom + 5f;
        float warnLow = nom - 10f;
        float warnHigh = nom + 10f;

        SpecialValueRange r = resolveTemperature(v, nom);

        return switch (r) {

            case OK -> String.format(
                    "%s: %.4f °C within OK range [%.2f .. %.2f]",
                    type, v, okLow, okHigh
            );

            case WARN -> String.format(
                    "%s: %.4f °C within WARN band (%.2f .. %.2f)",
                    type, v, warnLow, warnHigh
            );

            case CRITICAL -> String.format(
                    "%s: %.4f °C outside ±10°C from nominal %.2f → CRITICAL",
                    type, v, nom
            );
        };
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