package io.github.tshepomabs16.flightgeo.zones;

/**
 * A WGS 84 position in decimal degrees, with an optional altitude in metres.
 * Altitudes are compared as given, so keep one datum (above take-off, above sea level) throughout.
 *
 * @param latitude the latitude, from -90 to 90
 * @param longitude the longitude, from -180 to 180
 * @param altitude the altitude in metres, or null when it isn't known
 */
public record GeoPoint(double latitude, double longitude, Double altitude) {

    /** Refuses a position outside the valid ranges and an altitude that is not a finite number. */
    public GeoPoint {
        Coordinates.problem(latitude, longitude, true).ifPresent(problem -> {
            throw new IllegalArgumentException("Position " + problem);
        });
        if (altitude != null && !Double.isFinite(altitude)) {
            throw new IllegalArgumentException("Altitude is not a number");
        }
    }

    /**
     * A position without an altitude.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return the point
     */
    public static GeoPoint of(double latitude, double longitude) {
        return new GeoPoint(latitude, longitude, null);
    }

    /**
     * A position with an altitude.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param altitude the altitude in metres
     * @return the point
     */
    public static GeoPoint of(double latitude, double longitude, double altitude) {
        return new GeoPoint(latitude, longitude, altitude);
    }

    /** {@return true when the altitude is known} */
    public boolean hasAltitude() {
        return altitude != null;
    }

    /**
     * The same position at another altitude.
     *
     * @param newAltitude the altitude in metres, or null for unknown
     * @return a new point
     */
    public GeoPoint withAltitude(Double newAltitude) {
        return new GeoPoint(latitude, longitude, newAltitude);
    }

    /**
     * Great-circle distance to another point, ignoring altitude.
     *
     * @param other the other point
     * @return the distance in metres
     */
    public double distanceTo(GeoPoint other) {
        return Geodesy.distanceMetres(this, other);
    }
}
