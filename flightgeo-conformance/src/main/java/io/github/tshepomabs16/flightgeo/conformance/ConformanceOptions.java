package io.github.tshepomabs16.flightgeo.conformance;

import java.time.Duration;
import java.util.Objects;

/**
 * The thresholds the checker works to, all in metres, seconds and m/s. {@link #defaults()} suits small
 * aircraft on routes of a few kilometres; the README explains each one.
 */
public record ConformanceOptions(double reachedRadiusMetres, double altitudeToleranceMetres, double returnRadiusMetres,
                                 Duration maxGap, double movingSpeedMs, double movingAltitudeMetres,
                                 double defaultSpeedMs, boolean plannedReturnToStart) {

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

    public static ConformanceOptions defaults() {
        return new ConformanceOptions(10, 10, 25, Duration.ofMinutes(5), 1.0, 2.0, 10.0, true);
    }

    public ConformanceOptions withReachedRadius(double metres) {
        return new ConformanceOptions(metres, altitudeToleranceMetres, returnRadiusMetres, maxGap, movingSpeedMs,
                movingAltitudeMetres, defaultSpeedMs, plannedReturnToStart);
    }

    public ConformanceOptions withAltitudeTolerance(double metres) {
        return new ConformanceOptions(reachedRadiusMetres, metres, returnRadiusMetres, maxGap, movingSpeedMs,
                movingAltitudeMetres, defaultSpeedMs, plannedReturnToStart);
    }

    public ConformanceOptions withReturnRadius(double metres) {
        return new ConformanceOptions(reachedRadiusMetres, altitudeToleranceMetres, metres, maxGap, movingSpeedMs,
                movingAltitudeMetres, defaultSpeedMs, plannedReturnToStart);
    }

    public ConformanceOptions withMaxGap(Duration gap) {
        return new ConformanceOptions(reachedRadiusMetres, altitudeToleranceMetres, returnRadiusMetres, gap, movingSpeedMs,
                movingAltitudeMetres, defaultSpeedMs, plannedReturnToStart);
    }

    public ConformanceOptions withMovingThresholds(double speedMs, double altitudeMetres) {
        return new ConformanceOptions(reachedRadiusMetres, altitudeToleranceMetres, returnRadiusMetres, maxGap, speedMs,
                altitudeMetres, defaultSpeedMs, plannedReturnToStart);
    }

    public ConformanceOptions withDefaultSpeed(double speedMs) {
        return new ConformanceOptions(reachedRadiusMetres, altitudeToleranceMetres, returnRadiusMetres, maxGap, movingSpeedMs,
                movingAltitudeMetres, speedMs, plannedReturnToStart);
    }

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
