package io.github.tshepomabs16.flightgeo.zones;

import java.util.Objects;

/** A circle on the ground around a centre, with a radius in metres. The centre's altitude is ignored. */
public record CircleZone(String id, GeoPoint centre, double radiusMetres, AltitudeBand band) implements Zone {

    public CircleZone {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(centre, "centre");
        if (!Double.isFinite(radiusMetres) || radiusMetres <= 0) {
            throw new IllegalArgumentException("A circle needs a radius greater than zero, got " + radiusMetres);
        }
        band = band == null ? AltitudeBand.UNLIMITED : band;
    }
}
