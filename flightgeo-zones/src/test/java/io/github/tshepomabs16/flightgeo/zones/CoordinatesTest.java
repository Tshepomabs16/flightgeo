package io.github.tshepomabs16.flightgeo.zones;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CoordinatesTest {

    @Test
    void anOrdinaryPositionIsUsable() {
        assertThat(Coordinates.problem(47.37, 8.54)).isEmpty();
        assertThat(Coordinates.isUsable(-33.9, 151.2)).isTrue();
    }

    @Test
    void outOfRangeAndNonNumbersAreNamed() {
        assertThat(Coordinates.problem(91, 0.1)).hasValueSatisfying(p -> assertThat(p).contains("latitude of 91.0"));
        assertThat(Coordinates.problem(10, -181)).hasValueSatisfying(p -> assertThat(p).contains("longitude of -181.0"));
        assertThat(Coordinates.problem(Double.NaN, 10)).hasValue("is not a number");
    }

    @Test
    void zeroZeroCountsAsAMissingFixUnlessAllowed() {
        assertThat(Coordinates.problem(0, 0)).isPresent();
        assertThat(Coordinates.problem(0, 0, true)).isEmpty();
    }

    @Test
    void aGeoPointRefusesAnImpossiblePositionButAcceptsZeroZero() {
        assertThatThrownBy(() -> GeoPoint.of(95, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeoPoint(10, 10, Double.POSITIVE_INFINITY)).isInstanceOf(IllegalArgumentException.class);
        assertThat(GeoPoint.of(0, 0).hasAltitude()).isFalse();
    }
}
