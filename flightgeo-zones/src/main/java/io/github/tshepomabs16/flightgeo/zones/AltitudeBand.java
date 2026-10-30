package io.github.tshepomabs16.flightgeo.zones;

/** A vertical extent in metres. A missing floor starts at the ground; a missing ceiling has no top. */
public record AltitudeBand(Double floor, Double ceiling) {

    /** No floor and no ceiling: the zone applies at every height. */
    public static final AltitudeBand UNLIMITED = new AltitudeBand(null, null);

    public AltitudeBand {
        if ((floor != null && !Double.isFinite(floor)) || (ceiling != null && !Double.isFinite(ceiling))) {
            throw new IllegalArgumentException("Band edges must be finite numbers");
        }
    }

    public static AltitudeBand between(double floor, double ceiling) {
        return new AltitudeBand(floor, ceiling);
    }

    public static AltitudeBand from(double floor) {
        return new AltitudeBand(floor, null);
    }

    public static AltitudeBand upTo(double ceiling) {
        return new AltitudeBand(null, ceiling);
    }

    public boolean isUnlimited() {
        return floor == null && ceiling == null;
    }

    /** True when the altitude lies within the band, both edges included. */
    public boolean contains(double altitude) {
        return (floor == null || altitude >= floor) && (ceiling == null || altitude <= ceiling);
    }

    /** True when any height between {@code a} and {@code b}, in either order, lies within the band. */
    public boolean overlaps(double a, double b) {
        double low = Math.min(a, b);
        double high = Math.max(a, b);
        return (floor == null || high >= floor) && (ceiling == null || low <= ceiling);
    }
}
