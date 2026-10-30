package io.github.tshepomabs16.flightgeo.conformance;

import java.time.Duration;
import java.time.Instant;

/** One unbroken stretch above an altitude limit: when it started and ended, and the highest reading in it. */
public record AltitudeExceedance(Instant start, Instant end, double highestMetres) {

    public Duration duration() {
        return Duration.between(start, end);
    }
}
