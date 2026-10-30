package io.github.tshepomabs16.flightgeo.zones;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Answers whether points, legs and routes are in zones, horizontally and within each zone's band.
 * Immutable and thread-safe; start from {@link #defaults()} and adjust with the {@code with} methods.
 */
public final class ZoneChecker {

    /** A point within this many metres of a polygon's edge counts as inside: the edge is part of the zone. */
    public static final double DEFAULT_EDGE_TOLERANCE_METRES = 0.5;

    private static final ZoneChecker DEFAULTS = new ZoneChecker(DEFAULT_EDGE_TOLERANCE_METRES, UnknownAltitude.WITHIN_BAND);

    private final double edgeToleranceMetres;
    private final UnknownAltitude unknownAltitude;

    private ZoneChecker(double edgeToleranceMetres, UnknownAltitude unknownAltitude) {
        if (!Double.isFinite(edgeToleranceMetres) || edgeToleranceMetres < 0) {
            throw new IllegalArgumentException("Edge tolerance must be zero or more metres");
        }
        this.edgeToleranceMetres = edgeToleranceMetres;
        this.unknownAltitude = Objects.requireNonNull(unknownAltitude, "unknownAltitude");
    }

    /** Half a metre of edge tolerance, and an unknown altitude treated as within a zone's band. */
    public static ZoneChecker defaults() {
        return DEFAULTS;
    }

    public ZoneChecker withEdgeTolerance(double metres) {
        return new ZoneChecker(metres, unknownAltitude);
    }

    public ZoneChecker withUnknownAltitude(UnknownAltitude policy) {
        return new ZoneChecker(edgeToleranceMetres, policy);
    }

    public double edgeToleranceMetres() {
        return edgeToleranceMetres;
    }

    public UnknownAltitude unknownAltitude() {
        return unknownAltitude;
    }

    /** True when the point is inside the zone's area and within its band. */
    public boolean contains(Zone zone, GeoPoint point) {
        Objects.requireNonNull(zone, "zone");
        Objects.requireNonNull(point, "point");
        if (!withinBand(zone.band(), point.altitude())) {
            return false;
        }
        return insideArea(zone, point);
    }

    /** True when any part of the straight leg from {@code from} to {@code to} is in the zone. */
    public boolean legTouches(Zone zone, GeoPoint from, GeoPoint to) {
        Objects.requireNonNull(zone, "zone");
        if (!bandMeetsLeg(zone.band(), from.altitude(), to.altitude())) {
            return false;
        }
        if (zone instanceof CircleZone circle) {
            return Geodesy.distanceToSegmentMetres(circle.centre(), from, to) <= circle.radiusMetres();
        }
        PolygonZone polygon = (PolygonZone) zone;
        List<GeoPoint> corners = polygon.vertices();
        if (corners.size() < 3) {
            return false;
        }
        if (insideArea(polygon, from) || insideArea(polygon, to)) {
            return true;
        }
        double[] a = {0, 0};
        double[] b = Geodesy.project(to.latitude(), to.longitude(), from.latitude(), from.longitude());
        double[][] xy = projected(corners, from);
        for (int i = 0; i < xy.length; i++) {
            if (Segments.intersect(a, b, xy[i], xy[(i + 1) % xy.length])) {
                return true;
            }
        }
        return false;
    }

    /** True when any waypoint of the route is in the zone, or any leg between them passes through it. */
    public boolean routeTouches(Zone zone, List<GeoPoint> route) {
        return firstConflict(zone, route) != null;
    }

    /** The first place the route meets each zone, in the zones' order; zones it never meets are left out. */
    public List<RouteConflict> routeConflicts(Collection<? extends Zone> zones, List<GeoPoint> route) {
        List<RouteConflict> out = new ArrayList<>();
        for (Zone zone : zones) {
            RouteConflict conflict = firstConflict(zone, route);
            if (conflict != null) {
                out.add(conflict);
            }
        }
        return out;
    }

    /** The zones that contain the point. */
    public List<Zone> zonesContaining(Collection<? extends Zone> zones, GeoPoint point) {
        List<Zone> out = new ArrayList<>();
        for (Zone zone : zones) {
            if (contains(zone, point)) {
                out.add(zone);
            }
        }
        return out;
    }

    private RouteConflict firstConflict(Zone zone, List<GeoPoint> route) {
        Objects.requireNonNull(route, "route");
        for (int i = 0; i < route.size(); i++) {
            if (contains(zone, route.get(i))) {
                return new RouteConflict(zone, RouteConflict.Kind.WAYPOINT_INSIDE, i);
            }
            if (i + 1 < route.size() && legTouches(zone, route.get(i), route.get(i + 1))) {
                return new RouteConflict(zone, RouteConflict.Kind.LEG_CROSSES, i);
            }
        }
        return null;
    }

    private boolean insideArea(Zone zone, GeoPoint point) {
        if (zone instanceof CircleZone circle) {
            return Geodesy.distanceMetres(point, circle.centre()) <= circle.radiusMetres();
        }
        List<GeoPoint> corners = ((PolygonZone) zone).vertices();
        if (corners.size() < 3) {
            return false;
        }
        double[][] xy = projected(corners, point);
        double[] origin = {0, 0};
        for (int i = 0; i < xy.length; i++) {
            if (Geodesy.distanceToSegment(origin, xy[i], xy[(i + 1) % xy.length]) <= edgeToleranceMetres) {
                return true;
            }
        }
        // Ray casting along east from the point, which sits at the origin of the projection.
        boolean inside = false;
        for (int i = 0, j = xy.length - 1; i < xy.length; j = i++) {
            double yi = xy[i][1];
            double yj = xy[j][1];
            if ((yi > 0) != (yj > 0)) {
                double xCross = xy[i][0] + (0 - yi) * (xy[j][0] - xy[i][0]) / (yj - yi);
                if (xCross > 0) {
                    inside = !inside;
                }
            }
        }
        return inside;
    }

    private boolean withinBand(AltitudeBand band, Double altitude) {
        if (band.isUnlimited()) {
            return true;
        }
        if (altitude == null) {
            return unknownAltitude == UnknownAltitude.WITHIN_BAND;
        }
        return band.contains(altitude);
    }

    private boolean bandMeetsLeg(AltitudeBand band, Double a, Double b) {
        if (band.isUnlimited()) {
            return true;
        }
        if (a == null || b == null) {
            return unknownAltitude == UnknownAltitude.WITHIN_BAND;
        }
        return band.overlaps(a, b);
    }

    private static double[][] projected(List<GeoPoint> corners, GeoPoint origin) {
        double[][] xy = new double[corners.size()][];
        for (int i = 0; i < corners.size(); i++) {
            GeoPoint c = corners.get(i);
            xy[i] = Geodesy.project(c.latitude(), c.longitude(), origin.latitude(), origin.longitude());
        }
        return xy;
    }
}
