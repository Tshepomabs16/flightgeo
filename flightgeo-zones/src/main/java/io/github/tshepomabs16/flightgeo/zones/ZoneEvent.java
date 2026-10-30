package io.github.tshepomabs16.flightgeo.zones;

/**
 * A change reported by {@link ZoneMonitor}: which subject, which zone, what happened and where.
 *
 * @param <K> the type that identifies a subject
 * @param subject the subject that moved
 * @param zone the zone concerned
 * @param transition what changed
 * @param position the position that caused the change
 */
public record ZoneEvent<K>(K subject, Zone zone, ZoneTransition transition, GeoPoint position) {}
