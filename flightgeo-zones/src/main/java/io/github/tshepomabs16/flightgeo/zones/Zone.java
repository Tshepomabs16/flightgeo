package io.github.tshepomabs16.flightgeo.zones;

import java.util.List;

/** An area on the map with an optional vertical band: a {@link PolygonZone} or a {@link CircleZone}. */
public sealed interface Zone permits PolygonZone, CircleZone {

    /** {@return your identifier for the zone; {@link ZoneMonitor} keys its memory on it} */
    String id();

    /** {@return the heights the zone covers} */
    AltitudeBand band();

    /**
     * A polygon zone at every height.
     *
     * @param id your identifier for the zone
     * @param vertices the corners in order
     * @return the zone
     */
    static PolygonZone polygon(String id, List<GeoPoint> vertices) {
        return new PolygonZone(id, vertices, AltitudeBand.UNLIMITED);
    }

    /**
     * A polygon zone covering a band of heights.
     *
     * @param id your identifier for the zone
     * @param vertices the corners in order
     * @param band the heights the zone covers
     * @return the zone
     */
    static PolygonZone polygon(String id, List<GeoPoint> vertices, AltitudeBand band) {
        return new PolygonZone(id, vertices, band);
    }

    /**
     * A circle zone at every height.
     *
     * @param id your identifier for the zone
     * @param centre the middle of the circle
     * @param radiusMetres the radius in metres
     * @return the zone
     */
    static CircleZone circle(String id, GeoPoint centre, double radiusMetres) {
        return new CircleZone(id, centre, radiusMetres, AltitudeBand.UNLIMITED);
    }

    /**
     * A circle zone covering a band of heights.
     *
     * @param id your identifier for the zone
     * @param centre the middle of the circle
     * @param radiusMetres the radius in metres
     * @param band the heights the zone covers
     * @return the zone
     */
    static CircleZone circle(String id, GeoPoint centre, double radiusMetres, AltitudeBand band) {
        return new CircleZone(id, centre, radiusMetres, band);
    }
}
