package io.github.tshepomabs16.flightgeo.zones;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class ZoneValidatorTest {

    private static PolygonZone polygon(double... latLon) {
        List<GeoPoint> corners = new java.util.ArrayList<>();
        for (int i = 0; i < latLon.length; i += 2) {
            corners.add(GeoPoint.of(latLon[i], latLon[i + 1]));
        }
        return Zone.polygon("p", corners);
    }

    @Test
    void aSimplePolygonIsValid() {
        assertThat(ZoneValidator.problems(polygon(47.0, 8.0, 47.0, 8.02, 47.02, 8.02, 47.02, 8.0))).isEmpty();
    }

    @Test
    void aBowTieIsRefused() {
        assertThat(ZoneValidator.problems(polygon(47.0, 8.0, 47.02, 8.02, 47.0, 8.02, 47.02, 8.0)))
                .containsExactly("the polygon's edges cross each other");
    }

    @Test
    void tooFewCornersOrCornersInALineAreRefused() {
        assertThat(ZoneValidator.problems(polygon(47.0, 8.0, 47.01, 8.01))).containsExactly("a polygon needs at least three corners");
        assertThat(ZoneValidator.problems(polygon(47.0, 8.0, 47.01, 8.01, 47.02, 8.02)))
                .containsExactly("the polygon has no area: its corners lie on one line");
    }

    @Test
    void aCornerOrCentreAtZeroZeroIsFlagged() {
        assertThat(ZoneValidator.problems(polygon(0, 0, 0.01, 0.0, 0.01, 0.01))).anyMatch(p -> p.contains("0, 0"));
        assertThat(ZoneValidator.problems(Zone.circle("c", GeoPoint.of(0, 0), 10))).anyMatch(p -> p.contains("0, 0"));
    }

    @Test
    void anUpsideDownBandIsRefused() {
        Zone z = Zone.circle("c", GeoPoint.of(47, 8), 100, AltitudeBand.between(120, 50));
        assertThat(ZoneValidator.problems(z)).containsExactly("the floor must be below the ceiling");
    }

    @Test
    void aFloorAtOrAboveTheOperatingCeilingIsReportedAsUnreachable() {
        Zone z = Zone.circle("c", GeoPoint.of(47, 8), 100, AltitudeBand.from(150));
        assertThat(ZoneValidator.problems(z)).isEmpty();
        assertThat(ZoneValidator.problems(z, 120.0)).singleElement().asString().contains("could never apply");
    }

    @Test
    void aCircleNeedsARadius_andRequireValidThrowsWithTheReasons() {
        assertThatThrownBy(() -> Zone.circle("c", GeoPoint.of(47, 8), 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ZoneValidator.requireValid(polygon(47.0, 8.0, 47.01, 8.01)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("three corners");
    }
}
