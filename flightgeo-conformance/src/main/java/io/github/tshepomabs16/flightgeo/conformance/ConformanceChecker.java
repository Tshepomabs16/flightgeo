package io.github.tshepomabs16.flightgeo.conformance;

import io.github.tshepomabs16.flightgeo.zones.GeoPoint;
import io.github.tshepomabs16.flightgeo.zones.Geodesy;
import io.github.tshepomabs16.flightgeo.zones.ZoneEvent;
import io.github.tshepomabs16.flightgeo.zones.ZoneMonitor;
import io.github.tshepomabs16.flightgeo.zones.ZoneTransition;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Compares a planned route with the track that was flown and says whether it was flown as planned.
 * Pure and repeatable: the same inputs always give the same report. Immutable and thread-safe.
 */
public final class ConformanceChecker {

    private final ConformanceOptions options;

    public ConformanceChecker() {
        this(ConformanceOptions.defaults());
    }

    public ConformanceChecker(ConformanceOptions options) {
        this.options = Objects.requireNonNull(options, "options");
    }

    public ConformanceOptions options() {
        return options;
    }

    /** Checks a flight that ran to the end, with nothing else known about it. */
    public ConformanceReport check(List<Waypoint> plan, List<TrackFix> track) {
        return check(plan, track, FlightContext.completedFlight());
    }

    public ConformanceReport check(List<Waypoint> plan, List<TrackFix> track, FlightContext context) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(context, "context");
        List<TrackFix> fixes = new ArrayList<>(Objects.requireNonNull(track, "track"));
        fixes.sort(Comparator.comparing(TrackFix::time));
        boolean tracked = !fixes.isEmpty();

        boolean speedAssumed = context.plannedSpeedMs() == null;
        double speed = speedAssumed ? options.defaultSpeedMs() : context.plannedSpeedMs();
        List<Integer> arrivals = RoutePlanner.plannedArrivals(plan, speed);
        List<TrackFix> airborne = fixes.stream().filter(f -> TrackStats.isMoving(f, options)).toList();
        Instant takeOff = !airborne.isEmpty() ? airborne.get(0).time() : tracked ? fixes.get(0).time() : null;

        List<WaypointResult> results = new ArrayList<>();
        for (int i = 0; i < plan.size(); i++) {
            results.add(evaluate(i, plan.get(i), fixes, airborne, takeOff, arrivals.get(i)));
        }

        List<AltitudeExceedance> exceedances = context.altitudeLimitMetres() == null ? List.of()
                : TrackStats.altitudeExceedances(fixes, context.altitudeLimitMetres());
        int zoneBreaches = context.reportedZoneBreaches() + zoneEntries(fixes, context);

        List<Finding> findings = new ArrayList<>();
        List<Note> notes = new ArrayList<>();
        Verdict verdict = verdict(plan, fixes, results, context, exceedances, zoneBreaches, findings, notes);

