package io.github.tshepomabs16.flightgeo.conformance;

/** The route-wide figures compared in a report. */
public enum Measure {
    DISTANCE("Distance", "m"),
    FLIGHT_TIME("Flight time", "s"),
    HIGHEST_ALTITUDE("Highest altitude", "m");

    private final String label;
    private final String unit;

    Measure(String label, String unit) {
        this.label = label;
        this.unit = unit;
    }

    public String label() {
        return label;
    }

    public String unit() {
        return unit;
    }
}
