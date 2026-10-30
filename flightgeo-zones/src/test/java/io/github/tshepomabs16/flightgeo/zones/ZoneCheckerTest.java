package io.github.tshepomabs16.flightgeo.zones;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ZoneCheckerTest {

    private final ZoneChecker checker = ZoneChecker.defaults();

    // About 1.5 km east-west and 2.2 km north-south.
    private final PolygonZone square = Zone.polygon("square", List.of(
            GeoPoint.of(47.00, 8.00), GeoPoint.of(47.00, 8.02), GeoPoint.of(47.02, 8.02), GeoPoint.of(47.02, 8.00)));

    private final CircleZone circle = Zone.circle("circle", GeoPoint.of(47.0, 8.0), 500);

    @Test
    void aPointAtTheCentreIsInsideACircle_andOneBeyondTheRadiusIsNot() {
        assertThat(checker.contains(circle, GeoPoint.of(47.0, 8.0))).isTrue();
        assertThat(checker.contains(circle, GeoPoint.of(47.0, 8.0068))).isFalse();
    }

    @Test
    void aPolygonContainsWhatIsInsideIt_andNotWhatIsOutside() {
        assertThat(checker.contains(square, GeoPoint.of(47.01, 8.01))).isTrue();
        assertThat(checker.contains(square, GeoPoint.of(47.03, 8.01))).isFalse();
        assertThat(checker.contains(square, GeoPoint.of(47.01, 7.99))).isFalse();
    }

    @Test
    void aPointOnTheEdgeCountsAsInside() {
        assertThat(checker.contains(square, GeoPoint.of(47.00, 8.01))).isTrue();
        assertThat(checker.withEdgeTolerance(0).contains(square, GeoPoint.of(47.000003, 8.01))).isTrue();
    }

    @Test
    void theCeilingAndTheFloorAreEnforcedAsWellAsTheShape() {
        Zone banded = Zone.circle("banded", GeoPoint.of(47.0, 8.0), 500, AltitudeBand.between(30, 120));
        assertThat(checker.contains(banded, GeoPoint.of(47.0, 8.0, 60))).isTrue();
        assertThat(checker.contains(banded, GeoPoint.of(47.0, 8.0, 150))).isFalse();
        assertThat(checker.contains(banded, GeoPoint.of(47.0, 8.0, 10))).isFalse();
        assertThat(checker.contains(banded, GeoPoint.of(47.0, 8.0, 120))).isTrue();
    }

    @Test
    void anUnknownAltitudeFollowsThePolicy() {
        Zone banded = Zone.circle("banded", GeoPoint.of(47.0, 8.0), 500, AltitudeBand.from(50));
        GeoPoint noHeight = GeoPoint.of(47.0, 8.0);
        assertThat(checker.contains(banded, noHeight)).isTrue();
        assertThat(checker.withUnknownAltitude(UnknownAltitude.OUTSIDE_BAND).contains(banded, noHeight)).isFalse();
        assertThat(checker.withUnknownAltitude(UnknownAltitude.OUTSIDE_BAND).contains(circle, noHeight)).isTrue();
    }

    @Test
    void aDegeneratePolygonContainsNothing_ratherThanThrowing() {
        Zone line = Zone.polygon("line", List.of(GeoPoint.of(47.0, 8.0), GeoPoint.of(47.01, 8.01)));
        assertThat(checker.contains(line, GeoPoint.of(47.005, 8.005))).isFalse();
        assertThat(checker.legTouches(line, GeoPoint.of(47.0, 8.0), GeoPoint.of(47.01, 8.01))).isFalse();
    }

    @Test
    void aRepeatedClosingCornerIsDropped() {
        PolygonZone closed = Zone.polygon("closed", List.of(GeoPoint.of(47.0, 8.0), GeoPoint.of(47.0, 8.02),
                GeoPoint.of(47.02, 8.02), GeoPoint.of(47.0, 8.0)));
        assertThat(closed.vertices()).hasSize(3);
    }

    @Test
    void aLegCrossingAPolygonTouchesIt_thoughNeitherEndIsInside() {
        GeoPoint west = GeoPoint.of(47.01, 7.99);
        GeoPoint east = GeoPoint.of(47.01, 8.03);
        assertThat(checker.legTouches(square, west, east)).isTrue();
        assertThat(checker.legTouches(square, GeoPoint.of(47.03, 7.99), GeoPoint.of(47.03, 8.03))).isFalse();
    }

    @Test
    void aLegPassingNearACircleTouchesItOnlyWithinTheRadius() {
        assertThat(checker.legTouches(circle, GeoPoint.of(47.003, 7.99), GeoPoint.of(47.003, 8.01))).isTrue();
        assertThat(checker.legTouches(circle, GeoPoint.of(47.006, 7.99), GeoPoint.of(47.006, 8.01))).isFalse();
    }

    @Test
    void flyingAboveAZonesCeilingDoesNotTouchIt_butClimbingThroughDoes() {
        Zone low = Zone.polygon("low", square.vertices(), AltitudeBand.upTo(60));
        assertThat(checker.legTouches(low, GeoPoint.of(47.01, 7.99, 100), GeoPoint.of(47.01, 8.03, 110))).isFalse();
        assertThat(checker.legTouches(low, GeoPoint.of(47.01, 7.99, 40), GeoPoint.of(47.01, 8.03, 110))).isTrue();
    }

    @Test
    void routeConflictsGiveTheFirstPlaceEachZoneIsMet() {
        List<GeoPoint> route = List.of(GeoPoint.of(47.03, 7.99), GeoPoint.of(47.01, 7.99),
                GeoPoint.of(47.01, 8.03), GeoPoint.of(47.03, 8.03));
        Zone far = Zone.circle("far", GeoPoint.of(48.0, 9.0), 100);

        List<RouteConflict> conflicts = checker.routeConflicts(List.of(square, far), route);

        assertThat(conflicts).containsExactly(new RouteConflict(square, RouteConflict.Kind.LEG_CROSSES, 1));
        assertThat(checker.routeTouches(far, route)).isFalse();
    }

    @Test
    void aWaypointInsideAZoneIsReportedAsSuch() {
        List<GeoPoint> route = List.of(GeoPoint.of(47.01, 8.01), GeoPoint.of(47.03, 8.01));
        assertThat(checker.routeConflicts(List.of(square), route))
                .containsExactly(new RouteConflict(square, RouteConflict.Kind.WAYPOINT_INSIDE, 0));
    }

    @Test
    void zonesContainingListsOnlyTheZonesAroundThePoint() {
        assertThat(checker.zonesContaining(List.of(square, circle), GeoPoint.of(47.01, 8.01))).containsExactly(square);
    }

    @Test
    void aPolygonAcrossTheDateLineContainsPointsOnBothSides() {
        Zone pacific = Zone.polygon("pacific", List.of(GeoPoint.of(-1, 179.5), GeoPoint.of(-1, -179.5),
                GeoPoint.of(1, -179.5), GeoPoint.of(1, 179.5)));
        assertThat(checker.contains(pacific, GeoPoint.of(0, 179.9))).isTrue();
        assertThat(checker.contains(pacific, GeoPoint.of(0, -179.9))).isTrue();
        assertThat(checker.contains(pacific, GeoPoint.of(0, 178.0))).isFalse();
    }
}
