package io.github.tshepomabs16.flightgeo.conformance;

import java.time.Instant;

/**
 * How one waypoint was flown. Times and heights are null when they can't be known: a waypoint never
 * reached has no flown height or arrival. Arrival seconds count from take-off.
 */
public record WaypointResult(int sequence, Waypoint waypoint, boolean reached, Double closestMetres, Instant closestAt,
                             Instant firstReachedAt, Double flownAltitude, Integer plannedArrivalSeconds,
                             Integer actualArrivalSeconds) {

    public Double plannedAltitude() {
        return waypoint.plannedAltitude();
    }

    /** Flown minus planned height, or null when either is unknown. */
    public Double altitudeDifference() {
        Double planned = plannedAltitude();
        return planned == null || flownAltitude == null ? null : flownAltitude - planned;
    }
}
