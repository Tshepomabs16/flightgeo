package io.github.tshepomabs16.flightgeo.conformance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import io.github.tshepomabs16.flightgeo.zones.Geodesy;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoutePlannerTest {

    private final List<Waypoint> route = List.of(Waypoint.at(47.00, 8.00), Waypoint.at(47.01, 8.00).holding(30),
            Waypoint.at(47.01, 8.01));

    @Test
    void theRouteCanIncludeTheReturnToTheFirstWaypoint() {
        double open = RoutePlanner.distanceMetres(route, false);
        double closed = RoutePlanner.distanceMetres(route, true);
        assertThat(closed - open).isCloseTo(Geodesy.distanceMetres(47.01, 8.01, 47.00, 8.00), within(0.01));
        assertThat(RoutePlanner.distanceMetres(List.of(Waypoint.at(47, 8)), true)).isZero();
    }

    @Test
    void theEstimateAddsEveryHoldToTheTimeAtSpeed() {
        RoutePlanner.RouteEstimate estimate = RoutePlanner.estimate(route, 10, false);
        assertThat(estimate.flightSeconds()).isEqualTo((int) Math.round(estimate.distanceMetres() / 10) + 30);
    }

    @Test
    void batteriesUseOnlyTheRangeLeftAfterTheReserve() {
        assertThat(RoutePlanner.batteriesNeeded(7_000, 8_000, 0)).isEqualTo(1);
        assertThat(RoutePlanner.batteriesNeeded(7_000, 8_000, 20)).isEqualTo(2);
        assertThat(RoutePlanner.batteriesNeeded(0, 8_000, 20)).isZero();
        assertThatThrownBy(() -> RoutePlanner.batteriesNeeded(100, 8_000, 100)).isInstanceOf(IllegalArgumentException.class);
    }
}
