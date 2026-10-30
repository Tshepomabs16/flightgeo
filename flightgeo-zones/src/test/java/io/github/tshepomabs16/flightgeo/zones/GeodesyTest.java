package io.github.tshepomabs16.flightgeo.zones;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class GeodesyTest {

    @Test
    void oneDegreeOfLatitudeIsAbout111Kilometres() {
        assertThat(Geodesy.distanceMetres(47.0, 8.0, 48.0, 8.0)).isCloseTo(111_195, within(5.0));
    }

    @Test
    void distanceIgnoresAltitudeAndIsSymmetric() {
        GeoPoint a = GeoPoint.of(47.0, 8.0, 100);
        GeoPoint b = GeoPoint.of(47.001, 8.001, 400);
        assertThat(a.distanceTo(b)).isCloseTo(b.distanceTo(a), within(1e-9)).isCloseTo(134.6, within(0.5));
    }

    @Test
    void distanceToASegmentUsesTheNearestPointOnIt() {
        GeoPoint a = GeoPoint.of(47.0, 8.0);
        GeoPoint b = GeoPoint.of(47.0, 8.01);
        GeoPoint above = GeoPoint.of(47.001, 8.005);
        assertThat(Geodesy.distanceToSegmentMetres(above, a, b)).isCloseTo(111.2, within(0.5));
        assertThat(Geodesy.distanceToSegmentMetres(a, a, b)).isZero();
    }

    @Test
    void theProjectionWrapsAcrossTheDateLine() {
        double[] east = Geodesy.project(0.0, -179.99, 0.0, 179.99);
        assertThat(east[0]).isCloseTo(2_224, within(2.0));
    }
}
