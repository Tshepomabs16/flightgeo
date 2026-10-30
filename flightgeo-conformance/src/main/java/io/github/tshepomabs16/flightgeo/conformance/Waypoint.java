package io.github.tshepomabs16.flightgeo.conformance;

import io.github.tshepomabs16.flightgeo.zones.GeoPoint;
import java.util.Objects;

/**
 * A planned point in a route.
 *
 * @param position where, with the planned height as its altitude if there is one
 * @param holdSeconds how long the aircraft is planned to stay there
 */
public record Waypoint(GeoPoint position, int holdSeconds) {

    /** Refuses a missing position and a negative hold time. */
    public Waypoint {
        Objects.requireNonNull(position, "position");
        if (holdSeconds < 0) {
            throw new IllegalArgumentException("Hold time cannot be negative");
        }
    }

    /**
     * A waypoint with no planned height and no hold.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return the waypoint
     */
    public static Waypoint at(double latitude, double longitude) {
        return new Waypoint(GeoPoint.of(latitude, longitude), 0);
    }

    /**
     * A waypoint with a planned height and no hold.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param altitude the planned height in metres
     * @return the waypoint
     */
    public static Waypoint at(double latitude, double longitude, double altitude) {
        return new Waypoint(GeoPoint.of(latitude, longitude, altitude), 0);
    }

    /**
     * The same waypoint with a planned hold.
     *
     * @param seconds how long to stay there
     * @return a new waypoint
     */
    public Waypoint holding(int seconds) {
        return new Waypoint(position, seconds);
    }

    /** {@return the planned height in metres, or null when there is none} */
    public Double plannedAltitude() {
        return position.altitude();
    }
}
