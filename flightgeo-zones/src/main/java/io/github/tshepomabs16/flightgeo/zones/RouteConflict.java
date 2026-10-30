package io.github.tshepomabs16.flightgeo.zones;

/**
 * Where a route first meets a zone: a waypoint inside it, or the leg starting at {@code index} entering it.
 *
 * @param zone the zone met
 * @param kind whether a waypoint is inside it or a leg passes through it
 * @param index the waypoint's position in the route, counting from 0
 */
public record RouteConflict(Zone zone, Kind kind, int index) {

    /** How the route meets the zone. */
    public enum Kind {
        /** The waypoint at {@code index} is inside the zone. */
        WAYPOINT_INSIDE,
        /** The leg from waypoint {@code index} to the next one passes through the zone. */
        LEG_CROSSES
    }
}
