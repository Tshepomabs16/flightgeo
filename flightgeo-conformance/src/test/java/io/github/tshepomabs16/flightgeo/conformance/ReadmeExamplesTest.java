package io.github.tshepomabs16.flightgeo.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.tshepomabs16.flightgeo.zones.AltitudeBand;
import io.github.tshepomabs16.flightgeo.zones.GeoPoint;
import io.github.tshepomabs16.flightgeo.zones.RouteConflict;
import io.github.tshepomabs16.flightgeo.zones.Zone;
import io.github.tshepomabs16.flightgeo.zones.ZoneChecker;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The README's examples, kept here so they can't drift from the code. */
class ReadmeExamplesTest {

    @Test
    void theZonesExampleGivesTheResultsTheReadmeShows() {
        Zone airfield = Zone.circle("airfield", GeoPoint.of(47.4647, 8.5492), 3_000, AltitudeBand.upTo(150));
        Zone reserve = Zone.polygon("reserve", List.of(
                GeoPoint.of(47.40, 8.50), GeoPoint.of(47.40, 8.56),
                GeoPoint.of(47.44, 8.56), GeoPoint.of(47.44, 8.50)));
        ZoneChecker checker = ZoneChecker.defaults();

        assertThat(checker.contains(airfield, GeoPoint.of(47.47, 8.55, 120))).isTrue();
        assertThat(checker.contains(airfield, GeoPoint.of(47.47, 8.55, 200))).isFalse();

        List<GeoPoint> route = List.of(GeoPoint.of(47.38, 8.53, 60), GeoPoint.of(47.46, 8.53, 60));
        assertThat(checker.routeConflicts(List.of(airfield, reserve), route))
                .containsExactly(new RouteConflict(airfield, RouteConflict.Kind.LEG_CROSSES, 0),
                        new RouteConflict(reserve, RouteConflict.Kind.LEG_CROSSES, 0));
    }

    @Test
    void theConformanceExampleRuns_andTheCleanerFeedsTheChecker() {
        List<Waypoint> plan = List.of(
                Waypoint.at(47.00, 8.00, 100),
                Waypoint.at(47.01, 8.00, 100).holding(30),
                Waypoint.at(47.01, 8.01, 100));
        Instant t0 = Instant.parse("2026-05-04T08:00:00Z");
        List<TrackFix> rawFixes = List.of(
                TrackFix.of(t0, 47.00, 8.00, 100.0, 9.0), TrackFix.of(t0.plusSeconds(5), 0, 0, null, null),
                TrackFix.of(t0.plusSeconds(93), 47.01, 8.00, 121.0, 9.0), TrackFix.of(t0.plusSeconds(186), 47.01, 8.01, 100.0, 9.0));

        List<TrackFix> track = TrackCleaner.clean(rawFixes).fixes();
        ConformanceReport report = new ConformanceChecker().check(plan, track,
                FlightContext.completedFlight().withPlannedSpeed(12).withAltitudeLimit(120));

        assertThat(track).hasSize(3);
        assertThat(report.verdict()).isEqualTo(Verdict.WITH_DEVIATIONS);
        assertThat(report.reasons()).contains("Flown more than 10 m off the planned height at waypoint 2");
        assertThat(report.comparison(Measure.DISTANCE)).isPresent();
        assertThat(RoutePlanner.batteriesNeeded(RoutePlanner.distanceMetres(plan, true), 8_000, 20)).isEqualTo(1);
    }
}
