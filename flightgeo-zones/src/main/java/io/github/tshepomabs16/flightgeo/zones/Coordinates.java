package io.github.tshepomabs16.flightgeo.zones;

import java.util.Optional;

/** One rule for whether a latitude/longitude pair is a usable position. */
public final class Coordinates {

    private Coordinates() {}

    /**
     * Why a pair is not a usable position. Exactly 0, 0 counts as a missing fix.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return the reason, or empty when the position is usable
     */
    public static Optional<String> problem(double latitude, double longitude) {
        return problem(latitude, longitude, false);
    }

    /**
     * Why a pair is not a usable position, optionally accepting 0, 0.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param allowNullIsland true to accept exactly 0, 0 as a real position
     * @return the reason, or empty when the position is usable
     */
    public static Optional<String> problem(double latitude, double longitude, boolean allowNullIsland) {
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            return Optional.of("is not a number");
        }
        if (latitude < -90 || latitude > 90) {
            return Optional.of("has a latitude of " + latitude + ", outside -90 to 90");
        }
        if (longitude < -180 || longitude > 180) {
            return Optional.of("has a longitude of " + longitude + ", outside -180 to 180");
        }
        // 0, 0 is open ocean: in practice it is what an unset or failed GPS fix reports.
        if (!allowNullIsland && latitude == 0 && longitude == 0) {
            return Optional.of("is at 0, 0, which is what a missing GPS fix usually reports");
        }
        return Optional.empty();
    }

    /**
     * Whether a pair is a usable position, with 0, 0 counted as missing.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return true when {@link #problem(double, double)} finds nothing wrong
     */
    public static boolean isUsable(double latitude, double longitude) {
        return problem(latitude, longitude).isEmpty();
    }
}
