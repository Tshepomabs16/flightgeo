package io.github.tshepomabs16.flightgeo.zones;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers which zones each tracked subject (an aircraft, a vehicle) is in and reports only changes,
 * so a subject loitering in a zone raises one event, not one per position. Thread-safe; memory only.
 *
 * @param <K> the type that identifies a subject, such as a String or UUID
 */
public final class ZoneMonitor<K> {

    private final ZoneChecker checker;
    private final Map<K, Map<String, Boolean>> occupancy = new ConcurrentHashMap<>();

    /** A monitor using {@link ZoneChecker#defaults()}. */
    public ZoneMonitor() {
        this(ZoneChecker.defaults());
    }

    /**
     * A monitor using a checker of your own.
     *
     * @param checker decides whether a position is in a zone
     */
    public ZoneMonitor(ZoneChecker checker) {
        this.checker = Objects.requireNonNull(checker, "checker");
    }

    /**
     * Checks a new position against the zones and reports what changed.
     *
     * @param subject the subject that moved
     * @param position its new position
     * @param zones the zones to check it against
     * @return entries, exits and first sightings; empty when nothing changed
     */
    public List<ZoneEvent<K>> update(K subject, GeoPoint position, Collection<? extends Zone> zones) {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(position, "position");
        Map<String, Boolean> state = occupancy.computeIfAbsent(subject, k -> new ConcurrentHashMap<>());
        List<ZoneEvent<K>> events = new ArrayList<>();
        synchronized (state) {
            for (Zone zone : zones) {
                boolean inside = checker.contains(zone, position);
                Boolean before = state.put(zone.id(), inside);
                ZoneTransition change;
                if (before == null) {
                    change = inside ? ZoneTransition.FIRST_SEEN_INSIDE : ZoneTransition.FIRST_SEEN_OUTSIDE;
                } else if (before != inside) {
                    change = inside ? ZoneTransition.ENTERED : ZoneTransition.EXITED;
                } else {
                    continue;
                }
                events.add(new ZoneEvent<>(subject, zone, change, position));
            }
        }
        return events;
    }

    /**
     * Whether a subject was last seen inside a zone.
     *
     * @param subject the subject
     * @param zoneId the zone's id
     * @return true or false, or empty when the subject was never checked against the zone
     */
    public Optional<Boolean> isInside(K subject, String zoneId) {
        Map<String, Boolean> state = occupancy.get(subject);
        return state == null ? Optional.empty() : Optional.ofNullable(state.get(zoneId));
    }

    /**
     * Drops everything remembered about a subject, so its next position counts as a first sighting.
     *
     * @param subject the subject to forget
     */
    public void forget(K subject) {
        occupancy.remove(subject);
    }

    /**
     * Drops a zone from every subject's memory, for example after the zone is deleted or reshaped.
     *
     * @param zoneId the zone's id
     */
    public void forgetZone(String zoneId) {
        occupancy.values().forEach(state -> state.remove(zoneId));
    }
}
