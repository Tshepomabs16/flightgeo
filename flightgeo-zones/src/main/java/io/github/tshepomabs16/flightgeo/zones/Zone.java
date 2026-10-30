package io.github.tshepomabs16.flightgeo.zones;

import java.util.List;

/** An area on the map with an optional vertical band: a {@link PolygonZone} or a {@link CircleZone}. */
public sealed interface Zone permits PolygonZone, CircleZone {

    /** Your identifier for the zone; {@link ZoneMonitor} keys its memory on it. */
    String id();

    AltitudeBand band();

    static PolygonZone polygon(String id, List<GeoPoint> vertices) {
        return new PolygonZone(id, vertices, AltitudeBand.UNLIMITED);
    }

    static PolygonZone polygon(String id, List<GeoPoint> vertices, AltitudeBand band) {
        return new PolygonZone(id, vertices, band);
    }

    static CircleZone circle(String id, GeoPoint centre, double radiusMetres) {
        return new CircleZone(id, centre, radiusMetres, AltitudeBand.UNLIMITED);
    }

    static CircleZone circle(String id, GeoPoint centre, double radiusMetres, AltitudeBand band) {
        return new CircleZone(id, centre, radiusMetres, band);
    }
}
