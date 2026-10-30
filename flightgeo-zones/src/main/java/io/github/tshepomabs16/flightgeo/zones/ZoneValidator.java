package io.github.tshepomabs16.flightgeo.zones;

import java.util.ArrayList;
import java.util.List;

/** Finds what is wrong with a zone before you rely on it: crossed edges, an upside-down band, a missing position. */
public final class ZoneValidator {

    private static final double MIN_AREA_SQUARE_METRES = 0.01;

    private ZoneValidator() {}

    /**
     * Every reason a zone can't be relied on, as plain sentences.
     *
     * @param zone the zone to check
     * @return the problems found; empty when it is fine
     */
    public static List<String> problems(Zone zone) {
        return problems(zone, null);
    }

    /**
     * Every reason a zone can't be relied on, including a floor no permitted flight could reach.
     *
     * @param zone the zone to check
     * @param operatingCeilingMetres the highest permitted altitude, or null to skip that check
     * @return the problems found; empty when it is fine
     */
    public static List<String> problems(Zone zone, Double operatingCeilingMetres) {
        List<String> out = new ArrayList<>();
        if (zone.id().isBlank()) {
            out.add("the zone needs an id");
        }
        AltitudeBand band = zone.band();
        if (band.floor() != null && band.ceiling() != null && band.floor() >= band.ceiling()) {
            out.add("the floor must be below the ceiling");
        }
        if (operatingCeilingMetres != null && band.floor() != null && band.floor() >= operatingCeilingMetres) {
            out.add("the floor (" + format(band.floor()) + " m) is at or above the operating ceiling ("
                    + format(operatingCeilingMetres) + " m), so the zone could never apply");
        }
        if (zone instanceof CircleZone circle) {
            if (atNullIsland(circle.centre())) {
                out.add("the centre is at 0, 0, which is what a missing position usually looks like");
            }
            return out;
        }
        List<GeoPoint> corners = ((PolygonZone) zone).vertices();
        if (corners.size() < 3) {
            out.add("a polygon needs at least three corners");
            return out;
        }
        if (corners.stream().anyMatch(ZoneValidator::atNullIsland)) {
            out.add("a corner is at 0, 0, which is what a missing position usually looks like");
        }
        double[][] xy = new double[corners.size()][];
        for (int i = 0; i < corners.size(); i++) {
            xy[i] = Geodesy.project(corners.get(i).latitude(), corners.get(i).longitude(),
                    corners.get(0).latitude(), corners.get(0).longitude());
        }
        if (crossesItself(xy)) {
            out.add("the polygon's edges cross each other");
        } else if (Math.abs(area(xy)) < MIN_AREA_SQUARE_METRES) {
            out.add("the polygon has no area: its corners lie on one line");
        }
        return out;
    }

    /**
     * Throws when a zone has any problem.
     *
     * @param zone the zone to check
     * @throws IllegalArgumentException listing the problems, when there are any
     */
    public static void requireValid(Zone zone) {
        List<String> problems = problems(zone);
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException("Zone '" + zone.id() + "' is not valid: " + String.join("; ", problems));
        }
    }

    private static boolean crossesItself(double[][] xy) {
        int n = xy.length;
        if (n < 4) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 2; j < n; j++) {
                if (i == 0 && j == n - 1) {
                    continue;
                }
                if (Segments.intersect(xy[i], xy[(i + 1) % n], xy[j], xy[(j + 1) % n])) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double area(double[][] xy) {
        double twice = 0;
        for (int i = 0; i < xy.length; i++) {
            double[] a = xy[i];
            double[] b = xy[(i + 1) % xy.length];
            twice += a[0] * b[1] - b[0] * a[1];
        }
        return twice / 2;
    }

    private static boolean atNullIsland(GeoPoint p) {
        return p.latitude() == 0 && p.longitude() == 0;
    }

    private static String format(double metres) {
        return metres == Math.rint(metres) ? String.valueOf((long) metres) : String.valueOf(metres);
    }
}
