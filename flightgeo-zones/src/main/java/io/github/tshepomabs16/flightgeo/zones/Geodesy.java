package io.github.tshepomabs16.flightgeo.zones;

/** Distances on a spherical Earth, plus the local flat projection the zone checks use. */
public final class Geodesy {

    /** Mean Earth radius, as used by the haversine formula. */
    public static final double EARTH_RADIUS_METRES = 6_371_000.0;

    private static final double METRES_PER_DEGREE = Math.PI * EARTH_RADIUS_METRES / 180.0;

    private Geodesy() {}

    /**
     * Great-circle (haversine) distance between two positions.
     *
     * @param lat1 the first latitude in decimal degrees
     * @param lon1 the first longitude in decimal degrees
     * @param lat2 the second latitude in decimal degrees
     * @param lon2 the second longitude in decimal degrees
     * @return the distance in metres
     */
    public static double distanceMetres(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_METRES * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * Great-circle distance between two points, ignoring altitude.
     *
     * @param a one point
     * @param b the other point
     * @return the distance in metres
     */
    public static double distanceMetres(GeoPoint a, GeoPoint b) {
        return distanceMetres(a.latitude(), a.longitude(), b.latitude(), b.longitude());
    }

    /**
     * Distance from a point to the nearest point of a straight segment. Accurate over a few kilometres.
     *
     * @param p the point
     * @param a one end of the segment
     * @param b the other end of the segment
     * @return the distance in metres
     */
    public static double distanceToSegmentMetres(GeoPoint p, GeoPoint a, GeoPoint b) {
        double[] pa = project(a.latitude(), a.longitude(), p.latitude(), p.longitude());
        double[] pb = project(b.latitude(), b.longitude(), p.latitude(), p.longitude());
        return distanceToSegment(new double[] {0, 0}, pa, pb);
    }

    /** East and north metres of a point from an origin on a local flat plane; longitude wraps at 180. */
    static double[] project(double lat, double lon, double lat0, double lon0) {
        double dLon = lon - lon0;
        if (dLon > 180) {
            dLon -= 360;
        } else if (dLon < -180) {
            dLon += 360;
        }
        return new double[] {dLon * METRES_PER_DEGREE * Math.cos(Math.toRadians(lat0)), (lat - lat0) * METRES_PER_DEGREE};
    }

    static double distanceToSegment(double[] p, double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double lengthSquared = dx * dx + dy * dy;
        double t = lengthSquared == 0 ? 0 : ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        return Math.hypot(p[0] - (a[0] + t * dx), p[1] - (a[1] + t * dy));
    }
}
