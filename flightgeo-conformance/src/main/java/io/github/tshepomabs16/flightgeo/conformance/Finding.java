package io.github.tshepomabs16.flightgeo.conformance;

import java.util.List;

/** One reason behind the verdict, with the waypoint numbers it concerns and a plain-English sentence. */
public record Finding(Type type, List<Integer> waypoints, String message) {

    public enum Type { ABORTED, NOT_TRACKED, WAYPOINTS_MISSED, OUT_OF_ORDER, OFF_PLANNED_HEIGHT, ZONE_BREACHES, ALTITUDE_LIMIT_EXCEEDED }

    public Finding {
        waypoints = List.copyOf(waypoints);
    }
}
