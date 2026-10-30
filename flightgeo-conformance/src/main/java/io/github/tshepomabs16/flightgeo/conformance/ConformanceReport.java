package io.github.tshepomabs16.flightgeo.conformance;

import java.util.List;
import java.util.Optional;

/**
 * Everything the checker found: the verdict and its reasons, each waypoint, and the route-wide figures.
 *
 * @param verdict the one-word answer
 * @param findings the reasons behind the verdict
 * @param notes things worth knowing that don't change the verdict
 * @param waypoints how each waypoint was flown, in plan order
 * @param comparisons planned against actual for distance, flight time and highest altitude
 * @param altitudeExceedances each stretch above the altitude limit, when one was given
 * @param zoneBreaches restricted-zone entries found plus those reported by the caller
 * @param tracked whether any reading was recorded
 * @param plannedSpeedMs the speed the plan's timings were worked out at
 * @param plannedSpeedAssumed true when that speed is the default rather than one given for the flight
 */
public record ConformanceReport(Verdict verdict, List<Finding> findings, List<Note> notes, List<WaypointResult> waypoints,
                                List<Comparison> comparisons, List<AltitudeExceedance> altitudeExceedances,
                                int zoneBreaches, boolean tracked, double plannedSpeedMs, boolean plannedSpeedAssumed) {

    /** Copies every list, so the report can't change after it is made. */
    public ConformanceReport {
        findings = List.copyOf(findings);
        notes = List.copyOf(notes);
        waypoints = List.copyOf(waypoints);
        comparisons = List.copyOf(comparisons);
        altitudeExceedances = List.copyOf(altitudeExceedances);
    }

    /** {@return the findings as plain sentences, ready for a report or a log} */
    public List<String> reasons() {
        return findings.stream().map(Finding::message).toList();
    }

    /**
     * The comparison for one measure.
     *
     * @param measure the measure wanted
     * @return its comparison, which is always present for the three standard measures
     */
    public Optional<Comparison> comparison(Measure measure) {
        return comparisons.stream().filter(c -> c.measure() == measure).findFirst();
    }

    /** {@return how many waypoints were reached} */
    public long waypointsReached() {
        return waypoints.stream().filter(WaypointResult::reached).count();
    }
}
