package io.github.tshepomabs16.flightgeo.conformance;

import io.github.tshepomabs16.flightgeo.zones.Zone;
import java.util.List;

/**
 * What the track alone can't tell the checker. Start from {@link #completedFlight()} or {@link #abortedFlight()}.
 *
 * @param aborted whether the flight was stopped early
 * @param plannedSpeedMs the speed it was planned at, or null to use the options' default
 * @param altitudeLimitMetres a limit to report stretches above, or null for none
 * @param restrictedZones zones it had to stay out of
 * @param reportedZoneBreaches breaches your own system already counted
 * @param activeWindows the stretches it was active; empty means the whole track
 */
public record FlightContext(boolean aborted, Double plannedSpeedMs, Double altitudeLimitMetres, List<Zone> restrictedZones,
                            int reportedZoneBreaches, List<TimeWindow> activeWindows) {

    /** Copies the lists and refuses a speed or breach count that makes no sense. */
    public FlightContext {
        restrictedZones = List.copyOf(restrictedZones);
        activeWindows = List.copyOf(activeWindows);
        if (plannedSpeedMs != null && (!Double.isFinite(plannedSpeedMs) || plannedSpeedMs <= 0)) {
            throw new IllegalArgumentException("Planned speed must be greater than zero");
        }
        if (reportedZoneBreaches < 0) {
            throw new IllegalArgumentException("Reported zone breaches cannot be negative");
        }
    }

    /** {@return a flight that ran to the end, with nothing else known about it} */
    public static FlightContext completedFlight() {
        return new FlightContext(false, null, null, List.of(), 0, List.of());
    }

    /** {@return a flight that was stopped early} */
    public static FlightContext abortedFlight() {
        return new FlightContext(true, null, null, List.of(), 0, List.of());
    }

    /**
     * A copy with the speed the flight was planned at.
     *
     * @param speedMs the planned speed in metres per second
     * @return a new context
     */
    public FlightContext withPlannedSpeed(double speedMs) {
        return new FlightContext(aborted, speedMs, altitudeLimitMetres, restrictedZones, reportedZoneBreaches, activeWindows);
    }

    /**
     * A copy with an altitude limit; each stretch above it is reported.
     *
     * @param metres the limit in metres
     * @return a new context
     */
    public FlightContext withAltitudeLimit(double metres) {
        return new FlightContext(aborted, plannedSpeedMs, metres, restrictedZones, reportedZoneBreaches, activeWindows);
    }

    /**
     * A copy with zones the flight had to stay out of; each entry along the track counts as a breach.
     *
     * @param zones the restricted zones
     * @return a new context
     */
    public FlightContext withRestrictedZones(List<? extends Zone> zones) {
        return new FlightContext(aborted, plannedSpeedMs, altitudeLimitMetres, List.copyOf(zones), reportedZoneBreaches, activeWindows);
    }

    /**
     * A copy with breaches your own system already counted, added to any found from restricted zones.
     *
     * @param count the number of breaches
     * @return a new context
     */
    public FlightContext withReportedZoneBreaches(int count) {
        return new FlightContext(aborted, plannedSpeedMs, altitudeLimitMetres, restrictedZones, count, activeWindows);
    }

    /**
     * A copy with the stretches the flight was active (not paused); flight time is counted only inside them.
     *
     * @param windows the active stretches
     * @return a new context
     */
    public FlightContext withActiveWindows(List<TimeWindow> windows) {
        return new FlightContext(aborted, plannedSpeedMs, altitudeLimitMetres, restrictedZones, reportedZoneBreaches, windows);
    }
}
