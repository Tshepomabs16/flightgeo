package io.github.tshepomabs16.flightgeo.conformance;

import io.github.tshepomabs16.flightgeo.zones.GeoPoint;
import java.time.Instant;
import java.util.Objects;

/**
 * One recorded reading.
 *
 * @param time when it was taken
 * @param position where, with the altitude if known
 * @param groundSpeedMs the ground speed in metres per second, or null when it isn't known
 */
public record TrackFix(Instant time, GeoPoint position, Double groundSpeedMs) {

    /** Refuses a missing time or position and a negative or non-finite speed. */
    public TrackFix {
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(position, "position");
        if (groundSpeedMs != null && (!Double.isFinite(groundSpeedMs) || groundSpeedMs < 0)) {
            throw new IllegalArgumentException("Ground speed must be a finite number of zero or more");
        }
    }

    /**
     * A reading from plain values.
     *
     * @param time when it was taken
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param altitude the altitude in metres, or null
     * @param groundSpeedMs the ground speed in metres per second, or null
     * @return the reading
     */
    public static TrackFix of(Instant time, double latitude, double longitude, Double altitude, Double groundSpeedMs) {
        return new TrackFix(time, new GeoPoint(latitude, longitude, altitude), groundSpeedMs);
    }

    /** {@return the reading's altitude, or null when it isn't known} */
    public Double altitude() {
        return position.altitude();
    }
}
