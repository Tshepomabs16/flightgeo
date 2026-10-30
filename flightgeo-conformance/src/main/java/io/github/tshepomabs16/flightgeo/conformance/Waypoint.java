package io.github.tshepomabs16.flightgeo.conformance;

import io.github.tshepomabs16.flightgeo.zones.GeoPoint;
import java.util.Objects;

/** A planned point. Its altitude, if set, is the planned height; {@code holdSeconds} is the time planned there. */
public record Waypoint(GeoPoint position, int holdSeconds) {

    public Waypoint {
        Objects.requireNonNull(position, "position");
        if (holdSeconds < 0) {
            throw new IllegalArgumentException("Hold time cannot be negative");
        }
    }

    public static Waypoint at(double latitude, double longitude) {
        return new Waypoint(GeoPoint.of(latitude, longitude), 0);
    }

    public static Waypoint at(double latitude, double longitude, double altitude) {
        return new Waypoint(GeoPoint.of(latitude, longitude, altitude), 0);
    }

    public Waypoint holding(int seconds) {
        return new Waypoint(position, seconds);
    }

    public Double plannedAltitude() {
        return position.altitude();
    }
}
