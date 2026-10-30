package io.github.tshepomabs16.flightgeo.conformance;

import java.time.Duration;
import java.util.Objects;

/**
 * The thresholds the checker works to. {@link #defaults()} suits small aircraft on routes of a few kilometres.
 *
 * @param reachedRadiusMetres a waypoint is reached when the track comes this close
 * @param altitudeToleranceMetres a reached waypoint flown further than this off its planned height is a deviation
 * @param returnRadiusMetres ending this close to the first waypoint counts as returning to launch
 * @param maxGap readings further apart are not joined when measuring or matching the track
 * @param movingSpeedMs a reading at or above this ground speed is moving
 * @param movingAltitudeMetres a reading at or above this altitude is airborne
 * @param defaultSpeedMs the planned speed when the flight gives none
 * @param plannedReturnToStart whether the planned route ends with a leg back to the first waypoint
 */
public record ConformanceOptions(double reachedRadiusMetres, double altitudeToleranceMetres, double returnRadiusMetres,
                                 Duration maxGap, double movingSpeedMs, double movingAltitudeMetres,
                                 double defaultSpeedMs, boolean plannedReturnToStart) {

    /** Refuses values that make no sense, such as a negative radius or an empty gap. */
    public ConformanceOptions {
        Objects.requireNonNull(maxGap, "maxGap");
        positive(reachedRadiusMetres, "reachedRadiusMetres");
        positive(returnRadiusMetres, "returnRadiusMetres");
        positive(defaultSpeedMs, "defaultSpeedMs");
        notNegative(altitudeToleranceMetres, "altitudeToleranceMetres");
        notNegative(movingSpeedMs, "movingSpeedMs");
        notNegative(movingAltitudeMetres, "movingAltitudeMetres");
        if (maxGap.isNegative() || maxGap.isZero()) {
            throw new IllegalArgumentException("maxGap must be longer than zero");
        }
    }

    /** {@return 10 m reach and height tolerance, 25 m return radius, 5 min gaps, 10 m/s assumed speed} */
    public static ConformanceOptions defaults() {
        return new ConformanceOptions(10, 10, 25, Duration.ofMinutes(5), 1.0, 2.0, 10.0, true);
    }

    /**
     * A copy with another reached radius.
     *
     * @param metres how close the track must come to a waypoint
     * @return new options
     */
    public ConformanceOptions withReachedRadius(double metres) {
        return new ConformanceOptions(metres, altitudeToleranceMetres, returnRadiusMetres, maxGap, movingSpeedMs,
                movingAltitudeMetres, defaultSpeedMs, plannedReturnToStart);
    }

    /**
     * A copy with another height tolerance.
     *
     * @param metres how far off the planned height is still on plan
     * @return new options
     */
    public ConformanceOptions withAltitudeTolerance(double metres) {
        return new ConformanceOptions(reachedRadiusMetres, metres, returnRadiusMetres, maxGap, movingSpeedMs,
                movingAltitudeMetres, defaultSpeedMs, plannedReturnToStart);
    }

    /**
     * A copy with another return radius.
     *
     * @param metres how close to the first waypoint counts as back at launch
     * @return new options
     */
    public ConformanceOptions withReturnRadius(double metres) {
        return new ConformanceOptions(reachedRadiusMetres, altitudeToleranceMetres, metres, maxGap, movingSpeedMs,
                movingAltitudeMetres, defaultSpeedMs, plannedReturnToStart);
    }

    /**
     * A copy with another maximum gap between readings.
     *
     * @param gap the longest gap still joined into one track
     * @return new options
     */
    public ConformanceOptions withMaxGap(Duration gap) {
        return new ConformanceOptions(reachedRadiusMetres, altitudeToleranceMetres, returnRadiusMetres, gap, movingSpeedMs,
                movingAltitudeMetres, defaultSpeedMs, plannedReturnToStart);
    }

    /**
     * A copy with other thresholds for a reading to count as moving.
     *
     * @param speedMs the ground speed at or above which a reading is moving
     * @param altitudeMetres the altitude at or above which a reading is airborne
     * @return new options
     */
    public ConformanceOptions withMovingThresholds(double speedMs, double altitudeMetres) {
        return new ConformanceOptions(reachedRadiusMetres, altitudeToleranceMetres, returnRadiusMetres, maxGap, speedMs,
                altitudeMetres, defaultSpeedMs, plannedReturnToStart);
    }

    /**
     * A copy with another assumed planned speed.
     *
     * @param speedMs the speed used when the flight gives none
     * @return new options
     */
    public ConformanceOptions withDefaultSpeed(double speedMs) {
        return new ConformanceOptions(reachedRadiusMetres, altitudeToleranceMetres, returnRadiusMetres, maxGap, movingSpeedMs,
                movingAltitudeMetres, speedMs, plannedReturnToStart);
    }

    /**
     * A copy that does or doesn't plan a leg back to the first waypoint.
     *
     * @param returnToStart true when the planned route returns to launch
     * @return new options
     */
    public ConformanceOptions withPlannedReturnToStart(boolean returnToStart) {
        return new ConformanceOptions(reachedRadiusMetres, altitudeToleranceMetres, returnRadiusMetres, maxGap, movingSpeedMs,
                movingAltitudeMetres, defaultSpeedMs, returnToStart);
    }

    private static void positive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than zero");
        }
    }

    private static void notNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(name + " must be zero or more");
        }
    }
}
