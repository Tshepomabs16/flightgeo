package io.github.tshepomabs16.flightgeo.zones;

/** Where a route first meets a zone: a waypoint inside it, or the leg starting at {@code index} entering it. */
public record RouteConflict(Zone zone, Kind kind, int index) {

    public enum Kind {
        /** The waypoint at {@code index} is inside the zone. */
        WAYPOINT_INSIDE,
        /** The leg from waypoint {@code index} to the next one passes through the zone. */
        LEG_CROSSES
    }
}
