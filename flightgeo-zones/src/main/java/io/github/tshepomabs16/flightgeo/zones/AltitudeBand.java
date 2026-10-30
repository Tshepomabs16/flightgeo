package io.github.tshepomabs16.flightgeo.zones;

/**
 * A vertical extent in metres. A missing floor starts at the ground; a missing ceiling has no top.
 *
 * @param floor the lowest altitude in the band, or null for no floor
 * @param ceiling the highest altitude in the band, or null for no ceiling
 */
public record AltitudeBand(Double floor, Double ceiling) {

    /** No floor and no ceiling: the zone applies at every height. */
    public static final AltitudeBand UNLIMITED = new AltitudeBand(null, null);

    /** Refuses edges that are not finite numbers. */
    public AltitudeBand {
        if ((floor != null && !Double.isFinite(floor)) || (ceiling != null && !Double.isFinite(ceiling))) {
            throw new IllegalArgumentException("Band edges must be finite numbers");
        }
    }

    /**
     * A band with both a floor and a ceiling.
     *
     * @param floor the lowest altitude in metres
     * @param ceiling the highest altitude in metres
     * @return the band
     */
    public static AltitudeBand between(double floor, double ceiling) {
        return new AltitudeBand(floor, ceiling);
    }

    /**
     * A band from a floor upwards, with no ceiling.
     *
     * @param floor the lowest altitude in metres
     * @return the band
     */
    public static AltitudeBand from(double floor) {
        return new AltitudeBand(floor, null);
    }

    /**
     * A band from the ground up to a ceiling.
     *
     * @param ceiling the highest altitude in metres
     * @return the band
     */
    public static AltitudeBand upTo(double ceiling) {
        return new AltitudeBand(null, ceiling);
    }

    /** {@return true when the band has neither a floor nor a ceiling} */
    public boolean isUnlimited() {
        return floor == null && ceiling == null;
    }

    /**
     * Whether an altitude lies within the band, both edges included.
     *
     * @param altitude the altitude in metres
     * @return true when it is within the band
     */
    public boolean contains(double altitude) {
        return (floor == null || altitude >= floor) && (ceiling == null || altitude <= ceiling);
    }

    /**
     * Whether any height between two altitudes, given in either order, lies within the band.
     *
     * @param a one end of the range in metres
     * @param b the other end of the range in metres
     * @return true when the range and the band share any height
     */
    public boolean overlaps(double a, double b) {
        double low = Math.min(a, b);
        double high = Math.max(a, b);
        return (floor == null || high >= floor) && (ceiling == null || low <= ceiling);
    }
}
