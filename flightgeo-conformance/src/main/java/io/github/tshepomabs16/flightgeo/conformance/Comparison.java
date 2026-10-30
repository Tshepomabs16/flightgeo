package io.github.tshepomabs16.flightgeo.conformance;

/** Planned against actual for one measure; either side is null when it isn't known. */
public record Comparison(Measure measure, Double planned, Double actual) {

    /** Actual minus planned, or null when either is unknown. */
    public Double difference() {
        return planned == null || actual == null ? null : actual - planned;
    }
}
