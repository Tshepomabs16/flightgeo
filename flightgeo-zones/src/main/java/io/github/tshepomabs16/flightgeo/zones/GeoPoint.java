package io.github.tshepomabs16.flightgeo.zones;

/**
 * A WGS 84 position in decimal degrees, with an optional altitude in metres.
 * Altitudes are compared as given, so keep one datum (above take-off, above sea level) throughout.
 */
public record GeoPoint(double latitude, double longitude, Double altitude) {

    public GeoPoint {
        Coordinates.problem(latitude, longitude, true).ifPresent(problem -> {
            throw new IllegalArgumentException("Position " + problem);
        });
        if (altitude != null && !Double.isFinite(altitude)) {
            throw new IllegalArgumentException("Altitude is not a number");
        }
    }

    public static GeoPoint of(double latitude, double longitude) {
        return new GeoPoint(latitude, longitude, null);
    }

    public static GeoPoint of(double latitude, double longitude, double altitude) {
        return new GeoPoint(latitude, longitude, altitude);
    }

    public boolean hasAltitude() {
        return altitude != null;
    }

    public GeoPoint withAltitude(Double newAltitude) {
        return new GeoPoint(latitude, longitude, newAltitude);
    }

    /** Great-circle distance to another point in metres, ignoring altitude. */
    public double distanceTo(GeoPoint other) {
        return Geodesy.distanceMetres(this, other);
    }
}
