package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.get_data.infrastructure.generators.universal_valves;

import java.util.concurrent.ThreadLocalRandom;

public class Valves implements UniversalValvesGeneratorInterface {
    private Valves.SpecialValueRange range = null;

    public Valves(Valves.SpecialValueRange range) {
        this.range = range;
    }

    public Valves() {
        this(null);
    }

    public ValveState generate() {
        return generateValveState(range);
    }

    private ValveState generateValveState(SpecialValueRange range) {
        if (range == null) {
            int v = ThreadLocalRandom.current().nextInt(4);
            return switch (v) {
                case 0 -> ValveState.OPEN;
                case 1 -> ValveState.CLOSED;
                case 2 -> ValveState.PROCESSED;
                default -> ValveState.NOT_STATED;
            };
        }
        return switch (range) {
            case OK -> ValveState.OPEN;
            case WARN -> ValveState.PROCESSED;
            case CRITICAL -> ValveState.NOT_STATED;
        };
    }

    @Override
    public ResolveResult resolve(ValveState state) {

        if (state == null) {
            return new ResolveResult(
                    SpecialValueRange.CRITICAL,
                    "VALVE: state is null → CRITICAL (no telemetry)"
            );
        }

        SpecialValueRange range = switch (state) {
            case OPEN, CLOSED -> SpecialValueRange.OK;
            case PROCESSED -> SpecialValueRange.WARN;
            case NOT_STATED -> SpecialValueRange.CRITICAL;
        };

        return new ResolveResult(range, explain(state, range));
    }

    private String explain(ValveState state, SpecialValueRange range) {

        return switch (range) {

            case OK -> switch (state) {
                case OPEN -> "VALVE: OPEN → OK (flow allowed)";
                case CLOSED -> "VALVE: CLOSED → OK (intentionally closed)";
                default -> "VALVE: " + state + " → OK";
            };

            case WARN -> "VALVE: PROCESSED → WARN (transition / intermediate state)";

            case CRITICAL -> switch (state) {
                case NOT_STATED -> "VALVE: NOT_STATED → CRITICAL (undefined state)";
                default -> "VALVE: " + state + " → CRITICAL";
            };
        };
    }
}