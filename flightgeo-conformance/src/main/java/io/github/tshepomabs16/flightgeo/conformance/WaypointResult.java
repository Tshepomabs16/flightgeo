package io.github.tshepomabs16.flightgeo.conformance;

import java.time.Instant;

/**
 * How one waypoint was flown. Values are null when they can't be known.
 *
 * @param sequence the waypoint's number, counting from 1
 * @param waypoint the planned waypoint
 * @param reached whether the track came within the reached radius
 * @param closestMetres how close the track came, or null with no track
 * @param closestAt when it was closest, or null with no track
 * @param firstReachedAt when it first came within the radius, or null if never
 * @param flownAltitude the altitude when it left the waypoint, or null if never reached
 * @param plannedArrivalSeconds when it was planned to arrive, in seconds from take-off
 * @param actualArrivalSeconds when it arrived, in seconds from take-off, or null if never reached
 */
public record WaypointResult(int sequence, Waypoint waypoint, boolean reached, Double closestMetres, Instant closestAt,
                             Instant firstReachedAt, Double flownAltitude, Integer plannedArrivalSeconds,
                             Integer actualArrivalSeconds) {

    /** {@return the planned height, or null when the plan gives none} */
    public Double plannedAltitude() {
        return waypoint.plannedAltitude();
    }

    /** {@return flown minus planned height, or null when either is unknown} */
    public Double altitudeDifference() {
        Double planned = plannedAltitude();
        return planned == null || flownAltitude == null ? null : flownAltitude - planned;
    }
}
