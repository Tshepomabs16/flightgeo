package io.github.tshepomabs16.flightgeo.zones;

/** A change reported by {@link ZoneMonitor}: which subject, which zone, what happened and where. */
public record ZoneEvent<K>(K subject, Zone zone, ZoneTransition transition, GeoPoint position) {}
