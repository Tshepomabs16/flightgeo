package io.github.tshepomabs16.flightgeo.conformance;

/** The route-wide figures compared in a report. */
public enum Measure {
    /** The route's length in metres. */
    DISTANCE("Distance", "m"),
    /** Time in the air in seconds. */
    FLIGHT_TIME("Flight time", "s"),
    /** The highest altitude in metres. */
    HIGHEST_ALTITUDE("Highest altitude", "m");

    private final String label;
    private final String unit;

    Measure(String label, String unit) {
        this.label = label;
        this.unit = unit;
    }

    /** {@return the name to show, such as "Distance"} */
    public String label() {
        return label;
    }

    /** {@return the unit, "m" or "s"} */
    public String unit() {
        return unit;
    }
}
