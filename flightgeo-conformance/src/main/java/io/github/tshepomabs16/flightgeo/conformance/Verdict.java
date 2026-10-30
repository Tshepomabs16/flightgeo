package io.github.tshepomabs16.flightgeo.conformance;

/** One answer to "was the route flown as planned?". */
public enum Verdict {
    /** Every waypoint reached, in order, at height, with no breaches. */
    AS_PLANNED("Completed as planned"),
    /** Every waypoint reached, but something else went wrong; see the findings. */
    WITH_DEVIATIONS("Completed with deviations"),
    /** At least one waypoint was never reached. */
    PARTLY_COMPLETED("Partly completed"),
    /** The flight was aborted. */
    NOT_COMPLETED("Not completed"),
    /** Nothing was recorded, so nothing can be checked. */
    NOT_VERIFIED("Not verified");

    private final String label;

    Verdict(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
