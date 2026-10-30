package io.github.tshepomabs16.flightgeo.zones;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ZoneMonitorTest {

    private final Zone zone = Zone.circle("airfield", GeoPoint.of(47.0, 8.0), 500);
    private final GeoPoint inside = GeoPoint.of(47.0, 8.0);
    private final GeoPoint outside = GeoPoint.of(47.02, 8.0);
    private final ZoneMonitor<String> monitor = new ZoneMonitor<>();

    @Test
    void theFirstPositionIsReportedAsAFirstSighting() {
        assertThat(monitor.update("a", outside, List.of(zone))).extracting(ZoneEvent::transition)
                .containsExactly(ZoneTransition.FIRST_SEEN_OUTSIDE);
        assertThat(monitor.update("b", inside, List.of(zone))).extracting(ZoneEvent::transition)
                .containsExactly(ZoneTransition.FIRST_SEEN_INSIDE);
    }

    @Test
    void enteringAndLeavingAreReportedOnce_andStayingPutReportsNothing() {
        monitor.update("a", outside, List.of(zone));

        assertThat(monitor.update("a", inside, List.of(zone))).extracting(ZoneEvent::transition)
                .containsExactly(ZoneTransition.ENTERED);
        assertThat(monitor.update("a", inside, List.of(zone))).isEmpty();
        assertThat(monitor.update("a", outside, List.of(zone))).extracting(ZoneEvent::transition)
                .containsExactly(ZoneTransition.EXITED);
    }

    @Test
    void eachSubjectIsTrackedSeparately_andForgettingStartsAfresh() {
        monitor.update("a", inside, List.of(zone));
        monitor.update("b", outside, List.of(zone));
        assertThat(monitor.isInside("a", "airfield")).hasValue(true);
        assertThat(monitor.isInside("b", "airfield")).hasValue(false);

        monitor.forget("a");
        assertThat(monitor.isInside("a", "airfield")).isEmpty();
        assertThat(monitor.update("a", inside, List.of(zone))).extracting(ZoneEvent::transition)
                .containsExactly(ZoneTransition.FIRST_SEEN_INSIDE);
    }

    @Test
    void forgettingAZoneClearsItForEverySubject() {
        monitor.update("a", inside, List.of(zone));
        monitor.forgetZone("airfield");
        assertThat(monitor.isInside("a", "airfield")).isEmpty();
    }
}