        return new ConformanceReport(verdict, findings, notes, results,
                comparisons(plan, fixes, context, speed), exceedances, zoneBreaches, tracked, speed, speedAssumed);
    }

    private WaypointResult evaluate(int index, Waypoint waypoint, List<TrackFix> fixes, List<TrackFix> airborne,
                                    Instant takeOff, Integer plannedArrival) {
        GeoPoint p = waypoint.position();
        double closest = Double.MAX_VALUE;
        Instant closestAt = null;
        for (int i = 0; i < fixes.size(); i++) {
            TrackFix f = fixes.get(i);
            double d = Geodesy.distanceMetres(p, f.position());
            if (d < closest) {
                closest = d;
                closestAt = f.time();
            }
            // The aircraft may pass a waypoint between two readings, so the line between them counts too.
            if (i > 0 && TrackStats.gapOk(fixes.get(i - 1), f, options)) {
                double s = Geodesy.distanceToSegmentMetres(p, fixes.get(i - 1).position(), f.position());
                if (s < closest) {
                    closest = s;
                    closestAt = f.time();
                }
            }
        }
        boolean reached = !fixes.isEmpty() && closest <= options.reachedRadiusMetres();
        double radius = Math.max(options.reachedRadiusMetres(), 1);

        // The height flown is where it was when it left the first visit, not the first metre of a climb.
        TrackFix leaving = reached ? leavingFirstVisit(p, airborne, radius) : null;
        if (leaving == null && reached) {
            leaving = nearest(p, airborne);
        }
        Instant firstAt = reached ? firstWithin(p, airborne, radius) : null;
        if (firstAt == null && reached) {
            firstAt = closestAt;
        }
        Integer actualArrival = firstAt == null || takeOff == null ? null
                : (int) Math.max(0, Duration.between(takeOff, firstAt).getSeconds());
        return new WaypointResult(index + 1, waypoint, reached, fixes.isEmpty() ? null : closest, closestAt, firstAt,
                leaving == null ? null : leaving.altitude(), plannedArrival, actualArrival);
    }

    private Verdict verdict(List<Waypoint> plan, List<TrackFix> fixes, List<WaypointResult> results, FlightContext context,
                            List<AltitudeExceedance> exceedances, int zoneBreaches, List<Finding> findings, List<Note> notes) {
        List<Integer> missed = new ArrayList<>();
        int lastReached = 0;
        for (WaypointResult r : results) {
            if (r.reached()) {
                lastReached = r.sequence();
            } else {
                missed.add(r.sequence());
            }
        }
        boolean tracked = !fixes.isEmpty();
        Verdict verdict;
        if (context.aborted()) {
            verdict = Verdict.NOT_COMPLETED;
            findings.add(new Finding(Finding.Type.ABORTED, List.of(), "The flight was aborted"));
        } else if (!tracked) {
            verdict = Verdict.NOT_VERIFIED;
            findings.add(new Finding(Finding.Type.NOT_TRACKED, List.of(),
                    "No position was recorded, so the route cannot be checked"));
        } else {
            verdict = missed.isEmpty() ? Verdict.AS_PLANNED : Verdict.PARTLY_COMPLETED;
        }

        if (tracked) {
            if (!missed.isEmpty()) {
                findings.add(new Finding(Finding.Type.WAYPOINTS_MISSED, missed, (missed.size() == 1 ? "Waypoint " : "Waypoints ")
                        + join(missed) + " not reached (" + (results.size() - missed.size()) + " of " + results.size() + ")"));
            }
            if (outOfOrder(results)) {
                findings.add(new Finding(Finding.Type.OUT_OF_ORDER, List.of(), "Waypoints were reached out of the planned order"));
            }
            List<Integer> offHeight = new ArrayList<>();
            for (WaypointResult r : results) {
                Double diff = r.altitudeDifference();
                if (r.reached() && diff != null && Math.abs(diff) > options.altitudeToleranceMetres()) {
                    offHeight.add(r.sequence());
                }
            }
            if (!offHeight.isEmpty()) {
                findings.add(new Finding(Finding.Type.OFF_PLANNED_HEIGHT, offHeight, "Flown more than "
                        + metres(options.altitudeToleranceMetres()) + " m off the planned height at "
                        + (offHeight.size() == 1 ? "waypoint " : "waypoints ") + join(offHeight)));
            }
            if (zoneBreaches > 0) {
                findings.add(new Finding(Finding.Type.ZONE_BREACHES, List.of(),
                        zoneBreaches + " zone breach" + (zoneBreaches == 1 ? "" : "es")));
            }
            if (!exceedances.isEmpty()) {
                findings.add(new Finding(Finding.Type.ALTITUDE_LIMIT_EXCEEDED, List.of(), "Above the "
                        + metres(context.altitudeLimitMetres()) + " m altitude limit " + exceedances.size()
                        + (exceedances.size() == 1 ? " time" : " times")));
            }
            if (!plan.isEmpty() && returnedToLaunch(plan.get(0).position(), fixes.get(fixes.size() - 1).position())) {
                notes.add(new Note(Note.Type.RETURNED_TO_LAUNCH, missed.isEmpty() ? "Returned to the launch point (waypoint 1)"
                        : "Returned to the launch point after waypoint " + lastReached));
            }
        }
        return verdict == Verdict.AS_PLANNED && !findings.isEmpty() ? Verdict.WITH_DEVIATIONS : verdict;
    }

    private List<Comparison> comparisons(List<Waypoint> plan, List<TrackFix> fixes, FlightContext context, double speed) {
        boolean tracked = !fixes.isEmpty();
        Double plannedDistance = plan.size() < 2 ? null : RoutePlanner.distanceMetres(plan, options.plannedReturnToStart());
        Double actualDistance = tracked ? TrackStats.distanceMetres(fixes, options) : null;

        Double plannedSeconds = plan.isEmpty() ? null
                : (double) RoutePlanner.estimate(plan, speed, options.plannedReturnToStart()).flightSeconds();
        Double actualSeconds;
        if (!context.activeWindows().isEmpty()) {
            actualSeconds = (double) TrackStats.airborneSeconds(fixes, context.activeWindows(), options);
        } else {
            actualSeconds = tracked ? (double) TrackStats.airborneSeconds(fixes, options) : null;
        }

        Double plannedHighest = plan.stream().map(Waypoint::plannedAltitude).filter(Objects::nonNull)
                .max(Double::compare).orElse(null);
        var highest = TrackStats.highestAltitude(fixes);
        Double actualHighest = highest.isPresent() ? highest.getAsDouble() : null;

        return List.of(new Comparison(Measure.DISTANCE, plannedDistance, actualDistance),
                new Comparison(Measure.FLIGHT_TIME, plannedSeconds, actualSeconds),
                new Comparison(Measure.HIGHEST_ALTITUDE, plannedHighest, actualHighest));
    }

    private static int zoneEntries(List<TrackFix> fixes, FlightContext context) {
        if (context.restrictedZones().isEmpty()) {
            return 0;
        }
        ZoneMonitor<String> monitor = new ZoneMonitor<>();
        int entries = 0;
        for (TrackFix f : fixes) {
            for (ZoneEvent<String> e : monitor.update("flight", f.position(), context.restrictedZones())) {
                if (e.transition() == ZoneTransition.ENTERED || e.transition() == ZoneTransition.FIRST_SEEN_INSIDE) {
                    entries++;
                }
            }
        }
        return entries;
    }

    /** The last reading of the first unbroken run within the radius: the moment it left the waypoint. */
    private static TrackFix leavingFirstVisit(GeoPoint p, List<TrackFix> track, double radius) {
        TrackFix last = null;
        for (TrackFix f : track) {
            if (Geodesy.distanceMetres(p, f.position()) <= radius) {
                last = f;
            } else if (last != null) {
                break;
            }
        }
        return last;
    }

    private static Instant firstWithin(GeoPoint p, List<TrackFix> track, double radius) {
        for (TrackFix f : track) {
            if (Geodesy.distanceMetres(p, f.position()) <= radius) {
                return f.time();
            }
        }
        return null;
    }

    private static TrackFix nearest(GeoPoint p, List<TrackFix> track) {
        TrackFix best = null;
        double bestDistance = Double.MAX_VALUE;
        for (TrackFix f : track) {
            double d = Geodesy.distanceMetres(p, f.position());
            if (d < bestDistance) {
                bestDistance = d;
                best = f;
            }
        }
        return best;
    }

    /** First-reached times must rise with the waypoint numbers; a later waypoint reached first is out of order. */
    private static boolean outOfOrder(List<WaypointResult> results) {
        Instant previous = null;
        for (WaypointResult r : results) {
            if (!r.reached() || r.firstReachedAt() == null) {
                continue;
            }
            if (previous != null && r.firstReachedAt().isBefore(previous)) {
                return true;
            }
            previous = r.firstReachedAt();
        }
        return false;
    }

    private boolean returnedToLaunch(GeoPoint launch, GeoPoint last) {
        return Geodesy.distanceMetres(launch, last) <= Math.max(options.returnRadiusMetres(), options.reachedRadiusMetres());
    }

    private static String join(List<Integer> numbers) {
        List<String> s = numbers.stream().map(String::valueOf).toList();
        if (s.size() <= 1) {
            return String.join("", s);
        }
        return String.join(", ", s.subList(0, s.size() - 1)) + " and " + s.get(s.size() - 1);
    }

    private static String metres(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
