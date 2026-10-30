package io.github.tshepomabs16.flightgeo.zones;

import java.util.Objects;

/**
 * A circle on the ground around a centre, with a radius in metres. The centre's altitude is ignored.
 *
 * @param id your identifier for the zone
 * @param centre the middle of the circle
 * @param radiusMetres the radius in metres, greater than zero
 * @param band the heights the zone covers; null means every height
 */
public record CircleZone(String id, GeoPoint centre, double radiusMetres, AltitudeBand band) implements Zone {

    /** Refuses a missing id or centre and a radius that is not greater than zero. */
    public CircleZone {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(centre, "centre");
        if (!Double.isFinite(radiusMetres) || radiusMetres <= 0) {
            throw new IllegalArgumentException("A circle needs a radius greater than zero, got " + radiusMetres);
        }
        band = band == null ? AltitudeBand.UNLIMITED : band;
    }
}
