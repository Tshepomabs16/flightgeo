package io.github.tshepomabs16.flightgeo.conformance;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** A span of time, for example the stretches a flight was active rather than paused. */
public record TimeWindow(Instant start, Instant end) {

    public TimeWindow {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("A window cannot end before it starts");
        }
    }

    public long seconds() {
        return Duration.between(start, end).getSeconds();
    }

    public boolean contains(Instant moment) {
        return !moment.isBefore(start) && !moment.isAfter(end);
    }
}
