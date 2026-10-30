package io.github.tshepomabs16.flightgeo.conformance;

import java.util.List;
import java.util.Optional;

/** Everything the checker found: the verdict and its reasons, each waypoint, and the route-wide figures. */
public record ConformanceReport(Verdict verdict, List<Finding> findings, List<Note> notes, List<WaypointResult> waypoints,
                                List<Comparison> comparisons, List<AltitudeExceedance> altitudeExceedances,
                                int zoneBreaches, boolean tracked, double plannedSpeedMs, boolean plannedSpeedAssumed) {

    public ConformanceReport {
        findings = List.copyOf(findings);
        notes = List.copyOf(notes);
        waypoints = List.copyOf(waypoints);
        comparisons = List.copyOf(comparisons);
        altitudeExceedances = List.copyOf(altitudeExceedances);
    }

    /** The findings as plain sentences, ready for a report or a log. */
    public List<String> reasons() {
        return findings.stream().map(Finding::message).toList();
    }

    public Optional<Comparison> comparison(Measure measure) {
        return comparisons.stream().filter(c -> c.measure() == measure).findFirst();
    }

    public long waypointsReached() {
        return waypoints.stream().filter(WaypointResult::reached).count();
    }
}
