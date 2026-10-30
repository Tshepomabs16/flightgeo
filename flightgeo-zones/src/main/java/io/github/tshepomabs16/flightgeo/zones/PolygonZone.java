package io.github.tshepomabs16.flightgeo.zones;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A polygon given by its corners in order. The closing edge is implied, so a repeated first corner
 * (as in GeoJSON) is dropped. Corner altitudes are ignored; use the band for heights.
 *
 * @param id your identifier for the zone
 * @param vertices the corners in order
 * @param band the heights the zone covers; null means every height
 */
public record PolygonZone(String id, List<GeoPoint> vertices, AltitudeBand band) implements Zone {

    /** Copies the corners and drops a repeated closing corner. */
    public PolygonZone {
        Objects.requireNonNull(id, "id");
        List<GeoPoint> corners = new ArrayList<>(Objects.requireNonNull(vertices, "vertices"));
        corners.forEach(c -> Objects.requireNonNull(c, "vertex"));
        if (corners.size() > 1 && samePlace(corners.get(0), corners.get(corners.size() - 1))) {
            corners.remove(corners.size() - 1);
        }
        vertices = List.copyOf(corners);
        band = band == null ? AltitudeBand.UNLIMITED : band;
    }

    private static boolean samePlace(GeoPoint a, GeoPoint b) {
        return a.latitude() == b.latitude() && a.longitude() == b.longitude();
    }
}
