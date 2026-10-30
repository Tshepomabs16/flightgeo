package io.github.tshepomabs16.flightgeo.zones;

/** Segment intersection on the local flat plane, in metres. */
final class Segments {

    private static final double EPSILON_METRES = 1e-6;

    private Segments() {}

    /** True when segment a-b and segment c-d share any point, touching included. */
    static boolean intersect(double[] a, double[] b, double[] c, double[] d) {
        int o1 = side(a, b, c);
        int o2 = side(a, b, d);
        int o3 = side(c, d, a);
        int o4 = side(c, d, b);
        if (o1 != o2 && o3 != o4 && o1 != 0 && o2 != 0 && o3 != 0 && o4 != 0) {
            return true;
        }
        return (o1 == 0 && within(a, b, c)) || (o2 == 0 && within(a, b, d))
                || (o3 == 0 && within(c, d, a)) || (o4 == 0 && within(c, d, b));
    }

    private static int side(double[] p, double[] q, double[] r) {
        double cross = (q[0] - p[0]) * (r[1] - p[1]) - (q[1] - p[1]) * (r[0] - p[0]);
        return Math.abs(cross) < EPSILON_METRES ? 0 : (cross > 0 ? 1 : -1);
    }

    private static boolean within(double[] p, double[] q, double[] r) {
        return Math.min(p[0], q[0]) - EPSILON_METRES <= r[0] && r[0] <= Math.max(p[0], q[0]) + EPSILON_METRES
                && Math.min(p[1], q[1]) - EPSILON_METRES <= r[1] && r[1] <= Math.max(p[1], q[1]) + EPSILON_METRES;
    }
}
