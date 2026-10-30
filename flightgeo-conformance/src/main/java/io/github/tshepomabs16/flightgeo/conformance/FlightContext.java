package io.github.tshepomabs16.flightgeo.conformance;

import io.github.tshepomabs16.flightgeo.zones.Zone;
import java.util.List;

/**
 * What the track alone can't tell the checker: whether the flight was aborted, its planned speed, an
 * altitude limit, zones it had to avoid, breaches your own system counted, and the windows it was active.
 */
public record FlightContext(boolean aborted, Double plannedSpeedMs, Double altitudeLimitMetres, List<Zone> restrictedZones,
                            int reportedZoneBreaches, List<TimeWindow> activeWindows) {

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

    /** A flight that ran to the end, with nothing else known about it. */
    public static FlightContext completedFlight() {
        return new FlightContext(false, null, null, List.of(), 0, List.of());
    }

    /** A flight that was stopped early. */
    public static FlightContext abortedFlight() {
        return new FlightContext(true, null, null, List.of(), 0, List.of());
    }

    public FlightContext withPlannedSpeed(double speedMs) {
        return new FlightContext(aborted, speedMs, altitudeLimitMetres, restrictedZones, reportedZoneBreaches, activeWindows);
    }

    public FlightContext withAltitudeLimit(double metres) {
        return new FlightContext(aborted, plannedSpeedMs, metres, restrictedZones, reportedZoneBreaches, activeWindows);
    }

    /** Zones the flight had to stay out of; each entry along the track counts as a breach. */
    public FlightContext withRestrictedZones(List<? extends Zone> zones) {
        return new FlightContext(aborted, plannedSpeedMs, altitudeLimitMetres, List.copyOf(zones), reportedZoneBreaches, activeWindows);
    }

    /** Breaches your own system already counted, added to any found from restricted zones. */
    public FlightContext withReportedZoneBreaches(int count) {
        return new FlightContext(aborted, plannedSpeedMs, altitudeLimitMetres, restrictedZones, count, activeWindows);
    }

    /** The stretches the flight was active (not paused); flight time is counted only inside them. */
    public FlightContext withActiveWindows(List<TimeWindow> windows) {
        return new FlightContext(aborted, plannedSpeedMs, altitudeLimitMetres, restrictedZones, reportedZoneBreaches, windows);
    }
}
