package io.github.tshepomabs16.flightgeo.conformance;

import java.util.List;

/**
 * One reason behind the verdict.
 *
 * @param type what kind of reason it is
 * @param waypoints the waypoint numbers it concerns, counting from 1; empty when it concerns none
 * @param message the reason as a plain-English sentence
 */
public record Finding(Type type, List<Integer> waypoints, String message) {

    /** The kinds of reason. */
    public enum Type {
        /** The flight was aborted. */
        ABORTED,
        /** No reading was recorded. */
        NOT_TRACKED,
        /** One or more waypoints were never reached. */
        WAYPOINTS_MISSED,
        /** Waypoints were reached in a different order from the plan. */
        OUT_OF_ORDER,
        /** One or more waypoints were flown off their planned height. */
        OFF_PLANNED_HEIGHT,
        /** The flight entered restricted zones, or the caller reported breaches. */
        ZONE_BREACHES,
        /** The flight went above the altitude limit. */
        ALTITUDE_LIMIT_EXCEEDED
    }

    /** Copies the waypoint list. */
    public Finding {
        waypoints = List.copyOf(waypoints);
    }
}
