package io.github.tshepomabs16.flightgeo.conformance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.github.tshepomabs16.flightgeo.zones.GeoPoint;
import io.github.tshepomabs16.flightgeo.zones.Geodesy;
import io.github.tshepomabs16.flightgeo.zones.Zone;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConformanceCheckerTest {

    private static final Instant START = Instant.parse("2026-05-04T08:00:00Z");

    // A rectangle about 1.1 km north-south and 0.76 km east-west, planned at 100 m, with a 30 s hold at waypoint 2.
    private final List<Waypoint> plan = List.of(
            Waypoint.at(47.00, 8.00, 100), Waypoint.at(47.01, 8.00, 100).holding(30),
            Waypoint.at(47.01, 8.01, 100), Waypoint.at(47.00, 8.01, 100));

    private final ConformanceChecker checker = new ConformanceChecker();

    private static TrackFix fix(int seconds, double lat, double lon, double alt) {
        return TrackFix.of(START.plusSeconds(seconds), lat, lon, alt, 8.0);
    }

    private static TrackFix ground(int seconds, double lat, double lon) {
        return TrackFix.of(START.plusSeconds(seconds), lat, lon, 0.0, 0.0);
    }

    private List<TrackFix> flownAsPlanned(double heightAtWaypoint2) {
        return List.of(fix(0, 47.00, 8.00, 100), fix(139, 47.01, 8.00, heightAtWaypoint2),
                fix(265, 47.01, 8.01, 98), fix(404, 47.00, 8.01, 100), fix(500, 47.00, 8.00, 5));
    }

    @Test
    void aRouteFlownAsPlanned_isCompletedAsPlanned_andNotesTheReturn() {
        ConformanceReport report = checker.check(plan, flownAsPlanned(102));

        assertThat(report.verdict()).isEqualTo(Verdict.AS_PLANNED);
        assertThat(report.verdict().label()).isEqualTo("Completed as planned");
        assertThat(report.findings()).isEmpty();
        assertThat(report.notes()).extracting(Note::message).containsExactly("Returned to the launch point (waypoint 1)");
        assertThat(report.waypointsReached()).isEqualTo(4);
    }

    @Test
    void cuttingBackBeforeTheLastWaypoint_isPartlyCompleted_andSaysWhere() {
        List<TrackFix> track = List.of(fix(0, 47.00, 8.00, 100), fix(139, 47.01, 8.00, 100),
                fix(265, 47.01, 8.01, 100), fix(420, 47.00, 8.00, 5));

        ConformanceReport report = checker.check(plan, track);

        assertThat(report.verdict()).isEqualTo(Verdict.PARTLY_COMPLETED);
        assertThat(report.reasons()).contains("Waypoint 4 not reached (3 of 4)");
        assertThat(report.findings().get(0).waypoints()).containsExactly(4);
        assertThat(report.notes()).extracting(Note::message).containsExactly("Returned to the launch point after waypoint 3");
    }

    @Test
    void flyingTwentyMetresAboveThePlannedHeight_isADeviation_andTheDifferenceIsShown() {
        ConformanceReport report = checker.check(plan, flownAsPlanned(120));

        assertThat(report.verdict()).isEqualTo(Verdict.WITH_DEVIATIONS);
        assertThat(report.reasons()).containsExactly("Flown more than 10 m off the planned height at waypoint 2");
        assertThat(report.waypoints().get(1).altitudeDifference()).isEqualTo(20.0);
    }

    @Test
    void withinTheHeightTolerance_isNotADeviation() {
        assertThat(checker.check(plan, flownAsPlanned(108)).verdict()).isEqualTo(Verdict.AS_PLANNED);
        assertThat(new ConformanceChecker(ConformanceOptions.defaults().withAltitudeTolerance(5))
                .check(plan, flownAsPlanned(108)).verdict()).isEqualTo(Verdict.WITH_DEVIATIONS);
    }

    @Test
    void waypointsReachedOutOfOrder_isADeviation() {
        List<TrackFix> track = List.of(fix(0, 47.00, 8.00, 100), fix(200, 47.01, 8.01, 100),
                fix(300, 47.01, 8.00, 100), fix(500, 47.00, 8.01, 100), fix(700, 47.00, 8.00, 5));

        ConformanceReport report = checker.check(plan, track);

        assertThat(report.verdict()).isEqualTo(Verdict.WITH_DEVIATIONS);
        assertThat(report.reasons()).containsExactly("Waypoints were reached out of the planned order");
    }

    @Test
    void anAbortedFlightIsNotCompleted_andAnUntrackedOneIsNotVerified() {
        assertThat(checker.check(plan, flownAsPlanned(100), FlightContext.abortedFlight()).verdict())
                .isEqualTo(Verdict.NOT_COMPLETED);

        ConformanceReport untracked = checker.check(plan, List.of());
        assertThat(untracked.verdict()).isEqualTo(Verdict.NOT_VERIFIED);
        assertThat(untracked.tracked()).isFalse();
        assertThat(untracked.waypoints()).allSatisfy(w -> assertThat(w.closestMetres()).isNull());
    }

    @Test
    void breachesTurnACompleteRouteIntoOneWithDeviations() {
        ConformanceReport reported = checker.check(plan, flownAsPlanned(100),
                FlightContext.completedFlight().withReportedZoneBreaches(1));
        assertThat(reported.verdict()).isEqualTo(Verdict.WITH_DEVIATIONS);
        assertThat(reported.reasons()).containsExactly("1 zone breach");
    }

    @Test
    void enteringARestrictedZoneAlongTheTrackCountsAsABreach() {
        List<TrackFix> track = List.of(fix(0, 47.00, 8.00, 100), fix(70, 47.005, 8.00, 100), fix(139, 47.01, 8.00, 100),
                fix(265, 47.01, 8.01, 100), fix(404, 47.00, 8.01, 100), fix(500, 47.00, 8.00, 5));
        Zone tower = Zone.circle("tower", GeoPoint.of(47.005, 8.00), 50);

        ConformanceReport report = checker.check(plan, track, FlightContext.completedFlight().withRestrictedZones(List.of(tower)));

        assertThat(report.zoneBreaches()).isEqualTo(1);
        assertThat(report.verdict()).isEqualTo(Verdict.WITH_DEVIATIONS);
    }

    @Test
    void goingAboveTheAltitudeLimitIsReportedPerStretch() {
        List<TrackFix> track = List.of(fix(0, 47.00, 8.00, 100), fix(139, 47.01, 8.00, 100), fix(200, 47.01, 8.005, 130),
                fix(265, 47.01, 8.01, 100), fix(404, 47.00, 8.01, 100), fix(500, 47.00, 8.00, 5));

        ConformanceReport report = checker.check(plan, track, FlightContext.completedFlight().withAltitudeLimit(120));

        assertThat(report.altitudeExceedances()).singleElement().satisfies(e -> assertThat(e.highestMetres()).isEqualTo(130));
        assertThat(report.reasons()).containsExactly("Above the 120 m altitude limit 1 time");
    }

    @Test
    void plannedArrivalsCountTheLegsAtSpeed_andTheHoldsAtEarlierWaypoints() {
        ConformanceReport report = checker.check(plan, flownAsPlanned(100), FlightContext.completedFlight().withPlannedSpeed(10));
        double leg12 = Geodesy.distanceMetres(47.00, 8.00, 47.01, 8.00);
        double leg23 = Geodesy.distanceMetres(47.01, 8.00, 47.01, 8.01);

        assertThat(report.waypoints()).extracting(WaypointResult::plannedArrivalSeconds)
                .containsExactly(0, (int) Math.round(leg12 / 10), (int) Math.round(leg12 / 10 + 30 + leg23 / 10),
                        (int) Math.round(leg12 / 10 + 30 + leg23 / 10 + leg12 / 10));
        assertThat(report.plannedSpeedAssumed()).isFalse();
    }

    @Test
    void actualArrivalIsTimedFromTakeOff_notFromTheFirstReading() {
        List<TrackFix> track = List.of(ground(0, 47.00, 8.00), fix(20, 47.00, 8.00, 100), fix(159, 47.01, 8.00, 100),
                fix(285, 47.01, 8.01, 100), fix(424, 47.00, 8.01, 100), fix(520, 47.00, 8.00, 5));

        ConformanceReport report = checker.check(plan, track);

        assertThat(report.waypoints().get(1).actualArrivalSeconds()).isEqualTo(139);
        assertThat(report.plannedSpeedAssumed()).isTrue();
    }

    @Test
    void aWaypointClimbedThroughIsFlownAtTheHeightItLeftAt() {
        List<TrackFix> track = List.of(ground(0, 47.00, 8.00), fix(10, 47.00, 8.00, 20), fix(20, 47.00, 8.00, 60),
                fix(30, 47.00, 8.00, 100), fix(169, 47.01, 8.00, 100), fix(295, 47.01, 8.01, 100),
                fix(434, 47.00, 8.01, 100), fix(530, 47.00, 8.00, 5));

        ConformanceReport report = checker.check(plan, track);

        assertThat(report.waypoints().get(0).flownAltitude()).isEqualTo(100.0);
        assertThat(report.waypoints().get(0).altitudeDifference()).isZero();
    }

    @Test
    void aWaypointNeverReachedHasNoFlownHeightOrArrival() {
        List<TrackFix> track = List.of(fix(0, 47.00, 8.00, 100), fix(139, 47.01, 8.00, 100), fix(265, 47.01, 8.01, 100));

        WaypointResult missed = checker.check(plan, track).waypoints().get(3);

        assertThat(missed.reached()).isFalse();
        assertThat(missed.flownAltitude()).isNull();
        assertThat(missed.actualArrivalSeconds()).isNull();
        assertThat(missed.firstReachedAt()).isNull();
        assertThat(missed.closestMetres()).isGreaterThan(100);
    }

    @Test
    void aWaypointPassedBetweenTwoReadingsStillCountsAsReached() {
        List<TrackFix> track = List.of(fix(0, 47.00, 8.00, 100), fix(60, 47.009, 8.00, 100), fix(80, 47.011, 8.00, 100));
        WaypointResult second = checker.check(plan, track).waypoints().get(1);
        assertThat(second.reached()).isTrue();
        assertThat(second.closestMetres()).isCloseTo(0.0, within(0.5));
    }

    @Test
    void theTotalsCompareThePlanWithWhatWasFlown() {
        ConformanceReport report = checker.check(plan, flownAsPlanned(100), FlightContext.completedFlight().withPlannedSpeed(10));

        Comparison distance = report.comparison(Measure.DISTANCE).orElseThrow();
        assertThat(distance.planned()).isCloseTo(RoutePlanner.distanceMetres(plan, true), within(0.01));
        assertThat(distance.actual()).isCloseTo(distance.planned(), within(1.0));
        assertThat(report.comparison(Measure.FLIGHT_TIME).orElseThrow().actual()).isEqualTo(500.0);
        assertThat(report.comparison(Measure.HIGHEST_ALTITUDE).orElseThrow().planned()).isEqualTo(100.0);
        assertThat(report.comparison(Measure.HIGHEST_ALTITUDE).orElseThrow().difference()).isEqualTo(0.0);
    }
}
