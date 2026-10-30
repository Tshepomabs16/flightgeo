package io.github.tshepomabs16.flightgeo.conformance;

/**
 * Planned against actual for one measure; either side is null when it isn't known.
 *
 * @param measure what is compared
 * @param planned the planned value, in the measure's unit
 * @param actual the flown value, in the measure's unit
 */
public record Comparison(Measure measure, Double planned, Double actual) {

    /** {@return actual minus planned, or null when either is unknown} */
    public Double difference() {
        return planned == null || actual == null ? null : actual - planned;
    }
}
