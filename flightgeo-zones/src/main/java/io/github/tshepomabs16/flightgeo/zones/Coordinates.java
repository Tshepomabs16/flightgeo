package io.github.tshepomabs16.flightgeo.zones;

import java.util.Optional;

/** One rule for whether a latitude/longitude pair is a usable position. */
public final class Coordinates {

    private Coordinates() {}

    /** Why the pair is not a usable position, or empty when it is. Exactly 0, 0 counts as a missing fix. */
    public static Optional<String> problem(double latitude, double longitude) {
        return problem(latitude, longitude, false);
    }

    /** As {@link #problem(double, double)}, but 0, 0 is accepted when {@code allowNullIsland} is true. */
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

    /** True when {@link #problem(double, double)} finds nothing wrong. */
    public static boolean isUsable(double latitude, double longitude) {
        return problem(latitude, longitude).isEmpty();
    }
}
