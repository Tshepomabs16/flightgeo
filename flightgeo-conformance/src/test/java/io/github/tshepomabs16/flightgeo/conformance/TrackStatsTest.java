package io.github.tshepomabs16.flightgeo.conformance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.github.tshepomabs16.flightgeo.zones.Geodesy;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackStatsTest {

    private static final Instant T0 = Instant.parse("2026-05-04T08:00:00Z");
    private final ConformanceOptions options = ConformanceOptions.defaults();

    private static TrackFix at(int seconds, double lat, Double alt, Double speed) {
        return TrackFix.of(T0.plusSeconds(seconds), lat, 8.0, alt, speed);
    }

    @Test
    void distanceNeverBridgesAGapInTheTrack() {
        List<TrackFix> track = List.of(at(0, 47.00, 50.0, 8.0), at(60, 47.001, 50.0, 8.0), at(1000, 47.010, 50.0, 8.0));
        assertThat(TrackStats.distanceMetres(track, options))
                .isCloseTo(Geodesy.distanceMetres(47.00, 8.0, 47.001, 8.0), within(0.01));
    }

    @Test
    void onlyMovingTimeCounts_andAMidFlightLandingIsLeftOut() {
        List<TrackFix> track = List.of(at(0, 47.0, 0.0, 0.0), at(10, 47.0, 30.0, 6.0), at(70, 47.001, 30.0, 6.0),
                at(100, 47.001, 0.0, 0.0), at(800, 47.001, 0.0, 0.0), at(810, 47.001, 30.0, 6.0), at(870, 47.002, 30.0, 6.0));
        assertThat(TrackStats.airborneSeconds(track, options)).isEqualTo(60 + 30 + 60);
    }

    @Test
    void walkingSpeedWithoutAltitudeCountsAsMoving_andAStillTrackIsNotAirborne() {
        assertThat(TrackStats.isMoving(at(0, 47.0, null, 1.5), options)).isTrue();
        assertThat(TrackStats.airborneSeconds(List.of(at(0, 47.0, 0.0, 0.0), at(60, 47.0, 0.0, 0.0)), options)).isZero();
    }

    @Test
    void activeWindowsLeaveOutPausedTime() {
        List<TrackFix> track = List.of(at(0, 47.0, 30.0, 6.0), at(60, 47.001, 30.0, 6.0), at(120, 47.002, 30.0, 6.0));
        List<TimeWindow> active = List.of(new TimeWindow(T0, T0.plusSeconds(60)));
        assertThat(TrackStats.airborneSeconds(track, active, options)).isEqualTo(60);
    }

    @Test
    void eachStretchAboveTheLimitIsOneExceedance() {
        List<TrackFix> track = List.of(at(0, 47.0, 100.0, 6.0), at(10, 47.0, 125.0, 6.0), at(20, 47.0, 131.0, 6.0),
                at(30, 47.0, 110.0, 6.0), at(40, 47.0, 122.0, 6.0));
        List<AltitudeExceedance> found = TrackStats.altitudeExceedances(track, 120);
        assertThat(found).hasSize(2);
        assertThat(found.get(0).highestMetres()).isEqualTo(131);
        assertThat(found.get(0).duration().getSeconds()).isEqualTo(10);
        assertThat(TrackStats.highestAltitude(track)).hasValue(131);
    }
}
