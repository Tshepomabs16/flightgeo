package io.github.tshepomabs16.flightgeo.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackCleanerTest {

    private static final Instant T0 = Instant.parse("2026-05-04T08:00:00Z");

    private static TrackFix at(int seconds, double lat, double lon) {
        return TrackFix.of(T0.plusSeconds(seconds), lat, lon, 50.0, 8.0);
    }

    @Test
    void readingsComeBackInTimeOrder_withoutMissingFixesOrImpossibleJumps() {
        List<TrackFix> raw = List.of(at(20, 47.0002, 8.0), at(0, 47.0, 8.0), at(10, 0, 0), at(15, 47.05, 8.0), at(30, 47.0003, 8.0));

        TrackCleaner.Result result = TrackCleaner.clean(raw);

        assertThat(result.fixes()).extracting(f -> f.time().getEpochSecond() - T0.getEpochSecond()).containsExactly(0L, 20L, 30L);
        assertThat(result.droppedMissing()).isEqualTo(1);
        assertThat(result.droppedJumps()).isEqualTo(1);
    }

    @Test
    void theSpeedLimitCanBeRaisedForFasterAircraft() {
        List<TrackFix> raw = List.of(at(0, 47.0, 8.0), at(10, 47.005, 8.0));
        assertThat(TrackCleaner.clean(raw).fixes()).hasSize(1);
        assertThat(TrackCleaner.clean(raw, 100).fixes()).hasSize(2);
    }
}
