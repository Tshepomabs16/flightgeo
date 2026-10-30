package io.github.tshepomabs16.flightgeo.conformance;

import io.github.tshepomabs16.flightgeo.zones.GeoPoint;
import java.time.Instant;
import java.util.Objects;

/** One recorded reading: when, where (with altitude if known) and ground speed in m/s if known. */
public record TrackFix(Instant time, GeoPoint position, Double groundSpeedMs) {

    public TrackFix {
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(position, "position");
        if (groundSpeedMs != null && (!Double.isFinite(groundSpeedMs) || groundSpeedMs < 0)) {
            throw new IllegalArgumentException("Ground speed must be a finite number of zero or more");
        }
    }

    public static TrackFix of(Instant time, double latitude, double longitude, Double altitude, Double groundSpeedMs) {
        return new TrackFix(time, new GeoPoint(latitude, longitude, altitude), groundSpeedMs);
    }

    public Double altitude() {
        return position.altitude();
    }
}
