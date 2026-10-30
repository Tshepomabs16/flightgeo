package io.github.tshepomabs16.flightgeo.conformance;

import java.time.Duration;
import java.time.Instant;

/**
 * One unbroken stretch above an altitude limit.
 *
 * @param start the time of the first reading above the limit
 * @param end the time of the last reading above the limit
 * @param highestMetres the highest altitude in the stretch
 */
public record AltitudeExceedance(Instant start, Instant end, double highestMetres) {

    /** {@return how long the stretch lasted} */
    public Duration duration() {
        return Duration.between(start, end);
    }
}
