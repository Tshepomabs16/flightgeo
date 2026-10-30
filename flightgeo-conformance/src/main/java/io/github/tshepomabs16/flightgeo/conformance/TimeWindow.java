package io.github.tshepomabs16.flightgeo.conformance;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A span of time, for example the stretches a flight was active rather than paused.
 *
 * @param start when it began
 * @param end when it ended, not before the start
 */
public record TimeWindow(Instant start, Instant end) {

    /** Refuses a missing end or one before the start. */
    public TimeWindow {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("A window cannot end before it starts");
        }
    }

    /** {@return the window's length in whole seconds} */
    public long seconds() {
        return Duration.between(start, end).getSeconds();
    }

    /**
     * Whether a moment falls inside the window, both ends included.
     *
     * @param moment the moment
     * @return true when it is inside
     */
    public boolean contains(Instant moment) {
        return !moment.isBefore(start) && !moment.isAfter(end);
    }
}
