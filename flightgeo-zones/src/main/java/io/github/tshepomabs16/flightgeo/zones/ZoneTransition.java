package io.github.tshepomabs16.flightgeo.zones;

/** What changed for a subject and a zone since its last known position. */
public enum ZoneTransition {
    ENTERED,
    EXITED,
    /** The first position seen for this subject and zone was inside it. */
    FIRST_SEEN_INSIDE,
    /** The first position seen for this subject and zone was outside it. */
    FIRST_SEEN_OUTSIDE
}
